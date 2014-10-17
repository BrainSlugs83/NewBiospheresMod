package newBiospheresMod.Configuration;

import net.minecraft.client.gui.GuiScreen;
import newBiospheresMod.Helpers.Func2;

public class GuiConfigTabEntry
{
	public final String Category;
	public final String Title;

	public final Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen> getScreen;

	public GuiConfigTabEntry(String category, String title, Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen> getScreen)
	{
		this.Category = category;
		this.Title = title;
		this.getScreen = getScreen;
	}
}
