/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.util.vfs;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.image.ImageUtils;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * 
 * Initial date: 12 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSThumbnailResource implements MediaResource {

	public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";

	private String encoding;
	boolean unknownMimeType = false;
	private boolean downloadable = false;
	private VFSThumbnailMetadata metadata;
	private final long cacheControlDuration;

	public VFSThumbnailResource(VFSThumbnailMetadata metadata, long cacheControlDuration) {
		this.metadata = metadata;
		this.cacheControlDuration = cacheControlDuration;
	}

	@Override
	public long getCacheControlDuration() {
		return cacheControlDuration;
	}

	@Override
	public boolean acceptRanges() {
		return true;
	}

	@Override
	public String getContentType() {
		String mimeType = MIME_TYPE_OCTET_STREAM;
		if(metadata != null) {
			String name = metadata.getFilename();
			mimeType = WebappHelper.getMimeType(name);
			if(downloadable) {
				//html, xhtml and javascript are set to force download
				String suffix = FileUtils.getFileSuffix(name);
				if (FolderModule.isContentSusceptibleToForceDownload(suffix, mimeType)) {
					mimeType = MIME_TYPE_OCTET_STREAM;
					unknownMimeType = true;
				} else if (encoding != null) {
					mimeType = mimeType + ";charset=" + encoding;
				}
			} else if (mimeType == null) {
				mimeType = MIME_TYPE_OCTET_STREAM;
				unknownMimeType = true;
			} else {
				// if any encoding is set, append it for the browser
				if (encoding != null) {
					mimeType = mimeType + ";charset=" + encoding;
					unknownMimeType = false;
				}
			}
		}
		return mimeType;
	}

	@Override
	public Long getSize() {
		return metadata != null ? metadata.getFileSize() : VFSStatus.UNDEFINED;
	}

	@Override
	public InputStream getInputStream() {
		if(metadata == null) return null;
		VFSLeaf thumbnail = CoreSpringFactory.getImpl(VFSRepositoryService.class).getThumbnail(metadata);
		return thumbnail != null ? thumbnail.getInputStream() : null;
	}

	@Override
	public Long getLastModified() {
		return metadata != null
			? metadata.getLastModified().getTime()
			: VFSStatus.UNDEFINED;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		if(metadata != null) {
			String name = metadata.getFilename();
			String filename = StringHelper.urlEncodeUTF8(name).replace("+", "%20");
			if (unknownMimeType || downloadable) {
				hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
			} else {
				hres.setHeader("Content-Disposition", "filename*=UTF-8''" + filename);
			}
			if (ImageUtils.isSvgFileName(filename)) {
				hres.setHeader("Content-Security-Policy", "script-src 'none'");
				hres.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
				hres.setHeader("Pragma", "no-cache");
				hres.setDateHeader("Expires", 0);
			}
		}
	}

	@Override
	public void release() {
	// nothing to do here
	}
}
