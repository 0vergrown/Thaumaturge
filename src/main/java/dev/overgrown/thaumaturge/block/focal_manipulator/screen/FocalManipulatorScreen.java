package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FocalManipulatorScreen extends HandledScreen<FocalManipulatorScreenHandler> {
    private static final Identifier OUTER_TEXTURE =
            new Identifier("thaumaturge", "textures/gui/focal_manipulator/focal_manipulator_outer_layer.png");
    private static final Identifier INNER_TEXTURE =
            new Identifier("thaumaturge", "textures/gui/focal_manipulator/focal_manipulator_inner_layer.png");
    private static final Identifier INVENTORY_TEXTURE =
            new Identifier("thaumaturge", "textures/gui/focal_manipulator/focal_manipulator_player_inventory.png");

    private TextFieldWidget spellNameField;
    private ButtonWidget craftButton;

    public FocalManipulatorScreen(FocalManipulatorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 256;
        this.backgroundHeight = 256;
    }

    @Override
    protected void init() {
        super.init();
        this.spellNameField = new TextFieldWidget(this.textRenderer, this.x + 80, this.y + 6, 100, 12, Text.empty());
        this.spellNameField.setMaxLength(50);
        this.addSelectableChild(this.spellNameField);

        this.craftButton = ButtonWidget.builder(Text.literal("Craft"), button -> {
            // TODO: start crafting
        }).dimensions(this.x + 227, this.y + 6, 40, 20).build();
        this.addDrawableChild(this.craftButton);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw outer layer (bottom)
        context.drawTexture(OUTER_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Draw inventory texture on the left side
        context.drawTexture(INVENTORY_TEXTURE,
                this.x + FocalManipulatorScreenHandler.INVENTORY_TEX_X,
                this.y + FocalManipulatorScreenHandler.INVENTORY_TEX_Y,
                0, 0, 256, 256);

        // Draw inner layer (top)
        context.drawTexture(INNER_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Placeholder text (top right)
        context.drawText(this.textRenderer, "Complexity: 5", this.x + 229, this.y + 30, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Aether: 100",    this.x + 229, this.y + 42, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Shards: 2",      this.x + 229, this.y + 54, 0xFFFFFF, false);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Do not draw default title
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.spellNameField.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.spellNameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}