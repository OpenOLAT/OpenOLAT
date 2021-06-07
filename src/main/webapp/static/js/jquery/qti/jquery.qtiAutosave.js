(function ($) {
    "use strict";
    
    $.fn.qtiAutosave = function(options) {
    	var settings = $.extend({
    		responseUniqueId: null,
    		formName: null,//form name
    		dispIdField: null,//form dispatch id
    		dispId: null,//item id
    		eventIdField: null, // form eventFieldId
    		csrf: null
        }, options );
    	
    	var wrapperId = this.attr('id');
		var periodic = jQuery.periodic({period: 60000, decay:1.0, max_period: Number.MAX_VALUE }, function() {
			try {
				var currentMarker = jQuery('#' + wrapperId).data('auto-save-periodic');
	    		if(jQuery('#' + wrapperId).length > 0 &&
	    				(typeof periodic.dateNowMarker === "undefined" || currentMarker == periodic.dateNowMarker)) {
	    			
	    			var data = new Object();
	    			data['dispatchuri'] = settings.dispId;
	    			data['dispatchevent'] = '2';
	    			data['cid'] = 'tmpResponse';
	    			data['tmpResponse'] =  'qtiworks_presented_' + settings.responseUniqueId;
	    			data['qtiworks_presented_' + settings.responseUniqueId] = '1';
	    			data['qtiworks_response_' + settings.responseUniqueId] = jQuery('#oo_' + settings.responseUniqueId).val();
	    			data['no-response'] = 'oo-no-response';
	    			data['_csrf'] = settings.csrf;

	    			var targetUrl = jQuery('#' + settings.formName).attr("action");
	    			jQuery.ajax(targetUrl,{
	    				type:'POST',
	    				data: data,
	    				cache: false,
	    				dataType: 'json',
	    				success: function(responseData, textStatus, jqXHR) {
	    					var now = new Date();
	    					var hours = now.getHours();
	    					var minutes = now.getMinutes()
	    	    			var lastSaved = hours + ":" + (minutes < 10 ? "0" : "") + minutes;
	    	    			var containerEl = jQuery('#' + wrapperId).parent().get(0);
	    	    			jQuery('.o_qti_essay_last_save', containerEl).css('display','block');
	    	    			jQuery('.o_qti_essay_last_save-time', containerEl).html(lastSaved);
	    				}
	    			})
	    		} else {
	    			periodic.cancel();
	    		}
			} catch(e) {
				if(window.console) console.log(e);
			}
		});
		
		var dateNow = Date.now();
		periodic.dateNowMarker = dateNow;
		jQuery('#' + wrapperId).data('auto-save-periodic', dateNow);
        return this;
    };
}( jQuery ));