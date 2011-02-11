var B_AjaxLogger = {
	isDebugEnabled : function() { return o_info.JSTracingLogDebugEnabled; },
	logDebug : function (logMsg, jsFile) {
		if (!this.isDebugEnabled()) return;
		new Ajax.Request(o_info.JSTracingUri, { method: 'post', parameters : {level: 'debug', logMsg : logMsg, jsFile : jsFile} });	
	}
};
