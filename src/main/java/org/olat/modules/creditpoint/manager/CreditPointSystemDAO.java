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
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.model.CreditPointSystemImpl;
import org.olat.modules.creditpoint.model.CreditPointSystemWithWalletInfos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CreditPointSystemDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CreditPointSystem createSystem(String name, String label,
			Integer defaultExpiration, CreditPointExpirationType defaultExpirationType,
			boolean rolesRestrictions, boolean organisationsRestrictions) {
		CreditPointSystemImpl system = new CreditPointSystemImpl();
		system.setCreationDate(new Date());
		system.setLastModified(system.getCreationDate());
		system.setName(name);
		system.setLabel(label);
		system.setDefaultExpiration(defaultExpiration);
		system.setDefaultExpirationUnit(defaultExpirationType);
		system.setStatus(CreditPointSystemStatus.active);
		system.setRolesRestrictions(rolesRestrictions);
		system.setOrganisationsRestrictions(organisationsRestrictions);
		system.setOrganisations(new HashSet<>());
		dbInstance.getCurrentEntityManager().persist(system);
		return system;
	}
	
	public CreditPointSystem updateSystem(CreditPointSystem system) {
		((CreditPointSystemImpl)system).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(system);
	}
	
	public List<CreditPointSystem> loadCreditPointSystems() {
		String query = "select sys from creditpointsystem sys";
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointSystem.class)
				.getResultList();
	}
	
	public List<CreditPointSystem> loadActiveCreditPointSystems() {
		return dbInstance.getCurrentEntityManager().createNamedQuery("allActiveCreditPointSystems", CreditPointSystem.class)
				.setParameter("status", CreditPointSystemStatus.active)
				.getResultList();
	}
	
	public List<CreditPointSystem> getCreditPointSystemsWithProgramsOrTransactions(IdentityRef identity) {
		String query = """
				select sys from creditpointsystem sys
				where exists (select trx.key from creditpointtransaction as trx
				  inner join trx.wallet as wallet  
				  where wallet.identity.key=:identityKey and wallet.creditPointSystem.key=sys.key
				) or exists (select cer.key from certificate as cer
				  inner join cer.certificationProgram as program
				  where cer.identity.key=:identityKey and program.creditPointSystem.key=sys.key
				)""";
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointSystem.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<CreditPointSystem> loadCreditPointSystemsFor(List<OrganisationRef> organisations, List<OrganisationRef> restrictedOrganisations) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select sys from creditpointsystem sys")
		  .append(" where sys.organisationsRestrictions=false");
		if(organisations != null && !organisations.isEmpty()) {
			sb.append(" or exists (select rel.key from creditpointsystemtoorganisation as rel")
			  .append("  where sys.organisationsRestrictions=true and sys.rolesRestrictions=false")
			  .append("  and rel.creditPointSystem.key=sys.key and rel.organisation.key in (:organisationsKeys)")
			  .append(" )");
		}
		if(restrictedOrganisations != null && !restrictedOrganisations.isEmpty()) {
			sb.append(" or exists (select restrictedRel.key from creditpointsystemtoorganisation as restrictedRel")
			  .append("  where sys.organisationsRestrictions=true and sys.rolesRestrictions=true")
			  .append("  and restrictedRel.creditPointSystem.key=sys.key and restrictedRel.organisation.key in (:restrictedOrganisationsKeys)")
			  .append(" )");
		}

		TypedQuery<CreditPointSystem> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), CreditPointSystem.class);
		if(organisations != null && !organisations.isEmpty()) {
			List<Long> organisationsKeys = organisations.stream()
					.map(OrganisationRef::getKey)
					.toList();
			query.setParameter("organisationsKeys", organisationsKeys);
		}
		if(restrictedOrganisations != null && !restrictedOrganisations.isEmpty()) {
			List<Long> restrictedOrganisationsKeys = restrictedOrganisations.stream()
					.map(OrganisationRef::getKey)
					.toList();
			query.setParameter("restrictedOrganisationsKeys", restrictedOrganisationsKeys);
		}

		return query.getResultList();
	}
	
	public CreditPointSystem loadCreditPointSystem(Long systemKey) {
		String query = "select sys from creditpointsystem sys where sys.key=:systemKey";
		List<CreditPointSystem> systems = dbInstance.getCurrentEntityManager().createQuery(query, CreditPointSystem.class)
				.setParameter("systemKey", systemKey)
				.getResultList();
		return systems == null || systems.isEmpty() ? null : systems.get(0);
	}
	
	public List<CreditPointSystemWithWalletInfos> loadCreditPointSystemsWithInfos() {
		String query = """
			select new CreditPointSystemWithWalletInfos(sys,
			(select count(wallet.key) from creditpointwallet as wallet
			  where wallet.creditPointSystem.key=sys.key
			))
			from creditpointsystem sys""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointSystemWithWalletInfos.class)
				.getResultList();
	}
}
