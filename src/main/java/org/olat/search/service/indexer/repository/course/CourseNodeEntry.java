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
package org.olat.search.service.indexer.repository.course;

import org.olat.core.id.context.StateEntry;
import org.olat.course.nodes.CourseNode;

/**
 * Hack to pass the course node to the sub-indexer as a state entry
 * without reloading the whole course.
 * 
 * Initial date: 12 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeEntry implements StateEntry {

	private static final long serialVersionUID = 272047155723467386L;
	private final CourseNode courseNode;
	
	public CourseNodeEntry(CourseNode courseNode) {
		this.courseNode = courseNode;
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}

	@Override
	public CourseNodeEntry clone() {
		return new CourseNodeEntry(courseNode);
	}
}
