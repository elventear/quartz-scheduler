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
package org.quartz.impl.matchers;

import org.quartz.Matcher;
import org.quartz.utils.Key;

/**
 * Matches on group (ignores name) property of Keys.
 *  
 * @author jhouse
 */
public class GroupMatcher<T extends Key> extends StringMatcher<T> {

    protected GroupMatcher(String compareTo, StringOperatorName compareWith) {
        super(compareTo, compareWith);
    }
    
    public static GroupMatcher matchGroupEquals(String compareTo) {
        return new GroupMatcher(compareTo, StringOperatorName.EQUALS);
    }

    public static GroupMatcher matchGroupStartsWith(String compareTo) {
        return new GroupMatcher(compareTo, StringOperatorName.STARTS_WITH);
    }

    public static GroupMatcher matchGroupEndsWith(String compareTo) {
        return new GroupMatcher(compareTo, StringOperatorName.ENDS_WITH);
    }

    public static GroupMatcher matchGroupContains(String compareTo) {
        return new GroupMatcher(compareTo, StringOperatorName.CONTAINS);
    }

    @Override
    protected String getValue(T key) {
        return key.getGroup();
    }
    
}
