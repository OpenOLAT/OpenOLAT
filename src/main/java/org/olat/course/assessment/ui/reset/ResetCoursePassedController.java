/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.assessment.ui.reset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.SelectboxSelectionImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.assessment.ui.reset.ResetDataIdentitiesTableModel.IdentityCols;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jun 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ResetCoursePassedController extends StepFormBasicController {
	
	private static final String CERT_RESET_ARCHIVE = "archive";
	private static final String CERT_RESET_KEEP = "keep";
	private static final String[] CERT_RESET_KEYS = new String[] { CERT_RESET_ARCHIVE, CERT_RESET_KEEP };
	private final String[] certResetValues;
	private FlexiTableElement tableEl;
	private ResetDataIdentitiesTableModel dataModel;
	
	private final ResetDataContext dataContext;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private boolean certificateEnabled;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public ResetCoursePassedController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ResetDataContext dataContext, UserCourseEnvironment coachCourseEnv,
			AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "course_passed_reset");
		this.dataContext = dataContext;
		this.coachCourseEnv = coachCourseEnv;
		this.assessmentCallback = assessmentCallback;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		certificateEnabled = certificatesManager.isCertificateEnabled(coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		if (certificateEnabled) {
			certResetValues = new String[] {
					translate("certificate.reset.archive"),
					translate("certificate.reset.keep")
			};
		} else {
			certResetValues = null;
		}
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.passed, new PassedCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.passedAt));
		if (certificateEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.certificate, new CertificateRenderer(getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.certificateReset));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.initialLaunchDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.lastUserModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCols.lastCoachModified));
		
		dataModel = new ResetDataIdentitiesTableModel(columnsModel);
		Translator translator = userManager.getPropertyHandlerTranslator(getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, false, translator, formLayout);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "reset-data-course-passed");
	}
	
	/*
	 * @see ResetWizardContext#isPassedParticipantAvailable()
	 */
	private void loadModel() {
		RepositoryEntry courseEntry =  dataContext.getRepositoryEntry();
		String rootIdent = coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getIdent();
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, rootIdent, null, assessmentCallback);
		if (ResetParticipants.selected == dataContext.getResetParticipants()) {
			params.setIdentityKeys(dataContext.getSelectedParticipants().stream().map(Identity::getKey).collect(Collectors.toSet()));
		}
		
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, null);
		Map<Long,AssessmentEntry> entryMap = assessmentEntries.stream()
			.filter(entry -> entry.getIdentity() != null)
			.collect(Collectors.toMap(entry -> entry.getIdentity().getKey(), entry -> entry, (u, v) -> u));
		
		Map<Long, Date> initialLaunchDates = userInfosMgr.getInitialLaunchDates(courseEntry.getOlatResource());
		
		Map<Long, CertificateLight> certificateMap = certificateEnabled
				? certificatesManager.getLastCertificates(courseEntry.getOlatResource()).stream()
					.collect(Collectors.toMap(CertificateLight::getIdentityKey, Function.identity()))
				: Map.of();
		
		List<ResetDataIdentityRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			ResetDataIdentityRow row = new ResetDataIdentityRow(assessedIdentity, userPropertyHandlers, getLocale());
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			if(entry != null) {
				Overridable<Boolean> passedOverridable = entry.getPassedOverridable();
				Boolean passed = passedOverridable.getCurrent();
				
				if (passedOverridable.isOverridden()) {
					if (dataContext.getParticipantsResetPasedOverridden() != null && dataContext.getParticipantsResetPasedOverridden().contains(assessedIdentity)) {
						passed = passedOverridable.getOriginal();
					} else {
						// User has overridden status which is not reset
						continue;
					}
				}
				
				if (passed != null && passed.booleanValue()) {
					row.setAssessmentEntry(entry);
					row.setPassed(passed);
					row.setInitialCourseLaunchDate(initialLaunchDates.get(assessedIdentity.getKey()));
					row.setCertificate(certificateMap.get(assessedIdentity.getKey()));
					forgeCertificateReset(row);
					rows.add(row);
				}
			}
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		flc.contextPut("numRows", rows.size());
	}

	private void forgeCertificateReset(ResetDataIdentityRow row) {
		if (!certificateEnabled || row.getCertificate() == null || CertificateStatus.ok != row.getCertificate().getStatus()) {
			return;
		}
		
		String name = "cert.reset." + row.getIdentityKey();
		SingleSelection certificateResetItem = new SelectboxSelectionImpl(name, name, getLocale());
		certificateResetItem.setKeysAndValues(CERT_RESET_KEYS, certResetValues, null);
		certificateResetItem.select(CERT_RESET_ARCHIVE, true);
		row.setCertificateResetItem(certificateResetItem);
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		updateContext();
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	@Override
	protected void formFinish(UserRequest ureq) {
		updateContext();
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void updateContext() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<Identity> participantsResetPassed = new ArrayList<>();
		List<Identity> participantsArchiveCertificate = new ArrayList<>();
		for(Integer selectedIndex:selectedIndexes) {
			ResetDataIdentityRow row = dataModel.getObject(selectedIndex.intValue());
			if(row != null && row.getIdentity() != null) {
				participantsResetPassed.add(row.getIdentity());
				if (row.getCertificateResetItem() != null && row.getCertificateResetItem().isOneSelected() && row.getCertificateResetItem().isKeySelected(CERT_RESET_ARCHIVE)) {
					participantsArchiveCertificate.add(row.getIdentity());
				}
			}
		}
		dataContext.setParticipantsResetPassed(participantsResetPassed);
		dataContext.setParticipantsArchiveCertificate(participantsArchiveCertificate);
	}
	
	private static final class CertificateRenderer implements FlexiCellRenderer {
		
		private final Formatter formatter;

		public CertificateRenderer(Locale locale) {
			formatter = Formatter.getInstance(locale);
		}

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
				URLBuilder ubu, Translator translator) {
			if(cellValue instanceof CertificateLight certificate) {
				if (CertificateStatus.ok == certificate.getStatus()) {
					String name = formatter.formatDate(certificate.getCreationDate());
					target.append("<i class='o_icon o_filetype_pdf'> </i> ").append(name).append(".pdf").append("</a>");
				}
			}
		}
		
	}

}
