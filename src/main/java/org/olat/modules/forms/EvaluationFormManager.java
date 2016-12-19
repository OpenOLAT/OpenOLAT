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

import java.math.BigDecimal;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface EvaluationFormManager {
	
	
	public EvaluationFormSession createSessionForPortfolioEvaluation(Identity identity, PageBody body, RepositoryEntry formEntry); 
	
	public EvaluationFormSession getSessionForPortfolioEvaluation(IdentityRef identity, PageBody anchor);
	
	public EvaluationFormSession changeSessionStatus(EvaluationFormSession session, EvaluationFormSessionStatus status);
	
	/**
	 * 
	 * @param identity
	 * @param anchor
	 * @return
	 */
	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(IdentityRef identity, PageBody anchor);
	
	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(List<? extends IdentityRef> identities, PageBody anchor, EvaluationFormSessionStatus status);
	
	
	public EvaluationFormResponse createResponseForPortfolioEvaluation(String responseIdentifier, BigDecimal numericalValue, String stringuifiedResponse,
			EvaluationFormResponseDataTypes dataType, EvaluationFormSession session); 
	
	public EvaluationFormResponse updateResponseForPortfolioEvaluation(BigDecimal numericalValue, String stringuifiedResponse, EvaluationFormResponse response); 

	/**
	 * Is there some sessions using this repository entry.
	 * 
	 * @param formEntry
	 * @return
	 */
	public boolean isEvaluationFormActivelyUsed(RepositoryEntryRef formEntry);
}
