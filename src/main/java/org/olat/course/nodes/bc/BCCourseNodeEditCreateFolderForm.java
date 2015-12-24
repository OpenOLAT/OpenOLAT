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
package org.olat.course.nodes.bc;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;

/**
 * Initial Date: Dez 22, 2015
 *
 * @author dfurrer
 */
public class BCCourseNodeEditCreateFolderForm extends FormBasicController {

	private FormLink createButton;
	private TextElement createPath;
	private VFSContainer courseFolder;
	private BCCourseNode bcNode;

	public BCCourseNodeEditCreateFolderForm(UserRequest ureq, WindowControl wControl, ICourse course, BCCourseNode bcNode) {
		super(ureq, wControl);
		this.courseFolder = course.getCourseFolderContainer();
		this.bcNode = bcNode;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createPath = uifactory.addTextElement("createPath", "createPath", 100, "/"+bcNode.getShortTitle(), formLayout);
		createPath.setLabel("createPath", null);
		createButton = uifactory.addFormLink("createButton", formLayout, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createButton){
			String path = createPath.getValue();
			path = VFSManager.sanitizePath(path);

			if(VFSManager.resolveOrCreateContainerFromPath(courseFolder, path) != null){
				fireEvent(ureq, new Event(path));
			}else{
				logInfo("something went wrong with the creation", null);
			}
		}
	}


	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}
}
