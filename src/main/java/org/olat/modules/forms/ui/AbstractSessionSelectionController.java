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
package org.olat.modules.forms.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.ui.SessionSelectionModel.SessionSelectionCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSessionSelectionController extends FormBasicController 
{
	private static final String CMD_QUICKVIEW = "quickview";

	private SessionSelectionModel dataModel;
	private FlexiTableElement tableEl;

	private final Form form;
	private final DataStorage storage;
	private final SessionFilter filter;
	private final ReportHelper reportHelper;
	private final Component formHeader;
	private EvaluationFormExecutionController executionCtrl;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public AbstractSessionSelectionController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			SessionFilter filter, ReportHelper reportHelper, Component formHeader) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.form = form;
		this.storage = storage;
		this.filter = filter;
		this.reportHelper = reportHelper;
		this.formHeader = formHeader;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("report.session.quickview",
				"<i class='o_icon o_icon_quickview'> </i>", CMD_QUICKVIEW));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionSelectionCols.participant));
		
		if (SessionInformationsUIFactory.hasInformationType(form, InformationType.USER_FIRSTNAME)) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(SessionSelectionCols.firstname);
			columnModel.setHeaderLabel(SessionInformationsUIFactory.getTranslatedType(InformationType.USER_FIRSTNAME, getLocale()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		DefaultFlexiColumnModel lastNameColumnModel = new DefaultFlexiColumnModel(SessionSelectionCols.lastname);
		lastNameColumnModel.setHeaderLabel(SessionInformationsUIFactory.getTranslatedType(InformationType.USER_LASTNAME, getLocale()));
		columnsModel.addFlexiColumnModel(lastNameColumnModel);
		if (SessionInformationsUIFactory.hasInformationType(form, InformationType.USER_EMAIL)) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(SessionSelectionCols.email);
			columnModel.setHeaderLabel(SessionInformationsUIFactory.getTranslatedType(InformationType.USER_EMAIL, getLocale()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		if (SessionInformationsUIFactory.hasInformationType(form, InformationType.AGE)) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(SessionSelectionCols.age);
			columnModel.setHeaderLabel(SessionInformationsUIFactory.getTranslatedType(InformationType.AGE, getLocale()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		if (SessionInformationsUIFactory.hasInformationType(form, InformationType.USER_GENDER)) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(SessionSelectionCols.gender);
			columnModel.setHeaderLabel(SessionInformationsUIFactory.getTranslatedType(InformationType.USER_GENDER, getLocale()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		if (SessionInformationsUIFactory.hasInformationType(form, InformationType.USER_ORGUNIT)) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(SessionSelectionCols.orgUnit);
			columnModel.setHeaderLabel(SessionInformationsUIFactory.getTranslatedType(InformationType.USER_ORGUNIT, getLocale()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		if (SessionInformationsUIFactory.hasInformationType(form, InformationType.USER_STUDYSUBJECT)) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(SessionSelectionCols.studySubject);
			columnModel.setHeaderLabel(SessionInformationsUIFactory.getTranslatedType(InformationType.USER_STUDYSUBJECT, getLocale()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		dataModel = new SessionSelectionModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "sessions", dataModel, 25, true, getTranslator(), formLayout);
		loadModel();
	}

	private void loadModel() {
		List<SessionSelectionRow> rows = new ArrayList<>();
		int count = 1;
		List<EvaluationFormSession> sessions = evaluationFormManager.loadSessionsFiltered(filter, 0, -1);
		sessions.sort(reportHelper.getComparator());
		for (EvaluationFormSession session: sessions) {
			String participant = new StringBuilder()
					.append(translate("report.session.participant"))
					.append(" ")
					.append(count++)
					.toString();
			SessionSelectionRow row = new SessionSelectionRow(participant , session);
			rows.add(row);
		}
		dataModel.setObjects(rows);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				SessionSelectionRow row = dataModel.getObject(te.getIndex());
				if (CMD_QUICKVIEW.equals(cmd)) {
					doShowQuickview(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doShowQuickview(UserRequest ureq, SessionSelectionRow row) {
		EvaluationFormSession reloadedSession = evaluationFormManager.loadSessionByKey(row.getSession());
		EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(filter);
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(),
				reloadedSession, responses, form, storage, formHeader);
		String breadcrumbName = row.getParticipant();
		pushController(ureq, breadcrumbName, executionCtrl);
	}

	public abstract void pushController(UserRequest ureq, String breadcrumbName, EvaluationFormExecutionController theExecutionCtrl);

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(executionCtrl);
		executionCtrl = null;
        super.doDispose();
	}
}
