jQuery(document).ready(function() {
	$('#ps_ep_first_name_edit').click(function(){
		$('#ps_ep_first_name_val').toggle();
		$('#ps_ep_first_name_input').toggle();
	});
	$('#ps_ep_last_name_edit').click(function(){
		$('#ps_ep_last_name_val').toggle();
		$('#ps_ep_last_name_input').toggle();
	});
	$('#ps_ep_cancel').click(function(){
		$('#ps_ep_first_name_val').show();
		$('#ps_ep_first_name_input').hide();
		
		$('#ps_ep_last_name_val').show();
		$('#ps_ep_last_name_input').hide();
	});
	$('#ps_ep_save').click(function(){
		var dataFound = false;
		var firstName = $('#ps_ep_first_name_input').val().trim();
		var lastName = $('#ps_ep_last_name_input').val().trim();
		if($('#ps_ep_first_name_input').is(':visible')){
			if($('#ps_ep_first_name_input').val().trim().length === 0){
				$('#ps_ep_first_name_input').hide();
				$('#ps_ep_first_name_val').show();
			}else{
				dataFound = true;
			}
		}
		if($('#ps_ep_last_name_input').is(':visible')){
			if($('#ps_ep_last_name_input').val().trim().length === 0){
				$('#ps_ep_last_name_input').hide();
				$('#ps_ep_last_name_val').show();
			}else{
				dataFound = true;
			}
		}
		var token = $("meta[name='_csrf']").attr("content");
	    var header = $("meta[name='_csrf_header']").attr("content");
	    if(dataFound === true){
	    	var jsonData = {"firstName": firstName, "lastName": lastName};
			$.ajax({
		      success:    function() { 
				  $('#ps_ep_last_name_val').text($('#ps_ep_last_name_input').val());
				  $('#ps_ep_first_name_val').text($('#ps_ep_first_name_input').val());
				  $('#ps_ep_last_name_input').hide();
				  $('#ps_ep_last_name_val').show();
				  $('#ps_ep_first_name_input').hide();
				  $('#ps_ep_first_name_val').show();
		      },
		      beforeSend: function(xhr){
	              xhr.setRequestHeader(header, token);
		      },
		      contentType: 'application/json; charset=utf-8',
		      type: "POST",
		      url: "/edit_profile",
		      dataType: "json",
		      data: JSON.stringify(jsonData),
		      error: function(){
		    	  alert("Error.Please try again.");
		      }
		    });
	    }
	});
});