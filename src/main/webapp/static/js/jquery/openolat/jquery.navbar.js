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
		this.createMoreButton();
		
		var tabsExist = this.doTabsExist();
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
			tabsCollapsed : !tabsExist,
			tabsExtended : tabsExist,
			tabsDirty : false,
			personalToolsW : 0,
			personalToolsOffCanvas : false,
			personalToolsDirty : false,
			offCanvasWidth : 0,
			moreW: 0
		};
		
		//hide 'more' button before it is used
		this.hideMoreButton();
		
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
			this.cleanupMoreDropdown();
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
	    this.state.sitesW = $('#o_navbar_container .o_navbar_sites').outerWidth(true);
    	this.state.tabsW = $('#o_navbar_container .o_navbar_tabs').outerWidth(true);	    	
	    if (!this.state.personalToolsOffCanvas) {
		    //don't include margin, because left and right margins on this element are negative
	    	this.state.personalToolsW = $('#o_navbar_tools_personal').outerWidth(false);	    	
	    }
	    this.state.moreW = $('#o_navbar_more:visible').outerWidth(true);
	    
//	    console.log('calculateWidth w:' + this.state.navbarW + ' s:'+this.state.sitesW + ' d:'+this.state.tabsW + ' t:'+this.state.personalToolsW + ' o:'+this.getOverflow() );
	}

	Navbar.prototype.getOverflow = function(e) {
		// Calculate if more space is used in navbar than available. Get total width and substract feature by feature
		var o = this.state.navbarW;
		if (!this.state.staticOffCanvas) {
			o -= this.state.staticW;
		}
		o -= this.state.sitesW;
		o -= this.state.tabsW;
		if (!this.state.personalToolsOffCanvas) {
			o -= this.state.personalToolsW;
		}
		if (this.state.personalToolsOffCanvas || this.state.staticOffCanvas || !this.state.sitesExtended || !this.state.tabsExtended) {
			// subtract the space used for toggle only when toggle is actually used
			o -= this.state.toggleW;
		}
		// always subtract the space used by brand and the permanent tools
		o -= this.state.brandW;
		o -= this.state.permanentToolW;
		
		//take width of 'more' button into account
		o -= this.state.moreW;

		//element widths can sometimes be off by 1px, so we need a small buffer on o
		o -= 15;
		
		return -o;
	}
	    
	Navbar.prototype.optimize = function(e) {
		var o = this.getOverflow();
		//check state of tabs
		var tabsExist = this.doTabsExist();
		this.state.tabsCollapsed = !tabsExist;
		this.state.tabsExtended = tabsExist;
//		console.log('optimize o:' + o);
		// Move from toolbar to offcanvas
		while (o > 0 && (!this.state.personalToolsOffCanvas || !this.state.tabsCollapsed || !this.state.sitesCollapsed || !this.state.staticOffCanvas)) {
			if (!this.state.personalToolsOffCanvas) {	
//				console.log('collapse tools ' + o);
				$('#o_navbar_tools_personal').prependTo('#o_offcanvas_right_container'); 
				this.state.personalToolsOffCanvas = true;
			}
			else if (!this.state.tabsCollapsed) {
//				console.log('collapse tabs ' + o);
				this.collapseTabs();
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
		while (o < 0 && (this.state.personalToolsOffCanvas || !this.state.tabsExtended || !this.state.sitesExtended || this.state.staticOffCanvas)) {
			if (this.state.staticOffCanvas) {
				if (-o < this.state.staticW) {
					break;
				}
				
//				console.log('uncollapse static ' + o);
				$('.o_navbar_static').appendTo('#o_navbar_container .o_navbar-collapse'); 
				this.state.staticOffCanvas = false;
			}
			else if (!this.state.sitesExtended) {
//				console.log('extend sites ' + o);
				var siteFit = this.extendSites();
				if(!siteFit){
					break;
				}
			}
			else if (!this.state.tabsExtended) {
//				console.log('extend tabs ' + o);
				var tabFit = this.extendTabs();
				if(!tabFit) {
					break;
				}
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
		
		if (this.state.personalToolsOffCanvas || !this.state.tabsExtended || !this.state.sitesExtended) {
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
		var sites = $('#o_navbar_container .o_navbar_sites');
		var morePulldown = $('#o_navbar_more_dropdown');

		var site = sites.find('li:last-child');
		if(site.length){
//			console.log('collapsing site '+site.attr('class').match('o_site_[a-z]*'));
			site.prependTo(morePulldown);
		}

		if(sites.children().length == 0){
			this.state.sitesCollapsed = true;
		}
		
		this.state.sitesExtended = false;
		this.showMoreButton();
	}

	/*
	 * Extend sites by taking them out of 'more' one by one
	 */
	Navbar.prototype.extendSites = function(e) {
		var sites = $('#o_navbar_container .o_navbar_sites');
		var morePulldown = $('#o_navbar_more_dropdown');
		var siteFit = true;
		
		var site = morePulldown.find('li:first-child');
		if(site.length){
			site.appendTo(sites);
			//if site doesn't fit move it back
			var o = this.getOverflow();
			if(-o < site.outerWidth(true)){
				site.prependTo(morePulldown);
				siteFit = false;
			} else {
//				console.log('extending site '+site.attr('class').match('o_site_[a-z]*'));
			}
		}

		if(morePulldown.children().length == 0){
			this.state.sitesExtended = true;
			this.hideMoreButton();
		}
		
		this.state.sitesCollapsed = false;
		
		return siteFit;
	}

	/*
	 * Collapse tabs one by one
	 */
	Navbar.prototype.collapseTabs = function(e) {
		var tabs = $('#o_navbar_container .o_navbar_tabs');
		var morePulldown = $('#o_navbar_more_dropdown');

		var tab = tabs.find('li:first-child');
		if(tab.length){
//			console.log('collapsing tab '+tab.children(0).attr('title'));
			tab.addClass('o_tab_dropdown');
			tab.prependTo(morePulldown);
		}

		if(tabs.children().length == 0){
			this.state.tabsCollapsed = true;
		}
		
		this.state.tabsExtended = false;
		this.showMoreButton();
	}

	/*
	 * Extend tabs one by one
	 */
	Navbar.prototype.extendTabs = function(e) {
		var tabs = $('#o_navbar_container .o_navbar_tabs');
		var morePulldown = $('#o_navbar_more_dropdown');
		var tabFit = true;
		
		var tab = morePulldown.find('li:last-child');
		if(tab.length){
			tab.appendTo(tabs);
			//if tab doesn't fit move it back
			var o = this.getOverflow();
			if(-o < tab.outerWidth(true)){
				tab.prependTo(morePulldown);
				tabFit = false;
			} else {
//				console.log('extending tab '+tab.children(0).attr('title'));
				tab.removeClass('o_tab_dropdown');
			}
		}

		if(morePulldown.children().length == 0){
			this.state.tabsExtended = true;
			this.hideMoreButton();
		}
		
		this.state.tabsCollapsed = false;
		
		return tabFit;
	}
	
	/* 
	 * Get or create the 'more' navbar element.
	 * A button to display collapsed elements.
	 */
	Navbar.prototype.createMoreButton = function() {
		var collapse = $('#o_navbar_container .o_navbar-collapse');
		var more = $('#o_navbar_more');
		if(more.length == 0){
			more = $('<ul id="o_navbar_more" class="nav o_navbar-nav"><li>'
						+ '<a class="dropdown-toggle" data-toggle="dropdown" href="#"">More <b class="caret"></b></a>'
						+ '<ul id="o_navbar_more_dropdown" class="dropdown-menu dropdown-menu-right"></ul>'
					+ '</li></ul>');
			more.appendTo(collapse);
		}
	}

	Navbar.prototype.showMoreButton = function() {
		var more = $('#o_navbar_more');
		more.css('display', 'block');
	}

	Navbar.prototype.hideMoreButton = function() {
		var more = $('#o_navbar_more');
		more.css('display', 'none');
	}
	
	/*
	 * Dropdown needs to be cleared after dom refresh, otherwise items are duplicated
	 */
	Navbar.prototype.cleanupMoreDropdown = function() {
		//restore state of non-dirty elements
		if(!this.state.sitesDirty){
			//move sites back to navbar, so they can be correctly collapsed again
			var sites = $('#o_navbar_container .o_navbar_sites');
			var _sites = $('#o_navbar_more_dropdown').children().not('.o_tab_dropdown');
			_sites.appendTo(sites);
		}
		if(!this.state.tabsDirty){
			//move tabs back to navbar, so they can be correctly collapsed again
			var tabs = $('#o_navbar_container .o_navbar_tabs');
			var _tabs = $('#o_navbar_more_dropdown').children('.o_tab_dropdown');
			_tabs.prependTo(tabs);
		}
		//clear the rest (all dirty elements)
		$('#o_navbar_more_dropdown').empty();
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
	
	Navbar.prototype.doTabsExist = function() {
		return !!$('#o_navbar_container .o_navbar_tabs li:last-child').length;
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
