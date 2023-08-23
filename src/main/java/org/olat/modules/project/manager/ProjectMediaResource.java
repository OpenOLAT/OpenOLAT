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
package org.olat.modules.project.manager;

import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;

/**
 * 
 * Initial date: 23 Aug 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ProjectMediaResource.class);
	
	private final Identity doer;
	private final ProjProject project;
	private final Collection<ProjFile> files;
	
	private final ProjectService projectService;
	private final DB dbInstance;

	ProjectMediaResource(ProjectService projectService, DB dbInstance, Identity doer, ProjProject project, Collection<ProjFile> files) {
		this.projectService = projectService;
		this.dbInstance = dbInstance;
		this.doer = doer;
		this.project = project;
		this.files = files;
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
		String zipFileName = StringHelper.transformDisplayNameToFileSystemName(project.getTitle()) + ".zip";
		
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(zipFileName);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		String filePath = "";
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			if (files != null && !files.isEmpty()) {
				VFSContainer projectContainer = projectService.getProjectContainer(project);
				for (ProjFile file : files) {
					String filename = file.getVfsMetadata().getFilename();
					VFSItem vfsItem = projectContainer.resolve(filename);
					if (vfsItem instanceof VFSLeaf vfsLeaf) {
						InputStream inputStream = vfsLeaf.getInputStream();
						try {
							zout.putNextEntry(new ZipEntry(filePath + filename));
							FileUtils.copy(inputStream, zout);
							zout.closeEntry();
							projectService.createActivityDownload(doer, file.getArtefact());
						} catch(Exception e) {
							log.error("Error during export of project {} (key::{}), file {} (key::{})",
									project.getTitle(), project.getKey(), filename, file.getKey(), e);
						} finally {
							FileUtils.closeSafely(inputStream);
						}
					}
				}
				dbInstance.commit();
			}
		} catch (Exception e) {
			log.error("Error during export of project {} (key::{})", project.getTitle(), project.getKey(), e);
		}
	}

	@Override
	public void release() {
		//
	}
}
