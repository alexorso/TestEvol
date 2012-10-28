<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

<style type="text/css">


</style>
</head>

<body>

	<!-- topbar starts -->
	<%@include file="includes/navbar.jsp" %>
	<!-- topbar ends -->
	<div class="container-fluid">
		<div class="row-fluid">

			<!-- left menu starts -->
			<%@include file="includes/leftmenu.jsp" %>
			<!-- left menu ends -->

			<div id="content" class="span10">
				<!-- content starts -->


				<div>
					<ul class="breadcrumb">
						<li><a href="#">Home</a> <span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/list"/>">Projects</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}"/>">Project ${execution.project.name}</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}/executions"/>">Executions</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}/executions/${execution.id}"/>">${execution.name}</a><span class="divider">/</span></li>												
						<li id="titleName">TestEvol Report</li>
					</ul>
				</div>

				<div class="row-fluid sortable">
					<div class="box span12">
						<div class="box-content">
							Aiiiiiii
						</div>
					</div>
					<!--/span-->
				</div>
				<!--/row-->

				<!-- content ends -->
			</div>
			<!--/#content.span10-->
		</div>
		<!--/fluid-row-->

		<hr>

	</div>
	<!--/.fluid-container-->
	
	<%@include file="includes/scripts.jsp" %>

	</body>
</html>