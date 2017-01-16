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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.editor;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;

/**
 * Description:<br>
 * The course environment used when working in the course editor. Provides some
 * methods to help all the validation and consistency checking code
 * 
 * <P>
 * Initial Date: Jul 6, 2005 <br>
 * @author patrick
 */
public interface CourseEditorEnv {
	/**
	 * checks for an existing node id if the underlying course node is assessable,
	 * e.g. if it makes sense to be used in certain condition functions
	 * 
	 * @param nodeId
	 * @return
	 */
	boolean isAssessable(String nodeId);

	/**
	 * @param nodeId
	 * @return
	 */
	boolean existsNode(String nodeId);

	/**
	 * @param groupname
	 * @return
	 */
	boolean existsGroup(String groupname);
	
	/**
	 * @param areaname
	 * @return
	 */
	boolean existsArea(String areaNameOrKey);
	
	/**
	 * Return the invalid areas
	 * @param areaname
	 * @return
	 */
	public List<String> validateAreas(List<String> areaname);

	/**
	 * @return
	 */
	String getCurrentCourseNodeId();

	/**
	 * @param courseNodeId
	 */
	void setCurrentCourseNodeId(String courseNodeId);

	/**
	 * @param ci
	 */
	void setConditionInterpreter(ConditionInterpreter ci);

	/**
	 * the locale of the editor environment to have the facility of providing
	 * localized messages within the course editor environment.
	 * 
	 * @return Locale of editor environment
	 */
	Locale getEditorEnvLocale();

	/**
	 * validates the condition expression within the condition interpreter. If the
	 * condition expression is syntactically and semantically correct the method
	 * returns true, false otherwise.<BR>
	 * </BR> The valid soft references found are accessible via the condition
	 * expression, and also the complete error summary for the condition
	 * expression.
	 * 
	 * @param condExpr
	 * @return
	 */
	public ConditionErrorMessage[] validateConditionExpression(ConditionExpression condExpr);

	/**
	 * @param e
	 */
	public void pushError(Exception e);

	/**
	 * @param category
	 * @param softReference
	 */
	public void addSoftReference(String category, String softReference, boolean cycleDetector);

	/**
	 * after calling validate course, the course is checked for condition and
	 * configuration errors. These validation messages are accessible via
	 * CourseEditorEnv
	 */
	void validateCourse();

	/**
	 * lists problems and errors of the course at the very moment. Should be
	 * called after validateCourse and before a next click in the course editor.
	 * 
	 * @return
	 */
	StatusDescription[] getCourseStatus();

	/**
	 * check if the given node is of type enrollment
	 * 
	 * @param nodeId
	 * @return
	 */
	boolean isEnrollmentNode(String nodeId);
	
	/**
	 * The specified visibility, accessability and score rules defined for a
	 * course node reference other couse nodes ids. This references together with
	 * the course tree structure build a directed graph. An important precondition
	 * for publishing changes is the cycle freeness of this directed graph.
	 * 
	 * @return
	 */
	Set<String> listCycles();

	/**
	 * Return CourseGroupManager for this course environment.
	 * @return CourseGroupManager for this course environment
	 */
	public CourseGroupManager getCourseGroupManager();

	// <OLATCE-91>
	/**
	 * @return the requested course nod from the current course of this courseenvironement
	 */
	public CourseNode getNode(String nodeId);
	// </OLATCE-91>
	
	/**
	 *
	 * @return The ident of the course editor root node
	 */
	public String getRootNodeId();

}
