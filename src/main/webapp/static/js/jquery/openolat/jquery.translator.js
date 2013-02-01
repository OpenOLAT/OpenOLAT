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
	$.fn.ooTranslator = function() {
		var translator = null;
		$(document).each(function(){
			translator = $(document).data('_ooTranslator');
			if(translator == undefined) {
				translator = new JSTranslatorFactory();
				$(document).data('_ooTranslator', translator);
			}
		});
		return translator;
	}
	
	function JSTranslatorFactory() {
		return this;
	}
	
	JSTranslatorFactory.prototype = {
		mapperUrl : null, // base url where to get the localization files
		// translator cathe: translators[locale][bundleName] -> translator
		translators : null, // holds locales that contains translator for bundles

		// constructor. Don't create translators yourself, use the factory getTranslator() instead
		initialize : function(myMapperUrl) {
			this.mapperUrl = myMapperUrl;
			this.translators = new Object();
		},
		
		// Public method to get an instance of a translator
		getTranslator : function(locale, bundleName) {
			// Make sure cache datastructure exists
			if (this.translators[locale] == null) this.translators[locale] = new Object(); 
			// Load from server if not in cache
			if (this.translators[locale][bundleName] == null) {		
				// Use url that can be cached by browsers
				var url = this.mapperUrl + "/" + locale + "/" + bundleName + "/translations.js";
				// Get localization data is based on prototype ajax object
				// This ajax request will call the setTranslator method in the end
				jQuery.ajax(url, {
					async: false,
					dataType: 'json',
					success: function(transData, textStatus, jqXHR) {
						jQuery(document).ooTranslator()._createTranslator(transData, locale, bundleName);
					}
				});
			}
			return this.translators[locale][bundleName];
		},
		
		// private factory method called by the I18nManager after transmitting the localization file
		_createTranslator : function(localizationData, locale, bundleName) {
			this.translators[locale][bundleName] = new JSTranslator().initialize(localizationData, locale, bundleName);
		}
	}
	
	function JSTranslator() {
		return this;
	}
	
	JSTranslator.prototype = {
		localizationData : null, // key-value pairs
		bundle : null, 
		locale : null,
			
		// constructor. Don't create translators yourself, use the factory getTranslator() instead
		initialize : function(myLocalizationData, mylocale, mybundle) {
			this.bundle = mybundle;
			this.locale = mylocale;
			this.localizationData = myLocalizationData;
			return this;
		},
		
		// Method to translate a key in this bundle
		translate : function(key) {	
			if (this.localizationData[key]) {		
				return this.localizationData[key];
			} else {
				return this.bundle + ":" + key;		
			}		
		}
	}
})(jQuery);