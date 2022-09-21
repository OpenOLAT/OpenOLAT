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
 * Initial date: 2022-09-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum AVQualtiy {
	low("video.audio.quality.low", 480, 872000, 128000),
	medium("video.audio.quality.medium", 720, 1872000, 128000),
	high("video.audio.quality.high", 1080, 2872000, 128000);

	private final String textKey;
	private final int height;
	private final int videoBitsPerSecond;
	private final int audioBitsPerSecond;

	AVQualtiy(String textKey, int height, int videoBitsPerSecond, int audioBitsPerSecond) {
		this.textKey = textKey;
		this.height = height;
		this.videoBitsPerSecond = videoBitsPerSecond;
		this.audioBitsPerSecond = audioBitsPerSecond;
	}

	public String getTextKey() {
		return textKey;
	}

	public int getHeight() {
		return height;
	}

	public int getVideoBitsPerSecond() {
		return videoBitsPerSecond;
	}

	public int getAudioBitsPerSecond() {
		return audioBitsPerSecond;
	}

	public String toJson() {
		return "{" +
				"name: '" + name() + "', " +
				"height: " + height + ", " +
				"videoBitsPerSecond: " + videoBitsPerSecond + ", " +
				"audioBitsPerSecond: " + audioBitsPerSecond +
				"}";
	}
}
