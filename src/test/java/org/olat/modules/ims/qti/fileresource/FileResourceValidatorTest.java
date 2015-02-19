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
package org.olat.modules.ims.qti.fileresource;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.olat.ims.qti.qpool.ItemFileResourceValidator;

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
