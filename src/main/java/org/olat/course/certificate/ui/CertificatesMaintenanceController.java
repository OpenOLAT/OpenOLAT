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
package org.olat.course.certificate.ui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificatesMaintenanceController extends FormBasicController {
	
	private static final String RESEND_KEY = "resend";
	
	private DateChooser dateRangeEl;
	private StaticTextElement resultEl;
	private FormLink regenerateButton;
	private MultipleSelectionElement resendEmailEl;
	
	private List<Certificate> brokenCertificates;
	
	private CloseableModalController cmc;
	private ConfirmationController confirmationCtrl;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificatesMaintenanceController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("maintenance.broken.certificates.info");
		
		dateRangeEl = uifactory.addDateChooser("maintenance.range", "maintenance.range", null, formLayout);
		dateRangeEl.setSecondDate(true);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("search", buttonsCont);
		
		resultEl = uifactory.addStaticTextElement("maintenance.result", "maintenance.result", "", formLayout);
		resultEl.setDomWrapperElement(DomWrapperElement.div);
		resultEl.setVisible(false);
		
		SelectionValues onPK = new SelectionValues();
		onPK.add(SelectionValues.entry(RESEND_KEY, ""));
		resendEmailEl = uifactory.addCheckboxesHorizontal("maintenance.resend.emails", "maintenance.resend.emails", formLayout,
				onPK.keys(), onPK.values());
		resendEmailEl.setVisible(false);
		
		FormLayoutContainer regenerateButtonsCont = uifactory.addButtonsFormLayout("reg.buttons", null, formLayout);
		regenerateButton = uifactory.addFormLink("maintenance.regenerate", "maintenance.regenerate", null, regenerateButtonsCont, Link.BUTTON);
		regenerateButton.setVisible(false);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmationCtrl == source) {
			if(event == Event.DONE_EVENT && confirmationCtrl.getUserObject() instanceof Regeneration regeneration) {
				doRegenerate(regeneration.certificates(), regeneration.sendMail());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmationCtrl);
		removeAsListenerAndDispose(cmc);
		confirmationCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		dateRangeEl.clearError();
		if(dateRangeEl.getDate() != null && dateRangeEl.getSecondDate() != null && dateRangeEl.getDate().after(dateRangeEl.getSecondDate())) {
			dateRangeEl.setErrorKey("error.start.after.end");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == regenerateButton) {
			doConfirmRegenerate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSearch();
	}
	
	private void doSearch() {
		Date start = dateRangeEl.getDate();
		LocalDateTime startDateTime = (start == null ? null : DateUtils.toLocalDateTime(start));
		Date end = dateRangeEl.getSecondDate();
		LocalDateTime endDateTime = (end == null ? null : DateUtils.toLocalDateTime(DateUtils.getEndOfDay(end)));
		
		brokenCertificates = certificatesManager.getBrokenCertificates(startDateTime, endDateTime);
		String result;
		if(brokenCertificates.isEmpty()) {
			result = getMessage("o_success_with_icon", "maintenance.broken.certificates.zero", brokenCertificates.size());
		} else if(brokenCertificates.size() == 1) {
			result = getMessage("o_warning_with_icon", "maintenance.broken.certificates.singular", brokenCertificates.size());
		} else {
			result = getMessage("o_warning_with_icon", "maintenance.broken.certificates.plural", brokenCertificates.size());
		}
		resultEl.setValue(result);
		resultEl.setVisible(true);
		resendEmailEl.setVisible(!brokenCertificates.isEmpty());
		regenerateButton.setVisible(!brokenCertificates.isEmpty());
	}
	
	private String getMessage(String cssClass, String i18nKey, int number) {
		return "<div class='o_no_margin " + cssClass + "'>" + translate(i18nKey, Integer.toString(number)) + "</div>";
	}
	
	private void doConfirmRegenerate(UserRequest ureq) {
		boolean withMail = resendEmailEl.isAtLeastSelected(1);
		int numOfCertificates = brokenCertificates.size();
		Regeneration userObject = new Regeneration(new ArrayList<>(brokenCertificates), withMail);
		
		String messageKey = withMail
				? (numOfCertificates == 1 ? "maintenance.confirm.certificate.mail.singular" : "maintenance.confirm.certificate.mail.plural")
				: (numOfCertificates == 1 ? "maintenance.confirm.certificate.singular" : "maintenance.confirm.certificate.plural");
		String buttonKey = withMail
				? "maintenance.confirm.button.mail"
				: "maintenance.confirm.button";
		confirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate(messageKey, Integer.toString(brokenCertificates.size())), null, translate(buttonKey),
				ButtonType.submitPrimary);
		confirmationCtrl.setUserObject(userObject);
		listenTo(confirmationCtrl);		
		
		String title = translate("maintenance.confirm.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmationCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRegenerate(List<Certificate> certificates, boolean sendMail) {
		for(Certificate certificate:certificates) {
			certificate = certificatesManager.getCertificateById(certificate.getKey());
			if(certificate == null) continue;
			
			Identity assessedIdentity = certificate.getIdentity();
			if(certificate.getCertificationProgram() != null) {
				CertificationProgram program = certificationProgramService
						.getCertificationProgram(certificate.getCertificationProgram());
				regenerateProgramCertificate(certificate, assessedIdentity, program, sendMail);
			} else if(certificate.getOlatResource() != null && "CourseModule".equals(certificate.getOlatResource().getResourceableTypeName())) {
				try {
					RepositoryEntry courseEntry = repositoryService
							.loadByResourceId(certificate.getOlatResource().getResourceableTypeName(), certificate.getOlatResource().getResourceableId());
					if(courseEntry != null) {
						ICourse course = CourseFactory.loadCourse(courseEntry);
						regenerateCourseCertificate( certificate, assessedIdentity, courseEntry, course, sendMail);
					}
				} catch (CorruptedCourseException e) {
					getLogger().warn("Certificate cannot be regenerated, course corrupted: {}", certificate.getOlatResource());
				}
			}
		}
		
		certificatesManager.triggerGenerationJob();
		
		showInfo("maintenance.regenerate.started", Integer.toString(certificates.size()));
	}
	
	private void regenerateProgramCertificate(Certificate certificate, Identity assessedIdentity, CertificationProgram program, boolean sendMail) {
		CertificateTemplate template = program.getTemplate();
		
		boolean printTemplateEnabled = program.isPrintTemplateEnabled();
		CertificateTemplate printTemplate = program.getPrintTemplate();
		
		// No course informations, only certification program informations
		CertificateInfos certificateInfos = CertificateInfos.valueOf(assessedIdentity, null, null, getIdentity());
		certificateInfos.setCreationDate(certificate.getCreationDate());
		
		CertificateConfig config = CertificateConfig.builder()
				.withCustom1(program.getCertificateCustom1())
				.withCustom2(program.getCertificateCustom2())
				.withCustom3(program.getCertificateCustom3())
				.withSendEmail(sendMail)
				.withSendEmailBcc(false)
				.withSendEmailLinemanager(false)
				.withSendEmailIdentityRelations(false)
				.withCertificationProgramMailType(CertificationProgramMailType.certificate_issued)
				.build();
		getLogger().info(Tracing.M_AUDIT, "Regenerate certificate with ID: {} for user {} with email: {} in certification program {}", certificate.getKey(), assessedIdentity.getKey(), sendMail, program.getKey());
		certificatesManager.resetGenerateCertificateFile(certificate, certificateInfos, template, printTemplateEnabled, printTemplate, config);
	}
	
	private void regenerateCourseCertificate(Certificate certificate, Identity assessedIdentity, RepositoryEntry courseEntry, ICourse course, boolean sendMail) {
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(assessedIdentity);
		ienv.setRoles(securityManager.getRoles(assessedIdentity));
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

		ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
		CourseNode rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		AssessmentEvaluation rootEval = scoreAccounting.evalCourseNode(rootNode);

		RepositoryEntryCertificateConfiguration certificateConfig = certificatesManager.getConfiguration(courseEntry);
		CertificateTemplate template = certificateConfig.getTemplate();
		CertificateInfos certificateInfos = CertificateInfos.valueOf(assessedIdentity, rootEval,
				userCourseEnv.getCourseEnvironment(), getIdentity());
		certificateInfos.setCreationDate(certificate.getCreationDate());
		CertificateConfig config = CertificateConfig.builder()
				.withCustom1(certificateConfig.getCertificateCustom1())
				.withCustom2(certificateConfig.getCertificateCustom2())
				.withCustom3(certificateConfig.getCertificateCustom3())
				.withSendEmail(sendMail)
				.withSendEmailBcc(sendMail)
				.withSendEmailLinemanager(sendMail)
				.withSendEmailIdentityRelations(sendMail)
				.build();
		getLogger().info(Tracing.M_AUDIT, "Regenerate certificate with ID: {} for user {} with email: {} in course {}", certificate.getKey(), assessedIdentity.getKey(), sendMail, courseEntry.getKey());
		certificatesManager.resetGenerateCertificateFile(certificate, certificateInfos, template, false, null, config);
	}
	
	private record Regeneration(List<Certificate> certificates, boolean sendMail) {
		//
	}
}
