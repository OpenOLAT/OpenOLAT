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
package org.olat.modules.adobeconnect.manager;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.AdobeConnectCourseNode;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.model.AdobeConnectError;
import org.olat.modules.adobeconnect.model.AdobeConnectErrorCodes;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectPermission;
import org.olat.modules.adobeconnect.model.AdobeConnectPrincipal;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.model.BreezeSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractAdobeConnectProvider implements AdobeConnectSPI {
	
	private static final Logger log = Tracing.createLoggerFor(AbstractAdobeConnectProvider.class);

	private static final String PREFIX = "olat-";
	public static final String COOKIE = "BREEZESESSION=";
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
	private static final String DATE_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private static final SimpleDateFormat DATE_ISO_FORMATTER = new SimpleDateFormat(DATE_ISO_FORMAT);

	private static final String SHARED_TEMPLATES_FOLDER = "shared-meeting-templates";
	private static final String MY_TEMPLATES_FOLDER = "my-meeting-templates";

	private String adminFolderScoId;
	private BreezeSession currentSession;
	
	@Autowired
	protected AdobeConnectModule adobeConnectModule;
	@Autowired
	protected HttpClientService httpClientService;
	
	/**
	 * https://example.com/api/xml?action=sco-update
	 * 		&type=meeting
	 * 		&name=August%20All%20Hands%20Meeting
	 * 		&description=For all company employees
	 * 		&folder-id=2006258750 to create a sco OR &sco-id=23678 to update an sco
	 * 		&date-begin=2006-08-01T09:00
	 * 		&date-end=2006-08-01T17:00
	 * 		&url-path=august
	 * 		&lang=en
	 * 		&source-sco-id=2006349744
	 * @return
	 */
	@Override
	public AdobeConnectSco createScoMeeting(String name, String description, String folderScoId,
			String templateId, Date startDate, Date endDate, Locale locale, AdobeConnectErrors errors) {
		if(folderScoId == null) {
			folderScoId = adminFolderScoId(errors);
		}
		if(folderScoId == null || errors.hasErrors()) {
			return null;
		}

		String lang = getLanguage(locale);
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-update")
			.queryParam("type", "meeting")
			.queryParam("name", name)
			.queryParam("folder-id", folderScoId)
			.queryParam("lang", lang);
		if(StringHelper.containsNonWhitespace(description)) {
			builder.queryParam("description", description);
		}
		if(StringHelper.containsNonWhitespace(templateId)) {
			builder.queryParam("source-sco-id", templateId);
		}
		if(startDate != null) {
			builder.queryParam("date-begin", formatDate(startDate));
		}
		if(endDate != null) {
			builder.queryParam("date-end", formatDate(endDate));
		}
		
		List<AdobeConnectSco> createdScos = sendScoRequest(builder, errors);
		return createdScos == null || createdScos.isEmpty() ? null : createdScos.get(0);
	}
	
	private String getLanguage(Locale locale) {
		if(locale != null && isCompatibleLanguage(locale.getLanguage())) {
			return locale.getLanguage();
		}
		Locale defLocale = I18nModule.getDefaultLocale();
		if(isCompatibleLanguage(defLocale.getLanguage())) {
			return defLocale.getLanguage();
		}
		return "en";
	}
	
	private boolean isCompatibleLanguage(String language) {
		return "fr".equals(language) || "en".equals(language) || "de".equals(language)
				|| "ja".equals(language) ||"ko".equals(language);
	}

	@Override
	public AdobeConnectSco createFolder(String name, AdobeConnectErrors errors) {
		String folderScoId = adminFolderScoId(errors);
		if(folderScoId == null || errors.hasErrors()) {
			return null;
		}
		
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-update")
			.queryParam("type", "folder")
			.queryParam("name", name)
			.queryParam("folder-id", folderScoId);
		List<AdobeConnectSco> createdScos = sendScoRequest(builder, errors);
		return createdScos == null || createdScos.isEmpty() ? null : createdScos.get(0);
	}
	
	@Override
	public List<AdobeConnectSco> getFolderByName(String name, AdobeConnectErrors errors) {
		String folderScoId = adminFolderScoId(errors);
		if(folderScoId == null || errors.hasErrors()) {
			return new ArrayList<>();
		}
		
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-contents")
			.queryParam("sco-id", folderScoId)
			.queryParam("filter-type", "folder")
			.queryParam("filter-name", name);
		return sendScoRequest(builder, errors);
	}

	@Override
	public List<AdobeConnectSco> getMeetingByName(String name, AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-search-by-field")
			.queryParam("query", PREFIX + name)
			.queryParam("filter-type", "meeting");
		return sendScoRequest(builder, errors);
	}

	@Override
	public boolean updateScoMeeting(String scoId, String name, String description, String templateId,
			Date startDate, Date endDate, AdobeConnectErrors errors) {
		String folderScoId = adminFolderScoId(errors);
		if(folderScoId == null || errors.hasErrors()) {
			return false;
		}
		
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-update")
			.queryParam("type", "meeting")
			.queryParam("name", name)
			.queryParam("sco-id", scoId)
			.queryParam("folder-id", folderScoId);
		if(StringHelper.containsNonWhitespace(description)) {
			builder.queryParam("description", description);
		}
		if(StringHelper.containsNonWhitespace(templateId)) {
			builder.queryParam("source-sco-id", templateId);
		}
		if(startDate != null) {
			builder.queryParam("date-begin", formatDate(startDate));
		}
		if(endDate != null) {
			builder.queryParam("date-end", formatDate(endDate));
		}

		boolean ok = false;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200 || statusCode == 201) {
					ok = AdobeConnectUtils.isStatusOk(response.getEntity());
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return ok;
	}

	@Override
	public List<AdobeConnectSco> getTemplates() {
		AdobeConnectErrors error = new AdobeConnectErrors();
		List<AdobeConnectSco> shortCuts = getShortCuts(error);
		
		List<AdobeConnectSco> templates = new ArrayList<>();
		if(shortCuts != null) {
			for(AdobeConnectSco shortCut:shortCuts) {
				if(SHARED_TEMPLATES_FOLDER.equals(shortCut.getType())
						|| MY_TEMPLATES_FOLDER.equals(shortCut.getType())) {
					List<AdobeConnectSco> scos = getScoContents(shortCut.getScoId(), error);
					if(scos != null && !scos.isEmpty()) {
						templates.addAll(scos);
					}
				}
			}
		}
		
		return templates;
	}
	
	@Override
	public List<AdobeConnectSco> getRecordings(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		return getScoContents(meeting.getScoId(), error);
	}

	protected List<AdobeConnectSco> getScoContents(String scoId, AdobeConnectErrors error) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-contents")
			.queryParam("sco-id", scoId);
		return sendScoRequest(builder, error);
	}
	
	@Override
	public AdobeConnectSco getScoMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		if(meeting == null || !StringHelper.containsNonWhitespace(meeting.getScoId())) return null;
		
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-info")
			.queryParam("sco-id", meeting.getScoId());
		List<AdobeConnectSco> scos = sendScoRequest(builder, error);
		return scos == null || scos.isEmpty() ? null : scos.get(0);
	}
	
	@Override
	public boolean deleteScoMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		if(!StringHelper.containsNonWhitespace(meeting.getScoId())) {
			return true;// nothing to do
		}

		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-delete")
			.queryParam("sco-id", meeting.getScoId());

		boolean ok = false;
		HttpGet get = createAdminMethod(builder, error);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode >= 200 && statusCode < 400) {
					ok = AdobeConnectUtils.isStatusOk(response.getEntity());
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return ok;
	}
	
	/**
	 * https://example.com/api/xml?action=permissions-update&acl-id=2007018414
	 * 		&principal-id=public-access&permission-id=view-hidden
	 */
	@Override
	public boolean setPermissions(String scoId, boolean allAccess, AdobeConnectErrors error) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "permissions-update")
			.queryParam("principal-id", "public-access")
			.queryParam("acl-id", scoId);
		if(allAccess) {
			builder.queryParam("permission-id", "view-hidden");
		} else {
			builder.queryParam("permission-id", "remove");
		}

		boolean ok = false;
		HttpGet get = createAdminMethod(builder, error);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode >= 200 && statusCode < 400) {
					ok = true;
				}
				EntityUtils.consume(response.getEntity());
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return ok;
	}
	
	/**
	 * https://example.com/api/xml?action=permissions-update
	 * 		&principal-id=2006258745
	 * 		&acl-id=2007018414 (the sco-id)
	 * 		&permission-id=host
	 * @param scoId
	 * @param error
	 */
	@Override
	public boolean setMember(String scoId, String principalId, String permission, AdobeConnectErrors error) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "permissions-update")
			.queryParam("principal-id", principalId)
			.queryParam("acl-id", scoId)
			.queryParam("permission-id", permission);
		
		boolean ok = false;
		HttpGet get = createAdminMethod(builder, error);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode >= 200 && statusCode < 400) {
					ok = true;
				}
				EntityUtils.consume(response.getEntity());
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return ok;
	}
	
	/**
	 * https://server/lmsapi/xml?action=permissions-info
	 *    &acl-id=integer
	 *    &principal-id=integer
	 *    &filter-permission-id=value
	 *    &session=SessionCookie
	 */
	@Override
	public boolean isMember(String scoId, String principalId, String permission, AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "permissions-info")
			.queryParam("principal-id", principalId)
			.queryParam("acl-id", scoId)
			.queryParam("filter-permission-id", permission);

		boolean ok = false;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode >= 200 && statusCode < 400) {
					List<AdobeConnectPermission> permissions = parsePermissions(response.getEntity(), errors);
					ok = permissions != null && !permissions.isEmpty();
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return ok;
	}
	
	protected static final String formatDate(Date date) {
		synchronized(DATE_FORMATTER) {
			return DATE_FORMATTER.format(date);
		}
	}
	
	protected static final Date parseIsoDate(String val) {
		if(!StringHelper.containsNonWhitespace(val)) return null;
		
		try {
			synchronized(DATE_ISO_FORMATTER) {
				return DATE_ISO_FORMATTER.parse(val);
			}
		} catch (ParseException e) {
			log.error("", e);
			return null;
		}
	}
	
	protected String adminFolderScoId(AdobeConnectErrors error) {
		if(adminFolderScoId != null) {
			return adminFolderScoId;
		}

		String meetingsScoId = null;
		String myMeetingsScoId = null;
		List<AdobeConnectSco> shortCuts = getShortCuts(error);
		if(shortCuts != null) {
			for(AdobeConnectSco shortCut:shortCuts) {
				if("meetings".equals(shortCut.getType())) {
					meetingsScoId = shortCut.getScoId();
				} else if("my-meetings".equals(shortCut.getType())) {
					myMeetingsScoId = shortCut.getScoId();
				}
			}
		}
		adminFolderScoId = myMeetingsScoId == null ? meetingsScoId : myMeetingsScoId;
		return adminFolderScoId;
	}
	
	protected List<AdobeConnectSco> getShortCuts(AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "sco-shortcuts");

		List<AdobeConnectSco> shortCuts = null;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200) {
					shortCuts = parseScos(response.getEntity(), errors);
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return shortCuts;
	}

	/**
	 * https://server/lmsapi/xml?action=common-info
	 * 
	 * @return
	 */
	@Override
	public AdobeConnectPrincipal adminCommonInfo(AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "common-info");
		
		AdobeConnectPrincipal user = null;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200) {
					user = parseCommonInfo(response.getEntity(), errors);
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return user;
	}
	
	protected List<AdobeConnectSco> parseScos(HttpEntity entity, AdobeConnectErrors errors) {
		List<AdobeConnectSco> scos = new ArrayList<>();
		try {
			Document doc = AdobeConnectUtils.getDocumentFromEntity(entity);
			if(AdobeConnectUtils.isStatusOk(doc)) {
				NodeList nodes = doc.getElementsByTagName("sco");
				int numOfNodes = nodes.getLength();
				for(int i=0; i<numOfNodes; i++) {
					Element sco = (Element)nodes.item(i);

					AdobeConnectSco connectSco = new AdobeConnectSco();
					connectSco.setScoId(sco.getAttribute("sco-id"));
					connectSco.setFolderId(sco.getAttribute("folder-id"));
					connectSco.setType(sco.getAttribute("type"));
					connectSco.setIcon(sco.getAttribute("icon"));
					String urlPath = AdobeConnectUtils.getFirstElementValue(sco, "url-path");
					connectSco.setUrlPath(urlPath);
					String name = AdobeConnectUtils.getFirstElementValue(sco, "name");
					connectSco.setName(name);
					connectSco.setDateBegin(parseIsoDate(AdobeConnectUtils.getFirstElementValue(sco, "date-begin")));
					connectSco.setDateEnd(parseIsoDate(AdobeConnectUtils.getFirstElementValue(sco, "date-end")));
					connectSco.setDateCreated(parseIsoDate(AdobeConnectUtils.getFirstElementValue(sco, "date-created")));
					connectSco.setDateModified(parseIsoDate(AdobeConnectUtils.getFirstElementValue(sco, "date-modified")));
					
	            	scos.add(connectSco);
	            }
				AdobeConnectUtils.print(doc);
			} else {
				AdobeConnectUtils.print(doc);
				AdobeConnectUtils.error(doc, errors);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return scos;
	}
	


	/**
	 * https://server/lmsapi/xml?action=login
	 *    &login=email
	 *    &password=string
	 *    &session=SessionCookie
	 */
	protected BreezeSession getAdminSession(AdobeConnectErrors errors) {
		if(currentSession != null && currentSession.isValid()) {
			return currentSession;
		}
		
		UriBuilder builderc = adobeConnectModule.getAdobeConnectUriBuilder();
		URI uric = builderc
			.queryParam("action", "common-info")
			.build();

		BreezeSession session = null;
		HttpGet getInfo = new HttpGet(uric);
		try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
			CloseableHttpResponse response = httpClient.execute(getInfo)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				session = AdobeConnectUtils.getBreezeSession(response);
			}
			EntityUtils.consumeQuietly(response.getEntity());
		} catch(Exception e) {
			log.error("", e);
		}
		
		if(session == null) {
			errors.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
			return null;
		}
		
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder = builder
			.queryParam("action", "login")
			.queryParam("login", adobeConnectModule.getAdminLogin())
			.queryParam("password", adobeConnectModule.getAdminPassword())
			.queryParam("session", session.getSession());
		if(StringHelper.containsNonWhitespace(adobeConnectModule.getAccountId())) {
			builder = builder.queryParam("account-id", adobeConnectModule.getAccountId());
		}
		URI uri = builder
			.build();
		
		HttpGet getLogin = new HttpGet(uri);
		try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
			CloseableHttpResponse response = httpClient.execute(getLogin)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				BreezeSession loginSession = AdobeConnectUtils.getBreezeSessionIfOk(response, session);
				if(loginSession != null) {// OK
					session = loginSession;
					currentSession = loginSession;
				} else {
					errors.append(new AdobeConnectError(AdobeConnectErrorCodes.adminDenied));
				}
			}
			EntityUtils.consumeQuietly(response.getEntity());
		} catch(Exception e) {
			log.error("", e);
		}
		return session;
	}
	
	protected List<AdobeConnectSco> sendScoRequest(UriBuilder builder, AdobeConnectErrors errors) {
		List<AdobeConnectSco> scos = null;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200 || statusCode == 201) {
					scos = parseScos(response.getEntity(), errors);
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return scos;
	}
	
	protected List<AdobeConnectPrincipal> sendPrincipalRequest(UriBuilder builder, AdobeConnectErrors errors) {
		List<AdobeConnectPrincipal> users = null;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200 || statusCode == 201) {
					users = parsePrincipals(response.getEntity(), errors);
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return users;
	}
	
	protected List<AdobeConnectPrincipal> parsePrincipals(HttpEntity entity, AdobeConnectErrors errors) {
		List<AdobeConnectPrincipal> users = new ArrayList<>();
		try {
			Document doc = AdobeConnectUtils.getDocumentFromEntity(entity);
			AdobeConnectUtils.print(doc);
			if(AdobeConnectUtils.isStatusOk(doc)) {
				NodeList userList = doc.getElementsByTagName("principal");
				int numOfElements = userList.getLength();
				for(int i=0; i<numOfElements; i++) {
					Element userEl = (Element)userList.item(i);
					AdobeConnectPrincipal user = new AdobeConnectPrincipal();
					user.setPrincipalId(userEl.getAttribute("principal-id"));
					users.add(user);
				}
			} else {
				AdobeConnectUtils.error(doc, errors);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return users;
	}
	
	protected AdobeConnectPrincipal parseCommonInfo(HttpEntity entity, AdobeConnectErrors errors) {
		AdobeConnectPrincipal user = null;
		try {
			Document doc = AdobeConnectUtils.getDocumentFromEntity(entity);
			AdobeConnectUtils.print(doc);
			if(AdobeConnectUtils.isStatusOk(doc)) {
				NodeList userList = doc.getElementsByTagName("user");
				if(userList.getLength() == 1) {
					Element userEl = (Element)userList.item(0);
					user = new AdobeConnectPrincipal();
					user.setPrincipalId(userEl.getAttribute("user-id"));
				}
			} else {
				AdobeConnectUtils.error(doc, errors);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return user;
	}
	
	protected List<AdobeConnectPermission> parsePermissions(HttpEntity entity, AdobeConnectErrors errors) {
		List<AdobeConnectPermission> permissions = new ArrayList<>();
		try {
			Document doc = AdobeConnectUtils.getDocumentFromEntity(entity);
			AdobeConnectUtils.print(doc);
			if(AdobeConnectUtils.isStatusOk(doc)) {
				NodeList permissionList = doc.getElementsByTagName("permission");
				int numOfElements = permissionList.getLength();
				for(int i=0; i<numOfElements; i++) {
					Element permissionEl = (Element)permissionList.item(i);
					AdobeConnectPermission permission = new AdobeConnectPermission();
					permission.setAclId(permissionEl.getAttribute("acl-id"));
					permission.setPrincipalId(permissionEl.getAttribute("principal-id"));
					permission.setPermissionId(permissionEl.getAttribute("permission-id"));
					permissions.add(permission);
				}
			} else if(AdobeConnectUtils.isStatusNoData(doc)) {
				// ok, there isn't any permissions
			} else {
				AdobeConnectUtils.error(doc, errors);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return permissions;
	}
	
	protected HttpGet createAdminMethod(UriBuilder builder, AdobeConnectErrors errors) {
		BreezeSession session = getAdminSession(errors);
		HttpGet get = null;
		if(session != null) {
			builder.queryParam("session", session.getSession());
			get = new HttpGet(builder.build());
			get.setHeader(new BasicHeader("Cookie", COOKIE + session.getSession()));
		}
		return get;
	}

	public String getIdentifier(ICourse course, AdobeConnectCourseNode node) {
		return PREFIX + course.getResourceableId() + "_" + node.getIdent();
	}
	
	public String getIdentifier(RepositoryEntry entry, String subIdent) {
		return PREFIX + entry.getOlatResource().getResourceableId() + "_" + subIdent;
	}
}
