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
package org.olat.course.run;

import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 
 * Initial date: 18 Aug 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
public class InfoCourseNode {
	
	public static final InfoCourseNode of(CourseNode courseNode) {
		if (courseNode != null) {
			InfoCourseNode info = new InfoCourseNode();
			info.type = courseNode.getType();
			if (StringHelper.containsNonWhitespace(courseNode.getShortTitle())) {
				info.shortTitle = courseNode.getShortTitle();
			}
			if (StringHelper.containsNonWhitespace(courseNode.getLongTitle())) {
				info.longTitle = courseNode.getLongTitle();
			}
			if (StringHelper.containsNonWhitespace(courseNode.getDescription())) {
				info.description = courseNode.getDescription();
			}
			info.displayOption = courseNode.getDisplayOption();
			return info;
		}
		return null;
	}

	private String type;
	private String shortTitle;
	private String longTitle;
	private String description;
	private String displayOption;
	
	private InfoCourseNode() {
		//
	}

	public String getType() {
		return type;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public String getLongTitle() {
		return longTitle;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayOption() {
		return displayOption;
	}
	
}
