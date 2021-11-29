(function($) {
	var create_canvas_for, add_shape_to, clear_canvas, shape_from_area,
		canvas_style, hex_to_decimal, css3color, is_image_loaded, options_from_area;

	hex_to_decimal = function(hex) {
		return Math.max(0, Math.min(parseInt(hex, 16), 255));
	};
	
	css3color = function(color, opacity) {
		return 'rgba('+hex_to_decimal(color.substr(0,2))+','+hex_to_decimal(color.substr(2,2))+','+hex_to_decimal(color.substr(4,2))+','+opacity+')';
	};
	
	create_canvas_for = function(img) {
		var c = $('<canvas style="width:'+$(img).width()+'px;height:'+$(img).height()+'px;"></canvas>').get(0);
		c.getContext("2d").clearRect(0, 0, $(img).width(), $(img).height());
		return c;
	};
	
	var draw_annotation = function(context, shape, coords, annotation) {
		context.font = "18px Arial";
		if(shape == 'rect') {
			// x, y, width, height
			context.fillText(annotation, coords[0] + 5, coords[1] + 20);
		} else if(shape == 'poly') {
			context.fillText(annotation, coords[0], coords[1]);
		} else if(shape == 'circ') {
			context.fillText(annotation, coords[0] - 15, coords[1] + 3);
		}
	}
	
	var draw_shape = function(context, shape, coords, x_shift, y_shift) {
		x_shift = x_shift || 0;
		y_shift = y_shift || 0;
		
		context.beginPath();
		if(shape == 'rect') {
			// x, y, width, height
			context.rect(coords[0] + x_shift, coords[1] + y_shift, coords[2] - coords[0], coords[3] - coords[1]);
		} else if(shape == 'poly') {
			context.moveTo(coords[0] + x_shift, coords[1] + y_shift);
			for(var i=2; i < coords.length; i+=2) {
				context.lineTo(coords[i] + x_shift, coords[i+1] + y_shift);
			}
		} else if(shape == 'circ') {
			// x, y, radius, startAngle, endAngle, anticlockwise
			context.arc(coords[0] + x_shift, coords[1] + y_shift, coords[2], 0, Math.PI * 2, false);
		}
		context.closePath();
	}
	add_shape_to = function(canvas, shape, coords, annotation, options, name) {
		var context = canvas.getContext('2d');
		
		// Because I don't want to worry about setting things back to a base state
		
		// Shadow has to happen first, since it's on the bottom, and it does some clip /
		// fill operations which would interfere with what comes next.
		if(options.shadow) {
			context.save();
			if(options.shadowPosition == "inside") {
				// Cause the following stroke to only apply to the inside of the path
				draw_shape(context, shape, coords);
				context.clip();
			}
			
			// Redraw the shape shifted off the canvas massively so we can cast a shadow
			// onto the canvas without having to worry about the stroke or fill (which
			// cannot have 0 opacity or width, since they're what cast the shadow).
			var x_shift = canvas.width * 100;
			var y_shift = canvas.height * 100;
			draw_shape(context, shape, coords, x_shift, y_shift);
			
			context.shadowOffsetX = options.shadowX - x_shift;
			context.shadowOffsetY = options.shadowY - y_shift;
			context.shadowBlur = options.shadowRadius;
			context.shadowColor = css3color(options.shadowColor, options.shadowOpacity);
			
			// Now, work out where to cast the shadow from! It looks better if it's cast
			// from a fill when it's an outside shadow or a stroke when it's an interior
			// shadow. Allow the user to override this if they need to.
			var shadowFrom = options.shadowFrom;
			if (!shadowFrom) {
				if (options.shadowPosition == 'outside') {
					shadowFrom = 'fill';
				} else {
					shadowFrom = 'stroke';
				}
			}
			if (shadowFrom == 'stroke') {
				context.strokeStyle = "rgba(0,0,0,1)";
				context.stroke();
			} else if (shadowFrom == 'fill') {
				context.fillStyle = "rgba(0,0,0,1)";
				context.fill();
			}
			context.restore();
			
			// and now we clean up
			if(options.shadowPosition == "outside") {
				context.save();
				// Clear out the center
				draw_shape(context, shape, coords);
				context.globalCompositeOperation = "destination-out";
				context.fillStyle = "rgba(0,0,0,1);";
				context.fill();
				context.restore();
			}
		}
		
		context.save();
		
		draw_shape(context, shape, coords);
		draw_annotation(context, shape, coords, annotation);
		
		// fill has to come after shadow, otherwise the shadow will be drawn over the fill,
		// which mostly looks weird when the shadow has a high opacity
		if(options.fill) {
			context.fillStyle = css3color(options.fillColor, options.fillOpacity);
			context.fill();
		}
		// Likewise, stroke has to come at the very end, or it'll wind up under bits of the
		// shadow or the shadow-background if it's present.
		if(options.stroke) {
			context.strokeStyle = css3color(options.strokeColor, options.strokeOpacity);
			context.lineWidth = options.strokeWidth;
			context.stroke();
		}
		
		context.restore();
		
		if(options.fade) {
			$(canvas).css('opacity', 0).animate({opacity: 1}, 100);
		}
	};
	clear_canvas = function(canvas) {
		canvas.getContext('2d').clearRect(0, 0, canvas.width,canvas.height);
	};
	
	shape_from_area = function(area) {
		var i, coords = area.getAttribute('coords').split(',');
		for (i=0; i < coords.length; i++) { coords[i] = parseFloat(coords[i]); }
		return [area.getAttribute('shape').toLowerCase().substr(0,4), coords, area.getAttribute('data-annotation')];
	};

	options_from_area = function(area, options) {
		var $area = $(area);
		return $.extend({}, options, $.metadata ? $area.metadata() : false, $area.data('maphilight'));
	};
	
	is_image_loaded = function(img) {
		if(!img.complete) { return false; } // IE
		if(typeof img.naturalWidth != "undefined" && img.naturalWidth === 0) { return false; } // Others
		return true;
	};

	canvas_style = {
		position: 'absolute',
		left: 0,
		top: 0,
		padding: 0,
		border: 0
	};
	
	$.fn.maphilight = function(opts) {
		opts = $.extend({}, $.fn.maphilight.defaults, opts);
		
		return this.each(function() {
			var img, wrap, options, map, canvas, canvas_always, mouseover, highlighted_shape, usemap;
			img = $(this);
			
			if(!is_image_loaded(this)) {
				// If the image isn't fully loaded, this won't work right.  Try again later.
				return window.setTimeout(function() {
					img.maphilight(opts);
				}, 200);
			}

			options = $.extend({}, opts, $.metadata ? img.metadata() : false, img.data('maphilight'));

			// jQuery bug with Opera, results in full-url#usemap being returned from jQuery's attr.
			// So use raw getAttribute instead.
			usemap = img.get(0).getAttribute('usemap');

			if (!usemap) {
				return
			}

			map = $('map[name="'+usemap.substr(1)+'"]');

			if(!(img.is('img,input[type="image"]') && usemap && map.length > 0)) {
				return;
			}

			if(img.hasClass('maphilighted')) {
				// We're redrawing an old map, probably to pick up changes to the options.
				// Just clear out all the old stuff.
				var wrapper = img.parent();
				img.insertBefore(wrapper);
				wrapper.remove();
				$(map).unbind('.maphilight').find('area[coords]').unbind('.maphilight');
			}
			
			var imgWidth = $(img).width(),
				imgHeight = $(img).height();

			wrap = $('<div></div>').css({
				display:'block',
				"background-image":'url("'+this.src+'")',
				"background-size": imgWidth + 'px ' + imgHeight + 'px',
				position:'relative',
				padding:0,
				width:this.width,
				height:this.height
			});
			
			try {// try again
				var imageSrc = this.src;
				wrap.each(function(index, el) {
					el.style.setProperty("background-image", "url('" + imageSrc + "')", "important");
				});
			} catch(e) {
				if(window.console) console.log(e);
			}

			if(options.wrapClass) {
				if(options.wrapClass === true) {
					wrap.addClass($(this).attr('class'));
				} else {
					wrap.addClass(options.wrapClass);
				}
			}
			img.before(wrap).css('opacity', 0).css(canvas_style).remove();
			wrap.append(img);
			
			canvas = create_canvas_for(this);
			$(canvas).css(canvas_style);
			canvas.height = imgHeight;
			canvas.width = imgWidth;
			
			mouseover = function(e) {
				var shape, area_options;
				area_options = options_from_area(this, options);
				if(
					!area_options.neverOn
					&&
					!area_options.alwaysOn
				) {
					shape = shape_from_area(this);
					add_shape_to(canvas, shape[0], shape[1], shape[2], area_options, "highlighted");
					if(area_options.groupBy) {
						var areas;
						// two ways groupBy might work; attribute and selector
						if(/^[a-zA-Z][\-a-zA-Z]+$/.test(area_options.groupBy)) {
							areas = map.find('area['+area_options.groupBy+'="'+$(this).attr(area_options.groupBy)+'"]');
						} else {
							areas = map.find(area_options.groupBy);
						}
						var first = this;
						areas.each(function() {
							if(this != first) {
								var subarea_options = options_from_area(this, options);
								if(!subarea_options.neverOn && !subarea_options.alwaysOn) {
									var shapeArr = shape_from_area(this);
									add_shape_to(canvas, shapeArr[0], shapeArr[1], shapeArr[2], subarea_options, "highlighted");
								}
							}
						});
					}
				}
			}

			$(map).bind('alwaysOn.maphilight', function() {
				// Check for areas with alwaysOn set. These are added to a *second* canvas,
				// which will get around flickering during fading.
				if(canvas_always) {
					clear_canvas(canvas_always);
				}
				$(map).find('area[coords]').each(function() {
					var shape, area_options;
					area_options = options_from_area(this, options);
					if(area_options.alwaysOn) {
						if(!canvas_always) {
							canvas_always = create_canvas_for(img[0]);
							$(canvas_always).css(canvas_style);
							canvas_always.width = img[0].width;
							canvas_always.height = img[0].height;
							img.before(canvas_always);
						}
						area_options.fade = area_options.alwaysOnFade; // alwaysOn shouldn't fade in initially
						shape = shape_from_area(this);
						add_shape_to(canvas_always, shape[0], shape[1], shape[2], area_options, "");
					}
				});
			});
			
			$(map).trigger('alwaysOn.maphilight').find('area[coords]')
				.bind('mouseover.maphilight', mouseover)
				.bind('mouseout.maphilight', function(e) { clear_canvas(canvas); });
			
			img.before(canvas); // if we put this after, the mouseover events wouldn't fire.
			
			img.addClass('maphilighted');
		});
	};
	$.fn.maphilight.defaults = {
		fill: true,
		fillColor: '000000',
		fillOpacity: 0.2,
		stroke: true,
		strokeColor: 'ff0000',
		strokeOpacity: 1,
		strokeWidth: 1,
		fade: true,
		alwaysOn: false,
		neverOn: false,
		groupBy: false,
		wrapClass: true,
		// plenty of shadow:
		shadow: false,
		shadowX: 0,
		shadowY: 0,
		shadowRadius: 6,
		shadowColor: '000000',
		shadowOpacity: 0.8,
		shadowPosition: 'outside',
		shadowFrom: false
	};
})(jQuery);
