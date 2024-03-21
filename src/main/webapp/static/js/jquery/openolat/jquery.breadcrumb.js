/*
 * ========================================================
 *  <a href="https://www.openolat.org">
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
 *  26.08.2014 by frentix GmbH, https://www.frentix.com
 *  <p>
 *  This plugin can be used to apply a background image carrousel effect on
 *  a DOM element. The plugin will replace the images in the provided list one
 *  after another. 
 *  This plugin should be used from within a theme.js in your custom theme if
 *  required.
 *  
 *  @author uhensler, https://www.frentix.com
 *  @date March 2024
 * ========================================================
 */

+function ($) {
	'use strict';
	
	$.fn.oobreadcrumb = function() {
		new OOBreadcrumb(this);
	}
	
	var OOBreadcrumb = function(breadcrumbWrapper) {
		this.state = {
			busy: false,
			breadcrumbId: breadcrumbWrapper.attr('id')
		};
		this.elements = {};
		this.elements.breadcrumb = this.getBreadcrumb();
		this.elements.crumbs = this.getCrumbs();
		this.elements.moreList = this.getMoreList();
		this.elements.moreListItems = this.getMoreListItems();
		
		this.initListners();
		// First calculation when dom manipulations are done.
		// Important on reload of the page.
		requestAnimationFrame(() => {
			this.onResizeCallback();
		});
	};
	
	OOBreadcrumb.prototype.initListners = function() {
		$(window).resize($.proxy(this.onResizeCallback,this));
		$(window).on("orientationchange", $.proxy(this.onResizeCallback,this));
		
		// Add application event listeners to trigger offcanvas menu
		$('#o_navbar_right-toggle').on('click', $.proxy(this.onResizeCallback,this));
		$('#o_offcanvas_right .o_offcanvas_close').on('click', $.proxy(this.onResizeCallback,this));
	};
	
	OOBreadcrumb.prototype.onResizeCallback = function() {
		if (!this.state.busy) {
			this.state.busy = true;
			this.calculateMenu();
			this.state.busy = false;
		}
	};
	
	OOBreadcrumb.prototype.calculateMenu = function() {
		this.elements.moreList.removeClass('o_display_none');
		this.showMoreCrumbs(0);
	};
	
	OOBreadcrumb.prototype.showMoreCrumbs = function(numVisibleCrumbs) {
		if (numVisibleCrumbs === this.elements.crumbs.length) {
			// All crumbs are visible. More menu is hide.
			this.elements.moreList.addClass('o_display_none');
		} else {
			// Display one more crumb
			var visibleIndex = this.elements.crumbs.length - numVisibleCrumbs -1;
			this.elements.crumbs.each(function(index, value) {
				if (index >= visibleIndex) {
					$(this).removeClass('o_display_none');
				} else {
					$(this).addClass('o_display_none');
				}
			});
			
			if(this.isBreadcrumbFullyDisplayed()) {
				// If all crumbs still have enough space, another one is added ...
				this.showMoreCrumbs(numVisibleCrumbs + 1);
			} else {
				// ... (show at least the last crumb) ...
				if ((visibleIndex + 1) == this.elements.crumbs.length) {
					visibleIndex--;
				}
				// ... else the number of visible crumbs ...
				this.elements.crumbs.each(function(index, value) {
					if (index > visibleIndex) {
						$(this).removeClass('o_display_none');
					} else {
						$(this).addClass('o_display_none');
					}
				});
				// ... and visible menu entries are fixed.
				this.elements.moreListItems.each(function(index, value) {
					if (index > visibleIndex) {
						$(this).addClass('o_display_none');
					} else {
						$(this).removeClass('o_display_none');
					}
				});
			}
		}
	}
	
	OOBreadcrumb.prototype.isBreadcrumbFullyDisplayed = function() {
		return this.elements.breadcrumb[0].scrollWidth <= this.elements.breadcrumb[0].clientWidth;
	};
	
	OOBreadcrumb.prototype.getBreadcrumb = function() {
		return $('#' + this.state.breadcrumbId + ' > .breadcrumb');
	};
	
	OOBreadcrumb.prototype.getCrumbs = function() {
		return $('#' + this.state.breadcrumbId + ' .o_breadcrumb_crumb');
	};
	
	OOBreadcrumb.prototype.getMoreList = function() {
		return $('#' + this.state.breadcrumbId + "_more");
	};
	
	OOBreadcrumb.prototype.getMoreListItems = function() {
		return $('#' + this.state.breadcrumbId + "_more .o_breadcrumb_menu_item");
	};
	
}(jQuery);
