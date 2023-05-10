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

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ui.component.ProjAvatarComponent;
import org.olat.modules.project.ui.component.ProjAvatarComponent.Size;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjToDoAllController extends ProjToDoListController {
	
	private FormLink createLink;

	private final String avatarUrl;

	public ProjToDoAllController(UserRequest ureq, WindowControl wControl, ProjProject project,
			ProjProjectSecurityCallback secCallback, Date lastVisitDate, MapperKey avatarMapperKey) {
		super(ureq, wControl, "todo_all", avatarMapperKey, project, secCallback, lastVisitDate);
		ProjProjectImageMapper projectImageMapper = new ProjProjectImageMapper(projectService);
		String projectMapperUrl = registerCacheableMapper(ureq, ProjProjectImageMapper.DEFAULT_ID, projectImageMapper,
				ProjProjectImageMapper.DEFAULT_EXPIRATION_TIME);
		this.avatarUrl = projectImageMapper.getImageUrl(projectMapperUrl, project, ProjProjectImageType.avatar);
		
		initForm(ureq);
		
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		
		reload(ureq);
	}
	
	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return col != ToDoTaskCols.contextType
				&& col != ToDoTaskCols.contextTitle;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		formLayout.add("avatar", new ComponentWrapperElement(new ProjAvatarComponent("avatar", project, avatarUrl, Size.medium)));
		
		createLink = uifactory.addFormLink("todo.create", formLayout, Link.BUTTON);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setVisible(secCallback.canCreateToDos());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink) {
			doCreateToDoTask(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

}
