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
package org.olat.course.condition.additionalconditions;
import java.util.HashMap;
import java.util.Map;

/**
 * Initial Date:  17.09.2010 <br>
 * @author blaw
 */
public class AdditionalConditionAnswerContainer {
	private static final String SEPARATOR = "|";

	public final static String RESOURCE_NAME = "AnswerContainer";
	
	private final Map<String, Object> container = new HashMap<>();
	
	public Object getAnswers(String nodeKey, Long courseId){
		return container.get(generateMapKey(nodeKey, courseId));
	}
	
	private String generateMapKey(String nodeKey, Long courseId) {
		return nodeKey + SEPARATOR + courseId;
	}

	public void insertAnswer(String nodeKey, Long courseId, Object answer){
		Object object = container.get(generateMapKey(nodeKey, courseId));
		if(!answer.equals(object)) {
			container.put(generateMapKey(nodeKey, courseId), answer);
		}
	}
	
	public Map<String, Object> getContainer() {
		return container;
	}

	public boolean isContainerEmpty() {
		return container.isEmpty();
	}

	public void removeAnswer(String nodeKey, Long courseId) {
		container.remove(generateMapKey(nodeKey, courseId));
	}

	public boolean containsAnswer(String nodeKey, Long courseId) {
		return container.containsKey(generateMapKey(nodeKey, courseId));
	}
}
