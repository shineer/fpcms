<%@page import="com.fpcms.model.*" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/commons/taglibs.jsp" %>

<duowan:override name="head">
	<title>${cmsChannel.channelName}</title>
</duowan:override>

<duowan:override name="content">
		<h1>${cmsChannel.channelName} : ${cmsChannel.channelDesc} 更新时间:${cmsChannel.dateLastModified} 创建时间:${cmsChannel.dateCreated}</h1>
		<div>
			${cmsChannel.content}
		</div>
</duowan:override>

<%-- jsp模板继承,具体使用请查看: http://code.google.com/p/rapid-framework/wiki/rapid_jsp_extends --%>
<%@ include file="base.jsp" %>