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
    $.fn.drawingV2 = function(options) {
    	var draw = this.data("data-oo-drawing-v2");
    	if(typeof draw === "undefined") {
    		draw = new DrawingV2(this, options);
    		this.data("data-oo-drawing-v2", draw);
    	}
    	return draw;
	};
	
	var DrawingV2 = function(panels, params) {
		this.settings = $.extend({
    		resize: true,
    		drag: true,
    		selection: '',
    		scale: 1.0,
    		mixedLabel: 'mixed'
        }, params);
		
		this.divPanel = panels.get(0);
		this.divPanelId = this.divPanel.getAttribute("id");
		this.editorPanel = jQuery(".o_qti_hotspots_editor", this.divPanel).get(0);
		this.canvas = jQuery("canvas", this.editorPanel);
		this.updateButtons();
	}
	
	DrawingV2.prototype.scale = function() {
		var newWidth = this.scaleVal(jQuery(this.editorPanel).width());
		var newHeight = this.scaleVal(jQuery(this.editorPanel).height());
		jQuery(this.editorPanel).width(newWidth);
		jQuery(this.editorPanel).height(newHeight);
	}
	
	DrawingV2.prototype.scaleVal = function(val) {
		return Math.floor(this.settings.scale * val);
	}
	
		DrawingV2.prototype.unscaleVal = function(val) {
		return Math.floor(val / this.settings.scale);
	}
	
	DrawingV2.prototype.newCircle = function(prefix) {
		this.newShape(prefix,"circle","60,60,25")
	}
	
	DrawingV2.prototype.newRectangle = function(prefix) {
		this.newShape(prefix,"rect","50,50,100,100")
	}
	
	DrawingV2.prototype.newShape = function(prefix, shape, coords) {
		return this.shape(this.generateInputsAndId(prefix, shape, coords), shape, coords)
	}
	
	DrawingV2.prototype.shape = function(id, shape, coords) {
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
	DrawingV2.prototype.circle = function(id, coords) {
		jQuery(this.editorPanel).append("<div id='" + id + "' class='o_draw_circle " + id + "'></div>");
		
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
		
		var sHeight = this.scaleVal(height);
		var sWidth = this.scaleVal(width);
		var sTop = this.scaleVal(top);
		var sLeft = this.scaleVal(left);

		var nodes = jQuery("#" + this.divPanelId + " div." + id).height(sHeight + 'px').width(sWidth + 'px')
			.css('top', sTop + 'px').css('left', sLeft + 'px');
		var drawingBoard = this;
		if(this.settings.resize) {
			nodes = nodes.resizable({ aspectRatio: true, handles: "all", stop: function(event, ui) {
				drawingBoard.calculateCircleCoords(this);
			}});
		}
		if(this.settings.drag) {
			nodes = nodes.draggable({ containment: "parent", scroll: false, stop: function(event, ui) {
				drawingBoard.calculateCircleCoords(this);
			}});
		}
		
		if(this.settings.selection && this.settings.selection  != null && this.settings.selection.indexOf(id) >= 0) {
			nodes.addClass('o_hotspot_selected');
		}
		return nodes;
	}
	
	/**
	 * coords rect: left-x, top-y, right-x, bottom-y.
	 */
	DrawingV2.prototype.rectangle = function(id, coords) {
		jQuery(this.editorPanel).append("<div id='" + id + "' class='o_draw_rectangle " + id + "'></div>");
		
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
		
		var sHeight = this.scaleVal(height);
		var sWidth = this.scaleVal(width);
		var sTop = this.scaleVal(top);
		var sLeft = this.scaleVal(left);
		
		var nodes = jQuery("#" + this.divPanelId + " div." + id).height(sHeight + 'px').width(sWidth + 'px')
			.css('top', sTop + 'px').css('left', sLeft + 'px');
		var drawingBoard = this;
		if(this.settings.resize) {
			nodes = nodes.resizable({ handles: "all", stop: function(event, ui) {
				drawingBoard.calculateRectangleCoords(this);
			}});
		}

		if(this.settings.drag) {
			nodes = nodes.draggable({ containment: "parent", scroll: false, stop: function(event, ui) {
				drawingBoard.calculateRectangleCoords(this);
			}});
		}
		
		if(this.settings.selection && this.settings.selection  != null && this.settings.selection.indexOf(id) >= 0) {
			nodes.addClass('o_hotspot_selected');
		}
		return nodes;
	}
	
	DrawingV2.prototype.getCoords = function(spot) {
		if(spot.hasClass('o_draw_circle')) {
			return this.calculateCircleCoords(spot);
		} else if(spot.hasClass('o_draw_rectangle')) {
			return this.calculateRectangleCoords(spot);
		}
	};
	
	DrawingV2.prototype.generateInputsAndId = function(prefix, shape, coords) {
		if(typeof coords === "undefined") {
			prefix = "id";
		}
		var newId = prefix + Math.round(new Date().getTime());
		jQuery(this.editorPanel).append("<input type='hidden' id='" + newId + "_shape' name='" + newId + "_shape' value='" + shape + "' />");
		jQuery(this.editorPanel).append("<input type='hidden' id='" + newId + "_coords' name='" + newId + "_coords' value='" + coords + "' />");
		return newId;
	};
	
	
	DrawingV2.prototype.updateCoords = function() {
		var spots = jQuery("#" + this.divPanelId + ">div.o_qti_hotspots_editor>div");
		for(var i=spots.length; i-->0; ) {
			var spot = jQuery(spots[i]);
			var id = spot.attr("id");
			var coords = this.getCoords(spot);
			var inputEl = this.editorPanel.querySelector("#" + id + "_coords");
			inputEl.setAttribute("value", coords);
		}
		return this;
	};
	
	DrawingV2.prototype.updatePositionFields = function(spot) {
		var selectedSpots = this.selectedSpots();

		var x = "";
		var y = "";
		var width = "";
		var height = "";
		if(selectedSpots != null && selectedSpots.length > 0) {
			for(var i=0; i<selectedSpots.length; i++) {
				var obj = jQuery(selectedSpots[i]);
				var pos = obj.position();
				if(i == 0) {
					x = pos.left;
					y = pos.left + obj.width();
					width = obj.width();
					height = obj.height();
				} else {
					if(x != pos.left) {
						x = this.settings.mixedLabel;
					}
					if(y != (pos.left + obj.width())) {
						y = this.settings.mixedLabel;
					}
					if(width != obj.width()) {
						width = this.settings.mixedLabel;
					}
					if(height != obj.height()) {
						height = this.settings.mixedLabel;
					}
				}
			}
		}
		
		document.querySelector("#spot-position-x").setAttribute("value", x);
		document.querySelector("#spot-position-y").setAttribute("value", y);
		document.querySelector("#spot-position-w").setAttribute("value", width);
		document.querySelector("#spot-position-h").setAttribute("value", height);
		return  this;
	};
	
	DrawingV2.prototype.calculateCircleCoords = function(spot) {
		var id = jQuery(spot).attr('id');
        var position = jQuery(spot).position();
        var radius = parseInt(jQuery(spot).width(), 10) / 2;
        var left = this.unscaleVal(position.left + radius + 1);
        var top = this.unscaleVal(position.top + radius + 1);
        radius =  this.unscaleVal(radius);
        var coords = left + "," + top + "," + radius;
        jQuery("#" + id + "_shape").val("circle");
        jQuery("#" + id + "_coords").val(coords);
        return coords;
	};
	
	DrawingV2.prototype.calculateRectangleCoords = function(spot) {
		var id = jQuery(spot).attr('id');
        var position = jQuery(spot).position();
        var width = parseInt(jQuery(spot).width(), 10);
        var height = parseInt(jQuery(spot).height(), 10);
        var left = this.unscaleVal(position.left + 1);
        var top = this.unscaleVal(position.top + 1);
        var right = this.unscaleVal(position.left + width + 3);
        var bottom = this.unscaleVal(position.top + height + 3);
        var coords = left + "," + top + "," + right + "," + bottom;
        jQuery("#" + id + "_shape").val("rect");
        jQuery("#" + id + "_coords").val(coords);
        return coords;
	};
	
	DrawingV2.prototype.select = function(e, spot) {
		if(!e.originalEvent.metaKey && !e.originalEvent.altKey) {
			this.deselectAll();
		}
		jQuery(spot).addClass('o_hotspot_selected');
		
		var selections = "";
		this.selectedSpots().each(function(index, obj) {
			selections += obj.getAttribute("id") + " ";
		});
		document.querySelector("#" + this.divPanelId + " #hotspots_selection").setAttribute("value", selections);
		this.updateButtons();
	};
	
	DrawingV2.prototype.updateButtons = function(e, spot) {
		var selectedSpotList = this.selectedSpots();
		var disableClone = (selectedSpotList.length == 0);
		jQuery("div.o_group_clone a").each(function(index, obj) {
			if(disableClone) {
				jQuery(obj).addClass("disabled");
			} else {
				jQuery(obj).removeClass("disabled");
			}
		});
		
		
		var disableAlign = (selectedSpotList.length <= 1);
		jQuery("div.o_group_align a").each(function(index, obj) {
			if(disableAlign) {
				jQuery(obj).addClass("disabled");
			} else {
				jQuery(obj).removeClass("disabled");
			}
		});
		return this;
	};
	
	DrawingV2.prototype.selectedSpots = function() {
		return jQuery("div.o_hotspot_selected", this.editorPanel);
	};
	
	DrawingV2.prototype.deselectAll = function() {
		jQuery("div", this.editorPanel).each(function(index, obj) {
			jQuery(obj).removeClass('o_hotspot_selected');
		});
		this.updatePositionFields();
	};
	
	DrawingV2.prototype.alignLeft = function() {
		var selectedSpots = this.selectedSpots();
		var minLeft = 32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = jQuery(selectedSpots[i]).position();
			minLeft = Math.min(minLeft, position.left);
		}
		if(minLeft != 32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				jQuery(selectedSpots[i]).css({ left: minLeft });
			}
		}
		this.updateCoords();
		return this;
	};
	
	DrawingV2.prototype.alignCenterHorizontal = function() {
		var selectedSpots = this.selectedSpots();
		
		var minLeft = 32000;
		var maxRight = -32000;

		for(var i=selectedSpots.length; i-->0; ) {
			var obj = jQuery(selectedSpots[i]);
			minLeft = Math.min(minLeft, obj.position().left);
			var right = obj.position().left + obj.width();
			maxRight = Math.max(maxRight, right);
		}		
		if(minLeft != 32000 && maxRight != -32000) {
			var averageCenter = ((maxRight - minLeft) / 2) + minLeft;
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				var left = averageCenter - (obj.width() / 2);
				obj.css({ left: left });
			}
		}
		this.updateCoords();
		return this;
	};
	
	DrawingV2.prototype.alignRight = function() {
		var selectedSpots = this.selectedSpots();
		
		var maxRight = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var obj = jQuery(selectedSpots[i]);
			var right = obj.position().left + obj.width();
			maxRight = Math.max(maxRight, right);
		}
		if(maxRight != 32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				var left = maxRight - (obj.width());
				obj.css({ left: left });
			}
		}
		this.updateCoords();
		return this;
	};
	
	DrawingV2.prototype.alignTop = function() {
		var selectedSpots = this.selectedSpots();
		
		var minTop = 32000;
		for(var i=selectedSpots.length; i-->0; ) {
			minTop = Math.min(minTop, jQuery(selectedSpots[i]).position().top);
		}
		if(minTop != 32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				jQuery(selectedSpots[i]).css({ top: minTop });
			}
		}
		this.updateCoords();
		return this;
	};
	
	DrawingV2.prototype.alignCenterVertical = function() {
		var selectedSpots = this.selectedSpots();
		
		var minTop = 32000;
		var maxBottom = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var obj = jQuery(selectedSpots[i]);
			minTop = Math.min(minTop, obj.position().top);
			var bottom = obj.position().top + obj.height();
			maxBottom = Math.max(maxBottom, bottom);
		}
		
		if(minTop != 32000 && maxBottom != -32000) {
			var averageCenter = ((maxBottom - minTop) / 2) + minTop;
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				var top = averageCenter - (obj.height() / 2);
				obj.css({ top: top });
			}
		}
		this.updateCoords();
		return this;
	};
	
	DrawingV2.prototype.alignBottom = function() {
		var selectedSpots = this.selectedSpots();

		var maxBottom = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var obj = jQuery(selectedSpots[i]);
			var bottom = obj.position().top + obj.height();
			maxBottom = Math.max(maxBottom, bottom);
		}
		if(maxBottom != -32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				var top = maxBottom - obj.height();
				obj.css({ top: top });
			}
		}
		this.updateCoords();
		return this;
	};
	
	DrawingV2.prototype.equalizeWidth = function() {
		var selectedSpots = this.selectedSpots();
		
		var maxWidth = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var obj = jQuery(selectedSpots[i]);
			maxWidth = Math.max(maxWidth, obj.width());
		}
		if(maxWidth != -32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				if(obj.hasClass("o_draw_circle")) {
					obj.height(maxWidth);
				}
				obj.width(maxWidth);
			}
		}
		this.updateCoords();
		return this;
	};
	
	DrawingV2.prototype.equalizeHeight = function() {
		var selectedSpots = this.selectedSpots();
		
		var maxHeight = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var obj = jQuery(selectedSpots[i]);
			maxHeight = Math.max(maxHeight, obj.height());
		}
		if(maxHeight != -32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				if(obj.hasClass("o_draw_circle")) {
					obj.width(maxHeight);
				}
				obj.height(maxHeight);
			}
		}
		this.updateCoords();
		return this;
	};
	
}(jQuery));
