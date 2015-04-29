/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.configuration;

import net.minecraft.client.gui.GuiScreen;
import newbiospheresmod.helpers.Func2;

public class GuiConfigTabEntry {
  public final String category;
  public final Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen> getScreen;

  public final String title;

  public GuiConfigTabEntry(final String category, final String title,
      final Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen> getScreen) {
    this.category = category;
    this.title = title;
    this.getScreen = getScreen;
  }
}
