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
package org.olat.modules.curriculum.manager;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.todo.ToDoTaskSearchParams.ToDoTaskCustomQuery;

/**
 * Restricts to-do tasks to curriculum elements by a subselect, so the element
 * keys are never bound as parameters. The elements can be limited to the
 * elements a manager has access to and to an element with or without its descendants.
 *
 * Initial date: 2 Oct 2026<br>
 * @author uhensler, https://www.frentix.com
 *
 */
public class CurriculumElementToDoTaskQuery implements ToDoTaskCustomQuery {

	private final Long managerKey;
	private final CurriculumElement element;
	private final boolean descendants;

	private CurriculumElementToDoTaskQuery(Identity manager, CurriculumElement element, boolean descendants) {
		this.managerKey = manager != null ? manager.getKey() : null;
		this.element = element;
		this.descendants = descendants;
	}

	public static CurriculumElementToDoTaskQuery accessibleBy(Identity manager) {
		return new CurriculumElementToDoTaskQuery(manager, null, false);
	}

	public static CurriculumElementToDoTaskQuery accessibleBy(Identity manager, CurriculumElement element, boolean descendants) {
		return new CurriculumElementToDoTaskQuery(manager, element, descendants);
	}

	public static CurriculumElementToDoTaskQuery ofElementAndDescendants(CurriculumElement element) {
		return new CurriculumElementToDoTaskQuery(null, element, true);
	}

	@Override
	public void appendQuery(QueryBuilder sb) {
		QueryBuilder elements = new QueryBuilder(1024);
		elements.append("select el.key from curriculumelement el");
		if (managerKey != null) {
			elements.append(" inner join el.curriculum curriculum")
			  .append(" inner join el.group baseGroup")
			  .append(" left join curriculum.organisation organis");
		}
		elements.where().append("el.status ").in(CurriculumElementStatus.notDeleted());
		if (element != null) {
			elements.and().append(descendants
					? "(el.key=:elementKey or el.materializedPathKeys like :elementPath)"
					: "el.key=:elementKey");
		}
		if (managerKey != null) {
			CurriculumElementDAO.appendManagerAccess(elements, false);
		}

		sb.and().append("(case when toDoTask.type = '").append(CurriculumElementToDoProvider.TYPE).append("'")
		  .append(" then cast(toDoTask.originSubPath as long) end) in (").append(elements.toString()).append(")");
	}

	@Override
	public void addParameters(TypedQuery<?> query) {
		if (managerKey != null) {
			query.setParameter("managerKey", managerKey);
		}
		if (element != null) {
			query.setParameter("elementKey", element.getKey());
			if (descendants) {
				query.setParameter("elementPath", element.getMaterializedPathKeys() + "%");
			}
		}
	}

}
