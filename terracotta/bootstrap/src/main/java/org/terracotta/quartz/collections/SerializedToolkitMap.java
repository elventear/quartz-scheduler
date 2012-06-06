/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package org.terracotta.quartz.collections;

import org.terracotta.toolkit.ToolkitObjectType;
import org.terracotta.toolkit.collections.ToolkitMap;
import org.terracotta.toolkit.concurrent.locks.ToolkitLock;
import org.terracotta.toolkit.concurrent.locks.ToolkitLockType;
import org.terracotta.toolkit.config.Configuration;
import org.terracotta.toolkit.serializer.Serializer;

import com.terracotta.toolkit.serializer.SerializationHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SerializedToolkitMap<K, V extends Serializable> implements ToolkitMap<K, V> {
  private final ToolkitMap<String, V> toolkitMap;
  private final Serializer            serializer;

  public SerializedToolkitMap(ToolkitMap toolkitMap, Serializer serializer) {
    this.toolkitMap = toolkitMap;
    this.serializer = serializer;
  }

  @Override
  public int size() {
    return this.toolkitMap.size();
  }

  @Override
  public boolean isEmpty() {
    return this.toolkitMap.isEmpty();
  }

  private String serializeKeyToString(Object key) {
    try {
      return SerializationHelper.serializeToString(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private K deserializeKeyFromString(String key) {
    try {
      return (K) SerializationHelper.deserializeStringKey(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean containsKey(Object key) {
    String stringKey = serializeKeyToString(key);
    return this.toolkitMap.containsKey(stringKey);
  }

  @Override
  public V get(Object key) {
    String stringKey = serializeKeyToString(key);
    return this.toolkitMap.get(stringKey);
  }

  @Override
  public V put(K key, V value) {
    return this.toolkitMap.put(serializeKeyToString(key), value);
  }

  @Override
  public V remove(Object key) {
    return this.toolkitMap.remove(serializeKeyToString(key));
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    Map<String, V> tempMap = new HashMap<String, V>();
    for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
      tempMap.put(serializeKeyToString(entry.getKey()), entry.getValue());
    }

    toolkitMap.putAll(tempMap);
  }

  @Override
  public void clear() {
    toolkitMap.clear();
  }

  @Override
  public Set<K> keySet() {
    return new ToolkitKeySet(toolkitMap.keySet());
  }

  @Override
  public void lockEntry(K key) {
    toolkitMap.lockEntry(serializeKeyToString(key));
  }

  @Override
  public void unlockEntry(K key) {
    toolkitMap.unlockEntry(serializeKeyToString(key));
  }

  @Override
  public boolean isDestroyed() {
    return toolkitMap.isDestroyed();
  }

  @Override
  public void destroy() {
    toolkitMap.destroy();
  }

  @Override
  public String getName() {
    return toolkitMap.getName();
  }

  @Override
  public ToolkitObjectType getType() {
    return toolkitMap.getType();
  }

  @Override
  public ToolkitLock createFinegrainedLock(K key) {
    return toolkitMap.createFinegrainedLock(serializeKeyToString(key));
  }

  @Override
  public void removeNoReturn(K key) {
    toolkitMap.removeNoReturn(serializeKeyToString(key));
  }

  @Override
  public V unsafeGet(K key) {
    return toolkitMap.unsafeGet(serializeKeyToString(key));
  }

  @Override
  public void putNoReturn(K key, V value) {
    toolkitMap.putNoReturn(serializeKeyToString(key), value);
  }

  @Override
  public int localSize() {
    return toolkitMap.localSize();
  }

  @Override
  public Set<K> localKeySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unpinAll() {
    toolkitMap.unpinAll();
  }

  @Override
  public boolean isPinned(K key) {
    return toolkitMap.isPinned(serializeKeyToString(key));
  }

  @Override
  public void setPinned(K key, boolean pinned) {
    toolkitMap.setPinned(serializeKeyToString(key), pinned);
  }

  @Override
  public boolean containsLocalKey(K key) {
    return toolkitMap.containsLocalKey(serializeKeyToString(key));
  }

  @Override
  public Map<K, V> getAll(Collection<K> keys) {
    HashSet<String> tempSet = new HashSet<String>();
    for (K key : keys) {
      tempSet.add(serializeKeyToString(key));
    }

    Map<String, V> m = toolkitMap.getAll(tempSet);
    Map<K, V> tempMap = m.isEmpty() ? Collections.EMPTY_MAP : new HashMap<K, V>();

    for (Entry<String, V> entry : m.entrySet()) {
      tempMap.put(deserializeKeyFromString(entry.getKey()), entry.getValue());
    }

    return tempMap;
  }

  @Override
  public Configuration getConfiguration() {
    return toolkitMap.getConfiguration();
  }

  @Override
  public void setConfigField(String name, Object value) {
    toolkitMap.setConfigField(name, value);
  }

  @Override
  public boolean isBulkLoadEnabledInCluster() {
    return toolkitMap.isBulkLoadEnabledInCluster();
  }

  @Override
  public boolean isBulkLoadEnabledInCurrentNode() {
    return toolkitMap.isBulkLoadEnabledInCurrentNode();
  }

  @Override
  public void setBulkLoadEnabledInCurrentNode(boolean enableBulkLoad) {
    toolkitMap.setBulkLoadEnabledInCurrentNode(enableBulkLoad);
  }

  @Override
  public void waitUntilBulkLoadCompleteInCluster() throws InterruptedException {
    toolkitMap.waitUntilBulkLoadCompleteInCluster();
  }

  @Override
  public boolean containsValue(Object value) {
    return toolkitMap.containsValue(value);
  }

  @Override
  public V putIfAbsent(K key, V value) {
    return toolkitMap.putIfAbsent(serializeKeyToString(key), value);
  }

  @Override
  public Set<java.util.Map.Entry<K, V>> entrySet() {
    return new ToolkitEntrySet(this.toolkitMap.entrySet());
  }

  @Override
  public Collection<V> values() {
    return this.toolkitMap.values();
  }

  @Override
  public boolean remove(Object key, Object value) {
    return this.toolkitMap.remove(serializeKeyToString(key), value);
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    return this.toolkitMap.replace(serializeKeyToString(key), oldValue, newValue);
  }

  @Override
  public V replace(K key, V value) {
    return this.toolkitMap.replace(serializeKeyToString(key), value);
  }

  private class ToolkitEntrySet implements Set<java.util.Map.Entry<K, V>> {
    private final Set<java.util.Map.Entry<String, V>> set;

    public ToolkitEntrySet(Set<java.util.Map.Entry<String, V>> set) {
      this.set = set;
    }

    @Override
    public int size() {
      return set.size();
    }

    @Override
    public boolean isEmpty() {
      return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) { return false; }

      Map.Entry<K, V> entry = (java.util.Map.Entry<K, V>) o;
      ToolkitMapEntry<String, V> toolkitEntry = null;
      try {
        toolkitEntry = new ToolkitMapEntry<String, V>(serializer.serializeToString(entry.getKey()), entry.getValue());
      } catch (IOException e) {
        return false;
      }
      return this.set.contains(toolkitEntry);
    }

    @Override
    public Iterator<java.util.Map.Entry<K, V>> iterator() {
      return new ToolkitEntryIterator<K, V>(set.iterator(), serializer);
    }

    @Override
    public Object[] toArray() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(java.util.Map.Entry<K, V> e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends java.util.Map.Entry<K, V>> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }
  }

  private static class ToolkitEntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {

    private final Iterator<Map.Entry<String, V>> iter;
    private final Serializer                     serializer;

    public ToolkitEntryIterator(Iterator<Map.Entry<String, V>> iter, Serializer serializer) {
      this.iter = iter;
      this.serializer = serializer;
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public java.util.Map.Entry<K, V> next() {
      Map.Entry<String, V> entry = iter.next();
      if (entry == null) { return null; }

      try {
        return new ToolkitMapEntry(serializer.deserializeFromString(entry.getKey()), entry.getValue());
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void remove() {
      iter.remove();
    }

  }

  private static class ToolkitMapEntry<K, V> implements Map.Entry<K, V> {
    private final K k;
    private V       v;

    public ToolkitMapEntry(K k, V v) {
      this.k = k;
      this.v = v;
    }

    @Override
    public K getKey() {
      return k;
    }

    @Override
    public V getValue() {
      return v;
    }

    @Override
    public V setValue(V value) {
      V old = v;
      this.v = value;
      return old;
    }

  }

  private class ToolkitKeySet implements Set<K> {

    private final Set<String> set;

    public ToolkitKeySet(Set<String> set) {
      this.set = set;
    }

    @Override
    public int size() {
      return set.size();
    }

    @Override
    public boolean isEmpty() {
      return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      try {
        return set.contains(serializer.serializeToString(o));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Iterator<K> iterator() {
      return new ToolkitKeyIterator<K>(set.iterator(), serializer);
    }

    @Override
    public Object[] toArray() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(K e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }
  }

  private static class ToolkitKeyIterator<K> implements Iterator<K> {

    private final Iterator<String> iter;
    private final Serializer       serializer;

    public ToolkitKeyIterator(Iterator<String> iter, Serializer serializer) {
      this.iter = iter;
      this.serializer = serializer;
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public K next() {
      String k = iter.next();
      if (k == null) { return null; }
      try {
        return (K) serializer.deserializeFromString(k);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void remove() {
      iter.remove();
    }

  }

  @Override
  public Map<K, V> getAllQuiet(Collection<K> keys) {
    HashSet<String> tempSet = new HashSet<String>();
    for (K key : keys) {
      tempSet.add(serializeKeyToString(key));
    }

    Map<String, V> m = toolkitMap.getAllQuiet(tempSet);
    Map<K, V> tempMap = m.isEmpty() ? Collections.EMPTY_MAP : new HashMap<K, V>();

    for (Entry<String, V> entry : m.entrySet()) {
      tempMap.put(deserializeKeyFromString(entry.getKey()), entry.getValue());
    }

    return tempMap;
  }

  @Override
  public long localOnHeapSizeInBytes() {
    return this.toolkitMap.localOnHeapSizeInBytes();
  }

  @Override
  public long localOffHeapSizeInBytes() {
    return this.toolkitMap.localOffHeapSizeInBytes();
  }

  @Override
  public int localOnHeapSize() {
    return this.toolkitMap.localOnHeapSize();
  }

  @Override
  public int localOffHeapSize() {
    return this.toolkitMap.localOffHeapSize();
  }

  @Override
  public boolean containsKeyLocalOnHeap(K key) {
    return this.toolkitMap.containsKeyLocalOnHeap(serializeKeyToString(key));
  }

  @Override
  public boolean containsKeyLocalOffHeap(K key) {
    return this.toolkitMap.containsKeyLocalOffHeap(serializeKeyToString(key));
  }

  @Override
  public ToolkitLock createFinegrainedLock(K key, ToolkitLockType lockType) {
    return this.toolkitMap.createFinegrainedLock(serializeKeyToString(key), lockType);
  }

  @Override
  public void disposeLocally() {
    this.toolkitMap.disposeLocally();
  }
}
