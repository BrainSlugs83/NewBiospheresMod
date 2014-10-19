/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

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
