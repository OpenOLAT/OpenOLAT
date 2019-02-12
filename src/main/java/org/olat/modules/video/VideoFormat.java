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
package org.olat.modules.video;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 12 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum VideoFormat {

	/**
	 * Local mp4 encoded with h264
	 */
	mp4("video/mp4"),
	youtube("video/youtube"),
	vimeo("video/vimeo"),
	panopto("video/mp4");

	private static final OLog log = Tracing.createLoggerFor(VideoFormat.class);
	
	private final String mimeType;
	
	private VideoFormat(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String mimeType() {
		return mimeType;
	}
	
	public static VideoFormat valueOfFilename(String filename) {
		String extension = FilenameUtils.getExtension(filename);
		if("mp4".equalsIgnoreCase(extension) || "m4v".equalsIgnoreCase(extension) || "mov".equalsIgnoreCase(extension)) {
			return VideoFormat.mp4;
		}
		return null;
	}
	
	public static VideoFormat valueOfUrl(String url) {
		try {
			URL urlObj = new URL(url);
			String host = urlObj.getHost();
			if(host.endsWith("youtu.be") || host.endsWith("youtube.be") || host.endsWith("youtube.com")) {
				return VideoFormat.youtube;
			} else if(host.endsWith("vimeo.com")) {
				return VideoFormat.vimeo;
			} else if(host.endsWith("panopto.eu")) {
				return VideoFormat.panopto;
			} else if(url.endsWith(".mp4")) {
				return VideoFormat.mp4;
			}
		} catch (MalformedURLException e) {
			log.warn("Cannot read url: " + url, e);
		}
		return null;
	}
}
