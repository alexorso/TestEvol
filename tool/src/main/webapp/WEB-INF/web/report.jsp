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
						<li><a href="<c:url value="/projects/list"/>">Projects</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}"/>">Project ${execution.project.name}</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}/executions"/>">Analysis</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}/execution/${execution.id}"/>">${execution.name}</a><span class="divider">/</span></li>												
						<li id="titleName">Summary report</li>
					</ul>
				</div>

				<div class="row-fluid sortable">
					<div class="box span12">
						<div class="box-header well" data-original-title>
						<h2><i class="icon-inbox"></i> Summary for Project ${execution.project.name} - Analysis "${execution.name}"</h2>
						<div class="box-icon">
							<a href="<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/csv"/>" class="btn btn-round" title="Click here to download the report as a text file." data-rel="tooltip"><i class="icon-download"></i></a>
						</div>
					</div>
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable" id="summary_table">
						 <thead>
                    <tr>
                        <th></th>
                        <th><a href="#" id="toolTipCatTestRep" title="Tooltip, you can change the position." data-rel="tooltip">TESTREP</a></th>
                        <th><a href="#" id="toolTipCatTestModNotRep" title="Tooltip, you can change the position." data-rel="tooltip">TESTMODNOTREP</a></th>
                        <th colspan="3" class="testDelLeft testDelRight"><a href="#" id="toolTipCatTestDel" title="Tooltip, you can change the position." data-rel="tooltip">TESTDEL</a></th>
                        <th colspan="3" style="text-align: center"><a href="#" id="toolTipTestAdd" title="Tooltip, you can change the position." data-rel="tooltip">TESTADD</a></th>
                    </tr>
                    <tr>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th class="testDelLeft"><a href="#" id="toolTipCatTestDelAeRe" title="Tooltip, you can change the position." data-rel="tooltip">(AE|RE)</a></th>
                        <th><a href="#" id="toolTipCatTestDelCe" title="Tooltip, you can change the position." data-rel="tooltip">(CE)</a></th>
                        <th class="testDelRight"><a href="#" id="toolTipCatTestP" title="Tooltip, you can change the position." data-rel="tooltip">(P)</a></th>
                        <th><a href="#" id="toolTipCatTestAddAeRe" title="Tooltip, you can change the position." data-rel="tooltip">(AE|RE)</a></th>
                        <th><a href="#" id="toolTipCatTestAddCe" title="Tooltip, you can change the position." data-rel="tooltip">(CE)</a></th>
                        <th><a href="#" id="toolTipCatTestAddP" title="Tooltip, you can change the position." data-rel="tooltip">(P)</a></th>
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
	<script src='<c:url value="/js/tooltips_report.js"/>'></script>
		
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
		setTooltips();
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