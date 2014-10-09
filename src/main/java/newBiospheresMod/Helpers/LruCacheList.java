package newBiospheresMod.Helpers;

import java.util.concurrent.ConcurrentMap;

import akka.japi.Creator;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class LruCacheList<T>
{
	// apparently this library doesn't like using "null" as a key, which is stupid. So, let's use this random object
	// instead.
	private static final Object nullKeySubstitute = new Object();

	public final int TotalItems;
	public final IKeyProvider<T> KeyProvider;

	private final ConcurrentMap<Object, T> _backingMap;

	public LruCacheList(int totalItems, IKeyProvider<T> keyProvider)
	{
		this.TotalItems = totalItems;
		this.KeyProvider = keyProvider;

		_backingMap = new ConcurrentLinkedHashMap.Builder<Object, T>().maximumWeightedCapacity(totalItems).build();
	}

	public void Push(T item)
	{
		Object key = KeyProvider.provideKey(item);
		if (key == null) key = nullKeySubstitute;

		_backingMap.put(key, item);
	}

	public boolean Contains(T item)
	{
		Object key = KeyProvider.provideKey(item);
		if (key == null) key = nullKeySubstitute;

		return _backingMap.containsKey(key);
	}

	public T FindOrAdd(Object key, Creator<T> factory)
	{
		if (key == null) key = nullKeySubstitute;

		T returnValue = _backingMap.get(key);
		if (returnValue == null)
		{
			try
			{
				returnValue = factory.create();
			}
			catch (Throwable ignore)
			{
				// do nothing
			}

			if (returnValue != null)
			{
				_backingMap.put(key, returnValue);
			}
		}

		return returnValue;
	}
}
