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
package org.olat.course.nodes.members;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.editor.NodeEditController;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Description:<br>
 * Edit panel for the members course building block. Nothing to do.
 * 
 * <P>
 * Initial Date:  11 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * @autohr dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class MembersCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	public static final String PANE_TAB_MEMBERSCONFIG = "pane.tab.membersconfig";
	//Config keys
	public static final String CONFIG_KEY_SHOWOWNER = "showOwner";
	public static final String CONFIG_KEY_SHOWCOACHES = "showCoaches";
	public static final String CONFIG_KEY_SHOWPARTICIPANTS = "showParticpants";
	
	public static final String CONFIG_KEY_EMAIL_FUNCTION = "emailFunction";
	public static final String EMAIL_FUNCTION_ALL = "all";
	public static final String EMAIL_FUNCTION_COACH_ADMIN = "coachAndAdmin";

	private static final String[] paneKeys = {PANE_TAB_MEMBERSCONFIG};

	private TabbedPane myTabbedPane;

	private FormBasicController membersConfigForm;

	public MembersCourseNodeEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);

		membersConfigForm = new MembersConfigForm(ureq, getWindowControl(), config);
		listenTo(membersConfigForm);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}
	
	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_MEMBERSCONFIG), membersConfigForm.getInitialComponent());
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if(source == membersConfigForm){
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
