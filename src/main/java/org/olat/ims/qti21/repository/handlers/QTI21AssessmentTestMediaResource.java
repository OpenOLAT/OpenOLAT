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
package org.olat.ims.qti21.repository.handlers;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 14 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentTestMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21AssessmentTestMediaResource.class);
	
	private final OLATResourceable resource;
	
	public QTI21AssessmentTestMediaResource(OLATResourceable resource) {
		this.resource = resource;
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
		File unzipDir = FileResourceManager.getInstance().unzipFileResource(resource);
		File rootDir = FileResourceManager.getInstance().getFileResourceRoot(resource);
		
		String displayName = CoreSpringFactory.getImpl(RepositoryManager.class)
				.lookupDisplayNameByOLATResourceableId(resource.getResourceableId());
		
		String label = StringHelper.transformDisplayNameToFileSystemName(displayName) + ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			ZipUtil.addPathToZip(unzipDir.toPath(), zout);
			
			File sourceOptionsFile = new File(rootDir, QTI21Service.PACKAGE_CONFIG_FILE_NAME);
			if(sourceOptionsFile.exists()) {
				ZipUtil.addFileToZip(QTI21Service.PACKAGE_CONFIG_FILE_NAME, sourceOptionsFile, zout);
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
