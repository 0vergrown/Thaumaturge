package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.entity.ModEntities;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.pattern.ModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SpellHandler {
    private static final Identifier POTENTIA_ID = Thaumaturge.identifier("potentia");

    public static void tryCastSpell(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        List<GauntletComponent.FociEntry> entries = getEquippedFociEntries(player, tier);
        if (entries.isEmpty()) return;

        // Check if any entry has the Potentia aspect
        boolean hasPotentia = entries.stream()
                .anyMatch(entry -> entry.aspectId().equals(POTENTIA_ID));

        if (hasPotentia) {
            // Create and shoot the spell bolt
            SpellBoltEntity bolt = new SpellBoltEntity(ModEntities.SPELL_BOLT, player.getWorld());
            bolt.setCaster(player);

            // Hitbox at the middle of the lightning
            Vec3d hitbox = player.getEyePos().add(player.getRotationVector().multiply(SpellBoltEntity.LENGTH / 2));

            bolt.setPosition(hitbox);

            bolt.setYaw(player.getHeadYaw());
            bolt.setPitch(player.getPitch());

            /*
            Vec3d direction = player.getRotationVector().normalize();
            Vec3d spawnPos = player.getEyePos().add(direction.multiply(0.2));
            bolt.setPosition(spawnPos);
            bolt.setVelocity(
                    direction.x * 1.5,
                    direction.y * 1.5,
                    direction.z * 1.5
            );
            bolt.setVelocity(direction.multiply(1.5));
            ProjectileUtil.setRotationFromVelocity(bolt, 1.0f);
             */
            bolt.setTier(tier.ordinal());

            // Collect effects from other aspects and modifiers
            TargetedSpellDelivery dummyDelivery = new TargetedSpellDelivery();
            dummyDelivery.setCaster(player); // Set the caster here

            for (GauntletComponent.FociEntry entry : entries) {
                if (entry.aspectId().equals(POTENTIA_ID)) continue;

                AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId());
                ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

                if (aspectEffect != null) aspectEffect.apply(dummyDelivery);
                if (modifierEffect != null) modifierEffect.apply(dummyDelivery);
            }

            // Attach the collected effects to the bolt
            bolt.setOnHitEffects(dummyDelivery.getOnHitEffects());
            player.getWorld().spawnEntity(bolt);
        } else {
            // Proceed with regular spell delivery
            Object delivery = createDelivery(tier);
            for (GauntletComponent.FociEntry entry : entries) {
                AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId());
                ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

                if (aspectEffect != null) applyEffect(delivery, aspectEffect);
                if (modifierEffect != null) applyEffect(delivery, modifierEffect);
            }
            executeDelivery(delivery, player);
        }
    }

    private static Object createDelivery(SpellCastPacket.SpellTier tier) {
        return switch (tier) {
            case LESSER -> new SelfSpellDelivery();
            case ADVANCED -> new TargetedSpellDelivery();
            case GREATER -> new AoeSpellDelivery();
        };
    }

    private static void applyEffect(Object delivery, Object effect) {
        if (delivery instanceof SelfSpellDelivery self) {
            if (effect instanceof AspectEffect ae) ae.apply(self);
            if (effect instanceof ModifierEffect me) me.apply(self);
        } else if (delivery instanceof TargetedSpellDelivery targeted) {
            if (effect instanceof AspectEffect ae) ae.apply(targeted);
            if (effect instanceof ModifierEffect me) me.apply(targeted);
        } else if (delivery instanceof AoeSpellDelivery aoe) {
            if (effect instanceof AspectEffect ae) ae.apply(aoe);
            if (effect instanceof ModifierEffect me) me.apply(aoe);
        }
    }

    private static void executeDelivery(Object delivery, ServerPlayerEntity player) {
        if (delivery instanceof SelfSpellDelivery self) self.execute(player);
        else if (delivery instanceof TargetedSpellDelivery targeted) targeted.execute(player);
        else if (delivery instanceof AoeSpellDelivery aoe) aoe.execute(player);
    }

    public static List<GauntletComponent.FociEntry> getEquippedFociEntries(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        List<GauntletComponent.FociEntry> entries = new ArrayList<>();
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.contains(ModComponents.MAX_FOCI)) {
                GauntletComponent component = stack.get(ModComponents.GAUNTLET_STATE);
                if (component != null) {
                    entries.addAll(component.entries().stream()
                            // TODO: .filter(e -> e.tier().equals(tier))
                            .toList());
                }
            }
        }
        return entries;
    }

    public static SpellCastPacket.SpellTier getFociTier(Item item) {
        if (item == ModItems.LESSER_FOCI) return SpellCastPacket.SpellTier.LESSER;
        if (item == ModItems.ADVANCED_FOCI) return SpellCastPacket.SpellTier.ADVANCED;
        if (item == ModItems.GREATER_FOCI) return SpellCastPacket.SpellTier.GREATER;
        return null;
    }

    public static void castPotentiaSpell(PlayerEntity caster, int tier) {
        if(hasPotentiaFoci(caster)) {
            SpellBoltEntity bolt = new SpellBoltEntity(ModEntities.SPELL_BOLT, caster.getWorld());
            bolt.setCaster(caster); // Corrected variable name
            bolt.setPosition(caster.getEyePos());
            Vec3d rotation = caster.getRotationVector().normalize();
            bolt.setVelocity(rotation.multiply(1.5));
            bolt.setTier(tier);
            caster.getWorld().spawnEntity(bolt);
        }
    }

    private static boolean hasPotentiaFoci(PlayerEntity player) {
        // Check gauntlet for Potentia aspect
        return true;
    }
}