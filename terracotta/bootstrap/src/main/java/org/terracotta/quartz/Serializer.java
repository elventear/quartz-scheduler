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

import java.io.IOException;

public class Serializer {
  // private final SerializationStrategyImpl serializer = new SerializationStrategyImpl();

  // byte[] serialize(Object value) {
  // return serialize(value, true);
  // }
  //
  // private byte[] serialize(Object value, boolean retryOnNonSerializable) {
  // try {
  // return serializer.serialize(value);
  // } catch (NotSerializableException nse) {
  // if (retryOnNonSerializable) {
  // byte[] rv = springHack(value);
  // if (rv != null) return rv;
  // }
  // throw new RuntimeException(nse);
  // } catch (Exception e) {
  // throw new RuntimeException(e);
  // }
  // }
  //
  // private byte[] springHack(Object value) {
  // // Spring's JobDetailBean might be holding a reference to the applicationContext. If so, null it out temporarily
  // // while we serialize it
  //
  // Class<?> c = value.getClass();
  // while (c != null) {
  // if (c.getName().equals("org.springframework.scheduling.quartz.JobDetailBean")) {
  // Object restore = getAndSetContextField(value, null);
  // try {
  // return serialize(value, false);
  // } finally {
  // getAndSetContextField(value, restore);
  // }
  // } else if (c.getName().equals("org.springframework.scheduling.quartz.SimpleTriggerBean")
  // || c.getName().equals("org.springframework.scheduling.quartz.CronTriggerBean")) {
  // try {
  // Field jobDetailField = c.getDeclaredField("jobDetail");
  // jobDetailField.setAccessible(true);
  // Object jobDetail = jobDetailField.get(value);
  // Object restore = getAndSetContextField(jobDetail, null);
  // try {
  // return serialize(value, false);
  // } finally {
  // getAndSetContextField(jobDetail, restore);
  // }
  // } catch (Exception e) {
  // //
  // }
  // }
  //
  // c = c.getSuperclass();
  // }
  //
  // return null;
  // }
  //
  // static Object getAndSetContextField(Object target, Object context) {
  // Class<?> c = target.getClass();
  //
  // while (c != null) {
  // if (c.getName().equals("org.springframework.scheduling.quartz.JobDetailBean")) {
  // try {
  // Field contextField = c.getDeclaredField("applicationContext");
  // contextField.setAccessible(true);
  // Object prev = contextField.get(target);
  // contextField.set(target, context);
  // return prev;
  // } catch (Exception e) {
  // return null;
  // }
  // }
  // c = c.getSuperclass();
  // }
  //
  // return null;
  // }
  //
  // Object deserialize(byte[] data) {
  // try {
  // return serializer.deserialize(data);
  // } catch (Exception e) {
  // throw new RuntimeException(e);
  // }
  // }

  public String serializeToString(Object key) throws IOException {
    return SerializationHelper.serializeToString(key);
  }

  public Object deserializeFromString(String key) {
    try {
      return SerializationHelper.deserializeStringKey(key);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // public interface SerializationStrategy<T> {
  // public byte[] serialize(T serializable) throws IOException;
  //
  // public T deserialize(byte[] fromBytes) throws IOException, ClassNotFoundException;
  // }
  //
  // public class SerializationStrategyImpl implements SerializationStrategy {
  //
  // @Override
  // public byte[] serialize(Object serializable) throws IOException {
  // ByteArrayOutputStream baos = new ByteArrayOutputStream();
  // ObjectOutputStream oos = new ObjectOutputStream(baos);
  // oos.writeObject(serializable);
  // oos.flush();
  //
  // return baos.toByteArray();
  // }
  //
  // @Override
  // public Object deserialize(byte[] fromBytes) throws IOException, ClassNotFoundException {
  // ByteArrayInputStream bais = new ByteArrayInputStream(fromBytes);
  // ObjectInputStream inStream = new ObjectInputStream(bais);
  // return inStream.readObject();
  // }
  // }
}
