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
package org.olat.modules.forms;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface EvaluationFormParticipation extends EvaluationFormParticipationRef, CreateInfo, ModifiedInfo {
	
	/**
	 *
	 * @return the survey
	 */
	public EvaluationFormSurvey getSurvey();
	
	/**
	 *
	 * @return the unique identifier of the participation
	 */
	public EvaluationFormParticipationIdentifier getIdentifier();
	
	/**
	 *
	 * @return the status
	 */
	public EvaluationFormParticipationStatus getStatus();
	
	/**
	 * Whether it is a anonymous participation or not. An anonymous participation
	 * has no relation to a session if the status is done.
	 *
	 * @return whether it is an anonymous participation or not
	 */
	public boolean isAnonymous();
	
	public void setAnonymous(boolean anonymous);

	/**
	 * The executor of the participation. A participation must not have an executor.
	 *
	 * @return the executor or null
	 */
	public Identity getExecutor();
}
