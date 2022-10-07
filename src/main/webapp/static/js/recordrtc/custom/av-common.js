class RecState {
	static init = new RecState('init')
	static waitingToRecord = new RecState('waitingToRecord')
	static recording = new RecState('recording')
	static stopped = new RecState('stopped')
	static playing = new RecState('playing')

	constructor(name) {
		this.name = name;
	}
}

class AvService {
	constructor(config) {
		this.config = config;
	}

	fileName(fileExtension) {
		const d = new Date();
		const year = d.getFullYear();
		const month = d.getMonth() + 1;
		const day = d.getDate();
		const hours = d.getHours();
		const minutes = d.getMinutes();
		const seconds = d.getSeconds();
		const firstName = (this.config.firstName ? this.config.firstName : 'anonymous').replace(/[\W_]+/g,'-');
		const lastName = (this.config.lastName ? this.config.lastName : 'anonymous').replace(/[\W_]+/g,'-');
		return firstName + '-' + lastName + '-' +
			year + '-' + (month < 10 ? '0' : '') + month + '-' + (day < 10 ? '0' : '') + day + '-' +
			(hours < 10 ? '0' : '') + hours + '-' + (minutes < 10 ? '0' : '') + minutes + '-' + (seconds < 10 ? '0' : '') + seconds +
			'.' + fileExtension;
	}

	randomString() {
		if (window.crypto && window.crypto.getRandomValues && navigator.userAgent.indexOf('Safari') === -1) {
			const a = window.crypto.getRandomValues(new Uint32Array(3));
			let token = '';
			for (var i = 0, l = a.length; i < l; i++) {
				token += a[i].toString(36);
			}
			return token;
		} else {
			return (Math.random() * new Date().getTime()).toString(36).replace(/\./g, '');
		}
	}

	extensionFromType(type) {
		if (type.startsWith('video/webm')) {
			return 'webm';
		}
		if (type.startsWith('video/x-matroska')) {
			return 'mkv';
		}
		if (type.startsWith('video/mp4')) {
			return 'mp4';
		}

		if (type.startsWith('audio/mp3')) {
			return 'mp3';
		}
		if (type.startsWith('audio/mp4')) {
			return 'm4a';
		}
		if (type.startsWith('audio/ogg')) {
			return 'ogg';
		}
		if (type.startsWith('audio/webm')) {
			return 'weba';
		}

		return 'bin';
	}

	storeRecording({recorder}) {
		if (recorder) {
			const blob = recorder.getBlob();
			const type = blob.type;
			const fileExtension = this.extensionFromType(type);
			const fileName = this.fileName(fileExtension);
			const file = new File([blob], fileName, {
				type: type
			});

			o_setExtraMultipartFormData('avRecording', file);
		}
	}

	storePosterImage({imageCaptureBlob}) {
		if (imageCaptureBlob) {
			let posterFile = new File([imageCaptureBlob], "poster", {
				type: imageCaptureBlob.mimeType
			});
			o_setExtraMultipartFormData('posterImage', posterFile);
		}
	}
}

class AvUserInterface {
	constructor(avElement, config) {
		this.avElement = avElement;

		this.config = config;

		this.volumeButtonContainer = jQuery('#volume-button-container');
		this.volumeButton = jQuery('#volume-button');
		this.volumeButtonHandler = null;
		this.volumeSlider = jQuery('#volume-slider');
		this.volumeSliderHandler = null;
		this.volumeHandle = jQuery('#volume-handle');
		this.currentVolume = jQuery('#current-volume');

		this.startTime = null;
		this.totalTime = null;
		this.timeContainer = jQuery('#time-container');
		this.timeSlider = jQuery('#time-slider');
		this.timeSliderHandler = null;
		this.currentTimeText = jQuery('#current-time');
		this.currentTimeRail = jQuery('#current-time-rail');
		this.totalTimeText = jQuery('#total-time');
		this.recordingLengthLimitText = jQuery('#recording-length-limit');
	}

	init() {
		const self = this;

		if (this.config.compact) {
			this.volumeSlider.css('transform', 'matrix(0, 1, -1, 0, 40, 43)');
		}

		if (this.volumeButtonHandler == null) {
			this.volumeButtonHandler = (event) => {
				event.preventDefault();
				if (self.avElement.muted) {
					return;
				}
				self.volumeSlider.toggle();
			}
			this.volumeButton.click(this.volumeButtonHandler);
		}

		if (this.volumeSliderHandler == null) {
			this.volumeSliderHandler = (event) => {
				event.preventDefault();
				if (self.avElement.muted) {
					return;
				}
				const pageX = event.pageX;
				const pageY = event.pageY;
				const offsetX = self.volumeSlider.offset().left;
				const offsetY = self.volumeSlider.offset().top;
				if (self.config.compact) {
					const level = Math.max(0, Math.min(1, (pageX - offsetX - 10) / 100));
					self.setVolume(level);
				} else {
					const level = Math.max(0, Math.min(1, 1 - (pageY - offsetY - 10) / 100));
					self.setVolume(level);
				}
			}
			this.volumeSlider.click(this.volumeSliderHandler);
		}

		if (this.timeSliderHandler == null) {
			this.timeSliderHandler = (event) => {
				event.preventDefault();
				const pageX = event.pageX;
				const offsetX = self.timeSlider.offset().left;
				const width = self.timeSlider.width();
				const s = Math.max(0, Math.min(1, (pageX - offsetX) / width));
				if (self.totalTime) {
					const t = self.avElement.duration * s;
					self.avElement.currentTime = t;
					self.setCurrentTime(t * 1000);
				}
			}
			this.timeSlider.click(this.timeSliderHandler);
		}
	}

	setVolume(level) {
		this.avElement.volume = level;
		this.currentVolume.height(`${100 * level}%`);
		this.volumeHandle.css('bottom', `${100 * level}%`);
	}

	setMuted() {
		this.avElement.muted = true;
		this.volumeButtonContainer.addClass('mejs__unmute');
		this.volumeButtonContainer.removeClass('mejs__mute');
		this.volumeSlider.hide();
	}

	setUnmuted() {
		this.avElement.muted = false;
		this.volumeButtonContainer.removeClass('mejs__unmute');
		this.volumeButtonContainer.addClass('mejs__mute');
		this.volumeSlider.hide();
	}

	startTimer() {
		this.startTime = (new Date()).getTime();
		this.updateTimer();
	}

	updateTimer() {
		const t = (new Date()).getTime() - this.startTime;
		this.setCurrentTime(t);
	}

	setCurrentTime(msec) {
		const currentTime = this.formatTime(msec);
		this.currentTimeText.text(currentTime);

		if (this.totalTime) {
			this.currentTimeRail.css('transform', `scaleX(${msec / this.totalTime})`);
		}
	}

	setCurrentTimeToTotalTime() {
		if (this.totalTime) {
			this.setCurrentTime(this.totalTime);
		}
	}

	updateTotalTime() {
		this.totalTime = (new Date()).getTime() - this.startTime;
		this.setTotalTime(this.totalTime);
	}

	setTotalTime(msec) {
		const totalTime = this.formatTime(msec);
		this.totalTimeText.text('/ ' + totalTime);
	}

	getTotalTimeInMsec() {
		return this.totalTime;
	}

	formatTime(msec) {
		const sec = msec / 1000;
		const hours = Math.floor(sec / 3600);
		const minutes = Math.floor((sec - (hours * 3600)) / 60);
		const seconds = Math.floor(sec - (hours * 3600) - (minutes * 60));

		let timeString = '';
		if (hours > 0) {
			if (hours < 10) {
				timeString += '0' + hours + ':';
			} else {
				timeString += hours + ':';
			}
		}
		if (hours > 0 && minutes < 10) {
			timeString += '0';
		}
		timeString += minutes + ':';

		if (seconds < 10) {
			timeString += '0' + seconds;
		} else {
			timeString += seconds;
		}

		return timeString;
	}

	showTimeContainer() {
		this.timeContainer.show();
	}

	hideTimeContainer() {
		this.timeContainer.hide();
	}

	showRecordingLengthLimitIfApplicable() {
		if (this.config.recordingLengthLimit) {
			this.recordingLengthLimitText.show();
			this.recordingLengthLimitText.text('/ ' + this.formatTime(this.config.recordingLengthLimit));
		} else {
			this.recordingLengthLimitText.hide();
		}
	}

	hideRecordingLengthLimit() {
		this.recordingLengthLimitText.hide();
	}

	hideTotalTime() {
		this.totalTimeText.hide();
	}

	showTotalTime() {
		this.totalTimeText.show();
	}
}