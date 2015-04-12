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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Renderer {
    public static final double DELTA = 0.005;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Field fieldArrowRand;
    private final Field fieldArrowX;
    private final Field fieldArrowY;
    private final Field fieldArrowZ;
    private Vector3f playerPosition = new Vector3f();
    private List<Vector3f> points = new ArrayList<Vector3f>();
    private boolean isUsingBow = false;
    private int targetX = -1;
    private int targetY = -1;
    private int targetZ = -1;

    public Renderer() {
        this.fieldArrowRand = ReflectionHelper.findField(Entity.class, "field_70146_Z", "rand");
        this.fieldArrowX = ReflectionHelper.findField(EntityArrow.class, "field_145791_d", "xTile");
        this.fieldArrowY = ReflectionHelper.findField(EntityArrow.class, "field_145791_e", "yTile");
        this.fieldArrowZ = ReflectionHelper.findField(EntityArrow.class, "field_145791_f", "zTile");
    }

    @SubscribeEvent
    public void onRender(final RenderWorldLastEvent event) {
        if (this.isUsingBow) {
            final EntityPlayerSP player = this.minecraft.thePlayer;
            if (player != null) {
                this.playerPosition.x = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks);
                this.playerPosition.y = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks);
                this.playerPosition.z = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks);

                if (!compile(player)) {
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
                return false;
            }

            if (chargeTime > 1.0f) {
                chargeTime = 1.0f;
            }


            final EntityArrow arrow = new EntityArrow(player.worldObj, player, chargeTime * 2.0f);
            try {
                this.fieldArrowRand.set(arrow, NotRandom.INSTANCE);
            } catch (final Exception e) {
                Reference.logger.error("Could not set rand field!");
            }

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

            try {
                this.targetX = (Integer) this.fieldArrowX.get(arrow);
                this.targetY = (Integer) this.fieldArrowY.get(arrow);
                this.targetZ = (Integer) this.fieldArrowZ.get(arrow);
            } catch (final Exception e) {
                this.targetX = -1;
                this.targetY = -1;
                this.targetZ = -1;
            }
        }

        return true;
    }

    private void render() {
        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.translate(-this.playerPosition.x, -this.playerPosition.y, -this.playerPosition.z);

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        GL11.glPointSize(2.0f);

        worldRenderer.startDrawing(GL11.GL_POINTS);
        worldRenderer.setColorOpaque_I(0xFF00FF);
        for (final Vector3f point : this.points) {
            worldRenderer.addVertex(point.x, point.y, point.z);
        }
        tessellator.draw();

        final double x0 = this.targetX - DELTA;
        final double y0 = this.targetY - DELTA;
        final double z0 = this.targetZ - DELTA;
        final double x1 = this.targetX + 1 + DELTA;
        final double y1 = this.targetY + 1 + DELTA;
        final double z1 = this.targetZ + 1 + DELTA;

        worldRenderer.startDrawing(GL11.GL_QUADS);
        worldRenderer.setColorRGBA_I(0x00FF00, 0x7F);

        // down
        worldRenderer.addVertex(x1, y0, z0);
        worldRenderer.addVertex(x1, y0, z1);
        worldRenderer.addVertex(x0, y0, z1);
        worldRenderer.addVertex(x0, y0, z0);

        // up
        worldRenderer.addVertex(x1, y1, z0);
        worldRenderer.addVertex(x0, y1, z0);
        worldRenderer.addVertex(x0, y1, z1);
        worldRenderer.addVertex(x1, y1, z1);

        // north
        worldRenderer.addVertex(x1, y0, z0);
        worldRenderer.addVertex(x0, y0, z0);
        worldRenderer.addVertex(x0, y1, z0);
        worldRenderer.addVertex(x1, y1, z0);

        // south
        worldRenderer.addVertex(x0, y0, z1);
        worldRenderer.addVertex(x1, y0, z1);
        worldRenderer.addVertex(x1, y1, z1);
        worldRenderer.addVertex(x0, y1, z1);

        // west
        worldRenderer.addVertex(x0, y0, z0);
        worldRenderer.addVertex(x0, y0, z1);
        worldRenderer.addVertex(x0, y1, z1);
        worldRenderer.addVertex(x0, y1, z0);

        // east
        worldRenderer.addVertex(x1, y0, z1);
        worldRenderer.addVertex(x1, y0, z0);
        worldRenderer.addVertex(x1, y1, z0);
        worldRenderer.addVertex(x1, y1, z1);

        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();
    }
}
