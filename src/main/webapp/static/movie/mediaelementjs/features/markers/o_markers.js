/*!
 * MediaElement.js
 * http://www.mediaelementjs.com/
 *
 * Wrapper that mimics native HTML5 MediaElement (audio and video)
 * using a variety of technologies (pure JavaScript, Flash, iframe)
 *
 * Copyright 2010-2017, John Dyer (http://j.hn/)
 * License: MIT
 *
 */(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
'use strict';

Object.assign(mejs.MepDefaults, {
	markerColor: '#E9BC3D',

	markerWidth: 1,

	markers: [],
	markerCallback: function markerCallback() {}
});

Object.assign(MediaElementPlayer.prototype, {
	buildmarkers: function buildmarkers(player, controls, layers, media) {
		if (!player.options.markers.length) {
			return;
		}

		var t = this,
		    currentPos = -1,
		    currentMarker = -1,
		    lastPlayPos = -1,
		    lastMarkerCallBack = -1;

		for (var i = 0, total = player.options.markers.length; i < total; ++i) {
			var marker = document.createElement('span');
			marker.className = t.options.classPrefix + 'time-marker';
			controls.querySelector('.' + t.options.classPrefix + 'time-total').appendChild(marker);
		}

		media.addEventListener('durationchange', function () {
			player.setmarkers(controls);
		});
		media.addEventListener('loadedmetadata', function () {// for external video like YouTube
			player.setmarkers(controls);
		});
		media.addEventListener('timeupdate', function () {
			currentPos = Math.floor(media.currentTime);
			if (lastPlayPos > currentPos) {
				if (lastMarkerCallBack > currentPos) {
					lastMarkerCallBack = -1;
				}
			} else {
				lastPlayPos = currentPos;
			}

			if (player.options.markers.length) {
				for (var _i = 0, _total = player.options.markers.length; _i < _total; ++_i) {
					currentMarker = Math.floor(player.options.markers[_i].time);
					if (currentPos === currentMarker && currentMarker !== lastMarkerCallBack) {
						player.options.markerCallback(media, media.currentTime, player.options.markers[_i].id);
						lastMarkerCallBack = currentMarker;
					}
				}
			}
		}, false);
	},
	rebuildmarkers: function buildmarkers(player, markers) {
		player.options.markers = markers;
		player.buildmarkers(player, player.controls, null, player.media);
		player.setmarkers(player.controls);
	},
	clearmarkers: function clearmarkers(player) {
		var markers = jQuery('.' + player.options.classPrefix + 'time-marker', player.controls);
		for(var i=markers.length; i-->0; ) {
			markers.remove();
		}
	},
	setmarkers: function setmarkers(controls) {
		
		var t = this,
		    markers = controls.querySelectorAll('.' + t.options.classPrefix + 'time-marker');
		if (markers.length === 0) {
			return;
		}
		for (var i = 0, total = t.options.markers.length; i < total; ++i) {
			if (Math.floor(t.options.markers[i].time) <= t.media.duration && Math.floor(t.options.markers[i].time) >= 0) {
				var left = 100 * Math.floor(t.options.markers[i].time) / t.media.duration,
				    marker = markers[i];

				marker.style.width = t.options.markerWidth + 'px';
				marker.style.left = left + '%';
				if(!t.options.markers[i].showInTimeline) {
					marker.style.visibility = 'hidden';
				}
				if(t.options.markers[i].color == null || t.options.markers[i].color.length == 0) {
					marker.style.background = t.options.markers[i].color;
				} else {
					var color = t.options.markers[i].color;
					if(color.indexOf('#') == 0 || color.indexOf('rgb(') == 0 || color.indexOf('rgba(') == 0) {
						marker.style.background = color;
					} else {
						jQuery(marker).addClass(color);
					}
				}
			}
		}
	}
});

},{}]},{},[1]);
