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
package org.olat.core.util.mail;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description:<br>
 * This test case tests the mail address validator
 * 
 * <P>
 * Initial Date:  14.07.2009 <br>
 * @author gnaegi
 */
public class EmailAddressValidatorTest {

	@Test
	public void testPlainText() {
		// correct stuff
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("contact@openolat.org"));
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("hello.world@crazy.world"));
		
		// following did not work with implementation in OpenOLAT < 10.4.2
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("frustig.lustig@on-openolat.consulting"));
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("contakt@umläüte.biz"));
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("contakt@umläüte.biz"));
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("dont.knwo.what.it@means.中国"));
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("dont.knwo@in.рф"));
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("gnägi@frentix.com"));
		
		// wrong stuff
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress(null));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress(""));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("asdf"));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("asdf@.."));		
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("wrong email @ address dot com"));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("wrong.email@address_dot_com"));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("email @with.blank"));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("great.domain.com"));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("some.thing@"));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("gnaegi"));
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("g@g"));

		// valid addresses but disable in OLAT because this is not what we want users to enter as mail addresses
		Assert.assertFalse(EmailAddressValidator.isValidEmailAddress("\"Florian Gnaegi\" <gnaegi@frentix.com>")); 
		// Validates true by library. Don't know what this would be usefull for. Not needed by OpenOLAT
		Assert.assertTrue(EmailAddressValidator.isValidEmailAddress("someone@[192.168.1.100]")); 		
	}
}
