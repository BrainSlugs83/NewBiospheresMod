package newBiospheresMod;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import newBiospheresMod.Helpers.Utils;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public enum RenderHandler implements ISimpleBlockRenderingHandler
{
	INSTANCE;
	private static int renderIdNo3D = -2;
	private static int renderIdWith3D = -2;

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		final Block toRender;
		if (block instanceof BlockDome) {
			toRender = ((BlockDome) block).getProxiedBlock();
		} else {
			System.out.println("Warning: invalid inv block " + block.getLocalizedName());
			toRender = block;
		}
		try {
			renderer.renderBlockAsItem(toRender, metadata, 1.0f);
		} catch (Exception e) {
			System.out.println("Error rendering block as item:");
			System.out.println(block.getLocalizedName());
			System.out.println(metadata);
			System.out.println(modelId);
			System.out.println(Utils.getStackTrace(e));
		}
		
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
		RenderBlocks renderer)
	{
		final Block toRender;
		if (block instanceof BlockDome) {
			toRender = ((BlockDome) block).getProxiedBlock();
		} else {
			System.out.println("Warning: invalid world block " + block.getLocalizedName());
			toRender = block;
		}
		try {
			return renderer.renderBlockByRenderType(toRender, x, y, z);
		} catch (Exception e) {
			System.out.println("Error rendering block in world:");
			System.out.println(block.getLocalizedName());
			System.out.println(x);
			System.out.println(y);
			System.out.println(z);
			System.out.println(modelId);
			System.out.println(Utils.getStackTrace(e));
			return false;
		}
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		Utils.Assert(modelId == renderIdNo3D || modelId == renderIdWith3D, "Invalid render ID for 3D", false);
		return modelId == renderIdWith3D;
	}

	@Override
	public int getRenderId()
	{
		throw new UnsupportedOperationException("This should not happen.");
	}

	public static void init()
	{

		renderIdNo3D = RenderingRegistry.getNextAvailableRenderId();
		renderIdWith3D = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(renderIdNo3D, INSTANCE);
		RenderingRegistry.registerBlockHandler(renderIdWith3D, INSTANCE);
		
	}

	public static int get3dRenderId()
	{
		Utils.Assert(renderIdWith3D != -2, "3D Renderer not init'd yet!", true);
		return renderIdWith3D;
	}

	public static int get2dRenderId()
	{
		Utils.Assert(renderIdNo3D != -2, "2D Renderer not init'd yet!", true);
		return renderIdNo3D;
	}
	
	
}
