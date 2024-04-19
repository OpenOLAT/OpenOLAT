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
package org.olat.modules.sharedfolder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.media.ZippedContainerMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 19 avr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedFolderMediaResource implements MediaResource {

	private static final Logger log = Tracing.createLoggerFor(ZippedContainerMediaResource.class);
	
	private final OLATResourceable res;
	
	public SharedFolderMediaResource(OLATResourceable res) {
		this.res = res;
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
		RepositoryEntry entry = CoreSpringFactory.getImpl(RepositoryManager.class).lookupRepositoryEntry(res, false);
		String label = StringHelper.transformDisplayNameToFileSystemName(entry.getDisplayname());
		if(label != null && !label.toLowerCase().endsWith(".zip")) {
			label += ".zip";
		}
		
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);

		try(OutputStream out=hres.getOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(out, FileUtils.BSIZE))) {
			
			RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(entry, null);
			importExport.exportDoExportProperties("", zipOut);
			
			// do intermediate commit to avoid transaction timeout
			DBFactory.getInstance().intermediateCommit();
			
			LocalFolderImpl folder = SharedFolderManager.getInstance().getSharedFolder(res);
			List<VFSItem> items=folder.getItems(new VFSSystemItemFilter());
			for(VFSItem item:items) {
				ZipUtil.addToZip(item, "", zipOut, null, true);
				DBFactory.getInstance().intermediateCommit();
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
