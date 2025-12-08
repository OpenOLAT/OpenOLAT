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
package org.olat.repository.ui.list;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementListConfig;
import org.olat.modules.curriculum.ui.CurriculumElementListController;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationController extends BasicController {
	
	private final BreadcrumbedStackedPanel stackPanel;
	
	private final CurriculumElement rootElement;
	
	private ImplementationCurriculumElementInfosController infosCtrl;
	private final ImplementationHeaderController headerCtrl;
	private final CurriculumElementListController elementListCtrl;
	
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	
	public ImplementationController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			CurriculumRef curriculum, CurriculumElement rootElement,
			List<GroupRoles> asRoles, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		this.stackPanel = stackPanel;
		this.rootElement = rootElement;
		
		VelocityContainer mainVC = createVelocityContainer("implementation");
		
		headerCtrl = new ImplementationHeaderController(ureq, wControl, rootElement, true);
		listenTo(headerCtrl);
		mainVC.put("elementHeader", headerCtrl.getInitialComponent());
		
		CurriculumElementListConfig config = CurriculumElementListConfig.config(false, asRoles);
		elementListCtrl = new CurriculumElementListController(ureq, wControl, stackPanel,
				getIdentity(), curriculum, rootElement, secCallback, config);
		listenTo(elementListCtrl);
		mainVC.put("elementList", elementListCtrl.getInitialComponent());
		
		if(acService.getReservation(getIdentity(), rootElement.getResource()) != null) {
			mainVC.contextPut("pendingMsg", translate("access.denied.not.accepted.yet"));		
		}
	
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(headerCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			} else if(event instanceof ImplementationEvent) {
				doOpenDetails(ureq, rootElement);
			}
		} else if (source == infosCtrl) {
			if (event == Event.DONE_EVENT) {
				stackPanel.popUpToController(this);
			}
		} else if(elementListCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void doOpenDetails(UserRequest ureq, CurriculumElement element) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(element);
		
		removeAsListenerAndDispose(infosCtrl);
		
		OLATResourceable ores = CatalogBCFactory.createOfferOres(curriculumElement.getResource());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		
		infosCtrl = new ImplementationCurriculumElementInfosController(ureq, bwControl, curriculumElement, null, getIdentity());
		listenTo(infosCtrl);
		addToHistory(ureq, infosCtrl);
		
		String displayName = curriculumElement.getDisplayName();
		stackPanel.pushController(displayName, infosCtrl);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
	}
}
