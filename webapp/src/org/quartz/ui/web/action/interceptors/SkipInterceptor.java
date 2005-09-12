package org.quartz.ui.web.action.interceptors;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
/**
 * @author Matthew Payne
 * @since 12/19/2004
 * 
 * SkipInterceptor - skips executing an action if a "skipKey is present in parameters
 * Useful for a cancel result that does not require actions to implement any logic regarding "cancel" operations.
 * 
 * 
 * 	<action name="viewFoo" class="fooClass" >
			<interceptor-ref name="defaultComponentStack"/>
			<interceptor-ref name="skipIntercetor"/>
			
			<result name="success" type="velocity">edit.vm</result>
			<result name="error" type="velocity">error.vm</result>
			<result name="cancel">list.action</result>
	</action>
 * 
 * 
 * e.g. <!-- if action has "cancel" clicked/submited <input type="submit" name="cancel value="blah"/> cancel is result is executed -->
 *
 * skip "key" and result names can be overridden in the defintion
 *
 *          <interceptor name="doDat" class="com.diamondip.common.xwork.interceptors.SkipInterceptor">
                <param name="skipKey">jump</param>
                <param name="skipResultName">jump</param>
            </interceptor>      
 * 
 *
 *
 */
public class SkipInterceptor implements Interceptor {
    //~ Methods ////////////////////////////////////////////////////////////////

    private static final Log LOG = LogFactory.getLog(SkipInterceptor.class);
    private static final String DEFAULT_RESULT = "cancel" ;
    private static final String DEFAULT_SKIP_KEY = "cancel" ;
    
    private static final String PARAM_SKIP_KEY = "skipKey" ;
    private static final String PARAM_RESULT_NAME = "skipResultName" ;
    
    String skipResultName = DEFAULT_RESULT;
    String skipKey = DEFAULT_SKIP_KEY;
    
    
    /**
     * @return Returns the skipKey.
     */
    public String getSkipKey() {
        return skipKey;
    }
    /**
     * @param skipKey The skipKey to set.
     */
    public void setSkipKey(String skipKey) {
        this.skipKey = skipKey;
    }
    /**
     * @return Returns the skipResultName.
     */
    public String getSkipResultName() {
        return skipResultName;
    }
    /**
     * @param skipResultName The skipResultName to set.
     */
    public void setSkipResultName(String skipResultName) {
        this.skipResultName = skipResultName;
    }
    public void destroy() {
    }

    public void init() {
    }
 
    public String intercept(ActionInvocation invocation) throws Exception {

        final Map params = ActionContext.getContext().getParameters();
        String actionName = invocation.getInvocationContext().getName();

        if (params.containsKey(skipKey)) {
        	if (LOG.isDebugEnabled()) {  	
        		LOG.debug("Skipping out of action=[" +  actionName + "] and returning result:" + skipResultName);
        	}
        	return skipResultName;
        } else {
        	if (LOG.isDebugEnabled()) {  	
        		LOG.debug("Continuing action=[" +  actionName + "]  as normal");
        	}
        	return invocation.invoke();
        }
        
    }
}
