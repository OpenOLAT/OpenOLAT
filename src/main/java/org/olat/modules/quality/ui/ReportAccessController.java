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

import static org.olat.modules.quality.QualityReportAccessReference.of;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccess.Type;
import org.olat.modules.quality.QualityReportAccessSearchParams;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.ReportAccessDataModel.ReportAccessCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReportAccessController extends AbstractDataCollectionEditController {
	
	private static final String[] ONLINE_KEYS = new String[] { "enabled" };
	private static final String[] ONLINE_VALUES = new String[] { "on" };
	
	private ReportAccessDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private QualityReportAccessSearchParams searchParams;
	private List<QualityReportAccess> reportAccesses;
	
	@Autowired
	private QualityService qualityService;

	public ReportAccessController(UserRequest ureq, WindowControl windowControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityDataCollection dataCollection) {
		super(ureq, windowControl, secCallback, stackPanel, dataCollection, "report_access");
		this.searchParams = new QualityReportAccessSearchParams();
		this.searchParams.setReference(of(dataCollection));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateUI(ureq);
	}
	
	@Override
	protected void updateUI(UserRequest ureq) {
		initTable(ureq);
	}

	private void initTable(UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.online));
//		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.emailTrigger));
		
		dataModel = new ReportAccessDataModel(columnsModel, getTranslator());
		
		if (tableEl != null) flc.remove(tableEl);
		tableEl = uifactory.addTableElement(getWindowControl(), "reportaccess", dataModel, 25, true, getTranslator(), flc);
		tableEl.setAndLoadPersistedPreferences(ureq, "quality-report-access");
		tableEl.setEmtpyTableMessageKey("report.access.empty.table");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		loadDataModel();
	}

	private void loadDataModel() {
		reportAccesses = qualityService.loadReportAccesses(searchParams);
		List<ReportAccessRow> rows = createRows();
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private List<ReportAccessRow> createRows() {
		List<ReportAccessRow> rows = new ArrayList<>();

		rows.add(createRow("report.access.name.repo.owner", Type.GroupRoles, GroupRoles.owner.name()));
		rows.add(createRow("report.access.name.repo.coach", Type.GroupRoles, GroupRoles.coach.name()));
		
		return rows;
	}
	
	private ReportAccessRow createRow(String nameI18n, Type type, String role) {
		ReportAccessRow row = new ReportAccessRow(translate(nameI18n), type, role);
		QualityReportAccess access = getCachedReportAccess(type, role);
		MultipleSelectionElement onlineEl = createOnlineCheckbox(row, access);
		row.setOnlineEl(onlineEl);
		return row;
	}

	private MultipleSelectionElement createOnlineCheckbox(ReportAccessRow row, QualityReportAccess access) {
		String inlineElName =  "online-" + CodeHelper.getRAMUniqueID();
		MultipleSelectionElement onlineEl = uifactory.addCheckboxesHorizontal(inlineElName, null, flc, ONLINE_KEYS, ONLINE_VALUES);
		onlineEl.setUserObject(row);
		onlineEl.addActionListener(FormEvent.ONCHANGE);
		onlineEl.setAjaxOnly(true);
		onlineEl.select(ONLINE_KEYS[0], access != null && access.isOnline());
		onlineEl.setEnabled(secCallback.canEditReportAccesses());
		return onlineEl;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof MultipleSelectionElement) {
			MultipleSelectionElement onlineEl = (MultipleSelectionElement) source;
			doEnableOnline(onlineEl);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doEnableOnline(MultipleSelectionElement onlineEl) {
		boolean enable = onlineEl.isAtLeastSelected(1);
		ReportAccessRow row = (ReportAccessRow)onlineEl.getUserObject();
		QualityReportAccess reportAccess = getCachedReportAccess(row.getType(), row.getRole());
		if (reportAccess == null) {
			reportAccess = qualityService.createReportAccess(of(dataCollection), row.getType(), row.getRole());
		}
		reportAccess.setOnline(enable);
		reportAccess = qualityService.updateReportAccess(reportAccess);
		updateCache(reportAccess);
	}

	private QualityReportAccess getCachedReportAccess(Type type, String role) {
		for (QualityReportAccess access : reportAccesses) {
			if (type.equals(access.getType()) && equalsEmpty(role, access.getRole())) {
				return access;
			}
		}
		return null;
	}
	
	private void updateCache(QualityReportAccess reportAccess) {
		reportAccesses.removeIf(a -> reportAccess.getType().equals(a.getType()) && equalsEmpty(reportAccess.getRole(), a.getRole()));
		reportAccesses.add(reportAccess);
	}
	
	private boolean equalsEmpty(String s1, String s2) {
		if (!StringHelper.containsNonWhitespace(s1) && !StringHelper.containsNonWhitespace(s2)) {
			return true;
		}
		
		if (StringHelper.containsNonWhitespace(s1) && StringHelper.containsNonWhitespace(s2)) {
			return s1.equals(s2);
		}
		
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
