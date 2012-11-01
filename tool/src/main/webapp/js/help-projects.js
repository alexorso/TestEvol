var help_project = {
		"project_name":"Insert the project name.",
		"project_repository":"Project Repository help",
		"project_versions":"Select the version that you want to analyse using TestEvol",
		"project_git_url":"The URL used to retrieve information of your Git repository. Supported only HTTP(s) and Git Read-Only urls."
};

function show_help_project(message_id){
	$('#help_message').html(help_project[message_id]);
}