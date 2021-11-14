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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * 
 * Initial date: 26.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditorStatusController extends BasicController {
	
	private Boolean errorIsOpen = Boolean.TRUE;
	private Boolean warningIsOpen = Boolean.FALSE;
	
	private VelocityContainer main;
	
	public EditorStatusController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		main = createVelocityContainer("status");
		putInitialPanel(main);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		fireEvent(ureq, event);
	}
	
	public void updateStatus(CourseEditorTreeModel cetm, StatusDescription[] courseStatus) {
		main.contextRemove("hasWarnings");
		main.contextRemove("warningIsForNode");
		main.contextRemove("warningMessage");
		main.contextRemove("warningHelpWizardLink");
		main.contextRemove("warningsCount");
		main.contextRemove("warningIsOpen");
		main.contextRemove("hasErrors");
		main.contextRemove("errorIsForNode");
		main.contextRemove("errorMessage");
		main.contextRemove("errorHelpWizardLink");
		main.contextRemove("errorsCount");
		main.contextRemove("errorIsOpen");
		
		long lpTimeStamp = cetm.getLatestPublishTimestamp();
		if (lpTimeStamp == -1) {				
			main.contextPut("publishInfos", translate("published.never.yet"));
		} else { // course has been published before
			Date d = new Date(lpTimeStamp);
			main.contextPut("publishInfos", translate("published.latest", Formatter.getInstance(getLocale()).formatDateAndTime(d)));
		}
		
		if (courseStatus == null || courseStatus.length == 0) {
			main.contextPut("hasCourseStatus", Boolean.FALSE);
			main.contextPut("errorIsOpen", Boolean.FALSE);
		} else {
			List<String> errorIsForNode = new ArrayList<>();
			List<String> errorMessage = new ArrayList<>();
			List<String> errorHelpWizardLink = new ArrayList<>();
			List<String> warningIsForNode = new ArrayList<>();
			List<String> warningMessage = new ArrayList<>();
			List<String> warningHelpWizardLink = new ArrayList<>();
			//
			int errCnt = 0;
			int warCnt = 0;
			String helpWizardCmd;
			for (int i = 0; i < courseStatus.length; i++) {
				StatusDescription description = courseStatus[i];
				String nodeId = courseStatus[i].getDescriptionForUnit();
				String nodeName = cetm.getCourseNode(nodeId).getShortName();
				// prepare wizard link
				helpWizardCmd = courseStatus[i].getActivateableViewIdentifier();
				if (helpWizardCmd != null) {
					helpWizardCmd = "start.help.wizard" + courseStatus[i].getDescriptionForUnit() + "." + courseStatus[i].getShortDescriptionKey();
				} else {
					helpWizardCmd = "NONE";
				}
				if (description.isError()) {
					errCnt++;
					errorIsForNode.add(nodeName);
					errorMessage.add(description.getShortDescription(getLocale()));
					errorHelpWizardLink.add(helpWizardCmd);
				} else if (description.isWarning()) {
					warCnt++;
					warningIsForNode.add(nodeName);
					warningMessage.add(description.getShortDescription(getLocale()));
					warningHelpWizardLink.add(helpWizardCmd);
				}
			}
	
			if (errCnt > 0 || warCnt > 0) {
				if (warCnt > 0) {
					main.contextPut("hasWarnings", Boolean.TRUE);
					main.contextPut("warningIsForNode", warningIsForNode);
					main.contextPut("warningMessage", warningMessage);
					main.contextPut("warningHelpWizardLink", warningHelpWizardLink);
					main.contextPut("warningsCount", new String[] { Integer.toString(warCnt) });
					main.contextPut("warningIsOpen", warningIsOpen);
				}
				if (errCnt > 0) {
					main.contextPut("hasErrors", Boolean.TRUE);
					main.contextPut("errorIsForNode", errorIsForNode);
					main.contextPut("errorMessage", errorMessage);
					main.contextPut("errorHelpWizardLink", errorHelpWizardLink);
					main.contextPut("errorsCount", new String[] { Integer.toString(errCnt) });
					main.contextPut("errorIsOpen", errorIsOpen);
				}
			} else {
				main.contextPut("hasWarnings", Boolean.FALSE);
				main.contextPut("hasErrors", Boolean.FALSE);
			}
		}
	}
}
