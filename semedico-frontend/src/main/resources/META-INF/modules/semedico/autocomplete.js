define([ "jquery" ], function($, disambiguationManager) {
   return function setupAutoCompleter(elementId, url, options) {
       console.log("autocomplete is constructed")
        var inputFieldWidth = $('#searchInputDiv').width() - 2;
        options.resultsFormatter = function(item) {
            var resultsFormatter = '';
            if (item['type'] == 'term') {
                resultsFormatter = resultsFormatter + '<li class="term">';
                if (item['facetname']) {
                    resultsFormatter = resultsFormatter + '<span class="facetname">';
                    resultsFormatter = resultsFormatter + item['facetname'];
                    resultsFormatter = resultsFormatter + '</span>';
                }
                resultsFormatter = resultsFormatter + item['name'];
                // if (item['synonyms']) {
                // resultsFormatter = resultsFormatter + ' <span
                // class="synonym-text">';
                // resultsFormatter = resultsFormatter + item['synonyms'];
                // resultsFormatter = resultsFormatter + '</span>';
                // }
                if (item['preferredname'] && !(item['name'].toLowerCase() == item['preferredname'].toLowerCase())) {
                    resultsFormatter = resultsFormatter + '<span class="preferredname">';
                    resultsFormatter = resultsFormatter + '(' + item['preferredname'] + ')';
                    resultsFormatter = resultsFormatter + '</span>';
                }
                resultsFormatter = resultsFormatter + '</li>';
            } else {
                resultsFormatter = resultsFormatter + '<li class="facet">';
                resultsFormatter = resultsFormatter + item['name'];

                // the following, outcommented code would show the number of
                // suggestions in the respective facet; I currently don't think
                // this
                // is necessary or helpful
                // if (item['termSize'] == 1)
                // var termText = 'term';
                // else
                // var termText = 'terms';
                // resultsFormatter = resultsFormatter + ' (' + item['termSize']
                // + '
                // '
                // + termText + ')';
                resultsFormatter = resultsFormatter + '</li>';
            }
            return resultsFormatter;
        };

        options.onAdd = function(item) {
            // this handler is called after each addition of a single token;
            // We need to setup the tooltips for accepted suggestions. This
            // unfortunately also leads the the repeated call of this method
            // after
            // disambiguation because all tokens are replaced then. Not very
            // elegant.
            disambiguationManager.setupTokens();
            Tapestry.Initializer.assignTokenClasses();
        };
        options.remainingInputTokenizer = function(prefix) {
            var remainingTokens = [];
            // return the deferred object (similar concept to Futures in Java)
            // from
            // the asynchronous call
            return $.getJSON(options.conceptRecognitionUrl + '?q=' + encodeURI(prefix));
        };
        options.onRemainingTokensInserted = function() {
            // This handler is called when multiple words are input and only
            // after
            // that, a suggestion is selected. Then, all input before the
            // accepted
            // suggestion is sent to the options.remainingInputTokenizer method
            // above and the received tokens are added as "remaining" tokens.
            // After this is done, all new tokens must be handled according to
            // their
            // type, this is done here.
            disambiguationManager.setupTokens();
            Tapestry.Initializer.assignTokenClasses();
        };
        options.animateDropdown = false;
        options.onSelected = function(item) {
            if (item.triggerHandler("isTruncated")) {
                var content = item.triggerHandler("originalContent");
                // find the preferred name element (is it there at all?)
                var prefNameElement = $.grep(content, function(item) {
                    try {
                        return item.hasAttribute("class") && item.getAttribute("class") == "preferredname"
                    } catch (e) {
                        // this is not even a DOM object
                        return false;
                    }
                });
                // remove the parenthesis around the preferred name
                if (!prefNameElement || prefNameElement.length == 0)
                    return;
                var text = $(prefNameElement).text();
                text = text.substring(1, text.length - 1);
                var suggestionsBox = $("<div class=\"synonyms-box\">");
                item.append(suggestionsBox);
                var entryOffset = item.offset();
                suggestionsBox.append("<span class=\"all-synonyms-heading\">preferred name: </span>" + text);
                var entryWidth = item.outerWidth();
                entryOffset.left += entryWidth;
                entryOffset.top--;
                suggestionsBox.offset(entryOffset);
                var rect = suggestionsBox[0].getBoundingClientRect();
                var outerViewportBottom = rect.bottom >= (window.innerHeight || document.documentElement.clientHeight);
                if (outerViewportBottom) {
                    entryOffset.top -= rect.bottom - (window.innerHeight || document.documentElement.clientHeight) + 1;
                    suggestionsBox.offset(entryOffset);
                }
            }
        };
        options.onDeselected = function(item) {
            if (item.triggerHandler("isTruncated")) {
                var synonymsBox = item.find("div.synonyms-box");
                if (synonymsBox[0]) {
                    if (!synonymsBox[0].focus())
                        synonymsBox.remove();
                }
            }
        };
        options.onDropdownReady = function() {
            $('.token-input-dropdown-suggestions').width(inputFieldWidth);
            $('.term').dotdotdot({
                height : 35
            });
        };
        options.onHiddenInputUpdate = function(saved_tokens) {
            return JSON.stringify(saved_tokens);
        };
        options.propertiesToSubmit = function() {
            return [ this.tokenValue, this.propertyToSearch, "facetid", "freetext", "tokentype", "lexertype", "termid",
                    "synonyms", "descriptions", "showDialogLink", "getConceptTokensLink", "disambiguationOptions",
                    "userselected", "facetname", "preferredname", "query", "priority" ]
        };

        // if we don't set this to false, clicking on the "find" button will
        // delete
        // the search input field's contents, leading to an empty search
        options.clearOnBlur = false;

        // finally apply the actual TokenInput jQuery plugin to the input field
        $('#' + elementId).tokenInput(url, options);
    }
})
