package org.quartz.integrations.tests;

import org.quartz.simpl.CascadingClassLoadHelper;


/**
 * 
 * Always mimic CascadingClassLoadHelper except...
 * when looking for OnlyVisibleByDummyClassLoadHelperJob
 * 
 * @author adahanne
 *
 */
public class DummyClassLoadHelper extends CascadingClassLoadHelper {
  
  @Override
  public Class<?> loadClass(String className) throws ClassNotFoundException {
	  if(className.equals("imaginary.class.OnlyVisibleByDummyClassLoadHelperJob")){
		  //we just translate the className ! Only DummyClassLoadHelper is able to do that !
		  className = "org.quartz.integrations.tests.HelloJob";
	  }
	  return super.loadClass(className);
	  
  }

}