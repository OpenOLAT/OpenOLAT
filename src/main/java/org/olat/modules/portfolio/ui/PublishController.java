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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.ui.wizard.AccessRightsContext;
import org.olat.modules.portfolio.ui.wizard.AddMember_1_ChooseMemberStep;
import org.olat.user.UserManager;
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

	private CloseableModalController cmc;
	private AccessRightsEditController editAccessRightsCtrl;
	private StepsMainRunController addMembersWizard;
	
	private int counter;
	private Binder binder;
	private PortfolioElementRow binderRow;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
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
		
		binderRow = new PortfolioElementRow(binder);
		mainVC.contextPut("binderRow", binderRow);
		putInitialPanel(mainVC);
		reloadData();
	}
	
	@Override
	public void initTools() {
		if(secCallback.canEditAccessRights(binder)) {
			addAccessRightsLink = LinkFactory.createToolLink("add.member", translate("add.member"), this);
			addAccessRightsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(addAccessRightsLink, Align.right);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void reloadData() {
		binderRow.getChildren().clear();
		binderRow.getAccessRights().clear();
		
		List<AccessRights> rights = portfolioService.getAccessRights(binder);
		boolean canEditBinderAccessRights = secCallback.canEditAccessRights(binder);
		for(AccessRights right:rights) {
			if(right.getSectionKey() == null && right.getPageKey() == null) {
				Link editLink = null;
				if(canEditBinderAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
					editLink = LinkFactory.createLink("edit_" + (counter++), "edit", "edit_access", mainVC, this);
				}
				binderRow.getAccessRights().add(new AccessRightsRow(binder, right, editLink));
			}
		}

		//sections
		List<Section> sections = portfolioService.getSections(binder);
		Map<Long,PortfolioElementRow> sectionMap = new HashMap<>();
		for(Section section:sections) {
			PortfolioElementRow sectionRow = new PortfolioElementRow(section);
			binderRow.getChildren().add(sectionRow);
			sectionMap.put(section.getKey(), sectionRow);	

			boolean canEditSectionAccessRights = secCallback.canEditAccessRights(section);
			for(AccessRights right:rights) {
				if(section.getKey().equals(right.getSectionKey()) && right.getPageKey() == null) {
					Link editLink = null;
					if(canEditSectionAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
						editLink = LinkFactory.createLink("edit_" + (counter++), "edit", "edit_access", mainVC, this);
					}
					sectionRow.getAccessRights().add(new AccessRightsRow(section, right, editLink));
				}
			}
		}
		
		//pages
		List<Page> pages = portfolioService.getPages(binder);
		for(Page page:pages) {
			Section section = page.getSection();
			PortfolioElementRow sectionRow = sectionMap.get(section.getKey());
			
			PortfolioElementRow pageRow = new PortfolioElementRow(page);
			sectionRow.getChildren().add(pageRow);

			boolean canEditPageAccessRights = secCallback.canEditAccessRights(page);
			for(AccessRights right:rights) {
				if(page.getKey().equals(right.getPageKey())) {
					Link editLink = null;
					if(canEditPageAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
						editLink = LinkFactory.createLink("edit_" + (counter++), "edit", "edit_access", mainVC, this);
					}
					pageRow.getAccessRights().add(new AccessRightsRow(page, right, editLink));
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addAccessRightsLink == source) {
			doAddAccessRights(ureq);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("edit_access".equals(cmd)) {
				AccessRightsRow row = (AccessRightsRow)link.getUserObject();
				doEditAccessRights(ureq, row.getElement(), row.getIdentity());
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addMembersWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadData();
				}
				cleanUp();
			}
		} else if(editAccessRightsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				List<AccessRightChange> changes = editAccessRightsCtrl.getChanges();
				List<Identity> identities = Collections.singletonList(editAccessRightsCtrl.getMember());
				portfolioService.changeAccessRights(identities, changes);
				reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editAccessRightsCtrl);
		removeAsListenerAndDispose(addMembersWizard);
		removeAsListenerAndDispose(cmc);
		editAccessRightsCtrl = null;
		addMembersWizard = null;
		cmc = null;
	}
	
	private void doEditAccessRights(UserRequest ureq, PortfolioElement element, Identity member) {
		if(editAccessRightsCtrl != null) return;
		
		boolean canEdit = secCallback.canEditAccessRights(element);
		editAccessRightsCtrl = new AccessRightsEditController(ureq, getWindowControl(), binder, member, canEdit);
		listenTo(editAccessRightsCtrl);
		
		String title = translate("edit.access.rights");
		cmc = new CloseableModalController(getWindowControl(), null, editAccessRightsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddAccessRights(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizard);

		Step start = new AddMember_1_ChooseMemberStep(ureq, binder);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				AccessRightsContext rightsContext = (AccessRightsContext)runContext.get("rightsContext");
				addMembers(rightsContext);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizard);
		getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
		
	}
	
	private void addMembers(AccessRightsContext rightsContext) {
		List<Identity> identities = rightsContext.getIdentities();
		List<AccessRightChange> changes = rightsContext.getAccessRightChanges();
		portfolioService.changeAccessRights(identities, changes);
		reloadData();
	}
	
	public class AccessRightsRow {
		
		private final AccessRights rights;
		private final PortfolioElement element;
		private String fullName;
		private Link editLink;
		
		public AccessRightsRow(PortfolioElement element, AccessRights rights, Link editLink) {
			this.rights = rights;
			this.editLink = editLink;
			this.element = element;
			fullName = userManager.getUserDisplayName(rights.getIdentity());
			if(editLink != null) {
				editLink.setUserObject(this);
			}
		}
		
		public String getRole() {
			return rights.getRole().name();
		}
		
		public Identity getIdentity() {
			return rights.getIdentity();
		}
		
		public PortfolioElement getElement() {
			return element;
		}
		
		public String getFullName() {
			return fullName;
		}
		
		public String getCssClass() {
			if(PortfolioRoles.reviewer.equals(rights.getRole())) {
				return "o_icon o_icon_reviewer";
			}
			return "o_icon o_icon_user";
		}
		
		public boolean hasEditLink() {
			return editLink != null;
		}
		
		public String getEditLinkComponentName() {
			return editLink == null ? null : editLink.getComponentName();
		}
		
		public String getExplanation() {
			String explanation = null;
			if(PortfolioRoles.owner.equals(rights.getRole())) {
				explanation = translate("access.rights.owner.long");
			} else if(PortfolioRoles.coach.equals(rights.getRole())) {
				explanation = translate("access.rights.coach.long");
			} else if(PortfolioRoles.reviewer.equals(rights.getRole())) {
				explanation = translate("access.rights.reviewer.long");
			}
			return explanation;
		}
	}

	public static class PortfolioElementRow {
		
		private final PortfolioElement element;
		private List<PortfolioElementRow> children;
		private List<AccessRightsRow> accessRights = new ArrayList<>();
		
		public PortfolioElementRow(PortfolioElement element) {
			this.element = element;
		}
		
		public String getTitle() {
			return element.getTitle();
		}
		
		public List<AccessRightsRow> getAccessRights() {
			return accessRights;
		}
		
		public List<PortfolioElementRow> getChildren() {
			if(children == null) {
				children = new ArrayList<>();
			}
			return children;
		}
	}
}
