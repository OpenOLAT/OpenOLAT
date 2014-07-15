/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.course.nodes.portfolio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsBackController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPSecurityCallbackImpl;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Description:<br>
 * Assessment details controller.
 * 
 * <P>
 * Initial Date:  11 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioResultDetailsController extends FormBasicController {
	private final EPFrontendManager ePFMgr;
	private Identity assessedIdentity;
	private PortfolioStructureMap template;
	private List<PortfolioStructureMap> maps;
	private Map<PortfolioStructureMap, MapElements> mapToElements = new HashMap<PortfolioStructureMap, MapElements>();
	
	private DeadlineController deadlineCtr;
	private CloseableCalloutWindowController deadlineCalloutCtr;
	private final BreadcrumbPanel stackPanel;
	
	public PortfolioResultDetailsController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, PortfolioCourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		this.stackPanel = stackPanel;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		
		RepositoryEntry mapEntry = courseNode.getReferencedRepositoryEntry();
		if(mapEntry != null) {
			template = (PortfolioStructureMap)ePFMgr.loadPortfolioStructure(mapEntry.getOlatResource());
			Long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			OLATResourceable courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId);
			maps = ePFMgr.loadPortfolioStructureMaps(assessedIdentity, courseOres, courseNode.getIdent(), null);
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(maps == null || maps.isEmpty()) {
			uifactory.addStaticTextElement("no.map", "", formLayout);
		} else {
			Formatter formatter = Formatter.getInstance(getLocale());
			
			int count = 0;
			for(PortfolioStructureMap map:maps) {
				MapElements mapElements = new MapElements();
				if(map instanceof EPStructuredMap) {
					EPStructuredMap structuredMap = (EPStructuredMap)map;
					
					if(maps.size() > 1 || !structuredMap.getStructuredMapSource().equals(template)) {
						String templateTitle = structuredMap.getStructuredMapSource().getTitle();
						uifactory.addStaticTextElement("map.template." + count, "map.template", templateTitle, formLayout);
					}
					
					String copyDate = "";
					if(structuredMap.getCopyDate() != null) {
						copyDate = formatter.formatDateAndTime(structuredMap.getCopyDate());
					}
					uifactory.addStaticTextElement("map.copyDate." + count, "map.copyDate", copyDate, formLayout);
					
					String returnDate = "";
					if(structuredMap.getReturnDate() != null) {
						returnDate = formatter.formatDateAndTime(structuredMap.getReturnDate());
					}
					uifactory.addStaticTextElement("map.returnDate." + count, "map.returnDate", returnDate, formLayout);
					
					String deadLine = "";
					if(structuredMap.getDeadLine() != null) {
						deadLine = formatter.formatDateAndTime(structuredMap.getDeadLine());
					}
					mapElements.deadlineEl = uifactory.addStaticTextElement("map.deadline." + count, "map.deadline", deadLine, formLayout);
				}
				
				FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons." + count, getTranslator());
				buttonsCont.setRootForm(mainForm);
				formLayout.add(buttonsCont);
				if(map instanceof EPStructuredMap) {
					mapElements.changeDeadlineLink = uifactory.addFormLink("map.deadline.change." + count, "map.deadline.change", null, buttonsCont, Link.BUTTON);
					mapElements.changeDeadlineLink.setUserObject(map);
				}
				mapElements.openMapLink = uifactory.addFormLink("open.map." + count, "open.map", null, buttonsCont, Link.BUTTON);
				mapElements.openMapLink.setUserObject(map);
				
				count++;
				if(count != maps.size()) {
					uifactory.addSpacerElement("spacer-" + count, formLayout, false);
				}
				
				mapToElements.put(map, mapElements);
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link.getName().startsWith("map.deadline.change")) {
				if (deadlineCalloutCtr == null) {
					EPStructuredMap map = (EPStructuredMap)link.getUserObject();
					popupDeadlineBox(ureq, map);
				} else {
					// close on second click
					closeDeadlineBox();
				}
			} else if(link.getName().startsWith("open.map")) {
				PortfolioStructureMap map = (PortfolioStructureMap)link.getUserObject();
				doOpenMap(ureq, map);
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenMap(UserRequest ureq, PortfolioStructureMap map) {
		EPSecurityCallback secCallback = new EPSecurityCallbackImpl(false, true);
		Controller viewCtr = EPUIFactory.createPortfolioStructureMapController(ureq, getWindowControl(), map, secCallback);
		listenTo(viewCtr);
		if(stackPanel == null) {
			LayoutMain3ColsBackController ctr = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, viewCtr.getInitialComponent(), "portfolio" + map.getKey());
			ctr.activate();
		} else {
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(ureq, getWindowControl(), viewCtr);
			stackPanel.pushController(translate("preview.map"), ctr);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == deadlineCalloutCtr && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
			removeAsListenerAndDispose(deadlineCalloutCtr);
			deadlineCalloutCtr = null;
		} else if (source == deadlineCtr) {
			String deadLine = "";
			EPStructuredMap structuredMap = deadlineCtr.getMap();
			if(structuredMap.getDeadLine() != null) {
				Formatter formatter = Formatter.getInstance(getLocale());
				deadLine = formatter.formatDateAndTime(structuredMap.getDeadLine());
			}
			mapToElements.get(structuredMap).deadlineEl.setValue(deadLine);
			closeDeadlineBox();
		}
	}
	
	/**
	 * @param ureq
	 */
	private void popupDeadlineBox(UserRequest ureq, EPStructuredMap map) {
		String title = translate("map.deadline.change");
		
		removeAsListenerAndDispose(deadlineCtr);
		deadlineCtr = new DeadlineController(ureq, getWindowControl(), map);
		listenTo(deadlineCtr);

		removeAsListenerAndDispose(deadlineCalloutCtr);
		FormLink changeDeadlineLink = mapToElements.get(map).changeDeadlineLink;
		deadlineCalloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), deadlineCtr.getInitialComponent(),
				changeDeadlineLink, title, true, "o_ep_deadline_callout");
		listenTo(deadlineCalloutCtr);
		deadlineCalloutCtr.activate();
	}

	private void closeDeadlineBox() {
		if (deadlineCalloutCtr != null){
			deadlineCalloutCtr.deactivate();
			removeAsListenerAndDispose(deadlineCalloutCtr);
			deadlineCalloutCtr = null;
		}
	}
	
	private static class MapElements {
		private FormLink openMapLink;
		private FormLink changeDeadlineLink;
		private StaticTextElement deadlineEl;
	}
}