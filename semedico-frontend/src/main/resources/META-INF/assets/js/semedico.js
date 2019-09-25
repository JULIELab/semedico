var Semedico = {
    /* Semedico-wide constants or variables */

    /**
     * The selector to address the exact elements for which the facet term
     * tooltips should be applied. Since there are multiple locations that have
     * to access the selector, is is stored in this central object.
     */
    facetTooltipSelector : "a.tooltip-termlabel",

    /**
     * Applicable to an fixed-height element with overflow:scroll or
     * overflow-y:scroll. Adds a top and bottom element to the scroll area which
     * are switched on and off when scrolling such that the top element is
     * always visible if the content is not scrolled completely to top, the
     * bottom element analogously. The elements are styled with linear gradients
     * to make the borders appear smoothly and not just chopped off.
     * 
     * Additionally, for elements having no overflow, the overflow property is
     * set to 'auto' in order to hide the scroll bar.
     * 
     * @param jqueryElements
     *            a list of jQuery-objects referring to overflow:scroll
     *            elements.
     */
    scrollSmoothly : function(jqueryElements) {
        var fadeoutTopStyle = {
            "content" : '',
            'height' : '20px',
            'background' : 'linear-gradient(to bottom, rgba(255, 255, 255, 1) 15%, rgba(255, 255, 255, 0) 100%)',
            'position' : 'absolute',
            'pointer-events' : 'none',
            'display' : 'none'
        };
        var fadeoutBottomStyle = {
            "content" : "",
            "margin-top" : "-20px",
            "height" : "20px",
            "background" : "linear-gradient(to top, rgba(255, 255, 255, 1) 15%, rgba(255, 255, 255, 0) 100%)",
            "position" : "relative",
            "pointer-events" : "none"
        };

        jqueryElements.each(function(index, element) {
            if ($(this)[0].scrollHeight <= $(this).innerHeight()) {
                $(this).css("overflow", "auto");
                return;
            }
            var width = $(element).prop("scrollWidth");
            var fadeoutTop = $("<div class='fadeout-top' />");
            var fadeoutBottom = $("<div class='fadeout-bottom' />");
            $.extend(fadeoutTopStyle, {
                "width" : width
            });
            $.extend(fadeoutBottomStyle, {
                "width" : width
            });
            fadeoutTop.css(fadeoutTopStyle);
            fadeoutBottom.css(fadeoutBottomStyle);
            $(element).before(fadeoutTop);
            $(element).after(fadeoutBottom);
            $(element).scroll(function(event) {
                var scrollTop = $(this).scrollTop();
                var innerHeight = $(this).innerHeight();
                var scrollHeight = $(this)[0].scrollHeight;
                var width = $(element).prop("scrollWidth");

                if (scrollTop == 0) {
                    fadeoutTop.hide();
                } else {
                    fadeoutTop.show().width(width);
                }
                if (scrollTop + innerHeight >= scrollHeight) {
                    fadeoutBottom.hide();
                } else {
                    fadeoutBottom.show().width(width);
                }
            });
        });
    },
    /**
     * Uses dotdotdot() to abbreviate list items, i.e. li elements. The content
     * to be abbreviated must be embedded into the li elements and be found with
     * 'contentSelector' relative to each li element. The ellipsis is inserted
     * before the last two words such that not just the last part of the list
     * item is chopped off but the beginning and the ending is visible.
     * 
     * @param listItems
     *            a list of jQuery object representing li elements
     * @param contentSelector
     *            a selector relative to each li element that points to the
     *            content to be abbreviated
     */
    abbreviateListItems : function(listItems, contentSelector) {
        listItems.each(function() {
            if ($(this).children(contentSelector).length == 0)
                return;
            // derived from the 'shortened path' example on the dotdotdot
            // homepage
            var contentWords = $(this).children(contentSelector).html().split(' ');
            if (contentWords.length > 2) {
                var lastToken = contentWords.pop();
                var beforeLastToken = contentWords.pop();
                var suffix = beforeLastToken + ' ' + lastToken;
                $(this).children(contentSelector).html(
                        contentWords.join(' ') + '<span class="shortened-content"> ' + suffix + '</span>');
                $(this).dotdotdot({
                    after : 'span.shortened-content',
                    wrap : 'letter'
                });
            }
        });
    },
    /**
     * Initialization function for the tooltip component (default tooltips)
     * 
     * @param elementId
     * @param params
     */
    initTooltip : function(elementId, params) {
        var options = {
            content : function() {
                return params.content;
            }
        };
        $('#' + elementId).tooltip($.extend({}, options, {
            position : {
                my : "center top+15",
                at : "center bottom",
                using : function(position, feedback) {
                    $(this).css(position);
                    $("<div>").addClass("arrow callout-top").
                    // addClass(feedback.vertical).
                    addClass(feedback.horizontal).appendTo(this);
                }
            },
            close : function(event, ui) {
                ui.tooltip.hover(function() {
                    $(this).stop(true).fadeTo(1, 1);
                }, function() {
                    // $(this).fadeOut("100", function() {
                    // $(this).remove();
                    // })
                });
            }
        }));
    },
    /**
     * A function to unify tooltip contents for concepts. Takes a range of
     * concept related parameters like name, synonyms etc. and returns an HTML
     * structure for the display of the passed concept properties. Used by facet
     * concept tooltips and input token tooltips.
     * 
     * @param name
     * @param preferredName
     * @param facetName
     * @param synonyms
     * @param descriptions
     * @returns {String}
     */
    getConceptTooltipContent : function(name, preferredName, facetName, synonyms, descriptions) {
        var preferredNameSpan = "";
        if (preferredName)
            preferredNameSpan = "<span class='preferredName'>(" + preferredName + ')</span>';
        var markup = "<h1 class='name'>" + name + preferredNameSpan + "</h1>";
        markup += "<span class='facetName'>" + facetName + "</span>";
        markup += "<div class='nofloat' />"

        if (synonyms && synonyms.length > 0) {
            markup += "<h1>synonyms</h1>"
            // markup += "<div class='fadeout-top' />"
            markup += '<div class="list-box">';
            markup += "<ul class='tooltip-synonyms'>";
            $(synonyms).each(function(index, synonym) {
                markup += '<li><div class="square"/><span class="tooltip-list">' + synonym + '</span></li>';
            });
            // markup += '<li>&nbsp;</li>';
            markup += "</ul>";
            markup += "</div>";
            // markup += "<div class='fadeout-bottom' />"
        }

        if (descriptions && descriptions.length > 0) {
            markup += "<h1>description</h1>"
            markup += '<div class="list-box">';
            markup += "<ul class='descriptions'>";
            $(descriptions).each(function(index, description) {
                markup += '<li>' + description + '</li>';
            });
            markup += "</ul>";
            markup += "</div>";
        }
        return markup;
    }
}