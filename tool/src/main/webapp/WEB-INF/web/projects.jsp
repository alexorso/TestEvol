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


				<div>
					<ul class="breadcrumb">
						<li><a href="#">Home</a> <span class="divider">/</span></li>
						<li><a href="#">Projects</a></li>
					</ul>
				</div>

				<div class="row-fluid sortable">
					<div class="box span12">
						<div class="box-content">
							<form class="form-horizontal" action="projects" method="POST" onsubmit="return validate();">
								<fieldset>
									<legend>Project Information</legend>
									<div class="control-group">
										<label class="control-label" for="projectName">Name</label>
										<div class="controls">
											<input class="input-xlarge focused" id="projectName" name="name"
												type="text" placeholder="Project Name" title="Project Name (must only include letters and numbers)" required="required" pattern="([a-z]+|[A-Z]+|[0-9]+)*">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="vcs">Repository
											Type:</label>
										<div class="controls">
											<select onchange="onSelectRepositoryType(this)" id="vcs" name ="vcs" title="Repository type" required="required">
												<option value="">Select...</option>
												<option value="git">Git</option>
												<option value="svn">SVN</option>
												<option value="cvs">CVS</option>												
											</select>
										</div>
									</div>
									<div class="control-group" id="git_r">
										<label class="control-label" for="git_url">Git Read-Only</label>
										<div class="controls">
											<div class="input-append">
												<input id="git_url" name="gitUrl" class="input-xlarge" type="url" title="Git Repository URL">
												<button class="btn" type="button" id="git_get_info">Fetch
													data!</button>
												<img src="<c:url value="/img/ajax-loaders/ajax-loader-1.gif"/>"
													id="get_info_loading"
													title="Fetching project info">
											</div>
										</div>
									</div>
									<div class="control-group" id="versions_div">
										<div class="alert alert-error" style="width:300px;margin-left: 150px;" id="versions_error">
											<button type="button" class="close" data-dismiss="alert">x</button>
											<strong>Please, select at least two versions.</strong>
										</div>
										<label class="control-label">Available Versions</label>
										<div class="controls" id="versions"></div>
									</div>
									<div class="box-content alerts" id="error_repository">
										<div class="alert alert-error">
											<button type="button" class="close" data-dismiss="alert">x</button>
											<strong>Error retrieving repository info!</strong>
											<div id="error_repository_message"></div>
										</div>
									</div>
									<div class="form-actions">
										<button type="submit" class="btn btn-primary" id="save">Save Project</button>
										<button type="reset" class="btn">Cancel</button>
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
		$(function() {
			$('#git_get_info').click(function() {
				getAvailableBranches();
			});
		});

		function getAvailableBranches() {
			$("#versions_error").hide();
			$("#error_repository").hide();
			$("#get_info_loading").show();
			var vcsUrl;
			var vcs = $("#vcs").val();
			if (vcs == 'git') {
				vcsUrl = $("#git_url").val();
			}

			var urlValue = 'projects/getBranches?url=' + vcsUrl + '&vcs=' + vcs;
			$.get(urlValue, {}, function(data) {
				var success = data["success"];
				$("#get_info_loading").hide();
				if (success) {
					showBranches(data["branches"]);
					$("#versions_div").show();
				} else {
					$("#versions_div").hide();
					$("#error_repository").show();
					var error_msg = data["error"];
					$("#error_repository_message").html(error_msg);
				}
			}, 'json');
		}

		function showBranches(data) {
			$("#versions").empty();
			for ( var i = 0; i < data.length; i++) {
				createVersionCheckBox(data[i]);
			}
		}

		function createVersionCheckBox(version) {
			var labelEl = document.createElement("label");
			labelEl.className = "checkbox";
			var checkbox = document.createElement("input");
			checkbox.type = "checkbox";
			checkbox.name = "branchesToCheckout";
			checkbox.value = version;
			checkbox.innerHTML = version;
			labelEl.appendChild(checkbox);
			labelEl.innerHTML = labelEl.innerHTML + version;
			$("#versions").append(labelEl);
		}

		function onSelectRepositoryType(element) {
			$("#versions_div").hide();
			$("#error_repository").hide();
			$("#git_url").removeAttr("required");
			
			if (element.value == 'git') {
				$("#git_url").attr('required','required');
				$("#git_r").show();
			} else {
				$("#git_r").hide();
				if(element.value != ''){
					element.value = '';
					alert('Support for the selected reporitory type is not yet available.');
				}

			}
		}
		
		function validate(){
			var ckVersions = document.getElementsByName("branchesToCheckout");
			var versions = 0;
			for(var i = 0;i<ckVersions.length;i++){
				if(ckVersions[i]['checked']){
					versions++;
				}
			}
			if(versions < 2){
				$("#versions_error").show();
			}
			else{
				$("#versions_error").hide();
			}
			
			return true;
		}
	</script>

</body>
</html>