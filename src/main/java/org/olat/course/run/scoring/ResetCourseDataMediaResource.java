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
package org.olat.course.run.scoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.run.environment.CourseEnvironment;


/**
 * 
 * Initial date: 22 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetCourseDataMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ResetCourseDataMediaResource.class);
	
	private final List<String> archiveNames;
	private final CourseEnvironment courseEnv;
	
	public ResetCourseDataMediaResource(List<String> archiveNames, CourseEnvironment courseEnv) {
		this.archiveNames = archiveNames;
		this.courseEnv = courseEnv;
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
		VFSContainer archiveFolder = VFSManager.getOrCreateContainer(courseEnv.getCourseBaseContainer(), ResetCourseDataHelper.ROOT_FOLDER);
		if(archiveNames.size() == 1) {
			String name = archiveNames.get(0);
			String urlEncodedLabel = StringHelper.urlEncodeUTF8(name);
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
			hres.setHeader("Content-Description", urlEncodedLabel);
			VFSItem item = archiveFolder.resolve(name);
			if(item instanceof VFSLeaf archive) {
				try(OutputStream out=hres.getOutputStream();
						InputStream in=archive.getInputStream()) {
					FileUtils.cpio(in, out, "Archive download");
				} catch (IOException e) {
					log.error("", e);
				}
			}
		} else {
			String courseName = courseEnv.getCourseTitle();
			String filename = "Archive_" + courseName + "_data";
			String label = StringHelper.transformDisplayNameToFileSystemName(filename) + ".zip";
			String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
			hres.setHeader("Content-Description", urlEncodedLabel);
			
			try(OutputStream out=hres.getOutputStream()) {
				ZipUtil.zip(archiveFolder, out,
						item -> archiveNames.contains(item.getName()),
						false);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void release() {
		//
	}
}
