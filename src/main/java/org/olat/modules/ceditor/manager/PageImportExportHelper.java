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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ZipUtil;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PageImportExportHelper {

	private static final Logger log = Tracing.createLoggerFor(PageImportExportHelper.class);
	
	@Autowired
	private PageService pageService;
	
	public void export(Page page, ZipOutputStream zout)
	throws IOException {
		zout.putNextEntry(new ZipEntry("page.xml"));
		PageXStream.toStream(page, zout);
		zout.closeEntry();
		
		List<PagePart> parts = page.getBody().getParts();
		for(PagePart part:parts) {
			if(part instanceof MediaPart mediaPart) {
				export(mediaPart, zout);
			}
		}
	}
	
	private void export(MediaPart mediaPart, ZipOutputStream zout) {
		Media media = mediaPart.getMedia();
		File mediaDir = new File(FolderConfig.getCanonicalRoot(), media.getStoragePath());
		ZipUtil.addPathToZip(media.getStoragePath(), mediaDir.toPath(), zout);
	}
	
	public Page importPage(ZipFile zfile, Identity author) {
		ZipEntry entry = zfile.getEntry("page.xml");
		try(InputStream in=zfile.getInputStream(entry)) {
			Page page = PageXStream.fromStream(in);
			return pageService.importPage(author, page, zfile);
		} catch(IOException e) {
			log.error("", e);
		}
		return null;
	}
}
