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
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionAndWallet;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointServiceImpl creditPointService;
	@Autowired
	private CreditPointTransactionDAO transactionDao;
	
	@Test
	public void addAndRemoveTransaction() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-1");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();
		
		Assertions.assertThat(BigDecimal.ZERO)
			.isEqualByComparingTo(wallet.getBalance());
		
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), null,
				"Depot", wallet, user, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		wallet = creditPointService.getOrCreateWallet(user, system);
		Assertions.assertThat(new BigDecimal("100.0"))
			.isEqualByComparingTo(wallet.getBalance());
		
		CreditPointTransactionAndWallet trx = creditPointService.createCreditPointTransaction(CreditPointTransactionType.withdrawal, new BigDecimal("-50"), null,
				"Withdrawal", wallet, user, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(trx);
		Assert.assertEquals(wallet, trx.wallet());
		Assert.assertNotNull(trx.transaction());
		
		wallet = creditPointService.getOrCreateWallet(user, system);
		Assertions.assertThat(new BigDecimal("50.00"))
			.isEqualByComparingTo(wallet.getBalance());
	}
	
	@Test
	public void reversealTransaction() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-1");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();
		
		Assertions.assertThat(BigDecimal.ZERO)
			.isEqualByComparingTo(wallet.getBalance());
		
		CreditPointTransactionAndWallet depositTrx = creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), null,
				"Depot", wallet, user, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		wallet = creditPointService.getOrCreateWallet(user, system);
		Assertions.assertThat(new BigDecimal("100.0"))
			.isEqualByComparingTo(wallet.getBalance());
		
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.reversal, new BigDecimal("-100"), null,
				"Reversal", wallet, user, null, null, null, null, depositTrx.transaction());
		dbInstance.commitAndCloseSession();
		
		wallet = creditPointService.getOrCreateWallet(user, system);
		Assertions.assertThat(BigDecimal.ZERO)
			.isEqualByComparingTo(wallet.getBalance());
	}
	
	@Test
	public void calculateSimpleBalance() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-3");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT3", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();

		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), new BigDecimal("100"), null,
				"Depot 1", user, wallet, null, null, null, null, null);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("51"), new BigDecimal("51"), null,
				"Depot 2", user, wallet, null, null, null, null, null);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("325"), new BigDecimal("325"), null,
				"Depot 3", user, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		CreditPointWallet balancedWallet = creditPointService.recalculateBalance(wallet, new Date());
		Assertions.assertThat(new BigDecimal("476.00"))
			.isEqualByComparingTo(balancedWallet.getBalance());
	}
	
	@Test
	public void calculateBalanceWithAnExpiredDeposit() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-4");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT4", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();

		Date now = new Date();
		Date expirationDate = DateUtils.addDays(now, -2);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("121"), new BigDecimal("121"), expirationDate,
				"Depot 1", user, wallet, null, null, null, null, null);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("21"), new BigDecimal("21"), null,
				"Depot 2", user, wallet, null, null, null, null, null);
		Date expireSoonDate = DateUtils.addHours(now, 2);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("89"), new BigDecimal("89"), expireSoonDate,
				"Depot 3", user, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		CreditPointWallet balancedWallet = creditPointService.recalculateBalance(wallet, new Date());
		Assertions.assertThat(new BigDecimal("110"))
			.isEqualByComparingTo(balancedWallet.getBalance());
		
		// Check presence of the expiration transaction
		List<CreditPointTransaction> transactions = transactionDao.loadTransactions(balancedWallet);
		List<CreditPointTransaction> expirationsTansactions = transactions.stream()
				.filter(trx -> CreditPointTransactionType.expiration.equals(trx.getTransactionType()))
				.toList();
		Assertions.assertThat(expirationsTansactions)
			.hasSize(1);
		CreditPointTransaction expirationTansaction = expirationsTansactions.get(0);
		Assertions.assertThat(new BigDecimal("-121"))
			.isEqualByComparingTo(expirationTansaction.getAmount());
		
		// Calculate a second time the balance must give the same results
		CreditPointWallet controlWallet = creditPointService.recalculateBalance(wallet, new Date());
		Assertions.assertThat(new BigDecimal("110"))
			.isEqualByComparingTo(controlWallet.getBalance());
	}
	
	@Test
	public void filterTransactionsSetToExpire() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-5");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT5", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();

		Date now = new Date();
		Date expirationDate = DateUtils.addDays(now, -2);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("121"), new BigDecimal("121"), expirationDate,
				"Depot 1", user, wallet, null, null, null, null, null);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("21"), new BigDecimal("21"), null,
				"Depot 2", user, wallet, null, null, null, null, null);

		CreditPointTransaction expire1 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("14"), new BigDecimal("14"), DateUtils.addHours(now, 2),
				"Depot 3", user, wallet, null, null, null, null, null);
		CreditPointTransaction expire2 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("15"), new BigDecimal("15"), DateUtils.addHours(now, 8),
				"Depot 3", user, wallet, null, null, null, null, null);
		CreditPointTransaction expire3 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("16"), new BigDecimal("16"), DateUtils.addHours(now, 16),
				"Depot 3", user, wallet, null, null, null, null, null);
		
		dbInstance.commitAndCloseSession();
		
		List<CreditPointTransaction> allTransactions = transactionDao.loadTransactions(wallet);
		CreditPointTransaction withdraw = transactionDao.createTransaction(CreditPointTransactionType.withdrawal, new BigDecimal("35"), null, null,
				"Credit", user, wallet, null, null, null, null, null);
		
		Map<CreditPointTransaction,BigDecimal> mapping = creditPointService.filterTransactionsSetToExpire(allTransactions, withdraw, now);
		Assertions.assertThat(mapping)
			.hasSize(3)
			.containsKeys(expire1, expire2, expire3);
		
		BigDecimal remainingExpire1 = mapping.get(expire1);
		BigDecimal remainingExpire2 = mapping.get(expire2);
		BigDecimal remainingExpire3 = mapping.get(expire3);
		Assertions.assertThat(BigDecimal.ZERO)
			.isEqualByComparingTo(remainingExpire1);
		Assertions.assertThat(BigDecimal.ZERO)
			.isEqualByComparingTo(remainingExpire2);
		Assertions.assertThat(new BigDecimal("10"))
			.isEqualByComparingTo(remainingExpire3);
	}
	
	@Test
	public void removeCreditPointTransaction() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-5");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT5", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();

		Date now = new Date();
		Date expirationDate = DateUtils.addDays(now, -2);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("150"), new BigDecimal("150"), null,
				"Depot 1", user, wallet, null, null, null, null, null);
		transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("25"), new BigDecimal("25"), expirationDate,
				"Depot 2", user, wallet, null, null, null, null, null);

		CreditPointTransaction expire1 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("21"), new BigDecimal("21"), DateUtils.addHours(now, 2),
				"Depot 3", user, wallet, null, null, null, null, null);
		CreditPointTransaction expire2 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("31"), new BigDecimal("31"), DateUtils.addHours(now, 8),
				"Depot 3", user, wallet, null, null, null, null, null);
		CreditPointTransaction expire3 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("41"), new BigDecimal("41"), DateUtils.addHours(now, 16),
				"Depot 3", user, wallet, null, null, null, null, null);
		
		dbInstance.commitAndCloseSession();
		
		CreditPointTransactionAndWallet results = creditPointService.createCreditPointTransaction(CreditPointTransactionType.removal, new BigDecimal("-55"),
				null, null, wallet, user, null, null, null, null, null);
		CreditPointWallet updatedWallet = results.wallet();
		Assertions.assertThat(updatedWallet.getBalance())
			.isEqualByComparingTo(new BigDecimal("188"));
		dbInstance.commitAndCloseSession();
		
		CreditPointTransaction reloadedExpire1 = transactionDao.loadTransaction(expire1);
		Assertions.assertThat(reloadedExpire1.getRemainingAmount())
			.isEqualByComparingTo(BigDecimal.ZERO);
		CreditPointTransaction reloadedExpire2 = transactionDao.loadTransaction(expire2);
		Assertions.assertThat(reloadedExpire2.getRemainingAmount())
			.isEqualByComparingTo(BigDecimal.ZERO);
		CreditPointTransaction reloadedExpire3 = transactionDao.loadTransaction(expire3);
		Assertions.assertThat(reloadedExpire3.getRemainingAmount())
			.isEqualByComparingTo(new BigDecimal("38"));
	}
	
	@Test
	public void cancelCreditPointTransaction() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-7");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT7", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();

		CreditPointTransaction trx1 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("150"), new BigDecimal("150"), null,
				"Depot 1", user, wallet, null, null, null, null, null);
		CreditPointTransaction trx2 = transactionDao.createTransaction(CreditPointTransactionType.deposit, new BigDecimal("25"), new BigDecimal("25"), null,
				"Depot 2", user, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		CreditPointTransactionAndWallet result = creditPointService.cancelCreditPointTransaction(wallet, trx2, user);
		Assertions.assertThat(result.wallet().getBalance())
			.isEqualByComparingTo(new BigDecimal("150"));
		
		List<CreditPointTransaction> transactions = transactionDao.loadTransactions(result.wallet());
		Assertions.assertThat(transactions)
			.hasSize(3)
			.containsExactlyInAnyOrder(trx1, trx2, result.transaction());
	}
	
	@Test
	public void cancelCreditPointTransactionToNegative() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("coins-8");
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT8", null, null, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();
		
		CreditPointTransactionAndWallet trx1 = creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), null,
				"Deposit", wallet, user, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		CreditPointTransactionAndWallet trx2 = creditPointService.createCreditPointTransaction(CreditPointTransactionType.removal, new BigDecimal("-50"), null,
				"Removal", wallet, user, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		CreditPointTransactionAndWallet result = creditPointService.cancelCreditPointTransaction(wallet, trx1.transaction(), user);
		Assertions.assertThat(result.wallet().getBalance())
			.isEqualByComparingTo(new BigDecimal("-50"));
		
		List<CreditPointTransaction> transactions = transactionDao.loadTransactions(result.wallet());
		Assertions.assertThat(transactions)
			.hasSize(3)
			.containsExactlyInAnyOrder(trx1.transaction(), trx2.transaction(), result.transaction());
	}
}
