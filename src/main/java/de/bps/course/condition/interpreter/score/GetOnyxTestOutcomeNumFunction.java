
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */

package de.bps.course.condition.interpreter.score;

import org.olat.course.condition.interpreter.AbstractFunction;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * @author Ingmar Kroll
 */
public class GetOnyxTestOutcomeNumFunction extends AbstractFunction {
	public static final String name = "getOnyxTestOutcome";

	/**
	 * Default constructor to use the current date
	 *
	 * @param userCourseEnv
	 */
	public GetOnyxTestOutcomeNumFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {
		return defaultValue();
	}

	@Override
	protected Object defaultValue() {
		return Double.MIN_VALUE;
	}
}
