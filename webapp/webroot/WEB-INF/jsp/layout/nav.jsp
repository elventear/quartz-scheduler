
<%  String root = request.getContextPath(); %>
<table width="145" height="100%" class="nav" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td width="142" valign="top">
		<a href="<%=root%>/schedule/scheduleControl.action">Schedule Control</a>
		<td width="3" >&nbsp;</td>
	</tr>
	<tr>
		<td width="142" valign="top">
		<a href="<%=root%>/definition/list.action">Job Definitions</a>
		<td width="3" >&nbsp;</td>
	</tr>
	<tr>
		<td width="142" valign="top" ><a href="<%=root%>/jobs/createJob.action">Create Job</a></td>
		<td width="3" >&nbsp;</td>
	</tr>
	<tr>
		<td width="142" valign="top" ><a href="<%=root%>/schedule/listJobs.action">List Jobs</a></td>
		<td width="3" >&nbsp;</td>
	</tr>
	<tr>
		<td width="142" valign="top" ><a href="<%=root%>/schedule/listTriggers.action">List all Triggers</a></td>
		<td width="3" >&nbsp;</td>
	</tr>
	<tr>
		<td width="142" valign="top" ><a href="<%=root%>/quartzLog.action">Logging</a></td>
		<td width="3" >&nbsp;</td>
	</tr>
	<tr height="100%">
		<td width="142" valign="top" >&nbsp;</td>
		<td width="3" >&nbsp;</td>
	</tr>
</table>