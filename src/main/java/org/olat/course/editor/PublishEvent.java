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

import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.course.ICourse;

/**
* Description:<br>
* @author Felix Jost
*/
public class PublishEvent extends MultiUserEvent {

	private static final long serialVersionUID = 7105017036750676773L;
	public final static int PRE_PUBLISH =0;
	public final static int PUBLISH = 1;

	public final static String EVENT_IDENTIFIER = "usedForInstanceComparison";

	private Long authorKey;
	private Long publishedCourseResId;
	
	private Set<String> insertedCourseNodeIds;
	private Set<String> deletedCourseNodeIds;
	private Set<String> modifiedCourseNodeIds;

	private int state = PUBLISH;
	
	/**
	 * 
	 * @param pubtimestamp
	 * @param publishedCourse
	 * @param eventIdentifier is EVENT_IDENTIFIER
	 */
	public PublishEvent(ICourse publishedCourse, IdentityRef author) {
		super(EVENT_IDENTIFIER);
		authorKey = author == null ? null : author.getKey();
		publishedCourseResId = publishedCourse.getResourceableId();
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int publishState){
		this.state = publishState;
	}
	
	public Set<String> getInsertedCourseNodeIds() {
		return insertedCourseNodeIds;
	}

	public Set<String> getDeletedCourseNodeIds() {
		return deletedCourseNodeIds;
	}

	public Set<String> getModifiedCourseNodeIds() {
		return modifiedCourseNodeIds;
	}
	
	public Long getAuthorKey() {
		return authorKey;
	}

	/**
	 * @return Returns the key of the publishedCourse.
	 */
	public Long getPublishedCourseResId() {
		return publishedCourseResId;
	}
	
	void setInsertedCourseNodeIds(Set<String> nodeIds) { 
		insertedCourseNodeIds = nodeIds;
	}
	
	void setDeletedCourseNodeIds(Set<String> nodeIds) {
		deletedCourseNodeIds = nodeIds;
	}
	
	void setModifiedCourseNodeIds(Set<String> nodeIds) {
		modifiedCourseNodeIds = nodeIds;
	}
	
	/**
	 * Use command as event identifier.
	 * @return
	 */
	public String getEventIdentifier() {
		return getCommand();
	}
}