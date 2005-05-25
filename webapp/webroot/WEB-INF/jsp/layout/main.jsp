<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="webwork" %>
<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<html>
  <head>
    <title><decorator:title/></title>
    <link rel="stylesheet" href="decorators/style.css">
    <c:url var="url" value="/style/default.css"/>
    <c:url var="displaytag_css" value="/style/display.css"/>
    <link rel=stylesheet type=text/css href='<c:out value="${url}"/>'/>
    <link rel=stylesheet type=text/css href='<c:out value="${displaytag_css}"/>'/>
    <decorator:head/>
  </head>
  	<body>
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr><td><jsp:include page="/WEB-INF/jsp/layout/head.jsp"/></td></tr>
			<tr>
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td width="145" height="100%" valign="top"><jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/></td>
						<td width="10">&nbsp;</td>
						<td ><!--insert attribute="bodyhead"--><decorator:body/></td>
						  <webwork:if test="(actionErrors != null && actionErrors.size() > 0) || (fieldErrors != null && fieldErrors.size() > 0)">
						<td valign="top">
							<page:applyDecorator name="window" title="Errors" page="/WEB-INF/jsp/layout/msg.jsp"  />
						</td>
						</webwork:if>
					</tr>
				</table>
			</tr>
			<tr><td><jsp:include page="/WEB-INF/jsp/layout/foot.jsp"/></td></tr>
		</table>
	</body>
</html>
