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
package org.terracotta.quartz;

import org.terracotta.toolkit.Toolkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TerracottaJobStore extends AbstractTerracottaJobStore {
  private static final String REAL_JOB_STORE_CLASS_NAME = "org.terracotta.quartz.PlainTerracottaJobStore";

  @Override
  TerracottaJobStoreExtensions getRealStore(Toolkit toolkit) {
    try {
      Constructor c = Class.forName(REAL_JOB_STORE_CLASS_NAME).getConstructor(new Class[] {});
      return (TerracottaJobStoreExtensions) c.newInstance(new Object[] {});
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

}
