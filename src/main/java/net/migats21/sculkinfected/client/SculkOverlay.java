package net.migats21.sculkinfected.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.migats21.sculkinfected.SculkInfected;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SculkOverlay extends GuiComponent {
    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(SculkInfected.MODID, "textures/gui/sculkbar.png");
    private static final ResourceLocation OVERLAY = new ResourceLocation(SculkInfected.MODID, "textures/misc/sculk_overlay.png");
    protected final Minecraft minecraft = Minecraft.getInstance();

    public void renderSculkbar(PoseStack pos) {
        if (this.minecraft.gameMode == null) return;
        if (this.minecraft.gameMode.canHurtPlayer()) {
            int posX = this.minecraft.getWindow().getGuiScaledWidth() / 2 - (this.minecraft.player.getOffhandItem().isEmpty() || this.minecraft.player.getMainArm() == HumanoidArm.LEFT ? 145 : 173);
            int posY = this.minecraft.getWindow().getGuiScaledHeight() - 8;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShaderTexture(0, GUI_LOCATION);
            this.blit(pos, posX, posY, 0, 0, 50, 5);
            this.blit(pos, posX, posY, 0, 5, (int) (LocalSculkTimer.getInstance().getProgress() * 50), 5);
            this.blit(pos, posX + 13, posY - 24, 0, 10, 24, 24);
            Component text = Component.literal("Day " + LocalSculkTimer.getInstance().getDaytime());
            int textWidth = this.minecraft.font.width(text);
            this.minecraft.font.draw(pos, text, posX + 24 - textWidth / 2f, posY - 10, 0);
            this.minecraft.font.draw(pos, text, posX + 26 - textWidth / 2f, posY - 10, 0);
            this.minecraft.font.draw(pos, text, posX + 25 - textWidth / 2f, posY - 9, 0);
            this.minecraft.font.draw(pos, text, posX + 25 - textWidth / 2f, posY - 11, 0);
            this.minecraft.font.draw(pos, text, posX + 25 - textWidth / 2f, posY - 10, LocalSculkTimer.getInstance().getDaytimeColor());
            // TODO: Adding sculk hearts that render when taking infection damage
        }
    }

    // Copied from vanilla class
    public void renderOverlay(float opacity) {
        int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
        RenderSystem.setShaderTexture(0, OVERLAY);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0D, screenHeight, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(screenWidth, screenHeight, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(screenWidth, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
