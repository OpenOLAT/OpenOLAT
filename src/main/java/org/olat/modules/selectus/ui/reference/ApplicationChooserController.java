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
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.reference.ApplicationChooserDataModel.AppCols;

/**
 * 
 * Initial date: 7 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationChooserController extends FormBasicController {
	
	private FormLink selectButton;
	private FlexiTableElement tableEl;
	private ApplicationChooserDataModel tableModel;
	
	private final Position position;
	private final Set<Long> excludeApplicationsKeys;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public ApplicationChooserController(UserRequest ureq, WindowControl wControl, Position position,
			List<Application> excludedApplications) {
		super(ureq, wControl, "application_chooser", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		excludeApplicationsKeys = excludedApplications.stream()
				.map(Application::getKey)
				.collect(Collectors.toSet());
		
		initForm(ureq);
		loadModel();
	}
	
	public List<ApplicationLight> getSelectedApplications() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<ApplicationLight> apps = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			ApplicationLight app = tableModel.getObject(selectedIndex.intValue());
			if(app != null) {
				apps.add(app);
			}
		}
		return apps;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppCols.id));
		
		initColumnModel(AppCols.title, recruitingModule.getTableReferenceToApplicationTitleOption(), columnsModel);
		initColumnModel(AppCols.firstName, recruitingModule.getTableReferenceToApplicationFirstNameOption(), columnsModel);
		initColumnModel(AppCols.lastName, recruitingModule.getTableReferenceToApplicationLastNameOption(), columnsModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppCols.mail));

		if(position.isApplicationProject()) {
			initColumnModel(AppCols.projectTitle, recruitingModule.getTableReferenceToProjectTitleOption(), columnsModel);
		}

		tableModel = new ApplicationChooserDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "applications", tableModel, 25, true, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "applications-for-reference-chooser-v1");
	
		selectButton = uifactory.addFormLink("select", formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void initColumnModel(AppCols field, RecruitingTableOption option, FlexiTableColumnModel columnsModel) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field));
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				tableModel.quickSearch(se.getSearch());
				tableEl.reset(true, true, true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void loadModel() {
		List<ApplicationLight> applications = recruitingService.getApplications(position);
		applications = applications.stream()
				.filter(app -> !excludeApplicationsKeys.contains(app.getKey()))
				.collect(Collectors.toList());
		
		tableModel.setObjects(applications);
		tableEl.reset(true, true, true);
	}
}
