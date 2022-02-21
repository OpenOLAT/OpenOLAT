(function ($) {
    $.fn.qtiTimer = function(options) {
    	"use strict";
    	var settings = $.extend({
    		testDuration: null,
    		availableTime: null,
    		formName: null,//forn name
    		dispIdField: null,//form dispatch id
    		dispId: null,//item id
    		eventIdField: null // form eventFieldId
        }, options );
    	
    	if(!(typeof window.qti21TestTimer === "undefined")) {
    		window.qti21TestTimer.cancel();
    	}
    	
    	var wrapperId = this.attr('id');
    	var now = performance.now();
    	var startTime = now - settings.testDuration;
    	var availableTime = startTime + settings.availableTime;
    	var remainingTime = availableTime - now;
		displayRemainingTime(wrapperId, settings.availableTime, remainingTime);
    	
    	var periodic = jQuery.periodic({period: 1000, decay:1.0, max_period: remainingTime + 1000 }, function() {
			var remaining = availableTime - performance.now();
			if(remaining >= 0) {
				displayRemainingTime(wrapperId, settings.availableTime, remaining);
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
    	removeMessages();
    	//reload the page but only if in test
    	if(jQuery('#o_qti_run').length > 0) {
    		o_ffXHREvent(settings.formName, settings.dispIdField, settings.dispId, settings.eventIdField, '2', false, false, false, 'cid', 'timesUp');
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
    	if(remainingTime < 1 * 60 * 1000) {
    		addClass(wrapperId, "o_panic");
    	} else if(remainingTime < 5 * 60 * 1000) {
    		addClass(wrapperId, "o_5_minutes");
    	} else if(remainingTime < 10 * 60 * 1000) {
    		addClass(wrapperId, "o_10_minutes");
    	} 
    }
    
    function addClass(wrappedId, warningClass) {
    	var wrapperEl = jQuery('#' + wrappedId);
    	if(!wrapperEl.hasClass(warningClass)) {
    		removeMessages();
        	wrapperEl.removeClass("o_10_minutes").removeClass("o_5_minutes").removeClass("o_panic");
    		
    		wrapperEl.addClass(warningClass);
    		jQuery('.o_qti_times_message.' + warningClass).css("display", "inline");  
    	}
    }
    
    function removeMessages() {
		jQuery('.o_qti_times_message.o_10_minutes').css("display", "none");
		jQuery('.o_qti_times_message.o_5_minutes').css("display", "none");
		jQuery('.o_qti_times_message.o_panic').css("display", "none");
    }
    
    function formatRemainingTime(remaining) {
    	var seconds = Math.floor(remaining / 1000);
    	var minutes = Math.floor(seconds / 60);
    	seconds = seconds - (minutes * 60);
    	var hours = Math.floor(minutes / 60);
    	minutes = minutes - (hours * 60);
    	return (hours < 10 ? "0" : "") + hours + (minutes < 10 ? ":0" : ":") + minutes + (seconds < 10 ? ":0" : ":") + seconds;
    }
}( jQuery ));