(function ($) {
    $.fn.qtiTimer = function(options) {
    	"use strict";
    	var settings = $.extend({
    		testDuration: null,
    		availableTime: null,
    		formName: null,//forn name
    		dispIdField: null,//form dispatch id
    		dispId: null,//item id
    		eventIdField: null, // form eventFieldId,
    		csrfToken: ''
        }, options );
    	
    	if(!(typeof window.qti21TestTimer === "undefined")) {
    		window.qti21TestTimer.cancel();
    	}
    	
    	var wrapperId = this.attr('id');
    	var now = Date.now();
    	var startTime = now - settings.testDuration;
    	var availableTime = startTime + settings.availableTime;
    	var remainingTime = availableTime - now;
		displayRemainingTime(wrapperId, remainingTime);
    	
    	var periodic = jQuery.periodic({period: 1000, decay:1.0, max_period: remainingTime + 1000 }, function() {
			var remaining = availableTime - Date.now();
			if(remaining >= 0) {
				displayRemainingTime(wrapperId, remaining);
			} else {
				periodic.cancel();
				timesUp(settings);
			}
		});
    	window.qti21TestTimer = periodic;
        return this;
    };
    
    function timesUp(settings) {
    	//remove message, disable buttons
    	jQuery('.o_sel_assessment_item_submit').prop("disabled", "true");
    	jQuery('.o_qti_times_up').css("display", "inline");
    	//reload the page but only if in test
    	if(jQuery('#o_qti_run').length > 0) {
    		o_ffXHREvent(settings.formName, settings.dispIdField, settings.dispId, settings.eventIdField, '2', false, false, false, '_csrf', settings.csrfToken, 'cid', 'timesUp');
    	}
    }
    
    function displayRemainingTime(wrapperId, remainingTime) {
    	if(remainingTime < 0) {
    		remainingTime = 0;
    	}
    	
    	var seconds = Math.floor(remainingTime / 1000);
    	var minutes = Math.floor(seconds / 60);
    	seconds = seconds - (minutes * 60);
    	var hours = Math.floor(minutes / 60);
    	minutes = minutes - (hours * 60);
    
    	jQuery('#' + wrapperId + ' .o_qti_timer_hour').html(hours);
    	jQuery('#' + wrapperId + ' .o_qti_timer_minute').html(minutes);
    	jQuery('#' + wrapperId + ' .o_qti_timer_second').html(seconds);
  
		var cssClass = "o_hours";
    	if(remainingTime < 5 * 60 * 1000) {
    		cssClass = "o_5_minutes";
    	} else if(remainingTime < 10 * 60 * 1000) {
    		cssClass = "o_10_minutes";
    	} else if(remainingTime < 60 * 60 * 1000) {
    		cssClass = "o_60_minutes";
    	}
    	addClass(wrapperId, cssClass);
    }
    
    function addClass(wrappedId, warningClass) {
    	var wrapperEl = jQuery('#' + wrappedId);
    	if(!wrapperEl.hasClass(warningClass)) {
        	wrapperEl
        		.removeClass("o_5_minutes")
        		.removeClass("o_10_minutes")
        		.removeClass("o_60_minutes")
        		.removeClass("o_hours")
        		.addClass(warningClass);
    		jQuery('.o_qti_times_message.' + warningClass).css("display", "inline");  
    	}
    }
}( jQuery ));