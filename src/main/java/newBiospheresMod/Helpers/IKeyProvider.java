package newBiospheresMod.Helpers;

public interface IKeyProvider<T>
{
	Object provideKey(T item);
}
