package org.olat.modules.ims.qti.fileresource;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.ims.qti.fileresource.ItemFileResourceValidator;

/**
 * 
 * Initial date: 27.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FileResourceValidatorTest {
	
	@Test
	public void testItemValidation_xml() throws IOException, URISyntaxException {
		URL itemUrl = FileResourceValidatorTest.class.getResource("mchc_ir_005.xml");
		assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());

		ItemFileResourceValidator validator = new ItemFileResourceValidator();
		boolean valid = validator.validate(itemFile.getName(), itemFile);
		Assert.assertTrue(valid);
	}

	
	@Test
	public void testItemValidation_zip() throws IOException, URISyntaxException {
		URL itemUrl = FileResourceValidatorTest.class.getResource("mchc_i_002.zip");
		assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());

		ItemFileResourceValidator validator = new ItemFileResourceValidator();
		boolean valid = validator.validate(itemFile.getName(), itemFile);
		Assert.assertTrue(valid);
	}

}
