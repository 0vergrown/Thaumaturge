package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.matrix.MatrixBlock;
import dev.overgrown.thaumaturge.block.pedestal.PedestalBlock;
import dev.overgrown.thaumaturge.block.pedestal.PedestalBlockEntity;
import dev.overgrown.thaumaturge.block.alchemical_furnace.AlchemicalFurnaceBlock;
import dev.overgrown.thaumaturge.block.alchemical_furnace.AlchemicalFurnaceBlockEntity;
import dev.overgrown.thaumaturge.block.aspect_clusters.*;
import dev.overgrown.thaumaturge.block.faucet.FaucetBlock;
import dev.overgrown.thaumaturge.block.faucet.FaucetBlockEntity;
import dev.overgrown.thaumaturge.block.focal_manipulator.FocalManipulatorBlock;
import dev.overgrown.thaumaturge.block.focal_manipulator.FocalManipulatorBlockEntity;
import dev.overgrown.thaumaturge.block.jar.JarBlock;
import dev.overgrown.thaumaturge.block.jar.JarBlockEntity;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.block.vessel.VesselBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public class ModBlocks {
    public static final Block VESSEL = new VesselBlock(FabricBlockSettings.copyOf(Blocks.CAULDRON));
    public static final BlockEntityType<VesselBlockEntity> VESSEL_BLOCK_ENTITY =
            FabricBlockEntityTypeBuilder.create(VesselBlockEntity::new, VESSEL).build();

    // Aspect Clusters
    public static final Block AER_CLUSTER = new AerClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block AQUA_CLUSTER = new AquaClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block IGNIS_CLUSTER = new IgnisClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block TERRA_CLUSTER = new TerraClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block ORDO_CLUSTER = new OrdoClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block PERDITIO_CLUSTER = new PerditioClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    
    // Faucet
    public static final Block FAUCET = new FaucetBlock(FabricBlockSettings.create().nonOpaque().notSolid().hardness(1f).resistance(1f).mapColor(MapColor.BLACK).pistonBehavior(PistonBehavior.BLOCK));
    public static final BlockEntityType<FaucetBlockEntity> FAUCET_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(FaucetBlockEntity::new, FAUCET).build();

    // Jar
    public static final Block JAR = new JarBlock(FabricBlockSettings.create().nonOpaque().notSolid().hardness(1f).resistance(1f).pistonBehavior(PistonBehavior.BLOCK).mapColor(MapColor.BROWN).sounds(BlockSoundGroup.GLASS));
    public static final BlockEntityType<JarBlockEntity> JAR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(JarBlockEntity::new, JAR).build();

    // Alchemical Furnace
    public static final Block ALCHEMICAL_FURNACE = new AlchemicalFurnaceBlock(FabricBlockSettings.copyOf(Blocks.BLAST_FURNACE));
    public static final BlockEntityType<AlchemicalFurnaceBlockEntity> ALCHEMICAL_FURNACE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(AlchemicalFurnaceBlockEntity::new, ALCHEMICAL_FURNACE).build();

    public static final Block MATRIX = new MatrixBlock(AbstractBlock.Settings.create());
    public static final Block PEDESTAL = new PedestalBlock(AbstractBlock.Settings.create());
    public static final BlockEntityType<PedestalBlockEntity> PEDESTAL_BE = BlockEntityType.Builder.create(PedestalBlockEntity::new,PEDESTAL).build(null);

    public static final Block FOCAL_MANIPULATOR = new FocalManipulatorBlock(
            FabricBlockSettings.copyOf(Blocks.STONE).nonOpaque()
    );
    public static final BlockEntityType<FocalManipulatorBlockEntity> FOCAL_MANIPULATOR_BLOCK_ENTITY =
            FabricBlockEntityTypeBuilder.create(FocalManipulatorBlockEntity::new, FOCAL_MANIPULATOR).build();

    public static void initialize() {
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("vessel"), VESSEL);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Thaumaturge.identifier("vessel"), VESSEL_BLOCK_ENTITY);

        // Register Aspect Clusters
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("aer_cluster"), AER_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("aqua_cluster"), AQUA_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("ignis_cluster"), IGNIS_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("terra_cluster"), TERRA_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("ordo_cluster"), ORDO_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("perditio_cluster"), PERDITIO_CLUSTER);

        // Register Alchemical Blocks
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("faucet"), FAUCET);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Thaumaturge.identifier("faucet"), FAUCET_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("jar"), JAR);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Thaumaturge.identifier("jar"), JAR_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("alchemical_furnace"), ALCHEMICAL_FURNACE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Thaumaturge.identifier("alchemical_furnace"), ALCHEMICAL_FURNACE_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK,Thaumaturge.identifier("matrix"),MATRIX);
        Registry.register(Registries.BLOCK,Thaumaturge.identifier("pedestal"),PEDESTAL);
        Registry.register(Registries.BLOCK_ENTITY_TYPE,Thaumaturge.identifier("pedestal"),PEDESTAL_BE);


        Registry.register(Registries.BLOCK, Thaumaturge.identifier("focal_manipulator"), FOCAL_MANIPULATOR);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Thaumaturge.identifier("focal_manipulator"), FOCAL_MANIPULATOR_BLOCK_ENTITY);
    }
}