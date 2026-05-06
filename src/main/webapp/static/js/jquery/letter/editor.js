(function ($) {
    "use strict";
	$.fn.letterEditor = function(options) {
		var settings = $.extend({
			formName: null,//forn name
    		dispIdField: null,//form dispatch id
    		dispId: null,//item id
    		eventIdField: null, // form eventFieldId,
    		csrfToken: ''
		}, options );

		try {
			jQuery('*[data-sel-label]').on('click', function() {
				var jEl = jQuery(this);
				var selVariable = jEl.attr('data-sel-variable');
				var selType = jEl.attr('data-sel-type');
				var selLabel = jEl.attr('data-sel-label');
				var selDesc = jEl.attr('data-sel-desc');
				window.parent.o_ffXHREvent(settings.formName, settings.dispIdField, settings.dispId, settings.eventIdField, '2', false, false, true, '_csrf', settings.csrfToken,
					'cid', 'select-placeholder', 'data-sel-variable', selVariable, 'data-sel-type', selType, 'data-sel-label', selLabel, 'data-sel-desc', selDesc);
			});	
		} catch(e) {
			if(window.console) console.log(e);
		}
        return this;
    };
}( jQuery ));