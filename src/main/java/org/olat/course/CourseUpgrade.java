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
 * Copyright (c) since 2007 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.course;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Description:<br>
 * Upgrade-Code for course
 * will check for up-to-date editorTreeModel or runStructure first.
 * 
 * <P>
 * Initial Date:  17.07.2009 <br>
 * @author Roman Haag, www.frentix.com, roman.haag@frentix.com,
 */
public class CourseUpgrade extends LogDelegator {
	private static final String MS_TYPE = "ms";

	public CourseUpgrade(){
		//
	}
	
	public void migrateCourse(ICourse course){
		PersistingCourseImpl ccourse = (PersistingCourseImpl) course;
		// only upgrade from version 1 => 2 
		// this will migrate wiki-syntax to html
		int migrateTargetVersion = 2;
		CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
		if (!editorTreeModel.isVersionUpToDate() && editorTreeModel.getVersion() != migrateTargetVersion){
			upgradeEditorTreeModel(ccourse);
			editorTreeModel.setVersion(migrateTargetVersion);
			ccourse.setEditorTreeModel(editorTreeModel);
			ccourse.saveEditorTreeModel();
		}		
		Structure runStructure = course.getRunStructure();
		if (!runStructure.isVersionUpToDate() && runStructure.getVersion() != migrateTargetVersion){
			upgradeRunStructure(ccourse);						
			ccourse.getRunStructure().setVersion(migrateTargetVersion);
			ccourse.setRunStructure(runStructure);
			ccourse.saveRunStructure();
		}
	}

	
	private void upgradeRunStructure(ICourse course) {
		Structure cR = course.getRunStructure();
		CourseNode rsRootNode = cR.getRootNode();
		final Set<String> allSubTreeids = new HashSet<String>();
		TreeVisitor tv = new TreeVisitor(new Visitor() {
			public void visit(INode node) {
				allSubTreeids.add(node.getIdent());
			}
		}, rsRootNode, true);
		tv.visitAll();
		Structure runStructure = course.getRunStructure();

		int nodeCounter = 0;
		for (Iterator<String> iterator2 = allSubTreeids.iterator(); iterator2.hasNext();) {
			String nodeId = iterator2.next();
			CourseNode rsn = runStructure.getNode(nodeId);

			// migrate if this node is a Task
			if (rsn.getType().equals(TACourseNode.CONF_TASK_TYPE)) {
				migrateSingleTask(rsn);
			}
			// migrate no access text for every node:
			migrateSingleAccessDeniedExplanation(rsn);

			// migrate assessment nodes
			if (rsn.getType().equals(MS_TYPE)) {
				migrateSingleAssessment(rsn);
			}

			// migrate course node description
			migrateSingleNodeDesc(rsn);

			nodeCounter++;
		}


		logAudit("**** Lazy migration finished for runStructure of course " + course.getResourceableId() + " with a total of " + nodeCounter
				+ " course node descriptions. ****", null);

	}
	
	private void upgradeEditorTreeModel(ICourse course) {
		// EDITOR: get all course nodes
		CourseEditorTreeModel cT = course.getEditorTreeModel();
		TreeNode rootNode = cT.getRootNode();
		final Set<String> allSubTreeids = new HashSet<String>();
		TreeVisitor tv = new TreeVisitor(new Visitor() {
			public void visit(INode node) {
				allSubTreeids.add(node.getIdent());
			}
		}, rootNode, true);
		tv.visitAll();

		CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
		// EDITOR: loop all course nodes
		int nodeCounter = 0;
		for (Iterator<String> iterator2 = allSubTreeids.iterator(); iterator2.hasNext();) {
			String nodeId = iterator2.next();
			CourseNode cetn = editorTreeModel.getCourseNode(nodeId);
			// migrate if this node is a Task
			if (cetn.getType().equals(TACourseNode.CONF_TASK_TYPE)) {
				migrateSingleTask(cetn);
			}
			// migrate no access text for every node:
			migrateSingleAccessDeniedExplanation(cetn);

			// migrate assessment nodes
			if (cetn.getType().equals(MS_TYPE)) {
				migrateSingleAssessment(cetn);
			}

			// migrate course node description
			migrateSingleNodeDesc(cetn);

			nodeCounter++;

		} //for
		
		logAudit("**** Lazy migration finished for editorTreeModel of course " + course.getResourceableId() + " with a total of " + nodeCounter
				+ " course node descriptions. ****", null);
	}
	

	private void migrateSingleTask(CourseNode node) {
		ModuleConfiguration taskConf = node.getModuleConfiguration();
		String[] allKeys = new String[] { TACourseNode.CONF_TASK_TEXT, MSCourseNode.CONFIG_KEY_INFOTEXT_USER,
				MSCourseNode.CONFIG_KEY_INFOTEXT_COACH };
		for (int i = 0; i < allKeys.length; i++) {
			String thisKey = allKeys[i];
			String oldDesc = (String) taskConf.get(thisKey);
			if (StringHelper.containsNonWhitespace(oldDesc)) {
				String newDesc = Formatter.formatWikiMarkup(oldDesc);
				taskConf.set(thisKey, newDesc);
			}
		}
	}

	private void migrateSingleAccessDeniedExplanation(CourseNode node) {
		String oldDesc = node.getNoAccessExplanation();
		if (StringHelper.containsNonWhitespace(oldDesc)) {
			String newDesc = Formatter.formatWikiMarkup(oldDesc);
			node.setNoAccessExplanation(newDesc);
		}
	}

	private void migrateSingleAssessment(CourseNode node) {
		ModuleConfiguration modConfig = node.getModuleConfiguration();
		String infoUser = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		String newInfoUser = Formatter.formatWikiMarkup(infoUser);
		modConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, newInfoUser);
		String infoCoach = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		String newInfoCoach = Formatter.formatWikiMarkup(infoCoach);
		modConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, newInfoCoach);
	}

	
	private void migrateSingleNodeDesc(CourseNode node){
		String oldDesc = node.getLearningObjectives();
		if (StringHelper.containsNonWhitespace(oldDesc)) {
			String newDesc = Formatter.formatWikiMarkup(oldDesc);
			node.setLearningObjectives(newDesc);
		}
	}
}
