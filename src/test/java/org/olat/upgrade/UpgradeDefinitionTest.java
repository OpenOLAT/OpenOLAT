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
*/
package org.olat.upgrade;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


public class UpgradeDefinitionTest extends OlatTestCase {
	
	@Autowired @Qualifier("databaseUpgrades")
	private UpgradesDefinitions upgradesDefinitions;
	
	/**
	 * tests if one of the upgrade files needed for upgrading the database are accessible via
	 * classpath
	 */
	@Test
	public void testFileResourceFromClasspathPostreSQL() {
		testFileResourceFromClasspath("postgresql");
	}
	
	@Test
	public void testFileResourceFromClasspathMySQL() {
		testFileResourceFromClasspath("mysql");
	}
	
	@Test
	public void testFileResourceFromClasspathOracle() {
		testFileResourceFromClasspath("oracle");
	}
	
	private void testFileResourceFromClasspath(String database) {
		UpgradesDefinitions defs = upgradesDefinitions;
		for(OLATUpgrade upgrade: defs.getUpgrades()) {
			String path = "/database/" + database + "/" + upgrade.getAlterDbStatements();
			Resource file = new ClassPathResource(path);
			assertTrue("file not found: " + path, file.exists());
		}
	}
}
