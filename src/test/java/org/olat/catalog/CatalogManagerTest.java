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
package org.olat.catalog;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.test.OlatTestCase;

/**
 * Description:<br>
 * TODO: patrickb Class Description for CatalogManagerTest
 * 
 * <P>
 * Initial Date:  17.04.2007 <br>
 * @author patrickb
 */
public class CatalogManagerTest extends OlatTestCase {

	private static Logger log = Logger.getLogger(CatalogManagerTest.class.getName());
	

	/**
	 * 
	 *
	 */
	@Test
	public void testGetChildrenOf(){
		//TODO:pb: unit test for CatalogManagerTest
	}
	
	
	
	
	/**
	 * TearDown is called after each test
	 */
	@After public void tearDown() {
		try {
			DB db = DBFactory.getInstance();
			db.closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
		}
	}

	
}
