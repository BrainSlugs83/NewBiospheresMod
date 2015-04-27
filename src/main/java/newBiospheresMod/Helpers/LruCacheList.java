/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.helpers;

import java.util.concurrent.ConcurrentMap;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class LruCacheList<T> {
  // apparently this library doesn't like using "null" as a key, which is stupid. So, let's use this
  // random object
  // instead.
  private static final Object nullKeySubstitute = new Object();

  private final ConcurrentMap<Object, T> backingMap;
  public final IKeyProvider<T> keyProvider;

  public final int totalItems;

  public LruCacheList(final int totalItems, final IKeyProvider<T> keyProvider) {
    this.totalItems = totalItems;
    this.keyProvider = keyProvider;

    this.backingMap = new ConcurrentLinkedHashMap.Builder<Object, T>().maximumWeightedCapacity(totalItems).build();
  }

  public boolean contains(final T item) {
    Object key = this.keyProvider.provideKey(item);
    if (key == null) {
      key = LruCacheList.nullKeySubstitute;
    }

    return this.backingMap.containsKey(key);
  }

  public T findOrAdd(Object key, final Creator<T> factory) {
    if (key == null) {
      key = LruCacheList.nullKeySubstitute;
    }

    T returnValue = this.backingMap.get(key);
    if (returnValue == null) {
      try {
        returnValue = factory.create();
      } catch (final Throwable ignore) {
        // do nothing
      }

      if (returnValue != null) {
        this.backingMap.put(key, returnValue);
      }
    }

    return returnValue;
  }

  public void push(final T item) {
    Object key = this.keyProvider.provideKey(item);
    if (key == null) {
      key = LruCacheList.nullKeySubstitute;
    }

    this.backingMap.put(key, item);
  }
}
