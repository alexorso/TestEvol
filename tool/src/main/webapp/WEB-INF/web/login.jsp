<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="en">
<head>

<%@include file="includes/header.jsp" %>

</head>

<body>
	<div class="container-fluid">
		<div class="row-fluid">
		<div class="row-fluid">
				<div class="span12 center login-header">
					<h2>Welcome to TestEvol</h2>
				</div><!--/span-->
			</div><!--/row-->
			
			<div class="row-fluid">
				<div class="well span5 center login-box">
					<div class="alert alert-info">
						Please login with your Username and Password.
					</div>
					<form class="form-horizontal" action="<c:url value="/login"/>" method="post">
						<fieldset>
							<div class="input-prepend" title="Username" data-rel="tooltip">
								<span class="add-on"><i class="icon-user"></i></span><input autofocus class="input-large span10" name="username" id="username" type="text" placeholder="username" />
							</div>
							<div class="clearfix"></div>

							<div class="input-prepend" title="Password" data-rel="tooltip">
								<span class="add-on"><i class="icon-lock"></i></span><input class="input-large span10" name="password" id="password" type="password" placeholder="password" />
							</div>
							<div class="clearfix"></div>

							<div class="input-prepend">
							<label class="remember"><a href="projects/list">Guest login</a></label>
							</div>
							<div class="clearfix"></div>

							<p class="center span5">
								<button type="submit" class="btn btn-primary">Login</button>
							</p>
						</fieldset>
					</form>
				</div><!--/span-->
			</div><!--/row-->
		</div>
		<!--/fluid-row-->

		<hr>
	</div>
	<!--/.fluid-container-->
	
	
<!-- 	<script type="text/javascript" src="<c:url value="/projects/version/script"/>"></script>
 -->
 
 	<%@include file="includes/scripts.jsp" %>	
</body>
</html>