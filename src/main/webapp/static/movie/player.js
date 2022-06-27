var BPlayer = {
	/**
	 * Create a video player within the given DOM element with the given parameters. Same as insertHTML5Player() 
	 */
	insertPlayer: function (address,domId,width,height,start,duration,provider,streamer,autostart,repeat,controlbar,poster,errorCallback) {
		BPlayer.insertHTML5Player(address,domId,width,height,start,duration,provider,streamer,autostart,repeat,controlbar,poster,errorCallback);
	},
	
	/**
	 * Create a video player within the given DOM element with the given parameters
	 */
	insertHTML5Player : function (address, domId, width, height, start, duration, provider, streamer, autostart, repeat, controlbar, poster, errorCallback) {
		// Calculate relative video URL
		var videoUrl = address;
		if(address.indexOf('://') < 0 && (address.indexOf('/raw/static/') == 0 || address.indexOf('/secstatic/qtieditor/') >= 0 || address.indexOf('/secstatic/qti/') >= 0)) {
			videoUrl = address;
		} else if(address.indexOf('://') < 0 && ((provider != "rtmp" && provider != "http") ||
				((provider == "rtmp" || provider == "http") && (streamer == undefined || streamer.length == 0)))) {
			var documentUrl = document.location.href;
			videoUrl = documentUrl.substring(0, documentUrl.lastIndexOf('/'));
			if(address.indexOf('/') != 0) {
				videoUrl += '/';
			}
			videoUrl += address;
		}

		// Prepare arguments for player
		var args = {
			file:videoUrl,
			width:width,
			height:height,
			controlbar: { position: "bottom" }
		};		
		if(typeof provider != 'undefined') {
			args.provider = provider;
		}
		if(provider == "rtmp" || provider == "http") {
			args.streamer = streamer;
		}
		if(typeof start != 'undefined') {
			var startInSec = BPlayer._convertInSeconds(start);
			if(startInSec > 0) {
				args.start = startInSec;
			}
		}
		if(typeof duration != 'undefined') {
			var durationInSec = BPlayer._convertInSeconds(duration);
			if(durationInSec > 0) {
				args.duration = durationInSec;
			}
		}
		if(typeof autostart != 'undefined' && autostart) {
			args.autostart = true;
		}
		if(typeof repeat != 'undefined' && repeat) {
			args.repeat = "single";
		}
		if(typeof controlbar != 'undefined' && !controlbar) {
			args.controlbar = "none";
		}
		if(typeof poster != 'undefined' && poster) {
			args.image = poster;
		}
		if(typeof errorCallback != 'undefined') {
			args.errorCallback = errorCallback;
		} else {
			args.errorCallback = function(mediaElement, originalNode, player){};
		}

		// Finally, load player library and play video
		if(provider == "nanoo" || videoUrl.indexOf('nanoo.tv/') >= 0) {
			BPlayer._loadNanooTv(domId, args);
		} else {
			// After loading immediately insert HTML5 player
			var afterloadCallback = function() {
				BPlayer._insertHTML5MediaElementPlayerWorker(domId, args);
			}
			BPlayer.loadMediaelementJsPlayer(afterloadCallback);
		}
	},
	
	/**
	 * Make sure the mediaelementjs video player JS and CSS code is loaded and execute the
	 * callback when the player is available. Normally you should call the insertPlayer() method instead.
	 */
	loadMediaelementJsPlayer : function(afterloadCallback) {
		var mediaElementBaseUrl = BPlayer._mediaElementBaseUrl();
		// Use minified for production, plain when in debug mode for development
		var mediaElementcss = mediaElementBaseUrl + (BPlayer.debugEnabled ? 'mediaelementplayer.css' : 'mediaelementplayer.min.css');
		var mediaElementJs = mediaElementBaseUrl + (BPlayer.debugEnabled ? 'mediaelement-and-player.js' : 'mediaelement-and-player.min.js');
		
		if(jQuery('#mediaelementplayercss').length == 0) {
			jQuery('<link>')
			  .appendTo('head')
			  .attr({id : 'mediaelementplayercss', type : 'text/css', rel : 'stylesheet'})
			  .attr('href', mediaElementcss);
		}

		if(typeof jQuery('body').mediaelementplayer != 'undefined') {
			if (afterloadCallback) {
				afterloadCallback();					
			}
		} else {
			jQuery.ajax({
					dataType: 'script',
					cache: true,
					async: false, //prevent 2x load of the mediaelement.js which is deadly
					url: mediaElementJs
				}).done(function() {
					if (afterloadCallback) {
						afterloadCallback();
					}
				});
		}
	},
	
	_loadNanooTv : function(domId, config) {
		var frameName = domId + "_frame";
		for(i=jQuery('#' + frameName).length; i-->0; ) {
			jQuery('#' + frameName).remove();
		}
		
		if (config.image === undefined || config.image === null || config.image.length == 0) {
			BPlayer._loadNanooTvFrame(domId, frameName, config);
		} else {
			BPlayer._loadNanooTvPoster(domId, frameName, config);
		}
	},
	
	_loadNanooTvPoster : function(domId, frameName, config) {
		if(jQuery('#mediaelementplayercss').length == 0) {
			var mediaElementBaseUrl = BPlayer._mediaElementBaseUrl();
			var mediaElementcss = mediaElementBaseUrl + (BPlayer.debugEnabled ? 'mediaelementplayer.css' : 'mediaelementplayer.min.css');
			jQuery('<link>')
			  .appendTo('head')
			  .attr({id : 'mediaelementplayercss', type : 'text/css', rel : 'stylesheet'})
			  .attr('href', mediaElementcss);
		}

		BPlayer._loadNanooResize(domId, frameName, config);
		
		var poster = "<div id='" + frameName + "' class='mejs__container' role='application' style='width:" + config.width + "px; height:" + config.height + "px; overflow:hidden; overflow-x:hidden; overflow-y:hidden;'>"
			       + "<div class='mejs__layers'>"
		           + "<div class='mejs__poster mejs__layer' style='background-image: url(" + config.image + "); width: 100%; height: 100%;'><img src='" + config.image + "' width='0' height='0'></img></div>"
		           + "<div class='mejs__overlay mejs__layer mejs__overlay-play' style='width: 100%; height: 100%; z-index:10;'><div class='mejs__overlay-button' style='z-index:10;' role='button' tabindex='0' aria-label='Play' aria-pressed='false'></div></div>"
		           + "</div>"
		           + "</div>";
		var jDomEl = jQuery('#' + domId);
		jDomEl.append(jQuery(poster));
		jDomEl.css("border", "none");
		jQuery('#' + domId + ' div.mejs__overlay-button').on('click', function(el, index) {
			var cloneConfig = {
				file: config.file,
				width: config.width,
				height: config.height,
				autostart: true
			};
			BPlayer._loadNanooTvFrame(domId, frameName, cloneConfig);
		})
	},
	
	_loadNanooTvFrame : function(domId, frameName, config) {
		for(i=jQuery('#' + frameName).length; i-->0; ) {
			jQuery('#' + frameName).remove();
		}
		
		BPlayer._loadNanooResize(domId, frameName, config);

		var url = config.file;
		var parts = url.split('?');
		var nanooId = parts[0].substring(url.lastIndexOf('/') + 1);
		if(config.autostart) {
			url = "https://www.nanoo.tv/link/w/" + nanooId;
		} else {
			url = "https://www.nanoo.tv/link/n/" + nanooId;
		}
		var iframe = '<iframe name="' + frameName + '" id="' + frameName
		           + '" src="' + url
		           + '" style="width:' + config.width + 'px; height:' + config.height + 'px; overflow:hidden;"'
		           + ' frameborder="0" allow="fullscreen" allowfullscreen="true"'
		           + '></iframe>';
		var jDomEl = jQuery('#' + domId);
		jDomEl.append(jQuery(iframe));
		jDomEl.css("border", "none");
	},
	
	_loadNanooResize : function(domId, frameName, config) {
		try {
			var jDomEl = jQuery('#' + domId);
			var parentContainer = jDomEl.parent("p , div");
			if(parentContainer.length == 1 && typeof config.width !== "undefined" && typeof config.height !== "undefined") {
				var containerWidth =  jQuery(parentContainer).width();
				var originalWidth = config.width;
				var originalHeight = config.height;
				// If container width is available and smaller, resize to fit container 
				if(containerWidth && containerWidth < originalWidth) {
					var ratio = originalHeight / originalWidth;
					var tw = containerWidth;
					var th = ((containerWidth * ratio)|0);
					config.width = tw;
					config.height = th;
				}
				
				jQuery(window).resize(function() {
					var container = jQuery('#' + domId).parent("p , div");
					var cWidth =  jQuery(container).width();
					if(cWidth && cWidth < originalWidth) {
						var ratio = originalHeight / originalWidth;
						var tw = cWidth;
						var th = ((cWidth * ratio)|0);
						jQuery('#' + frameName)
							.width(tw + "px")
							.height(th + "px");
					}
				}).trigger('resize');
			}
		} catch(e) {
			if(window.console) console.log(e);
		}
	},

	/*
	 * Internal helper methods
	 */
	
	_insertHTML5MediaElementPlayerWorker: function(domId, config) {
		var mediaElementBaseUrl = BPlayer._mediaElementBaseUrl();
		
		var meConfig = {
			loop: config.repeat,
			pluginPath: mediaElementBaseUrl,
			stretching: 'responsive',
			hls: {
		        path: mediaElementBaseUrl + 'hls/hls.min.js'
		    },
		    flv : {
		        path: mediaElementBaseUrl + 'flv/flv.min.js',
		        withCredentials: true
		    },
			error: config.errorCallback,
			success: function(mediaElement, originalNode, player) {
				if(config.start) {
					player.load();
					mediaElement.addEventListener('canplay', function() {
						try {
							mediaElement.removeEventListener('canplay');// Firefox reload
							player.setCurrentTime(config.start);
							player.play();
							if(!config.autostart) {
								setTimeout(function() { player.pause(); }, 100)
							}
						} catch(e) {
							if(window.console) console.log(e);
						}
	                });
				} else if(config.autostart) {
					try {
						player.load();
						player.play();
					} catch(e) {
						if(window.console) console.log(e);
					}
				}
			}
		};

		var mimeType = null;
		var extension = config.file.split('.').pop().toLowerCase().split('&').shift();
		if(config.provider == 'sound') {
			if(extension == 'mp3') {
				mimeType = "audio/mp3";
			} else if(extension == 'aac') {
				mimeType = "audio/aac";
			} else if(extension == 'm4a') {
				mimeType = "audio/mp4";
			}
		} else if(config.provider == 'youtube') {
			mimeType = "video/youtube";
		} else if(config.provider == 'vimeo') {
			mimeType = "video/vimeo";
		} else if(config.provider == 'rtmp') {
			meConfig.flashStreamer = config.streamer;
			mimeType = "video/rtmp";
		} else if(config.provider == "http") {
			config.enablePseudoStreaming = true;
			if(extension == 'flv') {
				mimeType = "video/flv";
				meConfig.renderers = ['flash_video','native_flv'];
			} else {
				mimeType = "video/mp4";
			}
		} else {
			if(extension == 'flv') {
				mimeType = "video/flv";
				meConfig.renderers = ['flash_video','native_flv'];
			} else if(extension == 'f4v') {
				mimeType = "video/flv";
			} else if(extension == 'mp4') {
				mimeType = "video/mp4";
			} else if(extension == 'm4v') {
				mimeType = "video/mp4";
			} else if(extension == 'm3u8') {
				mimeType = "application/x-mpegURL";
			} else if(extension == 'aac') {
				mimeType = "audio/mp4";
				config.provider = "sound";
			} else if(extension == 'mp3') {
				mimeType = "audio/mp3";
				config.provider = "sound";
			} else if(extension == 'm4a') {
				mimeType = "audio/mp4";
				config.provider = "sound";
			} else if(config.file.indexOf('vimeo.com') > -1) {
				mimeType = "video/vimeo";
			} else if(config.file.indexOf('youtube.com') > -1 || config.file.indexOf('youtu.be') > -1 || config.file.indexOf('youtube.be') > -1) {
				mimeType = "video/youtube";
			} else if(extension.indexOf('mp4?') == 0) {
				mimeType = "video/mp4";
			} else if(config.file.indexOf('openmeetings/recording') > 0) {
				mimeType = "video/mp4";
			} else if(extension.indexOf('webm') == 0) {
				mimeType = "video/webm";
			} else {
				alert('Something go badly wrong!' + config.provider + "  " + extension);
			}
		}

		var content;
		var mediaDomId = domId + '_oo' + Math.floor(Math.random() * 1000000) + 'vid';
		var objectDomId = domId + '_oo' + Math.floor(Math.random() * 1000000) + 'obj';
		if(config.provider == "sound") {
			if(config.height) {
				meConfig.audioHeight = config.height;
			}
			if(config.width) {
				meConfig.audioWidth = config.width;
			}
			content = "<audio id='" + mediaDomId + "' controls='controls' oncontextmenu='return false;'";
			if(typeof config.repeat != 'undefined' && config.repeat) {
				content += " loop='loop'";
			}
			var objContent = "<object id='" + objectDomId + "' type='application/x-shockwave-flash'";
			if(typeof config.height != 'undefined') {
				content += " height='" + config.height + "'";
				objContent += " height='" + config.height + "'";
				meConfig.videoHeight = config.height;
			}
			if(typeof config.width != 'undefined') {
				content += " width='" + config.width + "'";
				objContent += " width='" + config.width + "'";
				meConfig.videoWidth = config.width;
			}
			if(typeof config.image != 'undefined') {
				content += " poster='" + config.image + "'";
			}
			content += "><source type='" + mimeType + "' src='" + config.file + "'>";
			
			var flashPlayer = "mediaelement-flash-video.swf";
			if(mimeType == "audio/mp3") {
				flashPlayer = "mediaelement-flash-audio.swf";
			} else if(mimeType == "audio/ogg") {
				flashPlayer = "mediaelement-flash-audio-ogg.swf";
			}
			content += objContent + " data='" + mediaElementBaseUrl + flashPlayer + "'>";
			content += "<param name='movie' value='" + mediaElementBaseUrl + flashPlayer + "' />";
			content += "<param name='flashvars' value='controls=true&amp;";
			if(typeof config.streamer != 'undefined') {
				content += "&amp;streamer=" + config.streamer;
			}
			content += "&amp;file=" + config.file + "' /></object>";
			content += "</audio>";
		} else {
			//controls are mandatory for Safari at least
			content = "<video id='" + mediaDomId + "' controls='controls' preload='none' oncontextmenu='return false;'";
			if(typeof config.repeat != 'undefined' && config.repeat) {
				content += " loop='loop'";
			}
			var objContent = "<object id='" + objectDomId + "' type='application/x-shockwave-flash'";
			if(typeof config.height != 'undefined') {
				meConfig.videoHeight = config.height;
			}
			if(typeof config.width != 'undefined') {
				meConfig.videoWidth = config.width;
			}
			if(typeof config.image != 'undefined') {
				content += " poster='" + config.image + "'";
			}
			content += "><source type='" + mimeType + "' src='" + config.file + "' />";
			
			content += objContent + " data='" + mediaElementBaseUrl + "mediaelement-flash-video.swf'>";
			content += "<param name='movie' value='" + mediaElementBaseUrl + "mediaelement-flash-video.swf' />";
			content += "<param name='flashvars' value='controls=true";
			if(typeof config.streamer != 'undefined') {
				content += "&amp;streamer=" + config.streamer;
			}
			content += "&amp;file=" + config.file + "' /></object></video>";
		}

		var target = jQuery('#' + domId);
		// Set height on target element to auto to support responsive scaling
		// with auto-resize
		target.css({'height' : ''});
		target.css({'border' : 'none'});
		// Set also width to auto in case the video is larger than the window.
		// Normally the max-width on the target does already fix this responsive
		// problem, but this does not work on iOS. 
		// Don't set it permanently to auto because this will expand all videos
		// to 100% and discard the configured width
		if (jQuery(window).width() <= config.width) {
			target.css({'width' : ''});
		}
		
		// Now finally add video tags and flash fallback HTML code to DOM and
		// call player on new video element
		target.html(content);
		
		if(mimeType == "video/vimeo") {
			var mediaElementBaseUrl = BPlayer._mediaElementBaseUrl();
			var vimeoJs = mediaElementBaseUrl + (BPlayer.debugEnabled ? 'renderers/vimeo.js' : 'renderers/vimeo.min.js');
			jQuery.ajax({
				dataType: 'script',
				cache: true,
				async: false, //prevent 2x load of the mediaelement.js which is deadly
				url: vimeoJs
			}).done(function() {
				jQuery('#' + mediaDomId).mediaelementplayer(meConfig);
			});
		} else {
			jQuery('#' + mediaDomId).mediaelementplayer(meConfig);
		}
	},
	
	_mediaElementBaseUrl: function() {
		var mediaElementUrl = BPlayer._findBaseUrl(window);
		if(mediaElementUrl == null) {
			mediaElementUrl = "/olat/raw/_noversion_/";
		}
		mediaElementUrl += "movie/mediaelementjs/";
		return mediaElementUrl;
	},
	
	_isIE8: function() {
		return (jQuery.support.opacity == false);	
	},
	
	_jwPlayerBaseUrl: function() {
		var jwPlayerBaseUrl = BPlayer._findBaseUrl(window);
		if(jwPlayerBaseUrl == null) {
			jwPlayerBaseUrl = "/olat/raw/_noversion_/";
		}
		jwPlayerBaseUrl += "movie/jw/";
		return jwPlayerBaseUrl;
	},
	
	_findBaseUrl: function(win) {
			if (win.o_info) return win.o_info.o_baseURI;
			else if (win.opener) return BPlayer._findBaseUrl(win.opener);
			else if (win.parent) return BPlayer._findBaseUrl(win.parent);
			else return null;
	},
	
	_convertInSeconds: function (time) {
		if(typeof time == 'undefined' || time == null) return 0;//default
		if(!time.length) return time;//already a number
		if(time.length == 0) return 0;
		if(time.indexOf('.') > 0){
			time = time.substring(0, time.indexOf('.'));
		}
	
		var sepIndex = time.lastIndexOf(':');
		if(sepIndex > 0) {
			var chunkSec = time.substring(sepIndex+1,time.length);
			var timeInSec = parseInt(chunkSec);
			time = time.substring(0,sepIndex);

			sepIndex = time.lastIndexOf(':');
			if(sepIndex > 0) {
				var chunkMin = time.substring(sepIndex+1,time.length);
				timeInSec += 60 * parseInt(chunkMin);
			}
			time = time.substring(0,sepIndex);
		
			if(time.length > 0) {
				timeInSec += 60 * 60 * parseInt(time);
			}
			return timeInSec;
		} else return time;
	},
	
	_isOODebug: function(win) {
		if (win.o_info) return win.o_info.debug;
		else if (win.opener) return BPlayer._isOODebug(win.opener);
		else if (win.parent) return BPlayer._isOODebug(win.parent);
		else return false;		
	}
};

// init debug flag only once
BPlayer.debugEnabled = BPlayer._isOODebug(window);