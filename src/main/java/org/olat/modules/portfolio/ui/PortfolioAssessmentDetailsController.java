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

package org.olat.modules.portfolio.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.portfolio.DeadlineController;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioAssessmentDetailsController extends FormBasicController {
	
	private Binder templateBinder;
	private List<Binder> binders;
	private Map<Binder, MapElements> binderToElements = new HashMap<Binder, MapElements>();
	
	private DeadlineController deadlineCtr;
	private CloseableCalloutWindowController deadlineCalloutCtr;
	private final BreadcrumbPanel stackPanel;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PortfolioAssessmentDetailsController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, RepositoryEntry templateEntry,
			Identity assessedIdentity) {
		super(ureq, wControl);

		this.stackPanel = stackPanel;
		
		if(templateEntry != null && BinderTemplateResource.TYPE_NAME.equals(templateEntry.getOlatResource().getResourceableTypeName())) {
			templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
			binders = portfolioService.getBinders(assessedIdentity, templateEntry, null);
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(binders == null || binders.isEmpty()) {
			setFormWarning("no.map");
		} else if(binders != null && binders.size() > 0) {
			initBindersForm(formLayout);
		}
	}
	
	protected void initBindersForm(FormItemContainer formLayout) {
		Formatter formatter = Formatter.getInstance(getLocale());
		
		int count = 0;
		for(Binder binder:binders) {
			MapElements mapElements = new MapElements();
	
			if(binders.size() > 1 || !binder.getTemplate().equals(templateBinder)) {
				String templateTitle = binder.getTemplate().getTitle();
				uifactory.addStaticTextElement("map.template." + count, "map.template", templateTitle, formLayout);
			}
			
			String copyDate = "";
			if(binder.getCopyDate() != null) {
				copyDate = formatter.formatDateAndTime(binder.getCopyDate());
			}
			uifactory.addStaticTextElement("map.copyDate." + count, "map.copyDate", copyDate, formLayout);
			
			String returnDate = "";
			if(binder.getReturnDate() != null) {
				returnDate = formatter.formatDateAndTime(binder.getReturnDate());
			}
			uifactory.addStaticTextElement("map.returnDate." + count, "map.returnDate", returnDate, formLayout);
			
			String deadLine = "";
			if(binder.getDeadLine() != null) {
				deadLine = formatter.formatDateAndTime(binder.getDeadLine());
			}
			mapElements.deadlineEl = uifactory.addStaticTextElement("map.deadline." + count, "map.deadline", deadLine, formLayout);

			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons." + count, getTranslator());
			buttonsCont.setRootForm(mainForm);
			formLayout.add(buttonsCont);

			mapElements.changeDeadlineLink = uifactory
					.addFormLink("map.binder.change." + count, "map.binder.change", "map.deadline.change", null, buttonsCont, Link.BUTTON);
			mapElements.changeDeadlineLink.setUserObject(binder);

			mapElements.openMapLink = uifactory.addFormLink("open.binder." + count, "open.binder", "open.map", null, buttonsCont, Link.BUTTON);
			mapElements.openMapLink.setUserObject(binder);
			
			count++;
			if(count != binders.size()) {
				uifactory.addSpacerElement("spacer-" + count, formLayout, false);
			}
			
			binderToElements.put(binder, mapElements);
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
			String cmd = link.getCmd();
			if("map.binder.change".equals(cmd)) {
				if (deadlineCalloutCtr == null) {
					Binder map = (Binder)link.getUserObject();
					popupDeadlineBox(ureq, map);
				} else {
					// close on second click
					closeDeadlineBox();
				}
			} else if(link.getName().startsWith("open.binder")) {
				Binder map = (Binder)link.getUserObject();
				doOpenMap(ureq, map);
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenMap(UserRequest ureq, Binder binder) {
		if(stackPanel instanceof TooledStackedPanel) {
			List<AccessRights> rights = portfolioService.getAccessRights(binder, getIdentity());
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForCoach(rights);
			BinderConfiguration config = BinderConfiguration.createConfig(binder);
			BinderController binderCtrl = new BinderController(ureq, getWindowControl(), (TooledStackedPanel)stackPanel, secCallback, binder, config);
			String displayName = StringHelper.escapeHtml(binder.getTitle());
			stackPanel.pushController(displayName, binderCtrl);
			binderCtrl.activate(ureq, null, null);
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
			if(deadlineCtr.getBinder() != null) {
				Binder binder = deadlineCtr.getBinder();
				if(binder.getDeadLine() != null) {
					Formatter formatter = Formatter.getInstance(getLocale());
					deadLine = formatter.formatDateAndTime(binder.getDeadLine());
				}
				binderToElements.get(binder).deadlineEl.setValue(deadLine);
			}
			closeDeadlineBox();
		}
	}

	private void popupDeadlineBox(UserRequest ureq, Binder binder) {
		String title = translate("map.deadline.change");
		
		removeAsListenerAndDispose(deadlineCtr);
		deadlineCtr = new DeadlineController(ureq, getWindowControl(), binder);
		listenTo(deadlineCtr);

		removeAsListenerAndDispose(deadlineCalloutCtr);
		FormLink changeDeadlineLink = binderToElements.get(binder).changeDeadlineLink;
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