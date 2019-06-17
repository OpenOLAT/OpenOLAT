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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectMeetingPermission;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.AdobeConnectUser;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectPrincipal;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.model.BreezeSession;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AdobeConnectManagerImpl implements AdobeConnectManager, DeletableGroupData, UserDataDeletable {
	
	private static final Logger log = Tracing.createLoggerFor(AdobeConnectManagerImpl.class);
	
	protected static final String ACONNECT_PROVIDER = "ACONNECT";

	private AdobeConnectSPI adapter;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private List<AdobeConnectSPI> providers;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AdobeConnectUserDAO adobeConnectUserDao;
	@Autowired
	protected AdobeConnectModule adobeConnectModule;
	@Autowired
	private AdobeConnectMeetingDAO adobeConnectMeetingDao;
	
	private AdobeConnectSPI getAdapter() {
		String providerId = adobeConnectModule.getProviderId();
		if(adapter == null || !adapter.getId().equals(providerId)) {
			for(AdobeConnectSPI provider:providers) {
				if(provider.getId().equals(providerId)) {
					adapter = provider;
				}
			}
		}
		return adapter == null ? new NoAdapterProvider() : adapter;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		adobeConnectUserDao.deleteAdobeConnectUser(identity);
	}
	
	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		List<AdobeConnectMeeting> meetings = adobeConnectMeetingDao.getMeetings(group);
		
		AdobeConnectErrors erros = new AdobeConnectErrors();
		for(AdobeConnectMeeting meeting:meetings) {
			deleteMeeting(meeting, erros);
		}
		return erros.hasErrors();
	}

	private String generateFolderName(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		StringBuilder name = new StringBuilder();
		name.append(WebappHelper.getInstanceId());
		if(entry != null) {
			name.append("-").append(entry.getKey().toString());
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			name.append("-").append(subIdent);
		}
		if(businessGroup != null) {
			name.append("-").append(businessGroup.getKey());
		}
		return name.toString();
	}

	@Override
	public void createMeeting(String name, String description, String templateId,
			Date start, Date end, Locale locale, boolean allAccess,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			Identity actingIdentity, AdobeConnectErrors error) {
		
		AdobeConnectSco folder;
		String folderName = generateFolderName(entry, subIdent, businessGroup);
		List<AdobeConnectSco> folderScos = getAdapter().getFolderByName(folderName, error);
		if(folderScos == null || folderScos.isEmpty()) {
			folder = getAdapter().createFolder(folderName, error);
		} else {
			folder = folderScos.get(0);
		}
		
		AdobeConnectSco sco = getAdapter().createScoMeeting(name, description, folder.getScoId(), templateId, start, end, locale, error);
		if(sco != null) {
			getAdapter().setPermissions(sco.getScoId(), true, error);
			AdobeConnectPrincipal admin = getAdapter().adminCommonInfo(error);
			if(admin != null) {
				getAdapter().setMember(sco.getScoId(), admin.getPrincipalId(), AdobeConnectMeetingPermission.host.permission(), error);
			}

			String actingUser = getOrCreateUser(actingIdentity, true, error);
			if(actingUser != null) {
				getAdapter().setMember(sco.getScoId(), actingUser, AdobeConnectMeetingPermission.host.permission(), error);
			}
			
			// try harder if the meeting hasn't a single host
			if(actingUser == null && admin == null) {
				admin = getAdapter().getPrincipalByLogin(adobeConnectModule.getAdminLogin(), error);
				if(admin != null) {
					getAdapter().setMember(sco.getScoId(), admin.getPrincipalId(), AdobeConnectMeetingPermission.host.permission(), error);
				}
			}
			
			String scoId = sco.getScoId();
			String envName = adobeConnectModule.getBaseUrl();
			adobeConnectMeetingDao.createMeeting(name, description, start, end, scoId, folder.getScoId(), envName, entry, subIdent, businessGroup);
		}
	}
	
	@Override
	public AdobeConnectMeeting updateMeeting(AdobeConnectMeeting meeting, String name, String description, String templateId,
			Date start, Date end, AdobeConnectErrors errors) {
		boolean ok = getAdapter().updateScoMeeting(meeting.getScoId(), name, description, templateId, start, end, errors);
		if(ok) {
			meeting.setName(name);
			meeting.setDescription(description);
			meeting.setStartDate(start);
			meeting.setEndDate(end);
			meeting = adobeConnectMeetingDao.updateMeeting(meeting);
		}
		return meeting;
	}
	
	@Override
	public AdobeConnectMeeting shareDocuments(AdobeConnectMeeting meeting, List<AdobeConnectSco> documents) {
		meeting = adobeConnectMeetingDao.loadByKey(meeting.getKey());
		List<String> scoIds = documents.stream().map(AdobeConnectSco::getScoId).collect(Collectors.toList());
		meeting.setSharedDocumentIds(scoIds);
		return adobeConnectMeetingDao.updateMeeting(meeting);
	}

	@Override
	public List<AdobeConnectSco> getTemplates() {
		List<AdobeConnectSco> templates = getAdapter().getTemplates();
		if(templates == null) {
			templates = new ArrayList<>();
		}
		return templates;
	}

	@Override
	public List<AdobeConnectSco> getRecordings(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		List<AdobeConnectSco> recordings = getAdapter().getRecordings(meeting, error);
		if(recordings == null) {
			recordings = new ArrayList<>();
		}
		return recordings;
	}

	@Override
	public boolean registerFor(AdobeConnectMeeting meeting, Identity identity, AdobeConnectMeetingPermission permission, AdobeConnectErrors error) {
		boolean registered = false;
		String actingUser = getOrCreateUser(identity, true, error);
		if(actingUser != null) {
			registered = getAdapter().setMember(meeting.getScoId(), actingUser, permission.permission(), error);
		}
		return registered;
	}

	@Override
	public boolean isRegistered(AdobeConnectMeeting meeting, Identity identity, AdobeConnectMeetingPermission permission, AdobeConnectErrors error) {
		boolean registered = false;
		String actingUser = getOrCreateUser(identity, false, error);
		if(actingUser != null) {
			registered = getAdapter().isMember(meeting.getScoId(), actingUser, permission.permission(), error);
		}
		return registered;
	}

	@Override
	public String join(AdobeConnectMeeting meeting, Identity identity, AdobeConnectErrors error) {
		String actingUser = getOrCreateUser(identity, false, error);
		if(actingUser != null) {
			AdobeConnectSco sco = getAdapter().getScoMeeting(meeting, error);
			String urlPath = sco.getUrlPath();
			UriBuilder builder = adobeConnectModule
					.getAdobeConnectHostUriBuilder()
					.path(urlPath);

			BreezeSession session = null;
			Authentication authentication = securityManager.findAuthentication(identity, ACONNECT_PROVIDER);
			if(authentication != null) {
				session = getAdapter().commonInfo(authentication, error);
			}

			if(session != null) {
				builder.queryParam("session", session.getSession());
			} else {
				String fullName = userManager.getUserDisplayName(identity);
				builder.queryParam("guestName", fullName).build();
			}
			return builder.build().toString();
		}
		return null;
	}

	@Override
	public String linkTo(AdobeConnectSco content, Identity identity, AdobeConnectErrors error) {
		String urlPath = content.getUrlPath();
		UriBuilder builder = adobeConnectModule
				.getAdobeConnectHostUriBuilder()
				.path(urlPath);
		
		BreezeSession session = null;
		Authentication authentication = securityManager.findAuthentication(identity, ACONNECT_PROVIDER);
		if(authentication != null) {
			session = getAdapter().commonInfo(authentication, error);
		}
		
		if(session != null) {
			builder.queryParam("session", session.getSession());
		} else {
			String fullName = userManager.getUserDisplayName(identity);
			builder.queryParam("guestName", fullName).build();
		}
		return builder.build().toString();
	}

	@Override
	public void delete(RepositoryEntry entry, String subIdent) {
		List<AdobeConnectMeeting> meetings = adobeConnectMeetingDao.getMeetings(entry, subIdent);
		AdobeConnectErrors errors = new AdobeConnectErrors();
		for(AdobeConnectMeeting meeting:meetings) {
			deleteMeeting(meeting, errors);
		}
	}

	@Override
	public boolean deleteMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors errors) {
		boolean deleted = false;
		AdobeConnectErrors error = new AdobeConnectErrors();
		if(getAdapter().deleteScoMeeting(meeting, error)) {
			AdobeConnectMeeting reloadedMeeting = adobeConnectMeetingDao.loadByKey(meeting.getKey());
			adobeConnectMeetingDao.deleteMeeting(reloadedMeeting);
			deleted = true;
		}
		errors.append(error);
		return deleted;
	}

	@Override
	public List<AdobeConnectMeeting> getMeetings(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		if(entry != null) {
			return adobeConnectMeetingDao.getMeetings(entry, subIdent);
		} else if(businessGroup != null) {
			return adobeConnectMeetingDao.getMeetings(businessGroup);
		}
		return Collections.emptyList();
	}

	@Override
	public List<AdobeConnectMeeting> getAllMeetings() {
		return adobeConnectMeetingDao.getAllMeetings();
	}
	
	private String getOrCreateUser(Identity identity, boolean create, AdobeConnectErrors error) {
		String envName = adobeConnectModule.getBaseUrl();
		AdobeConnectUser user = adobeConnectUserDao.getUser(identity, envName);
		if(user == null && create) {
			String login = identity.getUser().getEmail();
			AdobeConnectPrincipal aUser = getAdapter().getPrincipalByLogin(login, error);
			
			String creds = null;
			if(aUser == null) {
				if(getAdapter().isManagedPassword()) {
					creds = UUID.randomUUID().toString().replace("-", "");
					if(creds.length() > 32) {
						creds = creds.substring(0, 32);
					}
				}
				aUser = getAdapter().createPrincipal(identity, login, creds, error);
			}
			
			if(aUser != null && StringHelper.containsNonWhitespace(aUser.getPrincipalId())) {
				user = adobeConnectUserDao.createUser(aUser.getPrincipalId(), envName, identity);
				
				Authentication authentication = securityManager.findAuthentication(identity, ACONNECT_PROVIDER);
				if(authentication == null) {
					securityManager.createAndPersistAuthentication(identity, ACONNECT_PROVIDER, login, creds, Encoder.Algorithm.aes);
				} else if(creds != null) {
					securityManager.updateCredentials(authentication, creds, Encoder.Algorithm.aes);
				}
			}
		}
		return user == null ? null : user.getPrincipalId();
	}

	private String buildUrl(String url, String contextPath) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(url);
		if(StringHelper.containsNonWhitespace(contextPath)) {
			sb.append(contextPath);
		}
		return sb.toString();
	}

	@Override
	public boolean checkConnection(String url, String login, String password, AdobeConnectErrors error) {
		boolean allOk = false;
		
		String common = buildUrl(url, null) + "?action=common-info";
		
		BreezeSession session = null;
		HttpGet commonGet = new HttpGet(common);
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(commonGet)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				Header header = response.getFirstHeader("Set-Cookie");
				if(header != null) {
					session = BreezeSession.valueOf(header);
				}
			}
			EntityUtils.consume(response.getEntity());
		} catch(Exception e) {
			log.error("", e);
		}
		
		String request = buildUrl(url, null) + "?action=login&login=" + login + "&password=" + password;
		HttpGet get = new HttpGet(request);
		if(session != null) {
			request += "&session" + session.getSession();
			get.setHeader(new BasicHeader("Cookie", AbstractAdobeConnectProvider.COOKIE + session.getSession()));
		}
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200 && AdobeConnectUtils.isStatusOk(response.getEntity())) {
				allOk = true;
			}
		} catch(Exception e) {
			log.error("", e);
		}
		
		return allOk;
	}
}
