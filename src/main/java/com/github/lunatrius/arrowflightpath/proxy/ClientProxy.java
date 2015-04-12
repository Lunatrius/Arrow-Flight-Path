package com.github.lunatrius.arrowflightpath.proxy;

import com.github.lunatrius.arrowflightpath.handler.Renderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(final FMLInitializationEvent event) {
        super.init(event);

        MinecraftForge.EVENT_BUS.register(new Renderer());
    }
}
