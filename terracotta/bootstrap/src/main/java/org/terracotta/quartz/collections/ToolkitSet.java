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

import org.terracotta.toolkit.collections.ToolkitList;
import org.terracotta.toolkit.concurrent.locks.ToolkitReadWriteLock;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ToolkitSet<K extends Serializable> implements Set<K> {
  ToolkitList<K> list;

  public ToolkitSet(ToolkitList<K> list) {
    this.list = list;
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return list.contains(o);
  }

  @Override
  public Iterator<K> iterator() {
    return list.iterator();
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  @Override
  public boolean add(K e) {
    ToolkitReadWriteLock lock = list.getLock();
    lock.writeLock().lock();
    try {
      if (list.contains(e)) { return false; }

      return list.add(e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean remove(Object o) {
    return list.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends K> c) {
    boolean modified = false;

    ToolkitReadWriteLock lock = list.getLock();
    lock.writeLock().lock();
    try {
      for (K k : c) {
        if (add(k) && !modified) {
          modified = true;
        }
      }

      return modified;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  @Override
  public void clear() {
    list.clear();
  }

  public void destroy() {
    list.destroy();
  }

}
