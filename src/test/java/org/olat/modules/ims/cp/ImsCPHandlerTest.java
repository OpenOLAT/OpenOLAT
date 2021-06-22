package org.olat.modules.ims.cp;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImsCPHandlerTest extends OlatTestCase {
	
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	
	@Test
	public void importImsCP() throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("ims-cp-1");
		URL imsCpUrl = ImsCPHandlerTest.class.getResource("imscp.zip");
		File imsCpFile = new File(imsCpUrl.toURI());
		
		RepositoryHandler cpHandler = handlerFactory.getRepositoryHandler(ImsCPFileResource.TYPE_NAME);	
		RepositoryEntry entry = cpHandler.importResource(author, null, "IMS CP", null, false, null, Locale.ENGLISH, imsCpFile, imsCpFile.getName());
		Assert.assertNotNull(entry);
		
		File cpRoot = FileResourceManager.getInstance().unzipFileResource(entry.getOlatResource());
		File image = new File(cpRoot, "IMG_1482.jpg");
		Assert.assertTrue(image.exists());
		File manifestXml = new File(cpRoot, "imsmanifest.xml");
		Assert.assertTrue(manifestXml.exists());
		File page = new File(cpRoot, "new.html");
		Assert.assertTrue(page.exists());
	}
	
	@Test(expected=OLATRuntimeException.class)
	public void importImsCPSlide() throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("ims-cp-1");
		URL imsCpUrl = ImsCPHandlerTest.class.getResource("imscp_alt.zip");
		File imsCpFile = new File(imsCpUrl.toURI());
		
		RepositoryHandler cpHandler = handlerFactory.getRepositoryHandler(ImsCPFileResource.TYPE_NAME);	
		cpHandler.importResource(author, null, "IMS CP", null, false, null, Locale.ENGLISH, imsCpFile, imsCpFile.getName());
	}

}
