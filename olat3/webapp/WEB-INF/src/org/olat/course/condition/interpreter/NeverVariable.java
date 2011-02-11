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
* frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/

package org.olat.course.condition.interpreter;


import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 jan. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com
 */
public class NeverVariable extends AbstractVariable {

	public static final String name = "never";

	/**
	 * Default constructor to use the current date
	 * @param userCourseEnv
	 */
	public NeverVariable(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}
	
	/**
	 * @see com.neemsoft.jmep.VariableCB#getValue()
	 */
	public Object getValue() {
		return Double.POSITIVE_INFINITY;
	}

}
