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
package org.olat.course.nodes.cl.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 13.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListStepRunnerCallback implements StepRunnerCallback {
	
	private static final Logger log = Tracing.createLoggerFor(CheckListStepRunnerCallback.class);
	
	private final OLATResourceable courseOres;
	private final boolean scoreCalculatorSupported;
	
	public CheckListStepRunnerCallback(OLATResourceable courseOres, NodeAccessType nodeAccessType) {
		this.courseOres = OresHelper.clone(courseOres);
		NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
		scoreCalculatorSupported = nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		GeneratorData data = (GeneratorData)runContext.get("data");
		List<Checkbox> templateCheckbox = data.getCheckboxList();
		ModuleConfiguration templateConfig = data.getModuleConfiguration();
		
		ICourse course = CourseFactory.getCourseEditSession(courseOres.getResourceableId());
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		
		CourseNode rootNode = ((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode()).getCourseNode();
		CourseNode structureNode = createCourseNode(rootNode, data.getStructureShortTitle(), data.getStructureTitle(), data.getStructureDescription(), "st");
		course.getEditorTreeModel().addCourseNode(structureNode, rootNode);
		
		List<CheckListNode> nodes = data.getNodes();
		List<String> nodesIdent = new ArrayList<>();
		for(CheckListNode node:nodes) {
			String title = node.getTitle();
			CheckListCourseNode checkNode = (CheckListCourseNode)createCourseNode(structureNode, title, title, null, "checklist");
			nodesIdent.add(checkNode.getIdent());

			ModuleConfiguration config = checkNode.getModuleConfiguration();
			config.putAll(templateConfig);
			
			CheckboxList checkboxList = new CheckboxList();
			List<Checkbox> boxes = new ArrayList<>();
			for(Checkbox templateBox:templateCheckbox) {
				Checkbox checkbox = templateBox.clone();
				boxes.add(checkbox);
				
				if(StringHelper.containsNonWhitespace(templateBox.getFilename())) {
					File path = new File(FolderConfig.getCanonicalTmpDir(), templateBox.getCheckboxId());
					VFSContainer tmpContainer = new LocalFolderImpl(path);
					VFSItem item = tmpContainer.resolve(templateBox.getFilename());
					if(item instanceof VFSLeaf) {
						VFSContainer container = checkboxManager.getFileContainer(courseEnv, checkNode);
						VFSManager.copyContent(tmpContainer, container);
					}
				}
			}
			checkboxList.setList(boxes);
			config.set(CheckListCourseNode.CONFIG_KEY_CHECKBOX, checkboxList);
			
			boolean dueDate = node.getDueDate() != null;
			if(dueDate) {
				config.set(CheckListCourseNode.CONFIG_KEY_DUE_DATE, node.getDueDate());
			}
			config.set(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE, Boolean.valueOf(dueDate));
			
			course.getEditorTreeModel().addCourseNode(checkNode, structureNode);
		}
		
		for(Checkbox templateBox:templateCheckbox) {
			if(StringHelper.containsNonWhitespace(templateBox.getFilename())) {
				File path = new File(FolderConfig.getCanonicalTmpDir(), templateBox.getCheckboxId());
				if(path.exists()) {
					try {
						FileUtils.deleteDirsAndFiles(path.toPath());
					} catch (IOException e) {
						log.error("Cannot cleanup tmp directory: " + path);
					}
				}
			}
		}

		if (scoreCalculatorSupported) {
			setScoreCalculation(data, (STCourseNode)structureNode, nodesIdent);
		}
		
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void setScoreCalculation(GeneratorData data, STCourseNode stNode, List<String> checklistNodes) {
		if(!data.isPassed() && !data.isPoints()) return;

		ScoreCalculator sc = stNode.getScoreCalculator();
		if(data.isPoints()) {
			sc.setSumOfScoreNodes(new ArrayList<>(checklistNodes));
		} else {
			sc.setSumOfScoreNodes(null);
		}
		sc.setExpertMode(false);

		if(data.isPassed()) {
			Float cutValue = data.getCutValue();
			if(cutValue == null) {
				sc.setPassedType(ScoreCalculator.PASSED_TYPE_INHERIT);
				sc.setPassedNodes(new ArrayList<>(checklistNodes));
			} else {
				sc.setPassedType(ScoreCalculator.PASSED_TYPE_CUTVALUE);
				sc.setPassedCutValue(cutValue.intValue());
			}
		} else {
			sc.setPassedType(ScoreCalculator.PASSED_TYPE_NONE);
		}

		sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
		sc.setPassedExpression(sc.getPassedExpressionFromEasyModeConfiguration());
		stNode.setScoreCalculator(sc);
	}
	
	private CourseNode createCourseNode(CourseNode parent, String shortTitle, String title, String description, String type) {
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(type);
		CourseNode newNode = newNodeConfig.getInstance(parent);
		newNode.setShortTitle(shortTitle);
		newNode.setLongTitle(title);
		newNode.setDescription(description);
		newNode.setNoAccessExplanation("You don't have access");
		return newNode;
	}
}