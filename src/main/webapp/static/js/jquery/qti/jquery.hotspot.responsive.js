/*
* rwdImageMaps jQuery plugin v1.6
*
* Allows image maps to be used in a responsive design by recalculating the area coordinates to match the actual image size on load and window.resize
*
* Copyright (c) 2016 Matt Stow
* https://github.com/stowball/jQuery-rwdImageMaps
* http://mattstow.com
* Licensed under the MIT license
*/
(function($) {
	"use strict";
	$.fn.rwdImageMaps = function(options) {
		var settings = $.extend({
			fillColor: 'bbbbbb',
			fillOpacity: 0.5,
			strokeColor: '6E6E6E',
			strokeOpacity: 1.0,
		}, options );

		var $img = this;
		var loaded = null;
		var windowWidth = 0;
		var hotSpotContainerWidth = 0;

		var rwdImageMap = function() {
			$img.each(function() {
				if (typeof($(this).attr('usemap')) == 'undefined')
					return;

				var recalculateMap = function(that, $that) {
					// Since WebKit doesn't know the height until after the image has loaded, perform everything in an onload copy
					$('<img />').on('load', function() {
						var attrW = 'width',
							attrH = 'height';
						
						if (!$that.data(attrW)) // size are backuped
							$that.data(attrW, $that.attr(attrW));
						if (!$that.data(attrH)) // size are backuped
							$that.data(attrH, $that.attr(attrH));
						
						var w = $that.data(attrW),
							h = $that.data(attrH);
	
						if (!w || !h) {
							var temp = new Image();
							temp.src = $that.attr('src');
							if (!w) {
								w = temp.width;
								$that.data(attrW, h);
							}
							if (!h) {
								h = temp.height;
								$that.data(attrH, h);
							}
						}
	
						var containerWidth = $that.closest(".o_oo_hotcontainer").width();
						
						var tw = $that.width(),
							th = $that.height();
						if(containerWidth < w) {
							var ratio = h / w;
							tw = containerWidth;
							th = ((containerWidth * ratio)|0);
							$that.width(tw);
							$that.height(th);
						} else if(tw == 0 && th == 0) {
							// image was hidden
							tw = w;
							th = h;
							$that.width(tw);
							$that.height(th);
						} 
					
						var wPercent = tw/100,
							hPercent = th/100,
							map = $that.attr('usemap').replace('#', ''),
							c = 'coords';
	
						$('map[name="' + map + '"]').find('area').each(function() {
							var $this = $(this);
							if (!$this.data(c)) // coords are backuped
								$this.data(c, $this.attr(c));
							
							var coords = $this.data(c).split(','),
								coordsPercent = new Array(coords.length);
	
							for (var i = 0; i < coordsPercent.length; ++i) {
								if (i % 2 === 0)
									coordsPercent[i] = parseInt(((coords[i]/w)*100)*wPercent);
								else
									coordsPercent[i] = parseInt(((coords[i]/h)*100)*hPercent);
							}
							$this.attr(c, coordsPercent.toString());
						});
						
						loaded = true;
						
						$that.maphilight({
							fillColor:  settings.fillColor,
							fillOpacity: settings.fillOpacity,
							strokeColor: settings.strokeColor,
							strokeOpacity: settings.strokeOpacity,
							strokeWidth: 3,
							alwaysOn: true
						});
						
					}).attr('src', $that.attr('src'));
				};
				
				var that = this,
					$that = $(that);
				var containerWidth = $that.closest(".o_oo_hotcontainer").width();

				if(loaded) {
					if(windowWidth != window.innerWidth || hotSpotContainerWidth != containerWidth) {
						recalculateMap(that, $that);
						windowWidth = window.innerWidth;
						hotSpotContainerWidth = containerWidth
					}
				} else {
					windowWidth = window.innerWidth;
					hotSpotContainerWidth = containerWidth;
					recalculateMap(that, $that);
				}
			});
		};
		$(window).resize(rwdImageMap).trigger('resize');
		$($img).closest('.o_qti21_collapsable_solution').on('shown.bs.collapse', rwdImageMap);

		return this;
	};
})(jQuery);
