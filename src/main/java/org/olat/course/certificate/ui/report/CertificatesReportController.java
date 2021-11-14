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
package org.olat.course.certificate.ui.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.certificate.ui.report.CertificatesReportParameters.With;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.repository.ui.RepositoryEntryACColumnDescriptor;
import org.olat.repository.ui.RepositoryFlexiTableModel;
import org.olat.repository.ui.RepositoryFlexiTableModel.RepoCols;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesReportController extends FormBasicController {
	
	private static final String[] searchWithKeys = new String[] {
			With.withoutCertificate.name(), With.validCertificate.name(), With.expiredCertificate.name()
		};
	private static final String[] passedKeys = new String[] { "passed" };
	
	private TextElement searchStringEl;
	private FormLink generateReportButton;
	private DateChooser certificatesDateEl;
	private MultipleSelectionElement withEl;
	private MultipleSelectionElement passedEl;
	private FlexiTableElement tableEl;
	private RepositoryFlexiTableModel tableModel;
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CertificatesReportController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "reports", Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale(),
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale())));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchCont = FormLayoutContainer.createDefaultFormLayout("search.cont", getTranslator());
		formLayout.add("search.cont", searchCont);

		searchStringEl = uifactory.addTextElement("search.text", 2000, null, searchCont);
		certificatesDateEl = uifactory.addDateChooser("search.dates", "search.dates", null, searchCont);
		certificatesDateEl.setSecondDate(true);
		certificatesDateEl.setSeparator("search.dates.separator");
		
		String[] searchWithValues = new String[] {
			translate("search.without"), translate("search.with.valid"), translate("search.with.expired")
		};
		withEl = uifactory.addCheckboxesVertical("search.with", "search.with", searchCont, searchWithKeys, searchWithValues, 1);
		withEl.select(searchWithKeys[1], true);
		
		String[] passedValues = new String[] { translate("search.course.passed") };
		passedEl = uifactory.addCheckboxesVertical("search.passed", "search.passed", searchCont, passedKeys, passedValues, 1);
		passedEl.select(passedKeys[0], true);
		uifactory.addFormSubmitButton("search", searchCont);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.ac, new RepositoryEntryACColumnDescriptor()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.repoEntry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.externalId));// visible if managed
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.displayname, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.author));
		StaticFlexiCellRenderer reportRenderer = new StaticFlexiCellRenderer(translate("report"), "report");
		reportRenderer.setPush(true);
		reportRenderer.setDirtyCheck(false);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "report", null, -1, "report", false, null, FlexiColumnModel.ALIGNMENT_LEFT, reportRenderer));

		tableModel = new RepositoryFlexiTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "certificates-reports-courses-list");
		tableEl.setEmptyTableSettings("search.empty", null, "o_icon_certificate");
		
		generateReportButton = uifactory.addFormLink("report.certificates", "report.certificates", null, formLayout, Link.BUTTON);
		
		tableEl.addBatchButton(generateReportButton);
	}
	
	protected void loadModel(UserRequest ureq, String searchString) {
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.addResourceTypes("CourseModule");
		params.setIdentity(getIdentity());
		params.setRoles(ureq.getUserSession().getRoles());
		params.setIdRefsAndTitle(searchString);
		
		List<RepositoryEntry> entries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		tableModel.setObjects(entries);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSearch(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(generateReportButton == source) {
			doReport(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doSelect(ureq, tableModel.getObject(se.getIndex()));
				} else if("report".equals(se.getCommand())) {
					doReport(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSearch(UserRequest ureq) {
		String searchString = searchStringEl.getValue();
		loadModel(ureq, searchString);
	}
	
	private void doSelect(UserRequest ureq, RepositoryEntry re) {
		String businessPath = "[RepositoryEntry:" + re.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doReport(UserRequest ureq, RepositoryEntry re) {
		String filename = re.getDisplayname() + "_Certificates_" + Formatter.formatDatetimeWithMinutes(ureq.getRequestTimestamp());
		filename = StringHelper.transformDisplayNameToFileSystemName(filename) + ".xlsx";
		
		List<RepositoryEntry> entries = Collections.singletonList(re);
		CertificatesReportParameters reportParams = getReportParameters();
		MediaResource report = new CertificatesReportMediaResource(filename, entries, reportParams, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(report);
	}
	
	private void doReport(UserRequest ureq) {
		List<RepositoryEntry> entries = getSelectedEntries();
		if(entries.isEmpty()) {
			showWarning("warning.at.least.one.test");
		} else {
			String filename = "Certificates_" + Formatter.formatDatetimeWithMinutes(ureq.getRequestTimestamp());
			filename = StringHelper.transformDisplayNameToFileSystemName(filename) + ".xlsx";
			
			CertificatesReportParameters reportParams = getReportParameters();
			MediaResource report = new CertificatesReportMediaResource(filename, entries, reportParams, getTranslator());
			ureq.getDispatchResult().setResultingMediaResource(report);
		}
	}

	private CertificatesReportParameters getReportParameters() {
		List<With> with = With.values(withEl.getSelectedKeys());
		boolean onlyPassed = passedEl.isAtLeastSelected(1);
		Date certificatesStart = certificatesDateEl.getDate();
		if(certificatesStart != null) {
			certificatesStart = CalendarUtils.startOfDay(certificatesStart);
		}
		Date certificatesEnd = certificatesDateEl.getSecondDate();
		if(certificatesEnd != null) {
			certificatesEnd = CalendarUtils.endOfDay(certificatesEnd);
		}
		return new CertificatesReportParameters(certificatesStart, certificatesEnd, with, onlyPassed);
	}
	
	private List<RepositoryEntry> getSelectedEntries() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<RepositoryEntry> selectedEntries = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			RepositoryEntry entry = tableModel.getObject(selectedIndex.intValue());
			selectedEntries.add(entry);
		}
		return selectedEntries;
	}
}
