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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointWalletDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	
	@Test
	public void createWallet() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("wall-1");
		String name = "wall-1-coin";
		String label = "W1C";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(180), CreditPointExpirationType.DAY);
		dbInstance.commitAndCloseSession();
		
		CreditPointWallet wallet = creditPointWalletDao.createWallet(owner, cpSystem);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(wallet);
	}
	
	
	@Test
	public void getWallet() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("wall-2");
		String name = "wall-2-coin";
		String label = "W2C";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(180), CreditPointExpirationType.DAY);
		dbInstance.commitAndCloseSession();
		
		CreditPointWallet wallet = creditPointWalletDao.createWallet(owner, cpSystem);
		dbInstance.commitAndCloseSession();
		
		CreditPointWallet loadedWallet = creditPointWalletDao.getWallet(owner, cpSystem);
		Assert.assertNotNull(loadedWallet);
		Assert.assertEquals(wallet, loadedWallet);
		Assert.assertEquals(owner, loadedWallet.getIdentity());
		Assert.assertEquals(cpSystem, loadedWallet.getCreditPointSystem());
	}
	
	@Test
	public void getWalletsForBalanceRecalculation() {
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsRndUser("wall-3");
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsRndUser("wall-4");
		String name = "wall-3-coin";
		String label = "W3C";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(180), CreditPointExpirationType.DAY);
		dbInstance.commitAndCloseSession();
		
		Date now = new Date();
		CreditPointWallet walletFuture = creditPointWalletDao.createWallet(owner1, cpSystem);
		walletFuture.setBalanceRecalculationRequiredAfter(DateUtils.addDays(now, 1));
		walletFuture = creditPointWalletDao.update(walletFuture);
		CreditPointWallet walletPast = creditPointWalletDao.createWallet(owner2, cpSystem);
		walletPast.setBalanceRecalculationRequiredAfter(DateUtils.addDays(now, -1));
		walletPast = creditPointWalletDao.update(walletPast);
		dbInstance.commitAndCloseSession();
		
		List<CreditPointWallet> walletsList = creditPointWalletDao.getWalletsForBalanceRecalculation(now);
		Assertions.assertThat(walletsList)
			.containsAnyOf(walletPast)
			.doesNotContain(walletFuture);
	}
	
	

}
