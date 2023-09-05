class AudioRenderer {

	constructor(config) {
		this.config = config;
		this.audioContext = null;
		this.source = null;
		this.analyser = null;
		this.canvas = config.canvas;
		this.initCanvas();
		this.visualizing = false;
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

	createFrequencyVisualizer(mediaStream) {
		this.audioContext = new AudioContext();
		this.source = this.audioContext.createMediaStreamSource(mediaStream);
		this.analyser = this.audioContext.createAnalyser();
		this.source.connect(this.analyser);
		this.analyser.fftSize = 256;
		const bufferLength = this.analyser.frequencyBinCount;
		const dataArray = new Uint8Array(bufferLength);

		const padding = 20;

		this.visualizing = true;
		this.animationId = null;
		this.spectrumRecordingMode = false;

		const self = this;

		const availableWidth = this.canvas.width - 2 * padding;
		const barWidth = Math.floor(availableWidth / bufferLength);

		const drawSpectrum = () => {
			if (!self.visualizing) {
				return;
			}
			self.analyser.getByteFrequencyData(dataArray);
			self.canvasCtx.clearRect(0, 0, self.canvas.width, self.canvas.height);

			let barHeight;
			let x = padding;

			for (let i = 0; i < bufferLength; i++) {
				barHeight = dataArray[i];

				if (self.spectrumRecordingMode) {
					self.canvasCtx.fillStyle = `rgb(${barHeight + 100}, 50, 50)`;
				} else {
					self.canvasCtx.fillStyle = 'lightgray';
				}
				self.canvasCtx.fillRect(x, self.canvas.height - barHeight / 2, barWidth, barHeight / 2);

				x += barWidth + barWidth;

				if ((x + barWidth) >= (self.canvas.width - padding)) {
					break;
				}
			}

			self.animationId = requestAnimationFrame(drawSpectrum);
		};

		drawSpectrum();
	}

	record() {
		this.spectrumRecordingMode = true;
	}

	stop() {
		this.visualizing = false;
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
}