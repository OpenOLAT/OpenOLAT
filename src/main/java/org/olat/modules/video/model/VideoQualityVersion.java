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
 * Model of quality-versions to save in a seperate xml-file
 * 
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoQualityVersion{
	// Properties
	private String type;
	private String fileSize;
	private Size dimension;
	private String format;
	private boolean isTransforming;
	private int transcodingStatus = 0;
	
	public VideoQualityVersion(String type, String fileSize, Size dimension, String format){
		this.type = type;
		this.fileSize = fileSize;
		this.dimension = dimension;
		this.format = format;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
	
	public boolean getIsTransforming() {
		return isTransforming;
	}

	public void setIsTransforming(boolean isTranscoding) {
		this.isTransforming = isTranscoding;
	}
	
	/**
	 * @return 0: transcoding has not yet startet; 100: transcoding is done
	 */
	public int getTranscodingStatus() {
		return this.transcodingStatus;
	}

	/**
	 * Set transcoding status in percent
	 * @param status
	 */
	public void setTranscodingStatus(int status) {
		this.transcodingStatus = status;
	}
}