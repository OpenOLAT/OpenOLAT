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
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.tree.Visitor;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NavigationHandler;

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
	private final NodeAccessService nodeAccessService;

	public CourseTreeVisitor(ICourse course, IdentityEnvironment ienv) {
		this.courseEnv = course.getCourseEnvironment();
		this.ienv = ienv;
		this.nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
	}
	
	public CourseTreeVisitor(CourseEnvironment courseEnv, IdentityEnvironment ienv) {
		this.courseEnv = courseEnv;
		this.ienv = ienv;
		this.nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
	}
	
	public boolean isAccessible(CourseNode node, TreeFilter filter) {
		UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
		CourseTreeNode courseTreeNode = nodeAccessService.getNodeEvaluationBuilder(uce)
				.build(node, new TreeEvaluation(), filter);

		boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(courseTreeNode);
		if(mayAccessWholeTreeUp) {
			return true;
		}
		return false;
	}
	
	public void visit(Visitor visitor, TreeFilter filter) {
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, courseEnv);
		TreeEvaluation treeEval = new TreeEvaluation();
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		visit(visitor, rootNode, userCourseEnv, treeEval, filter);
	}
	
	private void visit(Visitor visitor, CourseNode node, UserCourseEnvironment userCourseEnv, TreeEvaluation treeEval, TreeFilter filter) {
		CourseTreeNode courseTreeNode = nodeAccessService.getNodeEvaluationBuilder(userCourseEnv)
				.build(node, treeEval, filter);
		boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(courseTreeNode);
		if(mayAccessWholeTreeUp) {
			visitor.visit(node);
			for(int i=0; i<node.getChildCount(); i++) {
				CourseNode childNode = (CourseNode)node.getChildAt(i);
				visit(visitor, childNode, userCourseEnv, treeEval, filter);
			}	
		}
	}
}
