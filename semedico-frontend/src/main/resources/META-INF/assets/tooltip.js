$j(function() {
    // This is the PROTOTPYE (not JQuery!) registration for completed Ajax
    // calls. This is because we currently use Tapestry in its default where it
    // uses Prototype as JavaScript library and the original facet boxes are
    // written with Prototype. Thus, facet Ajax calls happen with Prototype.
    //
    // What are we doing here:
    // We have a problem with binding Tooltip objects to elements that can be
    // reloaded via Ajax like the facet boxes (e.g. collapsing/expanding them,
    // drilling up etc). Then, the old bindings will disappear since the old
    // HTML elements are replaced by new ones, loaded via Ajax. Thus we have to
    // re-bind the tooltips after each Ajax call (actually only after Ajax calls
    // concerning elements with tooltips, but we just ignore that and bind all
    // tooltips anew on an Ajax call completing).
    // Here, we register a function that is called on completion of each Ajax
    // call that sets up the tooltips again.
    Ajax.Responders.register({
        onComplete : function() {
            setupTooltips();
        }
    });

    // And the jQuery version.
    $j(document).ajaxComplete(function(event, xhr, settings) {
        setupTooltips();
    });
    // Nontheless, we have to setup the tooltips ourselves on document ready -
    // the above callback will only happen after a Ajax call, but when we load
    // the page for the first time, there was none yet.
    setupTooltips();
});

function setupTooltips() {

    // The tooltip has two major customizations:
    // First, one can pass a JSON array of values to it. Each value will be
    // separated from the others by a blank line.
    // If we don't pass an array, we just take one single value.
    // Second, a tooltip can be kept open - e.g. for copy actions - by clicking
    // on it (see open and close methods). The hold-open-operation was inspired
    // by: http://stackoverflow.com/a/17403334/1314955
    var stayOpen = false;
    var tooltipElements = $j(".tooltip");

    tooltipElements.each(function(index, element) {
        if ($j(element).data('ui-tooltip'))
            return;
        $j(element).tooltip({
            tooltipClass : "tooltipBox",
            content : function() {
                var text = $j(this).prop('title');
                // Does the text look like a JSON array?
                if (text[0] == '[' && text[text.length - 1] == ']') {
                    // We will try to parse the string. Then, we take it as an
                    // array. Else we will just return the verbatim string.
                    try {
                        array = JSON.parse($j(this).prop('title'));
                        concatText = "";
                        for (var i = 0; i < array.length; i++) {
                            concatText += array[i];
                            if (i < array.length - 1 && array[i + 1] != "")
                                concatText += "<br/><br/>";
                        }
                        return concatText;
                    } catch (error) {
                        console.log(text)
                        console.log(error)
                        return text;
                    }
                }
                return text;
            },
            show : {
                delay : 500, // fade in
                duration : 100
            },
            hide : 0, // just disappear
            open : function(event, ui) {
                ui.tooltip.on("click", function() {
                    // By clicking on a tooltip, we tell it to stay visible.
                    stayOpen = true;
                    // Most probably the tooltip was already fading away when
                    // the
                    // click comes. Get it back.
                    $j(this).stop(true).fadeTo(100, 1);

                });
            },
            close : function(event, ui) {
                ui.tooltip.hover(function() {
                    // If someone has clicked on the tooltip - setting stayOpen
                    // to
                    // true - keep it open as long
                    // as the cursor hovers above the tooltip.
                    if (stayOpen) {
                        $j(this).stop(true).fadeTo(1, 1);
                    }
                }, function() {
                    // When the cursor leaves the tooltip, it will disappear, no
                    // matter what.
                    // $j(this).fadeOut("100", function() {
                    // Set stayOpen to false to avoid confusion if the same
                    // tooltip will be hovered over again without clicking on
                    // it. If stayOpen is still true, the tooltip would now
                    // always stay open when the cursor is hovering above it, if
                    // we clicked or not.
                    // stayOpen = false;
                    // $j(this).remove();
                    // })
                });
            },
            // position: {my: "left top", at: "right top", collision:
            // "flipfit"}, //
            // use either position or track
            track : false
        });
    });
}
