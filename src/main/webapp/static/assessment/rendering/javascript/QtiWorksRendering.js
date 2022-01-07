/*
 *
 * Requirements:
 *
 * jquery.js
 * jquery-ui.js (incl. Draggable, Resizable, Sortable, Dialog, Slider)
 *
 * Author: David McKain
 *
 * Copyright (c) 2012-2013, The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

var QtiWorksRendering = (function() {

    var submitCallbacks = [];
    var resetCallbacks = [];

    var registerSubmitCallback = function(callback) {
        submitCallbacks.push(callback);
    };

    var registerResetCallback = function(callback) {
        resetCallbacks.push(callback);
    };

    var queryInputElements = function(responseIdentifier) {
        return jQuery('input[name=qtiworks_response_' + responseIdentifier + ']');
    };



    /************************************************************/
    /* Public methods */

    return {
        maySubmit: function() {
            var allowSubmit = true;
            if(submitCallbacks.length > 0) {
	            for (var i in submitCallbacks) {
	                allowSubmit = submitCallbacks[i]();
	                if (!allowSubmit) {
	                    break;
	                }
	            }
            }
            return true;
        },

        reset: function() {
            for (var i in resetCallbacks) {
                resetCallbacks[i]();
            }
        },

        showInfoControlContent: function(inputElement) {
            jQuery(inputElement).next('div').show();
            inputElement.disabled = true;
            return false;
        },

        registerReadyCallback: function(callback) {
            jQuery(document).ready(function() {
				callback();
            });
        },

        validateInput: function(obj) {
            var errorMessage = '';
            var value = obj.value;
            for (var i=1; i<arguments.length; i++) {
                switch (arguments[i]) {
                    case 'float':
                        if (!value.match(/^-?[\d\.]+$/)){
                            errorMessage += 'This input must be a number!\n';
                        }
                        break;

                    case 'integer':
                        if (!value.match(/^-?\d+$/)){
                            errorMessage += 'This input must be an integer!\n';
                        }
                        break;

                    case 'regex':
                        var regex = arguments[++i];
                        if (!value.match(regex)) {
                            errorMessage += 'This input is not valid!\n';
                        }
                        break;
                }
            }
            if (errorMessage.length!=0) {
                alert(errorMessage);
                jQuery(obj).addClass("badResponse");
                return false;
            }
            else {
                jQuery(obj).removeClass("badResponse");
                return true;
            }
        }
    };
})();
