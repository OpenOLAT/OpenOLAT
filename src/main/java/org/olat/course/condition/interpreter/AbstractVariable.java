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

package org.olat.course.condition.interpreter;

import org.olat.course.run.userview.UserCourseEnvironment;

import com.neemsoft.jmep.VariableCB;

/**
 * Initial Date:  Feb 6, 2004
 * @author Felix Jost
 *
 */
public abstract class AbstractVariable extends VariableCB {
	private UserCourseEnvironment userCourseEnv;

	/**
	 * @param userCourseEnv
	 */
	public AbstractVariable(UserCourseEnvironment userCourseEnv) {
		this.userCourseEnv = userCourseEnv;
	}
	
	/**
	 * @see com.neemsoft.jmep.VariableCB#getValue()
	 */
	public abstract Object getValue();
	
	/**
	 * @return user course environment
	 */
	public UserCourseEnvironment getUserCourseEnv() {
		return userCourseEnv;
	}
}
