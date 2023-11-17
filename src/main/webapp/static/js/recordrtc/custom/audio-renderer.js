class AudioRenderer {

	constructor(config) {
		this.config = config;
		this.audioContext = null;
		this.source = null;
		this.analyser = null;
		this.canvas = config.canvas;
		this.renderingLive = false;
		this.currentTime = 0;
		this.scaleFactorX = 1;
		this.scaleFactorValue = 1;
		this.padding = 10;
		this.barWidth = 2;
		this.barGap = 1;
		this.duration = 0;
		this.waveColor = '#bbb';
		this.waveHighlightColor = '#888';
		this.waveRecordingColor = '#f88';
		this.scaleFactorY = 0.6;
		this.spectumMode = true;

		this.initCanvas();
	}

	initCanvas() {
		this.canvasCtx = this.canvas.getContext('2d');
		const width = this.canvas.offsetWidth;
		const height = this.canvas.offsetHeight;
		this.canvas.width = width;
		this.canvas.height = height;
		if (this.canvas.width > 800) {
			this.padding = 20;
		} else if (this.canvas.width > 500) {
			this.padding = 15;
		}
		this.canvasCtx.clearRect(0, 0, this.canvas.width, this.canvas.height);
	}

	mediaStreamReady(mediaStream) {
		if (this.spectumMode) {
			this.createFrequencyVisualizer(mediaStream);
		} else {
			this.createOscilloscopeVisualizer(mediaStream);
		}
	}

	blobReady(blob) {
		this.createTimeVisualizer(blob);
	}

	async createTimeVisualizer(blob) {
		const arrayBuffer = await blob.arrayBuffer();
		this.audioContext = new AudioContext({
			sampleRate: 8000
		});
		const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer);
		this.audioContext.close();
		this.audioContext = null;
		this.channelData = audioBuffer.getChannelData(0);
		this.duration = audioBuffer.duration;
		this.measureSamples(this.channelData);
		this.drawSamples(this.channelData);
	}

	measureSamples(channelData) {
		const availableWidth = this.canvas.width - 2 * this.padding;
		const numberOfBars = availableWidth / (this.barWidth + this.barGap);

		let max = 0.0;
		this.scaleFactorX = channelData.length / numberOfBars;
		for (let i = 0; i < numberOfBars; i++) {
			let index = Math.min(Math.floor(i * this.scaleFactorX), channelData.length - 1);
			let value = Math.abs(channelData[index]);
			max = Math.max(value, max);
		}

		this.scaleFactorValue = max ? 1 / max : 1;
	}

	drawSamples(channelData) {
		this.drawSamplesInRange(channelData, null, null);
	}

	drawSamplesInRange(channelData, from, to) {
		const rangeSpecified = from !== null && to !== null;

		if (rangeSpecified && from === to) {
			return;
		}

		const availableWidth = this.canvas.width - 2 * this.padding;
		const availableHeight = this.canvas.height - 2 * this.padding;
		const numberOfBars = availableWidth / (this.barWidth + this.barGap);

		this.canvasCtx.fillStyle = rangeSpecified ?
			this.waveHighlightColor : this.waveColor;

		let fromBar = 0;
		let toBar = numberOfBars - 1;

		if (rangeSpecified) {
			fromBar = Math.round(from / this.duration * numberOfBars);
			toBar = Math.round(to / this.duration * numberOfBars);
		}

		if (rangeSpecified) {
			const x = this.padding + (fromBar / numberOfBars * availableWidth);
			const width = (toBar - fromBar) / numberOfBars * availableWidth;
			this.canvasCtx.fillRect(x, 0.5 * this.canvas.height, width, 1);
		} else {
			this.canvasCtx.fillRect(this.padding, 0.5 * this.canvas.height, availableWidth, 1);
		}

		let x = this.padding;
		for (let i = 0; i < numberOfBars; i++) {
			if (i >= fromBar && i <= toBar) {
				this.drawSample(channelData, i, availableHeight, x);
			}
			x += this.barWidth + this.barGap;
		}
	}

	drawSample(channelData, barIndex, availableHeight, x) {
		const dataIndex = Math.min(Math.floor(barIndex * this.scaleFactorX), channelData.length - 1);
		const value = Math.abs(channelData[dataIndex]);
		const logValue = Math.log10(value);
		const transformedLogValue = logValue * 0.3333 + 1;
		const clippedValue = Math.max(transformedLogValue, 0);
		const barHeight = availableHeight * clippedValue * this.scaleFactorY;
		if (barHeight > 0) {
			this.canvasCtx.fillRect(x, 0.5 * (this.canvas.height - barHeight), this.barWidth, barHeight + 1);
		}
	}

	readyForPlayback() {
		this.currentTime = 0;
	}

	createFrequencyVisualizer(mediaStream) {
		const availableWidth = this.canvas.width - 2 * this.padding;
		const availableHeight = this.canvas.height - 2 * this.padding;
		const numberOfBars = Math.floor(0.5 * availableWidth / (this.barWidth + this.barGap));

		this.canvasCtx.clearRect(0, 0, this.canvas.width, this.canvas.height);

		this.audioContext = new AudioContext();
		this.source = this.audioContext.createMediaStreamSource(mediaStream);
		this.analyser = this.audioContext.createAnalyser();
		this.source.connect(this.analyser);
		this.analyser.fftSize = 512;
		const bufferLength = this.analyser.frequencyBinCount;
		const dataArray = new Uint8Array(bufferLength);

		let scaleX = dataArray.length / numberOfBars;

		this.renderingLive = true;
		this.animationId = null;
		this.liveRecordingMode = false;

		const self = this;

		const drawSpectrum = () => {
			if (!self.renderingLive) {
				return;
			}
			self.analyser.getByteFrequencyData(dataArray);
			self.canvasCtx.clearRect(0, 0, self.canvas.width, self.canvas.height);

			let barHeight;
			let x = 0;
			const xCenter = Math.floor(this.padding + availableWidth / 2);

			for (let i = 0; i < numberOfBars; i++) {
				let index = Math.min(Math.floor(i * scaleX), dataArray.length - 1);
				let value = Math.abs(dataArray[index] / 256);
				let barHeight = availableHeight * value;
				if (self.liveRecordingMode) {
					self.canvasCtx.fillStyle = self.waveRecordingColor;
				} else {
					self.canvasCtx.fillStyle = self.waveColor
				}
				self.canvasCtx.fillRect(xCenter + x, 0.5 * (self.canvas.height - barHeight), self.barWidth, barHeight);
				self.canvasCtx.fillRect(xCenter - x - self.barWidth - self.barGap, 0.5 * (self.canvas.height - barHeight), self.barWidth, barHeight);
				x += self.barWidth + self.barGap;
			}

			self.animationId = requestAnimationFrame(drawSpectrum);
		};

		drawSpectrum();
	}

	createOscilloscopeVisualizer(mediaStream) {
		const availableWidth = this.canvas.width - 2 * this.padding;
		const availableHeight = this.canvas.height - 2 * this.padding;
		const numberOfBars = availableWidth / (this.barWidth + this.barGap);

		this.canvasCtx.clearRect(0, 0, this.canvas.width, this.canvas.height);

		this.audioContext = new AudioContext();
		this.source = this.audioContext.createMediaStreamSource(mediaStream);
		this.analyser = this.audioContext.createAnalyser();
		this.source.connect(this.analyser);
		const dataArray = new Uint8Array(this.analyser.frequencyBinCount);

		let scaleX = dataArray.length / numberOfBars;

		this.renderingLive = true;
		this.animationId = 0;
		this.liveRecordingMode = false;

		const self = this;

		const segments = 1;
		const realNumberOfBars = Math.floor(numberOfBars / segments);

		const drawScope = () => {
			if (!self.renderingLive) {
				return;
			}
			self.analyser.getByteTimeDomainData(dataArray);
			self.canvasCtx.clearRect(0, 0, self.canvas.width, self.canvas.height);

			const segmentNumber = self.animationId % segments;

			let barHeight;
			let x = self.padding + (self.barWidth + self.barGap) * realNumberOfBars * segmentNumber;
			for (let i = 0; i < realNumberOfBars; i++) {
				let index = Math.min(Math.floor(i * scaleX), dataArray.length - 1);
				let value = Math.abs(dataArray[index] - 128) / 128.0;
				let barHeight = availableHeight * value * this.scaleFactorY;
				if (self.liveRecordingMode) {
					self.canvasCtx.fillStyle = self.waveRecordingColor;
				} else {
					self.canvasCtx.fillStyle = self.waveColor;
				}
				self.canvasCtx.fillRect(x, 0.5 * (self.canvas.height - barHeight), self.barWidth, barHeight);
				x += self.barWidth + self.barGap;
			}

			self.animationId = requestAnimationFrame(drawScope);
		};

		drawScope();
	}

	record() {
		this.liveRecordingMode = true;
	}

	stopRecording() {
		this.renderingLive = false;
		this.canvasCtx.clearRect(0, 0, this.canvas.width, this.canvas.height);

		if (this.animationId) {
			cancelAnimationFrame(this.animationId);
		}
		if (this.source) {
			this.source.disconnect();
		}
		if (this.audioContext) {
			this.audioContext.close();
			this.audioContext = null;
		}
	}

	setCurrentTime(currentTime) {
		if (!this.channelData) {
			return;
		}

		const from = this.currentTime;
		const to = currentTime;

		if (to > from && (to - from) < 0.01) {
			return;
		}

		this.currentTime = to;

		if (to >= from) {
			this.drawSamplesInRange(this.channelData, from, to);
		} else {
			this.currentTime = 0;
			this.drawSamples(this.channelData);
		}
	}
}