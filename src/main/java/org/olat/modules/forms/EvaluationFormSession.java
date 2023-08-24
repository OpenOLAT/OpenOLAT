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

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface EvaluationFormSession extends EvaluationFormSessionRef, CreateInfo, ModifiedInfo{
	
	public Date getSubmissionDate();
	
	public Date getFirstSubmissionDate();

	public EvaluationFormSessionStatus getEvaluationFormSessionStatus();
	
	public String getEmail();
	
	public String getFirstname();
	
	public String getLastname();
	
	public String getAge();
	
	public String getGender();
	
	public String getOrgUnit();
	
	public String getStudySubject();
	
	/**
	 * Returns the participation of the session. The session does not know the
	 * participation if it is an anonymous participation.
	 *
	 * @return the participation or null
	 */
	public EvaluationFormParticipation getParticipation();
	
	public EvaluationFormSurvey getSurvey();
	
	/**
	 * @deprecated Use getParticipation().getExecutor()
	 */
	@Deprecated
	public Identity getIdentity();
	
	/**
	 * @deprecated Use the survey for the PageBody. See {@link PortfolioService#loadOrCreateSurvey(PageBody)}.
	 */
	@Deprecated
	public PageBody getPageBody();
	
	/**
	 * @deprecated Use getSurvey().getFormEntry()
	 */
	@Deprecated
	public RepositoryEntry getFormEntry();

}
