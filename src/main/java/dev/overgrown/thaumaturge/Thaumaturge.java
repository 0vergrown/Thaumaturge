package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.AspectManager;
import dev.overgrown.thaumaturge.data.CustomItemTagManager;
import dev.overgrown.thaumaturge.data.ModRegistries;
import dev.overgrown.thaumaturge.item.ModItemGroups;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SyncAspectIdentifierPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumaturge implements ModInitializer {
	public static final String MOD_ID = "thaumaturge";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModRegistries.register();
		ModItems.register();
		ModBlocks.register();
		ModItemGroups.register();
		ModComponents.register();

		PayloadTypeRegistry.playS2C().register(SyncAspectIdentifierPacket.ID, SyncAspectIdentifierPacket.PACKET_CODEC);

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SyncAspectIdentifierPacket.ID.id(), (player, joined) -> {
			SyncAspectIdentifierPacket.sendMap(player, AspectManager.NAME_TO_ID);
		});

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("aspects"), AspectManager::new);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("item_aspects"), CustomItemTagManager::new);
	}
}
