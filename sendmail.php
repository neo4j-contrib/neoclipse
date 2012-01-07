<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Gridfolio - 960gs Based Single Page Portfolio by Cudazi</title>
	
    <link type="text/css" rel="stylesheet" href="css/960.css" media="screen" />
    <link type="text/css" rel="stylesheet" href="css/screen.css" media="screen" />
    
    <!--[if lt IE 7]>
	<script src="http://ie7-js.googlecode.com/svn/version/2.1(beta4)/IE7.js"></script>
	<![endif]-->
</head>
<body>
    <div id="outer">
		<div class="container_12 centered">
			<div class="grid_12">&nbsp;<!-- future --></div>
			
			<div class="prefix_4 grid_4">
				<div class="rounded shadow boxed">
					
					<?php
						/* CONTACT FORM */
						/* Edit the send_to line below */
						$send_to = 'example@example.com';
						/* Do not need to edit anything beyond this line */
						/* For more Contact forms, visit CodeCanyon */
						
						
						$hasErrors = false;
						// Check if name, email and message are filled out.
						if(empty($_POST["cf_name"]) || empty($_POST["cf_email"]) || empty($_POST["cf_message"]))
						{
							$hasErrors = true;
						}else{
							// Check if email is a valid email
							if(preg_match("/^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/", $_POST["cf_email"]) == 1) {
								$hasErrors = false;
							}else{
								$hasErrors = true;
							}
						}
						
						// email_2 is a HIDDEN dummy field
						// If this is filled out, it's spam, proceed if it's empty...
						if(empty($_POST["email_2"]))
						{
							if($hasErrors == false)
							{
								// Include an excerpt in the subject line
								$excerpt = " " . substr(stripslashes($_POST["cf_message"]), 0, 20) . "...";
								$to = $send_to; // pulled from above to make it easier to edit.
								$from = $_POST["cf_email"];
								$subject = 'Contact Form - ' . $excerpt;
								$headers = "From: ".$from." \r\n" .
								"Reply-To: ".$_POST["cf_email"];
								$body = "\nContact Form Message:\n\n";
								$body .= "From: " . $_POST["cf_name"] . " (".$_POST['cf_email'].")\n";
								$body .= "Email: " . $_POST["cf_email"] . "\n";
								$body .= "\nMessage:\n" . stripslashes($_POST["cf_message"]) . "\n\n";
								$body .= "IP: ". $_SERVER['REMOTE_ADDR'] . "\n";
								$body .= "". $_SERVER['SERVER_NAME'] . "\n";
								mail($to,$subject,$body,$headers);
							}
						}else{ 
							//email_2 is SPAM
						}
					?>
					
					<?php if($hasErrors == false){ ?>
						<h1>Thank you!</h1>
						<p><strong>Your message has been sent.</strong></p>
						<p><a href="index.html">Back to the site</a></p>
					<?php }else{ ?>
						<h1>Oops!</h1>
						<p><strong>Your message has not been sent.</strong></p>
						<p>Enter a name valid email and a message.</p>
					<?php } ?>
					
				</div>
			</div>
			
		</div><!--//container_12-->
	</div>
</body>
</html>
