package org.olat.ims.qti.qpool;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * Initial date: 20.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemFileResourceValidatorTest {
	
	@Test
	public void validate() throws URISyntaxException {
		URL itemUrl = ItemFileResourceValidatorTest.class.getResource("fibi_i_001.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		ItemFileResourceValidator validator = new ItemFileResourceValidator();
		boolean validate = validator.validate("fibi_i_001.xml", itemFile);
		Assert.assertTrue(validate);
	}
	
	@Test
	public void validate_missingDoctype() throws URISyntaxException {
		URL itemUrl = ItemFileResourceValidatorTest.class.getResource("oo_item_without_doctype.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		ItemFileResourceValidator validator = new ItemFileResourceValidator();
		boolean validate = validator.validate("oo_item_without_doctype.xml", itemFile);
		Assert.assertTrue(validate);
	}
	
	@Test
	public void validate_missingDoctype_invalid() throws URISyntaxException {
		URL itemUrl = ItemFileResourceValidatorTest.class.getResource("oo_item_without_doctype_invalid.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		ItemFileResourceValidator validator = new ItemFileResourceValidator();
		boolean validate = validator.validate("oo_item_without_doctype_invalid.xml", itemFile);
		Assert.assertFalse(validate);
	}
	



}
