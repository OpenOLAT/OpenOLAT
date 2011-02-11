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

package org.olat.course.editor;

import java.util.Set;

import org.olat.core.util.event.MultiUserEvent;
import org.olat.course.ICourse;

/**
* Description:<br>
* @author Felix Jost
*/
public class PublishEvent extends MultiUserEvent {
	public final static int PRE_PUBLISH =0;
	public final static int PUBLISH = 1;
	//TODO: LD: temporary introduced, for the purpose of identifying the source of the event (same VM or another cluster node)
	public final static String EVENT_IDENTIFIER = "usedForInstanceComparison";

	//FIXME:fj:c: make serializable / remove unneeded methods and info
	long pubtimestamp;
	private Long publishedCourseResId;
	
	private Set insertedCourseNodeIds;
	private Set deletedCourseNodeIds;
	private Set modifiedCourseNodeIds;

	private int state = PUBLISH;
	
	/**
	 * 
	 * @param pubtimestamp
	 * @param publishedCourse
	 * @param eventIdentifier is EVENT_IDENTIFIER
	 */
	public PublishEvent(long pubtimestamp, ICourse publishedCourse, String eventIdentifier) {
		super(eventIdentifier);
		this.pubtimestamp = pubtimestamp;
		this.publishedCourseResId = publishedCourse.getResourceableId();
	}
	
	public int getState(){
		return this.state;
	}
	
	public void setState(int publishState){
		this.state = publishState;
	}
	
	public long getLatestPublishTimestamp() {
		return pubtimestamp;
	}
	
	public Set getInsertedCourseNodeIds() {
		return insertedCourseNodeIds;
	}

	public Set getDeletedCourseNodeIds() {
		return deletedCourseNodeIds;
	}

	public Set getModifiedCourseNodeIds() {
		return modifiedCourseNodeIds;
	}
	
	/**
	 * @return Returns the key of the publishedCourse.
	 */
	public Long getPublishedCourseResId() {
		return publishedCourseResId;
	}
	
	void setInsertedCourseNodeIds(Set<String> nodeIds) { insertedCourseNodeIds = nodeIds; }
	void setDeletedCourseNodeIds(Set<String> nodeIds) { deletedCourseNodeIds = nodeIds; }
	void setModifiedCourseNodeIds(Set<String> nodeIds) { modifiedCourseNodeIds = nodeIds; }
	
	/**
	 * Use command as event identifier.
	 * @return
	 */
	public String getEventIdentifier() {
		return this.getCommand();
	}
	
}
