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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.ImageComparisonPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.manager.MediaToPagePartDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageImportExportHelperTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(PageImportExportHelperTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private PageImportExportHelper pageImportExportHelper;
	@Autowired
	private ContentEditorFileStorage contentEditorFileStorage;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaToPagePartDAO mediaToPagePartDAO;
	
	@Test
	public void importPage() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-1");
		
		URL archiveUrl = PageImportExportHelperTest.class.getResource("page_withImage.zip");
		File archiveFile = new File(archiveUrl.toURI());
		Page importedPage = null;
		try(ZipFile pageArchive=new ZipFile(archiveFile)) {
			importedPage = pageImportExportHelper.importPage(pageArchive, author, author);
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}

		Assert.assertNotNull(importedPage);	
	}

	@Test
	public void importPageWithoutPageOwner() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-2");
		
		URL archiveUrl = PageImportExportHelperTest.class.getResource("page_withImage.zip");
		File archiveFile = new File(archiveUrl.toURI());
		Page importedPage = null;
		try(ZipFile pageArchive=new ZipFile(archiveFile)) {
			importedPage = pageImportExportHelper.importPage(pageArchive, null, author);
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}

		Assert.assertNotNull(importedPage);	
	}
	
	@Test
	public void importPageWithQuizz() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-3");
		
		URL archiveUrl = PageImportExportHelperTest.class.getResource("page_withQuizz.zip");
		File archiveFile = new File(archiveUrl.toURI());
		Page importedPage = null;
		try(ZipFile pageArchive=new ZipFile(archiveFile)) {
			importedPage = pageImportExportHelper.importPage(pageArchive, author, author);
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}
		
		List<PagePart> parts = importedPage.getBody().getParts();
		List<QuizPart> quiz = parts.stream()
				.filter(p -> p instanceof QuizPart)
				.map(QuizPart.class::cast)
				.toList();
		Assertions.assertThat(quiz)
			.hasSize(1);
		
		QuizPart quizPart = quiz.get(0);
		QuizSettings settings = quizPart.getSettings();
		List<QuizQuestion> questions = settings.getQuestions();
		Assertions.assertThat(questions)
			.hasSize(2);
		
		String question1Path = questions.get(0).getXmlFilePath();
		File bcrootStorage = contentEditorFileStorage.getRootDirectory();
		File question1File = new File(bcrootStorage, question1Path);
		Assert.assertTrue(question1File.exists());	
	}
	
	@Test
	public void importPageWithSlippingQuizz() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-4");
		
		URL archiveUrl = PageImportExportHelperTest.class.getResource("page_slipQuizz.zip");
		File archiveFile = new File(archiveUrl.toURI());
		Page importedPage = null;
		try(ZipFile pageArchive=new ZipFile(archiveFile)) {
			importedPage = pageImportExportHelper.importPage(pageArchive, author, author);
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}
		
		List<PagePart> parts = importedPage.getBody().getParts();
		List<QuizPart> quiz = parts.stream()
				.filter(p -> p instanceof QuizPart)
				.map(QuizPart.class::cast)
				.toList();
		Assertions.assertThat(quiz)
			.hasSize(1);
		
		QuizPart quizPart = quiz.get(0);
		QuizSettings settings = quizPart.getSettings();
		List<QuizQuestion> questions = settings.getQuestions();
		Assertions.assertThat(questions)
			.hasSize(2);
		
		// Slippy question is not copied in the question folder
		String question1Path = questions.get(0).getXmlFilePath();
		File bcrootStorage = contentEditorFileStorage.getRootDirectory();
		File question1File = new File(bcrootStorage, question1Path);
		Assert.assertFalse(question1File.exists());

		// Slippy question is not copied in its target folder
		File slippyQuestion1File = new File(bcrootStorage, "portfolio/questions/sc3c493952b7429888d170173f747eee.xml");
		Assert.assertFalse(slippyQuestion1File.exists());
	}
	
	/**
	 * A second run of the this test with the same database will match the media per UUID.
	 * If the first test failed, the second run will probably fail too.
	 * 
	 * @throws Exception
	 */
	@Test
	public void importPageWithSlippingImage() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-5");
		
		URL archiveUrl = PageImportExportHelperTest.class.getResource("page_slipImage.zip");
		File archiveFile = new File(archiveUrl.toURI());
		Page importedPage = null;
		try(ZipFile pageArchive=new ZipFile(archiveFile)) {
			importedPage = pageImportExportHelper.importPage(pageArchive, author, author);
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}
		
		List<PagePart> parts = importedPage.getBody().getParts();
		List<MediaPart> medias = parts.stream()
				.filter(p -> p instanceof MediaPart)
				.map(MediaPart.class::cast)
				.toList();
		Assertions.assertThat(medias)
			.hasSize(1);
		
		MediaPart mediaPart = medias.get(0);
		String storage = mediaPart.getMediaVersion().getStoragePath();
		String filename = mediaPart.getMediaVersion().getRootFilename();
		File bcrootStorage = contentEditorFileStorage.getRootDirectory();
		File imageFile = new File(bcrootStorage, storage + "/" + filename);
		Assert.assertTrue(imageFile.exists());
		
		File slippyImageFile = new File("/tmp/briareos.jpg");
		Assert.assertFalse(slippyImageFile.exists());
	}

	/**
	 * Regression test for OO-9264: this fixture is a real-world course export whose
	 * ImageComparisonPart relations have no mediaVersion recorded at all (neither on the
	 * relation, nor recoverable from the media, since the export never captured it - see
	 * PageServiceImpl.getRelationMediaVersion). Importing it must not throw (it used to crash
	 * downstream in ImageComparisonRunController with an empty-list IndexOutOfBoundsException),
	 * and must not leave behind broken, versionless relations - such relations are skipped instead.
	 */
	@Test
	public void importPageWithImageComparisonUnresolvableVersion() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-6");

		URL archiveUrl = PageImportExportHelperTest.class.getResource("page_withImageComparison.zip");
		File archiveFile = new File(archiveUrl.toURI());
		Page importedPage;
		try(ZipFile pageArchive=new ZipFile(archiveFile)) {
			importedPage = pageImportExportHelper.importPage(pageArchive, author, author);
		}

		List<PagePart> parts = importedPage.getBody().getParts();
		List<ImageComparisonPart> imageComparisons = parts.stream()
				.filter(p -> p instanceof ImageComparisonPart)
				.map(ImageComparisonPart.class::cast)
				.toList();
		Assertions.assertThat(imageComparisons).hasSize(1);

		ImageComparisonPart imageComparisonPart = imageComparisons.get(0);
		List<MediaToPagePart> relations = imageComparisonPart.getRelations();
		Assertions.assertThat(relations).isEmpty();
	}

	/**
	 * Regression test for OO-9264: a relation created with an explicitly resolved media version
	 * (the shape ImageComparisonInspectorController.doSaveMedia now produces, mirroring
	 * GalleryEditorController.doSaveMedia) must round-trip through export and import with its
	 * media version intact, since Media.versions itself is never serialized (see
	 * PageXStream: xstream.omitField(MediaImpl.class, "versions")).
	 */
	@Test
	public void exportAndImportImageComparisonWithResolvedVersion() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-7");

		Page page = pageDao.createAndPersist("Page", "", null, null, false, null, null);
		ImageComparisonPart imageComparisonPart = new ImageComparisonPart();
		pageDao.persistPart(page.getBody(), imageComparisonPart);

		Media media = mediaDao.createMediaAndVersion("Test image", "", "", "some content", "file", null, null, 0, author);
		MediaVersion mediaVersion = media.getVersions().get(0);
		mediaToPagePartDAO.persistRelation(imageComparisonPart, media, mediaVersion, author);
		dbInstance.commitAndCloseSession();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zout = new ZipOutputStream(baos)) {
			pageImportExportHelper.export(page, zout);
		}

		File tempZip = new File(WebappHelper.getTmpDir(), "page_export_" + UUID.randomUUID() + ".zip");
		try (FileOutputStream fileOut = new FileOutputStream(tempZip)) {
			fileOut.write(baos.toByteArray());
		}

		Page importedPage;
		try (ZipFile zipFile = new ZipFile(tempZip)) {
			importedPage = pageImportExportHelper.importPage(zipFile, author, author);
		} finally {
			tempZip.delete();
		}

		List<PagePart> parts = importedPage.getBody().getParts();
		List<ImageComparisonPart> imageComparisons = parts.stream()
				.filter(p -> p instanceof ImageComparisonPart)
				.map(ImageComparisonPart.class::cast)
				.toList();
		Assertions.assertThat(imageComparisons).hasSize(1);

		List<MediaToPagePart> relations = imageComparisons.get(0).getRelations();
		Assertions.assertThat(relations).hasSize(1);
		Assertions.assertThat(relations.get(0).getMediaVersion()).isNotNull();
	}
}
