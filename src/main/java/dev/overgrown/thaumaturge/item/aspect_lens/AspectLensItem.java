package dev.overgrown.thaumaturge.item.aspect_lens;

import dev.overgrown.aspectslib.aether.AetherChunkData;
import dev.overgrown.aspectslib.aether.AetherManager;
import dev.overgrown.aspectslib.aspects.data.BiomeAspectModifier;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AspectLensItem extends Item {
    private static final Identifier VITIUM_ASPECT = new Identifier("aspectslib", "vitium");

    public AspectLensItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.thaumaturge.aspect_lens.tooltip").formatted(Formatting.GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            // Get player position and biome information
            BlockPos pos = user.getBlockPos();
            ChunkPos chunkPos = new ChunkPos(pos); // For chunk-based aether data

            // Get current aether data from AetherManager
            AetherChunkData aetherData = AetherManager.getAetherData(world, chunkPos);

            // Get biome ID for modifications
            Identifier biomeId = world.getBiome(pos).getKey().orElseThrow().getValue();

            // Get modified biome aspects instead of dynamic modifications
            var combinedBiomeAspects = BiomeAspectModifier.getCombinedBiomeAspects(biomeId);

            // Calculate vitium and total aspects from combined biome data
            double vitium = combinedBiomeAspects.getLevel(VITIUM_ASPECT);
            double totalOtherAspects = 0.0;

            for (Object2IntMap.Entry<Identifier> entry : combinedBiomeAspects.getMap().object2IntEntrySet()) {
                if (!entry.getKey().equals(VITIUM_ASPECT)) {
                    totalOtherAspects += entry.getIntValue(); // Use getIntValue() for primitive int
                }
            }

            // Send information to player
            user.sendMessage(Text.literal("=== Aether Density Report ===").formatted(Formatting.GOLD));
            user.sendMessage(Text.literal("Biome: " + biomeId));
            user.sendMessage(Text.literal("Chunk: " + chunkPos));

            // Display all aspects from combined biome data
            for (Object2IntMap.Entry<Identifier> entry : combinedBiomeAspects.getMap().object2IntEntrySet()) {
                Formatting color = entry.getKey().equals(VITIUM_ASPECT) ? Formatting.RED : Formatting.GREEN;
                user.sendMessage(Text.literal(
                        String.format("%s: %.2f", entry.getKey().getPath(), (double) entry.getIntValue())
                ).formatted(color));
            }

            // Display chunk aether information
            user.sendMessage(Text.literal("---").formatted(Formatting.GRAY));
            user.sendMessage(Text.literal("Chunk Aether Status:").formatted(Formatting.BLUE));

            for (Identifier aspectId : aetherData.getAspectIds()) {
                int current = aetherData.getCurrentAether(aspectId);
                int max = aetherData.getMaxAether(aspectId);
                double percentage = aetherData.getAetherPercentage(aspectId);
                Formatting color = percentage > 0.7 ? Formatting.GREEN :
                        percentage > 0.3 ? Formatting.YELLOW : Formatting.RED;

                user.sendMessage(Text.literal(
                        String.format("  %s: %d/%d (%.1f%%)",
                                aspectId.getPath(), current, max, percentage * 100)
                ).formatted(color));
            }

            // Display corruption status
            user.sendMessage(Text.literal("---").formatted(Formatting.GRAY));
            user.sendMessage(Text.literal(String.format("Vitium (Corruption): %.2f", vitium)).formatted(Formatting.RED));
            user.sendMessage(Text.literal(String.format("Total Other Aspects: %.2f", totalOtherAspects)).formatted(Formatting.GREEN));

            // Display corruption dominance
            if (vitium > totalOtherAspects) {
                user.sendMessage(Text.literal("Status: CORRUPTED").formatted(Formatting.DARK_RED));
            } else {
                user.sendMessage(Text.literal("Status: PURE").formatted(Formatting.DARK_GREEN));
            }

            // Display dead zone information
            if (AetherManager.isDeadZone(world, chunkPos)) {
                user.sendMessage(Text.literal("---").formatted(Formatting.GRAY));
                user.sendMessage(Text.literal("⚠ DEAD ZONE DETECTED!").formatted(Formatting.DARK_RED));
                user.sendMessage(Text.literal("Aether regeneration disabled in this chunk").formatted(Formatting.RED));
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    public static boolean hasLens(PlayerEntity player) {
        if (player == null) return false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof AspectLensItem) {
                return true;
            }
        }
        return false;
    }
}