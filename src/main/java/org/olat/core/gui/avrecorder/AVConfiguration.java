/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.avrecorder;

/**
 *
 * Initial date: 2022-08-10<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 *
 */
public class AVConfiguration {

	public enum Mode {
		audio,
		video,
		uploadAudio,
		uploadVideo,
	}

	private Mode mode = Mode.video;

	private AVQualtiy quality = AVQualtiy.medium;
	private int videoBitsPerSecond = 872000;
	private int audioBitsPerSecond = 128000;

	/**
	 * The desired height of the resulting video file. Depending on browser, camera and
	 * device orientation, the video recorder will might also choose a different height.
	 * The width is set automatically to match the height and the device capabilities.
	 */
	private int idealHeight = 480;

	/**
	 * The maximum size in bytes that the recording can have.
	 * After reaching this size, the recording is automatically stopped.
	 */
	private long fileSizeLimit = 0;

	/**
	 * The maximum length in milliseconds that the recording can have.
	 * After reaching this length, the recording is automatically stopped.
	 */
	private long recordingLengthLimit = 0;

	private boolean generatePosterImage = false;

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public int getVideoBitsPerSecond() {
		return videoBitsPerSecond;
	}

	public void setVideoBitsPerSecond(int videoBitsPerSecond) {
		this.videoBitsPerSecond = videoBitsPerSecond;
	}

	public int getAudioBitsPerSecond() {
		return audioBitsPerSecond;
	}

	public void setAudioBitsPerSecond(int audioBitsPerSecond) {
		this.audioBitsPerSecond = audioBitsPerSecond;
	}

	public int getIdealHeight() {
		return idealHeight;
	}

	public void setIdealHeight(int idealHeight) {
		this.idealHeight = idealHeight;
	}

	public long getFileSizeLimit() {
		return fileSizeLimit;
	}

	public void setFileSizeLimit(long fileSizeLimit) {
		this.fileSizeLimit = fileSizeLimit;
	}

	public long getRecordingLengthLimit() {
		return recordingLengthLimit;
	}

	public void setRecordingLengthLimit(long recordingLengthLimit) {
		this.recordingLengthLimit = recordingLengthLimit;
	}

	public AVQualtiy getQuality() {
		return quality;
	}

	public void setQuality(AVQualtiy quality) {
		this.quality = quality;
	}

	public boolean isGeneratePosterImage() {
		return generatePosterImage;
	}

	public void setGeneratePosterImage(boolean generatePosterImage) {
		this.generatePosterImage = generatePosterImage;
	}
}
