package newBiospheresMod.Helpers;

import java.util.concurrent.ConcurrentMap;

import akka.japi.Creator;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class LruCacheList<T>
{
	public final int TotalItems;
	public final IKeyProvider<T> KeyProvider;

	private final ConcurrentMap<Integer, T> _backingMap;

	public LruCacheList(int totalItems)
	{
		this(totalItems, null);
	}

	public LruCacheList(int totalItems, IKeyProvider<T> keyProvider)
	{
		this.TotalItems = totalItems;
		this.KeyProvider = keyProvider != null ? keyProvider : new IKeyProvider<T>()
		{
			@Override
			public int provideKey(T item)
			{
				if (item == null) { return 0; }
				return item.hashCode();
			}
		};

		_backingMap = new ConcurrentLinkedHashMap.Builder<Integer, T>().maximumWeightedCapacity(totalItems).build();
	}

	public void Push(T item)
	{
		int key = KeyProvider.provideKey(item);
		_backingMap.put(key, item);
	}

	public boolean Contains(T item)
	{
		int key = KeyProvider.provideKey(item);
		return _backingMap.containsKey(key);
	}

	public T FindOrAdd(int key, Creator<T> factory)
	{
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
