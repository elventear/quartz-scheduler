<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/fmt.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/displaytag-el-12.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tlds/webwork.tld" prefix="ww" %>
<h1><fmt:message key="title.listAllTriggers"/></h1>
<ww:set name="triggers" value="triggers" scope="request" />

<display:table name="triggers" class="simple" id="row" requestURI="listTriggers.action">
  <display:column  titleKey="label.global.actions" > 
  		<c:url var="joburl" value="/jobs/viewJob.action">
			<c:param name="jobName" value="${row.jobName}"/>
  			<c:param name="jobGroup" value="${row.jobGroup}"/>
		</c:url>   
	<a href='<c:out value="${joburl}"/>'><fmt:message key="label.global.view"/></a> &nbsp;  
  </display:column> 
  <display:column property="group" title="Job/Group" sortable="true" >
	  <c:out value="${row.jobName}" /> - <c:out value="${row.jobGroup}" /> 
  </display:column>
  <display:column property="group" titleKey="label.trigger.group" sortable="true"   />
  <display:column property="name" titleKey="label.trigger.name" sortable="true"  />
  <display:column property="description" titleKey="label.trigger.description" />
  <display:column property="class.name" titleKey="label.trigger.type" sortable="true"  />
  <display:column property="nextFireTime" titleKey="label.trigger.nextFireTime" sortable="true"  />
  <display:column property="startTime" titleKey="label.trigger.startTime" sortable="true"  />
  <display:column property="endTime" titleKey="label.trigger.stopTime" sortable="true"  />
  <display:column property="previousFireTime" titleKey="label.trigger.previousFireTime" sortable="true"  />
  <display:column property="misfireInstruction" titleKey="label.trigger.misFireInstruction" sortable="true"  />
</display:table>
