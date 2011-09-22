/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.shibboleth.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;


import org.junit.Test;


/**
 * Description:<br>
 * TODO: patrick Class Description for MultivalueAttributeTest
 * 
 * <P>
 * Initial Date:  Oct 27, 2010 <br>
 * @author patrick
 */
public class ShibbolethAttributeTest {

	
	private static final String ATTR_NAME = "InstitutionalEmail";
	private static final String LEARNER = "learner.ulrich@test.com";
	private static final String AUTHOR ="author.ernest@mailbox.com";
	private static final String ADMIN = "administrator.system@provider.com";
	private static final String MULTIVALUE_SEP =";";
	
	private static final String MULTIVALUE_VALID = createMultivalueString(LEARNER,AUTHOR,ADMIN);
	private static final String MULTIVALUE_INVALID_EMPTY = "";
	
	private static final String SINGLEVALUE_VALID = LEARNER;
	
	
	
	@Test
	public void multivalueAttributeWithEmails(){
		ShibbolethAttribute multivalueAttrValid = new ShibbolethAttribute(ATTR_NAME, MULTIVALUE_VALID);
		String[] values = multivalueAttrValid.getValues();
		assertNotNull("contains three values", values);
		assertEquals(3, values.length);
		assertEquals(LEARNER, values[0]);
		assertEquals(AUTHOR, values[1]);
		assertEquals(ADMIN, values[2]);
		
		assertEquals(LEARNER, multivalueAttrValid.getFirstValue());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void multivalueAttributeWithEmptyString(){
		new ShibbolethAttribute(ATTR_NAME, MULTIVALUE_INVALID_EMPTY);
	}
	
	@Test
	public void singlevalueAttribute(){
		ShibbolethAttribute singleAttrValid = new ShibbolethAttribute(ATTR_NAME, SINGLEVALUE_VALID);
		String[] values = singleAttrValid.getValues();
		assertNotNull("contains one value", values);
		assertEquals(1, values.length);
		assertEquals(LEARNER, values[0]);
		assertEquals(LEARNER, singleAttrValid.getFirstValue());
	}

	
	@Test
	public void multivalueAttributeWith8859String(){
		try {
			String rawRequestValue = new String(MULTIVALUE_VALID.getBytes("ISO-8859-1"));
			ShibbolethAttribute fromRequestShibbAttribute = ShibbolethAttribute.createFromUserRequestValue(ATTR_NAME, rawRequestValue);
			String[] values = fromRequestShibbAttribute.getValues();
			assertNotNull("contains three values",values);

			assertEquals(3, values.length);
			assertEquals(LEARNER, values[0]);
			assertEquals(AUTHOR, values[1]);
			assertEquals(ADMIN, values[2]);
			
			assertEquals(LEARNER, fromRequestShibbAttribute.getFirstValue());
		
		} catch (UnsupportedEncodingException e) {
			fail(e.toString());
		}
	}
	
	private static String createMultivalueString(String... values) {
		String retVal = values[0]+ MULTIVALUE_SEP;
		for (int i = 1; i < values.length; i++) {
			retVal = retVal + values[i] + MULTIVALUE_SEP; 
		}
		return retVal;
	}
	
}
