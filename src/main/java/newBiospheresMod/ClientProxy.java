package newBiospheresMod;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void initRenderHandler()
	{
		RenderHandler.init();
		
	}

}
