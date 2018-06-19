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

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.ui.SessionSelectionModel.SessionSelectionCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.05.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormSessionSelectionController extends FormBasicController implements BreadcrumbPanelAware {

	private static final String CMD_QUICKVIEW = "quickview";

	private BreadcrumbPanel stackPanel;
	private SessionSelectionModel dataModel;
	private FlexiTableElement tableEl;

	private final Form form;
	private final List<? extends EvaluationFormSessionRef> sessionRefs;
	private final ReportHelper reportHelper;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public EvaluationFormSessionSelectionController(UserRequest ureq, WindowControl wControl, Form form,
			List<? extends EvaluationFormSessionRef> sessionRefs, ReportHelper reportHelper) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.form = form;
		this.sessionRefs = sessionRefs;
		this.reportHelper = reportHelper;
		initForm(ureq);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("report.session.quickview",
				"<i class='o_icon o_icon_quickview'> </i>", CMD_QUICKVIEW));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionSelectionCols.submissionDate));
		
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
		
		SessionSelectionDataSource dataSource = new SessionSelectionDataSource(sessionRefs);
		dataModel = new SessionSelectionModel(dataSource, columnsModel, reportHelper);
		tableEl = uifactory.addTableElement(getWindowControl(), "sessions", dataModel, 25, true, getTranslator(), formLayout);
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
		EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(Collections.singletonList(reloadedSession));
		String legendName = reportHelper.getLegend(reloadedSession).getName();
		EvaluationFormExecutionController controller = new EvaluationFormExecutionController(ureq, getWindowControl(),
				reloadedSession, responses, form);
		stackPanel.pushController(legendName, controller);
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
