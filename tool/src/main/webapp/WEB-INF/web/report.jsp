<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

<link href="<c:url value="/css/summary.css"/>" rel="stylesheet">


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
						<li><a href="<c:url value="/projects/${execution.project.name}/execution/${execution.id}"/>">${execution.name}</a><span class="divider">/</span></li>												
						<li id="titleName">Summary report</li>
					</ul>
				</div>

				<div class="row-fluid sortable">
					<div class="box span12">
						<div class="box-header well" data-original-title>
						<h2><i class="icon-inbox"></i> Summary for Project ${execution.project.name} - Execution "${execution.name}"</h2>
					</div>
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable" id="summary_table">
						 <thead>
                    <tr>
                        <th></th>
                        <th>TESTREP</th>
                        <th>TESTMODNOTREP</th>
                        <th colspan="3" class="testDelLeft testDelRight">TESTDEL</th>
                        <th colspan="3" style="text-align: center">TESTADD</th>
                    </tr>
                    <tr>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th class="testDelLeft">(AE|RE)</th>
                        <th>(CE)</th>
                        <th class="testDelRight">(P)</th>
                        <th>(AE|RE)</th>
                        <th>(CE)</th>
                        <th>(P)</th>
                    </tr>
                </thead>
                	<tbody>
                		<tr id="loader"><td colspan="9">
                			<img src="<c:url value="/img/ajax-loaders/ajax-loader-5.gif"/>">
                		</td></tr>
                	</tbody>
                	
					  </table>            
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
	<script type="text/javascript" src="<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/script/summary.js"/>"></script>
	<script type="text/javascript">
	var versions = getVersions();
	var olData = "";
	var summaryInfo = new Array(0, 0, 0, 0, 0, 0, 0, 0);

	for(var i = 1; i < versions.length; i++) {
		var version = versions[i];
		document.write('<script type="text/javascript" src=<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/script/"/>' + version + '.js"><\/script>');
	}

	$(function() {
		for(var i = 1; i < versions.length; i++) {
			var version = versions[i];
			for(var j = 0; j < 8; j++) {
				summaryInfo[j] += versionsSummary[version][j];
			}
		}
		$("#versions").html(olData);
		versionsSummary["total"] = summaryInfo;
		populateTotals(versionsSummary);
		createSummaryTable($("#summary_table"), versions, versionsSummary, '<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/version"/>');
		$("#loader").hide();
	});
</script>

	</body>
</html>