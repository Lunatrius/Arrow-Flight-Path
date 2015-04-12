package com.github.lunatrius.arrowflightpath.handler;

import com.github.lunatrius.arrowflightpath.reference.Reference;
import com.github.lunatrius.arrowflightpath.util.NotRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private Vector3f playerPosition = new Vector3f();
    private List<Vector3f> points = new ArrayList<Vector3f>();
    private boolean isUsingBow = false;

    @SubscribeEvent
    public void onRender(final RenderWorldLastEvent event) {
        if (this.isUsingBow) {
            final EntityPlayerSP player = this.minecraft.thePlayer;
            if (player != null) {
                this.playerPosition.x = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks);
                this.playerPosition.y = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks);
                this.playerPosition.z = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks);

                if (compile(player)) {
                    return;
                }

                render();
            }
        }
    }

    @SubscribeEvent
    public void onArrowNock(final ArrowNockEvent event) {
        this.isUsingBow = true;
    }

    @SubscribeEvent
    public void onArrowLoose(final ArrowLooseEvent event) {
        this.isUsingBow = false;
    }

    private boolean compile(EntityPlayerSP player) {
        this.points.clear();

        final ItemStack currentEquippedItem = player.getCurrentEquippedItem();
        if (currentEquippedItem != null) {
            final int charge = currentEquippedItem.getItem().getMaxItemUseDuration(currentEquippedItem) - player.getItemInUseCount();

            float chargeTime = charge / 20.0f;
            chargeTime = (chargeTime * chargeTime + chargeTime * 2.0f) / 3.0f;

            if (chargeTime < 0.1f) {
                return true;
            }

            if (chargeTime > 1.0f) {
                chargeTime = 1.0f;
            }


            final EntityArrow arrow = new EntityArrow(player.worldObj, player, chargeTime * 2.0f);
            ReflectionHelper.setPrivateValue(Entity.class, arrow, NotRandom.INSTANCE, "field_70146_Z", "rand");

            arrow.canBePickedUp = 0;
            arrow.worldObj = player.worldObj;
            arrow.shootingEntity = player;
            arrow.setLocationAndAngles(player.posX, player.posY + player.getEyeHeight(), player.posZ, player.rotationYaw, player.rotationPitch);
            arrow.motionX = -MathHelper.sin(arrow.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(arrow.rotationPitch / 180.0F * (float) Math.PI);
            arrow.motionZ = MathHelper.cos(arrow.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(arrow.rotationPitch / 180.0F * (float) Math.PI);
            arrow.motionY = -MathHelper.sin(arrow.rotationPitch / 180.0F * (float) Math.PI);
            arrow.setThrowableHeading(arrow.motionX, arrow.motionY, arrow.motionZ, chargeTime * 2.0f * 1.5f, 0);

            for (int i = 0; i < 2000 && !arrow.isDead; i++) {
                Reference.logger.trace("Iteration #" + i + ": " + arrow);
                this.points.add(new Vector3f((float) arrow.posX, (float) arrow.posY, (float) arrow.posZ));
                arrow.onUpdate();
            }
        }

        return false;
    }

    private void render() {
        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.translate(-this.playerPosition.x, -this.playerPosition.y, -this.playerPosition.z);
        GlStateManager.color(1.0f, 0.0f, 1.0f);

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        GL11.glPointSize(2.0f);

        worldRenderer.startDrawing(GL11.GL_POINTS);
        for (final Vector3f point : this.points) {
            worldRenderer.addVertex(point.x, point.y, point.z);
        }
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();
    }
}
