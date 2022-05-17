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
package org.olat.course.nodes.practice.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.model.PracticeAssessmentItemGlobalRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PracticeAssessmentItemGlobalRefDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PracticeAssessmentItemGlobalRef createAssessmentItemGlobalRefDAO(Identity identity, String identifier) {
		PracticeAssessmentItemGlobalRefImpl ref = new PracticeAssessmentItemGlobalRefImpl();
		ref.setCreationDate(new Date());
		ref.setLastModified(ref.getCreationDate());
		ref.setIdentifier(identifier);
		ref.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(ref);
		return ref;
	}
	
	public List<PracticeAssessmentItemGlobalRef> getAssessmentItemGlobalRefByUuids(IdentityRef identity, List<String> identifiers) {
		if(identifiers == null || identifiers.isEmpty()) {
			return new ArrayList<>();
		}
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select itemRef from practiceglobalitemref as itemRef")
		  .append(" where itemRef.identity.key=:identityKey and itemRef.identifier in (:identifiers)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PracticeAssessmentItemGlobalRef.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("identifiers", identifiers)
				.getResultList();
	}
	
	public PracticeAssessmentItemGlobalRef getAssessmentItemGlobalRefByUuid(IdentityRef identity, String identifier) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select itemRef from practiceglobalitemref as itemRef")
		  .append(" where itemRef.identity.key=:identityKey and itemRef.identifier=:identifier");
		
		List<PracticeAssessmentItemGlobalRef> globalRefs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PracticeAssessmentItemGlobalRef.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("identifier", identifier)
				.getResultList();
		return globalRefs == null || globalRefs.isEmpty() ? null : globalRefs.get(0);
	}
	
	public PracticeAssessmentItemGlobalRef updateAssessmentItemGlobalRef(PracticeAssessmentItemGlobalRef globalRef) {
		globalRef.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(globalRef);	
	}
}
