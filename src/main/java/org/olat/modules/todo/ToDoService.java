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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ToDoService {
	
	public ToDoProvider getProvider(String type);
	
	public List<ToDoProvider> getProviders();
	
	public ToDoContextFilter getProviderContextFilter(String providerType);
	
	public List<ToDoContextFilter> getContextFilters();
	
	public ToDoTask createToDoTask(Identity doer, String type);
	
	public ToDoTask createToDoTask(Identity doer, String type, Long originId, String originSubPath, String originTitle, String originSubTitle, ToDoTask collection);
	
	public ToDoTask update(Identity doer, ToDoTask toDoTask, ToDoStatus previousStatus);

	public void updateOriginTitle(String type, Long originId, String originSubPath, String originTitle, String originSubTitle);
	
	public void updateOriginDeleted(String type, Long originId, String originSubPath, boolean deleted, Date originDeletedDate, Identity originDeletedBy);
	
	public void updateOriginDeleted(ToDoTaskRef toDoTask, boolean deleted, Date originDeletedDate, Identity originDeletedBy);
	
	public void deleteToDoTaskPermanently(ToDoTask toDoTask);
	
	public ToDoTask getToDoTask(ToDoTaskRef toDoTask);
	
	public List<ToDoTask> getToDoTasks(ToDoTaskSearchParams searchParams);
	
	public Long getToDoTaskCount(ToDoTaskSearchParams searchParams);
	
	public ToDoTaskStatusStats getToDoTaskStatusStats(ToDoTaskSearchParams searchParams);

	public void updateMember(Identity doer, ToDoTask toDoTask, Collection<? extends IdentityRef> assignees, Collection<? extends IdentityRef> delegatees);
	
	public Map<Long, ToDoTaskMembers> getToDoTaskGroupKeyToMembers(Collection<ToDoTask> toDoTasks, Collection<ToDoRole> roles);

	public void updateTags(ToDoTaskRef toDoTask, List<String> displayNames);
	
	public List<ToDoTaskTag> getToDoTaskTags(ToDoTaskSearchParams searchParams);
	
	public List<TagInfo> getTagInfos(ToDoTaskSearchParams searchParams, ToDoTaskRef selectionTask);
	
	public ToDoExpenditureOfWork getExpenditureOfWork(Long hours);
	
	public Long getHours(ToDoExpenditureOfWork expenditureOfWork);

}
