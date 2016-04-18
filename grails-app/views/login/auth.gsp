<head>
<meta name='layout' content='main' />
<title>Login to GLO manager</title>
</head>

<body >
<!-- Wrapper -->
		<div class="wrapper-login">
				<!-- Login form -->
				<section class="full"  >					
					
					<h3>Login</h3>
					
				<g:if test='${flash.message}'>
					<div class='box box-warning'>${flash.message}</div>
				</g:if>
				<g:if test='${flash.info}'>
					<div class='box box-info'>${flash.info}</div>
				</g:if>

					<form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>
 
						<p>
							<label class="required" for="username">User name:</label><br/>
							<input type='text' class='full' name='j_username' id='username' />
						</p>
						
						<p>
							<label class="required" for="password">Password:</label><br/>
							<input type='password' class='text_' name='j_password' id='password' />
						</p>
						
				  	<!-- 	<p>
							<input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me'
								<g:if test='${hasCookie}'>checked='checked'</g:if> />
							<label class="choice" for="remember">Remember me?</label>
						</p> -->
						
						<p>
							<input type='submit' class="btn btn-green big" value='Login' /> &nbsp; <a href="javascript: //;" onclick="$('#emailform').slideDown(); return false;">Change password</a>
						</p>
						<div class="clear">&nbsp;</div>
 
					</form>
					
					<form id="emailform" style="display:none" method="post">
						<div class="box">
							<p id="emailinput">
								<label for="user">User name</label><br/>
								<input type="text" id="user" class="full" value="" name="user"/>
								<label for="email">Old password</label><br/>
								<input type="password" id="email" class="full" value="" name="email"/>
								<label for="emailnew">New password</label><br/>
								<input type="password" id="emailnew" class="full" value="" name="emailnew"/>
								<label for="emailconfirm">Confirm</label><br/>
								<input type="password" id="emailconfirm" class="full" value="" name="emailconfirm"/>
							</p>
							<p>
							<g:actionSubmit	class="btn" controller="login" action="changePassword"	value="Change" />
							</p>
						</div>
					</form>
					
				</section>
				<!-- End of login form -->
				
		</div>
		<!-- End of Wrapper -->

<script type='text/javascript'>
<!--
(function(){
	document.forms['loginForm'].elements['j_username'].focus();
})();
// -->
</script>
</body>
