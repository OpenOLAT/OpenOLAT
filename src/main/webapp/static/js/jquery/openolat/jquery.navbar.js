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
		this.addExtraElements();

		this.state = {
			busy: false,
			brandW : 0,
			sitesW : 0,
			sitesDirty : false,
			sites: {
				collapsed: this.isSitesCollapsed(),
				extended: this.isSitesExtended
			},
			tabsW : 0,
			tabsDirty : false,
			tabs: {
				collapsed: this.isTabsCollapsed(),
				extended: this.isTabsExtended()
			},
			toolsW : 0,
			toolsDirty : false,
			tools: {
				collapsed: this.isToolsCollapsed(),
				extended: this.isToolsExtended()
			},
			offCanvasWidth : 0,
			moreW: 0
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
		$(document).on("oo.nav.sites.modified", $.proxy(function() {
			this.state.sitesDirty = true;
//			console.log('sites dirty');
		},this));
		$(document).on("oo.nav.tabs.modified", $.proxy(function() {
			this.state.tabsDirty = true;
//			console.log('tabs dirty');
		},this));
		$(document).on("oo.nav.tools.modified", $.proxy(function() {
			this.state.toolsDirty = true;
//			console.log('tools dirty');
		},this));

		// Optimize when DOM replacement is finished (and marked dirty previously)
		$(document).on("oo.dom.replacement.after", $.proxy(this.onDOMreplacementCallback,this));		

		// Always close menu on mobile device when orientation changes
		$(window).on("orientationchange", $.proxy(this.hideRight,this));
				
		// Add application event listeners to trigger offcanvas menu
		$('#o_navbar_right-toggle').on('click', $.proxy(this.toggleRight,this));
		$('#o_offcanvas_right .o_offcanvas_close').on('click', $.proxy(this.hideRight,this));
		
		// Add listeners for dropdown menu
		$('#o_navbar_more').on('shown.bs.dropdown', this.onDropdownShown);
		$('#o_navbar_more').on('hidden.bs.dropdown', this.onDropdownHidden);
	}
	
	Navbar.prototype.onResizeCallback = function() {
//		console.log('onResizeCallback with busy::' + this.state.busy);
		if (!this.state.busy) {			
			this.state.busy = true;
			this.calculateWidth();
			this.optimize();
			this.state.busy = false;
		}
	}
	Navbar.prototype.onPageWidthChangeCallback = function() {
//		console.log('onPageWidthChangeCallback with busy::' + this.state.busy);
		if (!this.state.busy) {			
			this.state.busy = true;
			this.cleanupMoreDropdown();
			this.calculateWidth();
			this.optimize();		
			this.state.sitesDirty = false;
			this.state.tabsDirty = false;
			this.state.toolsDirty = false;	
			this.state.busy = false;
		}
	}
	Navbar.prototype.onDOMreplacementCallback = function() {
//		console.log('onDOMreplacementCallback with busy::' + this.state.busy);
		if (!this.state.busy && (this.state.sitesDirty || this.state.tabsDirty || this.state.toolsDirty)) {			
			this.state.busy = true;
			this.cleanupMoreDropdown();
			this.calculateWidth();
			this.optimize();		
			this.state.sitesDirty = false;
			this.state.tabsDirty = false;
			this.state.toolsDirty = false;	
			this.state.busy = false;
		}
	}
	
	Navbar.prototype.onDropdownShown = function(e) {
		var menu = $('#o_navbar_more .dropdown-menu');
		if(menu.length){
			var o = menu.offset().left;
			if(o < 0){
				menu.removeClass('dropdown-menu-right');
			}
		}
	}
	
	Navbar.prototype.onDropdownHidden = function(e) {
		var menu = $('#o_navbar_more .dropdown-menu');
		menu.addClass('dropdown-menu-right');
	}
	
	Navbar.prototype.calculateWidth = function() {
		var el = $('#o_navbar_container .o_navbar-collapse');
	    // Get the dimensions of the viewport
	    this.state.navbarW = el.innerWidth();
	    // toggle and branding
	    this.state.brandW = $('.o_navbar-brand').outerWidth(true);
	    // the real content: sites, tabs and tools
	    this.state.sitesW = this.getSites().outerWidth(true);
    	this.state.tabsW = this.getTabs().outerWidth(true);	    	
	    //don't include margin, because left and right margins on this element are negative
    	this.state.toolsW = this.getTools().outerWidth(false);	  
    	var moreEl = $('#o_navbar_more:visible');
    	this.state.moreW = (moreEl.length > 0 ? moreEl.outerWidth(true) : 0);    		
	    
//	    console.log('calculateWidth w:' + this.state.navbarW + ' s:'+this.state.sitesW + ' d:'+this.state.tabsW + ' t:'+this.state.toolsW + ' m:' + this.state.moreW + ' o:'+this.getOverflow() );
	}

	Navbar.prototype.getOverflow = function(e) {
		// Calculate if more space is used in navbar than available. Get total width and substract feature by feature
		var o = this.state.navbarW;
		o -= this.state.sitesW;
		o -= this.state.tabsW;
		o -= this.state.toolsW;
		o -= this.state.brandW;
		//take width of 'more' button into account
		o -= this.state.moreW;
		//element widths can sometimes be off by 1px, so we need a small buffer on o
		o -= 25;
		
		return -o;
	}
	    
	Navbar.prototype.optimize = function(e) {
		var o = this.getOverflow();
		var sites = this.getSites();
		var tabs = this.getTabs();
		var tools = this.getTools();
		var moreDropdown = this.getMoreDropdown();
		var offcanvasRight = this.getOffcanvasRight();
		
		this.updateState();
		
//		console.log('optimize o:' + o);
		// Move from toolbar to offcanvas
		while (o > 0 && (!this.state.tabs.collapsed || !this.state.sites.collapsed || !this.state.tools.collapsed)) {
			if (!this.state.tabs.collapsed) {
//				console.log('collapse tabs ' + o);
				this.collapse(tabs, moreDropdown, 'li', 'o_dropdown_tab');
			}
			else if (!this.state.sites.collapsed) {
//				console.log('collapse sites ' + o);
				this.collapse(sites, moreDropdown, 'li', 'o_dropdown_site');
			}
			else if (!this.state.tools.collapsed) {	
//				console.log('collapse tools ' + o);
				this.collapse(tools, offcanvasRight, '.o_navbar_tool:not(#o_navbar_imclient, #o_navbar_search_opener, #o_navbar_my_menu)', 'o_tool_right');
			}

			this.calculateWidth();
			o = this.getOverflow();
			this.updateState();
		}
		// Move from offcanvas to toolbar
		while (o < 0 && (!this.state.tabs.extended || !this.state.sites.extended || !this.state.tools.extended)) {
			if (!this.state.tools.extended) {
//				console.log('uncollapse static ' + o);
				var toolFit = this.extend(offcanvasRight, tools.children('#o_navbar_imclient, #o_navbar_search_opener, #o_navbar_my_menu').first(), '.o_tool_right', 'o_tool_right', true);
				if(!toolFit){ break; }
			}
			if (!this.state.sites.extended) {
//				console.log('extend sites ' + o);
				var siteFit = this.extend(moreDropdown, sites, 'li', 'o_dropdown_site');
				if(!siteFit){ break; }
			}
			else if (!this.state.tabs.extended) {
//				console.log('extend tabs ' + o);
				var tabFit = this.extend(moreDropdown, tabs, 'li', 'o_dropdown_tab');
				if(!tabFit){ break; }
			}

			this.calculateWidth();
			o = this.getOverflow();
			this.updateState();
		}
		
		if(this.state.sites.extended && this.state.tabs.extended) {
			var more = $('#o_navbar_more');
			more.css('display', 'none');
		}
		
		this.checkToolsOrder();
	}
	
	Navbar.prototype.updateState = function() {
		this.state.sites.collapsed = this.isSitesCollapsed();
		this.state.sites.extended = this.isSitesExtended();
		this.state.tabs.collapsed = this.isTabsCollapsed();
		this.state.tabs.extended = this.isTabsExtended();
		this.state.tools.collapsed = this.isToolsCollapsed();
		this.state.tools.extended = this.isToolsExtended();
	}
	
	Navbar.prototype.collapse = function(source, dest, selector, addClass) {
		var item = source.find(selector)
		if(item.length){ item = item.last(); }
		if(item.length){
//			console.log('collapsing item '+item.attr('class'));
			addClass && item.addClass(addClass);
			if(dest) {
				item.prependTo(dest);
			}
		}

		this.updateDropdownToggle(dest);
	}

	Navbar.prototype.extend = function(source, dest, selector, removeClass, before) {
		var item = source.find(selector);
		if(item.length){ item = item.first(); }
		var itemFit = false;
		if(item.length){
			if(dest && dest.length){
				if(before){
					dest.before(item);
				} else {
					item.appendTo(dest);
				}
				//if item doesn't fit, revert the change
				this.updateDropdownToggle(source);
				this.calculateWidth();
				var o = this.getOverflow();
				if(o > 0){
					item.prependTo(source);
				} else {
//					console.log('extending item '+item.attr('class'));
					removeClass && item.removeClass(removeClass);
					itemFit = true;
				}
			}
		}
		
		this.updateDropdownToggle(source);
		
		return itemFit;
	}
	
	/*
	 * Find a dropdown toggle button as the element's parent.
	 * If the element has children, display the toggle, otherwise hide it.
	 */
	Navbar.prototype.updateDropdownToggle = function(element) {
		var dropdownToggle = element.parents('.o_dropdown_toggle');
		if(!dropdownToggle.length) {
//			console.log('updateDropdownToggle not found')
			return;
		}
		
		if(element.children().length){
			dropdownToggle.css('display', 'block');
//			console.log('updateDropdownToggle to block')
		} else {
			dropdownToggle.css('display', 'none');
//			console.log('updateDropdownToggle to invisible')
		}
	}

	/* 
	 * Add extra dom elements, like the 'more' button.
	 */
	Navbar.prototype.addExtraElements = function() {
		//create 'more' button for sites and tabs
		var collapse = $('#o_navbar_container .o_navbar-collapse');
		var more = $('#o_navbar_more');
		if(more.length == 0){
			more = $('<ul id="o_navbar_more" class="nav o_navbar-nav o_dropdown_toggle"><li>'
						+ '<a class="dropdown-toggle" data-toggle="dropdown" href="#">'+o_info.i18n_topnav_more+' <b class="caret"></b></a>'
						+ '<ul class="dropdown-menu dropdown-menu-right"></ul>'
					+ '</li></ul>');
			more.appendTo(collapse);
		}
		
		//append divider to sites, which will be visible in the dropdown menu
		this.getSites().append('<li class="divider o_dropdown_site"></li>');
		
		//add o_icon-fw to help and print icons, so they align when moved to the off-canvas menu
		$('#o_navbar_help .o_icon, #o_navbar_print .o_icon').addClass('o_icon-fw');
	}
	
	/*
	 * Dropdown needs to be cleared after dom refresh, otherwise items are duplicated
	 */
	Navbar.prototype.cleanupMoreDropdown = function() {
		//restore state of non-dirty elements
		if(!this.state.sitesDirty){
			//move sites back to navbar, so they can be correctly collapsed again
			var sites = this.getSites();
			var _sites = this.getMoreDropdown().children('.o_dropdown_site');
			_sites.appendTo(sites);
		} else {
			this.getSites().append('<li class="divider o_dropdown_site"></li>');
		}
		if(!this.state.tabsDirty){
			//move tabs back to navbar, so they can be correctly collapsed again
			var tabs = this.getTabs();
			var _tabs = this.getMoreDropdown().children('.o_dropdown_tab');
			_tabs.prependTo(tabs);
		}
		//clear the rest (all dirty elements)
		this.getMoreDropdown().empty();
	}
	
	Navbar.prototype.checkToolsOrder = function() {
		var tools = this.getTools();
		var help = tools.find('#o_navbar_help');
		var print = tools.find('#o_navbar_print');
		var im = tools.find('#o_navbar_imclient');
		if(im && print){
			im.after(print);
		}
		if(im && help){
			im.after(help);
		}
	}
	
	Navbar.prototype.showRight = function() {
		if (!this.isOffcanvasVisible() && !this.offcanvasTransitioning) {
			this.offcanvasTransitioning = true;
			var that = this;
			var box = $('#o_offcanvas_right');
			box.show().transition({ x: -that.state.offCanvasWidth}, function() {
				$('body').addClass('o_offcanvas_right_visible');	 
				// a11y: set focus on offcanvas for screenreader
				box.find('.o_offcanvas_close').focus();				
				// hide menu when clicking anywhere in content (timeout to pass the event in IE8/9 which hide the navbar)
				var listener = $.proxy(that.hideRightOnClick, that);
				setTimeout(function() {
					$('html').on('click', listener);
					that.offcanvasTransitioning = false;
				}, 10);			
			} );
		}
	}
	Navbar.prototype.hideRightOnClick = function(e) {
		if("INPUT" != e.target.nodeName) {
			this.hideRight();
		}
	}
	Navbar.prototype.hideRight = function() {
		if (this.isOffcanvasVisible() && !this.offcanvasTransitioning) {
			this.offcanvasTransitioning = true;
			// remove listener to hide menu
			$('html').off('click', $.proxy(this.hideRight,this));
			var that = this;
			var box = $('#o_offcanvas_right');
			box.transition({ x: that.state.offCanvasWidth}, function() {
				box.hide();
				$('body').removeClass('o_offcanvas_right_visible');
				that.offcanvasTransitioning = false;
			} );
		}
	}
	Navbar.prototype.toggleRight = function() {
		if (this.isOffcanvasVisible()) {
			this.hideRight();
		} else {
			this.showRight();
		}
	}

	Navbar.prototype.isOffcanvasVisible = function() {
		return $('#o_offcanvas_right:visible').length;
	}

	Navbar.prototype.getSites = function() {
		return $('#o_navbar_container .o_navbar_sites');
	}
	
	Navbar.prototype.getTabs = function() {
		return $('#o_navbar_container .o_navbar_tabs');
	}
	
	Navbar.prototype.getTools = function() {
		return $('#o_navbar_container #o_navbar_tools_permanent');
	}
	
	Navbar.prototype.getMoreDropdown = function() {
		return $('#o_navbar_more .dropdown-menu');
	}
	
	Navbar.prototype.getOffcanvasRight = function() {
		return $('#o_offcanvas_right_container .o_navbar-right');
	}
	
	Navbar.prototype.isSitesCollapsed = function() {
		return !this.getSites().children('li').not('.divider').length;
	}
	
	Navbar.prototype.isSitesExtended = function() {
		return !this.getMoreDropdown().children('.o_dropdown_site').not('.divider').length;
	}
	
	Navbar.prototype.isTabsCollapsed = function() {
		return !this.getTabs().children('li').length;
	}
	
	Navbar.prototype.isTabsExtended = function() {
		return !this.getMoreDropdown().children('.o_dropdown_tab').length;
	}
	
	Navbar.prototype.isToolsCollapsed = function() {
		return !this.getTools().children('.o_navbar_tool').not('#o_navbar_imclient, #o_navbar_search_opener, #o_navbar_my_menu').length;
	}
	
	Navbar.prototype.isToolsExtended = function() {
		return !this.getOffcanvasRight().children('.o_tool_right').length;
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
