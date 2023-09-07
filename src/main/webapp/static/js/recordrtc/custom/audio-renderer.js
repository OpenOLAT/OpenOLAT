class AudioRenderer {

	constructor(config) {
		this.config = config;
		this.audioContext = null;
		this.source = null;
		this.analyser = null;
		this.canvas = config.canvas;
		this.initCanvas();
		this.renderingSpectrum = false;
		this.currentTime = 0;
		this.scaleFactorX = 1;
		this.scaleFactorValue = 1;
		this.padding = 10;
		this.barWidth = 1;
		this.barGap = 1;
		this.duration = 0;
	}

	initCanvas() {
		this.canvasCtx = this.canvas.getContext('2d');
		const width = this.canvas.offsetWidth;
		const height = this.canvas.offsetHeight;
		this.canvas.width = width;
		this.canvas.height = height;
		this.canvasCtx.clearRect(0, 0, this.canvas.width, this.canvas.height);
	}

	mediaStreamReady(mediaStream) {
		this.createFrequencyVisualizer(mediaStream);
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
		const availableWidth = this.canvas.width - 2 * this.padding;
		const availableHeight = this.canvas.height - 2 * this.padding;
		const numberOfBars = availableWidth / (this.barWidth + this.barGap);

		this.canvasCtx.clearRect(0, 0, this.canvas.width, this.canvas.height);
		this.canvasCtx.fillStyle = '#454545';

		let x = this.padding;
		for (let i = 0; i < numberOfBars; i++) {
			let index = Math.min(Math.floor(i * this.scaleFactorX), channelData.length - 1);
			let value = Math.abs(channelData[index]) * this.scaleFactorValue;
			let barHeight = availableHeight * value;
			this.canvasCtx.fillRect(x, 0.5 * (this.canvas.height - barHeight), this.barWidth, barHeight);
			x += this.barWidth + this.barGap;
		}
	}

	drawSamplesInRange(channelData, from, to) {
		const availableWidth = this.canvas.width - 2 * this.padding;
		const availableHeight = this.canvas.height - 2 * this.padding;
		const numberOfBars = availableWidth / (this.barWidth + this.barGap);

		this.canvasCtx.fillStyle = '#d6d6d6';
		const fromBar = Math.round(from / this.duration * numberOfBars);
		const toBar = Math.round(to / this.duration * numberOfBars);

		let x = this.padding;
		for (let i = 0; i < numberOfBars; i++) {
			if (i >= fromBar && i <= toBar) {
				let index = Math.min(Math.floor(i * this.scaleFactorX), channelData.length - 1);
				let value = Math.abs(channelData[index]) * this.scaleFactorValue;
				let barHeight = availableHeight * value;
				this.canvasCtx.fillRect(x, 0.5 * (this.canvas.height - barHeight), this.barWidth, barHeight);
			}
			x += this.barWidth + this.barGap;
		}
	}

	readyForPlayback() {
		this.currentTime = 0;
	}

	createFrequencyVisualizer(mediaStream) {
		const availableWidth = this.canvas.width - 2 * this.padding;
		const availableHeight = this.canvas.height - 2 * this.padding;
		const numberOfBars = availableWidth / (this.barWidth + this.barGap);

		this.canvasCtx.clearRect(0, 0, this.canvas.width, this.canvas.height);

		this.audioContext = new AudioContext();
		this.source = this.audioContext.createMediaStreamSource(mediaStream);
		this.analyser = this.audioContext.createAnalyser();
		this.source.connect(this.analyser);
		this.analyser.fftSize = 512;
		const bufferLength = this.analyser.frequencyBinCount;
		const dataArray = new Uint8Array(bufferLength);

		let scaleX = dataArray.length / numberOfBars;

		this.renderingSpectrum = true;
		this.animationId = null;
		this.spectrumRecordingMode = false;

		const self = this;

		const drawSpectrum = () => {
			if (!self.renderingSpectrum) {
				return;
			}
			self.analyser.getByteFrequencyData(dataArray);
			self.canvasCtx.clearRect(0, 0, self.canvas.width, self.canvas.height);

			let barHeight;
			let x = self.padding;
			for (let i = 0; i < numberOfBars; i++) {
				let index = Math.min(Math.floor(i * scaleX), dataArray.length - 1);
				let value = Math.abs(dataArray[index] / 256);
				let barHeight = availableHeight * value;
				if (self.spectrumRecordingMode) {
					self.canvasCtx.fillStyle = '#d6d6d6';
				} else {
					self.canvasCtx.fillStyle = '#454545';
				}
				self.canvasCtx.fillRect(x, 0.5 * (self.canvas.height - barHeight), self.barWidth, barHeight);
				x += self.barWidth + self.barGap;
			}

			self.animationId = requestAnimationFrame(drawSpectrum);
		};

		drawSpectrum();
	}

	record() {
		this.spectrumRecordingMode = true;
	}

	stopRecording() {
		this.renderingSpectrum = false;
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
		const from = this.currentTime;
		const to = currentTime;
		this.currentTime = to;

		if (to >= from) {
			this.drawSamplesInRange(this.channelData, from, to);
		} else {
			this.currentTime = 0;
			this.drawSamples(this.channelData);
		}
	}
}