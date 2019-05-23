// This overrides a security function of jQueryUI which by default doesn't allow HTML in the title to avoid scripting vulnerabilities.
$.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
    _title : function(title) {
        if (!this.options.title) {
            title.html("&#160;");
        } else {
            title.html(this.options.title);
        }
    }
}));

/**
 * An empty, global variable to hold an instance of the DisambiguationManager.
 */
var disambiguationManager = {}
/**
 * Called from the Tapestry DisambiguationDialog component. Sets up the
 * DisambiguationManager that then handles all things associated with
 * disambiguation, from assigning the correct CSS class to showing the
 * disambiguation dialog and the disambiguation itself.
 */
Tapestry.Initializer.setupDisambiguation = function(parameters) {
    var dialogElementId = parameters.dialogElementId;
    var disambiguationZoneId = parameters.dialogZoneId;
    disambiguationManager = new DisambiguationManager(dialogElementId, disambiguationZoneId);
}

/**
 * This object is instantiated at first by the Tapestry initializer call issued
 * by DisambiguationDialog component.
 */
function DisambiguationManager(elementId, disambiguationZoneId) {
    // setup dialog
    function setupDisambiguationDialog() {
        $("#" + elementId).dialog({
            modal : false,
            draggable : false,
            autoOpen : false,
            dialogClass : 'disambiguation',
            show : true,
            hide : true
        });
        var widget = $('#' + elementId).dialog("widget");
        // stay open when mouse enters the dialog
        widget.mouseenter(function() {
            $("#" + elementId).dialog("open");
            $(this).stop(true).fadeTo(1, 1);
        });
        // close when the mouse leaves the dialog
        widget.mouseleave(function() {
            $('#' + elementId).dialog("close");
        });
        // append the help icon
        widget
                .find('div.ui-dialog-titlebar')
                .append(
                        $(
                                "<span class='help icon' title='This query term refers to multiple concepts. You may select a specific meaning to which the term will then be confined. Otherwise, all listed concepts will be searched for. '/>")
                                .tooltip({
                                    position : {
                                        my : "center top+15",
                                        at : "center bottom",
                                        using : function(position, feedback) {
                                            $(this).css(position);
                                            $("<div>").addClass("arrow callout-top black small").
                                            addClass(feedback.horizontal).appendTo(this);
                                        }
                                    },
                                    tooltipClass : "disambiguation-help small black"
                                }));

        // explicitly close the dialog when clicking outside of the dialog
        widget.bind("clickoutside", function(event) {
            $('#' + elementId).dialog("close");
        });
    }
    ;
    this.openDisambiguationDialog = function() {
        // If the dialog is still visible (e.g. is currently in its hide
        // animation), quickly show it again
        if ($("#" + elementId).dialog("widget").css("display") != "none") {
            $("#" + elementId).dialog("widget").stop(true).fadeTo(1, 1);
        }
        $("#" + elementId).dialog("open");
        var widget = $('#' + elementId).parent();
        widget.focus();
    };
    this.closeDisambiguationDialog = function() {
        $("#" + elementId).dialog("close");
    };
    this.dialogIsOpen = function() {
        return $("#" + elementId).dialog("isOpen");
    };
    this.getDialogWidget = function() {
        return $('#' + elementId).dialog("widget");
    };

    // setup token DOM objects (the li elements in of the token list)
    // this method is also called from autocomplete.js which leads to (currently
    // unavoidable) double call
    this.setupTokens = function() {
        var listItems = $('#searchInputDiv .token-input-list-suggestions').children();
        tokens = $('#searchInputField').tokenInput('get');
        $(tokens).each(
                function(index, token) {
                    if (token.tokentype == "AMBIGUOUS_CONCEPT") {
                        var domItem = listItems.eq(index);
                        if (!$(domItem).data('ambiguousToken')) {
                            var ambiguousToken = new AmbiguousQueryToken(token, domItem, index, elementId,
                                    disambiguationZoneId);
                            $(domItem).data('ambiguousToken', ambiguousToken);
                        }
                    }
                });
    }

    /**
     * A simple replace function for tokens from jquery tokeninput
     */
    this.replaceTokens = function(oldTokenIndex, newToken) {
        tokens = $('#searchInputField').tokenInput('get');
        var newTokens = [];
        for (var i = 0; i < tokens.length; i++) {
            if (i == oldTokenIndex) {
                newTokens.push(newToken);
            } else {
                newTokens.push(tokens[i]);
            }
        }
        $('#searchInputField').tokenInput('clear');
        $(newTokens).each(function(index, item) {
            $('#searchInputField').tokenInput('add', item);
        });
    }

    setupDisambiguationDialog();
    this.setupTokens();
}

/**
 * For each ambiguous token, one instance of this class is created by the
 * DisambiguationManager. This class handles the ajax communication with the
 * server to fill the disambiguation dialog with the correct HTML contents. This
 * class uses the DisambiguationManager to to open the dialog and update the
 * tokens when a token is disambiguated.
 */
function AmbiguousQueryToken(token, domElement, tokenindex, dialogElementId, disambiguationZoneId) {

    var init = function() {
        domElement.mouseenter(function() {
            // At first, remove all event listeners for the zone update.
            // Necessary for the case when we have multiple ambiguous tokens
            // because we only have a single dialog. When all ambiguous tokens
            // listen for the event, they will all get triggered as soon as we
            // disambiguate to a token and then all get replaced by the selected
            // concept.
            $('#' + disambiguationZoneId).off("t5:zone:did-update");
            // Now attach an event listener that is fired when the
            // disambiguation
            // dialog zone has been updated to show the possible options for the
            // currently selected ambiguous token.
            $('#' + disambiguationZoneId).on("t5:zone:did-update", setupDisambiguationHandler);
            if (!disambiguationManager.dialogIsOpen()) {
                // first, set the title and position of the dialog to the token
                // the cursor has entered
                $('#' + dialogElementId).dialog("option", {
                    title : "Disambiguation of <span class='disambiguatedTerm'>" + token.name,
                    position : {
                        my : "left top",
                        at : "left bottom+20",
                        of : domElement
                    }
                });
                disambiguate();
            }
        });
        domElement.mouseleave(function() {
            if (disambiguationManager.dialogIsOpen()) {
                disambiguationManager.closeDisambiguationDialog();
            }
        });
    }

    var disambiguate = function() {
        var url = token.showDialogLink + "?q=" + token.disambiguationOptions.join(',');
        // this triggers the Tapestry-provided zone refresh directly, so we
        // don't have to do much.
        $('#' + disambiguationZoneId).trigger("t5:zone:refresh", {
            url : url
        });

        // opens dialog
        disambiguationManager.openDisambiguationDialog('disambiguationdialog');
    }

    /**
     * Applies the click handler for the disambiguation items and their synonym tooltips.
     */
    function setupDisambiguationHandler(event) {
        var widget = disambiguationManager.getDialogWidget();
        applyTooltip(widget.find('li.disambiguationTerm .termname'));
        // append onclick eventhandlers to disambiguation options
        $('#' + dialogElementId + ' .disambiguationTerm').click(function() {
            // what happens if one of the disambiguation options is clicked
            // on...
            $('#' + dialogElementId).dialog("close");
            // var selectedConceptName = $(this.innerHTML).text();
            var selectedConceptId = this.getAttribute('conceptid');
            $.getJSON(token.getConceptTokensLink + "?q=" + selectedConceptId, function(response) {
                // Replace token in searchbar,
                disambiguationManager.replaceTokens(tokenindex, response[0]);
                // after disambiguation we have to set the correct tooltips;
                // however, autocomplete.js defines anyway and it is repeated
                // for
                // each token because we add all tokens anew. Best would be to
                // run it
                // once, of course.
                // disambiguationManager.setupTokens();
                // this function is defined in search-tokendecoration.js
                // Tapestry.Initializer.assignTokenClasses();
            });
        });
    }

    function applyTooltip(elements) {
        elements.each(function(index, element) {
            $(element).tooltip(
                    {
                        position : {
                            my : "left+20 top-9",
                            at : "right middle",
                            of : element,
                            using : function(position, feedback) {
                                $(this).css(position);
                                // only display the callout if the tooltip is
                                // actually positioned top
                                if (feedback.vertical == "top")
                                    $("<div>").addClass("arrow callout-left black small").addClass(feedback.vertical)
                                            .addClass(feedback.horizontal).appendTo(this);
                            }
                        },
                        content : function() {
                            var synonyms = JSON.parse($(element).attr("title"));
                            var markup = "<ul>";
                            markup += "<li class='synonyms-heading'>synonyms</li>";
                            if (synonyms && synonyms.length > 0) {
                                $(synonyms).each(function(index, synonym) {
                                    markup += "<li>" + synonym + "</li>";
                                });
                            } else {
                                markup += "<li>none</li>";
                            }
                            markup += "</ul>";
                            return markup;
                        },
                        tooltipClass : "disambiguation-dialog-synonyms small black"
                    });
        });
    }
    init();
};

