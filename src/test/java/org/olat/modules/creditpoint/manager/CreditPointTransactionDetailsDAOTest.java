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

import java.math.BigDecimal;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionDetails;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreditPointTransactionDetailsDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	@Autowired
	private CreditPointTransactionDAO transactionDao;
	@Autowired
	private CreditPointTransactionDetailsDAO transactionDetailsDao;
	
	@Test
	public void createTransactionDetails() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-details-1");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-DETAILS-1", "TRD-1", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(creator, cpSystem);
		
		BigDecimal amount = new BigDecimal("100");
		CreditPointTransaction transaction1 = transactionDao.createTransaction(CreditPointTransactionType.deposit, amount, amount,
				null, "Details 1", creator, wallet, null, null, null, null, null);
		dbInstance.commit();
		
		BigDecimal reversedAmount = new BigDecimal("-50");
		CreditPointTransaction transaction2 = transactionDao.createTransaction(CreditPointTransactionType.reversal, reversedAmount, null,
				null, "Details 2", creator, wallet, null, null, null, null, null);
		CreditPointTransactionDetails details = transactionDetailsDao.createTransactionDetails(reversedAmount, transaction1, transaction2);
		BigDecimal remaingAmount = new BigDecimal("50");
		boolean updatedRemaing = transactionDao.updateRemaingAmount(remaingAmount, transaction1);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(details);
		Assert.assertNotNull(details.getKey());
		Assert.assertNotNull(details.getCreationDate());
		Assert.assertEquals(transaction1, details.getSource());
		Assert.assertEquals(transaction2, details.getTarget());
		Assertions.assertThat(new BigDecimal("-50.00"))
			.isEqualByComparingTo(details.getAmount());
		
		Assert.assertTrue(updatedRemaing);
	}
	
	@Test
	public void loadTransactionDetails() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-1");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-DETAILS-2", "TRD-2", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(creator, cpSystem);
		
		BigDecimal amount = new BigDecimal("50");
		CreditPointTransaction transaction1 = transactionDao.createTransaction(CreditPointTransactionType.deposit, amount, amount,
				null, "Note 1", creator, wallet, null, null, null, null, null);
		dbInstance.commit();
		
		BigDecimal reversedAmount = new BigDecimal("-10");
		CreditPointTransaction transaction2 = transactionDao.createTransaction(CreditPointTransactionType.reversal, reversedAmount, null,
				null, "Note 2", creator, wallet, null, null, null, null, null);
		CreditPointTransactionDetails details = transactionDetailsDao.createTransactionDetails(reversedAmount, transaction1, transaction2);
		BigDecimal remaingAmount = new BigDecimal("40");
		boolean updatedRemaing = transactionDao.updateRemaingAmount(remaingAmount, transaction1);
		dbInstance.commitAndCloseSession();
		
		List<CreditPointTransactionDetails> detailsList = transactionDetailsDao.loadTransactionDetails(transaction1);
		Assertions.assertThat(detailsList)
			.hasSize(1);
		
		CreditPointTransactionDetails loadedDetails = detailsList.get(0);
		Assert.assertNotNull(loadedDetails);
		Assert.assertNotNull(loadedDetails.getKey());
		Assert.assertNotNull(loadedDetails.getCreationDate());
		Assert.assertEquals(details, loadedDetails);
		Assert.assertEquals(transaction1, loadedDetails.getSource());
		Assert.assertEquals(transaction2, loadedDetails.getTarget());
		Assertions.assertThat(new BigDecimal("-10.00"))
			.isEqualByComparingTo(loadedDetails.getAmount());
		
		List<CreditPointTransactionDetails> reversedDetailsList = transactionDetailsDao.loadTransactionDetails(transaction2);
		Assertions.assertThat(reversedDetailsList)
			.isEmpty();

		Assert.assertTrue(updatedRemaing);
	}
}
