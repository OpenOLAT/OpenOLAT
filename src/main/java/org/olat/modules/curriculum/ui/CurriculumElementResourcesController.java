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
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.modules.curriculum.Automation;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.event.CurriculumElementEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementResourcesController extends BasicController {
	
	private static final String CMD_OPEN_ELEMENT = "oelement";
	
	private final VelocityContainer mainVC;
	
	private CurriculumElement curriculumElement;
	private final CurriculumElement implementationElement;
	
	private CurriculumElementTemplateListController templatesCtrl;
	private final CurriculumElementResourceListController resourcesCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumAutomationService automationService;
	
	public CurriculumElementResourcesController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.curriculumElement = curriculumElement;
		this.implementationElement = curriculumService.getImplementationOf(curriculumElement);

		mainVC = createVelocityContainer("resources");
		
		resourcesCtrl = new CurriculumElementResourceListController(ureq, wControl, curriculumElement, secCallback);
		listenTo(resourcesCtrl);
		mainVC.put("resources", resourcesCtrl.getInitialComponent());
		
		CurriculumElementType type = curriculumElement.getType();
		if(type != null && type.getMaxRepositoryEntryRelations() == 1) {
			templatesCtrl = new CurriculumElementTemplateListController(ureq, wControl, curriculumElement, secCallback);
			listenTo(templatesCtrl);
			mainVC.put("templates", templatesCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}
	
	public void loadModel() {
		int linkedCourses = resourcesCtrl.loadModel();
		int linkedTemplates = 0;
		if(templatesCtrl != null) {
			linkedTemplates = templatesCtrl.loadModel();
		}
		
		resourcesCtrl.updateAddButtonAndEmptyMessages(linkedTemplates);
		if(templatesCtrl != null) {
			templatesCtrl.updateAddButtonAndEmptyMessages(linkedCourses);
		}
		updateAutomationInformations();
	}
	
	private void updateAutomationInformations() {
		if(implementationElement == null || !implementationElement.hasAutomation()) return;
		
		CurriculumElement beginElement = automationService.getBeginCurriculumElement(curriculumElement);
		CurriculumElement endElement = automationService.getEndCurriculumElement(curriculumElement);
		if(beginElement == null && endElement == null) return;

		List<Infos> statusAutomation = new ArrayList<>(4);
		if(beginElement != null) {
			Date beginDate = beginElement.getBeginDate();
			Infos instantiation = formatAutomationInformations(beginDate, beginElement,
					"automation.infos.label.course.templates", implementationElement.getAutoInstantiation(), true);
			updateAutomationInformations("instantiation", instantiation, new ArrayList<>());
			
			Infos accessforCoach = formatAutomationInformations(beginDate, beginElement,
					"automation.access.for.coach.enabled", implementationElement.getAutoAccessForCoach(), true);
			updateAutomationInformations("accessforCoach",  accessforCoach, statusAutomation);
			Infos published = formatAutomationInformations(beginDate, beginElement,
					"automation.published.enabled", implementationElement.getAutoPublished(), true);
			updateAutomationInformations("published", published, statusAutomation);
		}
		
		if(endElement != null) {
			Date endDate = endElement.getBeginDate();
			Infos closed = formatAutomationInformations(endDate, endElement,
					"automation.finished.enabled", implementationElement.getAutoClosed(), false);
			updateAutomationInformations("finished", closed, statusAutomation);
		}
		
		mainVC.contextPut("separatorLabel", Boolean.valueOf(statusAutomation.size() > 1));
	}

	private void updateAutomationInformations(String key, Infos infos, List<Infos> list) {
		if(infos != null) {
			mainVC.contextPut(key, infos);
			list.add(infos);
		}
	}
	
	private Infos formatAutomationInformations(Date date, CurriculumElement dateElement, String status, Automation automation, boolean before) {
		if(automation == null || automation.getUnit() == null) return null;
		
		String timeText = "";
		Date effectiveDate = date;
		AutomationUnit unit = automation.getUnit();
		if(unit == AutomationUnit.SAME_DAY) {
			timeText = translate("automation.infos.same.day");
		} else if(automation.getValue() != null) {
			int val = automation.getValue().intValue();
			timeText = translate("automation.infos." + unit.name().toLowerCase() + "." + (val > 1 ? "plural" : "singular"),
					Integer.toString(val));
			effectiveDate = before ? automation.getDateBefore(date) : automation.getDateAfter(date);
		}
		
		boolean sameElement = curriculumElement.equals(dateElement);

		String i18n;
		if(sameElement) {
			i18n = before ? "automation.infos.before" : "automation.infos.after";
		} else {
			i18n = before ? "automation.infos.before.link" : "automation.infos.after.link";
		}

		String text = translate(i18n, timeText);
		String dateFormatted = Formatter.getInstance(getLocale()).formatDateWithDay(effectiveDate);
		String elementTitle = buildLinkTitle(dateElement);
		
		Link link = LinkFactory.createLink("link." + status, elementTitle, CMD_OPEN_ELEMENT, elementTitle, getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
		link.setVisible(!sameElement);
		link.setUserObject(dateElement);
		return new Infos(translate(status), dateFormatted, text, link);
	}
	
	private String buildLinkTitle(CurriculumElement dateElement) {
		StringBuilder sb = new StringBuilder();
		if(dateElement.getType() != null) {
			sb.append(dateElement.getType().getDisplayName());
		}
		sb.append(" \"")
		  .append(dateElement.getDisplayName())
		  .append("\"");
		return sb.toString();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link && CMD_OPEN_ELEMENT.equals(link.getCommand())
				&& link.getUserObject() instanceof CurriculumElement element) {
			fireEvent(ureq, new CurriculumElementEvent(element, List.of()));
		}
	}
	
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(resourcesCtrl == source || templatesCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				loadModel();
			}
		}
	}
	
	public record Infos(String title, String date, String text, Link link) {
		//
	}
}
