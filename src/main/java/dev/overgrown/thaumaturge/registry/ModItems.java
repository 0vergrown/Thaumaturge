package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesItem;
import dev.overgrown.thaumaturge.item.alchemical_sludge_bottle.AlchemicalSludgeBottleItem;
import dev.overgrown.thaumaturge.item.apophenia.ApopheniaItem;
import dev.overgrown.thaumaturge.item.aspect_lens.AspectLensItem;
import dev.overgrown.thaumaturge.item.bonewits_dust.BonewitsDustItem;
import dev.overgrown.thaumaturge.item.focus.AdvancedFocusItem;
import dev.overgrown.thaumaturge.item.focus.GreaterFocusItem;
import dev.overgrown.thaumaturge.item.focus.LesserFocusItem;
import dev.overgrown.thaumaturge.item.gauntlet.AdvancedManipulationGauntletItem;
import dev.overgrown.thaumaturge.item.gauntlet.ArcaneEngineeringGauntletItem;
import dev.overgrown.thaumaturge.item.gauntlet.BasicCastingGauntletItem;
import dev.overgrown.thaumaturge.item.modifier.PowerModifierItem;
import dev.overgrown.thaumaturge.item.modifier.ScatterModifierItem;
import dev.overgrown.thaumaturge.item.modifier.StableModifierItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Thaumaturge.identifier(name), item);
    }

    public static final Item BONEWITS_DUST = registerItem("bonewits_dust",
            new BonewitsDustItem(new FabricItemSettings()
                    .maxCount(64)
            )
    );

    public static final Item APOPHENIA = registerItem("apophenia",
            new ApopheniaItem()
    );

    public static final Item ASPECT_LENS = registerItem("aspect_lens",
            new AspectLensItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    public static final Item AETHERIC_GOGGLES = registerItem("aetheric_goggles",
            new AethericGogglesItem()
    );

    // Gauntlets
    public static final Item BASIC_CASTING_GAUNTLET = registerItem("basic_casting_gauntlet",
            new BasicCastingGauntletItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    public static final Item ADVANCED_MANIPULATION_GAUNTLET = registerItem("advanced_manipulation_gauntlet",
            new AdvancedManipulationGauntletItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    public static final Item ARCANE_ENGINEERING_GAUNTLET = registerItem("arcane_engineering_gauntlet",
            new ArcaneEngineeringGauntletItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    // Foci
    public static final Item LESSER_FOCUS = registerItem("lesser_focus",
            new LesserFocusItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    public static final Item ADVANCED_FOCUS = registerItem("advanced_focus",
            new AdvancedFocusItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    public static final Item GREATER_FOCUS = registerItem("greater_focus",
            new GreaterFocusItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    // Modifiers
    public static final Item POWER_MODIFIER = registerItem("power_modifier",
            new PowerModifierItem(new FabricItemSettings()
                    .maxCount(16)
            )
    );

    public static final Item SCATTER_MODIFIER = registerItem("scatter_modifier",
            new ScatterModifierItem(new FabricItemSettings()
                    .maxCount(16)
            )
    );

    public static final Item STABLE_MODIFIER = registerItem("stable_modifier",
            new StableModifierItem(new FabricItemSettings()
                    .maxCount(16)
            )
    );

    public static final Item ALCHEMICAL_SLUDGE_BOTTLE = registerItem("alchemical_sludge_bottle",
            new AlchemicalSludgeBottleItem());

    // Block Items
    public static final Item VESSEL = new BlockItem(ModBlocks.VESSEL, new FabricItemSettings());
    public static final Item FAUCET = new BlockItem(ModBlocks.FAUCET, new FabricItemSettings());
    public static final Item JAR = new BlockItem(ModBlocks.JAR, new FabricItemSettings());
    public static final Item ALCHEMICAL_FURNACE = new BlockItem(ModBlocks.ALCHEMICAL_FURNACE, new FabricItemSettings());
    public static final Item FOCAL_MANIPULATOR = new BlockItem(ModBlocks.FOCAL_MANIPULATOR, new FabricItemSettings());

    // Aspect Cluster Block Items
    public static final Item AER_CLUSTER = new BlockItem(ModBlocks.AER_CLUSTER,
            new FabricItemSettings()
                    .maxCount(16)
    );

    public static final Item AQUA_CLUSTER = new BlockItem(ModBlocks.AQUA_CLUSTER,
            new FabricItemSettings()
                    .maxCount(16)
    );

    public static final Item IGNIS_CLUSTER = new BlockItem(ModBlocks.IGNIS_CLUSTER,
            new FabricItemSettings()
                    .maxCount(16)
    );

    public static final Item TERRA_CLUSTER = new BlockItem(ModBlocks.TERRA_CLUSTER,
            new FabricItemSettings()
                    .maxCount(16)
    );

    public static final Item ORDO_CLUSTER = new BlockItem(ModBlocks.ORDO_CLUSTER,
            new FabricItemSettings()
                    .maxCount(16)
    );

    public static final Item PERDITIO_CLUSTER = new BlockItem(ModBlocks.PERDITIO_CLUSTER,
            new FabricItemSettings()
                    .maxCount(16)
    );

    public static Item getModifierItem(String modifierType) {
        return switch (modifierType) {
            case "power" -> POWER_MODIFIER;
            case "scatter" -> SCATTER_MODIFIER;
            case "stable" -> STABLE_MODIFIER;
            default -> null;
        };
    }

    public static void initialize() {
        // Register block items
        Registry.register(Registries.ITEM, Thaumaturge.identifier("vessel"), VESSEL);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("faucet"), FAUCET);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("jar"), JAR);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("alchemical_furnace"), ALCHEMICAL_FURNACE);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("focal_manipulator"), FOCAL_MANIPULATOR);


        // Register Aspect Cluster block items
        Registry.register(Registries.ITEM, Thaumaturge.identifier("aer_cluster"), AER_CLUSTER);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("aqua_cluster"), AQUA_CLUSTER);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("ignis_cluster"), IGNIS_CLUSTER);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("terra_cluster"), TERRA_CLUSTER);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("ordo_cluster"), ORDO_CLUSTER);
        Registry.register(Registries.ITEM, Thaumaturge.identifier("perditio_cluster"), PERDITIO_CLUSTER);
    }
}