<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

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
						<li> Project ${project.name}</li>
					</ul>
				</div>

		<div class="row-fluid sortable">		
				<div class="box span12">
					<div class="box-header well" data-original-title>
						<h2><i class="icon-tags"></i> Versions</h2>
					</div>
					<form action="${project.name}/execute" method="POST">
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable">
						  <thead>
							  <tr>
							  	  <th style="100px;text-align: center;">
							  	  	<button type="submit" class="btn btn-success" id="execute" title="Execute TestEvol for the selected versions"><i class="icon-play icon-white"></i> Execute</button>
							  	  </th>
								  <th>Name</th>
								  <th style="width:50px;text-align: center">Config</th>
								  <th style="width:80px;text-align: center">Actions</th>
							  </tr>
						  </thead>   
						  <tbody>
						  <c:forEach var="version" items="${project.versionsList}" varStatus="i">
							<tr>
								<td style="width:100px;text-align: center;"><input type="checkbox" name="versionsToExecute" value="${version.name}" checked="checked"></td>
								<td><strong>${version.name}</strong></td>
								<td style="width:80px;text-align: center">${version.type}</td>
								<td style="width:150px;text-align: center" class="center">
									<a class="btn btn-info btn-setting" href="#" title="Refresh local copy" onclick="updateVersion('${project.name}','${version.name}')">
										<i class="icon-refresh icon-white"></i>                                            
									</a>
									<!-- <a class="btn btn-info" href="#" title="Edit">
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
		<div class="modal hide fade" id="myModal">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" id="close">×</button>
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
	</div>
	<!--/.fluid-container-->
	
	
<!-- 	<script type="text/javascript" src="<c:url value="/projects/version/script"/>"></script>
 -->
 
 	<%@include file="includes/scripts.jsp" %>
	<script type="text/javascript">
		//var closeClickEvent

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
		
		
		
	</script>
	
</body>
</html>