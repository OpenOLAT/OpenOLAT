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
package org.olat.modules.quality.manager;

import java.util.Collection;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoTaskSearchParams.ToDoTaskCustomQuery;

/**
 * 
 * Initial date: 24 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityToDoTaskQuery implements ToDoTaskCustomQuery {
	
	private final Long memberKey;
	private final boolean allGeneralToDoTasks;
	private final Collection<Long> originIds;
	
	public Collection<Long> getOriginIds() {
		return originIds;
	}

	public QualityToDoTaskQuery(Identity member, boolean allGeneralToDoTasks, Collection<Long> originIds) {
		this.memberKey = member.getKey();
		this.allGeneralToDoTasks = allGeneralToDoTasks;
		this.originIds = originIds;
	}

	@Override
	public void appendQuery(QueryBuilder sb) {
		sb.and().append("(");
		
		// member
		sb.append("toDoTask.type").in(GeneralToDoTaskProvider.TYPE, DataCollectionToDoTaskProvider.TYPE, EvaluationFormSessionToDoTaskProvider.TYPE);
		sb.append("and toDoTask.baseGroup.key in (");
		sb.append("select membership.group.key");
		sb.append("  from bgroupmember as membership");
		sb.append(" where membership.group.key = toDoTask.baseGroup.key");
		sb.append("   and membership.identity.key = :qualMemberKey");
		sb.append("   and membership.role").in(ToDoRole.assignee, ToDoRole.delegatee);
		sb.append(")");
		
		// quality manager etc
		sb.append(" or ");
		sb.append("toDoTask.type").in(DataCollectionToDoTaskProvider.TYPE, EvaluationFormSessionToDoTaskProvider.TYPE);
		sb.append(" and toDoTask.originId in :qualOriginIds");
		
		// all general qm todos
		if (allGeneralToDoTasks) {
			sb.append(" or ");
			sb.append("toDoTask.type = '").append(GeneralToDoTaskProvider.TYPE).append("'");
		}
		
		sb.append(")");
	}

	@Override
	public void addParameters(TypedQuery<?> query) {
		query.setParameter("qualMemberKey", memberKey);
		query.setParameter("qualOriginIds", originIds);
	}

}
