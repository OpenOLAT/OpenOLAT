/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * This class prints the status (info/error) messages for a course node to the UI. 
 * 
 * Initial date: 31 Aug 2021<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class NodeStatusController extends BasicController {
	private VelocityContainer main;

	protected NodeStatusController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);		
		main = createVelocityContainer("statusNode");
		main.setElementCssClass("o_course_node_status");
		putInitialPanel(main);
	}

	/**
	 * Read the status from the given course node and display the messages
	 * @param ureq
	 * @param courseNode
	 * @param courseEditorEnvironment
	 */
	public void updateFromNodeStatus(UserRequest ureq, CourseNode courseNode, CourseEditorEnv courseEditorEnvironment) {
		List<String[]> errors = new ArrayList<>();
		List<String[]> warnings = new ArrayList<>();
		List<String[]> infos = new ArrayList<>();
				
		StatusDescription[] allSds = courseNode.isConfigValid(courseEditorEnvironment);
		for (int i = 0; i < allSds.length; i++) {
			StatusDescription description = allSds[i];
			// skip lines with no errors
			if (description != StatusDescription.NOERROR) {
				String[] message = buildMessageLine(description);
				if (description.isError() ) {
					errors.add(message);
				} else if (description.isWarning()) {
					warnings.add(message);
				} else if (description.isInfo()) {
					infos.add(message);
				} 			
			}
		}
		main.contextPut("errors", errors);
		main.contextPut("warnings", warnings);
		main.contextPut("infos", infos);
	}

	private String[] buildMessageLine(StatusDescription description){
		String desc = description.getShortDescription(getLocale());
		String helpWizardCmd = description.getActivateableViewIdentifier();
		if (helpWizardCmd != null) {
			helpWizardCmd = "start.help.wizard" + description.getDescriptionForUnit() + "." + description.getShortDescriptionKey();
		}
		return new String[] {desc, helpWizardCmd};
	}
	
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// forward wizard activation event to parent
		fireEvent(ureq, event);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}
