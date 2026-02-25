package dev.overgrown.thaumaturge.item.aetheric_goggles.overlay;

import dev.overgrown.aspectslib.aspects.api.IAspectAffinityEntity;
import dev.overgrown.aspectslib.aspects.data.Aspect;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.aspectslib.aspects.data.ModRegistries;
import dev.overgrown.aspectslib.entity.aura_node.AuraNodeEntity;
import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.Set;

public class AethericGogglesOverlay implements HudRenderCallback {
    private static final int ASPECT_ICON_SIZE = 16;
    private static final int ASPECT_TEXT_OFFSET = 18;
    private static final int ASPECT_SPACING = 4;
    private static final int CROSSHAIR_OFFSET_Y = -30;
    private static final int TEXT_Y_OFFSET = 5;

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Only render if wearing goggles
        if (!AethericGogglesItem.isWearingGoggles(client.player)) return;

        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() == HitResult.Type.MISS) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth / 2;
        int y = screenHeight / 2 + CROSSHAIR_OFFSET_Y;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.world.getBlockState(pos);

            if (client.world.getBlockEntity(pos) instanceof AspectContainer container) {
                if (!container.getAspects().isEmpty()) {
                    renderBlockEntityAspects(drawContext, container, x, y);
                    return;
                }
            }
        } else if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            Entity entity = entityHit.getEntity();

            // Handle Aura Node entities specifically
            if (entity instanceof AuraNodeEntity auraNode) {
                renderAuraNodeAspects(drawContext, auraNode, x, y);
                return;
            }
        }

        AspectData aspectData = getAspectDataForTarget(client, hit);
        if (aspectData != null && !aspectData.isEmpty()) {
            renderAspectData(drawContext, aspectData, x, y);
        }
    }

    private AspectData getAspectDataForTarget(MinecraftClient client, HitResult hit) {
        if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            Entity entity = entityHit.getEntity();

            if (entity instanceof LivingEntity livingEntity) {
                // Handle living entities
                if (livingEntity instanceof IAspectAffinityEntity aspectAffinity) {
                    return aspectAffinity.aspectslib$getAspectData();
                }
            } else if (entity instanceof ItemEntity itemEntity) {
                // Handle item entities
                ItemStack stack = itemEntity.getStack();
                return ((dev.overgrown.aspectslib.aspects.api.IAspectDataProvider) (Object) stack).aspectslib$getAspectData();
            }
        }
        return null;
    }

    private void renderAuraNodeAspects(DrawContext context, AuraNodeEntity auraNode, int centerX, int centerY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        boolean showNames = MinecraftClient.getInstance().options.sneakKey.isPressed();

        // Get aspects from the aura node
        var aspects = auraNode.getAspects();
        if (aspects.isEmpty()) return;

        // Calculate total width for centering
        int totalWidth = 0;
        for (var entry : aspects.entrySet()) {
            Aspect aspect = ModRegistries.ASPECTS.get(entry.getKey());
            if (aspect == null) continue;

            int currentValue = entry.getValue().current;
            int textWidth = showNames ?
                    textRenderer.getWidth(aspect.getTranslatedName()) :
                    textRenderer.getWidth(currentValue + "/" + entry.getValue().original);

            totalWidth += ASPECT_ICON_SIZE + textWidth + ASPECT_SPACING;
        }

        // Start position for drawing
        int currentX = centerX - totalWidth / 2;

        for (var entry : aspects.entrySet()) {
            Identifier aspectId = entry.getKey();
            Aspect aspect = ModRegistries.ASPECTS.get(aspectId);
            if (aspect == null) continue;

            int currentValue = entry.getValue().current;
            int originalValue = entry.getValue().original;

            Identifier texture = aspect.textureLocation();
            context.drawTexture(texture, currentX, centerY, 0, 0,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE);

            if (showNames) {
                Text aspectText = aspect.getTranslatedName().formatted(Formatting.WHITE);
                context.drawText(textRenderer, aspectText,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(aspectText) + ASPECT_SPACING;
            } else {
                String value = currentValue + "/" + originalValue;
                context.drawText(textRenderer, value,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(value) + ASPECT_SPACING;
            }
        }
    }

    private void renderAspectData(DrawContext context, AspectData data, int centerX, int centerY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        boolean showNames = MinecraftClient.getInstance().options.sneakKey.isPressed();

        // Calculate total width for centering
        int totalWidth = 0;
        for (var entry : data.getMap().object2IntEntrySet()) {
            Aspect aspect = ModRegistries.ASPECTS.get(entry.getKey());
            if (aspect == null) continue;

            int textWidth = showNames ?
                    textRenderer.getWidth(aspect.getTranslatedName()) :
                    textRenderer.getWidth(String.valueOf(entry.getIntValue()));

            totalWidth += ASPECT_ICON_SIZE + textWidth + ASPECT_SPACING;
        }

        // Start position for drawing
        int currentX = centerX - totalWidth / 2;

        for (var entry : data.getMap().object2IntEntrySet()) {
            Identifier aspectId = entry.getKey();
            Aspect aspect = ModRegistries.ASPECTS.get(aspectId);
            if (aspect == null) continue;

            Identifier texture = aspect.textureLocation();
            context.drawTexture(texture, currentX, centerY, 0, 0,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE);

            if (showNames) {
                Text aspectText = aspect.getTranslatedName().formatted(Formatting.WHITE);
                context.drawText(textRenderer, aspectText,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(aspectText) + ASPECT_SPACING;
            } else {
                String value = String.valueOf(entry.getIntValue());
                context.drawText(textRenderer, value,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(value) + ASPECT_SPACING;
            }
        }
    }

    private void renderBlockEntityAspects(DrawContext context, AspectContainer container, int centerX, int centerY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        boolean showNames = MinecraftClient.getInstance().options.sneakKey.isPressed();

        // Calculate total width for centering
        int totalWidth = 0;
        Set<Identifier> aspects = container.getAspects();
        String[] displayStrings = new String[aspects.size()];

        for (Identifier identifier : aspects) {
            Aspect aspect = ModRegistries.ASPECTS.get(identifier);
            if (aspect == null) continue;


            int textWidth;
            if (showNames) {
                textWidth = textRenderer.getWidth(aspect.getTranslatedName());
            } else {
                int level = container.getAspectLevel(identifier);
                Integer desiredLevel = container.getDesiredAspectLeve(identifier);
                textWidth = textRenderer.getWidth(desiredLevel == null ? String.valueOf(level) : level + "/" + desiredLevel);
            }
            totalWidth += ASPECT_ICON_SIZE + textWidth + ASPECT_SPACING;
        }

        // Start position for drawing
        int currentX = centerX - totalWidth / 2;

        for (Identifier identifier : container.getAspects()) {
            Aspect aspect = ModRegistries.ASPECTS.get(identifier);
            if (aspect == null) continue;

            Identifier texture = aspect.textureLocation();
            context.drawTexture(texture, currentX, centerY, 0, 0,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE);

            if (showNames) {
                Text aspectText = aspect.getTranslatedName().formatted(Formatting.WHITE);
                context.drawText(textRenderer, aspectText,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(aspectText) + ASPECT_SPACING;
            } else {
                int level = container.getAspectLevel(identifier);
                Integer desiredLevel = container.getDesiredAspectLeve(identifier);
                String value = desiredLevel == null ? String.valueOf(level) : level + "/" + desiredLevel;
                context.drawText(textRenderer, value,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(value) + ASPECT_SPACING;
            }
        }
    }
}