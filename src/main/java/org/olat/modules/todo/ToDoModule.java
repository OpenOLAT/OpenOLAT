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
package org.olat.modules.todo;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.Roles;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 Ocs 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ToDoModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String KEY_PERSONAL_CREATE = "todo.personal.create";
	public static final String PERSONAL_CREATE_ALL = "all";
	public static final String PERSONAL_CREATE_AUTHORS_MANAGERS = "authorsManagers";
	public static final String PERSONAL_CREATE_NONE = "none";
	private static final String KEY_PERSONAL_ASSIGNEE_CANDIDATE = "todo.personal.assignee.candidate";
	private static final String KEY_PERSONAL_DELEGATEE_CANDIDATE = "todo.personal.delegatee.candidate";
	public static final String PERSONAL_CANDIDATE_ALL = "all";
	public static final String PERSONAL_CANDIDATE_BUDDIES = "buddies";
	public static final String PERSONAL_CANDIDATE_NONE = "none";

	@Value("${todo.personal.create}")
	private String personalCreate;
	@Value("${todo.personal.assignee.candidate}")
	private String personalAssigneeCandidate;
	@Value("${todo.personal.delegatee.candidate}")
	private String personalDelegateeCandidate;
	
	@Autowired
	public ToDoModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		personalCreate = getStringPropertyValue(KEY_PERSONAL_CREATE, personalCreate);
		personalAssigneeCandidate = getStringPropertyValue(KEY_PERSONAL_ASSIGNEE_CANDIDATE, personalAssigneeCandidate);
		personalDelegateeCandidate = getStringPropertyValue(KEY_PERSONAL_DELEGATEE_CANDIDATE, personalDelegateeCandidate);
		
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public boolean canCreatePersonalToDoTasks(Roles roles) {
		if (PERSONAL_CREATE_NONE.equals(personalCreate)) {
			return false;
		}
		if (PERSONAL_CREATE_AUTHORS_MANAGERS.equals(personalCreate)) {
			return roles.isManager() || roles.isAuthor();
		}
		return true;
	}

	public String getPersonalCreate() {
		return personalCreate;
	}

	public void setPersonalCreate(String personalCreate) {
		this.personalCreate = personalCreate;
		setStringProperty(KEY_PERSONAL_CREATE, personalCreate, true);
	}

	public String getPersonalAssigneeCandidate() {
		return personalAssigneeCandidate;
	}

	public void setPersonalAssigneeCandidate(String personalAssigneeCandidate) {
		this.personalAssigneeCandidate = personalAssigneeCandidate;
		setStringProperty(KEY_PERSONAL_ASSIGNEE_CANDIDATE, personalAssigneeCandidate, true);
	}

	public String getPersonalDelegateeCandidate() {
		return personalDelegateeCandidate;
	}

	public void setPersonalDelegateeCandidate(String personalDelegateeCandidate) {
		this.personalDelegateeCandidate = personalDelegateeCandidate;
		setStringProperty(KEY_PERSONAL_DELEGATEE_CANDIDATE, personalDelegateeCandidate, true);
	}
	
}
