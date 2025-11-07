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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.OrganisationOrderedTreeCache;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemToOrganisation;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionDetails;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.CurriculumElementCreditPointConfiguration;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.modules.creditpoint.model.CreditPointSystemInfos;
import org.olat.modules.creditpoint.model.CreditPointSystemWithWalletInfos;
import org.olat.modules.creditpoint.model.CreditPointTransactionAndWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionWithInfos;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CreditPointServiceImpl implements CreditPointService {
	
	private static final Logger log = Tracing.createLoggerFor(CreditPointServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private CreditPointTransactionDAO transactionDao;
	@Autowired
	private OrganisationOrderedTreeCache organisationTree;
	@Autowired
	private CreditPointTransactionDetailsDAO transactionDetailsDao;
	@Autowired
	private CreditPointSystemToOrganisationDAO creditPointSystemToOrganisationDao;
	@Autowired
	private RepositoryEntryCreditPointConfigurationDAO repositoryEntryConfigurationDao;
	@Autowired
	private CurriculumElementCreditPointConfigurationDAO curriculumElementConfigurationDao;

	@Override
	public CreditPointSystem createCreditPointSystem(String name, String label,
			Integer defaultExpiration, CreditPointExpirationType defaultExpirationType,
			boolean rolesRestrictions, boolean organisationsRestrictions) {
		return creditPointSystemDao.createSystem(name, label, defaultExpiration, defaultExpirationType,
				rolesRestrictions, organisationsRestrictions);
	}

	@Override
	public CreditPointSystem updateCreditPointSystem(CreditPointSystem creditPointSystem) {
		return creditPointSystemDao.updateSystem(creditPointSystem);
	}
	
	@Override
	public void updateCreditPointSystemOrganisations(CreditPointSystem creditPointSystem, Collection<Organisation> organisations) {
		if (organisations == null || organisations.isEmpty()) {
			creditPointSystemToOrganisationDao.deleteRelations(creditPointSystem);
			return;
		}
		
		List<CreditPointSystemToOrganisation> currentRelations = creditPointSystemToOrganisationDao.loadRelations(creditPointSystem);
		List<Organisation> currentOrganisations = currentRelations.stream()
				.map(CreditPointSystemToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		
		// Create relation for new organisations
		organisations.stream()
				.filter(org -> !currentOrganisations.contains(org))
				.forEach(org -> creditPointSystemToOrganisationDao.createRelation(creditPointSystem, org));

		// Remove relation
		currentRelations.stream()
				.filter(rel -> !organisations.contains(rel.getOrganisation()))
				.forEach(rel -> creditPointSystemToOrganisationDao.deleteRelation(rel));
	}
	
	@Override
	public CreditPointSystem loadCreditPointSystem(CreditPointSystem creditPointSystem) {
		if(creditPointSystem == null || creditPointSystem.getKey() == null) return null;
		return creditPointSystemDao.loadCreditPointSystem(creditPointSystem.getKey());
	}

	@Override
	public List<CreditPointSystem> getCreditPointSystems() {
		return creditPointSystemDao.loadCreditPointSystems();
	}

	@Override
	public List<CreditPointSystem> getCreditPointSystems(Roles roles) {
		// Without roles restrictions
		List<OrganisationRef> organisations = roles
				.getOrganisationsWithRoles(OrganisationRoles.author, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator);
		// With roles restrictions
		List<OrganisationRef> restrictedOrganisations = roles
				.getOrganisationsWithRoles(OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator);
		return creditPointSystemDao.loadCreditPointSystemsFor(organisations, restrictedOrganisations);
	}

	@Override
	public List<CreditPointSystemInfos> getCreditPointSystemsWithInfos() {
		final List<CreditPointSystemWithWalletInfos> systems = creditPointSystemDao.loadCreditPointSystemsWithInfos();
		final List<Long> systemsKeys = systems.stream()
				.map( sys -> sys.system().getKey())
				.toList();
		final Map<Long,List<Long>> systemsToOrganisationsMap = creditPointSystemToOrganisationDao.getOrganisationsMap(systemsKeys);
		final List<OrganisationWithParents> organisations = organisationTree.getOrderedOrganisationsWithParents();
		final Map<Long,OrganisationWithParents> organisationsMap = organisations.stream()
				.collect(Collectors.toMap(OrganisationWithParents::getKey, org -> org, (u,v) -> u));
		
		return systems.stream()
				.map(sys -> {
						List<Long> organisationsKeysList = systemsToOrganisationsMap.get(sys.system().getKey());
						List<OrganisationWithParents> orgs;
						if(organisationsKeysList == null || organisationsKeysList.isEmpty()) {
							orgs = List.of();
						} else {
							orgs = new ArrayList<>(organisationsKeysList.size());
							for(Long organisationKey:organisationsKeysList) {
								orgs.add(organisationsMap.get(organisationKey));
							}
							if(orgs.size() > 1) {
								Collections.sort(orgs);
							}
						}
						return new CreditPointSystemInfos(sys.system(), sys.usage(), orgs);
					})
				.toList();
	}

	@Override
	public CreditPointWallet getOrCreateWallet(Identity identity, CreditPointSystem system) {
		CreditPointWallet wallet = creditPointWalletDao.getWallet(identity, system);
		if(wallet == null) {
			wallet = creditPointWalletDao.createWallet(identity, system);
			dbInstance.commit();
		}
		return wallet;
	}

	@Override
	public CreditPointTransactionAndWallet createCreditPointTransaction(CreditPointTransactionType transactionType,
			BigDecimal amount, Date expirationDate, String note, CreditPointWallet wallet, Identity creator, 
			OLATResource transfertOrigin, Integer originRun, OLATResource transfertDestination, Integer destinationRun,
			CreditPointTransaction transactionReference) {
		
		CreditPointWallet loadedWallet = creditPointWalletDao.loadForUpdate(wallet);
		
		List<CreditPointTransaction> transactions = transactionDao.loadTransactions(loadedWallet);
		BigDecimal remainingAmount = null;
		if(transactionType == CreditPointTransactionType.deposit) {
			remainingAmount = amount;
		}
		
		CreditPointTransaction transaction = transactionDao.createTransaction(transactionType, amount, remainingAmount,
				expirationDate, note, creator, loadedWallet, transfertOrigin, originRun, transfertDestination, destinationRun,
				transactionReference);
		transactions.add(transaction);
		
		CreditPointTransaction removeTransaction = (transactionType == CreditPointTransactionType.removal
				|| transactionType == CreditPointTransactionType.withdrawal) 
				? transaction : null;

		loadedWallet = calculateUpdateAndCommitBalance(wallet, transactions, removeTransaction, transaction.getCreationDate());
		return new CreditPointTransactionAndWallet(transaction, loadedWallet);
	}

	/**
	 * 
	 * @param wallet The wallet
	 * @param transactions The list of all transactions of a wallet
	 * @param amountToRemove Amount to mark some deposit with expiration date
	 * @param referenceDate The date of the calculation
	 * @return
	 */
	private CreditPointWallet calculateUpdateAndCommitBalance(CreditPointWallet wallet, List<CreditPointTransaction> transactions,
			CreditPointTransaction removeTransaction, Date referenceDate) {
		Balance balance = recalculateBalance(wallet, transactions, removeTransaction, referenceDate);
		wallet.setBalance(balance.total());
		wallet.setBalanceRecalculationRequiredAfter(balance.nextExpiringTransactionDate());
		wallet = creditPointWalletDao.update(wallet);
		dbInstance.commit();
		return wallet;
	}
	
	private Balance recalculateBalance(CreditPointWallet wallet, List<CreditPointTransaction> transactions, CreditPointTransaction removeTransaction, Date referenceDate) {
		List<CreditPointTransaction> referencesTransactions = List.copyOf(transactions);
		Map<CreditPointTransaction, BigDecimal> transactionsToExpire = filterTransactionsSetToExpire(transactions, removeTransaction, referenceDate);
		
		BigDecimal totalAmount = BigDecimal.ZERO;
		Date nextExpirationDate = null;
		
		for(CreditPointTransaction transaction:transactions) {
			BigDecimal amount = transaction.getAmount();
			Date expirationDate = transaction.getExpirationDate();
			CreditPointTransactionType type = transaction.getTransactionType();
			
			totalAmount = totalAmount.add(amount);
			
			if(type == CreditPointTransactionType.deposit
					&& transaction.getRemainingAmount() != null
					&& transaction.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0
					&& expirationDate != null && expirationDate.compareTo(referenceDate) < 0) {
				
				//Expired transaction
				CreditPointTransaction expiredTrace = findReference(transaction, referencesTransactions);
				if(expiredTrace == null || expiredTrace.getTransactionType() != CreditPointTransactionType.expiration) {
					BigDecimal amountToExpire = transaction.getRemainingAmount();
					amountToExpire = amountToExpire.negate();
					transactionDao.createTransaction(CreditPointTransactionType.expiration, amountToExpire, null,
							null, null, null, wallet, null, null, null, null, transaction);
					
					totalAmount = totalAmount.add(amountToExpire);
				}
			} else if(transactionsToExpire.containsKey(transaction)) {
				BigDecimal remaining = transactionsToExpire.get(transaction);
				transactionDao.updateRemaingAmount(remaining, transaction);
				transactionDetailsDao.createTransactionDetails(totalAmount, removeTransaction, transaction);
			}
			
			if(expirationDate != null && (nextExpirationDate == null || nextExpirationDate.after(expirationDate))) {
				nextExpirationDate = expirationDate;
			}
		}
		
		return new Balance(totalAmount, nextExpirationDate);
	}
	
	/**
	 * 
	 * @param transactions The list of transactions
	 * @param amountToRemove The amount to remove if any
	 * @param referenceDate
	 * @return
	 */
	protected final Map<CreditPointTransaction, BigDecimal> filterTransactionsSetToExpire(List<CreditPointTransaction> transactions,
			CreditPointTransaction removeTransaction, Date referenceDate) {
		if(removeTransaction == null || removeTransaction.getAmount() == null) return Map.of();
		
		List<CreditPointTransaction> transactionsToExpire = transactions.stream()
				.filter(trx -> trx.getTransactionType() == CreditPointTransactionType.deposit)
				.filter(trx -> trx.getExpirationDate() != null && trx.getExpirationDate().compareTo(referenceDate) > 0)
				.collect(Collectors.toList());
		if(transactionsToExpire.size() > 1) {
			Collections.sort(transactionsToExpire, (t1, t2) -> 
				t1.getExpirationDate().compareTo(t2.getExpirationDate())
			);
		}
		
		BigDecimal amountToRemove = removeTransaction.getAmount();
		amountToRemove = amountToRemove.abs();
		
		Map<CreditPointTransaction, BigDecimal> transactionsToUpdate = new HashMap<>();
		for(CreditPointTransaction transactionToExpire:transactionsToExpire) {
			BigDecimal remainingAmout = transactionToExpire.getRemainingAmount();
			
			if(remainingAmout != null && BigDecimal.ZERO.compareTo(amountToRemove) < 0) {
				BigDecimal resulting;
				int compare = remainingAmout.compareTo(amountToRemove);
				if(compare == 0) {
					resulting = amountToRemove;
					amountToRemove = BigDecimal.ZERO;
				} else if(compare > 0) {
					resulting = remainingAmout.subtract(amountToRemove);
					amountToRemove = BigDecimal.ZERO;
				} else {
					resulting = BigDecimal.ZERO;
					amountToRemove = amountToRemove.subtract(remainingAmout);
				}
				transactionsToUpdate.put(transactionToExpire, resulting);
			}
		}
		return transactionsToUpdate;
	}
	
	private CreditPointTransaction findReference(CreditPointTransaction transaction, List<CreditPointTransaction> referencesTransactions) {
		return referencesTransactions.stream()
				.filter(ref -> transaction.equals(ref.getTransactionReference()))
				.findFirst().orElse(null);
	}
	
	private record Balance(BigDecimal total, Date nextExpiringTransactionDate) {
		//
	}

	@Override
	public void calculateBalance(Date referenceDate) {
		List<CreditPointWallet> wallets = creditPointWalletDao.getWalletsForBalanceRecalculation(referenceDate);
		dbInstance.commit();
		log.info("{} wallets need recalculation of their balances", wallets.size());
		
		for(CreditPointWallet wallet:wallets) {
			recalculateBalance(wallet, referenceDate);
		}
	}
	
	protected CreditPointWallet recalculateBalance(CreditPointWallet wallet, Date referenceDate) {
		wallet = creditPointWalletDao.loadForUpdate(wallet);
		List<CreditPointTransaction> transactions = transactionDao.loadTransactions(wallet);
		return calculateUpdateAndCommitBalance(wallet, transactions, null, referenceDate);
	}
	
	@Override
	public CreditPointTransactionAndWallet cancelCreditPointTransaction(CreditPointWallet wallet, CreditPointTransaction transactionToCancel, Identity actor) {
		BigDecimal reversedAmount = null;
		if(transactionToCancel.getTransactionType() == CreditPointTransactionType.deposit) {
			reversedAmount = transactionToCancel.getRemainingAmount() == null
					? transactionToCancel.getAmount()
					: transactionToCancel.getRemainingAmount();
			reversedAmount = reversedAmount.negate();
		} else if(transactionToCancel.getTransactionType() == CreditPointTransactionType.withdrawal
				|| transactionToCancel.getTransactionType() == CreditPointTransactionType.removal) {
			reversedAmount = transactionToCancel.getAmount()
					.abs();
		}
		
		if(reversedAmount != null) {
			return createCreditPointTransaction(CreditPointTransactionType.reversal, reversedAmount, null, null,
				wallet, actor, null, null, null, null, transactionToCancel);
		}
		return null;
	}

	@Override
	public List<CreditPointTransactionWithInfos> getCreditPointTransactions(CreditPointWallet wallet) {
		return transactionDao.loadTransactionsWithInfos(wallet);
	}
	
	@Override
	public CreditPointTransaction nextExpiringCreditPointTransactions(CreditPointWallet wallet, Date referenceDate) {
		return transactionDao.nextExpiringTransaction(wallet, referenceDate);
	}

	@Override
	public List<CreditPointTransactionDetails> getCreditPointTransactionsDetails(CreditPointTransaction transaction) {
		return transactionDetailsDao.loadTransactionDetails(transaction);
	}
	
	@Override
	public RepositoryEntryCreditPointConfiguration getConfiguration(RepositoryEntry entry) {
		return repositoryEntryConfigurationDao.loadConfiguration(entry);
	}

	@Override
	public RepositoryEntryCreditPointConfiguration getOrCreateConfiguration(RepositoryEntry entry) {
		RepositoryEntryCreditPointConfiguration config = repositoryEntryConfigurationDao.loadConfiguration(entry);
		if(config == null) {
			config = repositoryEntryConfigurationDao.createConfiguration(entry, null);
			dbInstance.commit();
		}
		return config;
	}

	@Override
	public RepositoryEntryCreditPointConfiguration updateConfiguration(RepositoryEntryCreditPointConfiguration config) {
		return repositoryEntryConfigurationDao.updateConfiguration(config);
	}

	@Override
	public CurriculumElementCreditPointConfiguration getConfiguration(CurriculumElement element) {
		CurriculumElementCreditPointConfiguration config = curriculumElementConfigurationDao.loadConfiguration(element);
		if(config == null) {
			config = curriculumElementConfigurationDao.createConfiguration(element, null);
			dbInstance.commit();
		}
		return config;
	}

	@Override
	public CurriculumElementCreditPointConfiguration updateConfiguration(CurriculumElementCreditPointConfiguration config) {
		return curriculumElementConfigurationDao.updateConfiguration(config);
	}

	@Override
	public boolean isTransfertAllowed(IdentityRef assessedIdentity, OLATResource resource, int run) {
		boolean hasAlreadTransaction = transactionDao.hasTransaction(assessedIdentity, resource, Integer.valueOf(run));
		return !hasAlreadTransaction;
	}

	@Override
	public Date calculateExpirationDate(Integer value, CreditPointExpirationType unit, Date referenceDate, CreditPointSystem system) {
		if(unit == CreditPointExpirationType.DEFAULT) {
			value = system.getDefaultExpiration();
			unit = system.getDefaultExpirationUnit();
		}

		Date expirationDate = null;
		if(value != null && unit != null) {
			switch(unit) {
				case DAY: expirationDate = DateUtils.addDays(referenceDate, value.intValue()); break;
				case MONTH: expirationDate = DateUtils.addMonth(referenceDate, value.intValue()); break;
				case YEAR: expirationDate = DateUtils.addYears(referenceDate, value.intValue()); break;
				default: expirationDate = null; break;
			}
		}
		if(expirationDate != null) {
			expirationDate = DateUtils.getEndOfDay(expirationDate);
		}
		return expirationDate;
	}
}
