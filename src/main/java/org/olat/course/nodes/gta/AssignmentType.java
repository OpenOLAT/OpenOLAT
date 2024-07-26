/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta;

import org.olat.course.nodes.GTACourseNode;

/**
 * 
 * Initial date: 25 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum AssignmentType {
	
	SAME_TASK(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_SAME_TASK),
	OTHER_TASK(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_OTHER_TASK),
	RANDOM(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_RANDOM);
	
	private final String key;
	
	private AssignmentType(String key) {
		this.key = key;
	}
	
	public String key() {
		return key;
	}
	
	public static AssignmentType keyOf(String key) {
		for(AssignmentType type:values()) {
			if(type.key().equals(key)) {
				return type;
			}
		}
		return null;
	}
}
