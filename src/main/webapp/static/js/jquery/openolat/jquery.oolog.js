/******************************************
 * frentix.com
 *
 * Usefully javascript code for openolat
 *
 * @author          srosse
 * @copyright       Initial code contributed and copyrighted by frentix GmbH, http://www.frentix.com
 * @license         Licensed under the Apache License, Version 2.0 (the "License"); 
 * @link            http://www.openolat.org
 * @mercurial       http://hg.openolat.org
 * @version			1.0.0
 *
 ******************************************/

(function($)
{
	//ajax logger
	$.fn.ooLog = function(level, logMsg, jsFile) {
		var logger = null;
		$(this).each(function(){
			logger = $(this).data('_ooLog');
			if(logger == undefined) {
				logger = new B_AjaxLogger();
				$(this).data('_ooLog', logger);
			}
		});
		
		if(level == undefined) {
			return logger;
		} else if(typeof level === 'string') {
			if(logger) {
				logger.log(level, logMsg, jsFile);
			}
		};
	}
	
	function B_AjaxLogger() {
		return this;
	}

	B_AjaxLogger.prototype = {	
		isDebugEnabled : function() {
			return o_info.JSTracingLogDebugEnabled;
		},
		
		log: function (level, logMsg, jsFile) {
			if (!this.isDebugEnabled()) {
				return;
			}
			jQuery.post(o_info.JSTracingUri, {'level':level, 'logMsg': logMsg, 'jsFile': jsFile});	
		}
	}
})(jQuery);