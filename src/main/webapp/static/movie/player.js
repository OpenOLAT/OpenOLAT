var BPlayer = {
	insertPlayer: function (address,domId,width,height,start,duration,provider,streamer,autostart,repeat,controlbar,poster) {
		BPlayer.insertHTML5Player(address,domId,width,height,start,duration,provider,streamer,autostart,repeat,controlbar,poster);
	},
	
	playSound : function(soundUrl, domId) {
		jQuery.getScript(BPlayer.playerJsUrl(), function() {
			if(!jwplayer.utils.isIE()) {
				var playerUrl = BPlayer.playerUrl();
				var args = {
					file:soundUrl,
					start:0,
					autostart:true,
					repeat:'none',
					controlbar:'none',
					controls: false,
					width: '1px',
					height: '1px',
					icons:false,
					showicons:false,
					flashplayer:playerUrl
				};
				jwplayer(domId).setup(args);
			}
		});
	},

	insertHTML5Player : function (address,domId,width,height,start,duration,provider,streamer,autostart,repeat,controlbar,poster) {
		var videoUrl = address
		if(address.indexOf('://') < 0 && (address.indexOf('/secstatic/qtieditor/') >= 0 || address.indexOf('/secstatic/qti/') >= 0)) {
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
		
		var playerUrl = BPlayer.playerUrl();
		var args = {
			file:videoUrl,
			width:width,
			height:height,
			controlbar: { position: "bottom" },
			flashplayer:playerUrl
		};
		
		if(provider != undefined) {
			args.provider = provider;
		}
		if(provider == "rtmp" || provider == "http") {
			args.streamer = streamer;
		}
		if(start != undefined) {
			var startInSec = BPlayer.convertInSeconds(start);
			if(startInSec > 0) {
				args.start = startInSec;
			}
		}
		if(duration != undefined) {
			var durationInSec = BPlayer.convertInSeconds(duration);
			if(durationInSec > 0) {
				args.duration = durationInSec;
			}
		}
		if(autostart) {
			args.autostart = true;
		}
		if(repeat) {
			args.repeat = "single";
		}
		if(controlbar != undefined && !controlbar) {
			args.controlbar = "none";
		}
		if(poster) {
			args.image = poster;
		}
		
		var realDomId;
		if(BPlayer.isIE8() && domId != 'prev_container' && jQuery('#' + domId).is("span")) {
			var spanEl = jQuery('#' + domId);
			var width = spanEl.width();
			var height = spanEl.height();
			var videoParent = jQuery(spanEl).parent('p');
			var newContainer = jQuery('<div id="' + domId + '_replacer" class="olatFlashMovieViewer" style="display:block;border:solid 1px #000; width:' + width + 'px; height:' + height + 'px;">Hello world</div>');
			newContainer.insertAfter(videoParent);
			spanEl.remove();
			realDomId = domId + '_replacer';
		} else {
			realDomId = domId;
		}
		
		jQuery.getScript(BPlayer.playerJsUrl(), function() {
			jwplayer(domId).setup(args);
		});
	},
	
	isIE8: function() {
		return (jQuery.support.opacity == false);	
	},
	
	playerUrl: function() {
		var playerUrl = BPlayer.findBaseUrl(window);
		if(playerUrl == null) {
			playerUrl = "/olat/raw/_noversion_/";
		}
		playerUrl += "movie/tinyMCE/movieViewer.swf";
		return playerUrl;
	},
	
	playerJsUrl: function() {
		var playerUrl = BPlayer.findBaseUrl(window);
		if(playerUrl == null) {
			playerUrl = "/olat/raw/_noversion_/";
		}
		playerUrl += "movie/player.jw.js";
		return playerUrl;
	},
	
	findBaseUrl: function(win) {
			if (win.o_info) return win.o_info.o_baseURI;
			else if (win.opener) return BPlayer.findBaseUrl(win.opener);
			else if (win.parent) return BPlayer.findBaseUrl(win.parent);
			else return null;
	},
	
	convertInSeconds: function (time) {
		if(time == null || typeof(time) == undefined) return 0;//default
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
	}
};