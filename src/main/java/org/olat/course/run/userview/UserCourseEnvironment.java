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
* <p>
*/ 

package org.olat.course.run.userview;

import java.util.Map;

import org.olat.core.id.IdentityEnvironment;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreAccounting;

/**
 * @author Felix Jost
 *
 */
public interface UserCourseEnvironment {
	/**
	 * @return Returns the courseEnvironment.
	 */
	public CourseEnvironment getCourseEnvironment();
	/**
	 * 
	 * @return returns a view to the course in the editor
	 */
	public CourseEditorEnv getCourseEditorEnv();
	
	public ConditionInterpreter getConditionInterpreter();
	
	public IdentityEnvironment getIdentityEnvironment();
	
	public ScoreAccounting getScoreAccounting();
	
	/**
	 * @return a temporary map (lives as long as the user is visiting the course)
	 * @param owner the owning class
	 * @param key the key
	 * 
	 * owner's classname and the key form a composite unique key / namespace
	 */
	public Map getTempMap(Class owner, String key);
	
	
	//TODO: add a method like isCourseAdmin() to be offered in a conditioninterpreter function
	
}