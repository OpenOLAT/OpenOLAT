/*
 * ========================================================
 *  <a href="http://www.openolat.org">
 *  OpenOLAT - Online Learning and Training</a><br>
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); <br>
 *  you may not use this file except in compliance with the License.<br>
 *  You may obtain a copy of the License at the
 *  <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 *  <p>
 *  Unless required by applicable law or agreed to in writing,<br>
 *  software distributed under the License is distributed on an "AS IS" BASIS, <br>
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 *  See the License for the specific language governing permissions and <br>
 *  limitations under the License.
 *  <p>
 *  Initial code contributed and copyrighted by<br>
 *  26.08.2014 by frentix GmbH, http://www.frentix.com
 *  <p>
 *  This plugin can be used to apply a background image carrousel effect on
 *  a DOM element. The plugin will replace the images in the provided list one
 *  after another.
 *  This plugin should be used from within a theme.js in your custom theme if
 *  required.
 *
 *  @author gnaegi, www.frentix.com
 *  @date August 2014
 * ========================================================
 */

+function ($) {
	'use strict';

	$.fn.ooBgCarrousel = function() {
		return new BgCarrousel();
	}

	var BgCarrousel = function() {
		// nothing to do
	}

	BgCarrousel.prototype.initCarrousel = function(params) {
    	this.settings = $.extend({
    		query: null,			// mandatory
    		images: [], 			// mandatory
			shuffle: false,			// true: shuffle image order on initialization
			shuffleFirst: false,	// true: shuffle also the first image (only relevant when shuffle=true)
 
			durationshow: 5000,		// duration of the display of every image
    		scale: 1.025,			// intensity of the zoom animation. Set to 0 for no zoom
			scaleease : 'linear',	// style of the zoom animation
    		durationout: 1500,		// duration of the fade-out animation. Set to 0 for no fade-out
			easeout : 'linear' 		// style of the fade-out animation

        }, params );

		// Query not defined? - stop right there
    	if (this.settings.query == null || this.settings.images.length == 0) return;
    	
    	// Only one image - stop
    	if (this.settings.images.length <= 1) return;

    	// Keep reference to initial image URL to allow CSS string replacement later
    	this.initialImage = this.settings.images[0];

    	// Shuffle image array if requested
		this.pos = 0;
    	if (this.settings.shuffle) {
    		var o = this.settings.images;
    		for(var j, x, i = o.length; i; j = parseInt(Math.random() * i), x = o[--i], o[i] = o[j], o[j] = x);
    		if (!this.settings.shuffleFirst) {
    			// Keep the initial image as the starting point: find its new position
    			// in the shuffled array so the sequence continues without gaps or duplicates.
    			for (var k = 0; k < this.settings.images.length; k++) {
    				if (this.settings.images[k] === this.initialImage) {
    					this.pos = k;
    					break;
    				}
    			}
    		}
    	}

		var container = $(this.settings.query);
		// Get background styling from container and keep it for later CSS replacement
		this.bgcss = container.css("background");
		// remove the container background to remove flickering
		container.css('background', 'none');

		// Create two persistent layers. The active layer (z-index 0) sits on top
		// and plays the zoom→fade animation. The inactive layer (z-index -1) sits
		// underneath and is preloaded with the next image while the active layer
		// is still animating. After the fade the layers are swapped.
		var layerStyle = 'position: absolute; top: 0; left: 0; width: 100%; height: 100%;';
		this.layerA = $("<div style='" + layerStyle + " z-index: 0'></div>");
		this.layerB = $("<div style='" + layerStyle + " z-index: -1'></div>");
		container.append(this.layerB).append(this.layerA);

		this.activeLayer   = this.layerA;
		this.inactiveLayer = this.layerB;

		// Load first image on the active layer
		this.activeLayer.css('background', this.bgcss);
		// If shuffleFirst is true, override with the randomly picked first image
		if (this.settings.shuffle && this.settings.shuffleFirst) {
			this.activeLayer.css('background', this.bgcss.replace(this.initialImage, this.settings.images[0]));
		}

		// Preload the second image on the inactive layer
		this._preloadNext();

		// Kick off the animation sequence
		this._zoom();
	}


	// Load the next image (by pos) into the inactive layer and reset its state
	// so it is ready to become the active layer after the current fade.
	BgCarrousel.prototype._preloadNext = function() {
		this.pos++;
		if (this.pos >= this.settings.images.length) {
			this.pos = 0;
		}
		var nextBg = this.bgcss.replace(this.initialImage, this.settings.images[this.pos]);
		this.inactiveLayer.css('background', nextBg);
		// Reset scale and opacity instantly so the layer is clean when it becomes active
		this.inactiveLayer.transition({ scale: 1, opacity: 1, duration: 0 });
	}


	// Step 1: zoom the active layer. When done, trigger the fade.
	BgCarrousel.prototype._zoom = function() {
		var self = this;
		// Ensure the active layer starts from a clean state
		self.activeLayer.transition({ scale: 1, opacity: 1, duration: 0 });

		if (self.settings.durationshow > 100) {
			self.activeLayer.transition({
				scale: self.settings.scale,
				duration: self.settings.durationshow,
				easing: self.settings.scaleease
			}, function() {
				self._fade();
			});
		} else {
			self._fade();
		}
	}


	// Step 2: fade out the active layer. When done, switch to the next image.
	BgCarrousel.prototype._fade = function() {
		var self = this;

		if (self.settings.durationout > 100) {
			self.activeLayer.transition({
				opacity: 0,
				duration: self.settings.durationout,
				easing: self.settings.easeout
			}, function() {
				self._switchToNext();
			});
		} else {
			self.activeLayer.css('opacity', 0);
			self._switchToNext();
		}
	}


	// Step 3: swap layers, preload the image after next, start zooming the new active layer.
	BgCarrousel.prototype._switchToNext = function() {
		// Swap active and inactive references
		var temp          = this.activeLayer;
		this.activeLayer   = this.inactiveLayer;
		this.inactiveLayer = temp;

		// Promote the new active layer to the top
		this.activeLayer.css('z-index', 0);
		this.inactiveLayer.css('z-index', -1);

		// Preload the image after this one on the now-inactive layer
		this._preloadNext();

		// Start the zoom on the newly active layer
		this._zoom();
	}


}(jQuery);
