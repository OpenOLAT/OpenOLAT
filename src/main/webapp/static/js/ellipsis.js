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
 *  This is a little plugin to hide text overflow using ellipsis. Optionally a
 *  'show more' 'hide details' link can be used as well. The HTML markup is simple:
 *  The container with the overflow must have the class "o_withEllipsis" and have a 
 *  fixed height. The more/less links must be in a wrapper div with class 
 *  "o_ellipsis_links" and have the class "o_morelink" and "o_lesslink". See that 
 *  catalog for an example.
 *  
 *  @author gnaegi, www.frentix.com
 *  @date Mai 2014
 * ========================================================
 */

+function ($, OPOL) {
	'use strict';

	var Ellipsis = function() {
		// nothing to do
	}
		
	Ellipsis.prototype.initEllipsisElement = function(el) {
		var elem = $(el);
		// initialize dotdotdot plugin on element
		elem.dotdotdot({
	    	callback : function(isTruncated){
	    		if (isTruncated) {
	    			// add marker class
	    			$(this).addClass('o_hasOverflow');
	    		} else {
	    			// remove marker class
	    			$(this).removeClass('o_hasOverflow');    			
	    		}
	    	},
	    	watch		: true, 				// listen to window resize 
	    	after: "div.o_ellipsis_links"		// add the more link when truncating
	    });	
	}	
	
	Ellipsis.prototype.showOverflow = function(elem) {
		var container = $(elem).parents('.o_withEllipsis');
		container.addClass('o_showOverflow');
		container.trigger("destroy.dot");
	}

	Ellipsis.prototype.hideOverflow = function(elem) {
		var container = $(elem).parents('.o_withEllipsis');
		container.removeClass('o_showOverflow');
		Ellipsis.prototype.initEllipsisElement(container);
	}
	
	OPOL.Ellipsis = new Ellipsis();
	
}(jQuery, OPOL);
