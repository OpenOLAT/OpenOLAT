/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.course.run.userview;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.tree.Visitor;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * 
 * Description:<br>
 * This is a utility class which help to travers the course
 * respecting the rules at each course node.
 * 
 * <P>
 * Initial Date:  6 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseTreeVisitor {
	
	private final CourseEnvironment courseEnv;
	private final IdentityEnvironment ienv;

	public CourseTreeVisitor(ICourse course, IdentityEnvironment ienv) {
		this.courseEnv = course.getCourseEnvironment();
		this.ienv = ienv;
	}
	
	public CourseTreeVisitor(CourseEnvironment courseEnv, IdentityEnvironment ienv) {
		this.courseEnv = courseEnv;
		this.ienv = ienv;
	}
	
	public boolean isAccessible(CourseNode node) {
		UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
		TreeNode treeNode = CoreSpringFactory.getImpl(NodeAccessService.class)
				.getCourseTreeModelBuilder(uce)
				.withFilter(AccessibleFilter.create())
				.build()
				.getNodeById(node.getIdent());
		return treeNode != null? treeNode.isAccessible(): false;
	}
	
	public void visit(Visitor visitor) {
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, courseEnv);
		CourseTreeNode rootTreeNode = (CourseTreeNode)CoreSpringFactory.getImpl(NodeAccessService.class)
				.getCourseTreeModelBuilder(userCourseEnv)
				.withFilter(AccessibleFilter.create())
				.build()
				.getRootNode();
		visit(visitor, rootTreeNode);
	}
	
	private void visit(Visitor visitor, CourseTreeNode node) {
		if(node.isAccessible()) {
			visitor.visit(node.getCourseNode());
			for(int i=0; i<node.getChildCount(); i++) {
				CourseTreeNode childNode = (CourseTreeNode)node.getChildAt(i);
				visit(visitor, childNode);
			}
		}
	}
}
