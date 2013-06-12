
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */

package de.bps.onyx.plugin;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.gui.media.MediaResource;

/**
 * @author Ingmar Kroll
 */

public class StreamMediaResource implements MediaResource {

	private InputStream is;
	private String fileName;
	private Long size;
	private Long lastModified;

	/**
	 * file assumed to exist, but if it does not exist or cannot be read,
	 * getInputStream() will return null and the class will behave properly.
	 *
	 * @param file
	 */
	public StreamMediaResource(InputStream is, String fileName, Long size, Long lastModified) {
		this.is = is;
		this.fileName = fileName;
		this.size = size;
		this.lastModified = lastModified;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getContentType()
	 */
	public String getContentType() {
		return "application/octet-stream";
	}

	/**
	 * @return
	 * @see org.olat.core.gui.media.MediaRequest#getSize()
	 */
	@Override
	public Long getSize() {
		return size;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return is;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getLastModified()
	 */
	public Long getLastModified() {
		return lastModified;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	public void release() {
	// void
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	public void prepare(HttpServletResponse hres) {
		hres.setHeader("Content-Disposition", "attachment; filename=" + this.fileName);
	}

}

