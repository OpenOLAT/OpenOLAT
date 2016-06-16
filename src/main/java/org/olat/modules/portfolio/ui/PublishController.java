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

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.ui.wizard.AddMember_1_ChooseMemberStep;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublishController extends BasicController implements TooledController {
	
	private Link addAccessRightsLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	
	private StepsMainRunController addMembersWizard;
	
	private Binder binder;
	private final BinderSecurityCallback secCallback;
	
	private List<Identity> owners;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PublishController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder) {
		super(ureq, wControl);
		this.binder = binder;
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		
		mainVC = createVelocityContainer("publish");
		mainVC.contextPut("binderTitle", binder.getTitle());
		
		owners = portfolioService.getMembers(binder, GroupRoles.owner.name());

		mainVC.contextPut("owners", owners);

		putInitialPanel(mainVC);
	}
	
	@Override
	public void initTools() {
		addAccessRightsLink = LinkFactory.createToolLink("edit.binder.metadata", translate("edit.binder.metadata"), this);
		addAccessRightsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
		stackPanel.addTool(addAccessRightsLink, Align.right);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void reloadData() {
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addAccessRightsLink == source) {
			doAddAccessRights(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addMembersWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(addMembersWizard);
				addMembersWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadData();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doAddAccessRights(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizard);

		Step start = new AddMember_1_ChooseMemberStep(ureq, binder);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				addMembers(uureq, runContext);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizard);
		getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
		
	}
	
	private void addMembers(UserRequest ureq, StepsRunContext runContext) {
		
	}
}
