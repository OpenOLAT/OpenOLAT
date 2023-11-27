/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 *
 * Initial date: 2023-09-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
		'use strict';

		Object.assign(mejs.MepDefaults, {
			visualizer: {
				canvas: null,
				audioUrl: '',
				waveColor: 'gray',
				waveHighlightColor: 'lightgray',
				scaleFactorY: 0.6,
				smallAudioCanvas: false
			}
		});

		Object.assign(MediaElementPlayer.prototype, {
			buildvisualizer: function buildvisualizer(player, controls, layers, media) {
				const t = this;

				t.barWidth = 2;
				t.barGap = 1;
				t.padding = 10;
				t.lastCurrentTime = 0;
				t.channelData = null;
				t.waveFormAvailable = false;
				t.currentTimeSpan = null;
				t.currentTimeHandle = null;
				t.currentTimeHovered = null;
				t.currentTimeLoaded = null;
				t.msPerFrame = null;

				media.addEventListener('loadedmetadata', function () {
					t.initCanvas();
					t.initWaveForm();
					t.initTimeBar();
					if (t.duration > 10) {
						//t.previewWave(media);
					}
				});

				media.addEventListener('timeupdate', function () {
					t.handleCurrentTime(media.currentTime);
				});

				media.addEventListener('play', function() {
					if (t.msPerFrame === null) {
						return;
					}
					setTimeout(() => {
							t.handlePlaying(media);
						}, t.msPerFrame);
				});
			},

			previewWave: function previewWave(media) {
				if (!this.options.visualizer.canvas) {
					console.log('Parameter visualizer.canvas not defined');
					return;
				}

				const canvas = this.options.visualizer.canvas;
				const availableWidth = canvas.width - 2 * this.padding;
				const availableHeight = canvas.height - 2 * this.padding;

				this.previewAudioContext = new AudioContext();
				this.previewSource = this.previewAudioContext.createMediaElementSource(this.node);
				this.previewAnalyzer = this.previewAudioContext.createAnalyser();
				this.previewSource.connect(this.previewAnalyzer);
				this.previewLastTime = this.currentTime;
				this.previewLastDrawnX = -1;
				const bufferLength = this.previewAnalyzer.fftSize;
				const dataArray = new Uint8Array(bufferLength);

				this.animationId = null;

				const t = this;

				const drawPreviewWave = () => {
					if (t.waveFormAvailable) {
						return;
					}

					const from = t.previewLastTime;
					let x0 = Math.floor(this.padding + from * availableWidth / t.duration);
					if (x0 !== t.previewLastDrawnX) {
						t.previewAnalyzer.getByteTimeDomainData(dataArray);

						const to = t.currentTime;
						const deltaT = to - from;
						const x1 = Math.floor(this.padding + to * availableWidth / t.duration);
						for (let x = x0; x <= x1; x++) {
							let index = Math.min(x - x0, dataArray.length - 1);
							let value = Math.abs(dataArray[index]);
							let logValue = Math.log10(value / 256.0);
							let transformedLogValue = logValue * 0.3333 + 1;
							let clippedValue = Math.max(transformedLogValue, 0);
							let barHeight = Math.max(availableHeight * clippedValue, 1);
							t.canvasCtx.fillStyle = t.options.visualizer.waveHighlightColor;
							t.canvasCtx.fillRect(x, 0.5 * (canvas.height - barHeight), 1, barHeight);
						}

						t.previewLastDrawnX = x1;
					}

					t.previewLastTime = t.currentTime;
					t.animationId = requestAnimationFrame(drawPreviewWave);
				}

				drawPreviewWave();
			},

			initCanvas: function initCanvas() {
				if (!this.options.visualizer.canvas) {
					console.log('Parameter visualizer.canvas not defined');
					return;
				}
				const canvas = this.options.visualizer.canvas;
				this.canvasCtx = canvas.getContext('2d');
				const width = canvas.offsetWidth;
				const height = canvas.offsetHeight;
				canvas.width = width;
				canvas.height = height;
				if (canvas.width > 800) {
					this.padding = 20;
				} else if (canvas.width > 500) {
					this.padding = 15;
				} else {
					this.padding = 10;
				}
				if (this.options.visualizer.smallAudioCanvas) {
					this.padding = 10;
				}
				this.canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
			},

			initWaveForm: function initWaveForm() {
				const t = this;

				const xhr = new XMLHttpRequest();
				xhr.open('GET', this.options.visualizer.audioUrl);
				xhr.responseType = 'arraybuffer';
				xhr.onload = () => t.drawWaveForm(xhr.response).finally(() => {});
				xhr.send();
			},

			drawWaveForm: async function drawWaveForm(arrayBuffer) {
				let userAgentString = navigator.userAgent;
				if (userAgentString && userAgentString.indexOf("Firefox") > -1) {
					this.audioContext = new AudioContext();
				} else {
					this.audioContext = new AudioContext({ sampleRate: 8000 });
				}
				const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer);
				await this.audioContext.close();
				this.audioContext = null;
				this.channelData = audioBuffer.getChannelData(0);
				this.waveFormAvailable = true;
				this.measureSamples(this.channelData);
				this.drawSamples(this.channelData);
			},

			measureSamples: function measureSamples(channelData) {
				if (!this.options.visualizer.canvas) {
					console.log('Parameter visualizer.canvas not defined');
					return;
				}
				const canvas = this.options.visualizer.canvas;

				const availableWidth = canvas.width - 2 * this.padding;
				const numberOfBars = availableWidth / (this.barWidth + this.barGap);

				this.scaleFactorX = channelData.length / numberOfBars;
			},

			drawSamples: function drawSamples(channelData) {
				this.drawSamplesInRange(channelData, null, null);
			},

			drawSamplesInRange: function drawSamplesInRange(channelData, from, to) {
				const rangeSpecified = from !== null && to !== null;

				if (rangeSpecified && from === to) {
					return;
				}

				if (!this.options.visualizer.canvas) {
					console.log('Parameter visualizer.canvas not defined');
					return;
				}
				const canvas = this.options.visualizer.canvas;

				const availableWidth = canvas.width - 2 * this.padding;
				const availableHeight = canvas.height - 2 * this.padding;
				const numberOfBars = availableWidth / (this.barWidth + this.barGap);

				this.canvasCtx.fillStyle = rangeSpecified ?
					this.options.visualizer.waveHighlightColor : this.options.visualizer.waveColor;

				let fromBar = 0;
				let toBar = numberOfBars - 1;

				if (rangeSpecified) {
					fromBar = this.duration ? Math.round(from / this.duration * numberOfBars) : 0;
					toBar = this.duration ? Math.round(to / this.duration * numberOfBars) : 0;
				}

				if (rangeSpecified) {
					const x = this.padding + (fromBar / numberOfBars * availableWidth);
					const width = (toBar - fromBar) / numberOfBars * availableWidth;
					this.canvasCtx.fillRect(x, 0.5 * canvas.height, width, 1);
				} else {
					this.canvasCtx.fillRect(this.padding, 0.5 * canvas.height, availableWidth, 1);
				}

				let x = this.padding;
				for (let i = 0; i < numberOfBars; i++) {
					if (i >= fromBar && i <= toBar) {
						this.drawSample(channelData, i, availableHeight, x);
					}
					x += this.barWidth + this.barGap;
				}
			},

			drawSample: function drawSample(channelData, barIndex, availableHeight, x) {
				const dataIndex = Math.min(Math.floor(barIndex * this.scaleFactorX), channelData.length - 1);
				const value = Math.abs(channelData[dataIndex]);
				const logValue = Math.log10(value);
				const transformedLogValue = logValue * 0.3333 + 1;
				const clippedValue = Math.max(transformedLogValue, 0);
				const barHeight = availableHeight * clippedValue *
					(this.options.visualizer.scaleFactorY ? this.options.visualizer.scaleFactorY : 1.0);
				if (barHeight > 0) {
					const canvas = this.options.visualizer.canvas;
					this.canvasCtx.fillRect(x, 0.5 * (canvas.height - barHeight), this.barWidth, barHeight + 1);
				}
			},

			handleCurrentTime: function handleCurrentTime(currentTime) {
				if (!this.channelData) {
					return;
				}

				const from = this.lastCurrentTime;
				const to = currentTime;

				if (to > from && (to - from) < 0.01) {
					return;
				}

				this.lastCurrentTime = to;

				if (to >= from) {
					this.drawSamplesInRange(this.channelData, from, to);
				} else {
					this.lastCurrentTime = 0;
					this.drawSamples(this.channelData);
				}
			},

			handlePlaying: function handlePlaying(media) {
				if (media && media.originalNode && media.readyState === 4 && !media.originalNode.paused &&
					!media.originalNode.ended) {
					this.handleCurrentTime(media.originalNode.currentTime);
					this.updateTimeBar(media);
					const t = this;
					setTimeout(() => {
						t.handlePlaying(media);
					}, this.msPerFrame);
				}
			},

			updateTimeBar: function updateTimeBar(media) {
				if (this.currentTimeSpan && this.currentTimeSpan.style) {
					const scaleX = media.originalNode.currentTime / this.duration;
					this.currentTimeSpan.style.transform = `scaleX(${scaleX})`;
					this.currentTimeSpan.style.transition = 'transform 0.15s linear';
					this.currentTimeHandle.style.opacity = 0;
					this.currentTimeHovered.style.opacity = 0;
					this.currentTimeLoaded.style.opacity = 0;
				}
			},

			initTimeBar: function initTimeBar() {
				this.currentTimeSpan = document.querySelector(`#${this.id} .mejs__time-current`);
				this.currentTimeHandle = document.querySelector(`#${this.id} .mejs__time-handle`);
				this.currentTimeHovered = document.querySelector(`#${this.id} .mejs__time-hovered`);
				this.currentTimeLoaded = document.querySelector(`#${this.id} .mejs__time-loaded`);
				this.msPerFrame = null;
				if (this.duration < 10) {
					this.msPerFrame = 20;
				} else if (this.duration < 30) {
					this.msPerFrame = 50;
				} else if (this.duration < 120) {
					this.msPerFrame = 100;
				}
			}
		});

	},{}]},{},[1]);
