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
package org.olat.modules.curriculum.ui;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.certificationprogram.CertificationModule;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.CertificationProgramSecurityCallback;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Nov 26, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementController extends BasicController implements Activateable2 {
	
	public static final String CONTEXT_AUTOMATION = "Automation";
	
	private final TooledStackedPanel toolbarPanel;
	private final VelocityContainer mainVC;
	private final ButtonGroupComponent segmentButtonsCmp;
	
	private final Link metadataLink;
	private final Link infosLink;
	private final Link executionLink;
	private Link automationLink;
	private final Link optionsLink;
	private final Link previewButton;
	private final Link assessmentLink;

	private EditCurriculumElementMetadataController metadataCtrl;
	private EditCurriculumElementInfosController infoCtrl;
	private EditCurriculumElementExecutionController executionCtrl;
	private EditCurriculumElementAutomationController automationCtrl;
	private EditCurriculumElementOptionsController optionsCtrl;
	private CurriculumElementInfosController previewCtrl;
	private EditCurriculumElementCertificationProgramController certificationProgramCtrl;
	
	private CurriculumElement element;
	private final CurriculumElement parentElement;
	private final Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	private final boolean administrator;
	private final MapperKey elementImageMapperKey;
	private final CurriculumElementImageMapper elementImageMapper;
	private final CertificationProgramSecurityCallback certificationSecCallback;
	
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CertificationModule certificationProgramModule;
	@Autowired
	private CertificationProgramService certificationProgramService;
	@Autowired
	private CurriculumAutomationService automationService;
	
	public EditCurriculumElementController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement element, CurriculumElement parentElement, Curriculum curriculum,
			CurriculumSecurityCallback secCallback, CertificationProgramSecurityCallback certificationSecCallback) {
		super(ureq, wControl);
		this.toolbarPanel = toolbarPanel;
		if (element != null) {
			this.element = curriculumService.getCurriculumElement(element);
		}
		this.parentElement = parentElement;
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.certificationSecCallback = certificationSecCallback;
		administrator = ureq.getUserSession().getRoles().isAdministrator();
		
		elementImageMapper = CurriculumElementImageMapper.mapper210x140();
		elementImageMapperKey = mapperService.register(null, CurriculumElementImageMapper.MAPPER_ID_210_140, elementImageMapper);

		mainVC = createVelocityContainer("curriculum_element_edit");
		putInitialPanel(mainVC);
		exposeToVC();
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		mainVC.put("segments", segmentButtonsCmp);
		metadataLink = LinkFactory.createLink("curriculum.element.metadata", getTranslator(), this);
		segmentButtonsCmp.addButton(metadataLink, true);
		infosLink = LinkFactory.createLink("curriculum.element.infos", getTranslator(), this);
		segmentButtonsCmp.addButton(infosLink, false);
		executionLink = LinkFactory.createLink("curriculum.element.execution", getTranslator(), this);
		segmentButtonsCmp.addButton(executionLink, false);
		CurriculumElementType elementType = element != null ? element.getType() : null;
		if (elementType != null && (elementType.isImplOnly() || !elementType.isAllowedAsRootElement())) {
			automationLink = LinkFactory.createLink("curriculum.element.automation", getTranslator(), this);
			segmentButtonsCmp.addButton(automationLink, false);
		}
		assessmentLink = LinkFactory.createLink("curriculum.element.assessment", getTranslator(), this);
		segmentButtonsCmp.addButton(assessmentLink, false);
		optionsLink = LinkFactory.createLink("curriculum.element.options", getTranslator(), this);
		segmentButtonsCmp.addButton(optionsLink, false);
		
		previewButton = LinkFactory.createButton("preview.info", mainVC, this);
		previewButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details");
		previewButton.setVisible(element != null && element.getParent() == null);
		
		updateUI();
		doOpenMetadata(ureq);
	}

	public CurriculumElement getElement() {
		return element;
	}
	
	private void updateUI() {
		boolean isCertificationAvailable = certificationProgramModule.isEnabled() && (element.isSingleCourseImplementation()
				|| certificationProgramService.isInCertificationProgram(element));
		assessmentLink.setVisible(isCertificationAvailable);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == metadataCtrl) {
			if (Event.DONE_EVENT == event) {
				Long previousTypeKey = element.getType() != null ? element.getType().getKey() : null;
				element = metadataCtrl.getCurriculumElement();
				Long newTypeKey = element.getType() != null ? element.getType().getKey() : null;
				if (!Objects.equals(previousTypeKey, newTypeKey) && !automationService.getConfigs(element).isEmpty()) {
					automationService.updateConfigs(element, List.of());
				}
				exposeToVC();
				updateUI();
				fireEvent(ureq, event);
			}
		} else if (source == infoCtrl) {
			if (Event.DONE_EVENT == event) {
				element = infoCtrl.getCurriculumElement();
				exposeToVC();
				fireEvent(ureq, event);
			}
		} else if (source == executionCtrl) {
			if (Event.DONE_EVENT == event) {
				element = executionCtrl.getCurriculumElement();
				exposeToVC();
				fireEvent(ureq, event);
			}
		} else if (source == automationCtrl) {
			if (Event.DONE_EVENT == event) {
				element = automationCtrl.getCurriculumElement();
				exposeToVC();
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(CONTEXT_AUTOMATION.equalsIgnoreCase(type) && automationLink != null) {
			doOpenAutomation(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == metadataLink) {
			doOpenMetadata(ureq);
		} else if (source == infosLink) {
			doOpenInfos(ureq);
		} else if (source == executionLink) {
			doOpenExecution(ureq);
		} else if (source == automationLink) {
			doOpenAutomation(ureq);
		} else if (source == optionsLink) {
			doOpenOptions(ureq);
		} else if (source == assessmentLink) {
			doOpenAssessmentSettings(ureq);
		} else if (source == previewButton) {
			doOpenPreview(ureq);
		}
	}
	
	private void exposeToVC() {
		if (element == null) {
			return;
		}
		
		String imageUrl = elementImageMapper.getThumbnailURL(elementImageMapperKey.getUrl(), element);
		if(StringHelper.containsNonWhitespace(imageUrl)) {
			mainVC.contextPut("imageUrl", imageUrl);
		}
		if (administrator) {
			mainVC.contextPut("key", element.getKey());
		}
		mainVC.contextPut("externalId", element.getExternalId());
		
		List<CurriculumAutomationConfig> automationConfig = automationService.getConfigs(element);
		if (automationConfig.isEmpty()) {
			CurriculumElementType type = element.getType();
			automationConfig = type == null ? List.of() : automationService.getConfigs(type);
		}
		boolean hasActiveAutomation = automationConfig.stream().anyMatch(CurriculumAutomationConfig::isEnabled);
		if (hasActiveAutomation) {
			Date nextExecution = automationService.getNextAutomationExecution(element, automationConfig);
			String date = nextExecution == null ? "-" : Formatter.getInstance(getLocale()).formatDate(nextExecution);
			mainVC.contextPut("automationNextExecution", translate("curriculum.element.automation.next.execution", date));
		} else {
			mainVC.contextRemove("automationNextExecution");
		}
		
		mainVC.contextPut("participants", CurriculumHelper.getParticipantRange(getTranslator(), element, true));
	}
	
	public void reload(UserRequest ureq) {
		element = curriculumService.getCurriculumElement(element);
		if (automationCtrl != null && segmentButtonsCmp.getSelectedButton() == automationLink) {
			doOpenAutomation(ureq);
		}
	}

	private void doOpenMetadata(UserRequest ureq) {
		removeAsListenerAndDispose(metadataCtrl);
		
		metadataCtrl = new EditCurriculumElementMetadataController(ureq, getWindowControl(), element, parentElement, curriculum, secCallback);
		listenTo(metadataCtrl);
		mainVC.put("content", metadataCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(metadataLink);
	}

	private void doOpenInfos(UserRequest ureq) {
		removeAsListenerAndDispose(infoCtrl);
		
		infoCtrl = new EditCurriculumElementInfosController(ureq, getWindowControl(), element, secCallback);
		listenTo(infoCtrl);
		mainVC.put("content", infoCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(infosLink);
	}

	private void doOpenExecution(UserRequest ureq) {
		removeAsListenerAndDispose(executionCtrl);
		
		executionCtrl = new EditCurriculumElementExecutionController(ureq, getWindowControl(), element, secCallback);
		listenTo(executionCtrl);
		mainVC.put("content", executionCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(executionLink);
	}
	
	private void doOpenAutomation(UserRequest ureq) {
		removeAsListenerAndDispose(automationCtrl);

		automationCtrl = new EditCurriculumElementAutomationController(ureq, getWindowControl(), element);
		listenTo(automationCtrl);
		mainVC.put("content", automationCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(automationLink);
	}

	private void doOpenAssessmentSettings(UserRequest ureq) {
		removeAsListenerAndDispose(certificationProgramCtrl);
		
		certificationProgramCtrl = new EditCurriculumElementCertificationProgramController(ureq, getWindowControl(), curriculum, element,
				secCallback, certificationSecCallback);
		listenTo(certificationProgramCtrl);
		mainVC.put("content", certificationProgramCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(assessmentLink);
	}

	private void doOpenOptions(UserRequest ureq) {
		removeAsListenerAndDispose(optionsCtrl);
		
		optionsCtrl = new EditCurriculumElementOptionsController(ureq, getWindowControl(), element, secCallback);
		listenTo(optionsCtrl);
		mainVC.put("content", optionsCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(optionsLink);
	}

	private void doOpenPreview(UserRequest ureq) {
		PreviewCurriculumElementHeaderConfig headerConfig = new PreviewCurriculumElementHeaderConfig(element);
		previewCtrl = new CurriculumElementInfosController(ureq, getWindowControl(), element, null, headerConfig);
		listenTo(previewCtrl);
		toolbarPanel.pushController(translate("preview.info"), previewCtrl);
	}

}
