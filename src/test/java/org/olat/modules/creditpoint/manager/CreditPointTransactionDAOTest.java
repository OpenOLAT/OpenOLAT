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
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionWithInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointTransactionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	@Autowired
	private CreditPointTransactionDAO creditPointTransactionDao;
	
	
	@Test
	public void createTransaction() {
		CreditPointTransactionType transactionType = CreditPointTransactionType.deposit;
		BigDecimal amount = new BigDecimal("100");
		BigDecimal remainingAmount = new BigDecimal("100");
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-1");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-1", "TRX-1", Integer.valueOf(180), CreditPointExpirationType.DAY);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(creator, cpSystem);

		CreditPointTransaction transaction = creditPointTransactionDao.createTransaction(transactionType, amount, remainingAmount,
				null, "Note", creator, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(transaction);
		Assert.assertNotNull(transaction.getKey());
		Assert.assertNotNull(transaction.getCreationDate());
	}
	
	@Test
	public void loadTransactions() {
		CreditPointTransactionType transactionType = CreditPointTransactionType.deposit;
		
		BigDecimal amount = new BigDecimal("100");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-6");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-6", "TRX-6", Integer.valueOf(180), CreditPointExpirationType.DAY);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(creator, cpSystem);

		CreditPointTransaction transaction = creditPointTransactionDao.createTransaction(transactionType, amount, null,
				null, "Remaing", creator, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		List<CreditPointTransaction> transactions = creditPointTransactionDao.loadTransactions(wallet);
		Assertions.assertThat(transactions)
			.hasSize(1)
			.containsExactlyInAnyOrder(transaction);
	}
	
	@Test
	public void loadTransactionsWithInfos() {
		CreditPointTransactionType transactionType = CreditPointTransactionType.deposit;
		
		BigDecimal amount = new BigDecimal("100");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-6");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-6", "TRX-6", Integer.valueOf(180), CreditPointExpirationType.DAY);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(creator, cpSystem);

		CreditPointTransaction transaction = creditPointTransactionDao.createTransaction(transactionType, amount, null,
				null, "Remaing", creator, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		List<CreditPointTransactionWithInfos> transactions = creditPointTransactionDao.loadTransactionsWithInfos(wallet);
		Assertions.assertThat(transactions)
			.hasSize(1);
		
		CreditPointTransactionWithInfos transactionWithInfos = transactions.get(0);
		Assert.assertEquals(transaction, transactionWithInfos.transaction());
	}
	
	@Test
	public void updateRemaingAmount() {
		CreditPointTransactionType transactionType = CreditPointTransactionType.deposit;
		
		BigDecimal amount = new BigDecimal("100");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-1");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-1", "TRX-1", Integer.valueOf(180), CreditPointExpirationType.DAY);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(creator, cpSystem);

		CreditPointTransaction transaction = creditPointTransactionDao.createTransaction(transactionType, amount, null,
				null, "Remaing", creator, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		BigDecimal remainingAmount = new BigDecimal("60");
		boolean updated = creditPointTransactionDao.updateRemaingAmount(remainingAmount, transaction);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(updated);
		
		List<CreditPointTransaction> transactions = creditPointTransactionDao.loadTransactions(wallet);
		Assertions.assertThat(transactions)
			.hasSize(1);
	
		CreditPointTransaction loadedTransaction = transactions.get(0);
		Assertions.assertThat(new BigDecimal("60.00"))
			.isEqualByComparingTo(loadedTransaction.getRemainingAmount());
	}
	
	@Test
	public void nextExpiringTransaction() {
		CreditPointTransactionType transactionType = CreditPointTransactionType.deposit;
		
		BigDecimal amount = new BigDecimal("100");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-1");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-1", "TRX-1", Integer.valueOf(180), CreditPointExpirationType.DAY);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(creator, cpSystem);

		Date expirationDatePast = DateUtils.addDays(new Date(), -2);
		CreditPointTransaction transactionPast = creditPointTransactionDao.createTransaction(transactionType, amount, null,
				expirationDatePast, "Expired", creator, wallet, null, null, null, null, null);
		
		Date expirationDateNext = DateUtils.addDays(new Date(), 1);
		CreditPointTransaction transactionNext = creditPointTransactionDao.createTransaction(transactionType, amount, null,
				expirationDateNext, "Expire soon", creator, wallet, null, null, null, null, null);
		
		Date expirationDateFuture = DateUtils.addDays(new Date(), 189);
		CreditPointTransaction transactionFuture = creditPointTransactionDao.createTransaction(transactionType, amount, null,
				expirationDateFuture, "Expire in 6 months", creator, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		CreditPointTransaction nextTransaction = creditPointTransactionDao.nextExpiringTransaction(wallet, new Date());

		Assert.assertNotNull(nextTransaction);
		Assert.assertEquals(transactionNext, nextTransaction);
		Assert.assertNotEquals(transactionPast, nextTransaction);
		Assert.assertNotEquals(transactionFuture, nextTransaction);
	}

	@Test
	public void hasTransaction() {
		BigDecimal amount = new BigDecimal("100");
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-4");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-4", "TRX-4", Integer.valueOf(180), CreditPointExpirationType.DAY);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(identity, cpSystem);
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		CreditPointTransaction transaction = creditPointTransactionDao.createTransaction(CreditPointTransactionType.deposit,
				amount, null, null, "Transfered", identity, wallet, course.getOlatResource(), Integer.valueOf(1), null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(transaction);
		
		boolean hasTransaction = creditPointTransactionDao.hasTransaction(identity, course.getOlatResource(), Integer.valueOf(1));
		Assert.assertTrue(hasTransaction);
	}
	
	@Test
	public void hasNotTransaction() {
		BigDecimal amount = new BigDecimal("100");
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("trx-5");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("TRX-5", "TRX-5", Integer.valueOf(180), CreditPointExpirationType.DAY);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(identity, cpSystem);
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		CreditPointTransaction transaction = creditPointTransactionDao.createTransaction(CreditPointTransactionType.deposit,
				amount, null, null, "Transfered", identity, wallet, course.getOlatResource(), Integer.valueOf(1), null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(transaction);
		
		boolean notThisRun = creditPointTransactionDao.hasTransaction(identity, course.getOlatResource(), Integer.valueOf(0));
		Assert.assertFalse(notThisRun);
	}
}
