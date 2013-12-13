package lunatrius.arrowflightpath;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.util.vector.Vector3f;

import java.util.EnumSet;

import static cpw.mods.fml.common.Mod.EventHandler;
import static cpw.mods.fml.common.Mod.Instance;

@Mod(modid = "ArrowFlightPath")
public class ArrowFlightPath {
	private Minecraft minecraft = Minecraft.getMinecraft();
	private Events events = null;

	@Instance
	public static ArrowFlightPath instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		this.events = new Events();
		MinecraftForge.EVENT_BUS.register(this.events);

		TickRegistry.registerTickHandler(new Ticker(EnumSet.of(TickType.CLIENT)), Side.CLIENT);
	}

	public boolean onTick(TickType tick, boolean start) {
		if (this.events.isUsingBow) {
			this.events.points.clear();

			for (int y = 0; y < 255; y++) {
				this.events.points.add(new Vector3f((float) this.minecraft.thePlayer.posX + 2, y, (float) this.minecraft.thePlayer.posZ + 2));
			}
		}

		return true;
	}
}
