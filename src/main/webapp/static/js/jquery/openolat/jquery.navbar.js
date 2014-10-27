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
			permanentToolW : 0,
			staticW : 0,
			staticOffCanvas : false,
			staticDirty : false,
			sitesW : 0,
			sitesCollapsed : false,
			sitesExtended: true,
			sitesDirty : false,
			tabsW : 0,
			tabsOffCanvas : false,
			tabsDirty : false,
			personalToolsW : 0,
			personalToolsOffCanvas : false,
			personalToolsDirty : false,
			offCanvasWidth : 0
		};
		// get site of menu from css
		var w = $('#o_offcanvas_right').css('width');
		//no width in full screen mode
		if(w) {
			this.state.offCanvasWidth = parseInt(w.replace(/[^\d.]/g,''));
		
			this.initListners();
			this.calculateWidth();
			this.optimize();
		}
	}
	
	Navbar.prototype.initListners = function() {
		// Optimize navbar when the browser changes size
		$(window).resize($.proxy(this.onResizeCallback,this));     

		// Mark nav components dirty when updated in DOM
		$(document).on("oo.nav.static.modified", $.proxy(function() {
			this.state.staticDirty = true;
			//console.log('sites dirty');
		},this));
		$(document).on("oo.nav.sites.modified", $.proxy(function() {
			this.state.sitesDirty = true;
			//console.log('sites dirty');
		},this));
		$(document).on("oo.nav.tabs.modified", $.proxy(function() {
			this.state.tabsDirty = true;
			//console.log('tabs dirty');
		},this));
		$(document).on("oo.nav.tools.modified", $.proxy(function() {
			this.state.personalToolsDirty = true;
			//console.log('tools dirty');
		},this));

		// Optimize when DOM replacement is finished (and marked dirty previously)
		$(document).on("oo.dom.replacement.after", $.proxy(this.onDOMreplacementCallback,this));		

		// Always close menu on mobile device when orientation changes
		$(window).on("orientationchange", $.proxy(this.hideRight,this));
				
		// Add application event listeners to trigger offcanvas menu
		$('#o_navbar_right-toggle').on('click', $.proxy(this.toggleRight,this));
		$('#o_offcanvas_right .o_offcanvas_close').on('click', $.proxy(this.hideRight,this));
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
		if (!this.state.busy && (this.state.staticDirty || this.state.sitesDirty || this.state.tabsDirty || this.state.personalToolsDirty)) {			
			this.state.busy = true;
			//offcanvas sites need to be cleared, otherwise they are duplicated
			$('.o_sites_offcanvas').empty();
			this.calculateWidth();
			this.optimize();		
			this.state.sitesDirty = false;
			this.state.tabsDirty = false;
			this.state.personalToolsDirty = false;	
			this.state.busy = false;
		}
	}
	
	Navbar.prototype.calculateWidth = function() {
		var el = $('#o_navbar_container .o_navbar-collapse');
	    // Get the dimensions of the viewport
	    this.state.navbarW = el.innerWidth();
	    // toggle and branding
	    this.state.toggleW = $('#o_navbar_right-toggle').outerWidth(true);
	    this.state.brandW = $('.o_navbar-brand').outerWidth(true);
	    // the permanent tools. only the personal tools are put to offsite
	    //don't include margin, because right margin on this element is negative
	    this.state.permanentToolW = $('#o_navbar_tools_permanent').outerWidth(false);
	    // the real content: sites, tabs and tools
	    if (!this.state.staticOffCanvas) {
	    	this.state.staticW = $('.o_navbar_static').outerWidth(true);
	    }
	    this.state.sitesW = $('.o_navbar_sites').outerWidth(true);
	    if (!this.state.tabsOffCanvas) {
	    	this.state.tabsW = $('.o_navbar_tabs').outerWidth(true);	    	
	    }
	    if (!this.state.personalToolsOffCanvas) {
		    //don't include margin, because left and right margins on this element are negative
	    	this.state.personalToolsW = $('#o_navbar_tools_personal').outerWidth(false);	    	
	    }
	    
//	    console.log('calculateWidth w:' + this.state.navbarW + ' s:'+this.state.sitesW + ' d:'+this.state.tabsW + ' t:'+this.state.personalToolsW + ' o:'+this.getOverflow() );
	}

	Navbar.prototype.getOverflow = function(e) {
		// Calculate if more space is used in navbar than available. Get total width and substract feature by feature
		var o = this.state.navbarW;
		if (!this.state.staticOffCanvas) {
			o -= this.state.staticW;
		}
		o -= this.state.sitesW;
		if (!this.state.tabsOffCanvas) {
			o -= this.state.tabsW;
		}
		if (!this.state.personalToolsOffCanvas) {
			o -= this.state.personalToolsW;
		}
		if (this.state.personalToolsOffCanvas || this.state.tabsOffCanvas || this.state.staticOffCanvas) {
			// subtract the space used for toggle only when toggle is actually used
			o -= this.state.toggleW;
		}
		// always subtract the space used by brand and the permanent tools
		o -= this.state.brandW;
		o -= this.state.permanentToolW;
		return -o;
	}
	    
	Navbar.prototype.optimize = function(e) {	    
		var o = this.getOverflow();
//		console.log('optimize o:' + o);
		// Move from toolbar to offcanvas
		//outerWidth can be off by 1px, so we need a small buffer on o
		while (o + 20 > 0 && (!this.state.personalToolsOffCanvas || !this.state.tabsOffCanvas || !this.state.sitesCollapsed || !this.state.staticOffCanvas)) {
			if (!this.state.personalToolsOffCanvas) {	
//				console.log('collapse tools ' + o);
				$('#o_navbar_tools_personal').prependTo('#o_offcanvas_right_container'); 
				this.state.personalToolsOffCanvas = true;
			}			
			else if (!this.state.tabsOffCanvas) {
//				console.log('collapse tabs ' + o);
				$('.o_navbar_tabs').prependTo('#o_offcanvas_right_container'); 
				this.state.tabsOffCanvas = true;
			}
			else if (!this.state.sitesCollapsed) {
//				console.log('collapse sites ' + o);
				this.collapseSites();
			}
			else if (!this.state.staticOffCanvas) {
//				console.log('collapse static ' + o);
				$('.o_navbar_static').prependTo('#o_offcanvas_right_container'); 
				this.state.staticOffCanvas = true;
			}

			this.calculateWidth();
			o = this.getOverflow();
		}
		// Move from offcanvas to toolbar
		while (o < 0 && (this.state.personalToolsOffCanvas || this.state.tabsOffCanvas || !this.state.sitesExtended || this.state.staticOffCanvas)) {
			if (this.state.staticOffCanvas) {
				if (-o < this.state.staticW) {
					break;
				}
				
//				console.log('uncollapse static ' + o);
				$('.o_navbar_static').appendTo('#o_navbar_container .o_navbar-collapse'); 
				this.state.staticOffCanvas = false;
			}
			else if (!this.state.sitesExtended) {
				if(triedToExtend){
					break;
				}
			
//				console.log('extend sites ' + o);
				this.extendSites();
				var triedToExtend = true;
			}
			else if (this.state.tabsOffCanvas) {
				if (-o < this.state.tabsW) {
					break;
				}
				
//				console.log('uncollapse tabs ' + o);
				$('.o_navbar_tabs').appendTo('#o_navbar_container .o_navbar-collapse'); 
				this.state.tabsOffCanvas = false;
			}
			else if (this.state.personalToolsOffCanvas) {
				if (-o < this.state.personalToolsW) {
					break;
				}

//				console.log('uncollapse tools ' + o);
				$('#o_navbar_tools_personal').prependTo('#o_navbar_container .o_navbar-collapse .o_navbar_tools'); 
				this.state.personalToolsOffCanvas = false;
			}

			this.calculateWidth();
			o = this.getOverflow();
		}
		
		if (this.state.personalToolsOffCanvas || this.state.tabsOffCanvas || !this.state.sitesExtended) {
			this.showToggle();
		} else {
			this.hideToggle();
			this.hideRight();
		}
		
	}

	/*
	 * Collapse sites into 'more' one by one
	 */
	Navbar.prototype.collapseSites = function(e) {
		var sites = $('.o_navbar_sites');
		var sitesOffcanvas = $('.o_sites_offcanvas');
		var more = $('.o_site_more');
		if(more.length == 0){
			/* as long as 'more' has no function, don't display it
			 */
			more = $('<li class=" o_site_more" style="display:none;"><a href=""><span>More</span></a></li>');
			more.appendTo(sites);
			more.bind('click', this.showMoreSites);
		}
		
		if(sitesOffcanvas.length == 0){
			sitesOffcanvas = $('<ul>', {
				'class': 'nav o_navbar-nav o_sites_offcanvas'
			});
			sitesOffcanvas.prependTo('#o_offcanvas_right_container');
		}

		var secondLast = sites.find('li:nth-last-child(2)');
		if(secondLast.length){
//			console.log('collapsing site '+secondLast.attr('class').match('o_site_[a-z]*'));
			secondLast.appendTo(sitesOffcanvas);
		}

		if(sites.children().length == 1){
			//only 'more' button left
			this.state.sitesCollapsed = true;
		}
		
		this.state.sitesExtended = false;
	}

	/*
	 * Extend sites by taking them out of 'more' one by one
	 */
	Navbar.prototype.extendSites = function(e) {
		var sites = $('.o_navbar_sites');
		var sitesOffcanvas = $('.o_sites_offcanvas');
		var more = $('.o_site_more');
		
		var site = sitesOffcanvas.find('li:last-child');
		if(site.length){
//			console.log('extending site '+site.attr('class').match('o_site_[a-z]*'));
			site.insertBefore(more);
			//if site doesn't fit move it back
			var o = this.getOverflow();
			if(-o < site.outerWidth(true)){
//				console.log('site doesn\'t fit');
				site.appendTo(sitesOffcanvas);
			}
		}

		if(sitesOffcanvas.children().length == 0){
			this.state.sitesExtended = true;
			sitesOffcanvas.remove();
			more.remove();
		}
		
		this.state.sitesCollapsed = false;
	}

	Navbar.prototype.showMoreSites = function(e) {
		
	}
	
	Navbar.prototype.showToggle = function() {
		if (!this.state.toggleVisible) {
			$('#o_navbar_right-toggle').show();	    	
			this.state.toggleVisible = true;			
		}
	}
	Navbar.prototype.hideToggle = function() {
		if (this.state.toggleVisible) {
			$('#o_navbar_right-toggle').hide();	    	
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
			// hide menu when clicking anywhere in content (timeout to pass the event in IE8/9 which hide the navbar)
			var listener = $.proxy(this.hideRight, this);
			setTimeout(function() {
				$('html').on('click', listener);
			}, 10);			
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
			// remove listener to hide menu
			$('html').off('click', $.proxy(this.hideRight,this));			
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
