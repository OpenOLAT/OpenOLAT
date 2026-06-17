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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.model.references.ReferenceToApplicationImpl;

/**
 * 
 * Initial date: 6 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("referenceToApplicationDao")
public class ReferenceToApplicationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ReferenceToApplication createRelation(Reference reference, Application application) {
		ReferenceToApplicationImpl rel = new ReferenceToApplicationImpl();
		rel.setCreationDate(new Date());
		rel.setApplication(application);
		rel.setReference(reference);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<ReferenceToApplication> getReferenceToApplications(Reference reference) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select reftoapp from rreferencetoapp reftoapp")
		  .append(" inner join fetch reftoapp.application as app")
		  .and().append(" reftoapp.reference.key=:referenceKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ReferenceToApplication.class)
				.setParameter("referenceKey", reference.getKey())
				.getResultList();
	}
	
	public List<ReferenceToApplication> getReferenceToApplications(List<Reference> references) {
		if(references == null || references.isEmpty()) return new ArrayList<>();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select reftoapp from rreferencetoapp reftoapp")
		  .append(" inner join fetch reftoapp.application as app")
		  .and().append(" reftoapp.reference.key in :referencesKeys");
		
		List<Long> referencesKeys = references.stream()
				.map(Reference::getKey)
				.collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ReferenceToApplication.class)
				.setParameter("referencesKeys", referencesKeys)
				.getResultList();
	}
	
	public List<Application> getReferenceToApplicationsList(Reference reference) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select app from rreferencetoapp reftoapp")
		  .append(" inner join reftoapp.application as app")
		  .and().append(" reftoapp.reference.key=:referenceKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Application.class)
				.setParameter("referenceKey", reference.getKey())
				.getResultList();
	}
	
	public List<ReferenceToApplication> getReferenceToApplications(Reference reference, Application application) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select reftoapp from rreferencetoapp reftoapp")
		  .append(" inner join reftoapp.application as app")
		  .and().append(" reftoapp.reference.key=:referenceKey")
		  .and().append(" reftoapp.application.key=:applicationKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ReferenceToApplication.class)
				.setParameter("referenceKey", reference.getKey())
				.setParameter("applicationKey", application.getKey())
				.getResultList();
	}
	
	public List<ReferenceToApplication> getReferenceToApplications(PositionRef position) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select reftoapp from rreferencetoapp reftoapp")
		  .append(" inner join fetch reftoapp.application as app")
		  .and().append(" app.position.key=:positionKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ReferenceToApplication.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public void deleteReferenceToApplications(Reference reference) {
		List<ReferenceToApplication> refToApps = getReferenceToApplications(reference);
		for(ReferenceToApplication refToApp:refToApps ) {
			dbInstance.getCurrentEntityManager().remove(refToApp);
		}
	}
	
	public int deleteReferencesToApplication(Application application) {
		String q = "delete from rreferencetoapp reftoapp where reftoapp.application.key=:applicationKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("applicationKey", application.getKey())
			.executeUpdate();
	}
	
	public void deleteReferenceToApplication(ReferenceToApplication refToApp) {
		dbInstance.getCurrentEntityManager().remove(refToApp);
	}
}
