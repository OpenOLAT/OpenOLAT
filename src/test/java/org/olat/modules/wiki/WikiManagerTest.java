package org.olat.modules.wiki;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.wildfly.common.Assert;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiManagerTest extends OlatTestCase {
	
	@Autowired
	private WikiManager wikiManager;
	
	private File tmpWikiDir;
	
	@Before
	public void createTmpDir() {
		tmpWikiDir = new File(WebappHelper.getTmpDir(), "wiki" + CodeHelper.getForeverUniqueID());
	}
	
	@After
	public void deleteTmpDir() {
		FileUtils.deleteDirsAndFiles(tmpWikiDir, true, true);
	}
	
	@Test
	public void importWiki() throws URISyntaxException {
		URL wikiUrl = WikiManagerTest.class.getResource("wiki.zip");
		File wikiFile = new File(wikiUrl.toURI());
		wikiManager.importWiki(wikiFile, null, tmpWikiDir);
		
		File image = new File(tmpWikiDir, "media/IMG_1482.jpg");
		Assert.assertTrue(image.exists());
		File imageMetadata = new File(tmpWikiDir, "media/IMG_1482.jpg.metadata");
		Assert.assertTrue(imageMetadata.exists());
		File indexPage = new File(tmpWikiDir, "wiki/SW5kZXg=.wp");
		Assert.assertTrue(indexPage.exists());
	}
	
	@Test(expected=OLATRuntimeException.class)
	public void importWikiSlide() throws URISyntaxException {
		URL wikiUrl = WikiManagerTest.class.getResource("wiki_alt.zip");
		File wikiFile = new File(wikiUrl.toURI());
		wikiManager.importWiki(wikiFile, null, tmpWikiDir);
	}

}
