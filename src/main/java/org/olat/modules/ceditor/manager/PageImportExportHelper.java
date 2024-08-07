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
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.model.jpa.ImageComparisonPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;
import org.hibernate.Hibernate;
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
		unproxy(page);

		zout.putNextEntry(new ZipEntry("page.xml"));
		PageXStream.toStream(page, zout);
		zout.closeEntry();
		
		List<PagePart> parts = page.getBody().getParts();
		for(PagePart part:parts) {
			if(part instanceof MediaPart mediaPart) {
				export(mediaPart, zout);
			}
			if (part instanceof QuizPart quizPart) {
				export(quizPart, zout);
			}
			if (part instanceof GalleryPart galleryPart) {
				export(galleryPart, zout);
			}
			if (part instanceof ImageComparisonPart imageComparisonPart) {
				export(imageComparisonPart, zout);
			}
		}
	}

	private void unproxy(Page page) {
		for (PagePart part : page.getBody().getParts()) {
			if (part instanceof GalleryPart galleryPart) {
				unproxy(galleryPart);
			}
			if (part instanceof ImageComparisonPart imageComparisonPart) {
				unproxy(imageComparisonPart);
			}
			if (part instanceof QuizPart quizPart) {
				unproxy(quizPart);
			}
		}
	}

	private void unproxy(GalleryPart galleryPart) {
		for (MediaToPagePart relation : galleryPart.getRelations()) {
			unproxy(relation);
		}
	}

	private void unproxy(ImageComparisonPart imageComparisonPart) {
		for (MediaToPagePart relation : imageComparisonPart.getRelations()) {
			unproxy(relation);
		}
	}

	private void unproxy(MediaToPagePart relation) {
		if (relation == null) {
			return;
		}

		Hibernate.unproxy(relation.getMedia());
		Hibernate.unproxy(relation.getMediaVersion());
	}

	private void unproxy(QuizPart quizPart) {
		if (quizPart.getBackgroundImageMedia() != null) {
			Hibernate.unproxy(quizPart.getBackgroundImageMedia());
		}
		if (quizPart.getBackgroundImageMediaVersion() != null) {
			Hibernate.unproxy(quizPart.getBackgroundImageMediaVersion());
		}
	}

	private void export(GalleryPart galleryPart, ZipOutputStream zout) {
		for (MediaToPagePart relation : galleryPart.getRelations()) {
			export(relation, zout);
		}
	}

	private void export(ImageComparisonPart imageComparisonPart, ZipOutputStream zout) {
		for (MediaToPagePart relation : imageComparisonPart.getRelations()) {
			export(relation, zout);
		}
	}

	private void export(MediaToPagePart relation, ZipOutputStream zout) {
		if (relation == null) {
			return;
		}

		if (relation.getMediaVersion() != null) {
			export(relation.getMediaVersion(), zout);
		} else if (relation.getMedia().getVersions() != null && !relation.getMedia().getVersions().isEmpty()) {
			export(relation.getMedia().getVersions().get(0), zout);
		}
	}

	private void export(QuizPart quizPart, ZipOutputStream zout) {
		MediaVersion mediaVersion = quizPart.getBackgroundImageMediaVersion();
		export(mediaVersion, zout);
		quizPart.getSettings().getQuestions().forEach((quizQuestion) -> export(quizQuestion, zout));
	}

	private void export(QuizQuestion quizQuestion, ZipOutputStream zout) {
		String relativeFilePath = quizQuestion.getXmlFilePath();
		if (!StringHelper.containsNonWhitespace(relativeFilePath)) {
			return;
		}

		int index = relativeFilePath.lastIndexOf(File.separator);
		if (index == -1) {
			return;
		}

		String relativeDirPath = relativeFilePath.substring(0, index);
		File questionDir = new File(FolderConfig.getCanonicalRoot(), relativeDirPath);
		ZipUtil.addPathToZip(relativeDirPath, questionDir.toPath(), zout);
	}

	private void export(MediaPart mediaPart, ZipOutputStream zout) {
		MediaVersion mediaVersion = mediaPart.getMediaVersion();
		export(mediaVersion, zout);
	}

	private void export(MediaVersion mediaVersion, ZipOutputStream zout) {
		if (mediaVersion != null && StringHelper.containsNonWhitespace(mediaVersion.getStoragePath())) {
			File mediaDir = new File(FolderConfig.getCanonicalRoot(), mediaVersion.getStoragePath());
			ZipUtil.addPathToZip(mediaVersion.getStoragePath(), mediaDir.toPath(), zout);
		}
	}
	
	public Page importPage(ZipFile zfile, Identity pageOwner, Identity mediaOwner) {
		ZipEntry entry = zfile.getEntry("page.xml");
		try(InputStream in=zfile.getInputStream(entry)) {
			Page page = PageXStream.fromStream(in);
			return pageService.importPage(pageOwner, mediaOwner, page, zfile);
		} catch(IOException e) {
			log.error("", e);
		}
		return null;
	}
}
