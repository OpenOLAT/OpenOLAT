/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.archiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.ExportUtil;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;

/**
 * Description: Course-Results-Archiver using ScoreAccountingHelper.class
 * 
 * Initial Date: Sep 23, 2004
 * @author gnaegi
 */
public class ScoreAccountingArchiveController extends BasicController {

	private final OLATResourceable ores;
	private StackedPanel myPanel;
	private VelocityContainer myContent;
	private Link startButton;
	private Link downloadButton;
	

	/**
	 * Constructor for the score accounting archive controller
	 * @param ureq
	 * @param course
	 */
	public ScoreAccountingArchiveController(UserRequest ureq, WindowControl wControl, 
			OLATResourceable ores) {
		super(ureq, wControl);
		this.ores = ores;

		myPanel = putInitialPanel(myPanel);
		myContent = createVelocityContainer("start");
		startButton = LinkFactory.createButton("cmd.start", myContent, this);
		myPanel.setContent(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startButton) {
			doStartExport();
		} else if(source == downloadButton) {
			File file = (File)downloadButton.getUserObject();
			if(file != null) {
				MediaResource resource = new FileMediaResource(file, true);
				ureq.getDispatchResult().setResultingMediaResource(resource);
			}
		}
	}
	
	private void doStartExport() {
		ICourse course = CourseFactory.loadCourse(ores);
		List<Identity> users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment());
		List<CourseNode> nodes = ScoreAccountingHelper.loadAssessableNodes(course.getCourseEnvironment());
		
		String courseTitle = course.getCourseTitle();
		String fileName = ExportUtil.createFileNameWithTimeStamp(courseTitle, "zip");
		// location for data export
		File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), courseTitle);
		File downloadFile = new File(exportDirectory, fileName);
		try(OutputStream fOut = new FileOutputStream(downloadFile);
				ZipOutputStream zout = new ZipOutputStream(fOut)) {
			ScoreAccountingHelper.createCourseResultsOverview(users, nodes, course, getLocale(), zout);
		} catch(IOException e) {
			logError("", e);
		}

		VelocityContainer vcFeedback = createVelocityContainer("feedback");
		vcFeedback.contextPut("body", translate("course.res.feedback", new String[] { downloadFile.getName() }));
		downloadButton = LinkFactory.createButtonSmall("cmd.download", vcFeedback, this);
		downloadButton.setUserObject(downloadFile);
		myPanel.setContent(vcFeedback);
	}
}