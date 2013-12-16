/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.terracotta.quartz.upgradability.serialization;


import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.TimeZone;

import org.junit.Test;
import org.quartz.CronExpression;

import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.junit.Assume.assumeThat;
import static org.terracotta.upgradability.serialization.SerializationUpgradabilityTesting.validateSerializedForm;

/**
 *
 * @author cdennis
 */
public class CronExpressionSerializationTest {

  private static final Comparator<CronExpression> COMPARATOR = new Comparator<CronExpression>() {

    @Override
    public int compare(CronExpression o1, CronExpression o2) {
      if (o1.getCronExpression().equals(o2.getCronExpression()) && o1.getTimeZone().equals(o2.getTimeZone())) {
        return 0;
      } else {
        return -1;
      }
    }
  };
  
  @Test
  public void testSimpleCron() throws ParseException, IOException, ClassNotFoundException {
    CronExpression expression = new CronExpression("0 0 12 * * ?");
    validateSerializedForm(expression, COMPARATOR, "serializedforms/CronExpressionSerializationTest.testSimpleCron.ser");
  }
  
  @Test
  public void testComplexCron() throws ParseException, IOException, ClassNotFoundException {
    CronExpression expression = new CronExpression("0 0/5 14,18,20-23 ? JAN,MAR,SEP MON-FRI 2002-2010");
    validateSerializedForm(expression, COMPARATOR, "serializedforms/CronExpressionSerializationTest.testComplexCron.ser");
  }
  
  @Test
  public void testCronWithTimeZone() throws ParseException, IOException, ClassNotFoundException {
    assumeThat(TimeZone.getAvailableIDs(), hasItemInArray("Antarctica/South_Pole"));
    CronExpression expression = new CronExpression("0 0 12 * * ?");
    expression.setTimeZone(new SimplisticTimeZone("Terra Australis"));
    validateSerializedForm(expression, COMPARATOR, "serializedforms/CronExpressionSerializationTest.testCronWithTimeZone.ser");
  }
}
