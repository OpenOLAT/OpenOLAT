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
package org.olat.course.assessment.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeGuardController extends BasicController implements GenericEventListener {

	private Link button;
	private final VelocityContainer mainVC;
	private final CloseableModalController cmc;
	
	private final String address;
	
	private boolean pushUpdate = false;
	private List<TransientAssessmentMode> modes;
	
	@Autowired
	private AssessmentModeManager assessmentmodeMgr;
	
	public AssessmentModeGuardController(UserRequest ureq, WindowControl wControl, List<TransientAssessmentMode> modes) {
		super(ureq, wControl);
		putInitialPanel(new Panel("assessment-mode-chooser"));

		this.modes = modes;
		address = ureq.getHttpReq().getRemoteAddr();
		
		mainVC = createVelocityContainer("choose_mode");
		syncAssessmentModes(ureq);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), mainVC, true, translate("current.mode"), false);	
		cmc.activate();
		listenTo(cmc);
		
		//register for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	public void deactivate() {
		try {
			cmc.deactivate();
		} catch (Exception e) {
			logWarn("", e);
		}
	}
	
	@Override
	protected void doDispose() {
		//deregister for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	public boolean updateAssessmentMode(UserRequest ureq) {
		boolean f = false;
		if(pushUpdate) {
			syncAssessmentModes(ureq);
			f = true;
			pushUpdate = false;
		} else {
			f = false;
		}
		return f;
	}
	
	private void syncAssessmentModes(UserRequest ureq) {
		List<Mode> modeWrappers = new ArrayList<Mode>();
		for(TransientAssessmentMode mode:modes) {
			Mode wrapper = initAssessmentMode(ureq, mode);
			if(wrapper != null) {
				modeWrappers.add(wrapper);
			}
		}
		mainVC.contextPut("modeWrappers", modeWrappers);
	}
	
	private Mode initAssessmentMode(UserRequest ureq, TransientAssessmentMode mode) {
		Date now = new Date();

		Date beginWithLeadTime = mode.getBeginWithLeadTime();
		Date endWithLeadTime = mode.getEndWithFollowupTime();
		if(beginWithLeadTime.after(now)) {
			return null;
		} else if(endWithLeadTime.before(now)) {
			return null;
		} else if(Status.end == mode.getStatus()) {
			return null;
		}
		
		String state;
		Date begin = mode.getBegin();
		Date end = mode.getEnd();

		StringBuilder sb = new StringBuilder();
		boolean allowed = true;
		if(mode.getIpList() != null) {
			boolean ipInRange = assessmentmodeMgr.isIpAllowed(mode.getIpList(), address);
			if(!ipInRange) {
				sb.append(translate("error.ip.range"));
			}
			allowed &= ipInRange;
		}
		if(mode.getSafeExamBrowserKey() != null) {
			boolean safeExamCheck = assessmentmodeMgr.isSafelyAllowed(ureq.getHttpReq(), mode.getSafeExamBrowserKey());
			if(!safeExamCheck) {
				if(sb.length() > 0) sb.append("<br />");
				sb.append(translate("error.safe.exam"));
			}
			allowed &= safeExamCheck;
		}

		if(allowed) {
			String name = "go-" + CodeHelper.getRAMUniqueID();
			if(button == null) {
				button = LinkFactory.createCustomLink(name, "go", "current.mode.start", Link.BUTTON, mainVC, this);
				button.setUserObject(mode);
			}

			if(beginWithLeadTime.compareTo(now) <= 0 && begin.compareTo(now) > 0) {
				state = Status.leadtime.name();
				button.setEnabled(false);
				button.setVisible(true);
			} else if(begin.compareTo(now) <= 0 && end.compareTo(now) > 0) {
				state = Status.assessment.name();
				button.setCustomEnabledLinkCSS("btn btn-primary");
				button.setEnabled(true);
				button.setVisible(true);
			} else if(end.compareTo(now) <= 0 && endWithLeadTime.compareTo(now) >= 0) {
				state = Status.followup.name();
				button.setEnabled(false);
				button.setVisible(false);
			} else {
				state = "error";
				button.setEnabled(false);
				button.setVisible(false);
			}
		} else {
			state = "refused";
		}
		
		return new Mode(button, state, sb.toString(), mode, getLocale());
	}

	@Override
	public void event(Event event) {
		 if (event instanceof AssessmentModeNotificationEvent) {
			processAssessmentModeNotificationEvent((AssessmentModeNotificationEvent)event);
		}
	}
	
	private void processAssessmentModeNotificationEvent(AssessmentModeNotificationEvent event) {
		if(getIdentity() == null || !event.getAssessedIdentityKeys().contains(getIdentity().getKey())) {
			return;//not for me
		}
		
		String cmd = event.getCommand();
		if(AssessmentModeNotificationEvent.LEADTIME.equals(cmd)
				|| AssessmentModeNotificationEvent.START_ASSESSMENT.equals(cmd)
				|| AssessmentModeNotificationEvent.STOP_ASSESSMENT.equals(cmd)) {
			TransientAssessmentMode mode = event.getAssessementMode();
			

			List<TransientAssessmentMode> updatedModes = new ArrayList<TransientAssessmentMode>();
			for(TransientAssessmentMode currentMode:modes) {
				if(currentMode.getModeKey().equals(mode.getModeKey())) {
					updatedModes.add(mode);
				} else {
					updatedModes.add(currentMode);
				}
			}
			
			pushUpdate = true;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("go".equals(link.getCommand())) {
				launchAssessmentMode(ureq, (TransientAssessmentMode)link.getUserObject());
			}
		}
	}
	
	/**
	 * Remove the list of assessment modes and lock the chief controller.
	 * 
	 * 
	 * @param ureq
	 * @param mode
	 */
	private void launchAssessmentMode(UserRequest ureq, TransientAssessmentMode mode) {
		ureq.getUserSession().setAssessmentModes(null);
		OLATResourceable resource = mode.getResource();
		ureq.getUserSession().setLockResource(resource, mode);
		Windows.getWindows(ureq).getChiefController().lockResource(resource);
		fireEvent(ureq, new ChooseAssessmentModeEvent(mode));
		
		String businessPath = "[RepositoryEntry:" + mode.getRepositoryEntryKey() + "]";
		if(StringHelper.containsNonWhitespace(mode.getStartElementKey())) {
			businessPath += "[CourseNode:" + mode.getStartElementKey() + "]";
		}
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	public static final class Mode {

		private String status;
		private String errors;
		private final Link goButton;
		
		private String name;
		private String displayName;
		private String description;
		private String safeExamBrowserHint;
		
		private String begin;
		private String end;
		private String leadTime;
		private String followupTime;
		
		public Mode(Link goButton, String status, String errors, TransientAssessmentMode mode, Locale locale) {
			this.goButton = goButton;
			this.errors = errors;
			this.status = status;
			
			name = mode.getName();
			displayName = mode.getDisplayName();
			description = mode.getDescription();
			safeExamBrowserHint = mode.getSafeExamBrowserHint();
			
			Formatter f = Formatter.getInstance(locale);
			begin = f.formatDateAndTime(mode.getBegin());
			end = f.formatDateAndTime(mode.getEnd());
			
			if(mode.getFollowupTime() > 0) {
				followupTime = Integer.toString(mode.getFollowupTime());
			} else {
				followupTime = null;
			}
			
			if(mode.getLeadTime() > 0) {
				leadTime = Integer.toString(mode.getLeadTime());
			} else {
				leadTime = null;
			}
		}
		
		public String getName() {
			return name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getSafeExamBrowserHint() {
			return safeExamBrowserHint;
		}
		
		public String getDisplayName() {
			return displayName;
		}
		
		public String getBegin() {
			return begin;
		}
		
		public String getEnd() {
			return end;
		}
		
		public String getLeadTime() {
			return leadTime;
		}
		
		public String getFollowupTime() {
			return followupTime;
		}
		
		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getErrors() {
			return errors;
		}

		public void setErrors(String errors) {
			this.errors = errors;
		}

		public String getButtonName() {
			return goButton.getComponentName();
		}
	}
}
