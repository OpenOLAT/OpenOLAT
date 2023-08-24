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
import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.manager.DataCollectionToDoTaskProvider;
import org.olat.modules.quality.manager.EvaluationFormSessionToDoTaskProvider;
import org.olat.modules.quality.manager.QualityToDoTaskProvider;
import org.olat.modules.quality.ui.security.DataCollectionSecurityCallback;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionToDoListController extends ToDoTaskListController {
	
	private static final List<String> FILTER_CONTEXT_TYPS = List.of(DataCollectionToDoTaskProvider.TYPE,
			EvaluationFormSessionToDoTaskProvider.TYPE);
	
	private final DataCollectionSecurityCallback secCallback;
	private final QualityDataCollectionRef dataCollection;
	private Date lastVisitDate;

	@Autowired
	private ToDoService toDoService;

	public DataCollectionToDoListController(UserRequest ureq, WindowControl wControl, DataCollectionSecurityCallback secCallback,
			QualityDataCollectionRef dataCollection) {
		super(ureq, wControl, "todos", null, DataCollectionToDoTaskProvider.TYPE, dataCollection.getKey(), null);
		this.secCallback = secCallback;
		this.dataCollection = dataCollection;
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			Object pref = guiPrefs.get(QualityToDoListController.class, QualityToDoListController.GUIPREF_KEY_LAST_VISIT);
			if (pref instanceof String prefDate) {
				try {
					lastVisitDate = Formatter.parseDatetime(prefDate);
				} catch (ParseException e) {
					//
				}
			}
		}
		
		initForm(ureq);
		
		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		
		reload(ureq);
	}

	@Override
	protected Date getLastVisitDate() {
		return lastVisitDate;
	}
	
	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return col != ToDoTaskCols.contextTitle;
	}
	
	@Override
	protected List<String> getFilterContextTypes() {
		return FILTER_CONTEXT_TYPS;
	}

	@Override
	protected List<TagInfo> getFilterTags() {
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setTypes(QualityToDoTaskProvider.ALL_TYPES);
		return toDoService.getTagInfos(tagSearchParams, null);
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(List.of(DataCollectionToDoTaskProvider.TYPE, EvaluationFormSessionToDoTaskProvider.TYPE));
		searchParams.setOriginIds(List.of(dataCollection.getKey()));
		return searchParams;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return secCallback;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		FormLayoutContainer headerCont = FormLayoutContainer.createVerticalFormLayout("header", getTranslator());
		headerCont.setRootForm(mainForm);
		formLayout.add(headerCont);
		headerCont.setFormTitle(translate("data.collection.todos"));
		headerCont.setFormInfo(translate("data.collection.todos.info"));
	}

}
