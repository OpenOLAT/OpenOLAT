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
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.course.certificate.Certificate;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.CertificationProgramEfficiencyStatementTableModel.StatmentCols;
import org.olat.modules.certificationprogram.ui.CertificationProgramRecertificationTableModel.RecertificationCols;
import org.olat.modules.certificationprogram.ui.component.CertificationStatusCellRenderer;
import org.olat.modules.certificationprogram.ui.component.DownloadCertificateCellRenderer;
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDays;
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDaysFlexiCellRenderer;
import org.olat.modules.certificationprogram.ui.component.ScoreCellRenderer;
import org.olat.modules.coach.ui.component.CompletionCellRenderer;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMemberDetailsController extends FormBasicController {

	private FlexiTableElement recertificationTableEl;
	private FlexiTableElement efficiencyStatementTableEl;
	private CertificationProgramRecertificationTableModel recertificationTableModel;
	private CertificationProgramEfficiencyStatementTableModel efficiencyStatementTableModel;
	
	private Object userObject;
	private final Identity assessedIdentity;
	private final CertificationProgram certificationProgram;
	private final NextRecertificationInDays nextRecertification;

	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramMemberDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CertificationProgram certificationProgram, NextRecertificationInDays nextRecertification,
			Identity assessedIdentity) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_details_view", rootForm);
		this.assessedIdentity = assessedIdentity;
		this.nextRecertification = nextRecertification;
		this.certificationProgram = certificationProgram;
		
		initForm(ureq);
		loadRecertificationModel(ureq);
		loadEfficiencyStatementModel();
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initPortraitForm(formLayout, ureq);
		if(certificationProgram.isRecertificationEnabled()) {
			if(nextRecertification != null && nextRecertification.isNotRenewable()
					&& formLayout instanceof FormLayoutContainer layoutCont) {
				layoutCont.contextPut("warningNotRenewable", Boolean.TRUE);
			}
			initRecertificationForm(formLayout, ureq);
		}
		initEfficiencyStatementForm(formLayout, ureq);
	}
	
	private void initPortraitForm(FormItemContainer formLayout, UserRequest ureq) {
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), assessedIdentity);
		FormBasicController profile = new CertificationProgramMemberPortraitController(ureq, getWindowControl(), mainForm, assessedIdentity, profileConfig, portraitUser);
		listenTo(profile);
		formLayout.add("portrait", profile.getInitialFormItem());
	}
	
	private void initRecertificationForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RecertificationCols.key));
		DefaultFlexiColumnModel recertificationCountCol = new DefaultFlexiColumnModel(RecertificationCols.recertificationCount);
		recertificationCountCol.setIconHeader("o_icon o_icon_recertification");
		columnsModel.addFlexiColumnModel(recertificationCountCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecertificationCols.certificate,
				new DownloadCertificateCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecertificationCols.issuedOn,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecertificationCols.status,
				new CertificationStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecertificationCols.nextRecertificationDate,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecertificationCols.nextRecertificationDays,
				new NextRecertificationInDaysFlexiCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecertificationCols.recertificationDeadline,
				new DateFlexiCellRenderer(getLocale())));
        ActionsColumnModel actionsCol = new ActionsColumnModel(RecertificationCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		recertificationTableModel = new CertificationProgramRecertificationTableModel(columnsModel);
		recertificationTableEl = uifactory.addTableElement(getWindowControl(), "recertificationTable", recertificationTableModel, 10, true, getTranslator(), formLayout);
		
		recertificationTableEl.setAndLoadPersistedPreferences(ureq, "member-details-recertification-v1");
	}
	
	private void loadRecertificationModel(UserRequest ureq) {
		Date referencedate = DateUtils.getEndOfDay(ureq.getRequestTimestamp());
		List<Certificate> certificates = certificationProgramService.getCertificates(assessedIdentity, certificationProgram);
		List<CertificationProgramRecertificationRow> rows = certificates.stream()
				.map(certificate -> forgeRecertificationRow(certificate, referencedate))
				.toList();
		recertificationTableModel.setObjects(rows);
		recertificationTableEl.reset(true, true, true);
	}
	
	private CertificationProgramRecertificationRow forgeRecertificationRow(Certificate certificate, Date referenceDate) {
		NextRecertificationInDays nextRecertification = NextRecertificationInDays.valueOf(certificate, referenceDate);
		CertificationStatus status = CertificationStatus.evaluate(certificate, referenceDate);
		CertificationProgramRecertificationRow row = new CertificationProgramRecertificationRow(certificate, nextRecertification, status);
		return row;
	}
	
	private void initEfficiencyStatementForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, StatmentCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatmentCols.repositoryEntryDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatmentCols.repositoryEntryExternalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatmentCols.completion,
				new CompletionCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatmentCols.score,
				new ScoreCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatmentCols.passed,
				new PassedCellRenderer(getLocale())));
		
        ActionsColumnModel actionsCol = new ActionsColumnModel(StatmentCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		efficiencyStatementTableModel = new CertificationProgramEfficiencyStatementTableModel(columnsModel);
		efficiencyStatementTableEl = uifactory.addTableElement(getWindowControl(), "statementTable", efficiencyStatementTableModel, 10, true, getTranslator(), formLayout);
		
		efficiencyStatementTableEl.setAndLoadPersistedPreferences(ureq, "member-details-statements-v1");
	}
	
	private void loadEfficiencyStatementModel() {
		List<RepositoryEntryStatusEnum> status = List.of(RepositoryEntryStatusEnum.preparationToClosed());
		List<AssessmentEntry> statements = certificationProgramService.getAssessmentEntries(certificationProgram, assessedIdentity, status);	
		List<CertificationProgramEfficiencyStatementRow> rows = new ArrayList<>();
		for(AssessmentEntry statement:statements) {
			rows.add(forgeStatementRow(statement));
		}
		efficiencyStatementTableModel.setObjects(rows);
		efficiencyStatementTableEl.reset(true, true, true);
	}
	
	private CertificationProgramEfficiencyStatementRow forgeStatementRow(AssessmentEntry assessmentEntry) {
		CertificationProgramEfficiencyStatementRow row = new CertificationProgramEfficiencyStatementRow(assessmentEntry);
		return row;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
