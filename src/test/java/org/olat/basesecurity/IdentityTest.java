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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.basesecurity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Desciption: jUnit testsuite to test the OLAT user module.
 * Most tests are method tests of the user manager. Currently no tests for
 * actions are available du to missing servlet stuff.
 * 
 * @author Florian Gnägi
 */
public class IdentityTest extends OlatTestCase {

	private static Logger log = Logger.getLogger(IdentityTest.class.getName());
	// variables for test fixture
	private static boolean isInitialized = false;
	private static Identity ident1, ident2;

	private final static String identityTest1Name = "identityTest1";
	private final static String identityTest2Name = "identityTest2";

	@Before
	public void setUp()throws Exception {
		if (isInitialized == false) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest1Name);
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest2Name);
			DBFactory.getInstance().closeSession();
			isInitialized = true;
		}
	}

	@After
	public void tearDown() {
		try {
			DB db = DBFactory.getInstance();
			db.closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
		}
	}

	@Test
	public void testEquals() {

		assertFalse("Wrong equals implementation, different types are recognized as equals ",ident1.equals(new Integer(1)));
		assertFalse("Wrong equals implementation, different users are recognized as equals ",ident1.equals(ident2));
		assertFalse("Wrong equals implementation, null value is recognized as equals ",ident1.equals(null));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1));
		BaseSecurity ma = BaseSecurityManager.getInstance();
		Identity ident1_2 = ma.findIdentityByName(identityTest1Name);
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1_2));
	}
	@Test
	public void testHashCode() {
		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",ident1.hashCode() == ident1.hashCode());
		assertFalse("Wrong hashCode implementation, different users have same hash-code",ident1.hashCode() == ident2.hashCode());
		BaseSecurity ma = BaseSecurityManager.getInstance();
		Identity ident1_2 = ma.findIdentityByName(identityTest1Name);
		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",ident1.hashCode() == ident1_2.hashCode());
	}

}