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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.certificate.ui.CertificatesOptionsController;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.run.RunMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseAssessmentSettingsController extends BasicController {
	
	private Controller scoreCtrl;
	private Controller efficiencyStatementCtrl;
	private Controller certificatesCtrl;
	
	private LockResult lockEntry;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private NodeAccessService nodeAccessService;

	protected CourseAssessmentSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker()
				.acquireLock(entry.getOlatResource(), getIdentity(), CourseFactory.COURSE_EDITOR_LOCK, getWindow());
		boolean editableAndLocked = (lockEntry != null && lockEntry.isSuccess()) && editable;
		
		if (lockEntry != null && !lockEntry.isSuccess()) {
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
		
		VelocityContainer mainVC = createVelocityContainer("assessment_settings");
		
		if (!nodeAccessService.isScoreCalculatorSupported(courseConfig.getNodeAccessType())) {
			scoreCtrl = new CourseScoreController(ureq, wControl, entry, editableAndLocked);
			listenTo(scoreCtrl);
			mainVC.put("score", scoreCtrl.getInitialComponent());
		}
		
		efficiencyStatementCtrl = new EfficiencyStatementController(ureq, wControl, entry, courseConfig, editableAndLocked);
		listenTo(efficiencyStatementCtrl);
		mainVC.put("efficiencyStatement", efficiencyStatementCtrl.getInitialComponent());
		
		certificatesCtrl = new CertificatesOptionsController(ureq, wControl, entry, courseConfig, editableAndLocked);
		listenTo(certificatesCtrl);
		mainVC.put("certificate", certificatesCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == scoreCtrl) {
			fireEvent(ureq, event);
		} else if (source == efficiencyStatementCtrl) {
			fireEvent(ureq, event);
		} if (source == certificatesCtrl) {
			fireEvent(ureq, event);
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
        super.doDispose();
	}

}
