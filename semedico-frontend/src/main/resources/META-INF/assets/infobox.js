/* The following code is based on/taken from the jQuery pop plugin, see:
 * https://github.com/seaofclouds/pop/
 *
 * Original header as follows:
 */

/* pop! for jQuery
 * v0.2 requires jQuery v1.2 or later
 *  
 * Licensed under the MIT:
 * http://www.opensource.org/licenses/mit-license.php
 *  
 * Copyright 2007,2008 SEAOFCLOUDS [http://seaofclouds.com]
 */

(function(jQuery) {
	jQuery.pop = function(options) {

		// settings
		var settings = {
			pop_class : '.infoBoxTooltip',
			pop_toggle_text : ''
		}

		// inject html wrapper
		function initpops() {
			jQuery(settings.pop_class).each(
					function() {
						var pop_classes = jQuery(this).attr("class");
						jQuery(this).addClass("infoBoxTooltip_menu");
						jQuery(this).wrap(
								"<div class='" + pop_classes + "'></div>");
						jQuery(".infoBoxTooltip_menu").attr("class", "infoBoxTooltip_menu");
						jQuery(this).before(
								" \
          <div class='infoBoxTooltip_toggle'>"
										+ settings.pop_toggle_text
										+ "</div> \
          ");
					});
		}
		initpops();

		// assign reverse z-indexes to each pop
		var totalpops = jQuery(settings.pop_class).size() + 1000;
		jQuery(settings.pop_class).each(function(i) {
			var popzindex = totalpops - i;
			jQuery(this).css({
				zIndex : popzindex
			});
		});
		// close pops if user clicks outside of pop
		activePop = null;
		function closeInactivePop() {
			jQuery(settings.pop_class).each(function(i) {
				if (jQuery(this).hasClass('active') && i != activePop) {
					jQuery(this).removeClass('active');
				}
			});
			return false;
		}
		jQuery(settings.pop_class).mouseover(function() {
			activePop = jQuery(settings.pop_class).index(this);
		});
		jQuery(settings.pop_class).mouseout(function() {
			activePop = null;
		});

		jQuery(document.body).click(function() {
			closeInactivePop();
		});

		// toggle that pop
		jQuery(".infoBoxTooltip_toggle").click(function() {
			jQuery(this).parent(settings.pop_class).toggleClass("active");
		});
	}
})(jQuery);