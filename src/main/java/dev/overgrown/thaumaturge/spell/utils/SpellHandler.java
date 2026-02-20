package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.aspectslib.aether.AetherAPI;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.pattern.SpellPattern;
import dev.overgrown.thaumaturge.spell.tier.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public final class SpellHandler {

    private SpellHandler() {}

    public static boolean cast(ServerPlayerEntity player, Object delivery, String keyType) {
        SpellPattern pattern = resolvePattern(player, keyType);
        if (pattern == null || pattern.getAspects().isEmpty()) return false;

        // Calculate spell cost from aspects
        AspectData spellCost = calculateSpellCost(pattern);

        // NEW: Check if we can cast the spell (enough TOTAL Aether, not aspect-specific)
        BlockPos castPos = player.getBlockPos();
        if (!hasSufficientAether(player.getWorld(), castPos, spellCost)) {
            // Failed cast - chance to create dead zone
            handleFailedCast(player, castPos, spellCost);
            return false;
        }

        // NEW: Consume Aether for the spell (total RU consumption)
        if (!consumeAetherForSpell(player.getWorld(), castPos, spellCost)) {
            handleFailedCast(player, castPos, spellCost);
            return false;
        }

        // NEW: Calculate environmental resonance effects
        EnvironmentalResonance resonance = calculateEnvironmentalResonance(player.getWorld(), castPos, pattern);

        // Apply spell effects with resonance modifications
        return applySpellEffects(player, delivery, pattern, resonance);
    }

    /**
     * NEW: Check if there's sufficient total Aether (not aspect-specific)
     */
    private static boolean hasSufficientAether(net.minecraft.world.World world, BlockPos pos, AspectData cost) {
        // Calculate total RU needed
        double totalCost = cost.calculateTotalRU();

        // Get total available Aether in the chunk (sum of all aspects)
        double totalAvailableAether = calculateTotalAvailableAether(world, pos);

        return totalAvailableAether >= totalCost;
    }

    /**
     * NEW: Calculate total available Aether from all aspects in the environment
     */
    private static double calculateTotalAvailableAether(net.minecraft.world.World world, BlockPos pos) {
        if (AetherAPI.isDeadZone(world, pos)) {
            return 0;
        }

        double total = 0;
        net.minecraft.util.math.ChunkPos chunkPos = new net.minecraft.util.math.ChunkPos(pos);

        // Sum Aether from all aspects in the chunk
        var aetherData = dev.overgrown.aspectslib.aether.AetherManager.getAetherData(world, chunkPos);
        for (Identifier aspectId : aetherData.getAspectIds()) {
            total += aetherData.getCurrentAether(aspectId);
        }

        return total;
    }

    /**
     * NEW: Consume Aether proportionally from all available aspects
     */
    private static boolean consumeAetherForSpell(net.minecraft.world.World world, BlockPos pos, AspectData cost) {
        if (AetherAPI.isDeadZone(world, pos)) {
            return false;
        }

        double totalCost = cost.calculateTotalRU();
        net.minecraft.util.math.ChunkPos chunkPos = new net.minecraft.util.math.ChunkPos(pos);
        var aetherData = dev.overgrown.aspectslib.aether.AetherManager.getAetherData(world, chunkPos);

        // Calculate total available Aether
        double totalAvailable = calculateTotalAvailableAether(world, pos);
        if (totalAvailable < totalCost) {
            return false;
        }

        // Consume proportionally from all aspects
        boolean success = true;
        for (Identifier aspectId : aetherData.getAspectIds()) {
            double aspectRatio = aetherData.getCurrentAether(aspectId) / totalAvailable;
            int aspectCost = (int) Math.ceil(totalCost * aspectRatio);

            if (aetherData.getCurrentAether(aspectId) >= aspectCost) {
                aetherData.harvestAether(aspectId, aspectCost);
            } else {
                // If one aspect doesn't have enough, try to compensate from others
                success = false;
                break;
            }
        }

        // If proportional consumption failed, try sequential consumption
        if (!success) {
            return consumeAetherSequentially(world, pos, (int) totalCost);
        }

        return true;
    }

    /**
     * NEW: Consume Aether sequentially when proportional fails
     */
    private static boolean consumeAetherSequentially(net.minecraft.world.World world, BlockPos pos, int totalCost) {
        net.minecraft.util.math.ChunkPos chunkPos = new net.minecraft.util.math.ChunkPos(pos);
        var aetherData = dev.overgrown.aspectslib.aether.AetherManager.getAetherData(world, chunkPos);

        int remainingCost = totalCost;

        // Try to consume from each aspect until cost is met
        for (Identifier aspectId : aetherData.getAspectIds()) {
            int available = aetherData.getCurrentAether(aspectId);
            int toConsume = Math.min(available, remainingCost);

            if (toConsume > 0 && aetherData.harvestAether(aspectId, toConsume)) {
                remainingCost -= toConsume;
            }

            if (remainingCost <= 0) {
                break;
            }
        }

        return remainingCost <= 0;
    }

    /**
     * NEW: Calculate environmental resonance effects
     */
    private static EnvironmentalResonance calculateEnvironmentalResonance(net.minecraft.world.World world, BlockPos pos, SpellPattern pattern) {
        EnvironmentalResonance resonance = new EnvironmentalResonance();
        net.minecraft.util.math.ChunkPos chunkPos = new net.minecraft.util.math.ChunkPos(pos);
        var aetherData = dev.overgrown.aspectslib.aether.AetherManager.getAetherData(world, chunkPos);

        // Check each aspect in the spell against environmental aspects
        for (Identifier spellAspect : pattern.getAspects().keySet()) {
            for (Identifier envAspect : aetherData.getAspectIds()) {
                if (aetherData.getCurrentAether(envAspect) > 0) {
                    checkResonanceInteraction(spellAspect, envAspect, resonance);
                }
            }
        }

        return resonance;
    }

    /**
     * NEW: Check for resonance interactions between spell and environmental aspects
     */
    private static void checkResonanceInteraction(Identifier spellAspect, Identifier envAspect, EnvironmentalResonance resonance) {
        // Get resonance relationships
        List<dev.overgrown.aspectslib.resonance.Resonance> resonances =
                dev.overgrown.aspectslib.resonance.ResonanceManager.RESONANCE_MAP.getOrDefault(
                        spellAspect, Collections.emptyList()
                );

        for (dev.overgrown.aspectslib.resonance.Resonance res : resonances) {
            if (res.matches(spellAspect, envAspect)) {
                if (res.type() == dev.overgrown.aspectslib.resonance.Resonance.Type.OPPOSING) {
                    resonance.addOpposingEffect(spellAspect, envAspect, res.factor());
                } else if (res.type() == dev.overgrown.aspectslib.resonance.Resonance.Type.AMPLIFYING) {
                    resonance.addAmplifyingEffect(spellAspect, envAspect, res.factor());
                }
            }
        }
    }

    /**
     * NEW: Apply spell effects with resonance modifications
     */
    private static boolean applySpellEffects(ServerPlayerEntity player, Object delivery, SpellPattern pattern, EnvironmentalResonance resonance) {
        boolean hasVinculum = pattern.getAspects().keySet().stream()
                .anyMatch(id -> id.getPath().equals("vinculum"));

        boolean spellApplied = false;

        for (Map.Entry<Identifier, Identifier> entry : pattern.getAspects().entrySet()) {
            AspectEffect aspect = resolveAspect(entry.getKey());
            ModifierEffect modifier = resolveModifier(entry.getValue());
            if (aspect == null) continue;

            List<ModifierEffect> mods = modifier != null ?
                    Collections.singletonList(modifier) : Collections.emptyList();

            // Apply resonance modifications to modifiers
            mods = resonance.applyToModifiers(entry.getKey(), mods);

            // If this pattern has Vinculum, only apply Vinculum and skip other aspects
            if (hasVinculum && !entry.getKey().getPath().equals("vinculum")) {
                continue;
            }

            // Apply the effect with resonance context
            if (applyEffectWithResonance(delivery, aspect, mods, resonance, entry.getKey())) {
                spellApplied = true;
            }
        }

        return spellApplied;
    }

    /**
     * NEW: Apply individual effect with resonance context
     */
    private static boolean applyEffectWithResonance(Object delivery, AspectEffect aspect,
                                                    List<ModifierEffect> mods,
                                                    EnvironmentalResonance resonance,
                                                    Identifier aspectId) {
        try {
            if (delivery instanceof SelfSpellDelivery selfDelivery) {
                selfDelivery.setModifiers(mods);
                selfDelivery.setResonance(resonance.getResonanceForAspect(aspectId));
                aspect.applySelf(selfDelivery);
                return true;
            }
            else if (delivery instanceof TargetedSpellDelivery targetedDelivery) {
                targetedDelivery.setModifiers(mods);
                targetedDelivery.setResonance(resonance.getResonanceForAspect(aspectId));
                aspect.applyTargeted(targetedDelivery);
                return true;
            }
            else if (delivery instanceof AoeSpellDelivery aoeDelivery) {
                aoeDelivery.setModifiers(mods);
                aoeDelivery.setResonance(resonance.getResonanceForAspect(aspectId));
                aspect.applyAoe(aoeDelivery);
                return true;
            }
        } catch (Exception e) {
            dev.overgrown.thaumaturge.Thaumaturge.LOGGER.error("Failed to apply spell effect {}: {}", aspectId, e.getMessage());
        }

        return false;
    }

    private static AspectData calculateSpellCost(SpellPattern pattern) {
        AspectData.Builder costBuilder = new AspectData.Builder(AspectData.DEFAULT);

        for (Identifier aspectId : pattern.getAspects().keySet()) {
            int baseCost = 1 * pattern.getAmplifier();
            costBuilder.add(aspectId, baseCost);
        }

        return costBuilder.build();
    }

    private static void handleFailedCast(ServerPlayerEntity player, BlockPos castPos, AspectData attemptedCost) {
        double totalCost = attemptedCost.calculateTotalRU();
        double deadZoneChance = Math.min(0.3, totalCost * 0.05);

        if (player.getWorld().random.nextDouble() < deadZoneChance) {
            boolean permanent = totalCost > 10 && player.getWorld().random.nextDouble() < 0.2;

            if (permanent) {
                AetherAPI.createPermanentDeadZone(player.getWorld(), castPos);
            } else {
                AetherAPI.createTemporaryDeadZone(player.getWorld(), castPos);
            }

            player.sendMessage(net.minecraft.text.Text.literal("§cThe spell backfires! A dead zone forms."), false);
        } else {
            player.sendMessage(net.minecraft.text.Text.literal("§cNot enough Aether to cast this spell."), false);
        }
    }

    private static SpellPattern resolvePattern(ServerPlayerEntity player, String keyType) {
        ItemStack gauntlet = findGauntlet(player);
        if (gauntlet.isEmpty()) return null;

        String tier = switch (keyType) {
            case "primary" -> "lesser";
            case "secondary" -> "advanced";
            case "ternary" -> "greater";
            default -> null;
        };

        return tier != null ? SpellPattern.fromGauntlet(gauntlet, tier) : null;
    }

    private static ModifierEffect resolveModifier(Identifier modifierId) {
        if (modifierId == null) return null;
        return ModifierRegistry.get(modifierId);
    }

    public static ItemStack findGauntlet(PlayerEntity player) {
        // Check hands first
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof ResonanceGauntletItem) {
                return stack;
            }
        }

        // Check equipment slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.getItem() instanceof ResonanceGauntletItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    // === Helpers ===
    private static AspectEffect resolveAspect(Identifier aspectId) {
        if (aspectId == null) return null;
        return AspectRegistry.get(aspectId).orElse(null);
    }
}