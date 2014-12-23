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
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeUserConfirmationController extends BasicController {

	private final VelocityContainer mainVC;
	private final CloseableModalController cmc;
	
	private final String address;
	
	@Autowired
	private AssessmentModeManager assessmentmodeMgr;
	
	public AssessmentModeUserConfirmationController(UserRequest ureq, WindowControl wControl, List<TransientAssessmentMode> modes) {
		super(ureq, wControl);
		putInitialPanel(new Panel("assessment-mode-chooser"));

		address = ureq.getHttpReq().getRemoteAddr();
		
		mainVC = createVelocityContainer("choose_mode");
		List<Mode> modeWrappers = new ArrayList<Mode>();
		for(TransientAssessmentMode mode:modes) {
			Mode wrapper = initAssessmentMode(ureq, mode);
			if(wrapper != null) {
				modeWrappers.add(wrapper);
			}
		}
		mainVC.contextPut("modeWrappers", modeWrappers);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), mainVC, true, translate("current.mode"), false);	
		cmc.activate();
		listenTo(cmc);
	}
	
	private Mode initAssessmentMode(UserRequest ureq, TransientAssessmentMode mode) {
		Date now = new Date();
		
		Status state = null;
		
		Date beginWithLeadTime = mode.getBeginWithLeadTime();
		Date begin = mode.getBegin();
		Date end = mode.getEnd();
		
		if(begin.after(now)) {
			return null;
		} else if(beginWithLeadTime.before(now) && begin.after(now)) {
			state = Status.wait;
		} else if(begin.before(now) && end.after(now)) {
			state = Status.allowed;
		} else if(end.before(now)) {
			return null;
		}

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

		Link button = null;
		if(allowed) {
			String name = "go-" + CodeHelper.getRAMUniqueID();
			button = LinkFactory.createCustomLink(name, "go", "current.mode.start", Link.BUTTON, mainVC, this);
			button.setCustomEnabledLinkCSS("btn btn-primary");
			button.setUserObject(mode);
			button.setEnabled(state == Status.allowed);	
		} else {
			state = Status.refused;
		}
		
		return new Mode(button, state.name(), sb.toString(), mode, getLocale());
	}
	
	@Override
	protected void doDispose() {
		//
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
	 * Remove thie list of assessment modes and lock the chief controller.
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
	
	public static enum Status {
		refused,
		allowed,
		wait,
		closed
	}
	
	public static final class Mode {

		private String status;
		private String errors;
		private final Locale locale;
		private final Link goButton;
		private final TransientAssessmentMode mode;
		
		public Mode(Link goButton, String status, String errors, TransientAssessmentMode mode, Locale locale) {
			this.goButton = goButton;
			this.mode = mode;
			this.errors = errors;
			this.status = status;
			this.locale = locale;
		}
		
		public String getName() {
			return mode.getName();
		}
		
		public String getDescription() {
			return mode.getDescription();
		}
		
		public String getSafeExamBrowserHint() {
			return mode.getSafeExamBrowserHint();
		}
		
		public String getDisplayName() {
			return mode.getDisplayName();
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

		public String getBegin() {
			return Formatter.getInstance(locale).formatDateAndTime(mode.getBegin());
		}
		
		public String getEnd() {
			return Formatter.getInstance(locale).formatDateAndTime(mode.getEnd());
		}
		
		public String getLeadTime() {
			String lt = "";
			if(mode.getLeadTime() > 0) {
				lt = Integer.toString(mode.getLeadTime());
			}
			return lt;
		}

		public String getButtonName() {
			return goButton.getComponentName();
		}

		public TransientAssessmentMode getMode() {
			return mode;
		}
	}
}
