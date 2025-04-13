package net.kyrptonaught.inventorysorter.client;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class HeartTextEntry extends AbstractConfigListEntry<Void> {

    private static final Identifier ICONS_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/hud/heart/full.png");

    private final Text message;
    private int lastRenderY;


    public HeartTextEntry(Text message) {
        super(message, false);
        this.message = message;
    }

    @Override
    public void render(DrawContext drawContext, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(drawContext, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        lastRenderY = y;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int heartSize = 9;
        int elementSpacing = 4;
        int textWidth = textRenderer.getWidth(message);

        int totalWidth = heartSize + elementSpacing + textWidth + elementSpacing + heartSize;
        int centerX = x + (entryWidth / 2);
        int startX = centerX - (totalWidth / 2);

        int textY = y + 6;
        int heartY = textY + (9 / 2) - (9 / 2);


        // Left heart
        renderPulsingHeart(drawContext, startX, heartY);

        // Text
        drawContext.drawTextWithShadow(textRenderer, message, startX + heartSize + elementSpacing, textY, 0xFFFFFF);

        if (mouseX >= startX && mouseX <= startX + totalWidth &&
                mouseY >= y && mouseY <= y + entryHeight) {

            drawContext.drawHoverEvent(
                    textRenderer,
                    message.getStyle(),
                    mouseX, mouseY
            );
        }

        // Right heart
        renderPulsingHeart(drawContext, startX + heartSize + elementSpacing + textWidth + elementSpacing, heartY);
    }

    private void renderPulsingHeart(DrawContext drawContext, int x, int y) {
        int speed = 2000;
        double time = System.currentTimeMillis() % speed;
        float scale = (float) (Math.sin(time / speed * 2 * Math.PI) * 0.1 + 1.0f);

        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(x + 4.5f, y + 4.5f, 0);
        drawContext.getMatrices().scale(scale, scale, 1.0f);
        drawContext.getMatrices().translate(-4.5f, -4.5f, 0);

        drawContext.drawTexture(RenderLayer::getGuiTextured, ICONS_TEXTURE, 0, 0, 0, 0, 9, 9, 9, 9);

        drawContext.getMatrices().pop();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            int heartSize = 9;
            int spacing = 4;
            int textWidth = textRenderer.getWidth(message);
            int totalWidth = heartSize + spacing + textWidth + spacing + heartSize;

            int screenWidth = 0;
            if (MinecraftClient.getInstance().currentScreen != null) {
                screenWidth = MinecraftClient.getInstance().currentScreen.width;
            }
            int centerX = screenWidth / 2;
            int startX = centerX - (totalWidth / 2);
            int textX = startX + heartSize + spacing;

            int textY = this.lastRenderY + 6;
            int height = textRenderer.fontHeight;

            // Bounding box of the whole [heart][text][heart] line
            int boundsX = textX;
            int boundsY = textY;
            int boundsW = textWidth;
            int boundsH = height;

            if (mouseX >= boundsX && mouseX <= boundsX + boundsW &&
                    mouseY >= boundsY && mouseY <= boundsY + boundsH) {

                Style style = message.getStyle();
                AbstractConfigScreen configScreen = this.getConfigScreen();
                if (configScreen != null && configScreen.handleTextClick(style)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public Optional<Void> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public void save() {
    }

    @Override
    public List<? extends Selectable> narratables() {
        return List.of();
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }
}

