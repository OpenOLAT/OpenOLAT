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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateItem;
import org.olat.core.gui.components.emptystate.EmptyStatePrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.IconPanelItem;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent.LabelText;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.CertificationHelper;
import org.olat.modules.certificationprogram.ui.CertificationProgramSecurityCallback;
import org.olat.modules.certificationprogram.ui.EditCertificationProgramConfigurationController;
import org.olat.modules.certificationprogram.ui.component.DurationCellRenderer;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementCertificationProgramController extends FormBasicController {
	
	private FormLink openButton;
	private FormLink removeButton;
	private FormToggle enableEl;
	private EmptyStateItem emptyStatePanel;
	private IconPanelItem certificationProgramPanel;
	private IconPanelLabelTextContent certificationProgramContent;
	
	private final boolean canEdit;
	private Organisation organisation;
	private final CurriculumElement element;
	private CertificationProgram certificationProgram;
	private final boolean canViewCertificationPrograms;
	private final CertificationProgramSecurityCallback certificationSecCallback;

	private CloseableModalController cmc;
	private ConfirmationController removeConfirmationCtrl;
	private SelectCertificationProgramController selectProgramCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public EditCurriculumElementCertificationProgramController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, CurriculumElement element, CurriculumSecurityCallback secCallback,
			CertificationProgramSecurityCallback certificationSecCallback) {
		super(ureq, wControl, "edit_certification_program",
				Util.createPackageTranslator(EditCertificationProgramConfigurationController.class, ureq.getLocale()));
		this.element = element;
		this.certificationSecCallback = certificationSecCallback;
		canEdit = secCallback.canEditCurriculumElementSettings(element);
		canViewCertificationPrograms = certificationSecCallback.canViewCertificationPrograms();
		
		organisation = curriculum.getOrganisation();
		if(organisation == null || !organisationModule.isEnabled()) {
			organisation = organisationService.getDefaultOrganisation();
		}
		certificationProgram = certificationProgramService.getCertificationProgram(element);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		enableEl = uifactory.addToggleButton("certification.program.enable", "curriculum.element.certification.program", translate("on"), translate("off"), formLayout);
		enableEl.toggle(certificationProgram != null);
		enableEl.setEnabled(canEdit);
		
		certificationProgramPanel = uifactory.addIconPanel("certification.program", null, formLayout);
		certificationProgramPanel.setElementCssClass("o_block_bottom o_sel_ac_offer");
		certificationProgramPanel.setIconCssClass("o_icon o_icon-fw o_icon_certificate");
		
		certificationProgramContent = new IconPanelLabelTextContent("cpcontent");
		certificationProgramPanel.setContent(certificationProgramContent);
		
		removeButton = uifactory.addFormLink("remove", "remove", "remove", null, formLayout, Link.BUTTON);
		removeButton.setDomReplacementWrapperRequired(false);
		removeButton.setGhost(true);
		removeButton.setIconLeftCSS("o_icon o_icon-fw o_icon_invalidate");
		certificationProgramPanel.addLink(removeButton);
		
		openButton = uifactory.addFormLink("open", "open", "open", null, formLayout, Link.BUTTON);
		openButton.setGhost(true);
		openButton.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
		openButton.setNewWindow(true, true, false);
		certificationProgramPanel.addLink(openButton);
		
		emptyStatePanel = uifactory.addEmptyState("no.certification.program", null, formLayout);
		emptyStatePanel.getFormItemComponent().setIconCss("o_icon o_icon_certificate");
		emptyStatePanel.getFormItemComponent().setMessageI18nKey("no.certification.program.hint");
		if(canEdit) {
			emptyStatePanel.getFormItemComponent().setPrimaryButton("o_icon o_icon_search", "select", null);
		}
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isOn();
		boolean withCertificationProgram = enabled && certificationProgram != null;
		certificationProgramPanel.setVisible(withCertificationProgram);
		if(certificationProgram != null) {
			certificationProgramPanel.setTitle(certificationProgram.getDisplayName());
			if(StringHelper.containsNonWhitespace(certificationProgram.getIdentifier())) {
				certificationProgramPanel.setTagline(certificationProgram.getIdentifier());
			}
			
			List<LabelText> labelTexts = new ArrayList<>(4);
			if(certificationProgram.getValidityTimelapseDuration() != null) {
				labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("validity.duration"),
						DurationCellRenderer.toString(certificationProgram.getValidityTimelapseDuration(), getTranslator())));
			}
			if(certificationProgram.isRecertificationEnabled()) {
				String mode = translate("recertification.mode." + certificationProgram.getRecertificationMode());
				labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("recertification.mode"), mode));
			}
			
			CreditPointSystem creditPointSystem = certificationProgram.getCreditPointSystem();
			if(creditPointSystem != null && certificationProgram.getCreditPoints() != null) {
				String pointsRequirement = CertificationHelper.creditPointsToString(certificationProgram.getCreditPoints(), creditPointSystem);
				labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("credit.point.need"),
						pointsRequirement));
			}
			
			certificationProgramContent.setLabelTexts(labelTexts);
		}
		removeButton.setVisible(withCertificationProgram && canEdit);
		
		boolean canOpenCertificationProgram = withCertificationProgram && canViewCertificationPrograms
				&& certificationSecCallback.canViewCertificationPrograms(certificationProgram);
		openButton.setVisible(canOpenCertificationProgram);
		openButton.setUrl(getCertificationProgramURL());
		
		boolean withoutCertificationProgram = enabled && certificationProgram == null;
		emptyStatePanel.setVisible(withoutCertificationProgram);
	}
	
	private String getCertificationProgramURL() {
		if(certificationProgram == null) return null;
		
		String businessPath = "[CurriculumAdmin:0][Certification:0][CertificationProgram:" + certificationProgram.getKey() + "][Overview:0]";
		List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		return BusinessControlFactory.getInstance().getAsAuthURIString(ces, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(removeConfirmationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doRemoveCertificationProgram(ureq);
				updateUI();
			} else if(event == Event.CANCELLED_EVENT) {
				enableEl.toggle(certificationProgram != null);
			}
			cmc.deactivate();
			cleanUp();
		} else if(selectProgramCtrl == source) {
			if(event == Event.DONE_EVENT) {
				certificationProgram = certificationProgramService.getCertificationProgram(element);
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(removeConfirmationCtrl);
		removeAsListenerAndDispose(selectProgramCtrl);
		removeAsListenerAndDispose(cmc);
		removeConfirmationCtrl = null;
		selectProgramCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			if(enableEl.isOn()) {
				updateUI();
			} else {
				doConfirmRemoveCertificationProgram(ureq);
			}
		} else if(removeButton == source) {
			doConfirmRemoveCertificationProgram(ureq);
		} else if(openButton == source) {
			doOpenCertificationProgram();
		} else if(emptyStatePanel == source) {
			if(event instanceof EmptyStatePrimaryActionEvent) {
				doSelectCertificationProgram(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectCertificationProgram(UserRequest ureq) {
		selectProgramCtrl = new SelectCertificationProgramController(ureq, getWindowControl(), element, organisation);
		listenTo(selectProgramCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectProgramCtrl.getInitialComponent(),
				true, translate("select.certification.program"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmRemoveCertificationProgram(UserRequest ureq) {
		if(certificationProgram == null) {
			updateUI();
			return;
		}
		
		String messageKey =  "confirmation.remove.certification.program";
		removeConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate(messageKey, StringHelper.escapeHtml(certificationProgram.getDisplayName())), null, translate("remove"),
				ButtonType.danger);
		listenTo(removeConfirmationCtrl);

		String titleKey =  "confirmation.remove.certification.program.title";
		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeConfirmationCtrl.getInitialComponent(),
				true, translate(titleKey), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRemoveCertificationProgram(UserRequest ureq) {
		certificationProgram = null;
		enableEl.toggle(false);
		certificationProgramService.removeCurriculumElementToCertificationProgram(element, getIdentity());
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doOpenCertificationProgram() {
		String url = getCertificationProgramURL();
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}
}
