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
package org.olat.course.run.userview;

import java.util.List;

import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeTreeFilter implements VisibilityFilter {
	
	private final RepositoryEntry courseEntry;
	private final ChiefController chiefController;
	
	public AssessmentModeTreeFilter(RepositoryEntry courseEntry, ChiefController chiefController) {
		this.courseEntry = courseEntry;
		this.chiefController = chiefController;
	}

	@Override
	public boolean isVisible(CourseTreeNode node) {
		LockResourceInfos lockInfos = chiefController.getLockResourceInfos();
		if(lockInfos == null) {
			return true;
		}
		
		TransientAssessmentMode assessmentMode = lockInfos.getLockMode();
		List<String> elementLists = assessmentMode.getElementList();
		if(elementLists == null || elementLists.isEmpty()) {
			return true;
		}
		
		String subIdent = node.getIdent();
		if(elementLists.contains(subIdent)) {
			return true;
		}
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Structure structure = course.getRunStructure();
		for(String nodeId:elementLists) {
			//allow the parent line
			for(INode courseNode = structure.getNode(nodeId); courseNode != null; courseNode = courseNode.getParent()) {
				if(subIdent.equals(courseNode.getIdent())) {
					return true;
				}
			}
		}
		return false;
	}
}