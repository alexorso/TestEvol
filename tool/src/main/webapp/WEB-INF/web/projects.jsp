<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

<style>
#git_r,#versions_div,#error_repository, #project-error{
	display: none;
}

#get_info_loading {
	display: none;
	margin-left: 5px;
}

#versions{
	width:365px;
	max-height: 300px;
	overflow-y: scroll;
	padding-left:5px;
	border: 1px solid #CCCCCC;
	border-radius:5px;
}

.clickable{
	cursor:pointer;
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
						<div class="box-content">
							<form class="form-horizontal" action="projects" method="POST" onsubmit="return validate();">
								<input type="hidden" name="username" id="username_form"/>
								<input type="hidden" name="password" id="password_form"/>
								<input type="hidden" name="url" id="url_form"/>
								<fieldset>
									<legend>Project Information</legend>
									<div class="control-group">
										<label class="control-label" for="projectName">Name <i class="btn-setting icon-question-sign clickable" id="help_name"></i></label>
										<div class="controls">
											<input class="input-xlarge focused" id="projectName" name="name"
												type="text" placeholder="Project Name" 
												title="Project Name (must only include letters and numbers)">
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="type">Repository Type <i class="btn-setting icon-question-sign clickable" id="help_repository"></i></label>
										<div class="controls">
											<select onchange="onSelectRepositoryType(this)" 
													id="vcs" name ="type" 
													title="Repository type">
												<option value="">Select...</option>
												<option value="git">GIT</option>
												<option value="svn">SVN</option>
												<option value="cvs">CVS</option>												
											</select>
										</div>
									</div>
									<div class="control-group" id="git_r">
										<label class="control-label" for="git_url">URL <i class="btn-setting icon-question-sign clickable" id="help_git_url"></i></label>
										<div class="controls">
											<div class="input-append">
												<input id="git_url" name="gitUrl" class="input-xlarge" type="url" title="Git Repository URL"/>
												<button class="btn" type="button" id="git_get_info">Fetch
													data!</button>
												<img src="<c:url value="/img/ajax-loaders/ajax-loader-1.gif"/>"
													id="get_info_loading"
													title="Fetching project info">
											</div>
										</div>
									</div>

								<div class="control-group" id="versions_div">
									<label class="control-label">Select the versions you want to analyse <i class="btn-setting icon-question-sign clickable" id="help_versions"></i></label>
									<div class="controls" id="versions">
									</div>
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
		
		<div class="modal hide fade" id="myModal">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"><i class="icon-remove"></i></button>
				<h3>Help</h3>
			</div>
			<div class="modal-body">
				<p id="help_message"></p>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal">Close</a>
			</div>
		</div>
		<div class="modal hide fade" id="requiredModal">
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
		<div class="modal hide fade" id="loginModal" style="width: 360px;">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"><i class="icon-remove"></i></button>
				<h3>Username and password required</h3>
			</div>
			<div class="modal-body center">
				<div class="input-prepend" title="Username" data-rel="tooltip">
					<span class="add-on"><i class="icon-user"></i></span><input autofocus class="input-large" name="username" id="username" type="text" placeholder="username" />
				</div>
				<div class="input-prepend" title="Username" data-rel="tooltip">
				    <span class="add-on"><i class="icon-lock"></i></span><input class="input-large" name="password" id="password" type="password" placeholder="password" />
				</div>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal" id="close_login">Close</a>
				<a href="#" onclick="getAvailableBranchesUsingUserAndPass();" class="btn btn-info" data-dismiss="modal">OK</a>
			</div>
		</div>
	</div>
	<!--/.fluid-container-->

	<%@include file="includes/scripts.jsp" %>
	<script src='<c:url value="/js/help-projects.js"/>'></script>
	
	<script type="text/javascript">
	
		var usernameSaved = '';
		var passwordSaved = '';
		$(function() {
			$('#git_get_info').click(function() {
				getAvailableBranches('','');
			});
			
			$('#help_name').click(function(){
				show_help_project("project_name");
			});
			
			$('#help_repository').click(function(){
				show_help_project("project_repository");
			});
			
			$('#help_versions').click(function(){
				show_help_project("project_versions");
			});
			$('#help_git_url').click(function(){
				show_help_project("project_git_url");
			});
			
			$("#password").keypress(function(event) {
 				  if ( event.which == 13 ) {
 					 getAvailableBranchesUsingUserAndPass();
				   }
			});

		});
		
		function getAvailableBranchesUsingUserAndPass(){
			var username = $('#username').val();
			var password = $('#password').val();
			$("#close_login").trigger('click');
			getAvailableBranches($('#username').val(),$('#password').val());
		}

		function getAvailableBranches(username, password) {
			$("#error_repository").hide();			
			var vcsUrl = '';
			var vcs = $("#vcs").val();
			if (vcs == 'git') {
				vcsUrl = $.trim($("#git_url").val());
			}
			
			if(vcsUrl == ''){
				showError("<li>Reporitory URL is required.</li>");
				return false;
			}
			else{
				if(vcs == 'git'){
					var httpPatt=/^http:\/\//;
					var httpsPatt=/^https:\/\//;
					var gitReadOnlyPatt=/^git:\/\//;
					if(!httpPatt.test(vcsUrl) && !httpsPatt.test(vcsUrl) && !gitReadOnlyPatt.test(vcsUrl)){
						showError("<li>URL not supported. Only HTTP(S) or Git Read-Only.</li>");
						return false;
					}
				}
			}

			$("#get_info_loading").show();
			
			var urlValue = 'projects/getBranches';
			var params = {"url":vcsUrl, "type":vcs, "username":username, "password":password};
			$.post(urlValue, params, function(data) {
				var success = data["success"];
				$("#get_info_loading").hide();
				if (success) {
					showBranches(data["branches"], username, password);
				} else {
					if(data["auth_fail"]){
						$('#loginModal').modal('show');
					}
					else{
						$("#versions_div").hide();
						$("#error_repository").show();
						var error_msg = data["error"];
						$("#error_repository_message").html(error_msg);						
					}
				}
			}, 'json');
		}

		function showBranches(data, username, password) {
			$("#versions").empty();
			if(data.length && data.length > 0){
				selectAllCheck = createVersionCheckBox("Select/Deselect all");
				selectAllCheck.id="all_check";
				//var currentOnClick = selectAllCheck.onclick;
				for ( var i = 0; i < data.length; i++) {
					createVersionCheckBox(data[i]);
				}
				
				selectAllCheck.onclick = (function(){
					var select = this.checked;
					var ckVersions = document.getElementsByName("branchesToCheckout");
					for(var i = 0;i<ckVersions.length;i++){
						if(ckVersions[i].id == 'all_check'){
							continue;
						}
						ckVersions[i].checked = select;
					}
				});
				usernameSaved = username;
				passwordSaved = password;
				$("#versions_div").show();
			}
			else{
				showError("<li>No versions available in this repository. Please try again.</li>")
			}
		}

		function createVersionCheckBox(version, attachListener) {
			var labelEl = document.createElement("label");
		  	labelEl.style.paddingLeft=0;
			
			var checkbox = document.createElement("input");
			checkbox.type = "checkbox";
			checkbox.name = "branchesToCheckout";
			checkbox.value = version;
			checkbox.checked = true;
			checkbox.style.marginTop=0;
			checkbox.style.marginRight='7px';

			labelEl.appendChild(checkbox);
			labelEl.appendChild(document.createTextNode(version));
			
			$("#versions").append(labelEl);
			return checkbox;
		}

		function onSelectRepositoryType(element) {
			$("#versions_div").hide();
			$("#error_repository").hide();
			
			if (element.value == 'git') {
				$("#git_r").show();
			} else {
				$("#git_r").hide();
				if(element.value != ''){
					element.value = '';
					showError('Support for the selected repository type is not yet available.');
				}

			}
		}
		
		function validate(){
			var errorMessage = '';
			var projectName = $.trim($("#projectName").val());
			if(projectName == ''){
				errorMessage = '<li>Project name is required.</li>';
			}
			else{
				var patt1 = /(\w+[-]*)*/g;
				var invChars = patt1.exec(projectName);	
				if(invChars == null || invChars[0] != projectName){
					errorMessage = '<li>Project name is invalid.</li>';
				}
			}
			var repoType = $("#vcs").val();
			if(repoType == '' || (repoType == 'git' && $.trim($("#git_url").val()) == '' )){
				errorMessage += '<li>Repository information is required.</li>';
			}
			
			if(errorMessage != ''){
				showError(errorMessage);
				return false;	
			}			
			
			var ckVersions = document.getElementsByName("branchesToCheckout");
			var versions = 0;
			for(var i = 0;i<ckVersions.length;i++){
				if(ckVersions[i].id == 'all_check'){
					continue;
				}
				if(ckVersions[i]['checked']){
					versions++;
				}
			}
			if(versions < 2){
				showError("<li>At least two versions of the program are required for a correct analysis to be performed.</li>");
				return false;
			}			
			document.getElementById("all_check")['checked']=false;
			$("#username_form").val(usernameSaved);
			$("#password_form").val(passwordSaved);
			if(repoType == 'git'){
				$("#url_form").val($.trim($("#git_url").val()));
			}
			
			return true;
		}
		
		function showError(errorMessage){
			$('#error_message').html('<ul>'+errorMessage+'</ul>');
			$('#requiredModal').modal('show');
		}
		
		function showLogin(){
			$('#loginModal').modal('show');
		}
	</script>

</body>
</html>