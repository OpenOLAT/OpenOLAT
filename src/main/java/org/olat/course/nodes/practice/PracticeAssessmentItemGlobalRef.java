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
package org.olat.course.nodes.practice;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 10 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PracticeAssessmentItemGlobalRef extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getIdentifier();
	
	public int getLevel();

	public void setLevel(int level);

	public int getAttempts();

	public void setAttempts(int attempts);

	public int getCorrectAnswers();

	public void setCorrectAnswers(int correctAnswers);

	public int getIncorrectAnswers();

	public void setIncorrectAnswers(int incorrectAnswers);
	
	public Date getLastAttempts();

	public void setLastAttempts(Date lastAttempts);

	public Boolean getLastAttemptsPassed();

	public void setLastAttemptsPassed(Boolean lastAttemptsPassed);

	public Identity getIdentity();

}
