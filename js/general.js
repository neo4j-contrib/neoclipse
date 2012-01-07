$(document).ready(function(){
   
   /*
   *	Used to scroll to a specific position.
   *	Edit the variables below to adjust speed and positioning
   */
	var scrollDuration = 500; // 1000 = 1 second
	var scrollGap = 0; // in Pixels, the gap left above the scroll to point
	
	$('a[href^=#]').click(function() 
	{
		if (
			location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') && 
			location.hostname == this.hostname
			) 
		{
			var $target = $(this.hash);
			$target = $target.length && $target || $('[name=' + this.hash.slice(1) +']');
			if ($target.length) 
			{
				var targetOffset = $target.offset().top - scrollGap;
				$('html,body').animate
					(
					{scrollTop: targetOffset}, 
					scrollDuration
					);
				
				// Remove all "active" classes
				$("a").removeClass("active");
				
				// Set "active" class on clicked item
				$(this).addClass("active");
								
				return false;
			}
		}
	});
	
	
	
	// jQuery Lightbox Init / settings
	// Visit http://leandrovieira.com/projects/jquery/lightbox/ for more options and updates
	$("a[href$=.jpg],a[href$=.png],a[href$=.gif]").lightBox({
		imageLoading:	'images/jquery-lightbox/lightbox-ico-loading.gif',	// (string) Path and the name of the loading icon
		imageBtnPrev:	'images/jquery-lightbox/lightbox-btn-prev.gif',		// (string) Path and the name of the prev button image
		imageBtnNext:	'images/jquery-lightbox/lightbox-btn-next.gif',		// (string) Path and the name of the next button image
		imageBtnClose:	'images/jquery-lightbox/lightbox-btn-close.gif',	// (string) Path and the name of the close btn
		imageBlank:		'images/jquery-lightbox/lightbox-blank.gif'			// (string) Path and the name of a blank image (one pixel)
	});
	
	
	// Contact form, click to remove text feature
	$('#cf_name,#cf_email,#cf_message').each(function() {
		var original_txt = this.value;
		
		$(this).focus(function() 
		{
			if(this.value == original_txt) { this.value = ''; }
		});
		$(this).blur(function() 
		{
			if(this.value == '') { this.value = original_txt; }
		});
	});
	
	
	// Slider navigation expand and contract
	var extraPadding = '15px';
	$('.slider a.prev').hover(function() {
		$(this).animate({ paddingLeft : "+="+extraPadding }, 100);        
	}, function() {
		$(this).animate({ paddingLeft : "-="+extraPadding }, 100);
	});
	$('.slider a.next').hover(function() {
		$(this).animate({ paddingRight : "+="+extraPadding }, 100);       
	}, function() {
		$(this).animate({ paddingRight : "-="+extraPadding }, 100);  
	});
	
	
	// Thumbnail hover
	$(".gallery .thumb").hover(function(){
		$(this).stop().animate({ opacity: '0.8'}, 100);
	}, function() {
		$(this).stop().animate({ opacity: '1'}, 200);
	});
	//
	
	
	// contact form validation
	var hasChecked = false;
	$("#cf_submit").click(function () { 
		hasChecked = true;
		return checkForm();
	});
	$("#cf_name,#cf_email,#cf_message").live('change click', function(){
		if(hasChecked == true)
		{
			return checkForm();
		}
	});
	function checkForm()
	{
		var hasError = false;
		var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
		if($("#cf_name").val() == '') {
			hasError = true;
		}
		if($("#cf_email").val() == '') {
			hasError = true;
		}else if(!emailReg.test( $("#cf_email").val() )) {
			hasError = true;
		}
		if($("#cf_message").val() == '') {
			hasError = true;
		}
		if(hasError == true)
		{
			$("#contact-form-errror").fadeIn();
			return false;
		}else{
			return true;
		}
	}
	// end contact form validation
	
	

	//	********************************
	//	Add additional functions here...
	//	********************************
	
	
	
	// Simple style switcher for live demo - you can remove this
	$(".switch-to-dark").click(function() {
	   $("#themeCSS").attr("href", "css/colors-dark.css");
	   return false;
	 });
	$(".switch-to-default").click(function() {
	   $("#themeCSS").attr("href", "css/colors-default.css");
	   return false;
	 });
	//
	
	
}); // end document.ready


	
