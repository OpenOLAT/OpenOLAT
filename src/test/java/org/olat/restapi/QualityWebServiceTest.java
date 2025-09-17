/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import static org.olat.test.JunitTestHelper.random;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.EvaluationFormHandler;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Sep 17, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class QualityWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private EvaluationFormHandler evaluationFormHandler;
	
	@Test
	public void getAnalysisReport_noAnalysisRigths() throws IOException, URISyntaxException {
		String userName = random();
		String userPw = random();
		JunitTestHelper.createAndPersistIdentityAsUser(userName, userPw);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(userName, userPw);
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("qm")
				.path("analysis")
				.path("1")
				.build();
		HttpGet method = conn.createGet(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void getAnalysisReport_formNotFound() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("qm")
				.path("analysis")
				.path("-1")
				.build();
		HttpGet method = conn.createGet(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void getAnalysisReport() throws IOException, URISyntaxException {
		Identity administrator = JunitTestHelper.getDefaultAdministrator();
		Identity executor = JunitTestHelper.getDefaultActor();
		RepositoryEntry formEntry = evaluationFormHandler.createResource(administrator, random(), null, null, JunitTestHelper.getDefaultOrganisation(), Locale.ENGLISH);
		
		QualityDataCollection dataCollection = qualityService.createDataCollection(List.of(JunitTestHelper.getDefaultOrganisation()), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, List.of(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0)).build();
		EvaluationFormSession session = evaluationFormManager.createSession(participations.get(0));
		evaluationFormManager.finishSession(session);
		qualityService.updateDataCollectionStatus(dataCollection, QualityDataCollectionStatus.FINISHED);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("qm")
				.path("analysis")
				.path(formEntry.getKey().toString())
				.build();
		HttpGet method = conn.createGet(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		String content = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
		Assert.assertFalse(content.isEmpty());
	}

}
