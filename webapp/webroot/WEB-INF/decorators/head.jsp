<% String root = request.getContextPath(); %>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td width="145" align="left"><a href="http://www.quartzscheduler.com/" target="_blank"><img src="<%= request.getContextPath()%>/icons/quartzEJS.jpg"/></a></td>
		<td width="10" valign="bottom">
		&nbsp;	<%  if (request.getRemoteUser() != null){ %>
			User:  <%=request.getRemoteUser()%> <a href="<%=root%>/logout.action">[Sign out] </a>
			<% } else { %>
				<a href="<%=root%>/logout.action">[Sign In]</a>
		 <%	}  %>
		 </td>
		<td></td>
	</tr>
	<tr><td height="10" width="145"  colspan="3" class="nav" >&nbsp; </td>
	</tr>
</table>
