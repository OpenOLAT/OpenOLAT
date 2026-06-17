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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.ReferenceStatusCellRenderer;
import org.olat.modules.selectus.ui.events.RequestPersistEvent;
import org.olat.modules.selectus.ui.reference.ApplicationReferenceDataModel.ARCols;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReferenceListController extends FormBasicController {
	
	private static final String PREFS_ID = "recruitingAppRecommendationFlexiList";
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	private FormLink addExpertButton;
	private FormLink addReferenceButton;
	private FormLink addComparativeExpertButton;
	private FlexiTableElement expertTableEl;
	private FlexiTableElement recommendationTableEl;
	private FlexiTableElement comparativeExpertTableEl;
	private ApplicationReferenceDataModel expertModel;
	private ApplicationReferenceDataModel recommendationModel;
	private ApplicationReferenceDataModel comparativeExpertModel;

	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteCtrl;
	private SendInvitationEmailController sendReferenceCtrl;
	private ApplicationReferenceEditController addExpertCtrl;
	private ApplicationReferenceEditController editReferenceCtrl;
	private ApplicationReferenceEditController addRecommendationCtrl;
	private ApplicationReferenceEditController addComparativeExpertCtrl;
	private ConfirmRemoveReferenceToApplicationController confirmRemoveComparativeExpertCtrl;
	
	private int counter = 0;
	private Application app;
	private Position position;
	private final boolean canEditReferences;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ApplicationReferenceListController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Application application, Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_CUSTOM, "app_reference_list", rootForm);
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		app = application;
		this.position = position;
		this.secCallback = secCallback;
		canEditReferences = secCallback.canEditApplicationReferences();
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		
		initForm(ureq);
		loadModel();
	}
	
	public void setApplication(Application application) {
		app = application;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(recruitingModule.isReferenceExplanationInEditApplicationEnabled()) {
				layoutCont.getFormItemComponent().contextPut("explanation", Boolean.TRUE);
			}
			if(recruitingModule.isReferenceConsentEnabled()
					&& secCallback.canSeeExpertBlackList() && app != null
					&& StringHelper.containsNonWhitespace(app.getExpertBlackList())) {
				String applicant = salutationGenerator.getTitleFullname(app, getLocale());
				String blackList = Formatter.escWithBR(StringHelper.escapeHtml(app.getExpertBlackList())).toString();
				layoutCont.getFormItemComponent().contextPut("expertWarningArgs", new String[] { StringHelper.escapeHtml(applicant), blackList });
			}
		}

		if(position.isExpertRecommendationEnabled()) {
			initExpertsForm(formLayout, ureq);
		}
		
		if(position.isRefereeRecommendationEnabled()) {
			initRecommandationsForm(formLayout, ureq);
		}
		
		if(position.isComparativeAssessmentExpertEnabled()) {
			initComparativeExpertsForm(formLayout, ureq);
		}
	}

	private void initExpertsForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.expertFullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.status, new ReferenceStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.submissionDeadline, new DateCellRenderer()));
		if(canEditReferences) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.sendMail));
			StaticFlexiCellRenderer editCol = new StaticFlexiCellRenderer(translate("edit"), "edit-exp");
			editCol.setDirtyCheck(false);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "edit", null, -1, "edit-exp", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, editCol));
			StaticFlexiCellRenderer deleteCol = new StaticFlexiCellRenderer(translate("delete"), "delete-exp");
			deleteCol.setDirtyCheck(false);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "delete", null, -1, "delete-exp", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, deleteCol));
		}
		
		expertModel = new ApplicationReferenceDataModel(columnsModel, salutationGenerator, getLocale());
		expertTableEl = uifactory.addTableElement(getWindowControl(), "experts", expertModel, 20, false, getTranslator(), formLayout);
		expertTableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		expertTableEl.setExportEnabled(false);
		expertTableEl.setElementCssClass("o_sel_position_expert_list");
		expertTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("recommendation.list.add.expert.empty")
				.build());
		expertTableEl.setPageSize(20);

		addExpertButton = uifactory.addFormLink("add.expert", formLayout, Link.BUTTON);
		addExpertButton.getComponent().setSuppressDirtyFormWarning(true);
		addExpertButton.setVisible(canEditReferences);
	}

	private void initRecommandationsForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.refereeFullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.status, new ReferenceStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.submissionDeadline, new DateCellRenderer()));
		if(canEditReferences) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.sendMail));
			StaticFlexiCellRenderer editCol = new StaticFlexiCellRenderer(translate("edit"), "edit-rec");
			editCol.setDirtyCheck(false);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "edit", null, -1, "edit-rec", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, editCol));
			StaticFlexiCellRenderer deleteCol = new StaticFlexiCellRenderer(translate("delete"), "delete-rec");
			deleteCol.setDirtyCheck(false);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "delete", null, -1, "delete-rec", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, deleteCol));
		}

		recommendationModel = new ApplicationReferenceDataModel(columnsModel, salutationGenerator, getLocale());
		recommendationTableEl = uifactory.addTableElement(getWindowControl(), "references", recommendationModel, 20, false, getTranslator(), formLayout);
		recommendationTableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		recommendationTableEl.setExportEnabled(false);
		recommendationTableEl.setElementCssClass("o_sel_position_reference_list");
		recommendationTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("recommendation.list.add.referee.empty")
				.build());
		recommendationTableEl.setPageSize(20);
		
		addReferenceButton = uifactory.addFormLink("add.recommendation", formLayout, Link.BUTTON);
		addReferenceButton.getComponent().setSuppressDirtyFormWarning(true);
		addReferenceButton.setVisible(canEditReferences);
	}
	
	private void initComparativeExpertsForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.comparativeExpertFullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.status, new ReferenceStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.submissionDeadline, new DateCellRenderer()));
		if(canEditReferences) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ARCols.sendMail));
			StaticFlexiCellRenderer editCol = new StaticFlexiCellRenderer(translate("edit"), "edit-rec");
			editCol.setDirtyCheck(false);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "edit", null, -1, "edit-rec", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, editCol));
			StaticFlexiCellRenderer deleteCol = new StaticFlexiCellRenderer(translate("remove"), "remove-rec");
			deleteCol.setDirtyCheck(false);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "remove", null, -1, "remove-rec", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, deleteCol));
		}

		comparativeExpertModel = new ApplicationReferenceDataModel(columnsModel, salutationGenerator, getLocale());
		comparativeExpertTableEl = uifactory.addTableElement(getWindowControl(), "comparative.experts", comparativeExpertModel, 20, false, getTranslator(), formLayout);
		comparativeExpertTableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		comparativeExpertTableEl.setExportEnabled(false);
		comparativeExpertTableEl.setElementCssClass("o_sel_position_comparative_experts_list");
		comparativeExpertTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("recommendation.list.add.comparative.expert.empty")
				.build());
		comparativeExpertTableEl.setPageSize(20);
		
		addComparativeExpertButton = uifactory.addFormLink("add.comparative.expert", formLayout, Link.BUTTON);
		addComparativeExpertButton.getComponent().setSuppressDirtyFormWarning(true);
		addComparativeExpertButton.setVisible(canEditReferences);
	}
	
	private void loadModel() {
		List<ApplicationReference> experts = new ArrayList<>();
		List<ApplicationReference> recommendations = new ArrayList<>();
		List<ApplicationReference> comparativeExperts = new ArrayList<>();
		
		List<Reference> references = recruitingService.getApplicationReferences(app, null);
		for(Reference reference:references) {
			ReferenceStatus status = reference.getReferenceStatus();
			
			String cmd = null;
			String i18nLink = null;
			switch(status) {
				case notSent:
				case late:
					i18nLink = translate("reference.invite");
					cmd = "ref-invite";
					break;
				case sentAwaiting:
					i18nLink = translate("reference.remind");
					cmd = "ref-invite";
					break;
				case submitted:
					i18nLink = translate("reference.reopen");
					cmd = "ref-reopen";
					break;
				case deactivated:
					i18nLink = translate("reference.reactivate");
					cmd = "ref-reactivate";
					break;	
			}

			FormLink mailLink = uifactory.addFormLink("send-" + (++counter), cmd, i18nLink, null, flc, Link.LINK | Link.NONTRANSLATED);
			mailLink.setIconLeftCSS("o_icon o_icon_mail");
			mailLink.getComponent().setSuppressDirtyFormWarning(true);
			ApplicationReference appRef = new ApplicationReference(reference, mailLink);
			mailLink.setUserObject(appRef);
			if(reference.getReferenceType() == ReferenceType.expert) {
				experts.add(appRef);
			} else if(reference.getReferenceType() == ReferenceType.recommendation) {
				recommendations.add(appRef);
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				comparativeExperts.add(appRef);
			}
		}
		
		if(expertTableEl != null) {
			expertModel.setObjects(experts);
			expertTableEl.reset();
			expertTableEl.reloadData();
		}
		
		if(recommendationTableEl != null) {
			recommendationModel.setObjects(recommendations);
			recommendationTableEl.reset();
			recommendationTableEl.reloadData();
		}
		
		if(comparativeExpertTableEl != null) {
			comparativeExpertModel.setObjects(comparativeExperts);
			comparativeExpertTableEl.reset();
			comparativeExpertTableEl.reloadData();
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addExpertButton == source) {
			doAddExpert(ureq);
		} else if(addReferenceButton == source) {
			doAddRecommendation(ureq);
		} else if(addComparativeExpertButton == source) {
			doAddComparativeExpert(ureq);
		} else if(expertTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ApplicationReference appReference = expertModel.getObject(se.getIndex());
				if("delete-exp".equals(cmd)) {
					doConfirmDelete(ureq, appReference);
				} else if("edit-exp".equals(cmd)) {
					doEdit(ureq, appReference);
				}
			}
		} else if(recommendationTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ApplicationReference appReference = recommendationModel.getObject(se.getIndex());
				if("delete-rec".equals(cmd)) {
					doConfirmDelete(ureq, appReference);
				} else if("edit-rec".equals(cmd)) {
					doEdit(ureq, appReference);
				}
			}
		} else if(comparativeExpertTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ApplicationReference appReference = comparativeExpertModel.getObject(se.getIndex());
				if("remove-rec".equals(cmd)) {
					doConfirmRemove(ureq, appReference);
				} else if("edit-rec".equals(cmd)) {
					doEdit(ureq, appReference);
				}
			}
		} 
		
		else if(source instanceof FormLink && source.getUserObject() instanceof ApplicationReference) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("ref-invite".equals(cmd) || "ref-reopen".equals(cmd) || "ref-reactivate".equals(cmd)) {
				ApplicationReference appReference = (ApplicationReference)link.getUserObject();
				doSendInvitation(ureq, appReference);
			}
		}

		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addExpertCtrl == source || addRecommendationCtrl == source
				|| addComparativeExpertCtrl == source || editReferenceCtrl == source
				|| sendReferenceCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				ApplicationReference appReference = (ApplicationReference)confirmDeleteCtrl.getUserObject();
				doDeleteReference(appReference);
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmRemoveComparativeExpertCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event == Event.CLOSE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveComparativeExpertCtrl);
		removeAsListenerAndDispose(addComparativeExpertCtrl);
		removeAsListenerAndDispose(addRecommendationCtrl);
		removeAsListenerAndDispose(sendReferenceCtrl);
		removeAsListenerAndDispose(editReferenceCtrl);
		removeAsListenerAndDispose(addExpertCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveComparativeExpertCtrl = null;
		addComparativeExpertCtrl = null;
		addRecommendationCtrl = null;
		sendReferenceCtrl = null;
		editReferenceCtrl = null;
		addExpertCtrl = null;
		cmc = null;
	}

	private void doAddExpert(UserRequest ureq) {
		if(guardModalController(addExpertCtrl)) return;
		
		if(app.getKey() == null) {//we need a persisted application
			fireEvent(ureq, new RequestPersistEvent());
		}
		
		addExpertCtrl = new ApplicationReferenceEditController(ureq, getWindowControl(), position,
				app, null, ReferenceType.expert, secCallback);
		listenTo(addExpertCtrl);
		
		String title = translate("add.expert");
		cmc = new CloseableModalController(getWindowControl(), "c", addExpertCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddRecommendation(UserRequest ureq) {
		if(guardModalController(addRecommendationCtrl)) return;
		
		if(app.getKey() == null) {//we need a persisted application
			fireEvent(ureq, new RequestPersistEvent());
		}
		
		addRecommendationCtrl = new ApplicationReferenceEditController(ureq, getWindowControl(), position,
				app, null, ReferenceType.recommendation, secCallback);
		listenTo(addRecommendationCtrl);
		
		String title = translate("add.recommendation");
		cmc = new CloseableModalController(getWindowControl(), "c", addRecommendationCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddComparativeExpert(UserRequest ureq) {
		if(guardModalController(addComparativeExpertCtrl)) return;
		
		if(app.getKey() == null) {//we need a persisted application
			fireEvent(ureq, new RequestPersistEvent());
		}
		
		List<Application> applicationsToCompare = new ArrayList<>();
		applicationsToCompare.add(app);
		
		addComparativeExpertCtrl = new ApplicationReferenceEditController(ureq, getWindowControl(), position,
				null, applicationsToCompare, ReferenceType.comparativeAssessmentExpert, secCallback);
		listenTo(addComparativeExpertCtrl);
		
		String title = translate("add.comparative.expert");
		cmc = new CloseableModalController(getWindowControl(), "c", addComparativeExpertCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEdit(UserRequest ureq, ApplicationReference appReference) {
		if(guardModalController(editReferenceCtrl)) return;
		
		Reference reference = appReference.getReference();
		editReferenceCtrl = new ApplicationReferenceEditController(ureq, getWindowControl(), position, reference, secCallback);
		listenTo(editReferenceCtrl);
		
		String title = translate("edit.recommendation");
		cmc = new CloseableModalController(getWindowControl(), "c", editReferenceCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, ApplicationReference appReference) {
		String title;
		String text;

		Reference reference = appReference.getReference();
		String fullName = salutationGenerator.getFullname(reference, getLocale());
		String[] textArgs = new String[] { fullName, reference.getInstitution() };
		if(reference.getReferenceType() == ReferenceType.expert) {
			title = translate("reference.confirm.delete.expert.title");
			text = translate("reference.confirm.delete.expert.text", textArgs);
		} else {
			title = translate("reference.confirm.delete.recommendation.title");
			text = translate("reference.confirm.delete.recommendation.text", textArgs);
		}

		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(appReference);
	}
	
	private void doDeleteReference(ApplicationReference appReference) {
		recruitingService.deleteReference(appReference.getReference());
	}
	
	private void doConfirmRemove(UserRequest ureq, ApplicationReference appReference) {
		Reference reference = appReference.getReference();
		
		List<Application> allApplications = recruitingService.getReferenceToApplicationsList(reference);
		allApplications.remove(app);
		int applicationsLeft = allApplications.size();
		
		confirmRemoveComparativeExpertCtrl = new ConfirmRemoveReferenceToApplicationController(ureq, getWindowControl(),
				reference, app, applicationsLeft);
		listenTo(confirmRemoveComparativeExpertCtrl);
		
		String i18nTitle = applicationsLeft == 0
				? "reference.management.confirm.delete.comparative.expert.title"
				: "reference.management.confirm.remove.application.from.reference.title";
		cmc = new CloseableModalController(getWindowControl(), "c", confirmRemoveComparativeExpertCtrl.getInitialComponent(), translate(i18nTitle));
		cmc.activate();
		listenTo(cmc);
	}

	private void doSendInvitation(UserRequest ureq, ApplicationReference appReference) {
		if(guardModalController(sendReferenceCtrl)) return;
		
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		Identity secretary = recruitingService.getSecretary(position);
		Reference reference = appReference.getReference();
		List<Application> appsList = null;
		if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			appsList = recruitingService.getReferenceToApplications(reference)
					.stream().map(ReferenceToApplication::getApplication)
					.collect(Collectors.toList());
		}
		
		ApplicationMailTemplate template = ReferenceHelper.referenceTemplate(headOfCommittee, secretary, position, app, appsList, reference,
				salutationGenerator, getTranslator());

		sendReferenceCtrl = new SendInvitationEmailController(ureq, getWindowControl(), position, reference, template);
		listenTo(sendReferenceCtrl);
		cmc = new CloseableModalController(getWindowControl(), "c", sendReferenceCtrl.getInitialComponent(), translate("reference.send.title"));
		cmc.activate();
		listenTo(cmc);
	}
}
