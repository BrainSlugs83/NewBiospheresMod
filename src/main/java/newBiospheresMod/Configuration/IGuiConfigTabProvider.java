package newBiospheresMod.Configuration;

public interface IGuiConfigTabProvider
{
	String getTitle();

	boolean getAllRequireWorldRestart();

	boolean getAllRequireMcRestart();

	Iterable<GuiConfigTabEntry> getTabs();
}
