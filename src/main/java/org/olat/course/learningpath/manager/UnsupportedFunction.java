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
package org.olat.course.learningpath.manager;

import java.util.function.Function;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.nodes.INode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * 
 * Initial date: 29 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UnsupportedFunction implements Function<INode, CourseNode> {

	private final CourseEditorTreeModel editorTreeModel;
	private final LearningPathNodeAccessProvider learningPathNodeAccessProvider;
	
	public UnsupportedFunction(CourseEditorTreeModel editorTreeModel) {
		this.editorTreeModel = editorTreeModel;
		this.learningPathNodeAccessProvider = CoreSpringFactory.getImpl(LearningPathNodeAccessProvider.class);
	}
	
	@Override
	public CourseNode apply(INode iNode) {
		CourseNode courseNode = editorTreeModel.getCourseNode(iNode.getIdent());
		if (courseNode != null && !learningPathNodeAccessProvider.isSupported(courseNode.getType())) {
			return courseNode;
		}
		return null;
	}

}
