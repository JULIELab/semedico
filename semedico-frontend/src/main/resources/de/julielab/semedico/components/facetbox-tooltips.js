Tapestry.Initializer.initializeFacetTermTooltips = function() {
    var activated = false;
    var timeoutId = null;

    // And the jQuery version.
    $j(document).ajaxComplete(function(event, xhr, settings) {
        setupTooltips();
    });
    // Nontheless, we have to setup the tooltips ourselves on document ready -
    // the above callback will only happen after a Ajax call, but when we load
    // the page for the first time, there was none yet.
    setupTooltips();

    // The tooltip has two major customizations:
    // First, one can pass a JSON array of values to it. Each value will be
    // separated from the others by a blank line.
    // If we don't pass an array, we just take one single value.
    // Second, a tooltip can be kept open - e.g. for copy actions - by clicking
    // on it (see open and close methods). The hold-open-operation was inspired
    // by: http://stackoverflow.com/a/17403334/1314955
    function setupTooltips() {
        var defaultShow = {
            delay : 500,
            show : 100
        };
        var facetTermLabels = $j(Semedico.facetTooltipSelector);
        facetTermLabels.each(function(index, label) {
            if ($j(label).data('ui-tooltip'))
                return;
            $j(label).tooltip(
                    {
                        tooltipClass : "term-tooltip",
                        position : {
                            my : "left+10 top",
                            at : "right top",
                            of : label,
                            collision : "fit",
                            using : function(position, feedback) {
                                // This function displays a tooltip callout pointing to the selected facet term list item.
                                // Most logic is required due to the correct vertical positioning of the callout.
                                // Since we use 'collision: fit', tooltips are shifted to the top of the page if they would
                                // otherwise flow across the bottom of the page. We have to compensate this.
                                
                                // this top shifting has occurred because there
                                // was not enough space to display the whole
                                // tooltip with the requested positioning (see
                                // my and at above)
                                var positioningTopshift = feedback.target.top - feedback.element.top;
                                // this top shifting is done due to the CSS
                                // proportions of the callout that is shifted a
                                // bit down due to styling reasons (it just
                                // looks better); corresponding CSS in
                                // semedico-tooltips.css.
                                var stylingTopshift = 7;
                                position.top -= stylingTopshift;
                                // with the "fit" collision detection,
                                // alignment just works, so stick to it
                                feedback.vertical = "top";
                                $j(this).css(position);
                                 var callout = $j("<div>").addClass("arrow callout-left").addClass(feedback.vertical).addClass(feedback.horizontal);
                                 // if the tooltip has been shifted (due to
                                 // the collision: fit setting):
                                 // move the callout vertically to actually
                                 // point to the facet term list item
                                 // (I have no idea why we have to add
                                 // another '3' pixels, but we have to (as
                                 // can be seen by taking it away and looking
                                 // at shifted tooltips)
                                 if (positioningTopshift)
                                     callout.css("top", ((feedback.target.top - feedback.element.top) + stylingTopshift+3) + "px");
                                 // the top of the callout is relative to the
                                 // top of the tooltip
                                 var calloutTop = parseInt(callout.css('top'), 10) + feedback.element.top;
                                 var calloutHeight = parseInt(callout.css('height'), 10);
                                 // Only show the callout if it does not flow
                                 // out of the tooltip, i.e. if the tooltip
                                 // is actually in juxtaposition of the list
                                 // item. This is not the case when
                                 // displaying tooltips at the very bottom of
                                 // the screen
                                 if (calloutTop + calloutHeight < feedback.element.top + feedback.element.height)
                                     callout.appendTo(this);
                                // $j("<div>").addClass("gapcloser-left").appendTo(this);
                                // var arrow =
                                // $j(feedback.element).find(".arrow");
                                // arrow.css("top", "30px");
                            }
                        },
                        content : function() {
                            var name = $j(this).attr('preferredName');
                            var facetName = $j(this).attr('facetName');
                            var synonyms = JSON.parse($j(this).attr('synonyms'));
                            var descriptions = JSON.parse($j(this).attr('descriptions'));

                            return Semedico.getConceptTooltipContent(name, null, facetName, synonyms, descriptions);
                        },
                        close : function(event, ui) {
                            timeoutId = window.setTimeout(function() {
                                $j(Semedico.facetTooltipSelector).tooltip({
                                    show : defaultShow
                                });
                                timeoutId = null;
                            }, 1000);
                            ui.tooltip.hover(function() {
                                $j(this).stop(true).fadeTo(1, 1);
                            }, function() {
                                $j(this).fadeOut("100", function() {
                                    $j(this).remove();
                                })
                            });
                        },
                        open : function(event, ui) {
                            if (timeoutId != null) {
                                window.clearTimeout(timeoutId);
                            }
                            // prepare the content of the tooltip; we have to
                            // wait until it is actually shown, so wait for the
                            // configured delay
                            window.setTimeout(
                                    function() {
                                        Semedico.scrollSmoothly(ui.tooltip.find('div.list-box'));
                                        Semedico.abbreviateListItems(ui.tooltip.find('div.list-box ul li'),
                                                'span.tooltip-list');
                                        $j(Semedico.facetTooltipSelector).tooltip({
                                            show : {
                                                delay : 0,
                                                duration : 0
                                            }
                                        });
                                    }, $j(event.target).tooltip("option", "show").delay);
                            activated = true;
                        },
                        show : defaultShow,
                        hide : 50
                    });
        });
    }
}
