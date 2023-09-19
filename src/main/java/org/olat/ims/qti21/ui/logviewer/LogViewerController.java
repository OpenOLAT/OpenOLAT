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
package org.olat.ims.qti21.ui.logviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.LogViewerDeserializer;
import org.olat.ims.qti21.model.LogViewerEntry;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.ims.qti21.ui.logviewer.LogViewerTableDataModel.LogEntryCols;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 24 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogViewerController extends FormBasicController implements FlexiTableCssDelegate {
	
	private static final String FILTER_TYPES = "types";
	private static final String FILTER_TITLE = "title";
	private static final String FILTER_DATE = "date";
	
	private FlexiTableElement tableEl;
	private LogViewerTableDataModel tableModel;
	
	private final File logFile;
	private final AssessmentTestSession testSession;
	
	@Autowired
	private QTI21Service qtiService;
	
	public LogViewerController(UserRequest ureq, WindowControl wControl, AssessmentTestSession testSession, File logFile) {
		super(ureq, wControl, "logviewer",
				Util.createPackageTranslator(AssessmentTestComposerController.class, ureq.getLocale()));
		this.logFile = logFile;
		this.testSession = testSession;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.date,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.event));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.itemTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.itemId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.interactionsTypes));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.response,
				new LogAnswerCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.responseIds,
				new LogAnswerCellRenderer(true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.minMaxScore));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogEntryCols.score));
		
		tableModel = new LogViewerTableDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 2048, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCssDelegate(this);
		initFilters();
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		CandidateTestEventType eventType = tableModel.getObject(pos).getTestEventType();
		if(eventType == CandidateTestEventType.ENTER_TEST) {
			return "success";
		}
		if(eventType == CandidateTestEventType.SUSPEND) {
			return "warning";
		}
		if(eventType == CandidateTestEventType.EXIT_TEST || eventType == CandidateTestEventType.EXIT_DUE_TIME_LIMIT) {
			return "danger";
		}
		return null;
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(4);
		
		SelectionValues eventTypesValues = new SelectionValues();
		eventTypesValues.add(SelectionValues.entry(LogViewerTableDataModel.OUTCOMES, LogViewerTableDataModel.OUTCOMES));
		for(CandidateTestEventType type:CandidateTestEventType.values()) {
			eventTypesValues.add(SelectionValues.entry(type.name(), type.name()));
		}
		for(CandidateItemEventType type:CandidateItemEventType.values()) {
			eventTypesValues.add(SelectionValues.entry(type.name(), type.name()));
		}
		FlexiTableMultiSelectionFilter eventTypesFilter = new FlexiTableMultiSelectionFilter(translate("filter.event.type"),
				FILTER_TYPES, eventTypesValues, true);
		filters.add(eventTypesFilter);
		
		filters.add(new FlexiTableTextFilter(translate("filter.event.title"), FILTER_TITLE, true));
		
		FlexiTableDateRangeFilter dateFilter = new FlexiTableDateRangeFilter(translate("filter.event.date"), FILTER_DATE,
				true, true, translate("filter.event.date.from"),
				translate("filter.event.date.to"), getLocale());
		filters.add(dateFilter);
		
		tableEl.setFilters(true, filters, false, true);
	}
	
	private void loadModel() {
		final AssessmentTestSession session = qtiService.getAssessmentTestSession(testSession.getKey());
		FileResourceManager frm = FileResourceManager.getInstance();
		final File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		LogViewerDeserializer deserializer = new LogViewerDeserializer(logFile, resolvedAssessmentTest, getTranslator());
		List<LogViewerEntry> entries = deserializer.readEntries();
		tableModel.setObjects(entries);
		tableEl.reset(true, true, true);
	}
	
	private void doSearch(List<FlexiTableFilter> filters) {
		List<String> types = null;
		FlexiTableFilter eventTypesFilter = FlexiTableFilter.getFilter(filters, FILTER_TYPES);
		if (eventTypesFilter != null) {
			types = ((FlexiTableMultiSelectionFilter)eventTypesFilter).getValues();
		}

		String title = null;
		FlexiTableFilter titleFilter = FlexiTableFilter.getFilter(filters, FILTER_TITLE);
		if (titleFilter != null) {
			title = titleFilter.getValue();
		}
	
		Date from = null;
		Date to = null;
		FlexiTableFilter dFilter = FlexiTableFilter.getFilter(filters, FILTER_DATE);
		if(dFilter instanceof FlexiTableDateRangeFilter dateFilter && dateFilter.getDateRange() != null) {
			from = dateFilter.getDateRange().getStart();
			to = dateFilter.getDateRange().getEnd();
		}
		
		tableModel.filter(types, title, from, to);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent ftse) {
				doSearch(ftse.getFilters());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
