class AudioRecorder {

	constructor(audioElement, config) {
		this.audioElement = audioElement;
		this.config = config;
		this.avService = new AvService(config);
		this.avUserInterface = new AvUserInterface(audioElement, config);
		this.state = null;
		this.mediaStream = null;
		this.recorder = null;
		this.blobs = null;
		this.endedHandler = null;
		this.timeupdateHandler = null;
		this.oneButtonHandler = null;
		if (this.config.audioRendererActive) {
			this.msPerFrame = null;
			this.renderer = new AudioRenderer(config);
		}
	}

	sizeContainer() {
		const container = jQuery('#avContainer');
		container.width('100%');
		container.height('3em');
		if (!this.config.audioRendererActive) {
			container.css('margin-top', '1em');
		}
	}

	mimeType() {
		return isSafari ? 'audio/mp4' : 'audio/webm';
	}

	stateInit() {
		this.state = RecState.init;
		this.avUserInterface.init();
		this.updateUI();
		this.getMediaStream();
		this.sizeContainer();
	}

	stateToWaitingToRecord() {
		if (this.state !== RecState.init) {
			return;
		}

		this.state = RecState.waitingToRecord;
		this.mute();
		this.updateUI();
	}

	handleOneButton() {
		switch (this.state) {
			case RecState.waitingToRecord:
				this.createRecorder();
				if (this.canRecord()) {
					this.state = RecState.recording;
					this.startRecording();
					this.updateUI();
				}
				break;
			case RecState.recording:
				this.state = RecState.stoppedRecording;
				this.stopRecording();
				this.updateUI();
				break;
			case RecState.stoppedRecording:
				this.state = RecState.playing;
				this.startPlaying();
				this.updateUI();
				break;
			case RecState.playing:
				this.state = RecState.stopped;
				this.stopPlaying();
				this.updateUI();
				break;
			case RecState.stopped:
				this.state = RecState.playing;
				this.startPlaying();
				this.updateUI();
				break;
			default:
				break;
		}
	}

	updateUI() {
		const recordingIndicator = jQuery('#recording-container');
		const oneButton = jQuery('#one-button');
		const startRecording = jQuery('#start-recording-symbol').add('#start-recording-text');
		const stopRecording = jQuery('#stop-recording-symbol').add('#stop-recording-text');
		const play = jQuery('#play-symbol').add('#play-text');
		const stop = jQuery('#stop-symbol').add('#stop-text');
		const retryButton = jQuery('#retry-button');
		const confirmButton = jQuery('.o_av_confirm_button');
		const qualityDropdown = jQuery('#o_fiovideo_audio_quality_SELBOX');
		const startRecordingInfo = jQuery('#o_start_recording_info');
		const volumeButton = jQuery('#volume-button');
		const fileSize = jQuery('#file-size');

		const self = this;

		oneButton.blur();

		switch (this.state) {
			case RecState.init:
				this.avUserInterface.hideTimeContainer();
				this.avUserInterface.hideRecordingLengthLimit();
				jQuery(this.audioElement).show();
				recordingIndicator.hide();
				qualityDropdown.hide();
				oneButton.hide();
				startRecording.hide();
				startRecordingInfo.hide();
				stopRecording.hide();
				volumeButton.show();
				play.hide();
				stop.hide();
				retryButton.hide();
				confirmButton.hide();

				jQuery('#time-rail').hide();
				fileSize.hide();

				if (this.oneButtonHandler === null) {
					this.oneButtonHandler = () => {
						self.handleOneButton();
					};
					oneButton.click(this.oneButtonHandler);
				}

				jQuery('#yes-no-dialog').hide();
				break;
			case RecState.waitingToRecord:
				oneButton.show();
				startRecording.show();
				startRecordingInfo.show();
				this.avUserInterface.hideTotalTime();
				fileSize.show();
				this.updateSize(0);
				break;
			case RecState.recording:
				this.avUserInterface.showTimeContainer();
				this.avUserInterface.showRecordingLengthLimitIfApplicable();
				recordingIndicator.show();
				startRecording.hide();
				startRecordingInfo.hide();
				stopRecording.show();
				volumeButton.hide();
				break;
			case RecState.stoppedRecording:
				recordingIndicator.hide();
				stopRecording.hide();
				volumeButton.show();
				play.show();
				retryButton.show();
				retryButton.click(() => {
					self.retryDialog();
				});
				retryButton.prop('disabled', false);
				confirmButton.show();
				confirmButton.prop('disabled', false);
				if (this.endedHandler) {
					this.audioElement.removeEventListener('ended', this.endedHandler);
					this.endedHandler = null;
				}
				if (this.timeupdateHandler) {
					this.audioElement.removeEventListener('timeupdate', this.timeupdateHandler);
					this.timeupdateHandler = null;
				}
				break;
			case RecState.playing:
				play.hide();
				stop.show();
				retryButton.prop('disabled', true);
				confirmButton.prop('disabled', true);
				break;
			case RecState.stopped:
				stop.hide();
				play.show();
				retryButton.show();
				retryButton.click(() => {
					self.retryDialog();
				});
				retryButton.prop('disabled', false);
				confirmButton.show();
				confirmButton.prop('disabled', false);
				if (this.endedHandler) {
					this.audioElement.removeEventListener('ended', this.endedHandler);
					this.endedHandler = null;
				}
				if (this.timeupdateHandler) {
					this.audioElement.removeEventListener('timeupdate', this.timeupdateHandler);
					this.timeupdateHandler = null;
				}
				break;
			default:
				break;
		}
	}

	retryDialog() {
		var self = this;

		jQuery('#retry-button').blur();
		jQuery('#yes-no-dialog').show();
		jQuery('#no-button').click(() => {
			jQuery('#yes-no-dialog').hide();
		});
		jQuery('#yes-button').click(() => {
			jQuery('#yes-no-dialog').hide();
			self.retryRecording();
		});
	}

	retryRecording() {
		jQuery('#retry-button').blur();
		if (this.state === RecState.stopped || this.state === RecState.stoppedRecording) {
			this.dispose();
			this.stateInit();
		}
	}

	getMediaStream() {
		const self = this;

		navigator.mediaDevices.getUserMedia({
			audio: {
				echoCancellation: true
			}
		}).then((mediaStream) => {
			console.log('Successfully captured mediaStream', mediaStream);
			self.mediaStream = mediaStream;
			self.mediaStreamReady(mediaStream);
			if (self.config.audioRendererActive) {
				self.renderer.mediaStreamReady(mediaStream);
			}
		}).catch((error) => {
			console.error(error);
			alert('Unable to capture your microphone. Please check console logs.');
		});
	}

	mediaStreamReady(mediaStream) {
		this.audioElement.srcObject = mediaStream;
		this.mute();
		const self = this;
		setTimeout(() => {
			self.stateToWaitingToRecord();
		}, 1000);
	}

	mute() {
		this.avUserInterface.setVolume(0);
		this.avUserInterface.setMuted();
	}

	createRecorder() {
		if (this.mediaStream === null) {
			console.log('No media stream available');
			return;
		}

		if (this.recorder) {
			console.log('Recorder already created');
			return;
		}

		const self = this;

		this.recorder = new RecordRTC(this.mediaStream, {
			mimeType: this.mimeType(),
			timeSlice: 1000,
			audioBitsPerSecond: 128000,
			ondataavailable: (blob) => {
				if (self.blobs === null) {
					self.blobs = [];
				}
				self.blobs.push(blob);
				let size = 0;
				self.blobs.forEach((b) => {
					size += b.size;
				});

				self.avUserInterface.updateTimer();
				self.updateSize(size);
			}
		});

		if (this.config.recordingLengthLimit) {
			this.recorder.setRecordingDuration(this.config.recordingLengthLimit, () => {
				self.state = RecState.stoppedRecording;
				self.updateUI();
				self.avUserInterface.updateTotalTime();
				self.stopRecordingCallback();
			});
		}
	}

	updateSize(size) {
		const sizeStr = bytesToSize(size);
		jQuery('#file-size').text(sizeStr);
	}

	canRecord() {
		return this.mediaStream && this.recorder;
	}

	startRecording() {
		if (!this.canRecord()) {
			console.log('No media stream or recorder available');
			return;
		}

		this.mute();

		this.recorder.startRecording();

		this.avUserInterface.startTimer();
		if (this.config.audioRendererActive) {
			this.renderer.record();
		}
	}

	stopRecording() {
		if (this.recorder === null) {
			console.log('Cannot stop recording. No recorder available.')
			return;
		}
		this.avUserInterface.updateTotalTime();
		const self = this;
		this.recorder.stopRecording(() => {
			self.stopRecordingCallback();
		});
	}

	startPlaying() {
		const self = this;

		if ((this.audioElement.currentTime * 1000 + 100) >= this.avUserInterface.getTotalTimeInMsec()) {
			this.audioElement.currentTime = 0;
			this.avUserInterface.setCurrentTime(0);
		}
		this.audioElement.play();

		this.timeupdateHandler = () => {
			self.avUserInterface.setCurrentTime(self.audioElement.currentTime * 1000);
			if (self.config.audioRendererActive) {
				self.renderer.setCurrentTime(self.audioElement.currentTime);
			}
		};
		this.audioElement.addEventListener('timeupdate', this.timeupdateHandler);

		this.endedHandler = () => {
			self.state = RecState.stopped;
			self.updateUI();
			self.avUserInterface.setCurrentTimeToTotalTime();
		}
		this.audioElement.addEventListener('ended', this.endedHandler);

		this.initMsPerFrame();
		if (this.msPerFrame === null) {
			return;
		}

		const t = this;
		setTimeout(() => {
			t.handlePlaying();
		}, this.msPerFrame);
	}

	stopPlaying() {
		this.audioElement.pause();
	}

	stopRecordingCallback() {
		this.audioElement.src = this.audioElement.srcObject = null;
		this.avUserInterface.setUnmuted();
		this.avUserInterface.setVolume(0.75);
		this.audioElement.src = URL.createObjectURL(this.recorder.getBlob());
		this.audioElement.pause();
		this.audioElement.currentTime = 0;
		this.avUserInterface.setCurrentTime(0);

		this.mediaStream.stop();

		this.avService.storeRecording({recorder: this.recorder});

		jQuery('#time-rail').show();
		this.avUserInterface.showTotalTime();
		this.avUserInterface.hideRecordingLengthLimit();

		if (this.config.audioRendererActive) {
			this.renderer.stopRecording();
			this.renderer.blobReady(this.recorder.getBlob());
			this.renderer.readyForPlayback();
		}
	}

	destroyRecorder() {
		if (this.recorder === null) {
			return;
		}
		this.recorder.destroy();
		this.recorder = null;
	}

	turnOffAudioElement() {
		this.audioElement.src = this.audioElement.srcObject = null;
	}

	dispose() {
		this.blobs = null;
		this.destroyRecorder();
		this.releaseMediaStream();
		if (this.config.audioRendererActive) {
			this.renderer.stopRecording();
		}
	}

	releaseMediaStream() {
		if (this.mediaStream === null) {
			return;
		}

		const tracks = this.mediaStream.getTracks();
		tracks.forEach((track) => {
			track.stop();
		});

		this.turnOffAudioElement();
	}

	initMsPerFrame() {
		this.msPerFrame = null;
		const duration = this.audioElement.duration;
		if (!duration) {
			return;
		}

		if (duration < 10) {
			this.msPerFrame = 20;
		} else if (duration < 30) {
			this.msPerFrame = 50;
		} else if (duration < 120) {
			this.msPerFrame = 100;
		}
	}

	handlePlaying() {
		if (this.audioElement && this.audioElement.readyState === 4 && !this.audioElement.paused &&
			!this.audioElement.ended) {
			this.renderer.setCurrentTime(this.audioElement.currentTime);
			this.updateTimeBar();
			const t = this;
			setTimeout(() => {
				t.handlePlaying();
			}, this.msPerFrame);
		}
	}

	updateTimeBar() {
		this.avUserInterface.setCurrentTime(this.audioElement.currentTime * 1000);
	}
}