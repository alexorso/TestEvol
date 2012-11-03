function add_menu(){
	var menu_code = '';
	menu_code += '<ul>';
	menu_code += '<li><a id="menu_index" href="index.html"><span>Home</span></a></li>';
	menu_code += '<li><a id="menu_documentation" href="documentation.html"><span>Documentation</span></a></li>';
	menu_code += '<li><a id="menu_publication" href="publications.html"><span>Publications</span></a></li>';
	menu_code += '<li><a id="menu_download" href="download.html"><span>Download</span></a></li>';
	menu_code += '<li><a id="menu_authors" href="authors.html"><span>Authors</span></a></li>';
	menu_code += '</ul>';
	$("#topmenu").html(menu_code);
	if(activeTab != ""){
		$(activeTab).addClass("active");		
	}
}

function getting_started(){
	var code ='';
	code += '<h3>Getting Started</h3>';
	code += '<ul>';
	code += '<li><a href="documentation.html">Documentation</a></li>';
	code += '<li><a href="help.html">Getting help</a></li>';
	code += '</ul>';
	$("#getting_started").html(code);
}

function show_smscom_box(){
	var code = '<div style="text-align: center;"><a href="http://www.erc-smscom.org/" target="_blank"><img id="smscom" src="images/smscom_logo.png" alt="SMSCom" /></a><br /><br />DSOL is developed as part of the SMSCom project</div>';
	$("#smscom_box").html(code);
}

function init(){
	add_menu();
	getting_started();
	show_smscom_box();
}
