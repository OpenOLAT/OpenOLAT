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
package org.olat.modules.selectus.ui.reference;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.i18n.I18nManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.PersonTitle;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.position.PositionEditProfileController.DocumentElement;
import org.olat.modules.selectus.ui.reference.ReferenceToApplicationsTableModel.RefToAppCols;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReferenceEditController extends FormBasicController {

	private static final Set<String> pdfType = Collections.singleton("application/pdf");

	private SingleSelection titleEl;
	private TextElement firstNameEl;
	private TextElement lastNameEl;
	private TextElement institutionEl;
	private TextElement emailEl;
	private DateChooser deadlineEl;
	private SingleSelection typeEl;
	private StaticTextElement staticTypeEl;
	private SingleSelection requestEl;
	private TextElement adminNoteEl;
	private FileElement letterEl;
	private SingleSelection chooseComparativeAssesssmentEl;
	
	private FormLink addApplicationButton;
	private FlexiTableElement applicationsTableEl;
	private StaticTextElement applicationsWarningEl;
	private ReferenceToApplicationsTableModel applicationsTableModel;
	
	private Position position;
	private Reference reference;
	private Application application;
	private ReferenceType referenceType;
	private List<ReferenceToApplicationRow> applicationsToCompare;
	private final RecruitingPositionSecurityCallback secCallback;

	private CloseableModalController cmc;
	private ConfirmDeleteExpertController confirmDeleteCtrl;
	private ApplicationChooserController chooseApplicationsCtrl;
	private ConfirmRemoveApplicationFromReferenceController confirmRemoveApplicationCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ApplicationReferenceEditController(UserRequest ureq, WindowControl wControl,
			Position position, Reference reference, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.application = reference.getApplication();
		
		if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			List<ReferenceToApplication> rels = recruitingService.getReferenceToApplications(reference);
			applicationsToCompare = rels.stream()
					.map(ReferenceToApplicationRow::new)
					.collect(Collectors.toList());
		} else {
			applicationsToCompare = new ArrayList<>();
		}
		
		this.reference = reference;
		this.secCallback = secCallback;
		referenceType = reference.getReferenceType();
		this.position = position;
		initForm(ureq);
		updateReference(reference);
	}
	
	public ApplicationReferenceEditController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, List<Application> applicationsToCompare,
			ReferenceType referenceType, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		if(referenceType == ReferenceType.comparativeAssessmentExpert) {
			this.applicationsToCompare = applicationsToCompare.stream()
					.map(ReferenceToApplicationRow::new)
					.collect(Collectors.toList());
		} else {
			this.applicationsToCompare = new ArrayList<>();
		}
		this.secCallback = secCallback;
		this.referenceType = referenceType;
		initForm(ureq);
		updateReference(null);
	}
	
	public Reference getReference() {
		return reference;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.reference.text");
		if(recruitingModule.isReferenceConsentEnabled()
				&& secCallback.canSeeExpertBlackList() && application != null
				&& StringHelper.containsNonWhitespace(application.getExpertBlackList())) {
			String applicant = salutationGenerator.getTitleFullname(application, getLocale());
			String blackList = Formatter.escWithBR(StringHelper.escapeHtml(application.getExpertBlackList())).toString();
			setFormWarning("warning.expert.blacklist", new String[] { StringHelper.escapeHtml(applicant), blackList });
		}
		
		if((reference == null || reference.getKey() == null) && referenceType == ReferenceType.comparativeAssessmentExpert) {
			initReferenceToApplicationsChooser(formLayout);
		}
		initFormPersonalInfos(formLayout);
		initFormReferenceSettings(formLayout);
		initFormReferenceToApplications(formLayout);
		initFormStatusAndFiles(formLayout, ureq);
	}
	
	private void initReferenceToApplicationsChooser(FormItemContainer formLayout) {
		setFormDescription("choose.comparative.assessment.desc");
		
		SelectionValues assessmentPK = new SelectionValues();
		assessmentPK.add(SelectionValues.entry("new", translate("new.comparative.assessment")));
		
		List<ReferenceToApplication> refToApps = recruitingService.getReferenceToApplications(position);
		Map<Reference,List<Application>> refToAppsMap = new HashMap<>();
		for(ReferenceToApplication refToApp:refToApps) {
			refToAppsMap
				.computeIfAbsent(refToApp.getReference(), ref -> new ArrayList<>())
				.add(refToApp.getApplication());
		}
		
		for(Map.Entry<Reference,List<Application>> refToAppsEntry:refToAppsMap.entrySet()) {
			Reference ref = refToAppsEntry.getKey();
			List<Application> applicationsList = refToAppsEntry.getValue();
			
			String referenceName = salutationGenerator.getFullname(ref, getLocale());
			String applicationsNames = salutationGenerator.getFullname(null, applicationsList, getLocale());
			String[] args = new String[] { referenceName, applicationsNames };
			assessmentPK.add(SelectionValues.entry(ref.getKey().toString(), translate("choice.comparative.assessment.option", args)));
		}
		
		chooseComparativeAssesssmentEl = uifactory.addDropdownSingleselect("choose.comparative.assessment", "choose.comparative.assessment", formLayout,
				assessmentPK.keys(), assessmentPK.values());
		chooseComparativeAssesssmentEl.setMandatory(true);
		chooseComparativeAssesssmentEl.setVisible(referenceType == ReferenceType.comparativeAssessmentExpert);
		chooseComparativeAssesssmentEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	private void initFormPersonalInfos(FormItemContainer formLayout) {
		PersonTitle[] personTitles = recruitingModule.getReferencePersonTitles();
		String[] titleKeys = new String[personTitles.length + 1];
		String[] titleValues = new String[personTitles.length + 1];
		titleKeys[0] = "";
		titleValues[0] = "-";
		for(int i=personTitles.length; i-->0; ) {
			titleKeys[i+1] = personTitles[i].title();
			titleValues[i+1] = translate(personTitles[i].i18nKey());
		}
		
		titleEl = uifactory.addDropdownSingleselect("edit.reference.title", "edit.reference.title", formLayout, titleKeys, titleValues, null);
		titleEl.setDomReplacementWrapperRequired(false);
		titleEl.setMandatory(true);
		
		firstNameEl = uifactory.addTextElement("edit.reference.firstname", "edit.application.firstName", 255, "", formLayout);
		firstNameEl.setDomReplacementWrapperRequired(false);
		firstNameEl.setMandatory(true);
		
		lastNameEl = uifactory.addTextElement("edit.reference.lastname", "edit.application.lastName", 255, "", formLayout);
		lastNameEl.setDomReplacementWrapperRequired(false);
		lastNameEl.setMandatory(true);
		
		institutionEl = uifactory.addTextElement("edit.reference.institution", "edit.reference.institution", 255, "", formLayout);
		institutionEl.setMandatory(true);
		
		emailEl = uifactory.addTextElement("edit.reference.email", "edit.reference.email", 255, "", formLayout);
		emailEl.setMandatory(true);
		uifactory.addSpacerElement("spacer1", formLayout, false);
	}
	
	private void initFormReferenceSettings(FormItemContainer formLayout) {
		deadlineEl = uifactory.addDateChooser("edit.reference.deadline", "edit.reference.deadline", null, formLayout);
		deadlineEl.setMandatory(true);
		
		uifactory.addSpacerElement("spacer2", formLayout, false);
		
		SelectionValues typesPK = new SelectionValues();
		if(position.isRefereeRecommendationEnabled()) {
			typesPK.add(SelectionValues.entry(ReferenceType.recommendation.name(), translate("edit.reference.type.recommendation")));
		}
		if(position.isExpertRecommendationEnabled()) {
			typesPK.add(SelectionValues.entry(ReferenceType.expert.name(), translate("edit.reference.type.expert")));
		}
		if(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()) {
			typesPK.add(SelectionValues.entry(ReferenceType.comparativeAssessmentExpert.name(), translate("edit.reference.type.comparative.expert")));
		}
		typeEl = uifactory.addRadiosHorizontal("edit.reference.type", "edit.reference.type", formLayout, typesPK.keys(), typesPK.values());
		typeEl.addActionListener(FormEvent.ONCHANGE);
		ReferenceType type = reference == null ? referenceType : reference.getReferenceType();
		boolean typeFound = false;
		if(type != null) {
			for(String typeKey:typesPK.keys()) {
				if(typeKey.equals(type.name())) {
					typeEl.select(typeKey, true);
					typeFound = true;
				}
			}
		}
		if(!typeFound) {
			if(position.isRefereeRecommendationEnabled()) {
				typeEl.select(ReferenceType.recommendation.name(), true);
			} else if(position.isExpertRecommendationEnabled()) {
				typeEl.select(ReferenceType.expert.name(), true);
			} else if(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()) {
				typeEl.select(ReferenceType.comparativeAssessmentExpert.name(), true);
			}
		}
		
		String val = null;
		if(typeEl.isOneSelected()) {
			val = typeEl.getSelectedValue();
		}
		staticTypeEl = uifactory.addStaticTextElement("static.reference.type", "edit.reference.type", val, formLayout);
		
		boolean typeVisible = isTypeVisible();
		if(typeEl.isOneSelected() && ReferenceType.comparativeAssessmentExpert.name().equals(typeEl.getSelectedKey()) ) {
			typeEl.setVisible(false);
			staticTypeEl.setVisible(typeVisible);
		} else {
			typeEl.setVisible(typeVisible);
			staticTypeEl.setVisible(false);
		}

		SpacerElement spacer3 = uifactory.addSpacerElement("spacer3", formLayout, false);
		spacer3.setVisible(typeVisible);
	}
	
	private void initFormReferenceToApplications(FormItemContainer formLayout) {

		addApplicationButton = uifactory.addFormLink("add.comparative.expert", "add.application.comparative.expert", null, formLayout, Link.BUTTON);
		addApplicationButton.getComponent().setSuppressDirtyFormWarning(true);
		addApplicationButton.setElementCssClass("text-right");
		addApplicationButton.setVisible(referenceType == ReferenceType.comparativeAssessmentExpert);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefToAppCols.id));
		
		initColumnModel(RefToAppCols.title, recruitingModule.getTableReferenceToApplicationTitleOption(), columnsModel);
		initColumnModel(RefToAppCols.firstName, recruitingModule.getTableReferenceToApplicationFirstNameOption(), columnsModel);
		initColumnModel(RefToAppCols.lastName, recruitingModule.getTableReferenceToApplicationLastNameOption(), columnsModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefToAppCols.mail));

		if(position.isApplicationProject()) {
			initColumnModel(RefToAppCols.projectTitle, recruitingModule.getTableReferenceToProjectTitleOption(), columnsModel);
		}

		StaticFlexiCellRenderer removeRenderer = new StaticFlexiCellRenderer(translate("remove"), "remove");
		removeRenderer.setPush(true);
		removeRenderer.setDirtyCheck(false);
		DefaultFlexiColumnModel removeColumn = new DefaultFlexiColumnModel(true, true, "remove", null, -1, "remove", false, null, FlexiColumnModel.ALIGNMENT_LEFT, removeRenderer);
		removeColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(removeColumn);

		applicationsTableModel = new ReferenceToApplicationsTableModel(columnsModel, getLocale());
		applicationsTableModel.setObjects(applicationsToCompare);
		applicationsTableEl = uifactory.addTableElement(getWindowControl(), "applications.compare", applicationsTableModel, 25, true, getTranslator(), formLayout);
		applicationsTableEl.setLabel("applications.to.compare", null);
		applicationsTableEl.setNumOfRowsEnabled(false);
		applicationsTableEl.setCustomizeColumns(false);
		applicationsTableEl.setVisible(referenceType == ReferenceType.comparativeAssessmentExpert);
		
		String message = "<p class='o_warning'>" + translate("warning.last.application") + "</p>";
		applicationsWarningEl = uifactory.addStaticTextElement("warning.applications.compare", null, message, formLayout);
		applicationsWarningEl.setElementCssClass("o_sel_applications_to_reference");
		applicationsWarningEl.setVisible(false);
	}
	
	private void initColumnModel(RefToAppCols field, RecruitingTableOption option, FlexiTableColumnModel columnsModel) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field));
		}
	}
	
	private void initFormStatusAndFiles(FormItemContainer formLayout, UserRequest ureq) {
		SelectionValues requestValues = new SelectionValues();
		requestValues.add(SelectionValues.entry(ReferenceRequestStatus.notAnswered.name(), translate("edit.reference.request.not.answered")));
		requestValues.add(SelectionValues.entry(ReferenceRequestStatus.declined.name(), translate("edit.reference.request.declined")));
		requestValues.add(SelectionValues.entry(ReferenceRequestStatus.accepted.name(), translate("edit.reference.request.accepted")));
		requestEl = uifactory.addDropdownSingleselect("edit.reference.request", "edit.reference.request", formLayout, requestValues.keys(), requestValues.values());
		requestEl.setVisible(recruitingModule.isReferenceRefereeConsentEnabled());
		
		adminNoteEl = uifactory.addTextAreaElement("edit.reference.admin.note", "edit.reference.admin.note", 4000, 6, 60, false, false, false, "", formLayout);
		adminNoteEl.setHelpTextKey("edit.reference.admin.note.hint", null);
		adminNoteEl.setVisible(recruitingModule.isReferenceAdminNotes());
		
		letterEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "document1", "edit.expert.document", formLayout);
		letterEl.setMaxUploadSizeKB(20480, "error.upload.maxsize", new String[] {"20"});
		letterEl.limitToMimeType(pdfType, "error.file.type", null);
		letterEl.setDeleteEnabled(true);
		letterEl.setUserObject(new DocumentElement());
		Attachment letter = reference == null ? null : reference.getLetter();
		if(letter != null) {
			File file = new File(letter.getName());
			letterEl.setInitialFile(file);
		}
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateLabels();
	}
	
	private boolean isTypeVisible() {
		int count = 0;
		if(position.isExpertRecommendationEnabled()) {
			count++;
		}
		if(position.isRefereeRecommendationEnabled()) {
			count++;
		}
		if(position.isComparativeAssessmentExpertEnabled()) {
			count++;
		}
		if(count > 1) {
			return true;
		}
		if(position.isExpertRecommendationEnabled()) {
			return referenceType == ReferenceType.recommendation || referenceType == ReferenceType.comparativeAssessmentExpert;
		}
		if(position.isRefereeRecommendationEnabled()) {
			return referenceType == ReferenceType.expert || referenceType == ReferenceType.comparativeAssessmentExpert;
		}
		if(position.isComparativeAssessmentExpertEnabled()) {
			return referenceType == ReferenceType.expert || referenceType == ReferenceType.recommendation;
		}
		return false;
	}
	
	private void updateLabels() {
		if(typeEl.isOneSelected()) {
			ReferenceType refType = ReferenceType.valueOf(typeEl.getSelectedKey());
			if(refType == ReferenceType.expert) {
				letterEl.setLabel("edit.expert.document", null);
				deadlineEl.setLabel("edit.expert.deadline", null);
				letterEl.setHelpTextKey("edit.docs.committee.expert.help", null);
			} else if(refType == ReferenceType.recommendation) {
				letterEl.setLabel("edit.recommendation.document", null);
				deadlineEl.setLabel("edit.reference.deadline", null);
				letterEl.setHelpTextKey("edit.docs.committee.recommendation.help", null);
			} else if(refType == ReferenceType.comparativeAssessmentExpert) {
				letterEl.setLabel("edit.comparative.expert.document", null);
				deadlineEl.setLabel("edit.expert.deadline", null);
				letterEl.setHelpTextKey("edit.docs.committee.expert.help", null);
			}
		}
	}
	
	private ReferenceRequestStatus getRequestStatus() {
		return requestEl.isOneSelected() ? ReferenceRequestStatus.valueOf(requestEl.getSelectedKey()) : ReferenceRequestStatus.notAnswered;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doDeleteReference();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(chooseApplicationsCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doAddApplications(chooseApplicationsCtrl.getSelectedApplications());
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRemoveApplicationCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateApplications();
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.CLOSE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveApplicationCtrl);
		removeAsListenerAndDispose(chooseApplicationsCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveApplicationCtrl = null;
		chooseApplicationsCtrl = null;
		confirmDeleteCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(chooseComparativeAssesssmentEl == source) {
			if(chooseComparativeAssesssmentEl.isOneSelected()) {
				if("new".equals(chooseComparativeAssesssmentEl.getSelectedKey())) {
					updateReference(null);
				} else if(StringHelper.isLong(chooseComparativeAssesssmentEl.getSelectedKey())) {
					updateReferenceByKey(Long.valueOf(chooseComparativeAssesssmentEl.getSelectedKey()));
				}
			}
		} else if(addApplicationButton == source) {
			doAddApplications(ureq);
		} else if(letterEl == source) {
			DocumentElement docEl = (DocumentElement)letterEl.getUserObject();
			if(event instanceof DeleteFileElementEvent) {
				if(letterEl.getInitialFile() != null) {
					if(letterEl.getUploadFile() != null) {
						letterEl.reset();
					} else {
						docEl.setDelete(true);
						letterEl.setInitialFile(null);
					}	
				} else if (letterEl.getUploadFile() != null) {
					letterEl.reset();
				}
			} else {
				docEl.setDelete(false);
			}
		} else if(typeEl == source) {
			updateLabels();
			updateTypes();
		} else if(applicationsTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("remove".equals(se.getCommand())) {
					ReferenceToApplicationRow row = applicationsTableModel.getObject(se.getIndex());
					doConfirmRemoveReferenceToApplication(ureq, row);
				}
			}
		}
	}
	
	private void updateTypes() {
		if(!typeEl.isEnabled() || !typeEl.isVisible() || referenceType == ReferenceType.comparativeAssessmentExpert) return;
		
		ReferenceType newType = ReferenceType.valueOf(typeEl.getSelectedKey());
		if(newType == ReferenceType.comparativeAssessmentExpert) {
			applicationsTableEl.setVisible(true);
			addApplicationButton.setVisible(true);

			List<ReferenceToApplicationRow> rows = applicationsTableModel.getObjects();
			if(reference != null && reference.getApplication() != null) {
				rows.add(new ReferenceToApplicationRow(reference.getApplication()));
			} else if(application != null) {
				rows.add(new ReferenceToApplicationRow(application));
			}
			applicationsTableModel.setObjects(rows);
			updateApplications();
		}
	}

	private void updateReferenceByKey(Long referenceKey) {
		Reference ref = recruitingService.getReferenceById(referenceKey);
		updateReference(ref);
	}
	
	private void updateReference(Reference reference) {
		this.reference = reference;
		
		String title = reference == null ? null : reference.getTitle();
		if(StringHelper.containsNonWhitespace(title)) {
			for(String key: titleEl.getKeys()) {
				if(key.equals(title)) {
					titleEl.select(key , true);
				}
			}
		} else {
			titleEl.select(titleEl.getKey(0), true);
		}
		
		String firstName = reference == null ? null : reference.getFirstName();
		firstNameEl.setValue(firstName);
		String lastName = reference == null ? null : reference.getLastName();
		lastNameEl.setValue(lastName);
		String institution = reference == null ? null : reference.getInstitution();
		institutionEl.setValue(institution);
		String email = reference == null ? null : reference.getEmail();
		emailEl.setValue(email);
		
		Date deadlineDate = reference == null ? null : reference.getSubmissionDeadline();
		if(deadlineDate == null) {
			if(referenceType == ReferenceType.expert) {
				deadlineDate = position.getExpertRecommandationDeadline();
			} else if(referenceType == ReferenceType.recommendation) {
				deadlineDate = position.getRefereeRecommandationDeadline();
			} else if(referenceType == ReferenceType.comparativeAssessmentExpert) {
				deadlineDate = position.getComparativeAssessmentExpertDeadline();
			} else {
				deadlineDate = new Date();
			}
		}
		deadlineEl.setDate(deadlineDate);
		
		ReferenceRequestStatus status = reference == null ? ReferenceRequestStatus.notAnswered : reference.getRequestStatus();
		if(status != null) {
			requestEl.select(status.name(), true);
		} else if(requestEl.getKeys().length > 0) {
			requestEl.select(requestEl.getKey(0), true);
		}

		String adminNote = reference == null ? null : reference.getAdminNote();
		adminNoteEl.setValue(adminNote);
		
		Attachment letter = reference == null ? null : reference.getLetter();
		if(letter != null) {
			File file = new File(letter.getName());
			letterEl.setInitialFile(file);
		} else {
			letterEl.reset();
			letterEl.setInitialFile(null);
		}

		if(applicationsTableModel != null) {
			if(reference == null) {
				applicationsTableModel.setObjects(applicationsToCompare);
				applicationsTableEl.reset(true, true, true);
			} else {
				updateApplications();
			}
		}
	}
	
	private void updateApplications() {
		if(applicationsTableModel == null) return;
		
		List<ReferenceToApplicationRow> rows = applicationsTableModel.getObjects();
		List<ReferenceToApplicationRow> updatedRows = new ArrayList<>();
		Set<Long> selectedApplications = applicationsToCompare.stream()
				.map(ReferenceToApplicationRow::getApplication)
				.map(Application::getKey)
				.collect(Collectors.toSet());
		Set<Long> deletedApplications = rows.stream()
				.filter(row -> row.isDeleted())
				.map(ReferenceToApplicationRow::getApplication)
				.map(Application::getKey)
				.collect(Collectors.toSet());
		
		Set<Long> applicationsKeys = new HashSet<>();
		if(reference != null) {
			List<ReferenceToApplication> rels = recruitingService.getReferenceToApplications(reference);
			for(ReferenceToApplication rel:rels) {
				Long applicationKey = rel.getApplication().getKey();
				applicationsKeys.add(applicationKey);
				if(!deletedApplications.contains(applicationKey)) {
					updatedRows.add(new ReferenceToApplicationRow(rel, selectedApplications.contains(applicationKey)));
				}
			}
		}

		for(ReferenceToApplicationRow row:rows) {
			Long applicationKey = row.getApplication().getKey();
			if(row.isNewRelation() && !row.isDeleted()
					&& !applicationsKeys.contains(applicationKey) && !deletedApplications.contains(applicationKey)) {
				applicationsKeys.add(applicationKey);
				updatedRows.add(row);
			}
		}

		applicationsTableModel.setObjects(updatedRows);
		applicationsTableEl.reset(true, true, true);
		applicationsWarningEl.setVisible(this.applicationsTableEl.isVisible()
				&& (updatedRows.isEmpty() || updatedRows.size() == 1));
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		deadlineEl.clearError();
		if(deadlineEl.getDate() == null) {
			deadlineEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			Calendar cal = Calendar.getInstance();
			int currentYear = cal.get(Calendar.YEAR) + 5; 
			cal.setTime(deadlineEl.getDate());
			int year = cal.get(Calendar.YEAR);
			if(year < 1900 || year > currentYear) {
				allOk &= false;
				deadlineEl.setErrorKey("date.error");
			}
		}

		allOk &= RecruitingHelper.validateTextElement(lastNameEl, 255, true, new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(firstNameEl, 255, true, new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(institutionEl, 255, true, new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateEmailElement(emailEl, 255, true, new OWASPAntiSamyXSSFilter());
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(confirmDeleteReference()) {
			doConfirmDeleteReference(ureq);
		} else {
			commit(ureq);
		}
	}
	
	private boolean confirmDeleteReference() {
		if(applicationsTableModel != null && ReferenceType.comparativeAssessmentExpert.name().equals(typeEl.getSelectedKey())) {
			int applicationsLeft = 0;
			List<ReferenceToApplicationRow> rows = applicationsTableModel.getObjects();
			for(ReferenceToApplicationRow row:rows) {
				if(!row.isDeleted()) {
					applicationsLeft++;
				}
			}
			return applicationsLeft == 0;
		}
		return false;
	}
	
	private void doAddApplications(UserRequest ureq) {
		List<ReferenceToApplicationRow> rows = applicationsTableModel.getObjects();
		List<Application> excludeApplications = rows.stream()
				.map(ReferenceToApplicationRow::getApplication)
				.collect(Collectors.toList());

		chooseApplicationsCtrl = new ApplicationChooserController(ureq, getWindowControl(), position, excludeApplications);
		listenTo(chooseApplicationsCtrl);
		
		String i18nTitle = "select.applications";
		cmc = new CloseableModalController(getWindowControl(), "c", chooseApplicationsCtrl.getInitialComponent(), translate(i18nTitle));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddApplications(List<ApplicationLight> applications) {
		List<ReferenceToApplicationRow> rows = new ArrayList<>(applicationsTableModel.getObjects());
		for(ApplicationLight app:applications) {
			Application applicaiton = recruitingService.getApplicationByKey(app.getKey());
			rows.add(new ReferenceToApplicationRow(applicaiton));
		}
		applicationsTableModel.setObjects(rows);
		updateApplications();
	}
	
	private void doConfirmDeleteReference(UserRequest ureq) {
		confirmDeleteCtrl = new ConfirmDeleteExpertController(ureq, getWindowControl());
		listenTo(confirmDeleteCtrl);
		
		String i18nTitle = "confirm.delete.expert.title";
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteCtrl.getInitialComponent(), translate(i18nTitle));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDeleteReference() {
		if(reference != null && reference.getKey() != null) {
			recruitingService.deleteReference(reference);
		}
	}
	
	private void commit(UserRequest ureq) {
		ReferenceType refType = referenceType;
		if(typeEl.isVisible() && typeEl.isOneSelected()) {
			refType = ReferenceType.valueOf(typeEl.getSelectedKey());
		}
		String title = null;
		if(titleEl.isOneSelected()) {
			title = titleEl.getSelectedKey();
		}
		
		if(reference != null) {
			reference = recruitingService.getReferenceById(reference.getKey());
		}
		
		ReferenceRequestStatus requestStatus = getRequestStatus();
		ReferenceRequestStatus currentRequestStatus = (reference == null || reference.getRequestStatus() == null)
				? ReferenceRequestStatus.notAnswered : reference.getRequestStatus();
		String currentAdminNote = (reference == null ? null : reference.getAdminNote());
		
		String before;
		if(reference == null) {
			before = null;
			Application app = refType == ReferenceType.comparativeAssessmentExpert ? null : application;
			reference = recruitingService.addReference(title, firstNameEl.getValue(), lastNameEl.getValue(), institutionEl.getValue(), emailEl.getValue(),
					deadlineEl.getDate(), refType, requestStatus, adminNoteEl.getValue(), app, null);
		} else {
			before = auditService.toAuditXml(reference);
			reference.setReferenceType(refType);
			reference.setRequestStatus(requestStatus);
			if(currentRequestStatus != requestStatus) {
				if(requestStatus == ReferenceRequestStatus.accepted || requestStatus == ReferenceRequestStatus.declined) {
					reference.setDateConsent(new Date());
				} else if(requestStatus == ReferenceRequestStatus.notAnswered) {
					reference.setDateConsent(null);
				}
			}
			reference.setConsentByStaff(Boolean.TRUE);
			reference.setTitle(title);
			reference.setFirstName(firstNameEl.getValue());
			reference.setLastName(lastNameEl.getValue());
			reference.setInstitution(institutionEl.getValue());
			reference.setEmail(emailEl.getValue());
			reference.setSubmissionDeadline(deadlineEl.getDate());
			if(StringHelper.containsNonWhitespace(adminNoteEl.getValue())) {
				reference.setAdminNote(adminNoteEl.getValue());
			} else {
				reference.setAdminNote(null);
			}
			
			if(refType == ReferenceType.comparativeAssessmentExpert && reference.getApplication() != null) {
				reference.setApplication(null);
			}
			
			reference = recruitingService.updateReference(reference);
		}
		
		DocumentElement letterDoc = (DocumentElement)letterEl.getUserObject();
		if(letterDoc.isDelete()) {
			if(reference.getLetter() != null) {
				reference = recruitingService.deleteAttachment(reference, reference.getLetter());
				logLetter(letterDoc.isDelete());
			}
			
			if(reference.getReferenceStatus() == ReferenceStatus.submitted) {
				reference.setReferenceStatus(ReferenceStatus.notSent);
				reference = recruitingService.updateReference(reference);
			}
		} else {
			Attachment letter = commitDocument(letterEl, reference.getLetter());
			if(letter != null) {
				reference.setLetter(letter);
				reference.setReferenceStatus(ReferenceStatus.submitted);
				reference = recruitingService.updateReference(reference);
				logLetter(false);
			}
		}
		
		if(applicationsTableModel != null && refType == ReferenceType.comparativeAssessmentExpert) {
			List<ReferenceToApplicationRow> rows = applicationsTableModel.getObjects();
			for(ReferenceToApplicationRow row:rows) {
				if(row.isDeleted()) {
					recruitingService.deleteReferenceToApplications(reference, row.getApplication());
				} else if(row.isNewRelation() && row.getReferenceToApplication() == null) {
					recruitingService.addReferenceToApplication(reference, row.getApplication());
				}
			}
		}
		
		dbInstance.commit();
		
		if(reference.getApplication() != null) {
			// Load it after merging
			reference.getApplication().getId();
		}
		
		String after = auditService.toAuditXml(reference);
		if(before == null || !before.equals(after)) {
			logChanges(before, after);
			if(reference.getReferenceType() == ReferenceType.recommendation && application != null && application.getIdentity() != null) {
				notifyApplicant(ureq);
			}
		}
		if(reference.getRequestStatus() != currentRequestStatus) {
			logRequestStatus(currentRequestStatus, reference.getRequestStatus());
		}
		if(!Objects.equals(currentAdminNote, reference.getAdminNote())) {
			logAdminNote(currentAdminNote, reference.getAdminNote());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void logChanges(String before, String after) {
		ActionTarget target = null;
		String messageI18n = "";
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = (before == null) ? "audit.log.expert.add" : "audit.log.expert.update";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = (before == null) ? "audit.log.referee.add" : "audit.log.referee.update";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeExpert;
			messageI18n = (before == null) ? "audit.log.comparative.expert.add" : "audit.log.comparative.expert.update";
		} else {
			return;
		}
		
		String[] messageArgs = getMessageArgs(getLocale());
		Action action = (before == null) ? Action.add : Action.update;
		if(application != null) {
			auditService.auditRefereeLog(action, target, before, after, messageI18n, messageArgs, getTranslator(),
					position, application, reference, getIdentity());
		} else {
			for(ReferenceToApplicationRow app:applicationsTableModel.getObjects()) {
				auditService.auditRefereeLog(action, target, before, after, messageI18n, messageArgs, getTranslator(),
						position, app.getApplication(), reference, getIdentity());
			}
		}
	}
	
	private void logRequestStatus(ReferenceRequestStatus beforeStatus, ReferenceRequestStatus afterStatus) {
		Action action;
		if(afterStatus == ReferenceRequestStatus.accepted) {
			action = Action.accepted;
		} else if(afterStatus == ReferenceRequestStatus.declined) {
			action = Action.declined;
		} else {
			action = Action.reset;
		}
		
		ActionTarget target = null;
		String messageI18n = null;
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			if(afterStatus == ReferenceRequestStatus.accepted) {
				messageI18n = "audit.log.expert.staff.accept.consent";
			} else if(afterStatus == ReferenceRequestStatus.declined) {
				messageI18n = "audit.log.expert.staff.decline.consent";
			} else {
				messageI18n = "audit.log.expert.staff.reset.consent";
			}
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			if(afterStatus == ReferenceRequestStatus.accepted) {
				messageI18n = "audit.log.referee.staff.accept.consent";
			} else if(afterStatus == ReferenceRequestStatus.declined) {
				messageI18n = "audit.log.referee.staff.decline.consent";
			} else {
				messageI18n = "audit.log.referee.staff.reset.consent";
			}	
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeAssessment;
			if(afterStatus == ReferenceRequestStatus.accepted) {
				messageI18n = "audit.log.comparative.expert.staff.accept.consent";
			} else if(afterStatus == ReferenceRequestStatus.declined) {
				messageI18n = "audit.log.comparative.expert.staff.decline.consent";
			} else {
				messageI18n = "audit.log.comparative.expert.staff.reset.consent";
			}	
		}
		
		
		String[] messageArgs = getMessageArgs(getLocale());
		auditService.auditRefereeLog(action, target, beforeStatus.name(), afterStatus.name(), messageI18n, messageArgs, getTranslator(),
				position, application, reference, getIdentity());
	}
	
	private void logAdminNote(String beforeAdminNote, String afterAdminNote) {
		Action action;
		if(!StringHelper.containsNonWhitespace(beforeAdminNote)) {
			action = Action.comment;
		} else if(StringHelper.containsNonWhitespace(beforeAdminNote) && StringHelper.containsNonWhitespace(afterAdminNote)) {
			action = Action.update;
		} else {
			action = Action.remove;
		}
		
		ActionTarget target = null;
		String messageI18n = null;
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.expert.staff.admin.note";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.referee.staff.admin.note";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.comparative.expert.staff.admin.note";
		} else {
			return;
		}

		String[] messageArgs = getMessageArgs(getLocale());
		auditService.auditRefereeLog(action, target, beforeAdminNote, afterAdminNote, messageI18n, messageArgs, getTranslator(),
				position, application, reference, getIdentity());
	}
	
	private void logLetter(boolean delete) {
		ActionTarget target = null;
		String messageI18n = "";
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expertOpinion;
			messageI18n = delete ? "audit.log.expert.doc.delete" : "audit.log.expert.doc.add";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referenceLetter;
			messageI18n = delete ? "audit.log.reference.doc.delete" : "audit.log.reference.doc.add";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.referenceLetter;
			messageI18n = delete ? "audit.log.comparative.expert.doc.delete" : "audit.log.comparative.expert.doc.add";
		} else {
			return;
		}

		String[] messageArgs = getMessageArgs(getLocale());
		Action action = delete ? Action.delete : Action.add;
		auditService.auditRefereeLog(action, target, null, null, messageI18n, messageArgs, getTranslator(),
				position, application, reference, getIdentity());
	}
	
	/**
	 * This is only used for referees, not an other type of reference.
	 * 
	 * @param ureq The user request
	 */
	private void notifyApplicant(UserRequest ureq) {
		if(!RecruitingHelper.isSendRefereeNotificationToApplicant(ureq, application, position)) {
			return;
		}
		
		Locale locale = i18nManager.getLocaleOrDefault(application.getLanguage());
		locale = recruitingModule.getPositionLocale(locale.getLanguage());

		String[] messageArgs = getMessageArgs(locale);
		final Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		final Identity secretary = recruitingService.getSecretary(position);
		Translator appTranslator = Util.createPackageTranslator(PositionController.class, locale);
		
		String subject = appTranslator.translate("refereedashboard.notification.subject", messageArgs);
		String body = appTranslator.translate("refereedashboard.notification.body", messageArgs);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, null, null, subject, body, null,
				headOfCommittee, secretary, new SubjectAndBody(subject, body, null),
				salutationGenerator, appTranslator);
		recruitingService.sendToApplicant(application, position, template, false);
	}
	
	private String[] getMessageArgs(Locale locale) {
		String applicant = null;
		String applicantId = null;
		if(application != null) {
			applicant = salutationGenerator.getTitleFullname(application, locale);
			applicantId = (application.getId() == null) ? "<new>" : application.getId().toString();
		} else if(applicationsTableModel != null) {
			List<String> ids = new ArrayList<>();
			List<String> fullNames = new ArrayList<>();
			List<ReferenceToApplicationRow> refToApps = applicationsTableModel.getObjects();
			for(ReferenceToApplicationRow refToApp:refToApps) {
				fullNames.add(salutationGenerator.getTitleFullname(refToApp.getApplication(), locale));
			}
			applicant = String.join(", ", fullNames);
			applicantId = String.join(", ", ids);
		}

		return new String[] {
				salutationGenerator.getTitleFullname(reference, locale),
				applicant,
				applicantId,
				position.getMLTitle(locale)
			};
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
				attachment = recruitingService.setAttachmentDatas(position, reference, attachment, filename, DocumentType.pdf, datas);
				FileUtils.closeSafely(fis);
				return attachment;
			} catch (Exception e) {
				logError("", e);
			}
		}
		return null;
	}
	
	private void doConfirmRemoveReferenceToApplication(UserRequest ureq, ReferenceToApplicationRow row) {
		confirmRemoveApplicationCtrl = new ConfirmRemoveApplicationFromReferenceController(ureq, getWindowControl(), reference, row);
		listenTo(confirmRemoveApplicationCtrl);
		
		String i18nTitle = "reference.management.confirm.remove.application.from.reference.alt.title";
		cmc = new CloseableModalController(getWindowControl(), "c", confirmRemoveApplicationCtrl.getInitialComponent(), translate(i18nTitle));
		cmc.activate();
		listenTo(cmc);
	}
}
