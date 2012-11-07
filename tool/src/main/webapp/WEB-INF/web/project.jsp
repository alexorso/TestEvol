<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

<style type="text/css">
#execute{
	margin: 5px;
	margin-left: 10px;

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
			<%@include file="includes/leftmenu.jsp" %>
			<!-- left menu ends -->

			<div id="content" class="span10">
				<!-- content starts -->


				<div>
					<ul class="breadcrumb">
						<li><a href="<c:url value="/projects/list"/>">Projects</a><span class="divider">/</span></li>
						<li> Project ${project.name}</li>
					</ul>
				</div>

		<div class="row-fluid sortable">		
				<div class="box span12">
					<button type="button" class="btn btn-success" onclick="executeTestEvol('${project.name}');" id="execute" title="Run TestEvol Analysis for the selected versions"><i class="icon-play icon-white"></i> Run Analysis</button>
					<div class="box-header well" data-original-title>
						<h2><i class="icon-tags"></i> Versions</h2>
					</div>
					<form action="${project.name}/execute" method="POST" id="executionForm">
					<input type="hidden" name="includeCoverageAnalysis" id="includeCoverageAnalysis" value="false"/>
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable">
						  <thead>
							  <tr>
							  	  <th style="150px;text-align: center;">
							  	  	<input type="checkbox" value="" id="check_all" checked="checked">All
							  	  </th>
								  <th>Name</th>
								  <th style="width:50px;text-align: center">Configuration</th>
								  <th style="width:10px;text-align: center">Actions</th>
							  </tr>
						  </thead>   
						  <tbody>
						  <c:forEach var="version" items="${project.versionsList}" varStatus="i">
							<tr>
								<td style="width:100px;text-align: center;">
									<c:choose>
										<c:when test="${version.configurationType eq 'Invalid'}">
											<a href="#" title="This version has an invalid configuration. <br/> Use the button 'Settings' in the 'Actions' column to configure it properly." data-rel="tooltip" class="btn btn-danger"><i class="icon-warning-sign icon-gray"></i></a>
										</c:when>
										<c:otherwise>
											<input type="checkbox" name="versionsToExecute" value="${version.name}" checked="checked">
										</c:otherwise>
									</c:choose>
								</td>
								<td><strong>${version.name}</strong></td>
								<td style="width:50px;text-align: center">${version.configurationType}</td>
								<td style="width:100px;text-align: center" class="center">
									<c:if test="${version.configurationType != 'Maven'}">
									<a class="btn btn-info" href="#" title="Settings" onclick='openSettings("${project.name}","${version.name}", eval(${version.versionSettings.jsonRepresentation}))'>
										<i class="icon-cog icon-white"></i>                                            
									</a> 
									</c:if>
									<!-- <a class="btn btn-info btn-setting" href="#" title="Refresh local copy" onclick="updateVersion('${project.name}','${version.name}')">
										<i class="icon-refresh icon-white"></i>                                            
									</a> -->
									<!--
									<a class="btn btn-info" href="#" title="Edit">
										<i class="icon-edit icon-white"></i>                                            
									</a> -->
									<a class="btn btn-danger btn-setting" onclick="deleteVersion('${project.name}','${version.name}')" href="#" title="Delete">
										<i class="icon-trash icon-white"></i>
									</a>
								</td>
							</tr>
							</c:forEach>					
						  </tbody>
					  </table>            
					</div>
					</form>
				</div><!--/span-->
			
			</div><!--/row-->

				<!-- content ends -->
			</div>
			<!--/#content.span10-->
		</div>
		<!--/fluid-row-->

		<hr>
		
		<div class="modal hide fade" id="ModalRequired">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"><i class="icon-remove"></i></button>
				<h3>Error</h3>
			</div>
			<div class="modal-body">
				<p id="error_message"></p>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal">Close</a>
			</div>
		</div>
		
		<div class="modal hide fade" id="myModal">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" id="close"><i class="icon-remove"></i></button>
				<h3 id="titleModal">Delete Version</h3>
			</div>
			<div class="modal-body">
				<p id="bodyModal"></p>
			</div>
			<div class="modal-footer">
				<a href="#" id="closeButton"  class="btn" data-dismiss="modal">Close</a>
				<a href="aaa" id="modalButton" class="btn btn-danger">Delete</a>
		</div>
	</div>
			<div class="modal hide fade" id="modalSettings">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" id="close"><i class="icon-remove"></i></button>
				<h3 id="titleModal">Version Settings</h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" id="settings-form" action="${project.name}/version/save_settings" method="post">
								<input type="hidden" name="project" id="project">
								<input type="hidden" name="version" id="version">
								<fieldset>
								
									<div class="control-group">
										<label class="control-label" for="basedirOnRepository">Base dir inside version folder - <b>&#36;{base_dir}</b></label>
										<div class="controls">
											<input class="input-xlarge focused" id="basedirOnRepository" name="basedirOnRepository"
												type="text" placeholder="Relative folder in version dir. Default: empty" style="margin-top:5px;">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="source">Application/Library sources directory</label>
										<div class="controls">
											<input class="input-xlarge focused" id="source" name="source"
												type="text" placeholder="(Default value: &#36;{base_dir}/src/main/java)" style="margin-top:5px;">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="resource">Application/Library resources directory</label>
										<div class="controls">
											<input class="input-xlarge" id="resource" name="resource"
												type="text" placeholder="(Default value: &#36;{base_dir}/src/main/resources)"  style="margin-top:5px;">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="testSources">Test Sources directory</label>
										<div class="controls">
											<input class="input-xlarge" id="testSources" name="testSources"
												type="text" placeholder="(Default value: &#36;{base_dir}/src/test/java)">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="testResource">Test resources directory</label>
										<div class="controls">
											<input class="input-xlarge" id="testResource" name="testResource"
												type="text" placeholder="(Using default value: &#36;{base_dir}/src/test/resources)"  style="margin-top:5px;">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="lib">Classpath directory</label>
										<div class="controls">
											<input class="input-xlarge" id="lib" name="lib"
												type="text" placeholder="(Using default value: &#36;{base_dir}/lib)">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="javaversion">Java version</label>
										<div class="controls">
											<select name ="javaversion" id="javaversion">
												<option value="1.4">1.4</option>
												<option value="1.5" selected="selected">1.5</option>
												<option value="1.6">1.6</option>												
											</select>
										</div>
									</div>
									
								</fieldset>
							</form>
			</div>
			<div class="modal-footer">
				<a href="#" id="closeButton"  class="btn" data-dismiss="modal">Close</a>
				<button type="submit" id="modalButton" class="btn btn-success" onclick="$('#settings-form').submit();">Save</button>
		</div>
	</div>
	
		
		
	<!--/.fluid-container-->
	
	
<!-- 	<script type="text/javascript" src="<c:url value="/projects/version/script"/>"></script>
 -->
 
 	<%@include file="includes/scripts.jsp" %>
	<script type="text/javascript">

		$(function(){
			$("#check_all").click(function(){
				var select = $(this)[0].checked;
				var ckVersions = document.getElementsByName("versionsToExecute");
				for(var i = 0;i<ckVersions.length;i++){
					if((select && !ckVersions[i].checked) ||
						(!select && ckVersions[i].checked)){
						ckVersions[i].click();						
					}

				}
			});
		});
		
		function openSettings(project, version, settingsStr){
			$("#project").val(project);
			$("#version").val(version);
			
			var currentSettings = eval(settingsStr);
			
			for (var key in currentSettings) {
				setVal(key, currentSettings);	
			}
			$("#modalSettings").modal('show');
		}
		
		function setVal(property, currentSettings){
			var val = currentSettings[property];
			if(val && $.trim(val) != ''){
				$("#"+property).val(val);
			}
		}

		function executeTestEvol(project){
			if(!validate()){
				return false;
			}
			
			$("#titleModal").html("Execute TestEvol for Project "+project);
			$("#bodyModal").html("Confirm execution?<br/><input type='checkbox' id='coverageAux'/> Include coverage analysis");
			$("#modalButton").removeClass();
			$("#modalButton").addClass("btn");
			$("#modalButton").addClass("btn-success");
			$("#modalButton").html("Execute");
			
			document.getElementById("modalButton").href='javascript:submitExecution()';
		
			$('#myModal').modal('show');
		}
		
		function submitExecution(){
			var checked = $("#coverageAux:checked").val() != undefined;
			$("#includeCoverageAnalysis").val(checked);
			$("#executionForm").submit();
		}
		
		function deleteVersion(projectName, version){
						
			$("#titleModal").html("Delete Version "+version);
			$("#bodyModal").html("Are you sure you want to delete version "+version+"?");
			$("#modalButton").removeClass();
			$("#modalButton").addClass("btn");
			$("#modalButton").addClass("btn-danger");
			$("#modalButton").html("Delete");
			
			document.getElementById("modalButton").href='<c:url value="/projects/"/>'+projectName+'/'+version+'/delete';
		}
		
		function updateVersion(projectName, version){			
			
			$("#titleModal").html("Update Version "+version);
			
			var ajaxImg = '<img style="display:none;margin:auto;" src="<c:url value="/img/ajax-loaders/ajax-loader-5.gif"/>" id="update_repo_img">';
			$("#bodyModal").html("Are you sure you want to update version "+version+"?<br/>"+ajaxImg);
			$("#modalButton").removeClass();
			$("#modalButton").addClass("btn");
			$("#modalButton").addClass("btn-success");
			$("#modalButton").html("Update");
			
			document.getElementById("modalButton").href='javascript:updateAjax(\'<c:url value="/projects/"/>'+projectName+'/'+version+'/updateRepo\');';
		}
		
		function updateAjax(url){
			$("#update_repo_img").show();
			$.get(url, function(data){
				var success = data['success'];
				$("#update_repo_img").hide();
				if(success){
					$("#closeButton").click(function(){
						location.reload();
					});
					$("#close").click(function(){
						location.reload();
					});
				}
				
				var messageDiv = document.createElement("div");
				var className = success?"alert alert-success":"alert alert-error";
				messageDiv.className= className;
				messageDiv.innerHTML='<strong>'+(success?"Success":"Error")+"</strong><br/>"+data['message'];
				
				$("#bodyModal").empty();
				$("#bodyModal").append(messageDiv);
			});
		}
		
		function validate(){
			var ckVersions = document.getElementsByName("versionsToExecute");
			var versions = 0;
			for(var i = 0;i<ckVersions.length;i++){
				if(ckVersions[i]['checked']){
					versions++;
				}
			}
			if(versions < 2){
				showError("<li>At least two versions of the program are required for a correct analysis to be performed.</li>");
				return false;
			}
			//document.getElementById("all_check")['checked']=false;
			
			return true;
		}
		
		function showError(errorMessage){
			$('#error_message').html('<ul>'+errorMessage+'</ul>');
			$('#ModalRequired').modal('show');
		}
		
		
	</script>
	
</body>
</html>