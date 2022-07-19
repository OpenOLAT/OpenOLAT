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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.ims.lti13.DeploymentConfigurationPermission;
import org.olat.ims.lti13.LTI13Module;
import org.olat.modules.invitation.InvitationConfigurationPermission;
import org.olat.modules.invitation.InvitationModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseOptionsAndInvitationSettingsController extends BasicController {
	
	private final CourseOptionsController courseOptionsCtrl;
	private CourseInvitationsAndDeploymentsSettingsController invitationSettingsCtrl;
	
	private LockResult lockEntry;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InvitationModule invitationModule;
	
	public CourseOptionsAndInvitationSettingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, ICourse course, RepositoryEntrySecurity reSecurity, boolean canEdit) {
		super(ureq, wControl);
		
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker()
				.acquireLock(entry.getOlatResource(), getIdentity(), CourseFactory.COURSE_EDITOR_LOCK, getWindow());
		boolean editable = (lockEntry != null && lockEntry.isSuccess()) && canEdit;
		
		VelocityContainer mainVC = createVelocityContainer("options_invitation_settings");
		
		courseOptionsCtrl = new CourseOptionsController(ureq, getWindowControl(), entry, course, editable);
		listenTo(courseOptionsCtrl);
		mainVC.put("options", courseOptionsCtrl.getInitialComponent());
		
		if(((invitationModule.isCourseInvitationEnabled() && invitationModule.getCourseOwnerPermission() == InvitationConfigurationPermission.perResource)
				|| (lti13Module.isEnabled() && lti13Module.getDeploymentRepositoryEntryOwnerPermission() == DeploymentConfigurationPermission.perResource))
				&& (reSecurity.isAdministrator() || reSecurity.isPrincipal() || reSecurity.isLearnResourceManager())) {
			invitationSettingsCtrl = new CourseInvitationsAndDeploymentsSettingsController(ureq, getWindowControl(), entry, editable);
			listenTo(invitationSettingsCtrl);
			mainVC.put("invitations", invitationSettingsCtrl.getInitialComponent());
		}

		putInitialPanel(mainVC);
		
		if(lockEntry != null && !lockEntry.isSuccess()) {
			String lockerName = "???";
			if(lockEntry.getOwner() != null) {
				lockerName = userManager.getUserDisplayName(lockEntry.getOwner());
			}
			if(lockEntry.isDifferentWindows()) {
				showWarning("error.editoralreadylocked.same.user", new String[] { lockerName });
			} else {
				showWarning("error.editoralreadylocked", new String[] { lockerName });
			}
		}
	}
	
	@Override
	protected void doDispose() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(courseOptionsCtrl == source || invitationSettingsCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT
					|| event instanceof ReloadSettingsEvent) {
				fireEvent(ureq, event);
			}
		}
	}
}
