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
	}
	
	DrawingV2.prototype.init = function() {
		this.updateButtons()
		    .updatePositionFields();
		if(this.settings.drag) {
			var board = this;
			// first destroy
			interact("#" + this.divPanelId + " div.o_draw_rectangle")
				.unset();
			interact("#" + this.divPanelId + " div.o_draw_circle")
				.unset();
			
			var onDragMove = function(event) {
				var target = event.target;
  				var x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx;
  				var y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;
				target.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
				target.setAttribute('data-x', x);
				target.setAttribute('data-y', y);
			
				board.updateCoords([target])
				     .updatePositionFields();
			};
			
			var onResizeMove = function(event) {
				var target = event.target
				var x = (parseFloat(target.getAttribute('data-x')) || 0)
				var y = (parseFloat(target.getAttribute('data-y')) || 0)
				target.style.width = event.rect.width + 'px'
				target.style.height = event.rect.height + 'px'

				// translate when resizing from top or left edges
				x += event.deltaRect.left
				y += event.deltaRect.top

				target.style.transform = 'translate(' + x + 'px,' + y + 'px)'
				target.setAttribute('data-x', x)
				target.setAttribute('data-y', y)

				board.updateCoords([target])
				     .updatePositionFields();
			};
			
			var onEnd = function(event) {
				board.updateCoords([event.target])
				     .updatePositionFields();
			}; 

			interact("#" + this.divPanelId + " div.o_draw_rectangle").resizable({
				edges: {
					left: true, right: true, bottom: true, top: true
				},
				invert: 'none'
   			}).draggable({
				modifiers: [
      				interact.modifiers.restrictRect({
        				restriction: 'parent'
      				})
    			]
			}).on('dragmove', onDragMove).on('resizemove', onResizeMove).on('dragend resizeend', onEnd);
			
			interact("#" + this.divPanelId + " div.o_draw_circle").resizable({
				edges: {
					left: true, right: true, bottom: true, top: true
				},
				invert: 'none',
				square: true
   			}).draggable({
				modifiers: [
      				interact.modifiers.restrictRect({
        				restriction: 'parent'
      				})
    			]
			}).on('dragmove', onDragMove).on('resizemove', onResizeMove).on('dragend resizeend', onEnd);
			
		}
		this.scale();
		return this;
	}
	
	DrawingV2.prototype.scale = function() {
		var newWidth = this.scaleVal(jQuery(this.editorPanel).width());
		var newHeight = this.scaleVal(jQuery(this.editorPanel).height());
		jQuery(this.editorPanel).width(newWidth);
		jQuery(this.editorPanel).height(newHeight);
	}
	
	DrawingV2.prototype.scaleVal = function(val) {
		if(this.settings.scale == 1.0) {
			return val;
		}
		return this.settings.scale * val;
	}
	
	DrawingV2.prototype.unscaleVal = function(val) {
		if(this.settings.scale == 1.0) {
			return val;
		}
		return Math.round(val / this.settings.scale);
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
		jQuery(this.editorPanel).append("<div id='" + id + "' class='o_draw_circle " + id + " o_draw_shape'></div>");

		var position = this.toAbsoluteCirclePosition(coords);
		var sPosition = this.toScaledPosition(position);
		var nodes = jQuery("#" + this.divPanelId + " div." + id)
			.height(sPosition.height + 'px')
			.width(sPosition.width + 'px')
			.css('top', sPosition.top + 'px')
			.css('left', sPosition.left + 'px');

		if(this.settings.selection && this.settings.selection  != null && this.settings.selection.indexOf(id) >= 0) {
			nodes.addClass('o_hotspot_selected');
		}
		return nodes;
	}
	
	/**
	 * coords rect: left-x, top-y, right-x, bottom-y.
	 */
	DrawingV2.prototype.rectangle = function(id, coords) {
		jQuery(this.editorPanel).append("<div id='" + id + "' class='o_draw_rectangle " + id + " o_draw_shape'></div>");
		
		var position = this.toAbsoluteRectPosition(coords);
		var sPosition = this.toScaledPosition(position);
		var nodes = jQuery("#" + this.divPanelId + " div." + id)
			.height(sPosition.height + 'px')
			.width(sPosition.width + 'px')
			.css('top', sPosition.top + 'px')
			.css('left', sPosition.left + 'px');
			
		if(this.settings.selection && this.settings.selection  != null && this.settings.selection.indexOf(id) >= 0) {
			nodes.addClass('o_hotspot_selected');
		}
		return nodes;
	}
	
	DrawingV2.prototype.calculateCoords = function(spot) {
		if(spot.classList.contains('o_draw_circle')) {
			return this.calculateCircleCoords(spot);
		} else if(spot.classList.contains('o_draw_rectangle')) {
			return this.calculateRectangleCoords(spot);
		}
	};
	
	DrawingV2.prototype.calculateCircleCoords = function(spot) {
		var jSpot = jQuery(spot);
		var id = jSpot.attr('id');
        var position = jSpot.position();
        var radius = parseInt(jSpot.width(), 10) / 2;
        var left = this.unscaleVal(position.left + radius) + 1;
        var top = this.unscaleVal(position.top + radius) + 1;
        radius =  this.unscaleVal(radius);
        var coords = left + "," + top + "," + radius;
        jQuery("#" + id + "_shape").val("circle");
        jQuery("#" + id + "_coords").val(coords);
        return coords;
	};
	
	DrawingV2.prototype.calculateRectangleCoords = function(spot) {
		var jSpot = jQuery(spot);
		var id = jSpot.attr('id');
        var position = jSpot.position();
        var width = parseInt(jSpot.width(), 10);
        var height = parseInt(jSpot.height(), 10);
        var left = this.unscaleVal(position.left) + 1;
        var top = this.unscaleVal(position.top) + 1;
        var right = this.unscaleVal(position.left + width) + 3;
        var bottom = this.unscaleVal(position.top + height) + 3;
        var coords = left + "," + top + "," + right + "," + bottom;
        jQuery("#" + id + "_shape").val("rect");
        jQuery("#" + id + "_coords").val(coords);
        return coords;
	};
	
	DrawingV2.prototype.getAbsolutePosition = function(spot) {
		var id = spot.getAttribute("id");
		var coords = this.editorPanel.querySelector("#" + id + "_coords").getAttribute("value");
		var shape = this.editorPanel.querySelector("#" + id + "_shape").getAttribute("value");
		if(shape === "circle") {
			return this.toAbsoluteCirclePosition(coords);
		} else if(shape === "rect") {
			return this.toAbsoluteRectPosition(coords);
		}
		return { left: 0, top: 0, width: 0, height: 0 };
	}
	
	DrawingV2.prototype.toScaledPosition = function(position) {
		var h = this.scaleVal(position.height);
		var w = this.scaleVal(position.width);
		var t = this.scaleVal(position.top);
		var l = this.scaleVal(position.left);
		return { left: l, top: t, width: w, height: h };
	}
	
	DrawingV2.prototype.toAbsoluteRectPosition = function(coords) {
		var parts = coords.split(',');
		var l = parseInt(parts[0]) - 1;
		var t = parseInt(parts[1]) - 1;
		var w = parseInt(parts[2]) - l - 3;
		var h = parseInt(parts[3]) - t - 3;
		return { left: l, top: t, width: w, height: h };
	}
	
	DrawingV2.prototype.toAbsoluteCirclePosition = function(coords) {
		var parts = coords.split(',');
		var radius = parseInt(parts[2]);//border
		var width = (2 * radius);
		var left = parseInt(parts[0]) - radius - 1;
		var top = parseInt(parts[1]) - radius - 1;
		return { left: left, top: top, width: width, height: width };
	}
	
	DrawingV2.prototype.generateInputsAndId = function(prefix, shape, coords) {
		if(typeof coords === "undefined") {
			prefix = "id";
		}
		var newId = prefix + Math.round(new Date().getTime());
		jQuery(this.editorPanel).append("<input type='hidden' id='" + newId + "_shape' name='" + newId + "_shape' value='" + shape + "' />");
		jQuery(this.editorPanel).append("<input type='hidden' id='" + newId + "_coords' name='" + newId + "_coords' value='" + coords + "' />");
		return newId;
	};
	
	DrawingV2.prototype.updateCoords = function(spots) {
		for(var i=spots.length; i-->0; ) {
			var id = spots[i].getAttribute("id");
			var coords = this.calculateCoords(spots[i]);
			var inputEl = this.editorPanel.querySelector("#" + id + "_coords");
			inputEl.setAttribute("value", coords);
		}
		return this;
	};
	
	DrawingV2.prototype.updateHotspotCoords = function(hotspot) {

		var spot = jQuery(hotspot);
		var id = spot.attr("id");
		var coords = this.getCoords(spot);
		var inputEl = this.editorPanel.querySelector("#" + id + "_coords");
		inputEl.setAttribute("value", coords);
		return this;
	};
	
	DrawingV2.prototype.updatePositionFields = function() {
		if(document.querySelector("#spot-position-x") == null) {
			return this; // nothing to update
		}
	
		var selectedSpots = this.selectedSpots();

		var x = "";
		var y = "";
		var width = "";
		var height = "";
		if(selectedSpots != null && selectedSpots.length > 0) {
			for(var i=0; i<selectedSpots.length; i++) {
				var pos = this.getAbsolutePosition(selectedSpots[i]);
				if(i == 0) {
					x = pos.left;
					y = pos.top;
					width = pos.width;
					height = pos.height;
				} else {
					if(x != pos.left) {
						x = this.settings.mixedLabel;
					}
					if(y != (pos.top)) {
						y = this.settings.mixedLabel;
					}
					if(width != pos.width) {
						width = this.settings.mixedLabel;
					}
					if(height != pos.height) {
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
	
	DrawingV2.prototype.select = function(e, spot) {
		if(!e.originalEvent.metaKey && !e.originalEvent.altKey) {
			this.deselectAll();
		}
		jQuery(spot).addClass('o_hotspot_selected');
		
		var selections = "";
		var panelId = this.divPanelId;
		this.selectedSpots().each(function(index, obj) {
			var id = obj.getAttribute("id");
			if(id != null) {
				selections += id + " ";
				var checkEl = document.querySelector("#" + panelId + " input[value='" + id + "']");
				if(checkEl != null) {
					checkEl.checked = true;
				}
			}
		});
		document.querySelector("#" + panelId + " #hotspots_selection").setAttribute("value", selections);

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
		var panelId = this.divPanelId;
		jQuery("div", this.editorPanel).each(function(index, obj) {
			var id = obj.getAttribute("id");
			if(id != null) {
				jQuery(obj).removeClass('o_hotspot_selected');
				var checkEl = document.querySelector("#" + panelId + " input[value='" + id + "']");
				if(checkEl != null) {
					checkEl.checked = false;
				}
			}
		});
		this.updatePositionFields();
	};
	
	DrawingV2.prototype.alignLeft = function() {
		var selectedSpots = this.selectedSpots();
		var minLeft = 32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			minLeft = Math.min(minLeft, position.left);
		}
		if(minLeft != 32000) {
			var scaledLeft = this.scaleVal(minLeft);
			for(var i=selectedSpots.length; i-->0; ) {
				jQuery(selectedSpots[i]).css({ left: scaledLeft });
			}
			this.updateCoords(selectedSpots)
		        .updatePositionFields();
		}
		
		return this;
	};
	
	DrawingV2.prototype.alignCenterHorizontal = function() {
		var selectedSpots = this.selectedSpots();
		
		var minLeft = 32000;
		var maxRight = -32000;

		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			var obj = jQuery(selectedSpots[i]);
			minLeft = Math.min(minLeft, position.left);
			var right = position.left + position.width;
			maxRight = Math.max(maxRight, right);
		}		
		if(minLeft != 32000 && maxRight != -32000) {
			var averageCenter = ((maxRight - minLeft) / 2) + minLeft;
			for(var i=selectedSpots.length; i-->0; ) {
				var position = this.getAbsolutePosition(selectedSpots[i]);
				var left = this.scaleVal(averageCenter - (position.width / 2));
				jQuery(selectedSpots[i]).css({ left: left });
			}
			this.updateCoords(selectedSpots)
			    .updatePositionFields();
		}
		
		return this;
	};
	
	DrawingV2.prototype.alignRight = function() {
		var selectedSpots = this.selectedSpots();
		
		var maxRight = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			var right = position.left + position.width;
			maxRight = Math.max(maxRight, right);
		}
		if(maxRight != 32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				var position = this.getAbsolutePosition(selectedSpots[i]);
				var left = this.scaleVal(maxRight - position.width);
				jQuery(selectedSpots[i]).css({ left: left });
			}
			this.updateCoords(selectedSpots)
			    .updatePositionFields();
		}
		
		return this;
	};
	
	DrawingV2.prototype.alignTop = function() {
		var selectedSpots = this.selectedSpots();
		
		var minTop = 32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			minTop = Math.min(minTop, position.top);
		}
		if(minTop != 32000) {
			var scaledTop = this.scaleVal(minTop);
			for(var i=selectedSpots.length; i-->0; ) {
				jQuery(selectedSpots[i]).css({ top: scaledTop });
			}
			this.updateCoords(selectedSpots)
			    .updatePositionFields();
		}
		return this;
	};
	
	DrawingV2.prototype.alignCenterVertical = function() {
		var selectedSpots = this.selectedSpots();
		
		var minTop = 32000;
		var maxBottom = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			minTop = Math.min(minTop, position.top);
			var bottom = position.top + position.height;
			maxBottom = Math.max(maxBottom, bottom);
		}
		
		if(minTop != 32000 && maxBottom != -32000) {
			var averageCenter = ((maxBottom - minTop) / 2) + minTop;
			for(var i=selectedSpots.length; i-->0; ) {
				var position = this.getAbsolutePosition(selectedSpots[i]);
				var top = this.scaleVal(averageCenter - (position.height / 2));
				jQuery(selectedSpots[i]).css({ top: top });
			}
			this.updateCoords(selectedSpots)
		        .updatePositionFields();
		}
		return this;
	};
	
	DrawingV2.prototype.alignBottom = function() {
		var selectedSpots = this.selectedSpots();

		var maxBottom = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			var bottom = position.top + position.height;
			maxBottom = Math.max(maxBottom, bottom);
		}
		if(maxBottom != -32000) {
			for(var i=selectedSpots.length; i-->0; ) {
				var position = this.getAbsolutePosition(selectedSpots[i]);
				var top = this.scaleVal(maxBottom - position.height);
				jQuery(selectedSpots[i]).css({ top: top });
			}
			this.updateCoords(selectedSpots)
			    .updatePositionFields();
		}
		return this;
	};
	
	DrawingV2.prototype.equalizeWidth = function() {
		var selectedSpots = this.selectedSpots();
		
		var maxWidth = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			maxWidth = Math.max(maxWidth, position.width);
		}
		if(maxWidth != -32000) {
			this.updateWidthOf(selectedSpots, maxWidth)
			    .updatePositionFields();
		}
		return this;
	};
	
	DrawingV2.prototype.equalizeHeight = function() {
		var selectedSpots = this.selectedSpots();
		
		var maxHeight = -32000;
		for(var i=selectedSpots.length; i-->0; ) {
			var position = this.getAbsolutePosition(selectedSpots[i]);
			maxHeight = Math.max(maxHeight, position.height);
		}
		if(maxHeight != -32000) {
			this.updateHeightOf(selectedSpots, maxHeight)
			    .updatePositionFields();
		}
		return this;
	};
	
	DrawingV2.prototype.setLeft = function(left) {
		var selectedSpots = this.selectedSpots();
		var scaledLeft = this.scaleVal(left);
		for(var i=selectedSpots.length; i-->0; ) {
			jQuery(selectedSpots[i]).css({ left : scaledLeft });
		}
		this.updateCoords(selectedSpots)
		    .updatePositionFields();
		return this;
	};
	
	DrawingV2.prototype.setTop = function(top) {
		var selectedSpots = this.selectedSpots();
		var scaledTop = this.scaleVal(top);
		for(var i=selectedSpots.length; i-->0; ) {
			jQuery(selectedSpots[i]).css({ top : scaledTop });
		}
		this.updateCoords(selectedSpots)
		    .updatePositionFields();
		return this;
	};
	
	DrawingV2.prototype.setWidth = function(width) {
		var selectedSpots = this.selectedSpots();
		this.updateWidthOf(selectedSpots, width)
		    .updatePositionFields();
		if(selectedSpots.length == 1 && jQuery(selectedSpots[0]).hasClass("o_draw_circle")) {
			document.querySelector("#spot-position-h").setAttribute("value", width);
		}
		return this;
	};
	
	DrawingV2.prototype.setHeight = function(height) {
		var selectedSpots = this.selectedSpots();
		this.updateHeightOf(selectedSpots, height)
		    .updatePositionFields();
		if(selectedSpots.length == 1 && jQuery(selectedSpots[0]).hasClass("o_draw_circle")) {
			document.querySelector("#spot-position-w").setAttribute("value", height);
		}
		return this;
	};
	
	DrawingV2.prototype.updateWidthOf = function(selectedSpots, absoluteWidth) {
		if(absoluteWidth > 0) {
			var width = this.scaleVal(absoluteWidth);
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				if(obj.hasClass("o_draw_circle")) {
					obj.height(width);
				}
				obj.width(width);
			}
			this.updateCoords(selectedSpots);
		}
		return this;
	};
	
	DrawingV2.prototype.updateHeightOf = function(selectedSpots, absoluteHeight) {
		if(absoluteHeight > 0) {
			var height = this.scaleVal(absoluteHeight);
			for(var i=selectedSpots.length; i-->0; ) {
				var obj = jQuery(selectedSpots[i]);
				if(obj.hasClass("o_draw_circle")) {
					obj.width(height);
				}
				obj.height(height);
			}
			this.updateCoords(selectedSpots);
		}
		return this;
	};
	
}(jQuery));
