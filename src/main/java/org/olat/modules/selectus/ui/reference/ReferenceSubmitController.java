/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.position.PositionEditProfileController.DocumentElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceSubmitController extends FormBasicController {

	private static final Set<String> pdfType = Collections.singleton("application/pdf");
	private static final String[] onKeys = new String[]{ "on" };
	
	private FileElement letterEl;
	private final List<MultipleSelectionElement> disclaimersEl = new ArrayList<>();
	
	private Position position;
	private Reference reference;
	private final Application application;
	private List<Application> applicationsList;
	private final int numOfReferenceDisclaimers;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ReferenceSubmitController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, List<Application> applicationsList, Reference reference) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.reference = reference;
		this.position = position;
		this.application = application;
		this.applicationsList = applicationsList;
		numOfReferenceDisclaimers = recruitingModule.getReferenceNumberOfDisclaimers();
		initForm(ureq);		 
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String i18nFileLabel = "edit.recommendation.document";
		if(reference.getReferenceType() == ReferenceType.expert) {
			setFormInfo("reference.upload.expert");
			i18nFileLabel = "edit.expert.document";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			setFormInfo("reference.upload.recommendation");
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			i18nFileLabel = "edit.comparative.expert.document";
			setFormInfo("reference.upload.comparative.expert");
		}

		letterEl = uifactory.addFileElement(getWindowControl(), getIdentity(), i18nFileLabel, formLayout);
		letterEl.setMaxUploadSizeKB(20480, "error.upload.maxsize", new String[] {"20"});
		letterEl.limitToMimeType(pdfType, "error.file.type", null);
		letterEl.addActionListener(FormEvent.ONCHANGE);
		letterEl.setMandatory(true);
		letterEl.setDeleteEnabled(true);
		letterEl.setUserObject(new DocumentElement());
		Attachment letter = reference.getLetter();
		if(letter != null) {
			File file = new File(letter.getName());
			letterEl.setInitialFile(file);
		}
		
		if(numOfReferenceDisclaimers > 0 && reference.getReferenceType() == ReferenceType.expert) {
			String disclaimerInfo = translate("reference.upload.disclaimer");
			uifactory.addStaticTextElement("disclaimer_info", null, disclaimerInfo, formLayout);
		
			for(int i=0; i<numOfReferenceDisclaimers; i++) {
				String[] onValues = new String[]{ translate("reference.upload.disclaimer." + i) };
				MultipleSelectionElement disclaimerEl = uifactory.addCheckboxesHorizontal("disclaimer_" + i, null, formLayout, onKeys, onValues);
				disclaimersEl.add(disclaimerEl);
			}
		}
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		letterEl.clearError();
		if(letterEl.getUploadSize() < 10) {
			letterEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= validateFormItem(ureq, letterEl);
		}
		
		for(MultipleSelectionElement disclaimerEl:disclaimersEl) {
			disclaimerEl.clearError();
			if(!disclaimerEl.isAtLeastSelected(1)) {
				disclaimerEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == letterEl) {
			if(letterEl.getUserObject() instanceof DocumentElement) {
				DocumentElement docEl = (DocumentElement)letterEl.getUserObject();
				if(event instanceof DeleteFileElementEvent) {
					if(letterEl.getInitialFile() != null) {
						if(letterEl.getUploadFile() != null) {
							letterEl.reset();
							letterEl.clearError();
						} else {
							docEl.setDelete(true);
							letterEl.setInitialFile(null);
							letterEl.clearError();
						}	
					} else if (letterEl.getUploadFile() != null) {
						letterEl.reset();
						letterEl.clearError();
					}
				} else {
					docEl.setDelete(false);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		reference = erFrontendManager.getReferenceById(reference.getKey());
		DocumentElement letterDoc = (DocumentElement)letterEl.getUserObject();
		if(letterDoc.isDelete()) {
			reference = erFrontendManager.deleteAttachment(reference, reference.getLetter());
		} else {
			Attachment letter = commitDocument(letterEl, reference.getLetter());
			if(letter != null) {
				reference.setLetter(letter);
				reference.setReferenceStatus(ReferenceStatus.submitted);
			}
			reference.setSubmissionDate(new Date());
			reference = erFrontendManager.updateReference(reference);
		}
		
		log(letterDoc.isDelete());
		fireEvent(ureq, Event.DONE_EVENT);
	}
		
	private void log(boolean delete) {
		ActionTarget target = null;
		String messageI18n = "";
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expertOpinion;
			messageI18n = delete ? "audit.log.expert.doc.delete" : "audit.log.expert.doc.add";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referenceLetter;
			messageI18n = delete ? "audit.log.reference.doc.delete" : "audit.log.reference.doc.add";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeAssessment;
			messageI18n = delete ? "audit.log.comparative.expert.doc.delete" : "audit.log.comparative.expert.doc.add";
		}
		
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(reference, getLocale()),
			salutationGenerator.getTitleFullname(application, applicationsList, getLocale()),
			RecruitingHelper.formatIDs(application, applicationsList)
		};
		
		Action action = delete ? Action.delete : Action.add;
		
		auditService.auditRefereeLog(action, target, null, null, messageI18n, messageArgs, getTranslator(), position, application, reference, null);
	}
	
	private Attachment commitDocument(FileElement fileEl, Attachment attachment) {
		File file = fileEl.getUploadFile();
		String filename = fileEl.getUploadFileName();
		if(file != null && file.exists()) {
			try(FileInputStream fis = new FileInputStream(file)) {
				byte[] datas = IOUtils.toByteArray(fis);
				if(!StringHelper.containsNonWhitespace(filename)) {
					filename = file.getName();
				}
				attachment = erFrontendManager.setAttachmentDatas(position, reference, attachment, filename, DocumentType.pdf, datas);
				FileUtils.closeSafely(fis);
				return attachment;
			} catch (Exception e) {
				logError("", e);
			}
		}
		return null;
	}
}