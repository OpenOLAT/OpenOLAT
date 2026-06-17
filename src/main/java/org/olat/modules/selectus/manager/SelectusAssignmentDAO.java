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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAssignment;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.assignment.ApplicationAssignmentImpl;
import org.olat.modules.selectus.model.assignment.ApplicationAssignmentLightImpl;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SelectusAssignmentDAO {

	@Autowired
	private DB dbInstance;
	
	public ApplicationAssignment createAssignment(Identity identity, Application application) {
		ApplicationAssignmentImpl assignment = new ApplicationAssignmentImpl();
		assignment.setCreationDate(new Date());
		assignment.setApplication(application);
		assignment.setAssignee(identity);
		dbInstance.getCurrentEntityManager().persist(assignment);
		return assignment;
	}
	
	public List<ApplicationAssignmentLight> getAssignmentPosition(PositionRef position) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment.key, assignment.application.key, assignment.assignee.key from appassignment as assignment")
		  .append(" inner join assignment.application as app")
		  .append(" where app.position.key=:positionKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
		List<ApplicationAssignmentLight> assignments = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			Long assignmentKey = (Long)rawObject[0];
			Long applicationKey = (Long)rawObject[1];
			Long assigneeKey = (Long)rawObject[2];
			assignments.add(new ApplicationAssignmentLightImpl(assignmentKey, applicationKey, assigneeKey));
		}
		return assignments;
	}
	
	public List<ApplicationAssignment> getAssignments(PositionRef position, IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from appassignment as assignment")
		  .append(" inner join assignment.application as app")
		  .append(" where app.position.key=:positionKey and assignment.assignee.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationAssignment.class)
				.setParameter("positionKey", position.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<Identity> getAssignees(ApplicationRef application) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignee from appassignment as assignment")
		  .append(" inner join assignment.assignee assignee")
		  .append(" inner join fetch assignee.user assigneeUser")
		  .append(" where assignment.application.key=:applicationKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
	}
	

	public void removeAssignment(Long assignmentKey) {
		ApplicationAssignment assignment = dbInstance.getCurrentEntityManager()
				.getReference(ApplicationAssignmentImpl.class, assignmentKey);
		dbInstance.getCurrentEntityManager().remove(assignment);
	}
	
	public void removeAssignee(Position position, Identity assignee) {
		List<ApplicationAssignment> assignments = getAssignments(position, assignee);
		for(ApplicationAssignment assignment:assignments) {
			dbInstance.getCurrentEntityManager().remove(assignment);
		}
	}
	
	public void deleteApplication(Application application) {
		String q = "delete from appassignment as assignment where assignment.application.key=:applicationKey";
		dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("applicationKey", application.getKey())
			.executeUpdate();
	}
}
