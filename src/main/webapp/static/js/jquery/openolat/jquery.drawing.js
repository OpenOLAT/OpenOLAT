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
 *  @author srosse, www.frentix.com
 *  @date Mar. 2016
 * ========================================================
 */
(function($) {
	"use strict";
    $.fn.drawing = function(options) {
    	var draw = this.data("data-oo-drawing");
    	if(typeof draw === "undefined") {
    		draw = new Drawing(this, options);
    		this.data("data-oo-drawing", draw);
    	}
    	return draw;
	};
	
	var Drawing = function(panels, params) {
		this.settings = $.extend({
    		resize: true,
    		drag: true
        }, params );
		
		this.divPanel = panels.get(0);
		this.canvas = jQuery("canvas", this.divPanel);
	}
	
	Drawing.prototype.newCircle = function(prefix) {
		this.newShape(prefix,"circle","60,60,25")
	}
	
	Drawing.prototype.newRectangle = function(prefix) {
		this.newShape(prefix,"rect","50,50,100,100")
	}
	
	Drawing.prototype.newShape = function(prefix, shape, coords) {
		return this.shape(this.generateInputsAndId(prefix, shape, coords), shape, coords)
	}
	
	Drawing.prototype.shape = function(id, shape, coords) {
		if("circle" == shape) {
			return this.circle(id, coords)
		} else if("rect" == shape || "rectangle" == shape) {
			return this.rectangle(id, coords)
		}
	}
	
	/**
	 * coords circle: center-x, center-y, radius. Note. When the radius value is a percentage value,
	 *         user agents should calculate the final radius value based on the associated
	 *         object's width and height. The radius should be the smaller value of the two.
	 */
	Drawing.prototype.circle = function(id, coords) {
		jQuery(this.divPanel).append("<div id='" + id + "' class='o_draw_circle'></div>");
		
		var width, height, top, left;
		if(typeof coords === "undefined") {
			width = height = "100px";
			top = left = "5px";
		} else {
			var parts = coords.split(',');
			var radius = parseInt(parts[2]);//border
			width = height = (2 * radius);
			left = parseInt(parts[0]) - radius - 1;
			top = parseInt(parts[1]) - radius - 1;
		}

		var nodes = jQuery("#" + id).height(height + 'px').width(width + 'px')
			.css('top', top + 'px').css('left', left + 'px');
		if(this.settings.resize) {
			nodes = nodes.resizable({ aspectRatio: true, handles: "all", stop: function(event, ui) {
				calculateCircleCoords(this);
			}});
		}
		if(this.settings.drag) {
			nodes = nodes.draggable({ containment: "parent", scroll: false, stop: function(event, ui) {
				calculateCircleCoords(this);
			}});
		}
		return nodes;
	}
	
	/**
	 * coords rect: left-x, top-y, right-x, bottom-y.
	 */
	Drawing.prototype.rectangle = function(id, coords) {
		jQuery(this.divPanel).append("<div id='" + id + "' class='o_draw_rectangle'></div>");
		
		var width, height, top, left;
		if(typeof coords === "undefined") {
			width = '150px';
			height = '100px';
			top = left = '5px';
		} else {
			var parts = coords.split(',');
			left = parseInt(parts[0]) - 1;
			top = parseInt(parts[1]) - 1;
			width = parseInt(parts[2]) - left - 3;
			height = parseInt(parts[3]) - top - 3;
		}
		
		var nodes = jQuery("#" + id).height(height + 'px').width(width + 'px')
			.css('top', top + 'px').css('left', left + 'px');
		if(this.settings.resize) {
			nodes = nodes.resizable({ handles: "all", stop: function(event, ui) {
				calculateRectangleCoords(this);
			}});
		}

		if(this.settings.drag) {
			nodes = nodes.draggable({ containment: "parent", scroll: false, stop: function(event, ui) {
				calculateRectangleCoords(this);
			}});
		}
		return nodes;
	}
	
	Drawing.prototype.getCoords = function(spot) {
		if(spot.hasClass('o_draw_circle')) {
			return calculateCircleCoords(spot);
		} else if(spot.hasClass('o_draw_rectangle')) {
			return calculateRectangleCoords(spot);
		}
	}
	
	Drawing.prototype.generateInputsAndId = function(prefix, shape, coords) {
		if(typeof coords === "undefined") {
			prefix = "id";
		}
		var newId = prefix + Math.round(new Date().getTime());
		jQuery(this.divPanel).append("<input type='hidden' id='" + newId + "_shape' name='" + newId + "_shape' value='" + shape + "' />");
		jQuery(this.divPanel).append("<input type='hidden' id='" + newId + "_coords' name='" + newId + "_coords' value='" + coords + "' />");
		return newId;
	};
	
	function calculateCircleCoords(spot) {
		var id = jQuery(spot).attr('id');
        var position = jQuery(spot).position();
        var radius = parseInt(jQuery(spot).width(), 10) / 2;
        var coords = (position.left + radius + 1) + "," + (position.top + radius + 1) + "," + radius;
        jQuery("#" + id + "_shape").val("circle");
        jQuery("#" + id + "_coords").val(coords);
        return coords;
	};
	
	function calculateRectangleCoords(spot) {
		var id = jQuery(spot).attr('id');
        var position = jQuery(spot).position();
        var width = parseInt(jQuery(spot).width(), 10);
        var height = parseInt(jQuery(spot).height(), 10);
        var coords = (position.left + 1) + "," + (position.top + 1) + "," + (position.left + width + 3) + "," + (position.top + height + 3);
        jQuery("#" + id + "_shape").val("rect");
        jQuery("#" + id + "_coords").val(coords);
        return coords;
	};
}(jQuery));
