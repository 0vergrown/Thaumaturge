/**
 * Thaumaturge.java
 * <p>
 * Main mod class for Thaumaturge - handles server-side initialization,
 * registry setup, recipe registration, spell system initialization,
 * and packet handling.
 */
package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.block.ModBlockEntities;
import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.data.AspectManager;
import dev.overgrown.thaumaturge.data.CustomItemTagManager;
import dev.overgrown.thaumaturge.data.ModRegistries;
import dev.overgrown.thaumaturge.event.ModEvents;
import dev.overgrown.thaumaturge.item.ModItemGroups;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.networking.SyncAspectIdentifierPacket;
import dev.overgrown.thaumaturge.predicate.component.ModComponentPredicateTypes;
import dev.overgrown.thaumaturge.recipe.Recipe;
import dev.overgrown.thaumaturge.spell.SpellHandler;
import dev.overgrown.thaumaturge.spell.SpellRegistry;
import dev.overgrown.thaumaturge.spell.combination.AerMotusCombination;
import dev.overgrown.thaumaturge.spell.impl.aer.AdvancedAerLaunch;
import dev.overgrown.thaumaturge.spell.impl.aer.GreaterAerBurst;
import dev.overgrown.thaumaturge.spell.impl.aer.LesserAerBoost;
import dev.overgrown.thaumaturge.spell.impl.motus.LesserMotusBoost;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Thaumaturge implements ModInitializer {
	public static final String MOD_ID = "thaumaturge";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Helper method to create Identifiers with the mod's namespace
	 *
	 * @param path The path for the identifier
	 * @return A new Identifier with the mod's namespace and the provided path
	 */
	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// Register all mod components, items, blocks, etc.
		ModRegistries.register();
		ModItems.register();
		ModBlocks.register();
		ModItemGroups.register();
		ModComponents.register();
		ModEvents.register();
		ModComponentPredicateTypes.register();
		registerRecipes();
		ModBlockEntities.register();
		registerSpells();

		// Register networking packets
		// For syncing aspect identifiers from server to client
		PayloadTypeRegistry.playS2C().register(SyncAspectIdentifierPacket.ID, SyncAspectIdentifierPacket.PACKET_CODEC);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SyncAspectIdentifierPacket.ID.id(), (player, joined) -> SyncAspectIdentifierPacket.sendMap(player, AspectManager.NAME_TO_ID));

		// Register resource reload listeners for loading aspect data from datapacks
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("aspects"), AspectManager::new);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("item_aspects"), CustomItemTagManager::new);

		// Register spell cast packet handling
		PayloadTypeRegistry.playC2S().register(SpellCastPacket.ID, SpellCastPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SpellCastPacket.ID, SpellCastPacket.PACKET_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SpellCastPacket.ID, (packet, context) -> handleSpellCast(packet, context.player()));
	}

	/**
	 * Registers all available spells and spell combinations in the mod
	 * Each spell has a unique identifier and implementation
	 */
	private void registerSpells() {
		// Register individual spells
		SpellRegistry.registerSpell(LesserAerBoost.ID, new LesserAerBoost());
		SpellRegistry.registerSpell(AdvancedAerLaunch.ID, new AdvancedAerLaunch());
		SpellRegistry.registerSpell(GreaterAerBurst.ID, new GreaterAerBurst());
		SpellRegistry.registerSpell(LesserMotusBoost.ID, new LesserMotusBoost());

		// Register spell combinations
		// When both Aer and Motus foci are equipped, they can create a special combined spell
		Set<Identifier> aerMotusCombo = Set.of(
				Registries.ITEM.getId(ModItems.LESSER_AER_FOCI),
				Registries.ITEM.getId(ModItems.LESSER_MOTUS_FOCI)
		);
		SpellRegistry.registerCombination(aerMotusCombo, new AerMotusCombination());
	}

	/**
	 * Handles spell cast packets received from clients
	 *
	 * @param packet The spell cast packet containing the type of spell to cast
	 * @param player The player casting the spell
	 */
	private void handleSpellCast(SpellCastPacket packet, ServerPlayerEntity player) {
		SpellHandler.tryCastSpell(player, packet.type().getSpellId());
	}

	private void registerRecipes() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			RegistryWrapper.WrapperLookup registries = server.getRegistryManager();
			RegistryWrapper.Impl<Aspect> aspectsRegistry = registries.getOrThrow(ModRegistries.ASPECTS);

			RegistryKey<Aspect> praecantatioKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("praecantatio"));
			RegistryKey<Aspect> vitreusKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("vitreus"));
			RegistryKey<Aspect> auramKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("auram"));
			RegistryKey<Aspect> aerKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("aer"));

			RegistryEntry.Reference<Aspect> praecantatio = aspectsRegistry.getOptional(praecantatioKey)
					.orElseThrow(() -> new IllegalStateException("Praecantatio aspect not found"));
			RegistryEntry.Reference<Aspect> vitreus = aspectsRegistry.getOptional(vitreusKey)
					.orElseThrow(() -> new IllegalStateException("Vitreus aspect not found"));
			RegistryEntry.Reference<Aspect> auram = aspectsRegistry.getOptional(auramKey)
					.orElseThrow(() -> new IllegalStateException("Auram aspect not found"));
			RegistryEntry.Reference<Aspect> aer = aspectsRegistry.getOptional(aerKey)
					.orElseThrow(() -> new IllegalStateException("Aer aspect not found"));

			new Recipe.Builder()
					.catalyst(ModItems.ORDO_ASPECT_SHARD)
					.requiresFluid(VesselBlock.FluidType.WATER, 3)
					.requires(praecantatio, 10)
					.requires(vitreus, 20)
					.requires(auram, 5)
					.output(new ItemStack(ModItems.LESSER_FOCI))
					.register();

			new Recipe.Builder()
					.catalyst(Items.AMETHYST_SHARD)
					.requires(aer, 1)
					.output(new ItemStack(ModItems.AER_ASPECT_SHARD))
					.register();
		});
	}
}
