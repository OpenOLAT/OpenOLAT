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
package org.olat.modules.edubase.manager;


import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.modules.edubase.BookDetails;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.edubase.EdubaseManager;
import org.olat.modules.edubase.EdubaseModule;
import org.olat.modules.edubase.model.BookDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 *
 * Initial date: 26.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdubaseManagerImpl implements EdubaseManager {

	private static final OLog log = Tracing.createLoggerFor(EdubaseManagerImpl.class);

	private static final String USER_ID_CONCAT = "#";
	private static final int TIMEOUT_5000_MILLIS = 5000;

	@Autowired
	private EdubaseModule edubaseModule;
	@Autowired
	private LoginModule loginModul;

	@Override
	public boolean validateBookId(String url) {
		String bookId = parseBookId(url);
		try {
			Integer.parseInt(bookId);
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public String parseBookId(String url) {
		String docTooken = "#doc/";
		String detailsTooken = "#details/";
		return extractBookIdAfterTooken(url, docTooken)
					.orElse(extractBookIdAfterTooken(url, detailsTooken)
					.orElse(url));
	}

	private Optional<String> extractBookIdAfterTooken(String url, String tookenBeforeId) {
		if (!StringHelper.containsNonWhitespace(url)) return Optional.empty();

		int tookenBeforeIdStart = url.indexOf(tookenBeforeId);
		if (tookenBeforeIdStart >= 0) {
			int bookIdStart = tookenBeforeIdStart + tookenBeforeId.length();
			// book id end by the next "/"
			int bookIdEnd = url.indexOf("/", bookIdStart);
			// if no slash found, use all char until the end of the String
			if (bookIdEnd < 0) {
				bookIdEnd = url.length();
			}
			return Optional.of(url.substring(bookIdStart, bookIdEnd));
		}
		return Optional.empty();
	}

	@Override
	public String getUserId(IdentityEnvironment identityEnvironment) {
		String providerName = identityEnvironment.getAttributes().get(AuthHelper.ATTRIBUTE_AUTHPROVIDER);
		AuthenticationProvider authenticationProvider = loginModul.getAuthenticationProvider(providerName);
		return new StringBuilder()
				.append(authenticationProvider.getIssuerIdentifier(identityEnvironment))
				.append(USER_ID_CONCAT)
				.append(identityEnvironment.getIdentity().getName())
				.toString();
	}

	@Override
	public String getLtiLaunchUrl(BookSection bookSection) {
		StringBuilder url = new StringBuilder();
		if (edubaseModule.getLtiLaunchUrl() != null) {
			url.append(edubaseModule.getLtiLaunchUrl());
			if (!edubaseModule.getLtiLaunchUrl().endsWith(("/"))) {
				url.append("/");
			}

			if (bookSection.getBookId() != null) {
				url.append(bookSection.getBookId());
				url.append("/");
				if (bookSection.getPageFrom() != null) {
					url.append(bookSection.getPageFrom());
				} else {
					url.append("1");
				}
			}
		}
		return url.toString();
	}

	@Override
	public String getApplicationUrl(Identity identity) {
		String readerUrl = edubaseModule.getReaderUrl();
		if (edubaseModule.isReaderUrlUnique()) {
			int protocolEnd = readerUrl.indexOf("//") + 2;
			String protocol = readerUrl.substring(0, protocolEnd);
			String host = readerUrl.substring(protocolEnd);
			// Is OpenOLAT identity ok or should it be the getUserId(identEnv)?
			String identityKey = String.valueOf(identity.getKey());
			String identityHash = UUID.nameUUIDFromBytes(identityKey.getBytes()).toString().replace("-", "");
			readerUrl = new StringBuilder()
					.append(protocol)
					.append(identityHash)
					.append("-")
					.append(WebappHelper.getInstanceId())
					.append(".")
					.append(host).toString();
		}
		return readerUrl;
	}

	@Override
	public BookDetails fetchBookDetails(String bookId) {
		BookDetails infoReponse = new BookDetailsImpl();

		RequestConfig requestConfig = RequestConfig.custom()
				  .setSocketTimeout(TIMEOUT_5000_MILLIS)
				  .setConnectTimeout(TIMEOUT_5000_MILLIS)
				  .setConnectionRequestTimeout(TIMEOUT_5000_MILLIS)
				  .build();

		String url = String.format(edubaseModule.getInfoverUrl(), bookId);
		HttpGet request = new HttpGet(url);
		request.setConfig(requestConfig);
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			String json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			ObjectMapper objectMapper = new ObjectMapper();
			infoReponse = objectMapper.readValue(json, BookDetailsImpl.class);
		} catch (SocketTimeoutException socketTimeoutException) {
			log.warn("Socket Timeout while requesting informations of the Edubase book with the id " + bookId);
		} catch (JsonParseException | EOFException noesNotExitsException) {
			log.debug("Error while requesting informations for the Edubase book with the id " + bookId
					+ ": Book does not exist.");
		} catch(Exception e) {
			log.error("", e);
		}
		
		return infoReponse;
	}

}
