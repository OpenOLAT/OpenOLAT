/**
 *  OpenOLAT theme JS extensions as jQuery module
 *  
 *  @dependencies jQuery
 */
+(function($) {

		var ThemeJS = function() {
			// nothing to do
		}
		/**
		 * Adds a link to the logo and the copyright footer
		 * 
		 * @method
		 */
		ThemeJS.prototype.addClientLinks = function(){
			var logoElement = $(".o_navbar-brand");
			if (logoElement && !logoElement.hasClass('o_clickable')) {
				// add marker css to remember this link is already ok, add link reference
				logoElement.addClass('o_clickable');					
				logoElement.prop('href', "http://www.openolat.org");
				logoElement.prop('target', "_blank");
				logoElement.prop('title', 'OpenOLAT - infinite learning');
			}
		},
		
		/**
		 * Method to install the theme add-ons. Will be re-executed after each DOM replacement. 
		 * 
		 * @method
		 */
		ThemeJS.prototype.execThemeJS = function() {
			OPOL.themejs.addClientLinks()
			// execute after each dom replacement (navbar might have been changed)
			$(document).on("oo.dom.replacement.after", OPOL.themejs.addClientLinks);
		}
		
		//Execute when loading of page has been finished
		$(document).ready(function() {
			OPOL.themejs = new ThemeJS();
			OPOL.themejs.execThemeJS();			
		});
		
})(jQuery);
