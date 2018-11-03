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

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccess.EmailTrigger;
import org.olat.modules.quality.QualityReportAccess.Type;
import org.olat.modules.quality.QualityReportAccessReference;
import org.olat.modules.quality.QualityReportAccessSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.ReportAccessDataModel.ReportAccessCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class ReportAccessController extends FormBasicController {
	
	private static final String[] ONLINE_KEYS = new String[] { "enabled" };
	private static final String[] ONLINE_VALUES = new String[] { "" };
	
	private ReportAccessDataModel dataModel;
	private FlexiTableElement tableEl;
	private final String[] emailTriggerKeys;
	private final String[] emailTriggerValues;
	
	private QualityReportAccessReference reference;
	private QualityReportAccessSearchParams searchParams;
	private List<QualityReportAccess> reportAccesses;
	
	@Autowired
	private QualityService qualityService;

	protected ReportAccessController(UserRequest ureq, WindowControl windowControl, QualityReportAccessReference reference) {
		super(ureq, windowControl, "report_access");
		this.reference = reference;
		this.searchParams = new QualityReportAccessSearchParams();
		this.searchParams.setReference(reference);
		KeyValues emailTriggerKV = getEmailTriggerKV();
		this.emailTriggerKeys = emailTriggerKV.keys();
		this.emailTriggerValues = emailTriggerKV.values();
	}
	
	protected abstract boolean canEditReportAccessOnline();
	
	protected abstract boolean canEditReportAccessEmail();

	private KeyValues getEmailTriggerKV() {
		EmailTrigger[] emailTriggers = QualityReportAccess.EmailTrigger.values();
		KeyValues kv = new KeyValues();
		for (int i = 0; i < emailTriggers.length; i++) {
			EmailTrigger emailTrigger = emailTriggers[i];
			String key = emailTrigger.name();
			String value = translate("report.access.email.trigger." + emailTrigger.name());
			kv.add(entry(key, value));
		}
		return kv;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTable(ureq);
	}

	protected void initTable(UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.online));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.emailTrigger));
		
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
		SingleSelection emailTriggerEl = createEmailTriggerEl(row, access);
		row.setEmailTriggerEl(emailTriggerEl);
		return row;
	}

	private MultipleSelectionElement createOnlineCheckbox(ReportAccessRow row, QualityReportAccess access) {
		String name =  "online-" + CodeHelper.getRAMUniqueID();
		MultipleSelectionElement onlineEl = uifactory.addCheckboxesHorizontal(name, null, flc, ONLINE_KEYS, ONLINE_VALUES);
		onlineEl.setUserObject(row);
		onlineEl.addActionListener(FormEvent.ONCHANGE);
		onlineEl.setAjaxOnly(true);
		onlineEl.select(ONLINE_KEYS[0], access != null && access.isOnline());
		onlineEl.setEnabled(canEditReportAccessOnline());
		return onlineEl;
	}

	private SingleSelection createEmailTriggerEl(ReportAccessRow row, QualityReportAccess access) {
		String name =  "email-" + CodeHelper.getRAMUniqueID();
		SingleSelection emailTriggerEl = uifactory.addDropdownSingleselect(name, flc, emailTriggerKeys, emailTriggerValues);
		emailTriggerEl.setUserObject(row);
		emailTriggerEl.addActionListener(FormEvent.ONCHANGE);
		String accessKey = access != null? access.getEmailTrigger().name(): null;
		if (Arrays.asList(emailTriggerKeys).contains(accessKey)) {
			emailTriggerEl.select(accessKey, true);
		}
		emailTriggerEl.setEnabled(canEditReportAccessEmail());
		return emailTriggerEl;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof MultipleSelectionElement) {
			MultipleSelectionElement onlineEl = (MultipleSelectionElement) source;
			doEnableOnline(onlineEl);
		} else if (source instanceof SingleSelection) {
			SingleSelection emailTriggerEl = (SingleSelection)source;
			doSetEmailTrigger(emailTriggerEl);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doEnableOnline(MultipleSelectionElement onlineEl) {
		boolean enable = onlineEl.isAtLeastSelected(1);
		ReportAccessRow row = (ReportAccessRow) onlineEl.getUserObject();
		QualityReportAccess reportAccess = getOrCreateReportAccess(row);
		reportAccess.setOnline(enable);
		updateReportAccess(reportAccess);
	}

	private void doSetEmailTrigger(SingleSelection emailTriggerEl) {
		String selectedKey = emailTriggerEl.isOneSelected()? emailTriggerEl.getSelectedKey(): null;
		EmailTrigger emailTrigger = QualityReportAccess.EmailTrigger.valueOf(selectedKey);
		ReportAccessRow row = (ReportAccessRow) emailTriggerEl.getUserObject();
		QualityReportAccess reportAccess = getOrCreateReportAccess(row);
		reportAccess.setEmailTrigger(emailTrigger);
		updateReportAccess(reportAccess);
	}

	private QualityReportAccess getOrCreateReportAccess(ReportAccessRow row) {
		QualityReportAccess reportAccess = getCachedReportAccess(row.getType(), row.getRole());
		if (reportAccess == null) {
			reportAccess = qualityService.createReportAccess(reference, row.getType(), row.getRole());
		}
		return reportAccess;
	}

	private QualityReportAccess getCachedReportAccess(Type type, String role) {
		for (QualityReportAccess access : reportAccesses) {
			if (type.equals(access.getType()) && equalsEmpty(role, access.getRole())) {
				return access;
			}
		}
		return null;
	}

	private void updateReportAccess(QualityReportAccess reportAccess) {
		reportAccess = qualityService.updateReportAccess(reportAccess);
		updateCache(reportAccess);
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
