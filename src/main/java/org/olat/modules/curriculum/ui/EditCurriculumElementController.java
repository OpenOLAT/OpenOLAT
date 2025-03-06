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

import java.util.List;
import java.util.Set;

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
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Nov 26, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementController extends BasicController {
	
	private final TooledStackedPanel toolbarPanel;
	private final VelocityContainer mainVC;
	private final ButtonGroupComponent segmentButtonsCmp;
	
	private Link automationLink;
	private final Link metadataLink;
	private final Link infosLink;
	private final Link executionLink;
	private final Link optionsLink;
	private final Link previewButton;
	
	private EditCurriculumElementMetadataController metadataCtrl;
	private EditCurriculumElementInfosController infoCtrl;
	private EditCurriculumElementExecutionController executionCtrl;
	private EditCurriculumElementAutomationController automationCtrl;
	private EditCurriculumElementOptionsController optionsCtrl;
	private CurriculumElementInfosController previewCtrl;
	
	private CurriculumElement element;
	private final CurriculumElement parentElement;
	private final Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	private final boolean administrator;
	private final CurriculumElementImageMapper elementImageMapper;
	private final String mapperUrl;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public EditCurriculumElementController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement element, CurriculumElement parentElement, Curriculum curriculum,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.toolbarPanel = toolbarPanel;
		if (element != null) {
			this.element = curriculumService.getCurriculumElement(element);
		}
		this.parentElement = parentElement;
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		administrator = ureq.getUserSession().getRoles().isAdministrator();
		
		elementImageMapper = new CurriculumElementImageMapper(curriculumService);
		mapperUrl = registerCacheableMapper(ureq, CurriculumElementImageMapper.DEFAULT_ID, elementImageMapper,
				CurriculumElementImageMapper.DEFAULT_EXPIRATION_TIME);
		
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
		
		if(element.getParent() == null) {// Only implementations
			automationLink = LinkFactory.createLink("curriculum.element.automation", getTranslator(), this);
			segmentButtonsCmp.addButton(automationLink, false);
		}
		optionsLink = LinkFactory.createLink("curriculum.element.options", getTranslator(), this);
		segmentButtonsCmp.addButton(optionsLink, false);
		
		previewButton = LinkFactory.createButton("preview.info", mainVC, this);
		previewButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details");
		previewButton.setVisible(element != null && element.getParent() == null);
		
		doOpenMetadata(ureq);
	}

	public CurriculumElement getElement() {
		return element;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == metadataCtrl) {
			if (Event.DONE_EVENT == event) {
				element = metadataCtrl.getCurriculumElement();
				exposeToVC();
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
		} else if (source == previewButton) {
			doOpenPreview(ureq);
		}
	}
	
	private void exposeToVC() {
		if (element == null) {
			return;
		}
		
		mainVC.contextPut("imageUrl", elementImageMapper.getImageUrl(mapperUrl, element, CurriculumElementFileType.teaserImage));
		if (administrator) {
			mainVC.contextPut("key", element.getKey());
		}
		mainVC.contextPut("externalId", element.getExternalId());
		if (element.getEducationalType() != null) {
			mainVC.contextPut("educationalTypeCss", element.getEducationalType().getCssClass());
			mainVC.contextPut("educationalTypeI18nKey", RepositoyUIFactory.getI18nKey(element.getEducationalType()));
		} else {
			mainVC.contextRemove("educationalTypeCss");
			mainVC.contextRemove("educationalTypeI18nKey");
		}
		
		Set<CurriculumElementToTaxonomyLevel> ce2taxonomyLevels = element.getTaxonomyLevels();
		if (ce2taxonomyLevels != null && !ce2taxonomyLevels.isEmpty()) {
			List<TaxonomyLevel> levels = ce2taxonomyLevels.stream()
				.map(CurriculumElementToTaxonomyLevel::getTaxonomyLevel)
				.limit(3)
				.toList();
			List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), levels);
			mainVC.contextPut("taxonomyLevels", taxonomyLevels);
			mainVC.contextPut("taxonomyLevelsEllipsis", ce2taxonomyLevels.size() > 3);
		} else {
			mainVC.contextRemove("taxonomyLevels");
		}
		
		mainVC.contextPut("participants", CurriculumHelper.getParticipantRange(getTranslator(), element, true));
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

	private void doOpenOptions(UserRequest ureq) {
		removeAsListenerAndDispose(optionsCtrl);
		
		optionsCtrl = new EditCurriculumElementOptionsController(ureq, getWindowControl(), element, secCallback);
		listenTo(optionsCtrl);
		mainVC.put("content", optionsCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(optionsLink);
	}

	private void doOpenPreview(UserRequest ureq) {
		previewCtrl = new CurriculumElementInfosController(ureq, getWindowControl(), element, getIdentity(), true);
		listenTo(previewCtrl);
		toolbarPanel.pushController(translate("preview.info"), previewCtrl);
	}

}
