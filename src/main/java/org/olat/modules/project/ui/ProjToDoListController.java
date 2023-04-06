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
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjTagInfo;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.manager.ProjToDoProvider;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class ProjToDoListController extends ToDoTaskListController {

	protected final ProjProject project;
	protected final ProjProjectSecurityCallback secCallback;
	protected final Date lastVisitDate;
	

	@Autowired
	protected ProjectService projectService;

	public ProjToDoListController(UserRequest ureq, WindowControl wControl, String pageName, MapperKey avatarMapperKey,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate) {
		super(ureq, wControl, pageName, avatarMapperKey, ProjToDoProvider.TYPE, project.getKey());
		this.project = project;
		this.secCallback = secCallback;
		this.lastVisitDate = lastVisitDate;
	}

	@Override
	protected Date getLastVisitDate() {
		return lastVisitDate;
	}

	@Override
	protected List<ProjTagInfo> getFilterTags() {
		return projectService.getTagInfos(project, null);
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return secCallback;
	}
	
	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(List.of(ProjToDoProvider.TYPE));
		searchParams.setOriginIds(List.of(project.getKey()));
		return searchParams;
	}

	@Override
	protected Long getToDoTaskKey(Long activateOresKey) {
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setToDos(List.of(() -> activateOresKey));
		List<ProjToDo> toDos = projectService.getToDos(searchParams);
		return !toDos.isEmpty()? toDos.get(0).getToDoTask().getKey(): Long.valueOf(0);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}