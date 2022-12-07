(function ($) {
    $.fn.qtiCountDown = function(options) {
    	"use strict";
    	var settings = $.extend({
    		timeLimit: null,
    		formName: null,//forn name
    		dispIdField: null,//form dispatch id
    		dispId: null,//item id
    		eventIdField: null // form eventFieldId
        }, options );
    	
    	if(!(typeof window.qti21CountDown === "undefined")) {
    		window.qti21CountDown.cancel();
    	}
    	
    	var wrapperId = this.attr('id');
    	var startTime = performance.now();
    	var availableTime = settings.timeLimit;
		displayRemainingTime(wrapperId, settings.timeLimit, settings.timeLimit);
    	
    	var periodic = jQuery.periodic({period: 1000, decay:1.0, max_period: availableTime + 1000 }, function() {
			var remaining = availableTime - (performance.now() - startTime);
			if(jQuery("#" + settings.formName).length == 0) {
				periodic.cancel();
				if(window.qti21CountDown == periodic) {
					window.qti21CountDown = null;
				}
			} else if(remaining >= 0) {
				displayRemainingTime(wrapperId, settings.timeLimit, remaining);
			} else {
				periodic.cancel();
				timesUp(settings);
			}
		});
    	window.qti21CountDown = periodic;
        return this;
    };
    
    function timesUp(settings) {
    	//remove message, disable buttons
    	jQuery('.o_sel_assessment_item_submit').prop("disabled", "true");
    	jQuery('.o_qti_times_up').css("display", "inline");
    	//reload the page but only if in test
    	if(jQuery('#o_qti_run').length > 0) {
    		o_ffXHREvent(settings.formName, settings.dispIdField, settings.dispId, settings.eventIdField, '2', false, false, false, e, 'cid', 'timesUp');
    	}
    }
    
    function displayRemainingTime(wrapperId, availableTime, remainingTime) {
    	var available = formatRemainingTime(availableTime);
    	jQuery('#' + wrapperId + ' .o_qti_timer_duration').html(available);
    	if(remainingTime < 0) {
    		remainingTime = 0;
    	}
    	var remaining = formatRemainingTime(remainingTime);
    	jQuery('#' + wrapperId + ' .o_qti_timer').html(remaining);
    	if(remainingTime < 5 * 1000) {
    		addClass(wrapperId, "o_panic");
    	} else if(remainingTime < 15 * 1000) {
    		addClass(wrapperId, "o_15_seconds");
    	} else if(remainingTime < 30 * 1000) {
    		addClass(wrapperId, "o_30_seconds");
    	} 
    }
    
    function addClass(wrappedId, warningClass) {
    	var wrapperEl = jQuery('#' + wrappedId);
    	if(!wrapperEl.hasClass(warningClass)) {
        	wrapperEl.removeClass("o_30_seconds").removeClass("o_15_seconds").removeClass("o_panic");
    		wrapperEl.addClass(warningClass);
    	}
    }
    
    function formatRemainingTime(remaining) {
    	return Math.floor(remaining / 1000);
    }
}( jQuery ));