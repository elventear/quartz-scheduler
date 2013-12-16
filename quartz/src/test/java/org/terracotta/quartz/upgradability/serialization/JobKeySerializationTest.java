/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.terracotta.quartz.upgradability.serialization;


import java.io.IOException;
import org.junit.Test;
import org.quartz.JobKey;

import static org.terracotta.upgradability.serialization.SerializationUpgradabilityTesting.validateSerializedForm;

/**
 *
 * @author cdennis
 */
public class JobKeySerializationTest {

  @Test
  public void testWithoutGroup() throws IOException, ClassNotFoundException {
    JobKey key = new JobKey("foo");
    validateSerializedForm(key, "serializedforms/JobKeySerializationTest.testWithoutGroup.ser");
  }
  
  @Test
  public void testWithGroup() throws IOException, ClassNotFoundException {
    JobKey key = new JobKey("foo", "bar");
    validateSerializedForm(key, "serializedforms/JobKeySerializationTest.testWithGroup.ser");
  }
}
