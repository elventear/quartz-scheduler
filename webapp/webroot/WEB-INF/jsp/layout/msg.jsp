<%@ taglib uri="webwork" prefix="webwork" %>
  <webwork:if test="actionErrors != null && actionErrors.size() > 0">
  	<p>
	<font color="red">
	<b>Action errors:</b><br/>
	<ul> 
	<webwork:iterator value="actionErrors">
            <li><webwork:property /></li>
        </webwork:iterator>
	</ul>
	</font>
	</p>
    </webwork:if>
     <webwork:if test="fieldErrors != null && fieldErrors.size() > 0">
     	<p>
	<font color="red">
	<b>Field errors:</b><br/>
	<ul>
	<webwork:iterator value="fieldErrors">
            <li><webwork:property /></li>
	</webwork:iterator>
	</ul>
	</font>
	</p>
     </webwork:if>

