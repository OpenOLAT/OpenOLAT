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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.olat.core.gui.components.form.ValidationError;
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
		boolean valid1 = urlHandler.isValidValue("http://www.openolat.org", error, null);
		assertTrue(valid1);
		
		boolean valid2 = urlHandler.isValidValue("http://test.ch", error, null);
		assertTrue(valid2);
		
		boolean valid3 = urlHandler.isValidValue("http://localhost", error, null);
		assertTrue(valid3);
		
		boolean invalid1 = urlHandler.isValidValue("http:www.openolat.org", error, null);
		assertFalse(invalid1);
	}



}