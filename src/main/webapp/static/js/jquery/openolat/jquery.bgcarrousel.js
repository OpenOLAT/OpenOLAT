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
    		shuffle: false,
    		shuffleFirst: false,
    		durationshow: 5000,
    		durationout: 500,
    		durationin: 500,
    		easeout : 'ease',
    		easein : 'ease'
        }, params );
		this.pos = null;
		
		// Query not defined? - stop right there
    	if (this.settings.query == null || this.settings.images.length == 0) return;    	
    	// Keep reference to initial image to remember even when shuffled
    	this.initialImage = this.settings.images[0];
    	// Shuffle image array
    	if (this.settings.shuffle) {
    		var o = this.settings.images;
    		for(var j, x, i = o.length; i; j = parseInt(Math.random() * i), x = o[--i], o[i] = o[j], o[j] = x);
    	}
    	// Replace the start image without animation right away when first image should also be shuffled
    	if (this.settings.shuffleFirst) {
    		this._replaceImage();    		
    	}    	
    	// Start rotation process
	    this.rotate();
	}
	
	BgCarrousel.prototype.rotate = function() {
		setTimeout($.proxy(this._hideCurrent, this), this.settings.durationshow);
	}
	
	BgCarrousel.prototype._hideCurrent = function() {
		// Stop animating if user enabled reduces motion settings
    	const mediaQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
    	if (!mediaQuery || !mediaQuery.matches) {
			var el = $(this.settings.query);
			if (el && el.length > 0) {	
				el.transition({
						opacity:0, 
						duration: this.settings.durationout, 
						easing: this.settings.easeout
					}, $.proxy(this._showNext, this)
				);
			}
	    }
	}	
	
	BgCarrousel.prototype._replaceImage = function(el) {
		if ( !el) {
			el = $(this.settings.query);			
		}
		if (el && el.length > 0) {
			this.newImg = "";
			this.oldImg = "";
			if (this.pos == null) {
				// initial value
				this.pos = 1;
				this.oldImg = this.initialImage;
			} else {
				this.oldImg = this.settings.images[this.pos];
				this.pos++;
				if (this.settings.images.length == this.pos) {
					// restart with first one
					this.pos = 0;
				}					
			}
			this.newImg = this.settings.images[this.pos];
			var css = el.css('background-image');
			if (css.indexOf(this.oldImg) == -1) {
				// abort, don't know what to do, show image again and exit
				el.transition({ opacity:1, duration: 0 });	
				return;
			}
			var newCss = css.replace(this.oldImg, this.newImg);
			el.css('background-image', newCss);	
		}
	}
	
	BgCarrousel.prototype._showNext = function() {
		var el = $(this.settings.query);
		this._replaceImage(el);
		el.transition({
				opacity:1, 
				duration: this.settings.durationin, 
				easing: this.settings.easein
			}, $.proxy(this.rotate,this)
		);	
	}
	
}(jQuery);
