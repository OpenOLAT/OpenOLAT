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
package org.olat.modules.forms.manager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormManagerImpl implements EvaluationFormManager {
	
	@Autowired
	private EvaluationFormSessionDAO evaluationFormSessionDao;
	@Autowired
	private EvaluationFormResponseDAO evaluationFormResponseDao;
	@Autowired
	private EvaluationFormStorage evaluationFormStorage;
	

	@Override
	public EvaluationFormSession createSession(OLATResourceable ores, String subIdent, Identity identity,
			RepositoryEntry formEntry) {
		return evaluationFormSessionDao.createSession(ores, subIdent, identity, formEntry);
	}

	@Override
	public EvaluationFormSession loadSession(OLATResourceable ores, String subIdent, IdentityRef identity) {
		return evaluationFormSessionDao.loadSession(ores, subIdent, identity);
	}

	@Override
	public boolean hasSessions(OLATResourceable ores, String subIdent) {
		return evaluationFormSessionDao.hasSessions(ores, subIdent);
	}

	@Override
	public EvaluationFormSession createSessionForPortfolioEvaluation(Identity identity, PageBody body, RepositoryEntry formEntry) {
		return evaluationFormSessionDao.createSessionForPortfolio(identity, body, formEntry);
	}

	@Override
	public EvaluationFormSession getSessionForPortfolioEvaluation(IdentityRef identity, PageBody anchor) {
		return evaluationFormSessionDao.getSessionForPortfolioEvaluation(identity, anchor);
	}

	@Override
	public EvaluationFormSession changeSessionStatus(EvaluationFormSession session, EvaluationFormSessionStatus status) {
		return evaluationFormSessionDao.changeStatusOfSession(session, status);
	}

	@Override
	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(IdentityRef identity, PageBody anchor) {
		return evaluationFormResponseDao.getResponsesFromPortfolioEvaluation(identity, anchor);
	}

	@Override
	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(List<? extends IdentityRef> identities, PageBody anchor, EvaluationFormSessionStatus status) {
		return evaluationFormResponseDao.getResponsesFromPortfolioEvaluation(identities, anchor, status);
	}

	@Override
	public EvaluationFormResponse createResponseForPortfolioEvaluation(String responseIdentifier, BigDecimal numericalValue, String stringuifiedResponse,
			EvaluationFormSession session) {
		return evaluationFormResponseDao.createResponse(responseIdentifier, numericalValue, stringuifiedResponse, null, session);
	}

	@Override
	public EvaluationFormResponse createResponseForPortfolioEvaluation(String responseIdentifier, File file,
			String filename, EvaluationFormSession session) throws IOException {
		Path relativePath = evaluationFormStorage.save(file, filename);
		return evaluationFormResponseDao.createResponse(responseIdentifier, null, filename, relativePath,
				session);
	}

	@Override
	public EvaluationFormResponse createStringResponse(String responseIdentifier, EvaluationFormSession session,
			String value) {
		return evaluationFormResponseDao.createResponse(responseIdentifier, null, value, null, session);
	}

	@Override
	public EvaluationFormResponse createNumericalResponse(String responseIdentifier, EvaluationFormSession session,
			BigDecimal value) {
		return evaluationFormResponseDao.createResponse(responseIdentifier, value, value.toPlainString(), null, session);
	}

	@Override
	public EvaluationFormResponse createNoResponse(String responseIdentifier, EvaluationFormSession session) {
		return evaluationFormResponseDao.createNoResponse(responseIdentifier, session);
	}

	@Override
	public EvaluationFormResponse updateNumericalResponse(EvaluationFormResponse response, BigDecimal value) {
		return evaluationFormResponseDao.updateResponse(value, value.toPlainString(), null, response);
	}

	@Override
	public EvaluationFormResponse updateResponseForPortfolioEvaluation(BigDecimal numericalValue, String stringuifiedResponse, EvaluationFormResponse response) {
		return evaluationFormResponseDao.updateResponse(numericalValue, stringuifiedResponse, null, response);
	}

	@Override
	public EvaluationFormResponse updateResponseForPortfolioEvaluation(File file, String filename,
			EvaluationFormResponse response) throws IOException {
		if (response.getFileResponse() != null) {
			evaluationFormStorage.delete(response.getFileResponse());
		}
		Path relativePath = null;
		String filenameToSave = null;
		if (file != null) {
			relativePath = evaluationFormStorage.save(file, filename);
			filenameToSave = filename;
		}
		return evaluationFormResponseDao.updateResponse(null, filenameToSave, relativePath, response);
	}

	@Override
	public EvaluationFormResponse updateResponse(EvaluationFormResponse response, String stringValue) {
		return evaluationFormResponseDao.updateResponse(null, stringValue, null, response);
	}

	@Override
	public EvaluationFormResponse updateNoResponse(EvaluationFormResponse response) {
		return evaluationFormResponseDao.updateNoResponse(response);
	}

	@Override
	public EvaluationFormResponse loadResponse(String responseIdentifier, EvaluationFormSession session) {
		return evaluationFormResponseDao.loadResponse(responseIdentifier, session);
	}

	@Override
	public List<EvaluationFormResponse> loadResponses(String responseIdentifier, EvaluationFormSession session) {
		return evaluationFormResponseDao.loadResponses(responseIdentifier, session);
	}

	@Override
	public File loadResponseFile(EvaluationFormResponse response) {
		if (response == null || response.getFileResponse() == null) return null;
		
		return evaluationFormStorage.load(response.getFileResponse());
	}

	@Override
	public VFSLeaf loadResponseLeaf(EvaluationFormResponse response) {
		if (response == null || response.getFileResponse() == null) return null;
		
		return evaluationFormStorage.resolve(response.getFileResponse());
	}

	@Override
	public void deleteResponse(Long key) {
		evaluationFormResponseDao.deleteResponses(Collections.singletonList(key));
	}

	@Override
	public void deleteResponses(List<Long> keys) {
		evaluationFormResponseDao.deleteResponses(keys);
	}

	@Override
	public boolean isEvaluationFormActivelyUsed(RepositoryEntryRef formEntry) {
		return evaluationFormSessionDao.isInUse(formEntry);
	}

}
