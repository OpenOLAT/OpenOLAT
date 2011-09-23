/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this uploadedFile except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.webFeed.models;

/**
 * Enclosure is a sub-element of an RSS item element. It can refer to a media
 * uploadedFile like an mp3-song or an mpeg-video.
 * 
 * <P>
 * Initial Date: Feb 25, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
public class Enclosure {
	private String fileName;
	private String type;
	private long length;
	private String externalUrl;

	// This is needed to pass the uploaded file to the manager
	// private FileElement uploadedFile;

	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param length The length to set.
	 */
	public void setLength(long length) {
		this.length = length;
	}

	/**
	 * @return Returns the length.
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @param externalUrl The externalUrl to set.
	 */
	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}

	/**
	 * @return Returns the externalUrl.
	 */
	public String getExternalUrl() {
		return externalUrl;
	}
}
