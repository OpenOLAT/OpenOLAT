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
package org.olat.basesecurity.manager;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.model.RelationRightImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RelationRightDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RelationRight createRelationRight(String right) {
		RelationRightImpl relationRight = new RelationRightImpl();
		relationRight.setCreationDate(new Date());
		relationRight.setRight(right);
		dbInstance.getCurrentEntityManager().persist(relationRight);
		return relationRight;
	}
	
	public RelationRight loadRelationRightByKey(Long rightKey) {
		List<RelationRight> rights = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadRelationRightByKey", RelationRight.class)
			.setParameter("rightKey", rightKey)
			.getResultList();
		return rights.isEmpty() ? null : rights.get(0);
	}
	
	public RelationRight loadRelationRightByRight(String right) {
		List<RelationRight> rights = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadRelationRightByRight", RelationRight.class)
			.setParameter("right", right)
			.getResultList();
		return rights.isEmpty() ? null : rights.get(0);
	}
	
	public List<RelationRight> loadRelationRights() {
		StringBuilder sb = new StringBuilder();
		sb.append("select relRight from relationright as relRight");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RelationRight.class)
				.getResultList();
	}
	
	/**
	 * This method commits and closes the hibernate session.
	 * 
	 * @param rights The rights to create if they not exists
	 */
	public void ensureRightsExists(Class<? extends Enum<?>> rightsEnum) {
		Enum<?>[] rights = rightsEnum.getEnumConstants();
		
		List<RelationRight> relationRights = loadRelationRights();
		Set<String> rightNames = relationRights.stream()
				.map(RelationRight::getRight).collect(Collectors.toSet());
		
		for(Enum<?> right:rights) {
			if(!rightNames.contains(right.name())) {
				createRelationRight(right.name());
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	/**
	 * This method commits and closes the hibernate session.
	 * 
	 * @param rights The rights to create if they not exists
	 */
	public void ensureRightsExists(String...  rights) {
		if(rights == null || rights.length == 0 || rights[0] == null) return;
		
		List<RelationRight> relationRights = loadRelationRights();
		Set<String> rightNames = relationRights.stream()
				.map(RelationRight::getRight).collect(Collectors.toSet());
		
		for(String right:rights) {
			if(StringHelper.containsNonWhitespace(right) && !rightNames.contains(right)) {
				createRelationRight(right);
			}
		}
		dbInstance.commitAndCloseSession();
	}

}
