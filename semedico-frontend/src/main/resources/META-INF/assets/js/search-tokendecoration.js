Tapestry.Initializer.assignTokenClasses = function() {
    var listItems = $('#searchInputDiv .token-input-list-suggestions').children();
    tokens = $('#searchInputField').tokenInput('get');
    var defaultTooltipOptions = {
        // Does not only define the position of a tooltip relative to the selected token but does also add the callout-triangle
         position : {
            my : "center top+20",
            at : "center bottom",
            using : function(position, feedback) {
                $(this).css(position);
                $("<div>").addClass("arrow callout-top").
                addClass(feedback.horizontal).
                appendTo(this);
            }
        },
        tooltipClass : "term-tooltip"
    }
    $(tokens).each(function(index, token) {
        var listItem = listItems.eq(index);
        if (listItem.data('hasBeenDecorated'))
            return;
        listItem.data('hasBeenDecorated', true);
        // check if we have found the input token
        if (listItem.children('input').length !== 0)
            // because then we actually meant the next token
            listItem = listItems.eq(index+1);
        if (token.tokentype == "AMBIGUOUS_CONCEPT") {
            listItem.addClass("ambiguousToken");
        } else if (token.tokentype == "KEYWORD") {
            listItem.addClass("keywordToken");
            listItem.attr('title', 'This term is searched for verbatim in document text.');
            listItem.tooltip($.extend({}, defaultTooltipOptions, {
                content:function(){
                    var markup =  '<h1 class="name">' + token.name + '</h1>';
                    markup += '<span class="facetName">' + token.facetname + '</span>';
                    markup += "<div class='nofloat' />";
                    markup += '<p>This term is searched for verbatim in document text.</p>';
                    return markup;
            }}));
        } else if (token.tokentype == "CONCEPT") {
            listItem.attr('title', token.name);
            listItem.tooltip($.extend({}, defaultTooltipOptions, {
                content:function(){
                    var name = token.name;
                    var preferredName = token.preferredname;
                    var facetName = token.facetname;
                    var synonyms = token.synonyms;
                    var descriptions = token.descriptions;
                    
                    return Semedico.getConceptTooltipContent(name, preferredName, facetName, synonyms, descriptions);
            },
            close : function(event, ui) {
                ui.tooltip.hover(function() {
                     $(this).stop(true).fadeTo(1, 1);
                }, function() {
                     $(this).fadeOut("100", function() {
                         $(this).remove();
                     })
                });
            },
            open: function(event, ui) {
                Semedico.scrollSmoothly(ui.tooltip.find('div.list-box'));
                Semedico.abbreviateListItems(ui.tooltip.find('div.list-box ul li'), 'span.tooltip-list');
            }
            }));
        } else if (token.tokentype == "AND" || token.tokentype == "OR" || token.tokentype == "NOT"
                || token.tokentype == "LEFT_PARENTHESIS" || token.tokentype == "RIGHT_PARENTHESIS") {
            listItem.addClass("booleanToken");
            listItem.attr('title', 'This term is a boolean operator.');
            listItem.tooltip(defaultTooltipOptions);
        }
    });
};
