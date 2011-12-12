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
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * Edit panel for the members course building block. Nothing to do.
 * 
 * <P>
 * Initial Date:  11 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	private static final String[] paneKeys = {};
	
	private TabbedPane myTabbedPane;

	public MembersCourseNodeEditController(UserRequest ureq, WindowControl wControl, MembersCourseNode courseNode, ICourse course,
			UserCourseEnvironment euce) {
		super(ureq,wControl);

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
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
