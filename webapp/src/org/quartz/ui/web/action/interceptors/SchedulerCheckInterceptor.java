package org.quartz.ui.web.action.interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.ui.web.action.schedule.ScheduleBase;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
/**
 * @author Matthew Payne
 * @since 07/19/2005
 * 
 * SchedulerCheckInterceptor - returns to logon if 
 *
 */
public class SchedulerCheckInterceptor implements Interceptor {
    //~ Methods ////////////////////////////////////////////////////////////////

    private static final Log LOG = LogFactory.getLog(SchedulerCheckInterceptor.class);
    private static final String DEFAULT_FAIL_RESULT = "error" ;
    
    String failResult = DEFAULT_FAIL_RESULT;
    
    
    public String getFailResult() {
        return failResult;
    }
    public void setFailResult(String failResult) {
        this.failResult = failResult;
    }
   
    public void destroy() {
    
    }

    public void init() {
    }
 
    public String intercept(ActionInvocation invocation) throws Exception {

    	String actionName = invocation.getInvocationContext().getName();
    	Scheduler scheduler = ScheduleBase.getCurrentScheduler();
    	if (!scheduler.isInStandbyMode() || !scheduler.isShutdown()) {
    		
    		return invocation.invoke();
    	}	else {
    		if (LOG.isDebugEnabled()) {  	
        		LOG.debug("Scheduler not running or is down will not attempt to execute:" + actionName);
        	}
	    	
    		return failResult;
    	}
  
    }
    
}
