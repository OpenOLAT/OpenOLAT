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
package org.olat.modules.selectus.ui.app_wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMLHelper;
import org.olat.modules.selectus.model.PositionProfessorship;
import org.olat.modules.selectus.model.ReferenceSendMailType;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  29 aug. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InstructionsController extends FormBasicController {

	private boolean onlyPdfs;
	private final boolean withTitle;
	private Position position;
	private TabConfiguration configuration;
	private OrganisationUnit organisationSettings;
	private Map<DocumentEnum,List<DocumentType>> docTypes;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;

	public InstructionsController(UserRequest ureq, WindowControl wControl, Form rootForm, Position position, TabConfiguration configuration, boolean withTitle) {
		super(ureq, wControl, "instructions", Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		
		this.withTitle = withTitle;
		// allowed document types
		docTypes = position.getDocumentTypes();
		onlyPdfs = DocumentType.isOnlyPDFs(docTypes);
		this.configuration = configuration;
		this.position = position;
		organisationSettings = recruitingService.getOrganisationUnit(position);
		initForm(ureq);
	}
	
	public void updatePosition(Position updatedPosition, TabConfiguration configuration) {
		docTypes = updatedPosition.getDocumentTypes();
		onlyPdfs = DocumentType.isOnlyPDFs(docTypes);
		this.position = updatedPosition;
		this.configuration = configuration;
		initInstructionsForm(flc);
		flc.setDirty(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(withTitle) {
			setFormTitle("wizard.instructions.legend", new String[] { StringHelper.escapeHtml(position.getMLTitle(getLocale())) });
		}
		initInstructionsForm(formLayout);
	}
	
	private void initInstructionsForm(FormItemContainer formLayout) {
		String instructions = configuration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(instructions)) {
			formLayout.contextPut("instructionsText", StringHelper.xssScan(RecruitingHelper.escWithBR(instructions)));
		} else {
			if(formLayout instanceof FormLayoutContainer) {
				((FormLayoutContainer)formLayout).contextRemove("instructionsText");
			}
			initStandardForm(formLayout);
		}
	}
	
	private void initStandardForm(FormItemContainer formLayout) {
		if(withTitle) {
			setFormTitle("wizard.instructions.legend", new String[] { StringHelper.escapeHtml(position.getMLTitle(getLocale())) });
		}
		Set<String> available = position.getAvailableDocuments();
		Set<String> mandatory = position.getMandatoryDocuments();
		Set<String> staffOnly = position.getStaffDocuments();
		
		List<String> mandatoryDocuments = new ArrayList<>();
		List<String> optionalDocuments = new ArrayList<>();

		for(DocumentOption docOption: recruitingModule.getDocumentOptions(position)) {
			DocumentEnum doc = docOption.getDoc();
			if(!available.contains(doc.name()) || staffOnly.contains(doc.name())) continue;
			
			String i18nKey = doc.i18nKey();
			if(DocumentEnum.other.equals(doc)) {
				i18nKey += ".long";
			}
			
			String documentName = position.getDocumentName(doc, getLocale());
			if(!StringHelper.containsNonWhitespace(documentName)) {
				 documentName = translate(i18nKey);
			}
			
			if(docOption.getMaxSize() > 0) {
				String maxUpload = translate("document.maxsize", new String[]{ Integer.toString(docOption.getMaxSize()) } );
				if(maxUpload.length() > 0 && maxUpload.length() < 1024) {
					documentName += " " + maxUpload;
				}
				
				if(!onlyPdfs) {
					documentName += " " + translate("document.types", new String[]{ DocumentType.toFlatString(docTypes.get(doc)) });
				}
				
				String explanation = position.getDocumentExplain(doc, getLocale());
				if(!StringHelper.containsNonWhitespace(explanation)) {
					explanation = translate(i18nKey + ".explain");
				}	
				if(explanation.length() > 0 && !explanation.equals(i18nKey + ".explain") && explanation.length() < 1024) {
					documentName += "<div style='padding-left: 1.28571em;'><span class='text-muted'>" + explanation + "</div>";
				}
			}

			if(mandatory.contains(doc.name())) {
				mandatoryDocuments.add(documentName);
			} else if (available.contains(doc.name())) {
				optionalDocuments.add(documentName);
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer container = (FormLayoutContainer)formLayout;
			container.contextPut("mandatoryDocuments", mandatoryDocuments);
			container.contextPut("optionalDocuments", optionalDocuments);
			container.contextPut("position", position);
			container.contextPut("positionTitle", StringHelper.escapeHtml(PositionMLHelper.getPositionMLTitle(position, getLocale())));
			
			String[] i18nArguments = new String[] {
				recruitingModule.getStaffMail(position, organisationSettings)	// 0
			};
			container.contextPut("i18nArguments", i18nArguments);
			
			if(recruitingModule.isProfessorshipTypeEnabled()) {
				String professorship = position.getProfessorship();
				if(StringHelper.containsNonWhitespace(professorship)) {
					container.contextPut("professorship", professorship);
				} else {
					container.contextPut("professorship", PositionProfessorship.any.name());
				}
			} else {
				container.contextPut("professorship", "disabled");
			}
			
			long min = position.getMinRefereesAsLong();
			long max = position.getMaxRefereesAsLong();
			if(position.isRefereeRecommendationEnabled() && recruitingModule.isReferenceEnabled() && max > 0) {
				if(min > max) {
					min = max;
				}

				String refereeInstructionI18nKey;
				if(position.getRefereeRecommandationSendMailType() == ReferenceSendMailType.auto) {
					if(min == 0 && max > 0) {
						refereeInstructionI18nKey = "referee.instruction.min.max.auto.optional";	
					} else if(min >= 1 && min == max) {
						refereeInstructionI18nKey = "referee.instruction.min.max.auto.equals";
					} else { // min >= 1 && max > 1
						refereeInstructionI18nKey = "referee.instruction.min.max.auto";
					}
				} else {
					if(min == 0 && max > 0) {
						refereeInstructionI18nKey = "referee.instruction.min.max.staff.optional";	
					} else if(min >= 1 && min == max) {
						refereeInstructionI18nKey = "referee.instruction.min.max.staff.equals";
					} else { // min >= 1 && max > 1
						refereeInstructionI18nKey = "referee.instruction.min.max.staff";
					}
				}

				String refereesMinMax = translate(refereeInstructionI18nKey, new String[]{ Long.toString(min), Long.toString(max) });
				container.contextPut("refereesMinMax", refereesMinMax);
				container.contextPut("referees", Boolean.TRUE);
			} else {
				container.contextPut("referees", Boolean.FALSE);
			}
			
			if(recruitingModule.isReferenceConsentEnabled() && position.isExpertRecommendationEnabled()) {
				String expertConsentInstructions = translate("expert.consent.instruction");
				container.contextPut("expertConsent", expertConsentInstructions);
			}
		}
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
