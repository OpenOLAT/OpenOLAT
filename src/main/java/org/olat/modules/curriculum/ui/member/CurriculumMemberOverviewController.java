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
package org.olat.modules.curriculum.ui.member;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.coach.security.ViewCreditPointsRightProvider;
import org.olat.modules.creditpoint.CreditPointModule;
import org.olat.modules.creditpoint.ui.CreditPointSecurityCallback;
import org.olat.modules.creditpoint.ui.CreditPointSecurityCallbackFactory;
import org.olat.modules.creditpoint.ui.CreditPointUserController;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;
import org.olat.modules.curriculum.ui.component.NextEvent;
import org.olat.modules.curriculum.ui.component.PreviousEvent;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.resource.accesscontrol.ViewAs;
import org.olat.resource.accesscontrol.ui.UserOrderController;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumMemberOverviewController extends BasicController {

	private Link nextButton;
	private Link previousButton;
	private TabbedPane memberTabP;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private final Identity member;
	private final List<String> selectedRights;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CreditPointModule creditPointModule;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public CurriculumMemberOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Identity member, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, Util
				.createPackageTranslator(CurriculumManagerRootController.class, ureq.getLocale()));
		this.member = member;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		selectedRights = curriculumModule.getUserOverviewRightList();
		
		mainVC = createVelocityContainer("member_overview");
		initPortrait(ureq);
		String fullname = userManager.getUserDisplayName(member);
		mainVC.contextPut("fullname", fullname);
		
		initTab(ureq);
		initSlide();
		
		putInitialPanel(mainVC);
	}
	
	private void initSlide() {
		previousButton = LinkFactory.createCustomLink("member.previous", "previous-member", "", Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		previousButton.setIconLeftCSS("o_icon o_icon-fw o_icon_slide_backward");
		previousButton.setTitle(translate("member.previous"));
		
		nextButton = LinkFactory.createCustomLink("member.next", "next-member", "", Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		nextButton.setIconLeftCSS("o_icon o_icon-fw o_icon_slide_forward");
		nextButton.setTitle(translate("member.next"));
	}
	
	private void initPortrait(UserRequest ureq) {
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), member);
		CurriculumMemberInfoController profile = new CurriculumMemberInfoController(ureq, getWindowControl(),
				member, profileConfig, portraitUser);
		listenTo(profile);
		mainVC.put("portrait", profile.getInitialComponent());
	}
	
	private void initTab(UserRequest ureq) {
		memberTabP = new TabbedPane("memberTabP", getLocale());
		memberTabP.addListener(this);
		mainVC.put("memberTabP", memberTabP);
		
		Roles managerRoles = ureq.getUserSession().getRoles();
		
		memberTabP.addTab(ureq, translate("tab.implementations"), uureq -> {
			Controller implementationsCtrl = new CurriculumMemberImplementationsController(uureq, getWindowControl(), toolbarPanel,
					member, secCallback);
			listenTo(implementationsCtrl);
			return implementationsCtrl.getInitialComponent();
		});
		
		memberTabP.addTab(ureq, translate("tab.bookings"), uureq -> {
			Controller accessCtr = new UserOrderController(uureq, getWindowControl(), member,
					ViewAs.CURRICULUM_AS_MANAGER, true);
			listenTo(accessCtr);
			return accessCtr.getInitialComponent();
		});
		
		if (creditPointModule.isEnabled() && selectedRights.contains(ViewCreditPointsRightProvider.RELATION_RIGHT)
				&& managerRoles.hasRole(OrganisationRoles.curriculummanager)) {
			memberTabP.addTab(ureq, translate("tab.credit.points"), uureq -> {
				CreditPointSecurityCallback secCallback = CreditPointSecurityCallbackFactory.createSecurityCallback(managerRoles);
				Controller creditPointCtr = new CreditPointUserController(uureq, getWindowControl(), member, false, secCallback);
				listenTo(creditPointCtr);
				return creditPointCtr.getInitialComponent();
			});
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == previousButton) {
			fireEvent(ureq, new PreviousEvent(member));
		} else if (source == nextButton) {
			fireEvent(ureq, new NextEvent(member));
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ActivateEvent) {
			fireEvent(ureq, event);
		}
	}
}
