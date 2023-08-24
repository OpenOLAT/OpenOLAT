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
package org.olat.modules.openbadges.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.openbadges.OpenBadgesModule;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesAdminController extends BasicController implements Activateable2 {

	private final static String TYPE_CONFIGURATION = "Configuration";
	private final static String TYPE_TEMPLATES = "Templates";
	private final static String TYPE_GLOBAL_BADGES = "GlobalBadges";
	private final static String TYPE_ISSUED_GLOBAL_BADGES = "IssuedGlobalBadges";

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link configurationLink;
	private final Link templatesLink;
	private final Link globalBadgesLink;
	private final Link issuedGlobalBadgesLink;
	private OpenBadgesAdminConfigurationController configCtrl;
	private OpenBadgesAdminTemplatesController templatesCtrl;
	private BadgeClassesController globalBadgesCtrl;
	private BreadcrumbedStackedPanel stackPanel;
	private IssuedGlobalBadgesController badgeAssertionsController;

	@Autowired
	private OpenBadgesModule openBadgesModule;

	public OpenBadgesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("open_badges_admin");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("openBadges.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		templatesLink = LinkFactory.createLink("openBadges.templates", mainVC, this);
		segmentView.addSegment(templatesLink, false);
		globalBadgesLink = LinkFactory.createLink("form.global.badges", mainVC, this);
		segmentView.addSegment(globalBadgesLink, false);
		issuedGlobalBadgesLink = LinkFactory.createLink("issuedGlobalBadges", mainVC, this);
		segmentView.addSegment(issuedGlobalBadgesLink, false);
		doOpenConfiguration(ureq);

		updateUI();

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent segmentViewEvent) {
				String segmentComponentName = segmentViewEvent.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentComponentName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == templatesLink) {
					doOpenTemplates(ureq);
				} else if (clickedLink == globalBadgesLink) {
					doOpenGlobalBadges(ureq, null, null);
				} else if (clickedLink == issuedGlobalBadgesLink) {
					doOpenIssuedGlobalBadges(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			if (event == Event.CHANGED_EVENT) {
				updateUI();
			}
		}
	}

	private void updateUI() {
		boolean enabled = openBadgesModule.isEnabled();

		globalBadgesLink.setEnabled(enabled);
		issuedGlobalBadgesLink.setEnabled(enabled);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			return;
		}

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (TYPE_CONFIGURATION.equalsIgnoreCase(type)) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
		} else if (TYPE_TEMPLATES.equalsIgnoreCase(type)) {
			doOpenTemplates(ureq);
			segmentView.select(templatesLink);
		} else if (TYPE_GLOBAL_BADGES.equalsIgnoreCase(type)) {
			entries = entries.subList(1, entries.size());
			doOpenGlobalBadges(ureq, entries, state);
			segmentView.select(globalBadgesLink);
		} else if (TYPE_ISSUED_GLOBAL_BADGES.equalsIgnoreCase(type)) {
			doOpenIssuedGlobalBadges(ureq);
			segmentView.select(issuedGlobalBadgesLink);
		}
	}

	private void doOpenConfiguration(UserRequest ureq) {
		removeAsListenerAndDispose(configCtrl);
		WindowControl windowControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(TYPE_CONFIGURATION, 0L), null);
		configCtrl = new OpenBadgesAdminConfigurationController(ureq, windowControl);
		listenTo(configCtrl);
		mainVC.put("segmentCmp", configCtrl.getInitialComponent());
	}

	private void doOpenTemplates(UserRequest ureq) {
		removeAsListenerAndDispose(templatesCtrl);
		WindowControl windowControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(TYPE_TEMPLATES, 0L), null);
		templatesCtrl = new OpenBadgesAdminTemplatesController(ureq, windowControl);
		listenTo(templatesCtrl);
		mainVC.put("segmentCmp", templatesCtrl.getInitialComponent());
	}

	private void doOpenGlobalBadges(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		stackPanel = new BreadcrumbedStackedPanel("stack", getTranslator(), this);

		removeAsListenerAndDispose(globalBadgesCtrl);
		WindowControl windowControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(TYPE_GLOBAL_BADGES, 0L), null);
		globalBadgesCtrl = new BadgeClassesController(ureq, windowControl, null, stackPanel,
				"manual_admin/administration/e-Assessment_openBadges/", "form.add.global.badge",
				"form.edit.badge");
		listenTo(globalBadgesCtrl);

		stackPanel.setInvisibleCrumb(0);
		stackPanel.pushController(translate("badges"), globalBadgesCtrl);

		if (entries != null) {
			globalBadgesCtrl.activate(ureq, entries, state);
		}

		mainVC.put("segmentCmp", stackPanel);
	}

	private void doOpenIssuedGlobalBadges(UserRequest ureq) {
		removeAsListenerAndDispose(badgeAssertionsController);
		WindowControl windowControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(TYPE_ISSUED_GLOBAL_BADGES, 0L), null);
		badgeAssertionsController = new IssuedGlobalBadgesController(ureq, windowControl);
		listenTo(badgeAssertionsController);
		mainVC.put("segmentCmp", badgeAssertionsController.getInitialComponent());
	}
}
