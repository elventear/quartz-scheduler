<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/fmt.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/displaytag-el-12.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tlds/webwork.tld" prefix="ww" %>

<ww:set name="jobz" value="jobs" scope="request" />

<h1><fmt:message key="title.listAllJobs"/></h1>
<!--decorator="org.quartz.ui.Decorator"  -->
<display:table name="jobz" class="simple" id="row" >
  <display:column  titleKey="label.global.actions" > 
  		<c:url var="viewurl" value="/jobs/viewJob.action">
			<c:param name="jobName" value="${row.name}"/>
  			<c:param name="jobGroup" value="${row.group}"/>
		</c:url>   
		<c:url var="editurl" value="/jobs/editJob.action">
			<c:param name="jobName" value="${row.name}"/>
  			<c:param name="jobGroup" value="${row.group}"/>
		</c:url>   
  		<c:url var="exeurl" value="/jobs/executeJob.action">
			<c:param name="jobName" value="${row.name}"/>
  			<c:param name="jobGroup" value="${row.group}"/>
  			<c:param name="executeJobAction" value="execute"/>
		</c:url>   
	<a href='<c:out value="${viewurl}"/>'><fmt:message key="label.global.view"/></a> |
	<a href='<c:out value="${editurl}"/>'><fmt:message key="label.global.edit"/></a> |
	<a href='<c:out value="${exeurl}"/>'><fmt:message key="label.global.execute"/></a> &nbsp;  
  </display:column> 

  <display:column property="group" titleKey="label.job.group" sortable="true"   />
  <display:column property="name" titleKey="label.job.name" sortable="true"  />
  <display:column property="description" titleKey="label.job.description" />
  <display:column property="jobClass" titleKey="label.job.jobClass" sortable="true"  />

</display:table>
