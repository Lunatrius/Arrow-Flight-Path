package lunatrius.arrowflightpath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Events {
	private final Minecraft minecraft = Minecraft.getMinecraft();
	public List<Vector3f> points = new ArrayList<Vector3f>();
	public boolean isUsingBow = false;

	@ForgeSubscribe
	public void onRender(RenderWorldLastEvent event) {
		if (this.isUsingBow) {
			EntityPlayerSP player = this.minecraft.thePlayer;
			if (player != null) {
				double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
				double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
				double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

				render(x, y, z);
			}
		}
	}

	@ForgeSubscribe
	public void onArrowNock(ArrowNockEvent event) {
		this.isUsingBow = true;
	}

	@ForgeSubscribe
	public void onArrowLoose(ArrowLooseEvent event) {
		this.isUsingBow = false;
	}

	private void render(double offsetX, double offsetY, double offsetZ) {
		GL11.glPushMatrix();
		GL11.glTranslated(-offsetX, -offsetY, -offsetZ);

		GL11.glColor3f(0.0f, 1.0f, 0.0f);

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

		GL11.glPointSize(5);

		GL11.glBegin(GL11.GL_POINTS);
		for (Vector3f point : this.points) {
			GL11.glVertex3f(point.x, point.y, point.z);
		}
		GL11.glEnd();

		GL11.glDisable(GL11.GL_BLEND);

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glPopMatrix();
	}
}
