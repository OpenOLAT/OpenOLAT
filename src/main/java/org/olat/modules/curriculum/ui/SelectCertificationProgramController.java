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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.CertificationProgramCurriculumElementListController;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationCellRenderer;
import org.olat.modules.certificationprogram.ui.component.RecertificationModeCellRenderer;
import org.olat.modules.curriculum.CurriculumElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SelectCertificationProgramController extends FormBasicController {
	
	private static final String CMD_SELECT = "cpselect";
	
	private FlexiTableElement tableEl;
	private CertificationProgramTableModel tableModel;
	
	private Organisation organisation;
	private final CurriculumElement curriculumElement;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public SelectCertificationProgramController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, Organisation organisation) {
		super(ureq, wControl, "select_certification_program", Util
				.createPackageTranslator(CertificationProgramCurriculumElementListController.class, ureq.getLocale()));
		this.organisation = organisation;
		this.curriculumElement = curriculumElement;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProgramCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.validityPeriod,
				new DurationCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.recertificationMode,
				new RecertificationModeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.requiredCreditPoint));
		
		tableModel = new CertificationProgramTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "implementation-select-certification-programs-v1");
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void loadModel() {
		List<CertificationProgram> programsList = certificationProgramService.getCertificationPrograms(List.of(organisation));
		tableModel.setObjects(programsList);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se && CMD_SELECT.equals(se.getCommand())) {
				CertificationProgram cp = tableModel.getObject(se.getIndex());
				doSelectCertificationProgram(ureq, cp);
			} else if(event instanceof FlexiTableSearchEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSelectCertificationProgram(UserRequest ureq, CertificationProgram program) {
		certificationProgramService.removeCurriculumElementToCertificationProgram(curriculumElement, getIdentity());
		certificationProgramService.addCurriculumElementToCertificationProgram(program, curriculumElement, getIdentity());
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private class CertificationProgramTableModel extends DefaultFlexiTableDataModel<CertificationProgram>
	implements SortableFlexiTableDataModel<CertificationProgram>, FilterableFlexiTableModel {
		
		private static final ProgramCols[] COLS = ProgramCols.values();
		
		private List<CertificationProgram> backupList;
		
		public CertificationProgramTableModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}
		
		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<CertificationProgram> views = new SortableFlexiTableModelDelegate<>(orderBy, this, getLocale()).sort();
				super.setObjects(views);
			}
		}
		
		@Override
		public void filter(String searchString, List<FlexiTableFilter> filters) {
			if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
				final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
						? null : searchString.toLowerCase();

				List<CertificationProgram> filteredRows = new ArrayList<>(backupList.size());
				for(CertificationProgram row:backupList) {
					boolean accept = accept(loweredSearchString, row);
					if(accept) {
						filteredRows.add(row);
					}
				}
				super.setObjects(filteredRows);
			} else {
				super.setObjects(backupList);
			}
		}
		
		private boolean accept(String searchValue, CertificationProgram elementRow) {
			if(searchValue == null) return true;
			return accept(searchValue, elementRow.getDisplayName())
					|| accept(searchValue, elementRow.getIdentifier());
		}
		
		private boolean accept(String searchValue, String val) {
			return val != null && val.toLowerCase().contains(searchValue);
		}

		@Override
		public Object getValueAt(int row, int col) {
			CertificationProgram program = getObject(row);
			return getValueAt(program, col);
		}

		@Override
		public Object getValueAt(CertificationProgram row, int col) {
			return switch(COLS[col]) {
				case key -> row.getKey();
				case identifier -> row.getIdentifier();
				case displayName -> row.getDisplayName();
				case recertificationMode -> row.getRecertificationMode();
				case validityPeriod -> getValidityPeriod(row);
				case requiredCreditPoint -> row.getCreditPoints();
				default -> "ERROR";
			};
		}
		
		private Duration getValidityPeriod(CertificationProgram certificationProgram) {
			return certificationProgram.isValidityEnabled()
					? new Duration(certificationProgram.getValidityTimelapse(), certificationProgram.getValidityTimelapseUnit())
					: null;
		}
		
		@Override
		public void setObjects(List<CertificationProgram> objects) {
			this.backupList = objects;
			super.setObjects(objects);
		}
	}
	
	public enum ProgramCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayname"),
		identifier("table.header.identifier"),
		validityPeriod("table.header.validity.period"),
		recertificationMode("table.header.recertification.mode"),
		requiredCreditPoint("table.header.required.credit.point");
		
		private final String i18nHeaderKey;
		
		private ProgramCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
