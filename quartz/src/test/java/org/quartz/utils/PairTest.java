/* 
 * Copyright 2001-2009 Terracotta, Inc. 
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
 */
package org.quartz.utils;

import junit.framework.TestCase;

/**
 * Unit test for the Pair class.
 */
public class PairTest extends TestCase {

    public void testPair() {
        Pair p = new Pair();
        assertNull(p.getFirst());
        assertNull(p.getSecond());
        p.setFirst("one");
        p.setSecond("two");
        assertEquals("one", p.getFirst());
        assertEquals("two", p.getSecond());

        Pair p2 = new Pair();
        p2.setFirst("one");
        p2.setSecond("2");
        assertFalse(p.equals(p2));
        p2.setSecond("two");
        assertEquals(p, p2);
    }

    public void testQuartz625() {
        Pair p = new Pair();

        Pair p2 = new Pair();
        p2.setFirst("one");
        assertFalse(p.equals(p2));

        Pair p3 = new Pair();
        p3.setSecond("two");
        assertFalse(p.equals(p3));

        Pair p4 = new Pair();
        p4.setFirst("one");
        p4.setSecond("two");
        assertFalse(p.equals(p4));

        Pair p5 = new Pair();
        assertEquals(p, p5);
    }

}
