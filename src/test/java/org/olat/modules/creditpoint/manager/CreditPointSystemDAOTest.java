/**

 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.manager;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSystemDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	
	@Test
	public void createNewSystem() {
		String name = "OpenOlat coin";
		String label = "OOC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(180), CreditPointExpirationType.DAY);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(cpSystem);
		Assert.assertNotNull(cpSystem.getKey());
		Assert.assertNotNull(cpSystem.getCreationDate());
		Assert.assertNotNull(cpSystem.getLastModified());
	}
	
	@Test
	public void loadCreditPointSystems() {
		String name = "frentix coin";
		String label = "FXC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(180), CreditPointExpirationType.DAY);
		dbInstance.commitAndCloseSession();
		
		List<CreditPointSystem> allCpSystems = creditPointSystemDao.loadCreditPointSystems();
		Assertions.assertThat(allCpSystems)
			.containsAnyOf(cpSystem);
	}
	
	@Test
	public void loadCreditPointSystem() {
		final String name = "OLAT coin";
		final String label = "OLATC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(192), CreditPointExpirationType.DAY);
		dbInstance.commitAndCloseSession();
		
		CreditPointSystem reloadedSystem = creditPointSystemDao.loadCreditPointSystem(cpSystem.getKey());
		Assert.assertNotNull(reloadedSystem);
		Assert.assertEquals(cpSystem, reloadedSystem);
		Assert.assertEquals(name, reloadedSystem.getName());
		Assert.assertEquals(label, reloadedSystem.getLabel());
		Assert.assertEquals(Integer.valueOf(192), reloadedSystem.getDefaultExpiration());
		
	}
}
