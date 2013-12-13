package lunatrius.arrowflightpath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Events {
	private final Minecraft minecraft = Minecraft.getMinecraft();
	private List<Vector3f> points = new ArrayList<Vector3f>();

	@ForgeSubscribe
	public void onRender(RenderWorldLastEvent event) {
		EntityPlayerSP player = this.minecraft.thePlayer;
		if (player != null) {
			double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
			double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
			double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

			render(x, y, z);
		}
	}

	public Events() {
		for (int y = 0; y < 255; y++) {
			this.points.add(new Vector3f(0, y, 0));
		}
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
