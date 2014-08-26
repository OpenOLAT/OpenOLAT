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
    		durationshow: 5000,
    		durationout: 500,
    		durationin: 500,
    		easeout : 'ease',
    		easein : 'ease'
        }, params );
		this.pos = null;
    	
    	if (this.settings.query == null || this.settings.images.length == 0) return;    	
    	// start rotation process
    	this.rotate();
	}
	
	BgCarrousel.prototype.rotate = function() {
		setTimeout($.proxy(this._hideCurrent, this), this.settings.durationshow);
	}
	
	BgCarrousel.prototype._hideCurrent = function() {
		var bgElement = $(this.settings.query);
		if (bgElement && bgElement.size() > 0) {
			this.newImg = "";
			this.oldImg = "";
			if (this.pos == null) {
				// initial value
				this.pos = 1;
				this.oldImg = this.settings.images[0];
			} else {
				this.oldImg = this.settings.images[this.pos];
				this.pos++;
				if (this.settings.images.length == this.pos) {
					// restart with first one
					this.pos = 0;
				}					
			}
			this.newImg = this.settings.images[this.pos];
			
			bgElement.transition({
					opacity:0, 
					duration: this.settings.durationout, 
					easing: this.settings.easeout
				}, $.proxy(this._showNext, this)
			);
		}
	}	
	
	BgCarrousel.prototype._showNext = function() {
		var el = $(this.settings.query);
		var css = el.css('background-image');
		if (css.indexOf(this.oldImg) == -1) {
			// abort, don't know what to do, show image again and exit
			el.transition({ opacity:1, duration: 0 });	
			return;
		}
		var newCss = css.replace(this.oldImg, this.newImg);
		el.css('background-image', newCss);
		el.transition({
				opacity:1, 
				duration: this.settings.durationin, 
				easing: this.settings.easein
			}, $.proxy(this.rotate,this)
		);	
	}
	
}(jQuery);
