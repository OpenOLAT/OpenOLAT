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

import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2023-11-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum VideoFormatExtended {
	mp4("video/mp4"),
	m3u8("application/x-mpegURL"),
	youtube("video/youtube"),
	vimeo("video/vimeo"),
	panopto("video/mp4"),
	nanoo("video/mp4");

	private static final Logger log = Tracing.createLoggerFor(VideoFormatExtended.class);

	private final String mimeType;

	VideoFormatExtended(String mimeType) {
		this.mimeType = mimeType;
	}

	public String mimeType() {
		return mimeType;
	}

	public static VideoFormatExtended valueOfUrl(String url) {
		if(StringHelper.containsNonWhitespace(url)) {
			try {
				URL urlObj = new URL(url);
				String host = urlObj.getHost();
				if(host.endsWith("youtu.be") || host.endsWith("youtube.be") || host.endsWith("youtube.com")) {
					return VideoFormatExtended.youtube;
				} else if(host.endsWith("vimeo.com")) {
					return VideoFormatExtended.vimeo;
				} else if(url.indexOf("/Panopto/") > 0) {
					return VideoFormatExtended.panopto;
				} else if(url.endsWith(".mp4")) {
					return VideoFormatExtended.mp4;
				} else if(url.endsWith(".m3u8")) {
					return VideoFormatExtended.m3u8;
				} else if (host.endsWith("nanoo.tv")) {
					return VideoFormatExtended.nanoo;
				}
			} catch (MalformedURLException e) {
				log.warn("Cannot read url: " + url, e);
			}
		}
		return null;
	}
}
