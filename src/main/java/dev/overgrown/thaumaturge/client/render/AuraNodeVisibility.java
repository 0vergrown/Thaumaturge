package dev.overgrown.thaumaturge.client.render;

import dev.overgrown.aspectslib.aspects.api.AspectsAPI;
import dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesItem;

public class AuraNodeVisibility {

    public static void initialize() {
        // Add condition to show aura nodes when wearing Aetheric Goggles
        AspectsAPI.addAuraNodeVisibilityCondition((player, hasAspects) -> {
            return AethericGogglesItem.isWearingGoggles(player);
        });
    }
}