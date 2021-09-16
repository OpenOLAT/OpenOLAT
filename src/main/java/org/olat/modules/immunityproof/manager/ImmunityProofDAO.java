/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.immunityproof.manager;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.immunityproof.model.ImmunityProofImpl;
import org.olat.modules.immunityproof.ImmunityProof;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ImmunityProofDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ImmunityProof createImmunityProof(Identity identity, Date safeDate, boolean sendMail, boolean validated) {
		ImmunityProofImpl immunityProof = new ImmunityProofImpl();
		immunityProof.setCreationDate(new Date());
		immunityProof.setIdentity(identity);
		immunityProof.setSafeDate(safeDate);
		immunityProof.setSendMail(sendMail);
		immunityProof.setValidated(validated);
		immunityProof.setMailSent(false);
		
		dbInstance.getCurrentEntityManager().persist(immunityProof);
		return immunityProof;
	}
	
	public ImmunityProof updateImmunityProof(ImmunityProof immunityProof) {
		if (immunityProof == null || immunityProof.getKey() == null) {
			return null;
		}
		
		return dbInstance.getCurrentEntityManager().merge(immunityProof);
	}

	public ImmunityProof getImmunitiyProof(IdentityRef identityRef) {
		StringBuilder sb = new StringBuilder(128);
		
		sb.append("select proof from immunityProof as proof ")
		  .append("where proof.identity.key =: identityKey");
		
		TypedQuery<ImmunityProof> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ImmunityProof.class);
		
		query.setParameter("identityKey", identityRef.getKey());
		
		List<ImmunityProof> results = query.getResultList();
		
		if (results.isEmpty()) {
			return null;
		}
		
		// Edge case, should not be possible
		// If more than one immunity proof is found, return the one with longest validity
		if (results.size() > 1) {
			Comparator<ImmunityProof> immunityProofComparator = Comparator.comparing(ImmunityProof::getSafeDate);
			return results.stream().max(immunityProofComparator).get();
		}
		
		return results.get(0);
	}
	
	public void deleteImmunityProof(IdentityRef identityRef) {
		if (identityRef == null) {
			return;
		}
		
		String query = new StringBuilder()
                .append("delete from immunityProof as entry ")
                .append("where entry.identity.key = :identityKey")
                .toString();

        dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("identityKey", identityRef.getKey())
                .executeUpdate();
	}
	
	public void deleteImmunityProof(ImmunityProof immunityProof) {
		if (immunityProof == null) {
			return;
		}
		
		String query = new StringBuilder()
                .append("delete from immunityProof as entry ")
                .append("where entry.key = :immunityProofKey")
                .toString();

        dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("immunityProofKey", immunityProof.getKey())
                .executeUpdate();
	}
	
	public void pruneImmunityProofs(Date pruneUntil) {
		if (pruneUntil == null) {
			return;
		}
		
		String query = new StringBuilder()
                .append("delete from immunityProof as entry ")
                .append("where entry.safeDate < :pruneUntil")
                .toString();

        dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("pruneUntil", pruneUntil)
                .executeUpdate();
	}
	
	public void deleteAllImmunityProofs() {
		String query = new StringBuilder()
                .append("delete from immunityProof")
                .toString();

        dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .executeUpdate();
	}
	
	public List<ImmunityProof> getAllCertificates() {
		StringBuilder sb = new StringBuilder(128);
		
		sb.append("select from immunityProof");
		
		TypedQuery<ImmunityProof> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ImmunityProof.class);
		
		return query.getResultList();
	}
}
