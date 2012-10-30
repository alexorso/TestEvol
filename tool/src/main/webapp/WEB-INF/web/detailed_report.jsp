<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>
 <style type="text/css">
			#summary tr td:nth-child(1) {
				cursor: pointer;
				text-decoration: underline;
			}

			#new del {
				display: none;
			}

			#old ins {
				display: none;
			}

			.dialog_label, .dialog_value {
				font-size: 14px;
			}

			.dialog_label {
				font-weight: bold;
				margin-right: 5px;
			}
			#dialog{
				display: none;
			}
			#headerCateogry{
				margin-bottom:10px;
				font-size:15px;
				font-weight: bold;
			}
			.activeCategory{
				color: #555555;
				cursor: default;
				background-color: #ffffff;
			}
        </style>
</head>
<body>

	<!-- topbar starts -->
	<%@include file="includes/navbar.jsp" %>
	<!-- topbar ends -->
	<div class="container-fluid">
		<div class="row-fluid">
			<!-- left menu starts -->
			<%@include file="includes/leftmenu_det_report.jsp" %>
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
						<li><a href="<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report"/>">Summary report</a><span class="divider">/</span></li>
						<li id="titleName">Detailed report for versions <span id="oldVersionTitle"></span> and <span id="newVersionTitle"></span></li>
					</ul>
				</div>

				<div class="row-fluid sortable">
					<div class="box span12">
						<div class="box-header well" data-original-title>
						<h2> Comparison between versions <span id="oldVersion"></span> and <span id="newVersion"></span></h2>
					</div>
					<div class="box-content">
						<div id="headerCateogry">Tests on category </div>
						<table class="table table-striped table-bordered bootstrap-datatable" id="dataTable">
						 
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
		
		<div id="dialog" title="Test details">
            <div>
                <span class="dialog_label">Package:</span><span class="dialog_value" id="package">br.com.teste</span>
            </div>
            <div>
                <span class="dialog_label">Class:</span><span class="dialog_value" id="class">class</span>
            </div>
            <div style="margin-bottom:5px;">
                <span class="dialog_label">Test name:</span><span class="dialog_value" id="test">testname</span>
            </div>
            <div>
                <pre class="prettyprint linenums" id="both" style="display:none;"></pre>
                <pre class="prettyprint linenums" id="old"></pre>
                <pre class="prettyprint linenums" id="new"></pre>
            </div>
        </div>
        <div id="dialog_coverage" title="Changes on Coverage">
            <div id="coverage_data"></div>
        </div>
		

		<hr>

	</div>
	<!--/.fluid-container-->
	
	<%@include file="includes/scripts.jsp" %>
	<script type="text/javascript" src="<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/script/summary.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/script/${version}.js"/>"></script>
	
	<link href="<c:url value="/js/google-code-prettify/prettify.css"/>" type="text/css" rel="stylesheet" />
    <script type="text/javascript" src="<c:url value="/js/google-code-prettify/prettify.js"/>"></script>
	
	<script type="text/javascript"></script>
 <script>

		showingInLine = false;
		var tests = new Array();
		$(function() {
			// Accordion

            populateSummaryBody();
			$("#totalOfTests").html(getTotalOfTests());
			$("#newVersion").html(getVersionName());
			$("#newVersionTitle").html(getVersionName());
			$("#oldVersion").html(getOldVersionName());
			$("#oldVersionTitle").html(getOldVersionName());			
			
			createTable(getCategoryLabel(0));

			$("#dialog").dialog({
				autoOpen : false,
				width : 'auto',
				modal : true
			});

            $("#dialog_coverage").dialog({
                autoOpen : false,
                width : 'auto',
                modal : true,
                position : ['center', 30],
                width : "600px"
            });

			var dialog = $("#dialog").dialog({
				position : ['center', 30],
				width : "1000px"
			});

			var titlebar = dialog.parents('.ui-dialog').find('.ui-dialog-titlebar');

			$('<a href="#" id="change-diff" class="ui-dialog-titlebar-close ui-corner-all" role="button" style="margin-right:30px;"><span id="diff_button" class="ui-icon ui-icon-shuffle">Change</span></a>').appendTo(titlebar).click(function() {

				showingInLine = !showingInLine;
				if(showingInLine) {
					$('#diff_button').removeClass("ui-icon-shuffle");
					$('#diff_button').addClass("ui-icon-transfer-e-w");
					$("#diff_button").attr('title', 'Separated diff');
				} else {
					$('#diff_button').addClass("ui-icon-shuffle");
					$('#diff_button').removeClass("ui-icon-transfer-e-w");
					$("#diff_button").attr('title', 'In-place diff');
				}
				$("#diff_button").qtip();
				$("#old").toggle();
				$("#new").toggle();
				$("#both").toggle();

			}).hover(function() {
				$(this).addClass("ui-state-hover");
			}, function() {
				$(this).removeClass("ui-state-hover");
			});
			$("#diff_button").attr('title', 'In-place diff');
			$("#diff_button").qtip();

			populate_versions();
			loadJavaCodeRepresentation();
			$("#li_"+getCategoryLabel(0,true)).addClass("active");
		});

		function populate_versions() {
			var versions = getVersions();
			var olData = "<li class='nav-header hidden-tablet'>Other versions</li>";//"<li><a href='../index.html'>Summary</a></li>";
			for(var i = 1; i < versions.length; i++) {
				//<li><a class="ajax-link" href="<c:url value="/projects/list"/>"><i class="icon-list-alt"></i><span class="hidden-tablet"> All Projects</span></a></li>
				olData += "<li><a class='ajax-link' href='<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/version/"/>?name=" + versions[i] + "'><span class='hidden-tablet'>" + versions[i - 1] + " - " + versions[i] + "</span></a></li>";
			}
			olData += "<li><a class='ajax-link' href='<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report/"/>'><span class='hidden-tablet'> View Summary </span></a></li>";
			$("#leftmenu_det_report").append(olData);
		}

		function showCode(testName) {
			var test = findTest(testName);
			var oldCode = test["old"];
			oldCode = oldCode.replace(/\[TREX_NEW_LINE\]/g, "\n");
			var sizeOld = oldCode.split("\n").length;

			//$("#old").html(oldCode);

			var newCode = test["new"];
			newCode = newCode.replace(/\[TREX_NEW_LINE\]/g, "\n");
			var sizeNew = newCode.split("\n").length;

			var dmp = new diff_match_patch();

			diffs = dmp.diff_main(oldCode, newCode);
			dmp.diff_cleanupSemantic(diffs);

			diffHtmlCode = dmp.diff_prettyHtml(diffs);
			oldHtmlCode = dmp.diff_prettyHtml_separated(diffs, 'old');
			newHtmlCode = dmp.diff_prettyHtml_separated(diffs, 'new');

			$("#new").html(newHtmlCode);
			$("#old").html(oldHtmlCode);
			$("#both").html(diffHtmlCode);

			prettyPrint();

			var parts = testName.split(".");
			var testName = parts.pop();
			var className = parts.pop();

			$("#package").html(parts.join("."));
			$("#class").html(className);
			$("#test").html(testName);
			$("#dialog").dialog('open');
		}

		function findTest(testName) {
			for(var i = 0; i < tests.length; i++) {
				if(tests[i]["name"] == testName) {
					return tests[i];
				}
			}
			return null;
		}
    </script>
	</body>
</html>