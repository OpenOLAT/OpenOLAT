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

import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeHelper;

/**
 * 
 * Initial date: 11 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PostMigrationVisitor implements Visitor {

	private final LearningPathRegistry registry;

	public PostMigrationVisitor(LearningPathRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void visit(INode node) {
		CourseNode courseNode = CourseNodeHelper.getCourseNode(node);
		if (courseNode != null) {
			LearningPathNodeHandler lpHandler = registry.getLearningPathNodeHandler(courseNode);
			lpHandler.onMigrated(courseNode);
			// Learning path configs have to be initialized like a new course node
			lpHandler.updateDefaultConfigs(courseNode, true);
		}
	}

}
