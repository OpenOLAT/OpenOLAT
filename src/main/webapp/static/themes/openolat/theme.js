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
			if (logoElement && logoElement.length > 0 && !logoElement.hasClass('o_clickable')) {
				// add marker css to remember this link is already ok, add link reference
				logoElement.addClass('o_clickable');					
				logoElement.prop('href', "https://www.openolat.org");
				logoElement.prop('target', "_blank");
				logoElement.prop('title', 'OpenOlat - infinite learning');
			}
		},
		
		/**
		 * Method to install the theme add-ons. Uncomment the single line in the method to override the logo by a custom theme.
		 * 
		 */
		ThemeJS.prototype.execThemeJS = function() {
			//OPOL.themejs.addClientLinks()
		}
		
		/**
		 * Use the carrousel effect for background images on the login screen based 
		 * on the ooBgCarrousel OpenOLAT jQuery plugin
		 */
		ThemeJS.prototype.initDmzCarrousel = function() {
			this.dmzCarrousel = jQuery().ooBgCarrousel();
			this.dmzCarrousel.initCarrousel({
				query: "#o_body.o_dmz #o_bg", 
				images: ['infinite.jpg', 'holger.jpg', 'marco.jpg', 'openolat_award.jpg', 'weg.jpg', 'christian.jpg'], 
				shuffle: true,
				shuffleFirst: false,
				durationshow: 5000,
				durationout: 500,
				durationin: 500,
				easeout : 'ease',
				easein : 'ease'
			});
		}
		
		//Execute when loading of page has been finished
		$(document).ready(function() {
			OPOL.themejs = new ThemeJS();
			OPOL.themejs.execThemeJS();			
			OPOL.themejs.initDmzCarrousel();
		});
		
})(jQuery);
