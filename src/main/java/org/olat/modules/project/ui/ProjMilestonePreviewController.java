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
package org.olat.modules.project.ui;

import org.olat.commons.calendar.CalendarManager;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ui.event.MilestoneDeleteEvent;
import org.olat.modules.project.ui.event.MilestoneEditEvent;
import org.olat.modules.project.ui.event.MilestoneStatusEvent;

/**
 * 
 * Initial date: 9 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMilestonePreviewController extends BasicController {
	
	private VelocityContainer mainVC;
	private Link statusLink;
	private Link editLink;
	private Link deleteLink;
	
	private final ProjMilestone milestone;
	
	public ProjMilestonePreviewController(UserRequest ureq, WindowControl wControl,
			ProjProjectSecurityCallback secCallback, ProjMilestoneInfo info) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.milestone = info.getMilestone();
		
		mainVC = createVelocityContainer("milestone_preview");
		putInitialPanel(mainVC);
		
		String formatDate = milestone.getDueDate() != null
				? Formatter.getInstance(getLocale()).formatDate(milestone.getDueDate())
				: translate("milestone.no.due.date");
		mainVC.contextPut("date", formatDate);
		mainVC.contextPut("achieved", Boolean.valueOf(ProjMilestoneStatus.achieved == milestone.getStatus()));
		mainVC.contextPut("subject", ProjectUIFactory.getDisplayName(getTranslator(), milestone));
		mainVC.contextPut("description", milestone.getDescription());
		
		mainVC.contextPut("formattedTags", TagUIFactory.getFormattedTags(getLocale(), info.getTags()));
		
		if (secCallback.canEditMilestone(milestone)) {
			if (ProjMilestoneStatus.open == milestone.getStatus()) {
				statusLink = LinkFactory.createButton("milestone.mark.done", mainVC, this);
				statusLink.setPrimary(true);
			}
			editLink = LinkFactory.createButton("edit", mainVC, this);
		}
		if (secCallback.canDeleteMilestone(milestone, getIdentity())) {
			deleteLink = LinkFactory.createButton("delete", mainVC, this);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == statusLink) {
			fireEvent(ureq, new MilestoneStatusEvent(milestone));
		} else if (source == editLink) {
			fireEvent(ureq, new MilestoneEditEvent(milestone));
		} else if (source == deleteLink) {
			fireEvent(ureq, new MilestoneDeleteEvent(milestone));
		}

	}

}
