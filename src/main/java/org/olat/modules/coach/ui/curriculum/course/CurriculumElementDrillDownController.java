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
package org.olat.modules.coach.ui.curriculum.course;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementInfosController;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.BasicDetailsHeaderConfig;
import org.olat.repository.ui.list.ImplementationEvent;
import org.olat.repository.ui.list.ImplementationHeaderController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Wrapper controller for the drill-down view of an implementation.
 * Combines a visual header (ImplementationHeaderController) with the
 * curriculum element list table (CurriculumElementListController).
 * Uses BasicController to avoid nested HTML forms.
 * <p>
 * Initial date: 2026-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CurriculumElementDrillDownController extends BasicController {

	private final BreadcrumbPanel stackPanel;
	private final CurriculumElement implementation;

	private CurriculumElementInfosController infosCtrl;
	private final ImplementationHeaderController headerCtrl;
	private final CurriculumElementListController elementListCtrl;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementDrillDownController(UserRequest ureq, WindowControl wControl,
			BreadcrumbPanel stackPanel, Identity assessedIdentity,
			List<CurriculumRef> curriculumRefList, CurriculumElement implementation,
			CurriculumSecurityCallback curriculumSecurityCallback,
			RoleSecurityCallback roleSecurityCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		this.stackPanel = stackPanel;
		this.implementation = implementation;

		VelocityContainer mainVC = createVelocityContainer("curriculum_element_drill_down");

		headerCtrl = new ImplementationHeaderController(ureq, wControl, implementation, true);
		listenTo(headerCtrl);
		mainVC.put("elementHeader", headerCtrl.getInitialComponent());

		elementListCtrl = new CurriculumElementListController(ureq, wControl, stackPanel,
				assessedIdentity, curriculumRefList, implementation,
				curriculumSecurityCallback, roleSecurityCallback, false);
		listenTo(elementListCtrl);
		mainVC.put("elementList", elementListCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (headerCtrl == source) {
			if (event instanceof ImplementationEvent) {
				doOpenDetails(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void doOpenDetails(UserRequest ureq) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(implementation);

		removeAsListenerAndDispose(infosCtrl);

		OLATResourceable ores = CatalogBCFactory.createOfferOres(curriculumElement.getResource());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

		infosCtrl = new CurriculumElementInfosController(ureq, bwControl, curriculumElement, null, new BasicDetailsHeaderConfig(getIdentity()));
		listenTo(infosCtrl);
		addToHistory(ureq, infosCtrl);

		String displayName = curriculumElement.getDisplayName();
		stackPanel.pushController(displayName, infosCtrl);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
	}
}