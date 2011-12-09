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
package org.olat.modules.scorm.archiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.GenericArchiveController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ScormCourseNode;

/**
 * 
 * Description:<br>
 * Main controller for the SCORM archive export. All the job is done in the wizard
 * 
 * <P>
 * Initial Date:  17 august 2009 <br>
 * @author srosse
 */
public class ScormResultArchiveController extends BasicController {

	private static final String CMD_SELECT_NODE = "cmd.select.node";
	
	private VelocityContainer introVC;
	
	private TableController nodeListCtr;
	private ScormArchiveWizardController wizardController;
	private CloseableModalController cmc;
	
	private final Long courseId;
	
	private List<Map<String,Object>> nodesTableObjectArrayList;
	private final Link startExportButton;
	
	public ScormResultArchiveController(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl, Util.createPackageTranslator(GenericArchiveController.class, ureq.getLocale()));
		
		courseId = course.getResourceableId();

		introVC = createVelocityContainer("intro");
		startExportButton = LinkFactory.createButtonSmall("command.start.exportwizard", introVC, this);
		
		nodesTableObjectArrayList = doNodeChoose(ureq);
		
		if (nodesTableObjectArrayList == null) {
			introVC.contextPut("hasScormNodes", Boolean.FALSE);
		} else {
			introVC.contextPut("hasScormNodes", Boolean.TRUE);
		}
		
		putInitialPanel(introVC);
	}

	@Override
	protected void doDispose() {
		// controllers autodisposed by basic controller
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == startExportButton){
			
			removeAsListenerAndDispose(wizardController);
			wizardController = new ScormArchiveWizardController(ureq, nodesTableObjectArrayList, courseId, getWindowControl());
			listenTo(wizardController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), wizardController.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == wizardController){
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		}
	}

	private List<Map<String,Object>> doNodeChoose(UserRequest ureq){
    //table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		
		removeAsListenerAndDispose(nodeListCtr);
		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), this.getTranslator());
		listenTo(nodeListCtr);
		// table columns		
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, 
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1,
				CMD_SELECT_NODE, ureq.getLocale()));
		
		// get list of course node data and populate table data model
		ICourse course = CourseFactory.loadCourse(courseId);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List<Map<String,Object>> objectArrayList = addScormNodesAndParentsToList(0, rootNode);
		return objectArrayList;		
	}
	
	private List<Map<String,Object>> addScormNodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<Map<String,Object>> childrenData = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List<Map<String,Object>> childData = addScormNodesAndParentsToList( (recursionLevel + 1),  child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}
		
		if (!childrenData.isEmpty() || courseNode instanceof ScormCourseNode) {
			// Store node data in hash map. This hash map serves as data model for 
			// the tasks overview table. Leave user data empty since not used in
			// this table. (use only node data)
			Map<String,Object> nodeData = new HashMap<String,Object>();
			// indent
			nodeData.put(AssessmentHelper.KEY_INDENT, new Integer(recursionLevel));
			// course node data
			nodeData.put(AssessmentHelper.KEY_TYPE, courseNode.getType());
			nodeData.put(AssessmentHelper.KEY_TITLE_SHORT, courseNode.getShortTitle());
			nodeData.put(AssessmentHelper.KEY_TITLE_LONG, courseNode.getLongTitle());
			nodeData.put(AssessmentHelper.KEY_IDENTIFYER, courseNode.getIdent());
			nodeData.put(AssessmentHelper.KEY_SELECTABLE, (courseNode instanceof ScormCourseNode) ? Boolean.TRUE : Boolean.FALSE);
			
			List<Map<String,Object>> nodeAndChildren = new ArrayList<Map<String,Object>>();
			nodeAndChildren.add(nodeData);
			nodeAndChildren.addAll(childrenData);
			return nodeAndChildren;
		}
		return null;
	}
}