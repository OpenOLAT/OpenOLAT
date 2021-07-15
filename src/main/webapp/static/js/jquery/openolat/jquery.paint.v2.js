/*
 * ========================================================
 *  The code is based on fabric.js:
 *  
 *  @author srosse, www.frentix.com
 *  @date Jui. 2021
 */
(function($) {
	"use strict";
    $.fn.paintV2 = function(options) {
    	var paint = this.data("data-oo-paint-v2");
    	if(typeof paint === "undefined") {
    		paint = new PaintV2(this, options);
    		this.data("data-oo-paint-v2", paint);
    	}
    	return paint;
	};
	
	var PaintV2 = function(panels, params) {
		this.settings = $.extend({
			inputHolderId: '',
			formDispatchFieldId: ''
		}, params );
		
		var inputHolderId = this.settings.inputHolderId;
		var wrapperId = 'paintw_' + inputHolderId;
		var canvas = new fabric.Canvas('paint_' + inputHolderId, {
			isDrawingMode: false,
			preserveObjectStacking: true
		});
		fabric.Object.prototype.erasable = true;
		fabric.Object.prototype.transparentCorners = false;
		fabric.Object.prototype.cornerColor = '#2980b9';
		fabric.Object.prototype.borderColor = '#2980b9';

		var brushCanvas = document.querySelector('#' + wrapperId + ' canvas.brush_size');
		var brushContext = brushCanvas.getContext('2d');
		
		initSelectTools();
		initFormTools();
		initBrush();
		initLineTool();
		initBrushSettings();
		initColors();
		initClear();
		
		{ // starts with a blue brush
			selectTool(jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=brush]")[0]);
			jQuery('#' + wrapperId + ' div.o_group_colors a.blue').addClass("active");
			canvas.isDrawingMode = true;
			canvas.freeDrawingBrush.width = getBrushWidth();
			canvas.freeDrawingBrush.color = getBrushColor();
		}
		
		drawBrush();
		
		var selectionChanged = function(e) {
			var obj = e.target;
			if("textbox" === obj.type) {
				setBrushColor(obj.fill);
				setBrushWidth(obj.fontSize);
			} else if("circle" === obj.type || "rect" === obj.type || "ellipse" === obj.type) {
				setBrushColor(obj.stroke);
				setBrushWidth(obj.strokeWidth);
			}
		}
		canvas.on('selection:created', selectionChanged);
		canvas.on('selection:updated', selectionChanged);
		
		// save object model and png
		var save = function() {
			try {
				var objects = canvas.getObjects();
				var image = canvas.toDataURL('png');
  				jQuery('#op_' + inputHolderId + '_png').val(image);
  				var json = JSON.stringify(canvas);
  				jQuery('#op_' + inputHolderId + '_json').val("data:application/json;base64," + json);
			} catch(e) {
				if(window.console) console.log(e);
			}
		}
		function clearAndSave() {
			try {
				var objects = canvas.getObjects();
  				jQuery('#op_' + inputHolderId + '_png').val("");
  				var json = JSON.stringify(canvas);
  				jQuery('#op_' + inputHolderId + '_json').val("data:application/json;base64," + json);
			} catch(e) {
				if(window.console) console.log(e);
			}
		}
		canvas.on('object:modified', save);
		canvas.on('object:added', save);
		canvas.on('object:removed', save);
		canvas.on('mouse:out', save);
		
		// restore state
		var val = jQuery('#op_' + inputHolderId + '_json').val();
		if(!(typeof val == "undefined") && val.length > 0) {
			var json = JSON.parse(val);
			canvas.loadFromJSON(json, function() {
				canvas.renderAll(); 
			}, function(o, object) {
				if(object.isType("circle")) {
					object.setControlsVisibility({ ml: false, mb: false, mr: false, mt: false, mtr: false });
				}
			});
		}
		
		// select, stack up / down
		function initSelectTools() {
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=select]").on('click', function() {
				selectTool(this);
				canvas.isDrawingMode = false;
				save();
			});

			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=front]").on('click', function() {
				canvas.isDrawingMode = false;
				var obj = canvas.getActiveObject();
				if(obj != null) {
					obj.bringToFront();
					canvas.renderAll();
				}
				save();
			});
			
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=back]").on('click', function() {
				canvas.isDrawingMode = false;
				var obj = canvas.getActiveObject();
				if(obj != null) {
					obj.sendToBack();
					canvas.renderAll();
				}
				save();
			});
		}
		
		// objects: circle, rectangle, ellipse, text
		function initFormTools() {
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=text]").on('click', function() {
				canvas.isDrawingMode = false;
				deselectTool();
	
				var color = getBrushColor();
				var width = getBrushWidth();
				
				var textbox = new fabric.Textbox('Hello', {
					left: 35, top: 20,
					fill: color,
					fontFamily: 'helvetica',
					fontSize: '' + width,
					originX: 'left',
					hasRotatingPoint: false,
					centerTransform: true,
					strokeUniform: true
			    });
				canvas.add(textbox);
				textbox.bringToFront();
				canvas.setActiveObject(textbox);
			});
				
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=circle]").on('click', function() {
				canvas.isDrawingMode = false;
				deselectTool();
	
				var color = getBrushColor();
				var width = getBrushWidth();
				
				var circle = new fabric.Circle({
					left: 45, top: 25, radius: 65,
					originX: 'left',
					fill: 'rgba(255,255,255,0.0)',
					strokeWidth: width, stroke: color,
					hasRotatingPoint: false,
					centerTransform: true,
					strokeUniform: true
			    });
				circle.setControlsVisibility({ ml: false, mb: false, mr: false, mt: false, mtr: false });
				canvas.add(circle);
				circle.bringToFront();
				canvas.setActiveObject(circle);
			});
				
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=rectangle]").on('click', function() {
				canvas.isDrawingMode = false;
				deselectTool();
				
				var color = getBrushColor();
				var width = getBrushWidth();
	
				var rect = new fabric.Rect({
					left: 50, top: 30, width: 50, height: 50,
					originX: 'left',
					fill: 'rgba(255,255,255,0.0)',
					strokeWidth: width,
					stroke: color,
					originX: 'left',
					hasRotatingPoint: false,
					centerTransform: true,
					strokeUniform: true
			    });
				canvas.add(rect);
				rect.bringToFront();
				canvas.setActiveObject(rect);
			});
			
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=ellipse]").on('click', function() {
				canvas.isDrawingMode = false;
				deselectTool();
				
				var color = getBrushColor();
				var width = getBrushWidth();
	
				var ellipse = new fabric.Ellipse({
					left: 15, top: 10, rx: 65, ry: 35,
					fill: 'rgba(255,255,255,0.0)',
					strokeWidth: width, stroke: color,
					hasRotatingPoint: false,
					centerTransform: true,
					strokeUniform: true
			    });
				canvas.add(ellipse);
				ellipse.bringToFront();
				canvas.setActiveObject(ellipse);
			});
		}
		
		// brush size and opacity
		function initBrushSettings() {
			jQuery("#" + wrapperId + " div.width_range_ui").slider({
				min: 1,
				max: 100,
				value: 20,
				change: function(event, ui) {
					var width = ui.value / 2;
					canvas.freeDrawingBrush.width = width;
					canvas.freeDrawingBrush.color = getBrushColor();
					drawBrush();
					
					var obj = canvas.getActiveObject();
					if(obj != null) {
						if("circle" === obj.type || "rect" === obj.type || "ellipse" === obj.type) {
							obj.set('strokeWidth', width);
							canvas.renderAll();
						} else if("textbox" === obj.type) {
							obj.set('fontSize', width);
							canvas.renderAll();
						}
						save();
					}
					configureBrush();
				}
			});
				
			jQuery("#" + wrapperId + " div.opacity_range_ui").slider({
				min: 1,
				max: 100,
				value: 100,
				change: function(event, ui) {
					var color = getBrushColor();
					canvas.freeDrawingBrush.width = getBrushWidth();
					canvas.freeDrawingBrush.color = color;
					drawBrush();
					
					var obj = canvas.getActiveObject();
					if(obj != null) {
						if("circle" === obj.type || "rect" === obj.type || "ellipse" === obj.type) {
							obj.set('stroke', color);
							canvas.renderAll();
						} else if("textbox" === obj.type) {
							obj.set('fill', color);
							canvas.renderAll();
						}
						save();
					}
					configureBrush();
				}
			});
		}

		// drawing: line, brush, spray, eraser
		function initBrush() {
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=eraser]").on('click', function() {
				selectTool(this);

		        canvas.isDrawingMode = true;
				canvas.freeDrawingBrush = new fabric.EraserBrush(canvas);
		        canvas.freeDrawingBrush.width = getBrushWidth();
			});

			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=brush]").on('click', function() {
				selectTool(this);

				canvas.isDrawingMode = true;
				canvas.freeDrawingBrush = new fabric['PencilBrush'](canvas);
				configureBrush();
			}).each(function(index, el) {
				selectTool(this);
			}) ;
			
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=spray]").on('click', function() {
				selectTool(this);

				canvas.isDrawingMode = true;
				canvas.freeDrawingBrush = new fabric['SprayBrush'](canvas);
				configureBrush();
			});
		}
		
		function initLineTool() {
			jQuery("#" + wrapperId + " div.o_group_tools a[data-tool=line]").on('click', function() {
				canvas.isDrawingMode = false;
				selectTool(this);
			});
			
			var line;
			var isLineDown = false;
			
			canvas.on('mouse:down', function(o) {
				var selectedTool = getSelectedTool();
				if(canvas.isDrawingMode) {
					configureBrush();
				} else if("line" === selectedTool) {
					isLineDown = true;

					var color = getBrushColor();
					var width = getBrushWidth();

					var pointer = canvas.getPointer(o.e);
					var points = [ pointer.x, pointer.y, pointer.x, pointer.y ];
					line = new fabric.Line(points, {
						stroke: color,
						strokeWidth: width,
						originX: 'center',
						originY: 'center'
					});
					canvas.add(line);
				}
			});
			
			canvas.on('mouse:move', function(o) {
				if (!isLineDown) {
					return;
				}
				var pointer = canvas.getPointer(o.e);
				line.set({ x2: pointer.x, y2: pointer.y });
				canvas.renderAll();
			});

			canvas.on('mouse:up', function(o) {
				if (isLineDown) {
					isLineDown = false;
					canvas
						.discardActiveObject()
						.renderAll();
					save();
				}
			});
		}
		
		// colors: black...
		function initColors() {
			jQuery('#' + wrapperId + ' div.o_group_colors a').on('click', function() {
				jQuery('#' + wrapperId + ' div.o_group_colors a').removeClass("active");
				jQuery(this).addClass('active');
				
				var color = getBrushColor();
				canvas.freeDrawingBrush.color = color;
				var obj = canvas.getActiveObject();
				if(obj != null) {
					if("circle" === obj.type || "rect" === obj.type || "ellipse" === obj.type) {
						obj.set('stroke', color);
						canvas.renderAll();
					} else if("textbox" === obj.type) {
						obj.set('fill', color);
						canvas.renderAll();
					}
				}
				save();
				drawBrush();
			});
		}
		
		function initClear() {
			jQuery('#' + wrapperId + ' a.clear').on('click', function() {
				var obj = canvas.getActiveObject();
				if(obj != null) {
					canvas.remove(obj);
					canvas.renderAll();
				}
				save();
			});
			
			jQuery('#' + wrapperId + ' a.clearall').on('click', function() {
				var mainWin = o_getMainWin();
				var cachedTrans;
				if (mainWin) {
					cachedTrans = jQuery(document).ooTranslator().getTranslator(mainWin.o_info.locale, 'org.olat.ims.qti21.ui');
				} else {
					cachedTrans = {	translate : function(key) { return key; } }
				}
				
				var cancel = cachedTrans.translate('cancel');
				var erase = cachedTrans.translate('paint.erase');
				var eraseHint = cachedTrans.translate('paint.erase.hint');
				
				var modal = '';
				modal += '<div id="paintModal" class="modal fade" tabindex="-1" role="dialog">';
				modal += '  <div class="modal-dialog" role="document">';
				modal += '    <div class="modal-content">';
				modal += '      <div class="modal-body">';
				modal += '        <p>' + eraseHint + '</p>';
				modal += '      </div>';
				modal += '      <div class="modal-footer">';
				modal += '        <button type="button" class="btn btn-default" data-dismiss="modal">' + cancel + '</button>';
				modal += '        <button type="button" class="btn btn-primary" data-dismiss="modal">' + erase + '</button>';
				modal += '      </div>';
				modal += '    </div>';
				modal += '  </div>';
				modal += '</div>';
				jQuery("body").append(modal);
				jQuery('#paintModal').modal('show');
				jQuery('#paintModal button.btn-primary').on('click', function() {
					canvas.clear();
					clearAndSave();
				});
				jQuery('#paintModal').on('hidden.bs.modal', function (event) {
					jQuery("#paintModal").remove();
				});
				o_scrollToElement('#o_top');
			});
		}

		function getSelectedTool() {
			var selectedTool = jQuery('#' + wrapperId + ' div.o_group_tools a.active');
			if(selectedTool != null && selectedTool.length != 0) {
				return selectedTool[0].getAttribute("data-tool");
			}
			return null;
		}
		
		function selectTool(tool) {
			deselectTool();
			jQuery(tool).addClass('active');
		}
			
		function deselectTool() {
			jQuery('#' + wrapperId + ' div.o_group_tools a').removeClass("active");
		}

		function configureBrush() {
			canvas.freeDrawingBrush.width = getBrushWidth();
			canvas.freeDrawingBrush.color = getBrushColor();
		}
			
		function getBrushColor() {
			var color = jQuery('#' + wrapperId + ' div.o_group_colors a.active');
			var cssColor = "rgba(0,0,0,1.0)";
			if(color.length > 0) {
				cssColor = color[0].getAttribute('data-color');
			}
			var opacity = parseInt(jQuery('#' + wrapperId + ' div.opacity_range_ui').slider("value"));
			if(opacity < 100) {
				var op = opacity / 100.0;
				cssColor = cssColor.substring(0, cssColor.lastIndexOf(','));
				cssColor += "," + op + ")";
			}
			return cssColor;
		}
		
		function setBrushColor(color) {
			// remove opacity
			var opacitySep = color.lastIndexOf(',');
			var rgbColor = color.substring(0, opacitySep);
			jQuery('#' + wrapperId + ' div.o_group_colors a').each(function(index, el) {
				var elColor = el.getAttribute('data-color');
				if(elColor && elColor.startsWith(rgbColor)) {
					jQuery(el).addClass("active");
				} else {
					jQuery(el).removeClass("active");
				}
			});

			var opacity = Math.round(parseFloat(color.substring(opacitySep + 1, color.length - 1)) * 100.0);
			if(opacity >= 0 && opacity <= 100) {
				jQuery('#' + wrapperId + ' div.opacity_range_ui').slider("value", opacity);
			}
		}
		
		function getBrushWidth() {
			var val = jQuery('#' + wrapperId + ' div.width_range_ui').slider("value");
			if(val == 0) {
				val = 1;
			}
			return val / 2;
		}
		
		function setBrushWidth(width) {
			if(width >= 0 && width <= 100) {
				jQuery('#' + wrapperId + ' div.width_range_ui').slider("value", width * 2);
			}
		}

		function drawBrush() {
			brushContext.clearRect(0, 0, brushCanvas.width, brushCanvas.height);

			var radius = getBrushWidth() / 2;
			var color = getBrushColor();
			var centerX = brushCanvas.width / 2;
			var centerY = radius + 1;
			
			brushContext.beginPath();
			brushContext.arc(centerX, centerY, radius, 0, 2 * Math.PI, false);
			brushContext.fillStyle = color;
			brushContext.fill();
			if(color.startsWith("rgba(255,255,255,") || color.startsWith("#ff0000")) {
				brushContext.strokeStyle = "#000000";
				brushContext.lineWidth = "1";
				brushContext.stroke();
			}
		}
	}
}(jQuery));