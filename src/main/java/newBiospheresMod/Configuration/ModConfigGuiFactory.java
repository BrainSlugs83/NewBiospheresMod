/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.configuration;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.IModGuiFactory;

public class ModConfigGuiFactory implements IModGuiFactory {
  @Override
  public RuntimeOptionGuiHandler getHandlerFor(final RuntimeOptionCategoryElement element) {
    return null;
  }

  @Override
  public void initialize(final Minecraft minecraftInstance) {
    /* do nothing */
  }

  @Override
  public Class<? extends GuiScreen> mainConfigGuiClass() {
    return ConfigScreens.GeneralGuiConfigTab.class;
  }

  @Override
  public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
    return null;
  }
}
