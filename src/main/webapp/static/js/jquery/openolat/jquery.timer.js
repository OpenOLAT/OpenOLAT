(function ($) {
    $.fn.ooTimer = function(options) {
    	"use strict";
    	var settings = $.extend({
    		remainingTime: null, // in milliseconds
    		endUrl: null,
    		csrfToken: ''
        }, options );
    	
    	if(!(typeof window.ooTimer === "undefined")) {
    		window.ooTimer.cancel();
    	}
    	
    	var wrapperId = this.attr('id');
    	settings.wrapperId = wrapperId; 
    	var now = Date.now();
    	var remainingTime = settings.remainingTime;//milliseconds
    	var availableTime = now + remainingTime;
		displayRemainingTime(wrapperId, remainingTime);

    	var periodic = jQuery.periodic({period: 1000, decay:1.0, max_period: remainingTime + 10000 }, function() {
			var remaining = availableTime - Date.now();
			if(remaining >= 0) {
				displayRemainingTime(wrapperId, remaining);
			} else {
				displayRemainingTime(wrapperId, 0);
				periodic.cancel();
				timesUp(settings);
			}
		});
    	window.ooTimer = periodic;
        return this;
    };
    
    function timesUp(settings) {
    	if(jQuery('#' + settings.wrapperId).length > 0 && settings.endUrl != null && settings.endUrl.length > 0) {
    		o_XHREvent(settings.endUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'timesUp');
    	}
    }
    
    function displayRemainingTime(wrapperId, remaining) {
    	var minutes = Math.ceil(remaining / (60.0 * 1000.0));
    	var hours = Math.floor(minutes / 60.0);
    	minutes = minutes - (hours * 60.0);
    	if(hours < 0) {
    		hours = 0;
    		minutes = 0;
    	}
    	var hoursEl = document.querySelector("#" + wrapperId + " .o_timer_hours");
    	hoursEl.innerHTML = (hours < 10 ? "0" : "") + hours;
    	var minutesEl = document.querySelector("#" + wrapperId + " .o_timer_minutes");
    	minutesEl.innerHTML = (minutes < 10 ? "0" : "") + minutes;
    }
}( jQuery ));