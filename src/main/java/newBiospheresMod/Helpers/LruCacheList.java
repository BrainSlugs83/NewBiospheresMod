/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Helpers;

import java.util.concurrent.ConcurrentMap;

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
