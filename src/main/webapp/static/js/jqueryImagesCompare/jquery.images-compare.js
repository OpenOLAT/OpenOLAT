/** global: Hammer */
;(function ($, window) {
	"use strict";

	function stringRepeat(s, precision) {
		// String repeat polyfill
		if (!String.prototype.repeat) {
			precision = precision || 1;
			return new Array(precision + 1).join(s);
		}
		return s.repeat(precision);
	}

	var pluginName = 'imagesCompare',
		defaults = {
			initVisibleRatio: 0.5,
			interactionMode: "drag", // "drag", "mousemove", "click"
			animationDuration: 400, // default animation duration in ms
			animationEasing: "swing",
			addSeparator: true, // add a html element on the separation
			addDragHandle: true, // add a html drag handle element on the separation
			precision: 4
		};

	// Our object, using revealing module pattern
	function ImagesCompare(element, options) {
		element = $(element);
		options = $.extend({}, defaults, options);
		options.roundFactor = parseInt('1' + stringRepeat('0', options.precision));

		this._name = pluginName;

		var frontElement, backElement, separator, dragHandle, lastRatio = 1, size = {
			width: 0,
			height: 0,
			maxWidth: 0,
			maxHeight: 0
		}, events = {
			initialised: "imagesCompare:initialised",
			changed: "imagesCompare:changed",
			resized: "imagesCompare:resized"
		};

		function onImagesLoaded() {
			var images = element.find('img'),
				totalImagesCount= images.length,
				elementsLoaded = 0;

			function onImageLoaded(){
				if (elementsLoaded >= totalImagesCount) {
					init();
				}
			}

			images.each(function() {
				// Image already loaded (cached)
				if ($(this)[0].complete) {
					totalImagesCount--;
					onImageLoaded();
				} else {
					// Image loading / error
					$(this).on('load', function() {
						elementsLoaded++;
						onImageLoaded();
					});
					$(this).on('error', function() {
						elementsLoaded++;
						onImageLoaded();
					});
				}
			});
		}

		onImagesLoaded();

		function init() {
			updateDom();
			patchSize();
			initInteractions();

			$(frontElement).attr('ratio', options.initVisibleRatio);
			setVisibleRatio(options.initVisibleRatio);

			// Let the world know we have done the init
			element.trigger({
				type: events.initialised
			});
		}

		function addResize() {
			$(window).on('resize', function (event) {
				frontElement.css('clip', '');
				patchSize();
				setVisibleRatio(lastRatio);

				// Let the world know we have done some resize updates
				element.trigger({
					type: events.resized,
					originalEvent: event
				});
			});
		}

		function initInteractions() {
			options.interactionMode = options.interactionMode.toLowerCase();

			if (options.interactionMode != "drag" && options.interactionMode != "mousemove" && options.interactionMode != "click") {
				console.warn('No valid interactionMode found, valid values are "drag", "mousemove", "click"');
			}

			switch (options.interactionMode) {
				case "drag":
					initDrag();
					break;
				case "mousemove":
					initMouseMove();
					break;
				case "click":
					initClick();
					break;
				default:
					initDrag();
			}
		}

		function initDrag() {
			if (typeof Hammer == 'undefined') {
				console.error('Please include the hammerjs library for drag support');
			}
			addDrag();
			addResize();
		}

		function initMouseMove() {
			addMouseMove();
			addResize();
		}

		function initClick() {
			addClick();
			addResize();
		}

		function addClick() {
			element.on('click', function (event) {
				var ratio = getElementRatio(event.pageX);
				setVisibleRatio(ratio);
			});
		}

		function addMouseMove() {
			var lastMove = 0;
			var eventThrottle = 1;
			element.on('mousemove', function (event) {
				event.preventDefault();
				var now = Date.now();
				if (now > lastMove + eventThrottle) {
					lastMove = now;
					var ratio = getElementRatio(event.pageX);
					setVisibleRatio(ratio);
				}
			});

			element.on('mouseout', function (event) {
				var ratio = getElementRatio(event.pageX);
				setVisibleRatio(ratio);
			});
		}

		function addDrag() {
			var hammertime = new Hammer(element[0]);
			hammertime.get('pan').set({direction: Hammer.DIRECTION_HORIZONTAL});
			hammertime.on('pan', function (event) {
				var ratio = getElementRatio(event.srcEvent.pageX);
				setVisibleRatio(ratio);
			});
		}

		function updateDom() {
			element.addClass('images-compare-container');
			element.css('display', 'inline-block');

			frontElement = element.find('> *:nth-child(1)');
			backElement = element.find('> *:nth-child(2)');

			frontElement.addClass("images-compare-before");
			frontElement.css('display', 'block');
			backElement.addClass("images-compare-after");
			backElement.css('display', 'block');

			if (options.addDragHandle) {
				buildDragHandle();
			}

			if (options.addSeparator) {
				buildSeparator();
			}
		}

		function buildSeparator() {
			element.prepend("<div class='images-compare-separator'></div>");
			separator = element.find(".images-compare-separator");

		}

		function buildDragHandle() {
			element.prepend("<div class='images-compare-handle'></div>");
			dragHandle = element.find(".images-compare-handle");
			dragHandle.append("<span class='images-compare-left-arrow'></span>");
			dragHandle.append("<span class='images-compare-right-arrow'></span>");
		}

		function patchSize() {
			var imgRef = backElement.find('img').first();
			setSize(imgRef.width(), imgRef.height(), imgRef.naturalWidth(), imgRef.naturalHeight());
			element.css('max-width', size.maxWidth + 'px');
			element.css('max-height', size.maxHeight + 'px');
			frontElement.width(size.width);
			frontElement.height(size.height);
		}

		/**
		 *
		 * @param x
		 * @return float
		 */
		function getElementRatio(x) {
			return roundRatio((x - element.offset().left) / frontElement.width());
		}

		/**
		 *
		 * @param ratio
		 * @return float
		 */
		function roundRatio(ratio) {
			ratio = Math.round((ratio * options.roundFactor)) / options.roundFactor;
			if (ratio > 1) {
				ratio = 1;
			}

			if (ratio < 0) {
				ratio = 0;
			}

			return ratio;

		}

		/**
		 * Animation request
		 *
		 * @param startValue float
		 * @param endValue float
		 * @param duration value in ms
		 * @param easing linear or swing
		 */
		function launchAnimation(startValue, endValue, duration, easing) {
			$(frontElement).attr('ratio', startValue).animate({ratio: startValue}, {
				duration: 0
			});

			$(frontElement).stop().attr('ratio', startValue).animate({ratio: endValue}, {
				duration: duration,
				easing: easing,
				step: function (now) {
					var width = getRatioValue(now);
					lastRatio = now;
					frontElement.attr('ratio', now).css('clip', 'rect(0, ' + width + 'px, ' + size.height + 'px, 0)');

					if (options.addSeparator) {
						separator.css('left', width + 'px');
					}

					if (options.addDragHandle) {
						dragHandle.css('left', width + 'px');
					}
				},
				done: function (animation, jumpedToEnd) {
					var ratio = $(frontElement).attr('ratio');
					// Let the world know something has changed
					element.trigger({
						type: events.changed,
						ratio: ratio,
						value: getRatioValue(ratio),
						animate: true,
						animation : animation,
						jumpedToEnd: jumpedToEnd
					});
				}
			});
		}

		/**
		 * Get value to reach, based on a ratio
		 *
		 * @param ratio float
		 * @return {number}
		 */
		function getRatioValue(ratio) {
			ratio = Math.round((ratio * options.roundFactor)) / options.roundFactor;
			return Math.round(frontElement.width() * ratio);
		}

		/**
		 * Change visible ratio
		 *
		 * @param ratio float
		 * @param animate boolean Do we want an animation ?
		 * @param duration in ms
		 * @param easing 'swing', 'linear'
		 */
		function setVisibleRatio(ratio, animate, duration, easing) {
			if (typeof animate == 'undefined') {
				animate = false;
			}

			var width = getRatioValue(ratio);

			if (animate) {
				var finalDuration = duration ? duration : options.animationDuration;
				var finalEasing = easing ? easing : options.animationEasing;

				launchAnimation(lastRatio, ratio, finalDuration, finalEasing);

				// Let the world know something has changed
				if (lastRatio != ratio) {
					element.trigger({
						type: events.changed,
						ratio: lastRatio,
						value: width,
						animate: animate
					});
				}

				return;

			} else {
				frontElement.stop().css('clip', 'rect(0, ' + width + 'px, ' + size.height + 'px, 0)');

				if (options.addSeparator) {
					$(separator).stop().css('left', width + 'px');
				}

				if (options.addDragHandle) {
					dragHandle.css('left', width + 'px');
				}
			}

			// Let the world know something has changed
			if (lastRatio != ratio) {
				element.trigger({
					type: events.changed,
					ratio: ratio,
					value: width,
					animate: animate
				});
			}

			lastRatio = ratio;
		}

		function setSize(width, height, maxWidth, maxHeight) {
			if (typeof width != 'undefined') {
				setWidth(width);
			}
			if (typeof height != 'undefined') {
				setHeight(height);
			}
			if (typeof maxWidth != 'undefined') {
				setMaxWidth(maxWidth);
			}
			if (typeof maxHeight != 'undefined') {
				setMaxHeight(maxHeight);
			}
			return size;
		}

		function setWidth(width) {
			size.width = width;
			return size;
		}

		function setMaxWidth(maxWidth) {
			size.maxWidth = maxWidth;
			return size;
		}

		function setHeight(height) {
			size.height = height;
			return size;
		}

		function setMaxHeight(maxHeight) {
			size.maxHeight = maxHeight;
			return size;
		}

		// public function declaration
		// returning element to preserve chaining
		return {
			"setValue": function (ratio, animate, duration, easing) {
				setVisibleRatio(ratio, animate, duration, easing);
				return element;
			},
			"getValue": function () {
				return lastRatio;
			},
			"on": function (eventName, callback) {
				element.on(eventName, callback);
				return element;
			},
			"off": function (eventName, callback) {
				element.off(eventName, callback);
				return element;
			},
			"events": function () {
				return events;
			}
		};
	}


	/**
	 * Plugin declaration
	 *
	 * @param userOptions
	 * @return {*}
	 */
	$.fn.imagesCompare = function (userOptions) {
		var options = $.extend(defaults, userOptions);
		return this.each(function () {
			if (!$.data(this, pluginName)) {
				$.data(this, pluginName, new ImagesCompare(this, options));
			}
		});
	};

})(jQuery, window, document);

// http://www.jacklmoore.com/notes/naturalwidth-and-naturalheight-in-ie/
(function ($) {
	var props = ['Width', 'Height'], prop, propsLength;

	propsLength = props.length;

	for (var index = 0; index < propsLength; index++) {
		prop = props[index];
		/*jslint loopfunc: true */
		(function (natural, prop) {
			$.fn[natural] = (natural in document.createElement('img')) ?
				function () {
					return this[0][natural];
				} :
				function () {
					var
						node = this[0],
						img,
						value = 0;

					if (node.tagName.toLowerCase() === 'img') {
						img = document.createElement('img');
						img.src = node.src;
						value = img[prop];
					}
					return value;
				};
		}('natural' + prop, prop.toLowerCase()));
		/*jslint loopfunc: false */
	}
}(jQuery));
