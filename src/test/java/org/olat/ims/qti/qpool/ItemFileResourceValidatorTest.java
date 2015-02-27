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
package org.olat.ims.qti.qpool;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
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
