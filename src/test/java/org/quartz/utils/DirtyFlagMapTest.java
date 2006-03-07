/*
 * Copyright NLG 2006
 */
package org.quartz.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.quartz.utils.DirtyFlagMap;

import junit.framework.TestCase;

/**
 * Unit test for DirtyFlagMap.  These tests focus on making
 * sure the isDirty flag is set correctly.
 */
public class DirtyFlagMapTest extends TestCase {

    public void testClear() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        assertFalse(dirtyFlagMap.isDirty());
        
        dirtyFlagMap.clear();
        assertFalse(dirtyFlagMap.isDirty());
        dirtyFlagMap.put("X", "Y");
        dirtyFlagMap.clearDirtyFlag();
        dirtyFlagMap.clear();
        assertTrue(dirtyFlagMap.isDirty());
    }
    
    public void testPut() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        dirtyFlagMap.put("a", "Y");
        assertTrue(dirtyFlagMap.isDirty());
    }
    
    public void testRemove() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        dirtyFlagMap.put("a", "Y");
        dirtyFlagMap.clearDirtyFlag();
        
        dirtyFlagMap.remove("b");
        assertFalse(dirtyFlagMap.isDirty());

        dirtyFlagMap.remove("a");
        assertTrue(dirtyFlagMap.isDirty());
    }
    
    public void testEntrySetRemove() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        Set entrySet = dirtyFlagMap.entrySet();
        dirtyFlagMap.remove("a");
        assertFalse(dirtyFlagMap.isDirty());
        dirtyFlagMap.put("a", "Y");
        dirtyFlagMap.clearDirtyFlag();
        entrySet.remove("b");
        assertFalse(dirtyFlagMap.isDirty());
        entrySet.remove(entrySet.iterator().next());
        assertTrue(dirtyFlagMap.isDirty());
    }

    public void testEntrySetRetainAll() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        Set entrySet = dirtyFlagMap.entrySet();
        dirtyFlagMap.clear();
        dirtyFlagMap.clearDirtyFlag();
        entrySet.retainAll(Collections.EMPTY_LIST);
        assertFalse(dirtyFlagMap.isDirty());
        dirtyFlagMap.put("a", "Y");
        dirtyFlagMap.clearDirtyFlag();
        entrySet.retainAll(Collections.singletonList(entrySet.iterator().next()));
        assertFalse(dirtyFlagMap.isDirty());
        entrySet.retainAll(Collections.EMPTY_LIST);
        assertTrue(dirtyFlagMap.isDirty());
    }
    
    public void testEntrySetRemoveAll() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        Set entrySet = dirtyFlagMap.entrySet();
        dirtyFlagMap.clear();
        dirtyFlagMap.clearDirtyFlag();
        entrySet.removeAll(Collections.EMPTY_LIST);
        assertFalse(dirtyFlagMap.isDirty());
        dirtyFlagMap.put("a", "Y");
        dirtyFlagMap.clearDirtyFlag();
        entrySet.removeAll(Collections.EMPTY_LIST);
        assertFalse(dirtyFlagMap.isDirty());
        entrySet.removeAll(Collections.singletonList(entrySet.iterator().next()));
        assertTrue(dirtyFlagMap.isDirty());
    }
    
    public void testEntrySetClear() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        Set entrySet = dirtyFlagMap.entrySet();
        dirtyFlagMap.clear();
        dirtyFlagMap.clearDirtyFlag();
        entrySet.clear();
        assertFalse(dirtyFlagMap.isDirty());
        dirtyFlagMap.put("a", "Y");
        dirtyFlagMap.clearDirtyFlag();
        entrySet.clear();
        assertTrue(dirtyFlagMap.isDirty());
    }        

    public void testEntrySetIterator() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        Set entrySet = dirtyFlagMap.entrySet();
        dirtyFlagMap.clear();
        dirtyFlagMap.put("a", "A");
        dirtyFlagMap.put("b", "B");
        dirtyFlagMap.put("c", "C");
        dirtyFlagMap.clearDirtyFlag();
        Iterator entrySetIter = entrySet.iterator();
        Map.Entry entryToBeRemoved = (Map.Entry)entrySetIter.next();
        String removedKey = (String)entryToBeRemoved.getKey();
        entrySetIter.remove();
        assertEquals(2, dirtyFlagMap.size());
        assertTrue(dirtyFlagMap.isDirty());
        assertFalse(dirtyFlagMap.containsKey(removedKey));
        dirtyFlagMap.clearDirtyFlag();
        Map.Entry entry = (Map.Entry)entrySetIter.next();
        entry.setValue("BB");
        assertTrue(dirtyFlagMap.isDirty());
        assertTrue(dirtyFlagMap.containsValue("BB"));
    }

    public void testEntrySetToArray() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        Set entrySet = dirtyFlagMap.entrySet();
        dirtyFlagMap.clear();
        dirtyFlagMap.put("a", "A");
        dirtyFlagMap.put("b", "B");
        dirtyFlagMap.put("c", "C");
        dirtyFlagMap.clearDirtyFlag();
        Object[] array = entrySet.toArray();
        assertEquals(3, array.length);
        Map.Entry entry = (Map.Entry)array[0];
        entry.setValue("BB");
        assertTrue(dirtyFlagMap.isDirty());
        assertTrue(dirtyFlagMap.containsValue("BB"));
    }

    public void testEntrySetToArrayWithArg() {
        DirtyFlagMap dirtyFlagMap = new DirtyFlagMap();
        Set entrySet = dirtyFlagMap.entrySet();
        dirtyFlagMap.clear();
        dirtyFlagMap.put("a", "A");
        dirtyFlagMap.put("b", "B");
        dirtyFlagMap.put("c", "C");
        dirtyFlagMap.clearDirtyFlag();
        Object[] array = entrySet.toArray(new Map.Entry[] {});
        assertEquals(3, array.length);
        Map.Entry entry = (Map.Entry)array[0];
        entry.setValue("BB");
        assertTrue(dirtyFlagMap.isDirty());
        assertTrue(dirtyFlagMap.containsValue("BB"));
    }
    
}
