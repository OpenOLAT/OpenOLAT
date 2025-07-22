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

import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionImpl;
import org.olat.modules.creditpoint.model.CreditPointTransactionWithInfos;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CreditPointTransactionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CreditPointTransaction createTransaction(CreditPointTransactionType transactionType,
			BigDecimal amount, BigDecimal remainingAmount, Date expirationDate, String note,
			Identity creator, CreditPointWallet wallet,
			OLATResource transfertOrigin, Integer originRun, OLATResource transfertDestination, Integer destinationRun,
			CreditPointTransaction transactionReference) {
		
		CreditPointTransactionImpl transaction = new CreditPointTransactionImpl();
		transaction.setCreationDate(new Date());
		transaction.setTransactionType(transactionType);
		transaction.setAmount(amount);
		transaction.setRemainingAmount(remainingAmount);
		transaction.setExpirationDate(expirationDate);
		transaction.setNote(note);
		transaction.setCreator(creator);
		transaction.setWallet(wallet);
		transaction.setTransfertOrigin(transfertOrigin);
		transaction.setOriginRun(originRun);
		transaction.setTransfertDestination(transfertDestination);
		transaction.setDestinationRun(destinationRun);
		transaction.setTransactionReference(transactionReference);
		dbInstance.getCurrentEntityManager().persist(transaction);
		return transaction;
	}
	
	public boolean updateRemaingAmount(BigDecimal remainingAmount, CreditPointTransaction transaction) {
		int rows = dbInstance.getCurrentEntityManager().createNamedQuery("updateTransactionRemaingAmountLaunchDates")
			.setParameter("remainingAmount", remainingAmount)
			.setParameter("transactionKey", transaction.getKey())
			.executeUpdate();
		return rows > 0;
	}
	
	public List<CreditPointTransaction> loadTransactions(CreditPointWallet wallet) {
		String query = """
				select trx from creditpointtransaction as trx
				inner join fetch trx.wallet as wallet
				where wallet.key=:walletKey""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointTransaction.class)
				.setParameter("walletKey", wallet.getKey())
				.getResultList();
	}
	
	public CreditPointTransaction loadTransaction(CreditPointTransaction transaction) {
		String query = """
				select trx from creditpointtransaction as trx
				inner join fetch trx.wallet as wallet
				where trx.key=:transactionKey""";
		
		List<CreditPointTransaction> transactions = dbInstance.getCurrentEntityManager().createQuery(query, CreditPointTransaction.class)
				.setParameter("transactionKey", transaction.getKey())
				.getResultList();
		return transactions == null || transactions.isEmpty() ? null : transactions.get(0);
	}
	
	public List<CreditPointTransactionWithInfos> loadTransactionsWithInfos(CreditPointWallet wallet) {
		String query = """
				select new CreditPointTransactionWithInfos(trx, entryOrigin, creator) from creditpointtransaction as trx
				inner join fetch trx.wallet as wallet
				left join trx.creator as creator
				left join fetch creator.user as creatorUser
				left join trx.transfertOrigin as originRes
				left join repositoryentry as entryOrigin on (entryOrigin.olatResource.key=originRes.key)
				where wallet.key=:walletKey""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointTransactionWithInfos.class)
				.setParameter("walletKey", wallet.getKey())
				.getResultList();
	}
	
	public CreditPointTransaction nextExpiringTransaction(CreditPointWallet wallet, Date referenceDate) {
		String query = """
				select trx from creditpointtransaction as trx
				inner join fetch trx.wallet as wallet
				where trx.expirationDate>=:referenceDate and wallet.key=:walletKey
				order by trx.expirationDate asc""";
		
		List<CreditPointTransaction> transactions = dbInstance.getCurrentEntityManager().createQuery(query, CreditPointTransaction.class)
				.setParameter("walletKey", wallet.getKey())
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return transactions == null || transactions.isEmpty() ? null : transactions.get(0);
	}
	
	public boolean hasTransaction(IdentityRef identity, OLATResource originResource, Integer run) {
		String query = """
				select trx.key from creditpointtransaction as trx
				inner join trx.wallet as wallet
				where wallet.identity.key=:identityKey and trx.transfertOrigin.key=:originKey and trx.originRun=:originRun""";
		
		List<Long> transactions = dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("originKey", originResource.getKey())
				.setParameter("originRun", run)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return transactions != null && !transactions.isEmpty()
				&& transactions.get(0) != null && transactions.get(0).longValue() > 0;
	}

}
