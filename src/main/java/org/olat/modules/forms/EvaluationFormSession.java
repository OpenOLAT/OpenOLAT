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
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface EvaluationFormSession extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public Date getSubmissionDate();
	
	public Date getFirstSubmissionDate();

	public EvaluationFormSessionStatus getEvaluationFormSessionStatus();
	
	public void setEvaluationFormSessionStatus(EvaluationFormSessionStatus sessionStatus);
	
	/**
	 * The author of the evaluation
	 * 
	 * @return
	 */
	public Identity getIdentity();
	
	/**
	 * The anchor for portfolio evaluation
	 * 
	 * @return
	 */
	public PageBody getPageBody();
	
	public RepositoryEntry getFormEntry();

}
