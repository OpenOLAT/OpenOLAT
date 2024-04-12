/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.folder.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSCollectingFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * 
 * Initial date: 27 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderZipMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(FolderZipMediaResource.class);
	
	private final List<VFSItem> vfsItems;

	public FolderZipMediaResource(List<VFSItem> vfsItems) {
		this.vfsItems = vfsItems;
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
		String urlEncodedLabel = "Archive.zip";
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		prepareZip(hres);
	}
	
	private void prepareZip(HttpServletResponse hres) {
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			VFSCollectingFilter filter = new VFSCollectingFilter(new VFSSystemItemFilter());
			for (VFSItem item : vfsItems) {
				ZipUtil.addToZip(item, "", zout, filter, false);
			}
			zout.flush();
		
			VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
			for (VFSItem vfsItem : filter.getAcceptedItems()) {
				if (vfsItem instanceof VFSLeaf vfsLeaf && vfsLeaf.canMeta() == VFSStatus.YES) {
					vfsRepositoryService.increaseDownloadCount(vfsLeaf);
				}
			}
		
		} catch (IOException e) {
			String className = e.getClass().getSimpleName();
			if("ClientAbortException".equals(className)) {
				log.debug("client browser probably abort when downloading zipped files", e);
			} else {
				log.error("client browser probably abort when downloading zipped files", e);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}

}
