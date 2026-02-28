package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class FocalListWidget extends AlwaysSelectedEntryListWidget<FocalListWidget.FocalEntry> {


    private final FocalManipulatorScreen focalManipulatorScreen;

    public FocalListWidget(FocalManipulatorScreen focalManipulatorScreen, MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.focalManipulatorScreen = focalManipulatorScreen;
        setLeftPos(focalManipulatorScreen.guiLeft()+ 20);
        init();
    }

    void init() {
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/air.png")));
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/bolt.png")));
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/break.png")));
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/build.png")));
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/burst.png")));
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/chain.png")));
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/charge.png")));
        addEntry(new FocalEntry(Thaumaturge.identifier("textures/gui/focal_manipulator/cloud.png")));


    }

    @Override
    protected int getScrollbarPositionX() {
        return width + focalManipulatorScreen.guiLeft()+22;
    }

    @Override
    public int getRowWidth() {
        return 32;
    }

    @Override
    public void setSelected(@Nullable FocalListWidget.FocalEntry entry) {
        super.setSelected(entry);
    }

    public class FocalEntry extends Entry<FocalEntry> {

        private final Identifier texture;

        public FocalEntry(Identifier texture) {
            this.texture = texture;
        }

        @Override
        public Text getNarration() {
            return ScreenTexts.EMPTY;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            FocalListWidget.this.setSelected(this);
            return true;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            RenderSystem.enableBlend();
            context.drawTexture(texture,x,y,0,0,entryWidth,entryHeight,32,32);
            RenderSystem.disableBlend();
        }
    }
}
