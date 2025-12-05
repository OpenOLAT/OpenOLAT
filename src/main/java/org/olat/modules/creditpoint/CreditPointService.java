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
package org.olat.modules.creditpoint;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.creditpoint.model.CreditPointSystemInfos;
import org.olat.modules.creditpoint.model.CreditPointTransactionAndWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionWithInfos;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface CreditPointService {
	
	/**
	 * 
	 * @param name The name of the credit point system
	 * @param label A very short label
	 * @param defaultExpiration The default expiration specified in days
	 * @return A persisted credit point system
	 */
	CreditPointSystem createCreditPointSystem(String name, String label,
			Integer defaultExpiration, CreditPointExpirationType defaultExpirationType,
			boolean rolesRestrictions, boolean organisationsRestrictions);
	
	CreditPointSystem updateCreditPointSystem(CreditPointSystem creditPointSystem);
	
	void updateCreditPointSystemOrganisations(CreditPointSystem creditPointSystem, Collection<Organisation> organisations);
	
	CreditPointSystem loadCreditPointSystem(CreditPointSystem creditPointSystem);
	
	List<CreditPointSystem> getCreditPointSystems();
	
	List<CreditPointSystem> getActiveCreditPointSystems();
	
	/**
	 * The specified user list of credit point systems which he has at least one
	 * transaction and in a certification program which use this system.
	 * 
	 * @param identity The identity
	 * @return A list of credit point systems
	 */
	List<CreditPointSystem> getCreditPointSystems(IdentityRef identity);
	
	List<CreditPointSystem> getCreditPointSystems(Roles roles);
	
	List<CreditPointSystemInfos> getCreditPointSystemsWithInfos();
	
	
	CreditPointWallet getOrCreateWallet(Identity identity, CreditPointSystem system);
	
	List<CreditPointWallet> getWallets(IdentityRef identity);
	
	
	CreditPointTransactionAndWallet createCreditPointTransaction(CreditPointTransactionType transactionType,
			BigDecimal amount, Date expirationDate, String note, CreditPointWallet wallet, Identity creator, 
			OLATResource transfertOrigin, Integer originRun, OLATResource transfertDestination, Integer destinationRun,
			CreditPointTransaction transactionReference);
	
	
	CreditPointTransactionAndWallet cancelCreditPointTransaction(CreditPointWallet wallet, CreditPointTransaction transactionReference, Identity actor);
	
	/**
	 * List the transactions hold by the specified wallet.
	 * 
	 * @param wallet The wallet
	 * @return A list of transactions
	 */
	List<CreditPointTransactionWithInfos> getCreditPointTransactions(CreditPointWallet wallet);
	
	CreditPointTransaction nextExpiringCreditPointTransactions(CreditPointWallet wallet, Date referenceDate);
	
	void calculateBalance(Date referenceDate);
	
	
	List<CreditPointTransactionDetails> getCreditPointTransactionsDetails(CreditPointTransaction transaction);
	
	
	boolean isTransfertAllowed(IdentityRef assessedIdentity, OLATResource resource, int run);
	
	Date calculateExpirationDate(Integer value, CreditPointExpirationType unit, Date referenceDate, CreditPointSystem system);
	

	RepositoryEntryCreditPointConfiguration getConfiguration(RepositoryEntry entry);
	
	RepositoryEntryCreditPointConfiguration getOrCreateConfiguration(RepositoryEntry entry);
	
	RepositoryEntryCreditPointConfiguration updateConfiguration(RepositoryEntryCreditPointConfiguration config);
	
	CurriculumElementCreditPointConfiguration getConfiguration(CurriculumElement element);
	
	CurriculumElementCreditPointConfiguration updateConfiguration(CurriculumElementCreditPointConfiguration config);

}
