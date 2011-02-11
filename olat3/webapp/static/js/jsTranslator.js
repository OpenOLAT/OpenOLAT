/** The JSTranslator allows the usage of the standard olatcore translation infrastrucuture 
 * from within Java Script. 
 * The translator does offer package translation with overlay and fallback to fallback 
 * language, however it does not offer fallback to the fallback packages. Use an explicit 
 * translator for the fallback packages if you need kees from there.
 * ==================
 * 30. September 2008
 * Florian Gnaegi, 
 * http://www.frentix.com
 **/

// needs prototype library
if(typeof Prototype == 'undefined') throw("jsTranslator.js requires including of prototype.js library!");

/**
 * JSTranslatorFactory provides a factory method to create a translator and to get instances
 * In your HTML code, only use the b_jsTranslatorFactory.getTranslator() method.
 * The factroy itself is automatically initialized in the body.html by the BaseChiefController.java
 */
var JSTranslatorFactory = Class.create();	
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
			new Ajax.Request(url, {asynchronous : false, encoding : 'UTF-8', method : 'get' });
			// new translator is now available
		}
		return this.translators[locale][bundleName];
	},
	
	// private factory method called by the I18nManager after transmitting the localization file
	_createTranslator : function(localizationData, locale, bundleName) {
		this.translators[locale][bundleName] = new JSTranslator(localizationData, locale, bundleName);
	}
}


/**
 * The translator object. Use the b_jsTranslatorFactory.getTranslator() method to get a 
 * translator instance, don't use the constructor below!
 */
var JSTranslator = Class.create();	
JSTranslator.prototype = {
	localizationData : null, // key-value pairs
	bundle : null, 
	locale : null,
		
	// constructor. Don't create translators yourself, use the factory getTranslator() instead
	initialize : function(myLocalizationData, mylocale, mybundle) {
		this.bundle = mybundle;
		this.locale = mylocale;
		this.localizationData = myLocalizationData;
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