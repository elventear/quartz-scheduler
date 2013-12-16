/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.terracotta.quartz.upgradability.serialization;


import java.io.IOException;
import org.junit.Test;
import org.quartz.TriggerKey;

import static org.terracotta.upgradability.serialization.SerializationUpgradabilityTesting.validateSerializedForm;

/**
 *
 * @author cdennis
 */
public class TriggerKeySerializationTest {

  @Test
  public void testWithoutGroup() throws IOException, ClassNotFoundException {
    TriggerKey key = new TriggerKey("foo");
    validateSerializedForm(key, "serializedforms/TriggerKeySerializationTest.testWithoutGroup.ser");
  }
  
  @Test
  public void testWithGroup() throws IOException, ClassNotFoundException {
    TriggerKey key = new TriggerKey("foo", "bar");
    validateSerializedForm(key, "serializedforms/TriggerKeySerializationTest.testWithGroup.ser");
  }
}
