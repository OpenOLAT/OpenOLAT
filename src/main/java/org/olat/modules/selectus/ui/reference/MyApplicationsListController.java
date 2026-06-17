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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.reference.MyApplicationsTableModel.MyAppsCols;

/**
 * 
 * Initial date: 20 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyApplicationsListController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private MyApplicationsTableModel tableModel;
	private final TooledStackedPanel stackPanel;
	
	@Autowired
	private RecruitingService recruitingService;
	
	public MyApplicationsListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "my_applicants_list", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.stackPanel = stackPanel;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MyAppsCols.positionTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MyAppsCols.refereeMgmtDeadline, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.my.applications.select",
				translate("my.applications.select"), "select", "o_icon o_icon-fw o_icon_content_popup"));
		
		tableModel = new MyApplicationsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "applicationsList", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("my.applications.list.empty")
				.build());
	}
	
	public int loadModel() {
		List<Application> applications = recruitingService.getCurrentApplicationsByApplicant(getIdentity());
		tableModel.setObjects(applications);
		tableEl.reset(true, true, true);
		return applications.size();
	}
	
	public int getNumOfApplications() {
		return tableModel.getRowCount();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("MyApplication".equals(type) || "MyApplications".equals(type)) {
			Long appKey = entries.get(0).getOLATResourceable().getResourceableId();
			Application app = tableModel.getApplicationByKey(appKey);
			if(app != null) {
				doOpenApplication(ureq, app);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doOpenApplication(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public void openFirstApplication(UserRequest ureq) {
		if(tableModel.getRowCount() > 0) {
			Application app = tableModel.getObject(0);
			doOpenApplication(ureq, app);
		}
	}
	
	private void doOpenApplication(UserRequest ureq, Application app) {
		Position position = recruitingService.getPosition(app.getPosition().getKey());
		ApplicantRefereeListController refereeListCtrl = new ApplicantRefereeListController(ureq, getWindowControl(), position, app);
		listenTo(refereeListCtrl);
		stackPanel.pushController(position.getMLTitle(getLocale()), refereeListCtrl);
	}
}
