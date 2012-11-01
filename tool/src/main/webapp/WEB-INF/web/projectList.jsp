<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

<style>
#git_r,#versions_div,#error_repository, #project-error,#versions_error {
	display: none;
}

#get_info_loading {
	display: none;
	margin-left: 5px;
}

#versions{
	width:365px;
	max-height: 100px;
	overflow-y: scroll;
	padding-left:5px;
	border: 1px solid #CCCCCC;
	border-radius:5px;
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

		<div class="row-fluid sortable">		
				<div class="box span12">
					<div class="box-header well" data-original-title>
						<h2><i class="icon-folder-open"></i> Projects</h2>
					</div>
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable">
						  <thead>
							  <tr>
								  <th>Name</th>
								  <th style="width:70px;text-align: center">Actions</th>
							  </tr>
						  </thead>   
						  <tbody>
						  <c:forEach var="project" items="${projects}">
							<tr>
								<td><strong>${project.name}</strong></td>
								<td style="width:120px;text-align: center" class="center">
									<a class="btn btn-success" href="<c:url value="/projects/${project.name}"/>" title="Run TestEvol Analysis for project ${project.name}">
										<i class="icon-play icon-white"></i>                                            
									</a>
									<a class="btn btn-info" href="<c:url value="/projects/${project.name}/executions"/>" title="Show TestEvol executions for project ${project.name}">
										<i class="icon-tasks icon-white"></i>                                            
									</a>
									<a class="btn btn-danger btn-setting" onclick="deleteProject('${project.name}')" title="Delete">
										<i class="icon-trash icon-white"></i>
									</a>
								</td>
							</tr>
							</c:forEach>					
						  </tbody>
					  </table>            
					</div>
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
				<button type="button" class="close" data-dismiss="modal">×</button>
				<h3>Delete Project</h3>
			</div>
			<div class="modal-body">
				<p>Are you sure you want to delete the project?</p>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal">Close</a>
				<a href="aaa" id="deleteButton" class="btn btn-danger">Delete</a>
			</div>
		</div>
	</div>
	<!--/.fluid-container-->

	<%@include file="includes/scripts.jsp" %>
	
	<script type="text/javascript">		
		function deleteProject(projectName){
			document.getElementById("deleteButton").href='<c:url value="/projects/"/>'+projectName+'/delete';
		}
		
	</script>

</body>
</html>