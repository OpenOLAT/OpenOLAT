/*
 *
 * Requirements:
 *
 * jquery.js (at least version 1.5.0)
 * UpConversionAjaxController.js
 * AsciiMathParser.js (optional - only needed for geek previews)
 *
 * Author: David McKain
 *
 * Copyright (c) 2008-2011, The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

var AsciiMathInputController = (function() {

    var helpDialog = null; /* (Created on first use) */

    /* See if AsciiMathParser.js was loaded in order to provide live raw
     * previews ACIIMath input.
     */
    var asciiMathParserLoaded = false;
    var asciiMathParser = null;
    try {
        asciiMathParser = new AsciiMathParser(AsciiMathParserBrowserUtilities.createXmlDocument());
        asciiMathParserLoaded = true;
    }
    catch (e) {
    }

    /************************************************************/

    var callAsciiMath = function(mathModeInput) {
        var mathElement = asciiMathParser.parseAsciiMathInput(mathModeInput, {
            displayMode: true,
            addSourceAnnotation: true
        });
        var mathml = AsciiMathParserBrowserUtilities.serializeXmlNode(mathElement);
        return AsciiMathParserBrowserUtilities.indentMathmlString(mathml);
    };

    var showHelpDialog = function(helpAElement) {
        if (helpDialog==null) {
            var helpPanel = jQuery('<div></div>');
            helpPanel.load(helpAElement.href);
            helpDialog = helpPanel.dialog({
                autoOpen: false,
                draggable: true,
                resizable: true,
                title: 'Input Hints',
                width: '70%'
            });
        }
        if (helpDialog.dialog('isOpen')) {
            helpDialog.dialog('close');
        }
        else {
            var buttonPosition = jQuery(helpAElement).position();
            helpDialog.dialog('option', 'position', [ buttonPosition.left, buttonPosition.top + 70 ]);
            helpDialog.dialog('open');
        }
    };

    /************************************************************/

    var Widget = function(_asciiMathInputId,  _verifierControl) {
        this.asciiMathInputControlId = _asciiMathInputId;
        this.verifierControl = _verifierControl;
        this.rawRenderingContainerId = null;
        this.rawSourceContainerId = null;
        this.helpButtonId = null;
        var lastInput = null;
        var currentXHR = null;
        var currentTimeoutId = null;
        var widget = this;

        /* Binds event handlers to the input widget to make it responsd to user input */
        this._init = function() {
            /* Bind help button */
            if (this.helpButtonId!=null) {
                var helpButton = jQuery("#" + this.helpButtonId);
                helpButton.click(function() {
                    showHelpDialog(this);
                    return false;
                });
            }

            /* Set up handler to update preview when required */
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            inputSelector.bind("change keyup keydown", function() {
                widget._userInputChanged();
            });
        };

        /**
         * Called after the user causes a change in input. This checks the input
         * to see if it is different from the last input and, if so, schedules
         * verification.
         */
        this._userInputChanged = function() {
            var asciiMathInput = this._getAsciiMathInput();
            if (lastInput==null || asciiMathInput!=lastInput) {
                lastInput = asciiMathInput;
                this._processInput(asciiMathInput);
            }
        };

        this._getAsciiMathInput = function() {
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            return inputSelector.get(0).value;
        };

        this._setAsciiMathInput = function(asciiMathInput) {
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            inputSelector.get(0).value = asciiMathInput || '';
            lastInput = asciiMathInput;
            this._processInput(asciiMathInput);
        };

        this._show = function(asciiMathInput, jsonData) {
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            inputSelector.get(0).value = asciiMathInput || '';
            lastInput = asciiMathInput;
            this.verifierControl.showVerificationResult(jsonData);
        };

        this._processInput = function(asciiMathInput) {
            /* Update live ASCIIMath preview (if used) */
            var asciiMathInput = widget._updateAsciiMathPreview(asciiMathInput);

            /* Call up verifier (if used) */
            if (this.verifierControl!=null) {
                if (asciiMathInput!=null && asciiMathInput.length>0) {
                    this.verifierControl.verifyLater(asciiMathInput);
                }
                else {
                    this.verifierControl.clear();
                }
            }
        };

        this._updateAsciiMathPreview = function(asciiMathInput) {
            /* Get ASCIIMathML to generate a <math> element */
            var mathmlSource = null;
            var message = null;
            if (asciiMathInput.match(/\S/)) {
                if (asciiMathParserLoaded) {
                    mathmlSource = callAsciiMath(this.getAsciiMathInput());
                }
                else {
                    message = "(AsciiMathParser.js not loaded)";
                }
            }
            else {
                message = "(Blank input)";
            }
            /* Update preview elements */
            if (this.rawRenderingContainerId!=null) {
                if (mathmlSource!=null) {
                    UpConversionAjaxController.replaceContainerMathMLContent(jQuery("#" + this.rawRenderingContainerId), mathmlSource);
                }
                else {
                    UpConversionAjaxController.replaceContainerMathMLContent(jQuery("#" + this.rawRenderingContainerId),
                    "<math><mtext>" + message + "</mtext></math>");
                }
            }
            if (this.rawSourceContainerId!=null) {
                UpConversionAjaxController.replaceContainerPreformattedText(jQuery("#" + this.rawSourceContainerId), mathmlSource || message);
            }
            return asciiMathInput;
        };

    };

    Widget.prototype.setHelpButtonId = function(id) {
        this.helpButtonId = id;
    };

    Widget.prototype.setRawRenderingContainerId = function(id) {
        this.rawRenderingContainerId = id;
    };

    Widget.prototype.setRawSourceContainerId = function(id) {
        this.rawSourceContainerId = id;
    };

    Widget.prototype.init = function() {
        this._init();
    };

    Widget.prototype.syncWithInput = function() {
        this._processInput(this._getAsciiMathInput());
    };

    Widget.prototype.getAsciiMathInput = function() {
        return this._getAsciiMathInput();
    };

    Widget.prototype.setAsciiMathInput = function(asciiMathInput) {
        this._setAsciiMathInput(asciiMathInput);
    };

    Widget.prototype.show = function(asciiMathInput, jsonData) {
        this._show(asciiMathInput, jsonData);
    };

    Widget.prototype.reset = function() {
        this._setAsciiMathInput(null);
    };

    return {
        bindInputWidget: function(asciiMathInputId, verifierControl) {
            if (asciiMathInputId==null) {
                throw new Error("asciiMathInputId must not be null");
            }
            if (verifierControl==null) {
                throw new Error("verifierControl must not be null");
            }
            return new Widget(asciiMathInputId, verifierControl);
        }
    };

})();
