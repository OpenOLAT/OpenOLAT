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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectUIFactory;

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
	private final Collection<ProjNote> notes;
	private final String filename;
	
	private final ProjectService projectService;
	private final DB dbInstance;

	ProjectMediaResource(ProjectService projectService, DB dbInstance, Identity doer, ProjProject project,
			Collection<ProjFile> files, Collection<ProjNote> notes, String filename) {
		this.projectService = projectService;
		this.dbInstance = dbInstance;
		this.doer = doer;
		this.project = project;
		this.files = files;
		this.notes = notes;
		this.filename = filename;
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
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(StringHelper.transformDisplayNameToFileSystemName(filename) + ".zip");
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		String filePath = "";
		String notePath = "";
		if (files != null && !files.isEmpty() && notes != null && !notes.isEmpty()) {
			filePath = "files/";
			notePath = "notes/";
		}
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
			
			if (notes != null && !notes.isEmpty()) {
				for (ProjNote note : notes) {
					String noteFileContent = ProjectUIFactory.createNoteFileContent(note);
					try (InputStream inputStream = new ByteArrayInputStream(noteFileContent.getBytes(StandardCharsets.UTF_8));) {
						zout.putNextEntry(new ZipEntry(notePath + ProjectUIFactory.createNoteFilename(note)));
						FileUtils.copy(inputStream, zout);
						zout.closeEntry();
						projectService.createActivityDownload(doer, note.getArtefact());
					} catch(Exception e) {
						log.error("Error during export of project {} (key::{}), note {} (key::{})",
								project.getTitle(), project.getKey(), note.getTitle(), note.getKey(), e);
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
