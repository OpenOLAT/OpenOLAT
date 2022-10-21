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
package org.olat.course.run.calendar;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.commons.calendar.ui.LinkProvider;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper.LinkProviderCreator;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;

/**
 * 
 * Initial date: 21 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseLinkProviderControllerCreator implements LinkProviderCreator {
	
	private final Long courseId;
	private final List<OLATResourceable> availableCourses;
	
	public CourseLinkProviderControllerCreator(List<OLATResourceable> courses) {
		courseId = null;
		availableCourses = courses.stream()
				.filter(Objects::nonNull)
				.map(OresHelper::clone)
				.collect(Collectors.toList());
	}
	
	public CourseLinkProviderControllerCreator(ICourse course) {
		courseId = course.getResourceableId();
		availableCourses = List.of(OresHelper.clone(course));
	}

	@Override
	public LinkProvider createController(UserRequest lureq, WindowControl lwControl) {
		return new CourseLinkProviderController(courseId, availableCourses, lureq, lwControl);
	}
}
