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
package org.olat.group.ui.lifecycle;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroupModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewBusinessGroupLifecycleController extends BasicController implements Activateable2 {

	private Link activeLink;
	private Link inactiveLink;
	private Link deletedLink;
	private final VelocityContainer mainVC;
	
	private BusinessGroupSoftDeleteListController softDeleteCtrl;
	private BusinessGroupActiveListController activeListCtrl;
	private BusinessGroupInactiveListController inactiveListCtrl;
	private AbstractBusinessGroupLifecycleListController selectedCtrl;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public OverviewBusinessGroupLifecycleController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("overview_lifecycle");
		
		activeLink = LinkFactory.createLink("active.step", "active", getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		activeLink.setElementCssClass("btn-primary btn-arrow-right o_businessgroup_active");
		inactiveLink = LinkFactory.createLink("inactive.step", "inactive", getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		inactiveLink.setElementCssClass("btn-arrow-right o_businessgroup_inactive");
		deletedLink = LinkFactory.createLink("deleted.step", "deleted", getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		deletedLink.setElementCssClass("btn-arrow-right o_businessgroup_deleted");
		
		initActiveStep();
		initInactiveStep();
		initDeleteStep();

		selectedCtrl = doOpenActive(ureq);
		putInitialPanel(mainVC);
	}
	
	private void initActiveStep() {
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupInactivationEnabled();
		int daysBeforeEmail = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		
		String mode = automaticEnabled ? translate("process.auto") : translate("process.manual");
		String mail;
		if(daysBeforeEmail <= 0) {
			mail = translate("process.without.email");
		} else {
			mail = translate("process.with.email", Integer.toString(daysBeforeEmail));
		}
		
		String days = translate("search.process.active.sub.title",
				Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeDeactivation()));
		updateStep(activeLink, translate("search.process.active.title"), mode, mail, days);
	}
	
	private void initInactiveStep() {
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupSoftDeleteEnabled();
		int dayBeforeEmail = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
		
		String mode = automaticEnabled ? translate("process.auto") : translate("process.manual");
		String mail;
		if(dayBeforeEmail <= 0) {
			mail = translate("process.without.email");
		} else {
			mail = translate("process.with.email", Integer.toString(dayBeforeEmail));
		}
		
		String days = translate("search.process.inactive.sub.title",
				Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete()));
		updateStep(inactiveLink, translate("search.process.inactive.title"), mode, mail, days);
	}
	
	private void initDeleteStep() {
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupDefinitivelyDeleteEnabled();
		
		String mode = automaticEnabled ? translate("process.auto") : translate("process.manual");
		String mail = translate("process.without.email");

		String days = translate("search.process.delete.sub.title",
				Integer.toString(businessGroupModule.getNumberOfSoftDeleteDayBeforeDefinitivelyDelete()));
		updateStep(deletedLink, translate("search.process.delete.title"), mode, mail, days);
	}
	
	private void updateStep(Link link, String title, String mode, String mail, String process) {
		StringBuilder sb = new StringBuilder();
		sb.append("<strong>").append(title).append("</strong>").append("<br>")
		  .append(mode).append(" - ").append(mail)
		  .append("<br>").append(process);
		  
		link.setCustomDisplayText(sb.toString());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if("Inactive".equals(name)) {
				doOpenInactive(ureq).activate(ureq, subEntries, state);
				selectLink(inactiveLink);
			} else if("Deleted".equals(name)) {
				doOpenDelete(ureq).activate(ureq, subEntries, state);
				selectLink(deletedLink);
			} else {
				doOpenActive(ureq).activate(ureq, subEntries, state);
				selectLink(activeLink);
			}
		} else if(selectedCtrl != null) {
			selectedCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == activeLink) {
			selectedCtrl = doOpenActive(ureq);
			selectedCtrl.activate(ureq, null, null);
			selectLink(activeLink);
		} else if (source == inactiveLink) {
			selectedCtrl = doOpenInactive(ureq);
			selectedCtrl.activate(ureq, null, null);
			selectLink(inactiveLink);
		} else if (source == deletedLink) {
			selectedCtrl = doOpenDelete(ureq);
			selectedCtrl.activate(ureq, null, null);
			selectLink(deletedLink);
		}
	}
	
	private void selectLink(Link selectedLink) {
		setElementCssLink(activeLink, selectedLink, "o_businessgroup_active");
		setElementCssLink(inactiveLink, selectedLink, "o_businessgroup_inactive");
		setElementCssLink(deletedLink, selectedLink, "o_businessgroup_deleted");
	}
	
	private void setElementCssLink(Link link, Link selectedLink, String elementCssClass) {
		String css = link == selectedLink ? "btn-primary btn-arrow-right " : "btn-arrow-right ";
		link.setElementCssClass(css + elementCssClass);
	}
	
	private AbstractBusinessGroupLifecycleListController doOpenActive(UserRequest ureq) {
		if(activeListCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Active", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			activeListCtrl = new BusinessGroupActiveListController(ureq, bwControl, "active-admin-v2");
			listenTo(activeListCtrl);
		}
		
		mainVC.put("groupList", activeListCtrl.getInitialComponent());
		addToHistory(ureq, activeListCtrl);
		return activeListCtrl;
	}
	
	private AbstractBusinessGroupLifecycleListController doOpenInactive(UserRequest ureq) {
		if(inactiveListCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Inactive", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			inactiveListCtrl = new BusinessGroupInactiveListController(ureq, bwControl, "inactive-admin-v2");
			listenTo(inactiveListCtrl);
		}
		
		mainVC.put("groupList", inactiveListCtrl.getInitialComponent());
		addToHistory(ureq, inactiveListCtrl);
		return inactiveListCtrl;
	}
	
	private AbstractBusinessGroupLifecycleListController doOpenDelete(UserRequest ureq) {
		if(softDeleteCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Deleted", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			softDeleteCtrl = new BusinessGroupSoftDeleteListController(ureq, bwControl, "soft-delete-admin-v2");
			listenTo(softDeleteCtrl);
		}
		
		mainVC.put("groupList", softDeleteCtrl.getInitialComponent());
		addToHistory(ureq, softDeleteCtrl);
		return softDeleteCtrl;
	}
}
