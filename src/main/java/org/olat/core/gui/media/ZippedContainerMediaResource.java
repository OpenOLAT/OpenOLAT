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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.filters.VFSRevisionsAndThumbnailsFilter;

/**
 * 
 * Initial date: 8 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ZippedContainerMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ZippedContainerMediaResource.class);
	
	private final String filename;
	private final boolean withMetadata;
	private final VFSContainer unzipContainer;
	
	public ZippedContainerMediaResource(String filename, VFSContainer unzipContainer, boolean withMetadata) {
		this.filename = filename;
		this.withMetadata = withMetadata;
		this.unzipContainer = unzipContainer;
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String label = StringHelper.transformDisplayNameToFileSystemName(filename);
		if(label != null && !label.toLowerCase().endsWith(".zip")) {
			label += ".zip";
		}
		
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);
	
		try(OutputStream out=hres.getOutputStream()) {
			ZipUtil.zip(unzipContainer, out, new VFSRevisionsAndThumbnailsFilter(), withMetadata);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
