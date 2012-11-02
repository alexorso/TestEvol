	<!-- external javascript
	================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->

	<!-- jQuery -->
	<script src="<c:url value="/js/jquery-1.7.2.min.js"/>"></script>
	<!-- jQuery UI -->
	<script src="<c:url value="/js/jquery-ui-1.8.21.custom.min.js"/>"></script>
	<!-- transition / effect library -->
	<!-- transition / effect library -->
	<script src="<c:url value="/js/bootstrap-transition.js"/>"></script>
	<!-- alert enhancer library -->
	<script src="<c:url value="/js/bootstrap-alert.js"/>"></script>
	<!-- modal / dialog library -->
	<script src="<c:url value="/js/bootstrap-modal.js"/>"></script>
	<!-- custom dropdown library -->
	<script src="<c:url value="/js/bootstrap-dropdown.js"/>"></script>
	<!-- scrolspy library -->
	<script src="<c:url value="/js/bootstrap-scrollspy.js"/>"></script>
	<!-- library for creating tabs -->
	<script src="<c:url value="/js/bootstrap-tab.js"/>"></script>
	<!-- library for advanced tooltip -->
	<script src="<c:url value="/js/bootstrap-tooltip.js"/>"></script>
	<!-- popover effect library -->
	<script src="<c:url value="/js/bootstrap-popover.js"/>"></script>
	<!-- button enhancer library -->
	<script src="<c:url value="/js/bootstrap-button.js"/>"></script>
	<!-- accordion library (optional, not used in demo) -->
	<script src="<c:url value="/js/bootstrap-collapse.js"/>"></script>
	<!-- carousel slideshow library (optional, not used in demo) -->
	<script src="<c:url value="/js/bootstrap-carousel.js"/>"></script>
	<!-- autocomplete library -->
	<script src="<c:url value="/js/bootstrap-typeahead.js"/>"></script>
	<!-- tour library -->
	<script src="<c:url value="/js/bootstrap-tour.js"/>"></script>
	<!-- library for cookie management -->
	<script src="<c:url value="/js/jquery.cookie.js"/>"></script>
	<!-- calander plugin -->
	<script src='<c:url value="/js/fullcalendar.min.js"/>'></script>
	<!-- data table plugin -->
	<script src='<c:url value="/js/jquery.dataTables.min.js"/>'></script>

	<!-- chart libraries start -->
	<script src="<c:url value="/js/excanvas.js"/>"></script>
	<script src="<c:url value="/js/jquery.flot.min.js"/>"></script>
	<script src="<c:url value="/js/jquery.flot.pie.min.js"/>"></script>
	<script src="<c:url value="/js/jquery.flot.stack.js"/>"></script>
	<script src="<c:url value="/js/jquery.flot.resize.min.js"/>"></script>
	<!-- chart libraries end -->

	<!-- select or dropdown enhancer -->
	<script src="<c:url value="/js/jquery.chosen.min.js"/>"></script>
	<!-- checkbox, radio, and file input styler -->
	<script src="<c:url value="/js/jquery.uniform.min.js"/>"></script>
	<!-- plugin for gallery image view -->
	<script src="<c:url value="/js/jquery.colorbox.min.js"/>"></script>
	<!-- rich text editor library -->
	<script src="<c:url value="/js/jquery.cleditor.min.js"/>"></script>
	<!-- notification plugin -->
	<script src="<c:url value="/js/jquery.noty.js"/>"></script>
	<!-- file manager library -->
	<script src="<c:url value="/js/jquery.elfinder.min.js"/>"></script>
	<!-- star rating plugin -->
	<script src="<c:url value="/js/jquery.raty.min.js"/>"></script>
	<!-- for iOS style toggle switch -->
	<script src="<c:url value="/js/jquery.iphone.toggle.js"/>"></script>
	<!-- autogrowing textarea plugin -->
	<script src="<c:url value="/js/jquery.autogrow-textarea.js"/>"></script>
	<!-- multiple file upload plugin -->
	<script src="<c:url value="/js/jquery.uploadify-3.1.min.js"/>"></script>
	<!-- history.js for cross-browser state change on ajax -->
	<script src="<c:url value="/js/jquery.history.js"/>"></script>
	<!-- application script for Charisma demo -->
	<script src="<c:url value="/js/charisma.js"/>"></script>
	
	
	<script src="<c:url value="/js/testevol.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/diff_match_patch.js"/>"></script>
	<link type="text/css" rel="stylesheet" href="<c:url value="/css/jquery.qtip.min.css"/>" />
    <script type="text/javascript" src="<c:url value="/js/jquery.qtip.min.js"/>"></script>
	
	<script>
	

		/**
		 * Convert a diff array into a pretty HTML report.
		 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples.
		 * @return {string} HTML representation.
		 */
		diff_match_patch.prototype.diff_prettyHtml_separated = function(diffs,
				oldOrNew) {
			var html = [];
			var pattern_amp = /&/g;
			var pattern_lt = /</g;
			var pattern_gt = />/g;
			var pattern_para = /\n/g;
			for ( var x = 0; x < diffs.length; x++) {
				var op = diffs[x][0];
				// Operation (insert, delete, equal)
				var data = diffs[x][1];
				// Text of change.
				var text = data.replace(pattern_amp, '&amp;').replace(
						pattern_lt, '&lt;').replace(pattern_gt, '&gt;')
						.replace(pattern_para, '<br>');
				switch (op) {
				case DIFF_INSERT:
					if (oldOrNew == 'old') {
						break;
					}
					html[x] = '<ins style="background:#e6ffe6;">' + text
							+ '</ins>';
					break;
				case DIFF_DELETE:
					if (oldOrNew == 'new') {
						break;
					}
					html[x] = '<del style="background:#ffe6e6;">' + text
							+ '</del>';
					break;
				case DIFF_EQUAL:
					html[x] = '<span>' + text + '</span>';
					break;
				}
			}
			return html.join('');
		};

		function addFeedbackMessage() {
			var buttonMessage = $('<button class="btn btn-primary noty" style="display:none" id="feedbackMessage" data-noty-options=\'{"text":"This is a success information","layout":"top","type":"information"}\'><i class="icon-bell icon-white"></i> Top Full Width</button>');
			$('body').append(buttonMessage);
		}

		function showFeedbackMessage(message, type) {
			if (!type) {
				type = getFeedbackMessageType();
			}
			$('#feedbackMessage').attr(
					'data-noty-options',
					'{"text":"' + message + '","layout":"top","type":"' + type
							+ '"}');
			$('#feedbackMessage').trigger('click');
		}

		function mustShowFeedbackMessage() {
			return window.location.search.indexOf("success=true") >= 0
					|| window.location.search.indexOf("success=false") >= 0;
		}

		function successExecution() {
			return window.location.search.indexOf("success=true") >= 0;
		}

		function getFeedbackMessageType() {
			if (!successExecution()) {
				return 'error';
			}
			return 'information';
		}

		var projects = [];
		var loaded=true;
		
		if((typeof load_projects == 'undefined') || load_projects){
			$.get('<c:url value="/projects/names"/>', function(data) {
				loadProjects(data);
			});			
		}

		function loadProjects(data) {
			for ( var i = 0; i < data.length; i++) {
				$("#main_menu")
						.append(
								$('<li><a class="ajax-link" href="<c:url value="/projects/'+data[i]+'"/>"><span class="hidden-tablet"> '
										+ data[i] + ' </span></a></li>'));
			}
		}
	</script>