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
package org.olat.course.nodeaccess;

import org.olat.course.ICourse;
import org.olat.course.nodeaccess.model.NodeAccessTypeImpl;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 28 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface NodeAccessType {

	public String getType();
	
	public static NodeAccessType of(String type) {
		return new NodeAccessTypeImpl(type);
	}
	
	public static NodeAccessType of(ICourse course) {
		return course.getCourseConfig().getNodeAccessType();
	}
	
	public static NodeAccessType of(CourseEnvironment courseEnvironment) {
		return courseEnvironment.getCourseConfig().getNodeAccessType();
	}
	
	public static NodeAccessType of(UserCourseEnvironment userCourseEnvironment) {
		return userCourseEnvironment.getCourseEnvironment().getCourseConfig().getNodeAccessType();
	}

}
