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
package org.olat.core.commons.services.vfs.ui.media;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSConstants;

/**
 * 
 * Initial date: 4 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSRevisionMediaResource implements MediaResource {
	
	private static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
	private static final Logger log = Tracing.createLoggerFor(VFSRevisionMediaResource.class);

	private final VFSRevision revision;
	private final VFSMetadata metadata;

	public VFSRevisionMediaResource(VFSMetadata metadata, VFSRevision revision) {
		this.revision = revision;
		this.metadata = metadata;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_ONE_HOUR;
	}

	@Override
	public boolean acceptRanges() {
		return true;
	}

	@Override
	public String getContentType() {
		String mimeType = WebappHelper.getMimeType(metadata.getFilename());
		if (mimeType == null) {
			mimeType = MIME_TYPE_OCTET_STREAM;
		}
		return mimeType;
	}

	@Override
	public InputStream getInputStream() {
		File revFile = CoreSpringFactory.getImpl(VFSRepositoryService.class).getRevisionFile(revision);
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(revFile));
		} catch(Exception e) {
			log.error("", e);
		}
		return in;
	}

	@Override
	public Long getLastModified() {
		Date lastModified = revision.getFileLastModified();
		return lastModified == null ? null : Long.valueOf(lastModified.getTime());
	}

	@Override
	public Long getSize() {
		long size = revision.getSize();
		return (size == VFSConstants.UNDEFINED) ? null : Long.valueOf(size);
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String filename = StringHelper.urlEncodeUTF8(metadata.getFilename());
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
	}

	@Override
	public void release() {
		//do nothing
	}
}
