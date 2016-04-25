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
package org.olat.modules.video.model;

import org.olat.core.commons.services.image.Size;

/**
 * Model of quality-versions to save in a separate xml-file
 * 
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoQualityVersion {
	public static final int TRANSCODING_STATUS_WAITING = 0;
	public static final int TRANSCODING_STATUS_DONE = 100;

	// Properties
	private int resolution;
	private String fileSize;
	private Size dimension;
	private String format;
	private int transcodingStatus = 0;

	public VideoQualityVersion(int resolution, String fileSize, Size dimension, String format) {
		this.resolution = resolution;
		this.fileSize = fileSize;
		this.dimension = dimension;
		this.format = format;
	}

	/**
	 * The resolution of the transcoded video. The resolution if defined using
	 * the video height. E.g. a 1080p video has a resolution of 1080
	 * 
	 * @return The height of the transcoded video
	 */
	public int getResolution() {
		return resolution;
	}

	/**
	 * @param resolution
	 *            The resolution if defined using the video height. E.g. a 1080p
	 *            video has a resolution of 1080
	 */
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public Size getDimension() {
		return dimension;
	}

	public void setDimension(Size dimension) {
		this.dimension = dimension;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return 0: transcoding has not yet startet; 100: transcoding is done
	 */
	public int getTranscodingStatus() {
		return this.transcodingStatus;
	}

	/**
	 * Set transcoding status in percent (0-100)
	 * 
	 * @param status
	 */
	public void setTranscodingStatus(int status) {
		if (status > 100 || status < 0) {
			status = TRANSCODING_STATUS_DONE;
		}
		this.transcodingStatus = status;
	}
}