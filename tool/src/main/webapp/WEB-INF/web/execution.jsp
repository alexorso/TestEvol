<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

<style type="text/css">

#get_info_loading, #get_info_loading_name, #openReport{
	display: none;
	margin-left: 5px;
}

#logs{
	overflow-y: scroll;
	max-height: 300px;
}

#log_message{
	height:100%;
}

<c:if test="${execution.status.code eq '2'}">
	#openReport{
		display: inline;
		margin-left: 5px;
	}
</c:if>
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
						<li><a href="<c:url value="/projects/list"/>">Projects</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}"/>">Project ${execution.project.name}</a><span class="divider">/</span></li>
						<li><a href="<c:url value="/projects/${execution.project.name}/executions"/>">Analysis</a><span class="divider">/</span></li>						
						<li id="titleName">${execution.name}</li>
					</ul>
				</div>

				<div class="row-fluid sortable">
					<div class="box span12">
						<div class="box-content">
							<form class="form-horizontal" action="projects" method="POST" onsubmit="return validate();">
								<fieldset>
									<legend>TestEvol Analysis</legend>
									<div class="control-group" style="margin-bottom:10px;" id="nameDiv">
										<label class="control-label" for="executionName">Name</label>
										<div class="controls">
											<input class="input-xlarge focused" id="executionName" name="name"
												type="text" value="${execution.name}">
												<img src="<c:url value="/img/ajax-loaders/ajax-loader-1.gif"/>"
													id="get_info_loading_name"/>
											<i class="icon icon-color icon-check" id="check"></i>
											<i class="icon icon-color icon-close" id="error"></i>
											<span class="help-inline" id="helpName"></span>
										</div>
									</div>
									<div class="control-group" style="margin-bottom:10px;">
										<label class="control-label" for="projectName">Compared Versions</label>
										<div class="controls" style="margin-top:5px;">
											<strong>${execution.versions}</strong>
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="projectName">Status</label>
										<div class="controls" style="margin-top:5px;">
											<span class="${execution.status.style}" style="padding: 5px" id="status"> ${execution.status.label} </span>
											<img src="<c:url value="/img/ajax-loaders/ajax-loader-1.gif"/>"
													id="get_info_loading">
											<span class="label label-info" style="padding: 5px;cursor:pointer;" id="openReport" onclick="openReport('${execution.project.name}','${execution.id}');"> <i class="icon-book icon-white" style="margin-right:5px;"></i> Open Report </span>		
										</div>
									</div>
									<div class="box-content alerts" id="logs">
										<div class="alert alert-info">
											<div>
												<br/>
												<strong style="font-size: 18px;">Analysis Output</strong><br/><br/>
												<div id="log_message">${execution.executionLog}</div>
											</div>
										</div>
									</div>
								</fieldset>
							</form>

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

	<script type="text/javascript">
	
		function openReport(project, executionId){
			window.location = '<c:url value="/projects/${execution.project.name}/execution/${execution.id}/report"/>';
		}
	
		var intervalStatus = 2000;	
		$(function() {
			$('#check, #helpName, #error').hide(); 
			
			$("#executionName").change(
				function(){
					saveName($(this));
				}		
			);
			$("#executionName").click(
				function(){
					$('#check').hide();
					toggleError('hide');	
				}
			);
			<c:if test="${execution.status.code != '2' && execution.status.code != '3'}">
				setTimeout(function(){checkStatus();},intervalStatus);
			</c:if>
			<c:if test="${execution.status.code eq '1'}">
				$('#get_info_loading').show();
			</c:if>
			
		});
		
		function checkStatus(){
			$('#get_info_loading').show();
			$.get("<c:url value="/projects/${execution.project.name}/execution/${execution.id}/status"/>",
				  function(data){changeStatus(data);});
		}
		
		function changeStatus(data){
			var resetTimeout = true;
			if(data['req_success']){
				var code = data['code'];
				resetTimeout = code != '2' && code != '3';
				$('#status').removeClass();
				$('#status').addClass(data['style']);
				$('#status').html(data['label']);
				$('#log_message').html(data['log']);
				$("#logs").prop({ scrollTop: $("#logs").prop("scrollHeight") });
				$("html, body").prop({ scrollTop: $(document).height()});
				if(code == '2'){
					$('#openReport').show();
				}
				if(code != '1'){
					$('#get_info_loading').hide();
				}
				
			}
			if(resetTimeout){
				setTimeout(function(){checkStatus();},intervalStatus);				
			}
		}

		function toggleError(showOrHide,errorMessage){
			$('#check').hide();
			if(showOrHide == 'show'){
				$("#nameDiv").addClass("error");
				$("#helpName").html(errorMessage);
				$("#helpName").show();
				$("#error").show();
			}
			else{
				 $("#nameDiv").removeClass("error");
				 $("#helpName").hide();
				 $("#error").hide();
			}
		}
		
		
		function saveName(element){
			var name = $.trim(element.val());
			 if( name != ''){
				 toggleError('hide');
				 $("#get_info_loading_name").show();
				 $.get('<c:url value="/projects/${execution.project.name}/execution/${execution.id}/save"/>?name='+name,
				  		function(data){saveNameCallback(data, name);});
			 }
			 else{
				 toggleError('show', 'Analysis name is required!');
			 }
		}
		
		function saveNameCallback(data, newName){
			$("#get_info_loading_name").hide();
			if(data['success']){
				$('#check').show();
				$('#titleName').html(newName);
			}
			else{
				toggleError('show', 'Error while saving execution name!');
			}
		}
	
	</script>
</body>
</html>