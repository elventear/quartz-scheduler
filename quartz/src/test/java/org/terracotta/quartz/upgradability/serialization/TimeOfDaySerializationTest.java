/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.terracotta.quartz.upgradability.serialization;


import java.io.IOException;
import org.junit.Test;
import org.quartz.TimeOfDay;

import static org.terracotta.upgradability.serialization.SerializationUpgradabilityTesting.validateSerializedForm;

/**
 *
 * @author cdennis
 */
public class TimeOfDaySerializationTest {

  @Test
  public void testMidday() throws IOException, ClassNotFoundException {
    TimeOfDay tod = new TimeOfDay(12, 00);
    validateSerializedForm(tod, "serializedforms/TimeOfDaySerializationTest.testMidday.ser");
  }

  @Test
  public void testMidnight() throws IOException, ClassNotFoundException {
    TimeOfDay tod = new TimeOfDay(00, 00);
    validateSerializedForm(tod, "serializedforms/TimeOfDaySerializationTest.testMidnight.ser");
  }

  @Test
  public void testEagle() throws IOException, ClassNotFoundException {
    TimeOfDay tod = new TimeOfDay(20, 17, 40);
    validateSerializedForm(tod, "serializedforms/TimeOfDaySerializationTest.testEagle.ser");
  }
}
