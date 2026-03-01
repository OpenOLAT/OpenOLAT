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
				images: [
						'sunrise-surge.jpg',
						'deep-surge.jpg',
						'tahoe-landscape.jpg',
						'sonoma-mesh.jpg',
						'jaguar-starburst.jpg',
						'tiger-streak.jpg',						
						'violet-lime-teal.jpg',
						'amber-rose-teal.jpg',
						'sky-emerald-peach.jpg',
						'indigo-mint-sweep.jpg',
						'coral-lavender-teal.jpg',
						'arctic-sapphire.jpg',						
						'solar.jpg',
						'bop.jpg',
						'firelicked.jpg',
						'bright-flow.jpg'						
						],
				shuffle: true,			// true: shuffle image order on initialization
				shuffleFirst: false,		// true: shuffle also the first image (only relevant when shuffle=true)
    			durationshow: 5000,		// duration of the display of every image
	    		scale: 1.05,			// intensity of the zoom animation. Set to 0 for no zoom
    			scaleease : 'linear',	// style of the zoom animation
	    		durationout: 1500,		// duration of the fade-out animation. Set to 0 for no fade-out
    			easeout : 'linear' 		// style of the fade-out animation
			});
		}
		
		//Execute when loading of page has been finished
		$(document).ready(function() {
			OPOL.themejs = new ThemeJS();
			OPOL.themejs.execThemeJS();			
			OPOL.themejs.initDmzCarrousel();
		});
		
})(jQuery);

