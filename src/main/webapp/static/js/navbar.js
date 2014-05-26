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
 *  28.04.2014 by frentix GmbH, http://www.frentix.com
 *  <p>
 *  @author gnaegi, www.frentix.com
 *  @date April. 2014
 * ========================================================
 */

+function ($) {
	'use strict';
	var Navbar = function() {
		this.state = {
			rightVisible: false,
			toggleVisible: false,
			busy: false,
			brandW : 0,
			toggleW : 0,
			sitesW : 0,
			sitesOffCanvas : false,
			sitesDirty : false,
			tabsW : 0,
			tabsOffCanvas : false,
			tabsDirty : false,
			toolsW : 0,
			toolsOffCanvas : false,
			toolsDirty : false,
			offCanvasWidth : 0
		};
		// get site of menu from css
		this.state.offCanvasWidth = parseInt($('#o_offcanvas_right').css('width').replace(/[^\d.]/g,''));
		
		this.initListners();
		this.calculateWidth();
		this.optimize();
	}
	
	Navbar.prototype.initListners = function() {
		// Optimize navbar when the browser changes size
		$(window).resize($.proxy(this.onResizeCallback,this));     

		// Mark nav components dirty when updated in DOM
		$(document).on("oo.nav.sites.modified", $.proxy(function() {
			this.state.sitesDirty = true;
			//console.log('sites dirty');
		},this));
		$(document).on("oo.nav.tabs.modified", $.proxy(function() {
			this.state.tabsDirty = true;
			//console.log('tabs dirty');
		},this));
		$(document).on("oo.nav.tools.modified", $.proxy(function() {
			this.state.toolsDirty = true;
			//console.log('tools dirty');
		},this));

		// Optimize when DOM replacement is finished (and marked dirty previously)
		$(document).on("oo.dom.replacement.after", $.proxy(this.onDOMreplacementCallback,this));		

		// Always close menu on mobile device when orientation changes
		$(window).on("orientationchange", $.proxy(this.hideRight,this));
				
		// Add application event listeners to trigger offcanvas menu
		$('.o_navbar-toggle').on('click', $.proxy(this.toggleRight,this));
		$('.o_offcanvas_close').on('click', $.proxy(this.hideRight,this));
//		$('#o_offcanvas_right').on('click', $.proxy(this.hideRight,this));
	}
	
	Navbar.prototype.onResizeCallback = function() {
		if (!this.state.busy) {			
			this.state.busy = true;
			this.calculateWidth();
			this.optimize();
			this.state.busy = false;
		}
	}
	Navbar.prototype.onDOMreplacementCallback = function() {
		if (!this.state.busy && (this.state.sitesDirty || this.state.tabsDirty || this.state.toolsDirty)) {			
			this.state.busy = true;
			this.calculateWidth();
			this.optimize();		
			this.state.sitesDirty = false;
			this.state.tabsDirty = false;
			this.state.toolsDirty = false;	
			this.state.busy = false;
			// close offcanvas when clicking a link
			this.hideRight();
		}
	}
	
	Navbar.prototype.calculateWidth = function() {
		var el = $('#o_navbar_container .o_navbar-collapse');
	    // Get the dimensions of the viewport
	    this.state.navbarW = el.innerWidth();
	    // toggle and branding
	    this.state.toggleW = $('.o_navbar-toggle').outerWidth(true);
	    this.state.brandW = $('.o_navbar-brand').outerWidth(true);
	    // the real content: sites, tabs and tools
	    if (!this.state.sitesOffCanvas) {
	    	this.state.sitesW = $('.o_navbar_sites').outerWidth(true);
	    }
	    if (!this.state.tabsOffCanvas) {
	    	this.state.tabsW = $('.o_navbar_tabs').outerWidth(true);	    	
	    }
	    if (!this.state.toolsOffCanvas) {
	    	this.state.toolsW = $('#o_navbar_tools').outerWidth(true) + 15;	    	
	    }
	    
//	    console.log('calculateWidth w:' + this.state.navbarW + ' s:'+this.state.sitesW + ' d:'+this.state.tabsW + ' t:'+this.state.toolsW + ' o:'+this.getOverflow() );
	}

	Navbar.prototype.getOverflow = function(e) {
		var o = this.state.navbarW;
		if (!this.state.sitesOffCanvas) {
			o -= this.state.sitesW;
		}
		if (!this.state.tabsOffCanvas) {
			o -= this.state.tabsW;
		}
		if (!this.state.toolsOffCanvas) {
			o -= this.state.toolsW;
		}
		if (this.state.toolsOffCanvas || this.state.tabsOffCanvas || this.state.sitesOffCanvas) {
			o -= this.state.toggleW;
		}
		o -= this.state.brandW;
		return -o;
	}
	    
	Navbar.prototype.optimize = function(e) {	    
		var o = this.getOverflow();
//		console.log('optimize o:' + o);
		// Move from toolbar to offcanvas
		while (o > 0 && (!this.state.toolsOffCanvas || !this.state.tabsOffCanvas || !this.state.sitesOffCanvas)) {
			if (!this.state.toolsOffCanvas) {	
//				console.log('collapse tools ' + o);
				$('.o_navbar_tools').prependTo('#o_offcanvas_container'); 
				this.state.toolsOffCanvas = true;
				o = this.getOverflow();	
				continue;
			}			
			if (!this.state.tabsOffCanvas) {
//				console.log('collapse tabs ' + o);
				$('.o_navbar_tabs').prependTo('#o_offcanvas_container'); 
				this.state.tabsOffCanvas = true;
				o = this.getOverflow();				
				continue;
			}
			if (!this.state.sitesOffCanvas) {
//				console.log('collapse sites ' + o);
				$('.o_navbar_sites').prependTo('#o_offcanvas_container'); 
				this.state.sitesOffCanvas = true;
				o = this.getOverflow();				
				continue;
			}
			
			break;
		}
		// Move from offcanvas to toolbar
		while (o < 0 && (this.state.toolsOffCanvas || this.state.tabsOffCanvas || this.state.sitesOffCanvas)) {
			if (this.state.sitesOffCanvas) {
				if (-o >= this.state.sitesW) {
//					console.log('uncollapse sites ' + o);
					$('.o_navbar_sites').appendTo('#o_navbar_container .o_navbar-collapse'); 
					this.state.sitesOffCanvas = false;
					o = this.getOverflow();				
					continue;
				} else {
					break;
				}
			}
			if (this.state.tabsOffCanvas) {
				if (-o >= this.state.tabsW) {
//					console.log('uncollapse tabs ' + o);
					$('.o_navbar_tabs').appendTo('#o_navbar_container .o_navbar-collapse'); 
					this.state.tabsOffCanvas = false;
					o = this.getOverflow();				
					continue;
				} else {
					break;
				}
			}
			if (this.state.toolsOffCanvas) {
				if (-o >= this.state.toolsW) {	
//					console.log('uncollapse tools ' + o);
					$('.o_navbar_tools').appendTo('#o_navbar_container .o_navbar-collapse'); 
					this.state.toolsOffCanvas = false;
					o = this.getOverflow();	
					continue;
				} else {
					break;
				}
			}
			break;
		}
		if (this.state.toolsOffCanvas || this.state.tabsOffCanvas || this.state.sitesOffCanvas) {
			this.showToggle();
		} else {
			this.hideToggle();
			this.hideRight();
		}
		
		
	}
	
	Navbar.prototype.showToggle = function() {
		if (!this.state.toggleVisible) {
			$('.o_navbar-toggle').show();	    	
			this.state.toggleVisible = true;			
		}
	}
	Navbar.prototype.hideToggle = function() {
		if (this.state.toggleVisible) {
			$('.o_navbar-toggle').hide();	    	
			this.state.toggleVisible = false;			
		}
	}
	
	Navbar.prototype.showRight = function() {
		if (!this.state.rightVisible) {					
			var that = this;
			var box = $('#o_offcanvas_right');
			box.show().transition({ x: -that.state.offCanvasWidth}, function() {
				$('body').addClass('o_offcanvas_right_visible');	    	
				that.state.rightVisible = true;			
			} );
			
		}
	}
	Navbar.prototype.hideRight = function() {
		if (this.state.rightVisible) {
			var that = this;
			var box = $('#o_offcanvas_right');
			box.transition({ x: that.state.offCanvasWidth}, function() {
				box.hide();
				$('body').removeClass('o_offcanvas_right_visible');	    	
				that.state.rightVisible = false;	    				
			} );

		}
	}
	Navbar.prototype.toggleRight = function() {
		if (this.state.rightVisible) {
			this.hideRight();
		} else {
			this.showRight();
		}
	}
	
	// Initialize navbar
	$(document).ready(function() {
		var nav = $('#o_navbar_wrapper');
		if (nav) {
			var navbar = new Navbar();
			window.OPOL.navbar = navbar;			
		}
	});
  
	
}(jQuery);
