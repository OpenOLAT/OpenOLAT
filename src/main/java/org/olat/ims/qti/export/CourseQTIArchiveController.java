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

package org.olat.ims.qti.export;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSURVCourseNode;

/**
 * 
 * @author schneider
 * 
 * Comment:  
 * Archives all QTI results from a specific QTI node in the personal folder
 * of the current user.  
 */
public class CourseQTIArchiveController extends BasicController {

	private CloseableModalController cmc;
	private StepsMainRunController archiveWizardCtrl;

	private Link startExportButton;
	private Link startExportDummyButton;

	private final OLATResourceable courseOres;
	private final List<AssessmentNodeData> nodeList;
	
	/**
	 * Constructor for the assessment tool controller. 
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public CourseQTIArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable courseOres) { 
		super(ureq, wControl);	
		this.courseOres = courseOres;

		VelocityContainer introVC = createVelocityContainer("intro");
		startExportDummyButton = LinkFactory.createButtonSmall("command.start.exportwizard.dummy", introVC, this);
		startExportButton = LinkFactory.createButtonSmall("command.start.exportwizard", introVC, this);

		nodeList = doNodeChoose();
		if (nodeList == null || nodeList.isEmpty()) {
			introVC.contextPut("hasQTINodes", Boolean.FALSE);
		} else {
			introVC.contextPut("hasQTINodes", Boolean.TRUE);
		}
		putInitialPanel(introVC);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startExportButton){
			doArchive(ureq, true);
		} else if (source == startExportDummyButton) {
			doArchive(ureq, false);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == archiveWizardCtrl){
			getWindowControl().pop();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(archiveWizardCtrl);
		removeAsListenerAndDispose(cmc);
		archiveWizardCtrl = null;
		cmc = null;
	}

	private void doArchive(UserRequest ureq, boolean advanced) {
		StepRunnerCallback finish = new FinishArchive();
		Step start  = new Archive_1_SelectNodeStep(ureq, courseOres, nodeList, advanced);
		archiveWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("archive.wizard.title"), "o_sel_test_archive_wizard");
		listenTo(archiveWizardCtrl);
		getWindowControl().pushAsModalDialog(archiveWizardCtrl.getInitialComponent());
	}
	
	public class FinishArchive implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest uureq, WindowControl lwControl, StepsRunContext runContext) {
			QTIArchiver archiver = (QTIArchiver)runContext.get("archiver");
			MediaResource resource = archiver.export();
			uureq.getDispatchResult().setResultingMediaResource(resource);
			return StepsMainRunController.DONE_MODIFIED;
		}
	}
	
	/**
	 * A filtered list of course nodes
	 * @param ureq
	 * @return 
	 */
	private List<AssessmentNodeData> doNodeChoose(){
		// get list of course node data and populate table data model
		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		return addQTINodesAndParentsToList(0, rootNode);
	}
	
	/**
	 * Recursive method that adds tasks nodes and all its parents to a list
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of Object[indent, courseNode, selectable]
	 */
	private List<AssessmentNodeData> addQTINodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<AssessmentNodeData> childrenData = new ArrayList<>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List<AssessmentNodeData> childData = addQTINodesAndParentsToList( (recursionLevel + 1),  child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}
		
		if (childrenData.size() > 0 || courseNode instanceof IQSURVCourseNode) {
			// Store node data in hash map. This hash map serves as data model for 
			// the tasks overview table. Leave user data empty since not used in
			// this table. (use only node data)
			AssessmentNodeData nodeData = new AssessmentNodeData(recursionLevel, courseNode);
			if (courseNode instanceof IQSURVCourseNode) {
				nodeData.setSelectable(true);
			} else {
				nodeData.setSelectable(false);
			}
			
			List<AssessmentNodeData> nodeAndChildren = new ArrayList<>();
			nodeAndChildren.add(nodeData);
			nodeAndChildren.addAll(childrenData);
			return nodeAndChildren;
		}
		return null;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {		
		//
	}
}
