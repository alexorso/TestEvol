<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

<style type="text/css">
	.bootstrap-datatable tr td:nth-child(1), .bootstrap-datatable tr th:nth-child(1){
		width:50%;
	}
	.bootstrap-datatable tr td:nth-child(n+2), .bootstrap-datatable tr th:nth-child(n+2){
		text-align: center;
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
						<li><a href="<c:url value="/projects/${project.name}"/>">Project ${project.name}</a><span class="divider">/</span></li>
						<li> Executions</li>
					</ul>
				</div>

		<div class="row-fluid sortable">		
				<div class="box span12">
					<div class="box-header well" data-original-title>
						<h2><i class="icon-tasks"></i> Executions</h2>
					</div>
					<form action="${project.name}/execute" method="POST">
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable">
						  <thead>
							  <tr>
								  <th>Name</th>
								  <th>Versions</th>
								  <th>Date</th>
								  <th>Status</th>
								  <th style="text-align: center">Actions</th>
							  </tr>
						  </thead>   
						  <tbody>
						  <c:forEach var="execution" items="${executions}">
							<tr>
								<td><strong>${execution.name}</strong></td>
								<td>${execution.versions}</td>
								<td>
									<jsp:useBean id="dateValue" class="java.util.Date" />
									<jsp:setProperty name="dateValue" property="time" value="${execution.createdAt}" />
									<fmt:formatDate value="${dateValue}" pattern="dd-MMM-yyyy HH:mm" />
								</td>
								<td><span class="${execution.status.style}" style="padding: 5px" id="status">${execution.status.label}</span></td>
								<td style="width:150px;text-align: center" class="center">
									<a class="btn btn-info" href="<c:url value="/projects/${project.name}/execution/${execution.id}"/>" title="Show Details">
										<i class="icon-eye-open icon-white"></i>                                            
									</a>
									<a class="btn btn-danger btn-setting" onclick="deleteExecution('${project.name}','${execution.id}','${execution.name}')" href="#" title="Delete">
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
				<h3 id="titleModal">Delete Execution</h3>
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
	
		if(mustShowFeedbackMessage()){
			addFeedbackMessage();
			$(function(){
				if(successExecution()){
					showFeedbackMessage('Execution deleted successfully!');					
				}
				else{
					showFeedbackMessage('An exception was thrown while deleting the Execution!');
				}

			});	
		}
		
		//var closeClickEvent	
		function deleteExecution(projectName, executionId, executionName){
						
			$("#titleModal").html("Delete Execution "+executionName);
			$("#bodyModal").html("Are you sure you want to delete the execution "+executionName+"?");
			
			document.getElementById("modalButton").href='<c:url value="/projects/"/>'+projectName+'/execution/'+executionId+'/delete';
		}
		
		<c:if test="${empty executions}">
			$('.table').append('<tr><td colspan="5">No executions found for this project.</td></tr>');
		</c:if>
		
		
	</script>
	
</body>
</html>