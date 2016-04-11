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
package org.olat.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.user.propertyhandlers.PhonePropertyHandler;
import org.olat.user.propertyhandlers.URLPropertyHandler;

/**
 * 
 * Description:<br>
 * Test the validation method of PropertyHandler(s)
 * 
 * <P>
 * Initial Date:  23 janv. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserPropertiesTest {


	@Test
	public void testURLPropertyHandlerValidation() {
		
		URLPropertyHandler urlHandler = new URLPropertyHandler();
		ValidationError error = new ValidationError();
		// test for valid URL's
		assertTrue(urlHandler.isValidValue(null, "http://www.openolat.org", error, null));
		assertTrue(urlHandler.isValidValue(null, "https://www.openolat.org", error, null));
		assertTrue(urlHandler.isValidValue(null, "http://test.ch", error, null));
		assertTrue(urlHandler.isValidValue(null, "http://localhost", error, null));
		// test for invalid URL's
		assertFalse(urlHandler.isValidValue(null, "http:www.openolat.org", error, null));
		assertFalse(urlHandler.isValidValue(null, "www.openolat.org", error, null));
	}

	@Test
	public void testPhonePropertyHandlerValidation() {
		
		PhonePropertyHandler phoneHandler = new PhonePropertyHandler();
		ValidationError error = new ValidationError();
		// test for valid phone number formats
		assertTrue(phoneHandler.isValidValue(null, "043 544 90 00", error, null));
		assertTrue(phoneHandler.isValidValue(null, "043/544'90'00", error, null));
		assertTrue(phoneHandler.isValidValue(null, "043/544'90'00", error, null));
		assertTrue(phoneHandler.isValidValue(null, "043-544-90-00", error, null));
		assertTrue(phoneHandler.isValidValue(null, "043.544.90.00", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 43 544 90 00", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 (0)43 544 90 00", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 43 544 90 00 x0", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 43 544 90 00 ext. 0", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 43 544 90 00 ext0", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 43 544 90 00 ext 0", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 43 544 90 00 extension 0", error, null));
		assertTrue(phoneHandler.isValidValue(null, "+41 43 544 90 00 Extension 0", error, null));
		// test for invalid phone number formats
		assertFalse(phoneHandler.isValidValue(null, "+41 43 frentix GmbH", error, null));
	}

	@Test
	public void testPhonePropertyHandlerHTMLnormalizer() {
		// test for valid phone number formats
		assertTrue(PhonePropertyHandler.normalizePhonenumber("043 544 90 00").equals("0435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("043/544'90'00").equals("0435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("043-544-90-00").equals("0435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("043.544.90.00").equals("0435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 (0)43 544 90 00").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00 x0").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00 ext. 0").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00 ext0").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00 ext 0").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00 extension 0").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00 Extension 0").equals("+41435449000"));
		assertTrue(PhonePropertyHandler.normalizePhonenumber("+41 43 544 90 00 ext. 0").equals("+41435449000"));
	}

}