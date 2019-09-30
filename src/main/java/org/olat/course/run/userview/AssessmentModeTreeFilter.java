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

import java.util.HashSet;
import java.util.Set;

import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentMode;

/**
 * 
 * Initial date: 23.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeTreeFilter implements VisibilityFilter {
	
	private final boolean enable;
	private final Set<String> nodeIds = new HashSet<>();
	
	public AssessmentModeTreeFilter(AssessmentMode mode, Structure structure) {
		String nodes = mode.getElementList();
		if(StringHelper.containsNonWhitespace(nodes)) {
			enable = true;
			
			String[] nodeIdArr = nodes.split(",");
			for(String nodeId:nodeIdArr) {
				//allow the parent line
				for(INode courseNode = structure.getNode(nodeId); courseNode != null; courseNode = courseNode.getParent()) {
					nodeIds.add(courseNode.getIdent());
				}
			}
		} else {
			enable = false;
		}
	}

	@Override
	public boolean isVisible(CourseTreeNode node) {
		return !enable || nodeIds.contains(node.getCourseNode().getIdent());
	}
}