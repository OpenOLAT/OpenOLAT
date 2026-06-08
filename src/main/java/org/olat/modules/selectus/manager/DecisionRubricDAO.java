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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.DecisionRubricDefinitionImpl;
import org.olat.modules.selectus.model.DecisionRubricImpl;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 17 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DecisionRubricDAO {
	
	@Autowired
	private DB dbInstance;
	
	public DecisionRubricDefinition createDecisionRubricDefinition() {
		DecisionRubricDefinitionImpl def = new DecisionRubricDefinitionImpl();
		def.setCreationDate(new Date());
		def.setLastModified(def.getCreationDate());
		return def;
	}
	
	public DecisionRubricDefinition saveDefinition(DecisionRubricDefinition def, Position position) {
		def.setLastModified(new Date());
		if(def.getKey() == null) {
			((DecisionRubricDefinitionImpl)def).setPosition(position);
			dbInstance.getCurrentEntityManager().persist(def);
		} else {
			def = dbInstance.getCurrentEntityManager().merge(def);
		}
		return def;
	}
	
	public List<DecisionRubricDefinition> getDecisionRubricDefinition(Position position) {
		StringBuilder sb = new StringBuilder();
		sb.append("select defs from rdecisionrubricdef defs ")
		  .append(" where defs.position.key=:positionKey")
		  .append(" order by defs.pos asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DecisionRubricDefinition.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public int deleteRubrics(Application app) {	
		String sbr = "delete from rdecisionrubric as rubrics where rubrics.application.key=:appKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sbr)
				.setParameter("appKey", app.getKey())
				.executeUpdate();
	}
	
	public int deleteDefinition(DecisionRubricDefinition def) {
		int row = 0;
		
		String sbr = "delete from rdecisionrubric as rubrics where rubrics.definition.key=:definitionKey";
		row += dbInstance.getCurrentEntityManager()
				.createQuery(sbr)
				.setParameter("definitionKey", def.getKey())
				.executeUpdate();

		String sb = "delete from rdecisionrubricdef as defs where defs.key=:definitionKey";
		row += dbInstance.getCurrentEntityManager()
				.createQuery(sb)
				.setParameter("definitionKey", def.getKey())
				.executeUpdate();
		return row;
	}
	
	public DecisionRubric createDecisionRubric(DecisionRubricDefinition definition, ApplicationLight app) {
		DecisionRubricImpl decision = new DecisionRubricImpl();
		decision.setCreationDate(new Date());
		decision.setLastModified(decision.getCreationDate());
		decision.setApplication(app);
		decision.setDefinition(definition);
		return decision;
	}
	
	public DecisionRubric saveDecisionRubric(DecisionRubric decision) {
		decision.setLastModified(new Date());
		if(decision.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(decision);
		} else {
			decision = dbInstance.getCurrentEntityManager().merge(decision);
		}
		return decision;
	}
	
	public DecisionRubric loadDecisionRubric(Long rubricKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rubrics from rdecisionrubric as rubrics")
		  .append(" inner join fetch rubrics.definition as def")
		  .append(" inner join fetch rubrics.application as app")
		  .append(" where rubrics.key=:rubricKey");
		List<DecisionRubric> rubrics = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DecisionRubric.class)
				.setParameter("rubricKey", rubricKey)
				.getResultList();
		return rubrics.size() > 0 ? rubrics.get(0) : null;
	}
	
	public List<DecisionRubric> getDecisionRubric(Position position) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rubrics from rdecisionrubric as rubrics")
		  .append(" inner join fetch rubrics.definition as def")
		  .append(" inner join fetch rubrics.application as app")
		  .append(" where def.position.key=:positionKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DecisionRubric.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
}
