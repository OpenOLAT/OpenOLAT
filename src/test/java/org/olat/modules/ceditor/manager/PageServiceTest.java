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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.jpa.HTMLPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.SpacerPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.portfolio.handler.TextHandler;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private PageService pageService;
	@Autowired
	private TextHandler textHandler;
	@Autowired
	private ImageHandler imageHandler;
	
	@Test
	public void appendNewPagePart() {
		Page page = pageDao.createAndPersist("Add part", "Add some part", null, null, true, null, null);
		ParagraphPart pPart = new ParagraphPart();
		pPart = pageService.appendNewPagePart(page, pPart);
		HTMLPart hPart = new HTMLPart();
		hPart = pageService.appendNewPagePart(page, hPart);
		dbInstance.commitAndCloseSession();
		
		List<PagePart> parts = pageService.getPageParts(page);
		Assert.assertNotNull(parts);
		Assert.assertEquals(2, parts.size());
		Assert.assertTrue(parts.get(0) instanceof ParagraphPart);
		Assert.assertEquals(pPart, parts.get(0));
		Assert.assertTrue(parts.get(1) instanceof HTMLPart);
		Assert.assertEquals(hPart, parts.get(1));
	}
	
	@Test
	public void appendNewPagePartAtWithIndex() {
		Page page = pageDao.createAndPersist("Add part", "Add some part", null, null, true, null, null);
		ParagraphPart pPart = new ParagraphPart();
		pPart = pageService.appendNewPagePartAt(page, pPart, 0);
		dbInstance.commitAndCloseSession();
		
		List<PagePart> parts = pageService.getPageParts(page);
		Assert.assertNotNull(parts);
		Assert.assertEquals(1, parts.size());
		Assert.assertTrue(parts.get(0) instanceof ParagraphPart);
		Assert.assertEquals(pPart, parts.get(0));
	}
	
	@Test
	public void removePart() {
		Page page = pageDao.createAndPersist("Remove part", "Remove some part", null, null, true, null, null);
		ParagraphPart pPart = new ParagraphPart();
		pageService.appendNewPagePartAt(page, pPart, 0);
		dbInstance.commit();
		
		List<PagePart> parts = pageService.getPageParts(page);
		Assert.assertEquals(1, parts.size());
		dbInstance.commitAndCloseSession();
		
		pageService.removePagePart(page, pPart);
		dbInstance.commitAndCloseSession();
		
		List<PagePart> emptyParts = pageService.getPageParts(page);
		Assert.assertTrue(emptyParts.isEmpty());
	}
	
	@Test
	public void moveUpPagePart() {
		Page page = pageDao.createAndPersist("Move up part", "Move some part", null, null, true, null, null);
		ParagraphPart pPart = new ParagraphPart();
		pPart = pageService.appendNewPagePart(page, pPart);
		HTMLPart hPart = new HTMLPart();
		hPart = pageService.appendNewPagePart(page, hPart);
		dbInstance.commitAndCloseSession();
		
		pageService.moveUpPagePart(page, hPart);
		dbInstance.commitAndCloseSession();
		
		List<PagePart> parts = pageService.getPageParts(page);
		Assert.assertEquals(2, parts.size());
		Assert.assertTrue(parts.get(0) instanceof HTMLPart);
		Assert.assertEquals(hPart, parts.get(0));
		Assert.assertTrue(parts.get(1) instanceof ParagraphPart);
		Assert.assertEquals(pPart, parts.get(1));
	}
	
	@Test
	public void moveDownPagePart() {
		Page page = pageDao.createAndPersist("Move down part", "Move some part", null, null, true, null, null);
		ParagraphPart pPart = new ParagraphPart();
		pPart = pageService.appendNewPagePart(page, pPart);
		HTMLPart hPart = new HTMLPart();
		hPart = pageService.appendNewPagePart(page, hPart);
		dbInstance.commitAndCloseSession();
		
		pageService.moveDownPagePart(page, pPart);
		dbInstance.commitAndCloseSession();
		
		List<PagePart> parts = pageService.getPageParts(page);
		Assert.assertEquals(2, parts.size());
		Assert.assertTrue(parts.get(0) instanceof HTMLPart);
		Assert.assertEquals(hPart, parts.get(0));
		Assert.assertTrue(parts.get(1) instanceof ParagraphPart);
		Assert.assertEquals(pPart, parts.get(1));
	}
	
	@Test
	public void movePagePartWithAfter() {
		Page page = pageDao.createAndPersist("Move part after", "Move some part", null, null, true, null, null);
		ParagraphPart pPart = new ParagraphPart();
		pPart = pageService.appendNewPagePart(page, pPart);
		SpacerPart sPart = new SpacerPart();
		sPart = pageService.appendNewPagePart(page, sPart);
		HTMLPart hPart = new HTMLPart();
		hPart = pageService.appendNewPagePart(page, hPart);
		dbInstance.commitAndCloseSession();
		
		pageService.movePagePart(page, pPart, sPart, true);
		dbInstance.commitAndCloseSession();
		
		List<PagePart> parts = pageService.getPageParts(page);
		Assert.assertEquals(3, parts.size());
		Assert.assertTrue(parts.get(0) instanceof SpacerPart);
		Assert.assertEquals(sPart, parts.get(0));
		Assert.assertTrue(parts.get(1) instanceof ParagraphPart);
		Assert.assertEquals(pPart, parts.get(1));
		Assert.assertTrue(parts.get(2) instanceof HTMLPart);
		Assert.assertEquals(hPart, parts.get(2));
	}
	
	@Test
	public void movePagePart() {
		Page page = pageDao.createAndPersist("Move part", "Move some part", null, null, true, null, null);
		ParagraphPart pPart = new ParagraphPart();
		pPart = pageService.appendNewPagePart(page, pPart);
		SpacerPart sPart = new SpacerPart();
		sPart = pageService.appendNewPagePart(page, sPart);
		HTMLPart hPart = new HTMLPart();
		hPart = pageService.appendNewPagePart(page, hPart);
		dbInstance.commitAndCloseSession();
		
		pageService.movePagePart(page, pPart, hPart, false);
		dbInstance.commitAndCloseSession();
		
		List<PagePart> parts = pageService.getPageParts(page);
		Assert.assertEquals(3, parts.size());
		Assert.assertTrue(parts.get(0) instanceof SpacerPart);
		Assert.assertEquals(sPart, parts.get(0));
		Assert.assertTrue(parts.get(1) instanceof ParagraphPart);
		Assert.assertEquals(pPart, parts.get(1));
		Assert.assertTrue(parts.get(2) instanceof HTMLPart);
		Assert.assertEquals(hPart, parts.get(2));
	}
	
	@Test
	public void getFullPageByKey() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("page-1");
		Page page = pageDao.createAndPersist("Remove part", "Remove some part", null, null, true, null, null);
		ParagraphPart paragraphPart = new ParagraphPart();
		paragraphPart = pageService.appendNewPagePartAt(page, paragraphPart, 0);
		
		Media media = textHandler.createMedia("Text", "Some text", "Alternative", "The real content", "[Text:0]", id);
		MediaPart mediaPart = MediaPart.valueOf(id, media);
		mediaPart = pageService.appendNewPagePartAt(page, mediaPart, 1);
		dbInstance.commitAndCloseSession();
		
		Page fullPage = pageService.getFullPageByKey(page.getKey());
		PageBody body = fullPage.getBody();
		List<PagePart> parts = body.getParts();
		Assert.assertNotNull(parts);
		Assert.assertEquals(2, parts.size());
		Assert.assertTrue(parts.get(0) instanceof ParagraphPart);
		Assert.assertEquals(paragraphPart, parts.get(0));
		Assert.assertTrue(parts.get(1) instanceof MediaPart);
		
		MediaPart fullMediaPart = (MediaPart)parts.get(1);
		MediaVersion mediaVersion = fullMediaPart.getMediaVersion();
		Assert.assertNotNull(mediaVersion);
		Assert.assertEquals("The real content", mediaVersion.getContent());
	}
	
	@Test
	public void mediaPartValueOf() throws URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("page-1");
		Page page = pageDao.createAndPersist("Remove part", "Remove some part", null, null, true, null, null);
		
		// Create an image media
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageFile = new File(imageUrl.toURI());
		Media media = imageHandler.createMedia("Image", null, null, imageFile, imageFile.getName(), "[Image:0]", id);
		// Create and ad the media part
		MediaPart mediaPart = MediaPart.valueOf(id, media);
		mediaPart = pageService.appendNewPagePartAt(page, mediaPart, 1);
		dbInstance.commitAndCloseSession();
		
		Page fullPage = pageService.getFullPageByKey(page.getKey());
		PageBody body = fullPage.getBody();
		List<PagePart> parts = body.getParts();
		Assert.assertEquals(mediaPart, parts.get(0));
		Assert.assertTrue(parts.get(0) instanceof MediaPart);
		
		MediaPart reloadedMediaPart = (MediaPart)parts.get(0);
		Assert.assertEquals(id, reloadedMediaPart.getIdentity());
		Assert.assertEquals(media, reloadedMediaPart.getMedia());
		Assert.assertNotNull(reloadedMediaPart.getMediaVersion());
	}

}
