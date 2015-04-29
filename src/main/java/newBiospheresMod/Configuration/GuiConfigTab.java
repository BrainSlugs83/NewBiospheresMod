/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import newbiospheresmod.helpers.ModConsts;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class GuiConfigTab extends GuiConfig {
  private static final Random Rnd = new Random(System.currentTimeMillis());

  private final String category;
  private final IGuiConfigTabProvider provider;
  private Map<GuiButton, GuiConfigTabEntry> tabMap;

  public GuiConfigTab(final GuiScreen parent, final IGuiConfigTabProvider provider, final String category,
      final List<IConfigElement> childElements) {
    super(parent, childElements, ModConsts.ModId, provider.getAllRequireWorldRestart(), provider
        .getAllRequireMcRestart(), provider.getTitle());

    this.provider = provider;
    this.category = category;
  }

  @Override
  protected void actionPerformed(final GuiButton guiButton) {
    super.actionPerformed(guiButton);

    if (guiButton != null) {
      if (this.tabMap.containsKey(guiButton)) {
        this.navigateTo(this.tabMap.get(guiButton));
      }
    }
  }

  private int getUniqueButtonId() {
    int id = 0;
    boolean keepTrying = true;
    while (keepTrying) {
      id = 600000 + Math.abs(GuiConfigTab.Rnd.nextInt(400000));
      keepTrying = false;

      if (this.buttonList != null) {
        for (final Object btn : this.buttonList) {
          if ((btn != null) && (btn instanceof GuiButton)) {
            if (((GuiButton) btn).id == id) {
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
  public void initGui() {
    if (this.tabMap != null) {
      this.buttonList.removeAll(this.tabMap.keySet());
      this.tabMap.clear();
    } else {
      this.tabMap = new HashMap<GuiButton, GuiConfigTabEntry>();
    }

    int x = 8;
    final int y = 27;
    final int height = 16;

    for (final GuiConfigTabEntry tab : this.provider.getTabs()) {
      final String text = tab.title;
      final int width = this.fontRendererObj.getStringWidth(text) + 16;

      final GuiButton button = new GuiButton(this.getUniqueButtonId(), x, y, width, height, text);
      if (tab.category.equals(this.category)) {
        button.packedFGColour = 255 << 8;
      }

      this.tabMap.put(button, tab);
      this.buttonList.add(button);

      x += (width + 8);
    }

    super.initGui();
    this.entryList.top = y + height;
  }

  public void navigateTo(final GuiConfigTabEntry entry) {
    if ((entry != null) && (this.provider != null)) {
      if (!entry.category.equals(this.category)) {
        this.entryList.saveConfigElements();
        this.mc.displayGuiScreen(entry.getScreen.func(this.parentScreen, this.provider));
      }
    }
  }
}
