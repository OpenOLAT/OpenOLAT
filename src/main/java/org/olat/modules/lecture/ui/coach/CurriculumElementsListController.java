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
package org.olat.modules.lecture.ui.coach;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.CurriculumSearchManagerController;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureCurriculumElementInfos;
import org.olat.modules.lecture.model.LectureCurriculumElementSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.CurriculumElementsTableModel.LectureCurriculumCols;
import org.olat.modules.lecture.ui.event.SelectLectureCurriculumElementEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementsListController extends FormBasicController {

	private final LecturesSecurityCallback secCallback;
	
	private FlexiTableElement tableEl;
	private CurriculumElementsTableModel tableModel;
	
	@Autowired
	private LectureService lectureService;
	
	public CurriculumElementsListController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "curriculums", Util.createPackageTranslator(CurriculumSearchManagerController.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(LectureRepositoryAdminController.class, getLocale(), getTranslator()));
		this.secCallback = secCallback;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureCurriculumCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureCurriculumCols.curriculum));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureCurriculumCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureCurriculumCols.identifier, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureCurriculumCols.externalId, "select"));
		DateFlexiCellRenderer dateRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureCurriculumCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureCurriculumCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureCurriculumCols.numOfParticipants));
		if(secCallback.viewAs() == LectureRoles.lecturemanager || secCallback.viewAs() == LectureRoles.mastercoach || secCallback.viewAs() == LectureRoles.teacher) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.absences", LectureCurriculumCols.absences.ordinal(), "absences",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.absences"), "absences", null, "o_icon o_icon_lecture o_icon-fw"), null)));
		}

		tableModel = new CurriculumElementsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				LectureCurriculumElementInfos infos = tableModel.getObject(se.getIndex());
				if("select".equals(se.getCommand())) {
					fireEvent(ureq, new SelectLectureCurriculumElementEvent(infos.getElement(), false));
				} else if("absences".equals(se.getCommand())) {
					fireEvent(ureq, new SelectLectureCurriculumElementEvent(infos.getElement(), true));
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent ftse = (FlexiTableSearchEvent)event;
				loadModel(ftse.getSearch());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel(String searchString) {
		LectureCurriculumElementSearchParameters searchParams = new LectureCurriculumElementSearchParameters();
		searchParams.setSearchString(searchString);
		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		List<LectureCurriculumElementInfos> infos = lectureService.searchCurriculumElements(searchParams);
		tableModel.setObjects(infos);
		tableEl.reset(true, true, true);
	}
}
