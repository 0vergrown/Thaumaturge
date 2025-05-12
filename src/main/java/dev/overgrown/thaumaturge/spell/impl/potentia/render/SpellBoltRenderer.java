package dev.overgrown.thaumaturge.spell.impl.potentia.render;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class SpellBoltRenderer extends EntityRenderer<SpellBoltEntity, SpellBoltRenderState> {
    public SpellBoltRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public SpellBoltRenderState createRenderState() {
        return new SpellBoltRenderState();
    }

    @Override
    public void updateRenderState(SpellBoltEntity entity, SpellBoltRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.seed = entity.getSeed();
        state.tier = entity.getTier();
        state.yaw = entity.getYaw();
        state.pitch = entity.getPitch();
    }

    @Override
    public void render(SpellBoltRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.pitch));

        // Move beam little bit to the right
        matrices.translate(-0.2, -0.2, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(3));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(3));

        renderBolt(matrices, vertexConsumers, state.tier, state.seed);
        matrices.pop();
    }

    private void renderBolt(MatrixStack matrices, VertexConsumerProvider consumers, int tier, long seed) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLightning());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float r = 0.2f, g = 0.9f, b = 1.0f; // Adjust color to be more electric
        float alpha = 0.5f;

        Random random = Random.create(seed);
        // Generate multiple main branches
        for (int i = 0; i < 4; i++) {
            generateBranch(matrix, consumer, random, r, g, b, alpha);
        }
    }

    private void generateBranch(Matrix4f matrix, VertexConsumer consumer, Random random, float r, float g, float b, float alpha) {

        int segments = (int) SpellBoltEntity.LENGTH;
        float segmentLength = 1f;
        float spread = 0.35f;

        float prevX = 0;
        float prevY = 0;
        float prevZ = 0;

        for (int i = -segments/2; i < segments/2; i++) {
            // Move forward in local Z axis (bolt's direction)
            float z = i * segmentLength;

            // Add lateral spread
            float x = (random.nextFloat() - 0.5f) * spread;
            float y = (random.nextFloat() - 0.5f) * spread;

                // Draw line from previous point to current
                consumer.vertex(matrix, prevX, prevY, prevZ)
                        .color(r, g, b, alpha);
                consumer.vertex(matrix, x, y, z)
                        .color(r, g, b, alpha);


            prevX = x;
            prevY = y;
            prevZ = z;
        }
    }

    private void drawLine(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1,
                          float x2, float y2, float z2, float r, float g, float b, float alpha) {
        // Draw a line between two points with given color and alpha
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha);
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha);
    }

    public Identifier getTexture(SpellBoltRenderState state) {
        return Thaumaturge.identifier("textures/entity/spell_bolt.png");
    }
}