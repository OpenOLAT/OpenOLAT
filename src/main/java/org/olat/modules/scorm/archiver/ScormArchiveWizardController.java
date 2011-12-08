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
* software distributed under the License is distributed on an "AS IS" BASIS,
* <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2009 frentix GmbH, Switzerland<br>
* <p>
*/
package org.olat.modules.scorm.archiver;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.MimedFileMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.GenericArchiveController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.NodeTableDataModel;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * A wizard to export the results of a SCORM course in a tab separated file. There is two steps:
 * select a SCORM element and then download the file. 
 * 
 * <P>
 * Initial Date:  17 août 2009 <br>
 * @author srosse
 */
public class ScormArchiveWizardController extends BasicController {

	private static final String CMD_SELECT_NODE = "cmd.select.node";
	
	private final Long courseId; 
	private final NodeTableDataModel nodeTableModel;

	private Link showFileButton;
	private WizardController wc;
	private TableController nodeListCtr;
	private VelocityContainer finishedVC;
	private VelocityContainer noResultsVC;
	private Link backLinkAtNoResults;
	
	private String charset;
	private File exportDir;
	private String targetFileName;
	
	public ScormArchiveWizardController(UserRequest ureq, List<Map<String,Object>> nodesTableObjectArrayList, Long courseId, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(GenericArchiveController.class, ureq.getLocale()));
		this.courseId = courseId;

    //table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(getTranslator().translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		
		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);
		// table columns		
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, 
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1,
				CMD_SELECT_NODE, ureq.getLocale()));
		
		nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
		nodeListCtr.setTableDataModel(nodeTableModel);
		
		wc = new WizardController(ureq, wControl, 2);
		listenTo(wc);
		wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
		wc.setNextWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeListCtr.getInitialComponent());
		putInitialPanel(wc.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == showFileButton){
			File exportFile = new File(exportDir, targetFileName);
			MediaResource resource = new MimedFileMediaResource(exportFile, "application/vnd.ms-excel; charset="+charset, true);
			ureq.getDispatchResult().setResultingMediaResource(resource);
			showFileButton.setDirty(false);
		} else if (source == backLinkAtNoResults){
			wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
			wc.setBackWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeListCtr.getInitialComponent());
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
    if(source == nodeListCtr) {
    	if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
    		TableEvent tae = (TableEvent)event;
    		
    		Map<String,Object> nodeData = (Map<String,Object>) nodeTableModel.getObject(tae.getRowId());
    		ICourse course = CourseFactory.loadCourse(courseId);
				CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
    		if(node instanceof ScormCourseNode) {
    			finishedVC = createVelocityContainer("finished");
					showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
					finishedVC.contextPut("nodetitle", node.getShortTitle());
					
					targetFileName = doExport(ureq, (ScormCourseNode)node);
					if(StringHelper.containsNonWhitespace(targetFileName)) {
					  finishedVC.contextPut("filename", targetFileName);
		    	  wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
					  wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
					} else { // no success
						noResultsVC = createVelocityContainer("noresults");
						backLinkAtNoResults = LinkFactory.createLinkBack(noResultsVC, this);
						noResultsVC.contextPut("nodetitle", node.getShortTitle());
						
						wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
						wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), noResultsVC);						
					}
    		}
    	}
    }
		else if (source == wc){
			if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}
	
	private String doExport(UserRequest ureq, ScormCourseNode node) {
		ICourse course = CourseFactory.loadCourse(courseId);
		exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		UserManager um = UserManager.getInstance();
    charset = um.getUserCharset(ureq.getIdentity());
    
    ScormExportManager sreManager = ScormExportManager.getInstance();
		return sreManager.exportResults(course.getCourseEnvironment(), node, getTranslator(), exportDir, charset);
	}
}