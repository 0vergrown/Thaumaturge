package dev.overgrown.thaumaturge.block.pedestal.client;

import dev.overgrown.thaumaturge.block.pedestal.PedestalBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

public class PedestalBlockEntityRenderer implements BlockEntityRenderer<PedestalBlockEntity> {

    private final ItemRenderer itemRenderer;
    public PedestalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        itemRenderer = ctx.getItemRenderer();
    }
    @Override
    public void render(PedestalBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
            ItemStack stack = entity.getItem();
            if (!stack.isEmpty()) {
                matrices.push();
                //entity.random.setSeed(j);
                BakedModel bakedModel = this.itemRenderer.getModel(stack, entity.getWorld(), null,0);
                float m = bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
                matrices.translate(0.5F, 1 + 0.25F * m, 0.5F);
                float n = Util.getMeasuringTimeMs()/1000f;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(n));
                matrices.push();
                this.itemRenderer
                        .renderItem(stack, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, bakedModel);
                matrices.pop();
                matrices.pop();
            }
    }
}
