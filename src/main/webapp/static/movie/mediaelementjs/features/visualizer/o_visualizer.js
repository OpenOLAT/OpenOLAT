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
				waveHighlightColor: 'lightgray'
			}
		});

		Object.assign(MediaElementPlayer.prototype, {
			buildvisualizer: function buildvisualizer(player, controls, layers, media) {
				const t = this;

				t.barWidth = 1;
				t.barGap = 0;
				t.padding = 10;
				t.lastCurrentTime = 0;
				t.channelData = null;
				t.waveFormAvailable = false;

				media.addEventListener('loadedmetadata', function () {
					t.initCanvas();
					t.initWaveForm();
					if (t.duration > 10) {
						//t.previewWave(media);
					}
				});

				media.addEventListener('timeupdate', function () {
					t.handleCurrentTime(media.currentTime);
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
				if (!this.options.visualizer.canvas) {
					console.log('Parameter visualizer.canvas not defined');
					return;
				}
				const canvas = this.options.visualizer.canvas;

				const availableWidth = canvas.width - 2 * this.padding;
				const availableHeight = canvas.height - 2 * this.padding;
				const numberOfBars = availableWidth / (this.barWidth + this.barGap);

				this.canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
				this.canvasCtx.fillStyle = this.options.visualizer.waveColor;

				let x = this.padding;
				for (let i = 0; i < numberOfBars; i++) {
					let index = Math.min(Math.floor(i * this.scaleFactorX), channelData.length - 1);
					let value = Math.abs(channelData[index]);
					let logValue = Math.log10(value);
					let transformedLogValue = logValue * 0.3333 + 1;
					let clippedValue = Math.max(transformedLogValue, 0);
					let barHeight = Math.max(availableHeight * clippedValue, 1);
					this.canvasCtx.fillRect(x, 0.5 * (canvas.height - barHeight), this.barWidth, barHeight);
					x += this.barWidth + this.barGap;
				}
			},

			drawSamplesInRange: function drawSamplesInRange(channelData, from, to) {
				if (from === to) {
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

				this.canvasCtx.fillStyle = this.options.visualizer.waveHighlightColor;
				const fromBar = this.duration ? Math.round(from / this.duration * numberOfBars) : 0;
				const toBar = this.duration ? Math.round(to / this.duration * numberOfBars) : 0;

				let x = this.padding;
				for (let i = 0; i < numberOfBars; i++) {
					if (i >= fromBar && i <= toBar) {
						let index = Math.min(Math.floor(i * this.scaleFactorX), channelData.length - 1);
						let value = Math.abs(channelData[index]);
						let logValue = Math.log10(value);
						let transformedLogValue = logValue * 0.3333 + 1;
						let clippedValue = Math.max(transformedLogValue, 0);
						let barHeight = Math.max(availableHeight * clippedValue, 1);
						this.canvasCtx.fillRect(x, 0.5 * (canvas.height - barHeight), this.barWidth, barHeight);
					}
					x += this.barWidth + this.barGap;
				}
			},

			handleCurrentTime: function handleCurrentTime(currentTime) {
				if (!this.channelData) {
					return;
				}

				const from = this.lastCurrentTime;
				const to = currentTime;
				this.lastCurrentTime = to;

				if (to >= from) {
					this.drawSamplesInRange(this.channelData, from, to);
				} else {
					this.lastCurrentTime = 0;
					this.drawSamples(this.channelData);
				}
			}
		});

	},{}]},{},[1]);
