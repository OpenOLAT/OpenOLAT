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
import org.olat.course.assessment.model.TransientAssessmentMode;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeUserConfirmationController extends BasicController {

	private final CloseableModalController cmc;
	
	public AssessmentModeUserConfirmationController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, TransientAssessmentMode.create(ureq.getUserSession().getAssessmentModes()));
	}
	
	public AssessmentModeUserConfirmationController(UserRequest ureq, WindowControl wControl, List<TransientAssessmentMode> modes) {
		super(ureq, wControl);
		putInitialPanel(new Panel("assessment-mode-chooser"));
		
		VelocityContainer mainVC = createVelocityContainer("choose_mode");
		List<Mode> modeWrappers = new ArrayList<Mode>();
		for(TransientAssessmentMode mode:modes) {
			String name = "go-" + CodeHelper.getRAMUniqueID();
			Link button = LinkFactory.createCustomLink(name, "go", "current.mode.start", Link.BUTTON, mainVC, this);
			button.setUserObject(mode);
			
			Mode wrapper = new Mode(button, mode, getLocale());
			modeWrappers.add(wrapper);
		}
		mainVC.contextPut("modeWrappers", modeWrappers);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), mainVC, true, translate("current.mode"), false);	
		cmc.activate();
		listenTo(cmc);
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
		Windows.getWindows(ureq).getChiefController().lockResource(resource);
		fireEvent(ureq, new ChooseAssessmentModeEvent(mode));
		
		String businessPath = "[RepositoryEntry:" + mode.getRepositoryEntryKey() + "]"; //TODO node
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	public static final class Mode {
		
		private final Locale locale;
		private final Link goButton;
		private final TransientAssessmentMode mode;
		
		public Mode(Link goButton, TransientAssessmentMode mode, Locale locale) {
			this.goButton = goButton;
			this.mode = mode;
			this.locale = locale;
		}
		
		public String getName() {
			return mode.getName();
		}
		
		public String getDescription() {
			return mode.getDescription();
		}
		
		public String getDisplayName() {
			return mode.getDisplayName();
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
