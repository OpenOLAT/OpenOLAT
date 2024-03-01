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
package org.olat.modules.quality.ui;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.manager.DataCollectionToDoTaskProvider;
import org.olat.modules.quality.manager.EvaluationFormSessionToDoTaskProvider;
import org.olat.modules.quality.manager.GeneralToDoTaskProvider;
import org.olat.modules.quality.manager.QualityToDoTaskProvider;
import org.olat.modules.quality.manager.QualityToDoTaskQuery;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityToDoListController extends ToDoTaskListController {
	
	public static final String GUIPREF_KEY_LAST_VISIT = "qm.todos.last.visit";
	private static final List<String> TYPES = List.of(
			GeneralToDoTaskProvider.TYPE,
			DataCollectionToDoTaskProvider.TYPE,
			EvaluationFormSessionToDoTaskProvider.TYPE);

	private FormLink createLink;
	
	private final MainSecurityCallback secCallback;
	private final ToDoTaskSecurityCallback toDoSecCallback;
	private final Set<Long> editDataCollectionKeys;
	private final Set<Long> viewDataCollectionKeys;
	private final Set<Long> allDataCollectionKeys;
	private Date lastVisitDate;

	@Autowired
	private QualityService qualityService;
	@Autowired
	private ToDoService toDoService;

	public QualityToDoListController(UserRequest ureq, WindowControl wControl, MainSecurityCallback secCallback) {
		super(ureq, wControl, "todos", null, GeneralToDoTaskProvider.TYPE, null, null);
		this.secCallback = secCallback;
		this.toDoSecCallback = new QualityToDoTaskSecurityCallback();
		
		editDataCollectionKeys = new HashSet<>(qualityService.loadDataCollectionKeysByOrganisations(secCallback.getEditDataCollectionOrganisationRefs()));
		viewDataCollectionKeys = new HashSet<>(qualityService.loadDataCollectionKeysByOrganisations(secCallback.getViewOnlyDataCollectionOrganisationRefs()));
		allDataCollectionKeys = new HashSet<>(editDataCollectionKeys);
		allDataCollectionKeys.addAll(viewDataCollectionKeys);
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			Object pref = guiPrefs.get(QualityToDoListController.class, GUIPREF_KEY_LAST_VISIT);
			if (pref instanceof String prefDate) {
				try {
					lastVisitDate = Formatter.parseDatetime(prefDate);
				} catch (ParseException e) {
					//
				}
			}
			
			String lastVisit = Formatter.formatDatetime(new Date());
			guiPrefs.putAndSave(QualityToDoListController.class, GUIPREF_KEY_LAST_VISIT, lastVisit);
		}
		
		initForm(ureq);
		
		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		setAndLoadPersistedPreferences(ureq, "quality-todos-all");
		
		reload(ureq);
	}

	@Override
	protected Date getLastVisitDate() {
		return lastVisitDate;
	}

	@Override
	protected List<TagInfo> getFilterTags() {
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setTypes(QualityToDoTaskProvider.ALL_TYPES);
		return toDoService.getTagInfos(tagSearchParams, null);
	}

	@Override
	protected List<String> getTypes() {
		return TYPES;
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setCustomQuery(new QualityToDoTaskQuery(getIdentity(), secCallback.canCreateToDoTasks(), allDataCollectionKeys));
		return searchParams;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return toDoSecCallback;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		FormLayoutContainer headerCont = FormLayoutContainer.createVerticalFormLayout("header", getTranslator());
		headerCont.setRootForm(mainForm);
		formLayout.add(headerCont);
		headerCont.setFormTitle(translate("data.collection.todos"));
		headerCont.setFormInfo(translate("data.collection.todos.info"));
		
		createLink = uifactory.addFormLink("todo.create", formLayout, Link.BUTTON);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setVisible(secCallback.canCreateToDoTasks());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink) {
			doCreateToDoTask(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private final class QualityToDoTaskSecurityCallback implements ToDoTaskSecurityCallback {

		@Override
		public boolean canCreateToDoTasks() {
			return secCallback.canCreateToDoTasks();
		}

		@Override
		public boolean canCopy(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return secCallback.canCreateDataCollections();
		}

		@Override
		public boolean canEdit(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return assignee
					|| delegatee
					|| toDoTask.getOriginId() == null && secCallback.canCreateToDoTasks()
					|| editDataCollectionKeys.contains(toDoTask.getOriginId());
		}
		
		@Override
		public boolean canBulkDeleteToDoTasks() {
			return true;
		}

		@Override
		public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return canEdit(toDoTask, creator, assignee, delegatee);
		}
		
		@Override
		public boolean canRestore(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return false;
		}
		
	}

}
