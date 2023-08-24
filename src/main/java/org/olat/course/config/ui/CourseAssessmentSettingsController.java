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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.ui.CertificateReminderProvider;
import org.olat.course.certificate.ui.CertificatesOptionsController;
import org.olat.course.certificate.ui.RecertificationOptionsController;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.reminder.ui.CourseReminderListController;
import org.olat.course.run.RunMainController;
import org.olat.modules.openbadges.OpenBadgesModule;
import org.olat.modules.openbadges.ui.OpenBadgesAssessmentSettingsController;
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
	
	private final VelocityContainer mainVC;
	
	private Controller scoreCtrl;
	private Controller efficiencyStatementCtrl;
	private CourseReminderListController remindersCtrl;
	private CertificatesOptionsController certificatesCtrl;
	private OpenBadgesAssessmentSettingsController badgesCtrl;
	private RecertificationOptionsController recertificationCtrl;
	
	private LockResult lockEntry;
	private RepositoryEntry entry;
	private CertificateReminderProvider certificateReminderProvider;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private OpenBadgesModule openBadgesModule;

	protected CourseAssessmentSettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry entry, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.entry = entry;
		
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
		
		mainVC = createVelocityContainer("assessment_settings");
		
		if (!nodeAccessService.isScoreCalculatorSupported(courseConfig.getNodeAccessType())) {
			scoreCtrl = new CourseScoreController(ureq, wControl, entry, editableAndLocked);
			listenTo(scoreCtrl);
			mainVC.put("score", scoreCtrl.getInitialComponent());
		}
		
		efficiencyStatementCtrl = new EfficiencyStatementController(ureq, wControl, entry, courseConfig, editableAndLocked);
		listenTo(efficiencyStatementCtrl);
		mainVC.put("efficiencyStatement", efficiencyStatementCtrl.getInitialComponent());
		
		certificatesCtrl = new CertificatesOptionsController(ureq, wControl, entry, editableAndLocked);
		listenTo(certificatesCtrl);
		mainVC.put("certificate", certificatesCtrl.getInitialComponent());

		recertificationCtrl = new RecertificationOptionsController(ureq, getWindowControl(), entry, editableAndLocked);
		listenTo(recertificationCtrl);
		mainVC.put("recertification", recertificationCtrl.getInitialComponent());
		
		certificateReminderProvider = new CertificateReminderProvider();
		remindersCtrl = new CourseReminderListController(ureq, wControl, stackPanel, entry, certificateReminderProvider, null, false);
		listenTo(remindersCtrl);
		mainVC.put("reminders", remindersCtrl.getInitialComponent());

		badgesCtrl = new OpenBadgesAssessmentSettingsController(ureq, wControl, entry, editableAndLocked);
		listenTo(badgesCtrl);
		mainVC.put("badges", badgesCtrl.getInitialComponent());

		putInitialPanel(mainVC);
		updateUI(ureq);
	}
	
	private void updateUI(UserRequest ureq) {
		RepositoryEntryCertificateConfiguration certificateConfig = certificatesManager.getConfiguration(entry);
		boolean certificationWithValidityEnabled = (certificateConfig.isAutomaticCertificationEnabled()
				|| certificateConfig.isManualCertificationEnabled()) && certificateConfig.isValidityEnabled();
		boolean recertification = certificationWithValidityEnabled && certificateConfig.isRecertificationEnabled();
	
		recertificationCtrl.getInitialComponent().setVisible(certificationWithValidityEnabled);
		remindersCtrl.getInitialComponent().setVisible(recertification);
		if(recertification) {
			remindersCtrl.reload(ureq);
		}

		boolean openBadgesEnabled = openBadgesModule.isEnabled();
		badgesCtrl.getInitialComponent().setVisible(openBadgesEnabled);

		mainVC.setDirty(true);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == scoreCtrl || source == efficiencyStatementCtrl
				|| source == certificatesCtrl || source == recertificationCtrl) {
			fireEvent(ureq, event);
			updateUI(ureq);
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
