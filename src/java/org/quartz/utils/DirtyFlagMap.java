/* 
 * Copyright 2004-2005 OpenSymphony 
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

/*
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An implementation of <code>Map</code> that wraps another <code>Map</code>
 * and flags itself 'dirty' when it is modified.
 * </p>
 * 
 * @author James House
 */
public class DirtyFlagMap implements Map, Cloneable, java.io.Serializable {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    private static final long serialVersionUID = 1433884852607126222L;

    private boolean dirty = false;
    private transient boolean locked = false;
    private Map map;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a DirtyFlagMap that 'wraps' the given <code>Map</code>.
     * </p>
     */
    public DirtyFlagMap(Map mapToWrap) {
        if (mapToWrap == null)
                throw new IllegalArgumentException("mapToWrap cannot be null!");

        map = mapToWrap;
    }

    /**
     * <p>
     * Create a DirtyFlagMap that 'wraps' a <code>HashMap</code>.
     * </p>
     * 
     * @see java.util.HashMap
     */
    public DirtyFlagMap() {
        map = new HashMap();
    }

    /**
     * <p>
     * Create a DirtyFlagMap that 'wraps' a <code>HashMap</code> that has the
     * given initial capacity.
     * </p>
     * 
     * @see java.util.HashMap
     */
    public DirtyFlagMap(int initialCapacity) {
        map = new HashMap(initialCapacity);
    }

    /**
     * <p>
     * Create a DirtyFlagMap that 'wraps' a <code>HashMap</code> that has the
     * given initial capacity and load factor.
     * </p>
     * 
     * @see java.util.HashMap
     */
    public DirtyFlagMap(int initialCapacity, float loadFactor) {
        map = new HashMap(initialCapacity, loadFactor);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */


    public void setMutable(boolean mutable) {
        this.locked = !mutable;
        
        map = (locked) ? Collections.unmodifiableMap(map) : new HashMap(map);
    }
    
    
    public boolean isMutable() {
        return !locked;
    }
    
    /**
     * <p>
     * Clear the 'dirty' flag (set dirty flag to <code>false</code>).
     * </p>
     */
    public void clearDirtyFlag() {
        dirty = false;
    }

    /**
     * <p>
     * Determine whether the <code>Map</code> is flagged dirty.
     * </p>
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * <p>
     * Get a direct handle to the underlying Map.
     * </p>
     */
    public Map getWrappedMap() {
        return map;
    }

    public void clear() {
        if (map.isEmpty() == false) {
            dirty = true;
        }
        
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object val) {
        return map.containsValue(val);
    }

    public Set entrySet() {
        if (locked) {
            return map.entrySet();
        } else {
            return new DirtyFlagMapEntrySet(map.entrySet());
        }
    }
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DirtyFlagMap)) return false;

        return map.equals(((DirtyFlagMap) obj).getWrappedMap());
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Object put(Object key, Object val) {
        dirty = true;

        return map.put(key, val);
    }

    public void putAll(Map t) {
        if (!t.isEmpty()) dirty = true;

        map.putAll(t);
    }

    public Object remove(Object key) {
        Object obj = map.remove(key);

        if (obj != null) dirty = true;

        return obj;
    }

    public int size() {
        return map.size();
    }

    public Collection values() {
        return map.values();
    }

    public Object clone() {
        DirtyFlagMap copy;
        try {
            copy = (DirtyFlagMap) super.clone();
            if (map instanceof HashMap)
                    copy.map = (Map) ((HashMap) map).clone();
        } catch (CloneNotSupportedException ex) {
            throw new IncompatibleClassChangeError("Not Cloneable.");
        }

        return copy;
    }
    

    /**
     * Wrap a Map.Entry Set so we can mark the Map as dirty if 
     * the Set is modified, and return Map.Entry objects
     * wrapped in the <code>DirtyFlagMapEntry</code> class.
     */
    private class DirtyFlagMapEntrySet implements Set {
        private Set set;
        
        public DirtyFlagMapEntrySet(Set set) {
            this.set = set;
        }
        
        public Iterator iterator() {
            return new DirtyFlagMapEntryIterator(set.iterator());
        }

        public Object[] toArray() {
            return toArray(new Object[size()]);
        }

        public Object[] toArray(Object[] array) {
            if (array.getClass().getComponentType().isAssignableFrom(Map.Entry.class) == false) {
                throw new IllegalArgumentException("Array must be of type assignable from Map.Entry");
            }
            
            int size = size();
            
            Object[] result = 
                (array.length < size) ? 
                    (Object[])Array.newInstance(array.getClass().getComponentType(), size) : array;

            Iterator entryIter = iterator();
            for (int i = 0; i < size; i++) {
                result[i] = entryIter.next();
            }
            
            if (result.length > size) {
                result[size] = null;
            }
            
            return result;
        }

        public boolean remove(Object o) {
            boolean removed = set.remove(o);
            if (removed) {
                dirty = true;
            }
            return removed;
        }

        public boolean retainAll(Collection c) { 
            boolean changed = set.retainAll(c);
            if (changed) {
                dirty = true;
            }
            return changed;
        }

        public boolean removeAll(Collection c) {
            boolean changed = set.removeAll(c);
            if (changed) {
                dirty = true;
            }
            return changed;
        }

        public void clear() {
            if (set.isEmpty() == false) {
                dirty = true;
            }
            set.clear();
        }
        
        // Pure wrapper methods
        public int size() { return set.size(); }
        public boolean isEmpty() { return set.isEmpty(); }
        public boolean contains(Object o) { return set.contains(o); }
        public boolean add(Object o) { return add(o); } // Not supported
        public boolean addAll(Collection c) { return set.addAll(c); } // Not supported
        public boolean containsAll(Collection c) { return set.containsAll(c); }
    }
    
    /**
     * Wrap an Iterator over Map.Entry objects so that we can
     * mark the Map as dirty if an element is removed or modified. 
     */
    private class DirtyFlagMapEntryIterator implements Iterator {
        Iterator iterator;
        
        public DirtyFlagMapEntryIterator(Iterator iterator) {
            this.iterator = iterator;
        }
        
        public Object next() {
            return new DirtyFlagMapEntry((Map.Entry)iterator.next());
        }

        public void remove() {
            dirty = true;
            iterator.remove();
        }
        
        // Pure wrapper methods
        public boolean hasNext() { return iterator.hasNext(); }
    }
    
    /**
     * Wrap a Map.Entry so we can mark the Map as dirty if 
     * a value is set.
     */
    private class DirtyFlagMapEntry implements Map.Entry {
        private Map.Entry entry;
        
        public DirtyFlagMapEntry(Map.Entry entry) {
            this.entry = entry;
        }
        
        public Object setValue(Object o) {
            dirty = true;
            return entry.setValue(o);
        }
        
        // Pure wrapper methods
        public Object getKey() { return entry.getKey(); }
        public Object getValue() { return entry.getValue(); }
    }
}