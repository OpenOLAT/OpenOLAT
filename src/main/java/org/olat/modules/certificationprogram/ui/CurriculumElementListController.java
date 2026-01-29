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
package org.olat.modules.certificationprogram.ui;

import java.util.ArrayList;
import java.util.List;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.CurriculumElementTableModel.CurriculumElementCols;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementListController extends FormBasicController {
	
	private static final String CMD_SELECT = "rselect";
	
	private FormLink selectButton;
	
	private FlexiTableElement tableEl;
	private CurriculumElementTableModel tableModel;
	
	private CertificationProgram certificationProgram;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CurriculumElementListController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl, "select_element");
		this.certificationProgram = certificationProgram;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumElementCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumElementCols.curriculum));
		
		tableModel = new CurriculumElementTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "certification-programs-select-element-v1");
		
		selectButton = uifactory.addFormLink("select", formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void loadModel() {
		CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams(getIdentity());
		searchParams.setImplementationsOnly(true);
		
		List<CurriculumElementInfos> elementInfosList = curriculumService.getCurriculumElementsWithInfos(searchParams);
		List<CurriculumElementRow> rows = new ArrayList<>(elementInfosList.size());
		for(CurriculumElementInfos elementInfos:elementInfosList) {
			rows.add(new CurriculumElementRow(elementInfos.curriculumElement(), elementInfos.curriculum(),
					elementInfos.numOfParticipants(), -1l, elementInfos.numOfResources()));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectButton == source) {
			fireEvent(ureq, FormEvent.DONE_EVENT);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se && CMD_SELECT.equals(se.getCommand())) {
				CurriculumElementRow elementRow = tableModel.getObject(se.getIndex());
				doSelectCurriculumElement(ureq, elementRow);
			} else if(event instanceof FlexiTableSearchEvent) {
				filterModel();
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
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}
	
	private void doSelectCurriculumElement(UserRequest ureq, CurriculumElementRow elementRow) {
		CurriculumElement element = curriculumService.getCurriculumElement(elementRow);
		certificationProgramService.addCurriculumElementToCertificationProgram(certificationProgram, element, getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
