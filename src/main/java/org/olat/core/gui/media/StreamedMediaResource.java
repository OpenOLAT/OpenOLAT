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
package org.olat.core.gui.media;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 26.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StreamedMediaResource implements MediaResource {

	private final InputStream is;
	private final String filename;
	private final Long size;
	private final Long lastModified;
	private final String contentType;


	public StreamedMediaResource(InputStream is, String filename, String contentType) {
		this(is, filename, contentType, null, null);
	}
	
	public StreamedMediaResource(InputStream is, String filename, Long size, Long lastModified) {
		this(is, filename, "application/octet-stream", size, lastModified);
	}
	
	public StreamedMediaResource(InputStream is, String filename, String contentType, Long size, Long lastModified) {
		this.is = is;
		this.size = size;
		this.filename = filename;
		this.lastModified = lastModified;
		this.contentType = contentType;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return contentType;
		
	}

	@Override
	public Long getSize() {
		return size;
	}

	@Override
	public InputStream getInputStream() {
		return is;
	}

	@Override
	public Long getLastModified() {
		return lastModified;
	}

	@Override
	public void release() {
		IOUtils.closeQuietly(is);
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String encodedFileName = StringHelper.urlEncodeUTF8(filename);
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
		hres.setHeader("Content-Description", encodedFileName);
	}
}

