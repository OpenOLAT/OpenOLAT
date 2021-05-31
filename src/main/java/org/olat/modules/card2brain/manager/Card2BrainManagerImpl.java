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
package org.olat.modules.card2brain.manager;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.ims.lti.LTIManager;
import org.olat.modules.card2brain.Card2BrainManager;
import org.olat.modules.card2brain.Card2BrainModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Initial date: 20.04.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class Card2BrainManagerImpl implements Card2BrainManager {

	private static final Logger log = Tracing.createLoggerFor(Card2BrainManagerImpl.class);

	@Autowired
	private Card2BrainModule card2brainModule;
	@Autowired
	private LTIManager ltiManager;
	@Autowired
	private HttpClientService httpClientService;

	private ObjectMapper mapper;

	public Card2BrainManagerImpl() {
		mapper = new ObjectMapper();
	}

	@Override
	public boolean checkSetOfFlashcards(String alias) {
		boolean setOfFlashcardExists = false;

		String url = String.format(card2brainModule.getPeekViewUrl(), alias);

		HttpGet request = new HttpGet(url);
		try(CloseableHttpClient httpclient = httpClientService.createHttpClient();
				CloseableHttpResponse response = httpclient.execute(request);) {
			setOfFlashcardExists = isSetOfFlashcardExisting(response);
		} catch(Exception e) {
			log.error("", e);
		}

		log.info(new StringBuilder("Check card2brain set of flaschcards (").append(url).append("): ").append(setOfFlashcardExists).toString());
		return setOfFlashcardExists;
	}

	/**
	 * evaluates if a set of flashcards is existing according to a http response
	 * @param response the http response
	 * @return true if it is existing
	 */
	protected boolean isSetOfFlashcardExisting(HttpResponse response) {
		boolean isSetOfFlashcardExisting = false;
		// The response of a non existent set of flashcards returns with an empty body
		try {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && EntityUtils.toByteArray(response.getEntity()).length > 0) {
				isSetOfFlashcardExisting = true;
			}
		} catch (Exception e) {
			// nothing to do: isSetOfFlashcardExisting is false.
		}
		return isSetOfFlashcardExisting;
	}

	@Override
	public Card2BrainVerificationResult checkEnterpriseLogin(String url, String key, String secret) {
		Card2BrainVerificationResult card2BrainValidationResult = null;

		try {
			Map<String,String> signedPros = ltiManager.sign(null, url, key, secret);
			String content = ltiManager.post(signedPros, url);
			card2BrainValidationResult = mapper.readValue(content, Card2BrainVerificationResult.class);
		} catch (JsonParseException jsonParseException) {
			// ignore and return null
		} catch (Exception e) {
			log.error("", e);
		}

		return card2BrainValidationResult;
	}

	@Override
	public String parseAlias(String alias) {
		String parsedString = alias.endsWith("/editor")? alias.substring(0, alias.length()-7): alias;
		parsedString = parsedString.replace("https://card2brain.ch/box/", "");
		parsedString = parsedString.replace(" ", "_");
		return parsedString;
	}

}
