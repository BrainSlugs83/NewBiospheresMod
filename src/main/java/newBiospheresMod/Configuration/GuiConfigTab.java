/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import newBiospheresMod.Helpers.ModConsts;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class GuiConfigTab extends GuiConfig
{
	private static final Random Rnd = new Random(System.currentTimeMillis());

	private final IGuiConfigTabProvider Provider;
	private final String Category;
	private Map<GuiButton, GuiConfigTabEntry> TabMap;

	public GuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider, String category,
			List<IConfigElement> childElements)
	{
		super(
			parent,
			childElements,
			ModConsts.ModId, provider.getAllRequireWorldRestart(), provider.getAllRequireMcRestart(),
			provider.getTitle());

		this.Provider = provider;
		this.Category = category;
	}

	private int getUniqueButtonId()
	{
		int id = 0;
		boolean keepTrying = true;
		while (keepTrying)
		{
			id = 600000 + Math.abs(Rnd.nextInt(400000));
			keepTrying = false;

			if (this.buttonList != null)
			{
				for (Object btn: this.buttonList)
				{
					if (btn != null && btn instanceof GuiButton)
					{
						if (((GuiButton)btn).id == id)
						{
							keepTrying = true;
							break;
						}
					}
				}
			}
		}
		return id;
	}

	@Override
	public void initGui()
	{
		if (TabMap != null)
		{
			this.buttonList.removeAll(TabMap.keySet());
			TabMap.clear();
		}
		else
		{
			TabMap = new HashMap<GuiButton, GuiConfigTabEntry>();
		}

		int x = 8;
		int y = 27;
		int height = 16;

		for (GuiConfigTabEntry tab: Provider.getTabs())
		{
			String text = tab.Title;
			int width = this.fontRendererObj.getStringWidth(text) + 16;

			GuiButton button = new GuiButton(getUniqueButtonId(), x, y, width, height, text);
			if (tab.Category.equals(this.Category))
			{
				button.packedFGColour = 255 << 8;
			}

			this.TabMap.put(button, tab);
			this.buttonList.add(button);

			x += (width + 8);
		}

		super.initGui();
		this.entryList.top = y + height;
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);

		if (guiButton != null)
		{
			if (TabMap.containsKey(guiButton))
			{
				NavigateTo(TabMap.get(guiButton));
			}
		}
	}

	public void NavigateTo(GuiConfigTabEntry entry)
	{
		if (entry != null && this.Provider != null)
		{
			if (!entry.Category.equals(this.Category))
			{
				this.entryList.saveConfigElements();
				mc.displayGuiScreen(entry.getScreen.func(this.parentScreen, this.Provider));
			}
		}
	}
}
