/**
 * Error dialog
 * 
 */
function showErrorDialog() {
	$("#errorDialog").dialog({
		modal : true,
		draggable : false,
		buttons : {
			Close : function() {
				$(this).dialog("close");
			}
		},
		open : function() {
			setTimeout(function(){
				$('#searchInputField').blur();
				$('.ui-dialog-buttonset button').focus();
			},1)
		},
		close: function(){
			$('#searchInputField').focus();
		},
		position: {my: "center bottom", at: "center", of: window}
	});
	var widget = $("#errorDialog" ).dialog( "widget" );
	widget.find("span.ui-dialog-title").addClass("error");
};

// This only makes the enter key closing the dialog.
$($(document)
		.delegate(
				'.ui-dialog',
				'keydown',
				function(e) {
					var tagName = e.target.tagName.toLowerCase();

					tagName = (tagName === 'input' && e.target.type === 'button') ? 'button'
							: tagName;

					if (e.which === $.ui.keyCode.ENTER
							&& tagName !== 'textarea' && tagName !== 'select'
							&& tagName !== 'button') {
						$(this).find('.ui-dialog-buttonset button').eq(1)
								.trigger('click');

						return false;
					}
				}));