/*
 * ========================================================
 *  The code is heavily based on several blog entries:
 *  http://codetheory.in/html5-canvas-drawing-lines-with-smooth-edges/	
 *  
 *  @author srosse, www.frentix.com
 *  @date Mar. 2016
 */
(function($) {
	"use strict";
    $.fn.paint = function(options) {
    	var paint = this.data("data-oo-paint");
    	if(typeof paint === "undefined") {
    		paint = new Paint(this, options);
    		this.data("data-oo-paint", paint);
    	}
    	return paint;
	};
	
	var Paint = function(panels, params) {
		this.settings = $.extend({
			inputHolderId: '',
			formDispatchFieldId: ''
		}, params );
		
		var inputHolderId = this.settings.inputHolderId;
		var formDispatchFieldId = this.settings.formDispatchFieldId;
		
		this.divPanel = panels.get(0);
		this.canvas = jQuery("canvas", this.divPanel).get(0);
		var canvas = this.canvas;
		var ctx = this.canvas.getContext('2d');
	
		var sketchId = jQuery(this.divPanel).attr('id');
		var sketch = document.querySelector('#' + sketchId);
	
		var canvas_small = document.getElementById('brush_size');
		var context_small = canvas_small.getContext('2d');
		var centerX = canvas_small.width / 2;
		var centerY = canvas_small.height / 2;
		var radius;
	
		// Creating a tmp canvas
		var tmp_canvas = document.createElement('canvas');
		var tmp_ctx = tmp_canvas.getContext('2d');
		tmp_canvas.id = 'tmp_canvas';
		tmp_canvas.draggable = false;
		tmp_canvas.width = this.canvas.width;
		tmp_canvas.height = this.canvas.height;
		sketch.appendChild(tmp_canvas);

		var mouse = { x: 0, y: 0, out_x: 0, out_y: 0, paint: false, leave: false};
		var start_mouse = {x: 0, y: 0};
	
		var sprayIntervalID;
	
		// Pencil Points
		var ppts = [];
	
		// current tool
		var tool = 'brush';
		jQuery('#tools a#brush').addClass("active");
		jQuery('#tools a').on('click', function() {
			tool = jQuery(this).attr('id');
			jQuery('#tools a').removeClass("active");
			jQuery(this).addClass('active');
		});
		// colors
		jQuery('#colors a').on('click', function() {
			tmp_ctx.strokeStyle = jQuery(this).attr('id');
			tmp_ctx.fillStyle = tmp_ctx.strokeStyle;
			
			jQuery('#colors a').removeClass("active");
			jQuery(this).addClass('active');
			drawBrush();
		});
	
		/* Mouse Capturing Work */
		jQuery(tmp_canvas).on('mousemove touchmove', function(e) {
			copyEventCoordinateToMouse(e, tmp_canvas);
		});
		
		/* Mouse Capturing Work out of the canvas */
		jQuery(document).on('mousemove touchmove', function(e) {
			if(mouse.leave) {
				var rect = tmp_canvas.getBoundingClientRect();
				mouse.out_x = e.clientX - rect.left;
				mouse.out_y = e.clientY - rect.top;
			}
		});
	
		jQuery(this.canvas).on('mousemove touchmove', function(e) {
			copyEventCoordinateToMouse(e, this.canvas);
		});
		
		/* Draw the example brush */
		var drawBrush = function() {
			context_small.clearRect(0, 0, canvas_small.width, canvas_small.height);
			
			radius = tmp_ctx.lineWidth;
			radius = radius / 2;
			
			context_small.beginPath();
			context_small.arc(centerX, centerY, radius, 0, 2 * Math.PI, false);
			context_small.fillStyle = tmp_ctx.strokeStyle;
			context_small.globalAlpha = tmp_ctx.globalAlpha;
			context_small.fill();
		};
	
		/* Drawing on Paint App */
		tmp_ctx.lineWidth = 10;
		tmp_ctx.lineJoin = 'round';
		tmp_ctx.lineCap = 'round';
		tmp_ctx.strokeStyle = 'blue';
		tmp_ctx.fillStyle = 'blue';
		jQuery('#colors a.blue').addClass("active");
				
		//show current brush view
		drawBrush();

		jQuery(tmp_canvas).on('mousedown touchstart', function(e) {

			if(isDoubleTouch(e) || isRightClick(e)) {
				return;
			}
			jQuery(tmp_canvas).on('mousemove touchmove', onPaint);

			copyEventCoordinateToMouse(e, tmp_canvas);
			
			mouse.paint = true;
			start_mouse.x = mouse.x;
			start_mouse.y = mouse.y;
			
			ppts.push({x: mouse.x, y: mouse.y});
			
			//spraying tool.
			sprayIntervalID = setInterval(onPaint, 50);
			
			onPaint(e);
		}).on('mousedown touchstart', {formId: formDispatchFieldId}, setFlexiFormDirtyByListener);
	
		/* Events which stops drawing */
		jQuery(tmp_canvas).on('mouseup click touchend', function() {
			stopPainting();
		});
		
		/* Prevent action */
		jQuery(tmp_canvas).on('dragstart scroll', function() {
			return false;
		});
		
		jQuery(tmp_canvas).on('mouseleave', function(e) {
			mouse.leave = true;
			if(tool == "brush") {
				ppts.push({x: -1, y: -1});
			}
			
			jQuery(document).on("mouseup touchend", function(e){
				stopPainting();
			});
		});
		
		jQuery(tmp_canvas).on('mouseenter', function(e) {
			mouse.leave = false;
			jQuery(document).off("mouseup touchend");
		});
		
		var copyEventCoordinateToMouse = function(e, canvasElement) {
			if(typeof e.offsetX == 'undefined' && typeof e.layerX == 'undefined') {
				if((typeof e.originalEvent.layerX == 'undefined') && e.originalEvent.touches !== 'undefined' && e.originalEvent.touches.length > 0) {
					var te = e.originalEvent.touches[0];
					var canvasOffset = jQuery(canvasElement).offset();
					mouse.x = te.pageX - canvasOffset.left;
					mouse.y = te.pageY - canvasOffset.top;
				} else {
					mouse.x = e.originalEvent.layerX;
					mouse.y = e.originalEvent.layerY;
				}
			} else {
				mouse.x = typeof e.offsetX !== 'undefined' ? e.offsetX : e.layerX;
				mouse.y = typeof e.offsetY !== 'undefined' ? e.offsetY : e.layerY;
			}
		}
		
		var stopPainting = function() {
			jQuery(tmp_canvas).off('mousemove touchmove', onPaint);
			
			// for erasing
			ctx.globalCompositeOperation = 'source-over';
			//spraying tool.
			clearInterval(sprayIntervalID);
			
			// Writing down to real canvas now
			ctx.drawImage(tmp_canvas, 0, 0);
			// Clearing tmp canvas
			tmp_ctx.clearRect(0, 0, tmp_canvas.width, tmp_canvas.height);
			
			// Emptying up Pencil Points
			ppts = [];
			
			var image = canvas.toDataURL();
			jQuery('#' + inputHolderId).val(image);
		}
		
		jQuery("#width_range_ui").slider({
			min: 1,
			max: 100,
			value: 20,
			change: function(event, ui) {
				tmp_ctx.lineWidth = ui.value / 2;
				drawBrush();
			}
		});
		jQuery("#opacity_range_ui").slider({
			min: 1,
			max: 100,
			value: 100,
			change: function(event, ui) {
				tmp_ctx.globalAlpha = ui.value / 100;
				drawBrush();
			}
		});
		drawBrush();
		
		jQuery("#clear").on("click", function() {
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
			$('#paintModal').modal('show');
			$('#paintModal button.btn-primary').on('click', function() {
				ctx.clearRect(0, 0, tmp_canvas.width, tmp_canvas.height);
			});
			$('#paintModal').on('hidden.bs.modal', function (event) {
				jQuery("#paintModal").remove();
			});
			o_scrollToElement('#o_top');
		});

		var onPaintBrush = function() {
			// Saving all the points in an array
			if(!mouse.leave) {
				ppts.push({x: mouse.x, y: mouse.y});
			}

			if (ppts.length > 0 && ppts.length < 3) {
				var b = ppts[0];
				if(typeof b !== "undefined" && b.x >= 0 && b.y >= 0) {
					tmp_ctx.beginPath();
					tmp_ctx.arc(b.x, b.y, tmp_ctx.lineWidth / 2, 0, Math.PI * 2, !0);
					tmp_ctx.fill();
					tmp_ctx.closePath();
				}
				return;
			}
			
			
			// Tmp canvas is always cleared up before drawing.
			tmp_ctx.clearRect(0, 0, tmp_canvas.width, tmp_canvas.height);
			
			var stopped = true;
			for (var i = 0; i < ppts.length - 1; i++) {
				var ptx = ppts[i].x;
				var pty = ppts[i].y;
				if(ptx == -1 && pty == -1) {
					continue;
				}
				
				var nextPtx = ppts[i + 1].x;
				var nextPty = ppts[i + 1].y;

				if(stopped) {	
					tmp_ctx.beginPath();
					tmp_ctx.moveTo(ptx, pty);
					stopped = false;
				} else if(nextPtx >= 0 && nextPty >= 0) {
					var c = (ptx + nextPtx) / 2;
					var d = (pty + nextPty) / 2;
					tmp_ctx.quadraticCurveTo(ptx, pty, c, d);
					
				} else {
					tmp_ctx.stroke();
					stopped = true;
				}
			}
			tmp_ctx.stroke();
		};
	
		var onPaintLine = function() {
		    // Tmp canvas is always cleared up before drawing.
		    tmp_ctx.clearRect(0, 0, tmp_canvas.width, tmp_canvas.height);
		 
		    tmp_ctx.beginPath();
		    tmp_ctx.moveTo(start_mouse.x, start_mouse.y);
		    tmp_ctx.lineTo(mouse.x, mouse.y);
		    tmp_ctx.stroke();
		    tmp_ctx.closePath();
		};
		
		var onPaintCircle = function() {
		    // Tmp canvas is always cleared up before drawing.
		    tmp_ctx.clearRect(0, 0, tmp_canvas.width, tmp_canvas.height);
		    
		    var mx = mouse.leave ? mouse.out_x : mouse.x;
		    var my = mouse.leave ? mouse.out_y : mouse.y;

		    var x = (mx + start_mouse.x) / 2;
			var y = (my + start_mouse.y) / 2;
			var radius = Math.max(Math.abs(mx - start_mouse.x), Math.abs(my - start_mouse.y)) / 2;

		    tmp_ctx.beginPath();
		    tmp_ctx.arc(x, y, radius, 0, Math.PI*2, false);
		    tmp_ctx.stroke();
		    tmp_ctx.closePath();
		};

		var onPaintRect = function() {
		    // Tmp canvas is always cleared up before drawing.
		    tmp_ctx.clearRect(0, 0, tmp_canvas.width, tmp_canvas.height);
		    
		    var mx = mouse.leave ? mouse.out_x : mouse.x;
		    var my = mouse.leave ? mouse.out_y : mouse.y;
		 
		    var x = Math.min(mx, start_mouse.x);
			var y = Math.min(my, start_mouse.y);
			var width = Math.abs(mx - start_mouse.x);
			var height = Math.abs(my - start_mouse.y);
			tmp_ctx.strokeRect(x, y, width, height);
		};

		function onPaintEllipse(ctx) {
			tmp_ctx.clearRect(0, 0, tmp_canvas.width, tmp_canvas.height);
			
		    var mx = mouse.leave ? mouse.out_x : mouse.x;
		    var my = mouse.leave ? mouse.out_y : mouse.y;
			
			var x = Math.min(mx, start_mouse.x);
			var y = Math.min(my, start_mouse.y);
			
			var w = Math.abs(mx - start_mouse.x);
			var h = Math.abs(my - start_mouse.y);
		
			var kappa = .5522848,
			  ox = (w / 2) * kappa, // control point offset horizontal
		      oy = (h / 2) * kappa,   // control point offset vertical
		      xe = x + w,             // x-end
		      ye = y + h,             // y-end
		      xm = x + w / 2,         // x-middle
		      ym = y + h / 2;         // y-middle
		
			tmp_ctx.beginPath();
			tmp_ctx.moveTo(x, ym);
			tmp_ctx.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y);
			tmp_ctx.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym);
			tmp_ctx.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye);
			tmp_ctx.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym);
			tmp_ctx.closePath();
			tmp_ctx.stroke();
		}
	
		var onErase = function() {
			// Saving all the points in an array
			ppts.push({x: mouse.x, y: mouse.y});
			
			ctx.globalCompositeOperation = 'destination-out';
			ctx.fillStyle = 'rgba(0,0,0,1)';
			ctx.strokeStyle = 'rgba(0,0,0,1)';
			ctx.lineWidth = tmp_ctx.lineWidth;
			
			if (ppts.length < 3) {
				var b = ppts[0];
				ctx.beginPath();
				ctx.arc(b.x, b.y, ctx.lineWidth / 2, 0, Math.PI * 2, !0);
				ctx.fill();
				ctx.closePath();
				return;
			}
		
			ctx.beginPath();
			ctx.moveTo(ppts[0].x, ppts[0].y);
			
			for (var i = 1; i < ppts.length - 2; i++) {
				var c = (ppts[i].x + ppts[i + 1].x) / 2;
				var d = (ppts[i].y + ppts[i + 1].y) / 2;
				
				ctx.quadraticCurveTo(ppts[i].x, ppts[i].y, c, d);
			}
		
			// For the last 2 points
			ctx.quadraticCurveTo(
				ppts[i].x,
				ppts[i].y,
				ppts[i + 1].x,
				ppts[i + 1].y
			);
			ctx.stroke();
		};
	
		var getRandomOffset = function(radius) {
		    var random_angle = Math.random() * (2*Math.PI);
		    var random_radius = Math.random() * radius;
		    return {
		        x: Math.cos(random_angle) * random_radius,
		        y: Math.sin(random_angle) * random_radius
		    };
		};

		var generateSprayParticles = function() {
		    // Particle count, or, density
		    var density = tmp_ctx.lineWidth*2;
		     
		    for (var i = 0; i < density; i++) {
		        var offset = getRandomOffset(tmp_ctx.lineWidth);
		         
		        var x = mouse.x + offset.x;
		        var y = mouse.y + offset.y;
		         
		        tmp_ctx.fillRect(x, y, 1, 1);
		    }
		};
		
		function isDoubleTouch(e) {
			try {
				if(!(typeof e == "undefined")
						&& !(typeof e.originalEvent.touches == "undefined")
						&& e.originalEvent.touches.length > 1) {
					return true;
				}
			} catch(ex) {
				if(window.console) console.log(ex);
			}
			return false;
		}
		
		function isRightClick(e) {
			try {
				if(!(typeof e == "undefined")
						&& !(typeof e.which == "undefined")
						&& (e.which == 2 || e.which == 3)) {
					return true;
				}
			} catch(ex) {
				if(window.console) console.log(ex);
			}
			return false;
		}

		function onPaint(e) {
			if(!(typeof e == "undefined")) {
				if(isDoubleTouch(e) || isRightClick(e)) {
					return;
				}
				e.preventDefault();
			}
			if ( tool == 'brush' ) {
				onPaintBrush();
			} else if ( tool == 'circle' ) {
				onPaintCircle();
			} else if ( tool == 'line' ) {
				onPaintLine();
			} else if ( tool == 'rectangle' ) {
				onPaintRect();
			} else if ( tool == 'ellipse' ) {
				onPaintEllipse();
			} else if ( tool == 'eraser' ) {
				onErase();
			} else if ( tool == 'spray' ) {
				generateSprayParticles();
			}
		}
	}
}(jQuery));