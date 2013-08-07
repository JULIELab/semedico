$j(function () {
	$j.widget("ui.tooltip", $j.ui.tooltip, {
		options: {
			content: function () {
				return $j(this).prop('title');
            }
          }
      });
      
      $j(document).tooltip();
});