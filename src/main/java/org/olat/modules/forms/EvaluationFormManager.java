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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
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
	
	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(IdentityRef identity, PageBody anchor);
	
	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(List<? extends IdentityRef> identities, PageBody anchor, EvaluationFormSessionStatus status);
	
	
	public EvaluationFormResponse createResponseForPortfolioEvaluation(String responseIdentifier,
			BigDecimal numericalValue, String stringuifiedResponse, EvaluationFormSession session);

	public EvaluationFormResponse createResponseForPortfolioEvaluation(String responseIdentifier, File file,
			String filename, EvaluationFormSession session) throws IOException;
	
	public EvaluationFormResponse createStringResponse(String responseIdentifier, EvaluationFormSession session, String value);
	
	public EvaluationFormResponse createNumericalResponse(String responseIdentifier, EvaluationFormSession session, BigDecimal value);
	
	public EvaluationFormResponse createNoResponse(String responseIdentifier, EvaluationFormSession session);

	public EvaluationFormResponse updateNumericalResponse(EvaluationFormResponse response, BigDecimal value);
	
	public EvaluationFormResponse updateResponseForPortfolioEvaluation(BigDecimal numericalValue,
			String stringuifiedResponse, EvaluationFormResponse response);
	
	public EvaluationFormResponse updateResponse(EvaluationFormResponse response, String stringValue);

	public EvaluationFormResponse updateResponseForPortfolioEvaluation(File file, String filename,
			EvaluationFormResponse response) throws IOException;
	
	public EvaluationFormResponse updateNoResponse(EvaluationFormResponse response);
	
	public EvaluationFormResponse loadResponse(String responseIdentifier, EvaluationFormSession session);
	
	public List<EvaluationFormResponse> loadResponses(String responseIdentifier, EvaluationFormSession session);
	
	public File loadResponseFile(EvaluationFormResponse response);
	
	public VFSLeaf loadResponseLeaf(EvaluationFormResponse response);
	
	public void deleteResponse(Long key);
	
	public void deleteResponses(List<Long> keys);

	/**
	 * Is there some sessions using this repository entry.
	 * 
	 * @param formEntry
	 * @return
	 */
	public boolean isEvaluationFormActivelyUsed(RepositoryEntryRef formEntry);

}
