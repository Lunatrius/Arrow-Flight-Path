package lunatrius.arrowflightpath;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
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
		if (start) {
			return true;
		}

		if (this.events.isUsingBow) {
			this.events.points.clear();

			ItemStack currentEquippedItem = this.minecraft.thePlayer.getCurrentEquippedItem();
			if (currentEquippedItem != null) {
				int charge = currentEquippedItem.getItem().getMaxItemUseDuration(currentEquippedItem) - this.minecraft.thePlayer.getItemInUseCount();

				float chargeTime = charge / 20.0f;
				chargeTime = (chargeTime * chargeTime + chargeTime * 2.0f) / 3.0f;

				if (chargeTime < 0.1f) {
					return true;
				}

				if (chargeTime > 1.0f) {
					chargeTime = 1.0f;
				}

				EntityArrow entityArrow = new EntityArrow(this.minecraft.thePlayer.worldObj, this.minecraft.thePlayer, chargeTime * 2.0f);

				for (int i = 0; i < 10000; i++) {
					this.events.points.add(new Vector3f((float) entityArrow.posX, (float) entityArrow.posY, (float) entityArrow.posZ));
					entityArrow.onUpdate();
				}

				System.out.println(entityArrow + "; " + entityArrow.onGround);
				System.out.println(this.events.points.size());
			}
		}

		return true;
	}
}
