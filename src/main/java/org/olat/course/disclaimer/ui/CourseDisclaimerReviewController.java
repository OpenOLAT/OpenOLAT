/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.disclaimer.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.course.disclaimer.CourseDisclaimerConsent;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.event.CourseDisclaimerEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Date: 19 May 2022<br>
 * @author Florian Gnägi
 */
public class CourseDisclaimerReviewController extends BasicController {
	private RepositoryEntry repositoryEntry;
	private CourseDisclaimerConsentController disclaimerCtr;
	private VelocityContainer reviewVC;
	
	private Link withdrawLink;
	private DialogBoxController dbc;
	
	@Autowired
	private CourseDisclaimerManager disclaimerManager;

	public CourseDisclaimerReviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;
		// First load the current consent of the user
		CourseDisclaimerConsent consent= disclaimerManager.getConsent(repositoryEntry, ureq.getIdentity());
		if (consent == null) {		
			// something strange happened, user should not be able to call this code if he never accepted the disclaimer
			logWarn("CourseDisclaimerReviewController called with NULL consent for repoEntry::" + repositoryEntry.getKey() + " and identity::" + getIdentity().getKey(), null);
			putInitialPanel(new Panel("empty"));
		}		
		
		// Reuse the disclaimer form
		disclaimerCtr = new CourseDisclaimerConsentController(ureq, wControl, repositoryEntry);
		listenTo(disclaimerCtr);
		// Init the view with the user values of his consent, setup in read-only mode
		disclaimerCtr.initForConsentReview(consent);
		// Use the review.html for layouting
		reviewVC = createVelocityContainer("review");
		reviewVC.put("disclaimer", disclaimerCtr.getInitialComponent());
		reviewVC.contextPut("consentDate", consent.getConsentDate());			
		// Add a link to revoke the disclaimer
		withdrawLink = LinkFactory.createButton("withdraw.action", reviewVC, this);
		withdrawLink.setCustomEnabledLinkCSS("btn btn-danger");
		withdrawLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reject");
		
		putInitialPanel(reviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == withdrawLink) {
			String message = "<div class='o_warning'><i class='o_icon o_icon_warn o_icon-fw'> </i> " + translate("course.disclaimer.cancel.message") + "</div>";
			dbc = activateYesNoDialog(ureq, translate("course.disclaimer.cancel.confirmation"), message, dbc);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == dbc) {
			if (DialogBoxUIFactory.isYesEvent(event) ){
				// mark DB that user denied by setting the checkboxes to false
				disclaimerManager.acceptDisclaimer(repositoryEntry, getIdentity(), ureq.getUserSession().getRoles(), false, false);
				// notify course to close the window
				fireEvent(ureq, CourseDisclaimerEvent.REJECTED);
			}
			// else do nothing, user can still accept
			removeAsListenerAndDispose(dbc);
			dbc = null;
		}
	}

}
