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
package org.olat.course.learningpath;

/**
 * Configuration how a type of course node can be configured.
 * 
 * Initial date: 20 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface LearningPathEditConfigs {
	
	public boolean isTriggerNodeVisited();
	
	public boolean isTriggerConfirmed();
	
	public boolean isTriggerScore();
	
	public boolean isTriggerPassed();
	
	public boolean isTriggerStatusInReview();
	
	public boolean isTriggerStatusDone();
	
	public boolean isTriggerNodeCompleted();
	
	public LearningPathTranslations getTranslations();
	
	public static LearningPathEditConfigsBuilder builder() {
		return new LearningPathEditConfigsBuilder();
	}

}
