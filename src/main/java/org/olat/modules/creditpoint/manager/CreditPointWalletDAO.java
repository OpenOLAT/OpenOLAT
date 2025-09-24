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

import jakarta.persistence.LockModeType;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointWalletImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CreditPointWalletDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CreditPointWallet createWallet(Identity owner, CreditPointSystem system) {
		CreditPointWalletImpl wallet = new CreditPointWalletImpl();
		wallet.setCreationDate(new Date());
		wallet.setLastModified(wallet.getCreationDate());
		wallet.setBalance(BigDecimal.ZERO);
		wallet.setIdentity(owner);
		wallet.setCreditPointSystem(system);
		dbInstance.getCurrentEntityManager().persist(wallet);
		return wallet;
	}
	
	public CreditPointWallet getWallet(IdentityRef owner, CreditPointSystem system) {
		String query = """
				select wallet from creditpointwallet as wallet
				inner join fetch wallet.creditPointSystem as sys
				inner join fetch wallet.identity as ident
				where sys.key=:systemKey and ident.key=:identityKey""";
		
		List<CreditPointWallet> wallets = dbInstance.getCurrentEntityManager().createQuery(query, CreditPointWallet.class)
				.setParameter("identityKey", owner.getKey())
				.setParameter("systemKey", system.getKey())
				.getResultList();
		return wallets == null || wallets.isEmpty() ? null : wallets.get(0);
	}
	
	public CreditPointWallet loadForUpdate(CreditPointWallet wallet) {
		//first remove it from caches
		dbInstance.getCurrentEntityManager().detach(wallet);

		String query = """
			select wallet from creditpointwallet as wallet
			where wallet.key=:walletKey""";

		List<CreditPointWallet> wallets = dbInstance.getCurrentEntityManager().createQuery(query, CreditPointWallet.class)
				.setParameter("walletKey", wallet.getKey())
				.getResultList();
		if(wallets.size() == 1) {
			CreditPointWallet loadedWallet = wallets.get(0);
			dbInstance.getCurrentEntityManager().lock(loadedWallet, LockModeType.PESSIMISTIC_WRITE);
			return loadedWallet;
		}
		return null;
	}
	
	public CreditPointWallet update(CreditPointWallet wallet) {
		wallet.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(wallet);
	}
	
	public List<CreditPointWallet> getWalletsForBalanceRecalculation(Date referenceDate) {
		String query = """
				select wallet from creditpointwallet as wallet
				inner join fetch wallet.creditPointSystem as sys
				inner join fetch wallet.identity as ident
				where wallet.balanceRecalculationRequiredAfter<:referenceDate""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointWallet.class)
				.setParameter("referenceDate", referenceDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<CreditPointWallet> loadWalletOfCertificationProgram(CertificationProgramRef  certificationProgram) {
		String query = """
				select wallet from creditpointwallet as wallet
				inner join wallet.identity ident
				inner join fetch wallet.creditPointSystem system
				where exists (select membership.key from bgroupmember as membership
				 inner join curriculumelement curEl on (membership.group.key=curEl.group.key)
				 inner join certificationprogramtoelement as rel on (rel.curriculumElement.key=curEl.key)
				 inner join certificationprogram as program on (rel.certificationProgram.key=program.key)
				 where program.key=:programKey and program.creditPointSystem.key=system.key
				 and membership.identity.key=ident.key and membership.role=:role
				) """;

		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointWallet.class)
				.setParameter("programKey", certificationProgram.getKey())
				.setParameter("role", GroupRoles.participant.name())
				.getResultList();
	}

}
