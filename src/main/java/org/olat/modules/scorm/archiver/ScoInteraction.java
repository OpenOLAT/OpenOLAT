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
package org.olat.modules.scorm.archiver;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Description:<br>
 * Hold a single interaction of the sco's datamodel
 * 
 * <P>
 * Initial Date:  17 august 2009 <br>
 * @author srosse
 */
public class ScoInteraction {
	private final int position;
	private String interactionId;
	private String result;
	private String studentResponse;
	private String correctResponse;
	private final List<String> objectiveIds = new ArrayList<>(2);
	
	public ScoInteraction(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}

	public String getInteractionId() {
		return interactionId;
	}
	
	public void setInteractionId(String interactionId) {
		this.interactionId = interactionId;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public String getStudentResponse() {
		return studentResponse;
	}

	public void setStudentResponse(String studentResponse) {
		this.studentResponse = studentResponse;
	}

	public String getCorrectResponse() {
		return correctResponse;
	}

	public void setCorrectResponse(String correctResponse) {
		this.correctResponse = correctResponse;
	}

	public List<String> getObjectiveIds() {
		return objectiveIds;
	}
	
	public String getObjectiveId(int index) {
		if(index > -1 && objectiveIds.size() > index) {
			return objectiveIds.get(index);
		}
		return null;
	}
}
