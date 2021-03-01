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
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.course.nodes.adobeconnect.compatibility.MeetingCompatibilityDate;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectMeetingPermission;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.AdobeConnectUser;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectMeetingImpl;
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
	private DB dbInstance;
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
	public AdobeConnectMeeting getMeeting(AdobeConnectMeeting meeting) {
		if(meeting == null || meeting.getKey() == null) return meeting;
		return adobeConnectMeetingDao.loadByKey(meeting.getKey());
	}

	@Override
	public List<AdobeConnectMeeting> getMeetingsBefore(Date date) {
		return adobeConnectMeetingDao.getMeetingsBefore(date);
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		adobeConnectUserDao.deleteAdobeConnectUser(identity);
	}
	
	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		List<AdobeConnectMeeting> meetings = adobeConnectMeetingDao.getMeetings(group);
		
		AdobeConnectErrors errors = new AdobeConnectErrors();
		for(AdobeConnectMeeting meeting:meetings) {
			deleteMeeting(meeting, errors);
		}
		return errors.hasErrors();
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
	public void createMeeting(String name, String description, String templateId, boolean permanent,
			Date start, long leadtime, Date end, long followupTime, Locale locale, boolean allAccess,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			Identity actingIdentity, AdobeConnectErrors errors) {
		if(adobeConnectModule.isCreateMeetingImmediately()) {
			createAdobeMeeting(name, description, templateId, permanent, start, leadtime, end, followupTime, locale,
					allAccess, entry, subIdent, businessGroup, actingIdentity, errors);
		} else {
			adobeConnectMeetingDao.createMeeting(name, description, permanent,
					start, leadtime, end, followupTime, templateId, null, null, null, entry, subIdent, businessGroup);
		}
	}
	
	@Override
	public AdobeConnectMeeting createAdobeMeeting(AdobeConnectMeeting meeting, Locale locale, boolean allAccess, AdobeConnectErrors errors) {
		AdobeConnectSco sco = null;
		if(adobeConnectModule.isSingleMeetingMode()) {
			sco = getSingleMeetingRoom(meeting.getEntry(), meeting.getSubIdent(), meeting.getBusinessGroup(), errors);
		} else if(meeting.isPermanent()) {
			sco = getPermanentMeetingRoom(meeting.getEntry(), meeting.getSubIdent(), meeting.getBusinessGroup(), errors);
		}
		if(sco == null) {
			AdobeConnectSco folder;
			String folderName = generateFolderName(meeting.getEntry(), meeting.getSubIdent(), meeting.getBusinessGroup());
			List<AdobeConnectSco> folderScos = getAdapter().getFolderByName(folderName, errors);
			if(folderScos == null || folderScos.isEmpty()) {
				folder = getAdapter().createFolder(folderName, errors);
			} else {
				folder = folderScos.get(0);
			}

			if(!errors.hasErrors()) {
				sco = getAdapter().createScoMeeting(meeting.getName(), meeting.getDescription(), folder.getScoId(),
						meeting.getTemplateId(), meeting.getStartDate(), meeting.getEndDate(), locale, errors);	
			}
		}
		if(sco != null) {
			((AdobeConnectMeetingImpl)meeting).setFolderId(sco.getFolderId());
			((AdobeConnectMeetingImpl)meeting).setScoId(sco.getScoId());
			((AdobeConnectMeetingImpl)meeting).setEnvName(adobeConnectModule.getBaseUrl());
			meeting = adobeConnectMeetingDao.updateMeeting(meeting);
		}
		dbInstance.commit();
		return meeting;
	}

	/**
	 * Create the Adobe Meeting first and the OpenOlat database object only
	 * if the meeting was successfully scheduled.
	 * 
	 * @param name The name of the meeting
	 * @param description The description of the meeting
	 * @param templateId The template
	 * @param start Date to start the meeting
	 * @param leadtime Preparation time
	 * @param end End of the meeting
	 * @param followupTime Follow-up time
	 * @param locale Language
	 * @param allAccess 
	 * @param entry The course
	 * @param subIdent The course node identifier (for example)
	 * @param businessGroup The business group
	 * @param actingIdentity The user which create the meeting
	 * @param errors Errors
	 */
	private void createAdobeMeeting(String name, String description, String templateId, boolean permanent,
			Date start, long leadtime, Date end, long followupTime, Locale locale, boolean allAccess,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			Identity actingIdentity, AdobeConnectErrors errors) {
		
		AdobeConnectSco folder;
		String folderName = generateFolderName(entry, subIdent, businessGroup);
		List<AdobeConnectSco> folderScos = getAdapter().getFolderByName(folderName, errors);
		if(folderScos == null || folderScos.isEmpty()) {
			folder = getAdapter().createFolder(folderName, errors);
		} else {
			folder = folderScos.get(0);
		}

		if(errors.hasErrors()) {
			return;// we need a folder
		}
		
		AdobeConnectSco sco = null;
		if(adobeConnectModule.isSingleMeetingMode()) {
			sco = getSingleMeetingRoom(entry, subIdent, businessGroup, errors);
		} else if(permanent) {
			sco = getPermanentMeetingRoom(entry, subIdent, businessGroup, errors);
		}
		if(sco == null) {
			sco = getAdapter().createScoMeeting(name, description, folder.getScoId(), templateId, start, end, locale, errors);
		}
		if(sco != null) {
			getAdapter().setPermissions(sco.getScoId(), allAccess, errors);

			String actingUser = getOrCreateUser(actingIdentity, true, errors);
			if(actingUser != null) {
				getAdapter().setMember(sco.getScoId(), actingUser, AdobeConnectMeetingPermission.host.permission(), errors);
			}
			
			// try harder if the meeting hasn't a single host
			if(actingUser == null) {
				AdobeConnectPrincipal admin = getAdapter().getPrincipalByLogin(adobeConnectModule.getAdminLogin(), errors);
				if(admin != null) {
					getAdapter().setMember(sco.getScoId(), admin.getPrincipalId(), AdobeConnectMeetingPermission.host.permission(), errors);
				}
			}
			
			String scoId = sco.getScoId();
			String envName = adobeConnectModule.getBaseUrl();
			adobeConnectMeetingDao.createMeeting(name, description,
					permanent, start, leadtime, end, followupTime,
					templateId, scoId, folder.getScoId(), envName, entry, subIdent, businessGroup);
		}
	}
	
	/**
	 * Search for a meeting room in single meeting mode.
	 * 
	 * @return The meeting or null if not found.
	 */
	private AdobeConnectSco getSingleMeetingRoom(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			AdobeConnectErrors errors) {
		List<AdobeConnectMeeting> currentMeetings = getMeetings(entry, subIdent, businessGroup);	
		if(currentMeetings != null && !currentMeetings.isEmpty()) {
			for (AdobeConnectMeeting meeting:currentMeetings) {
				if (StringHelper.containsNonWhitespace(meeting.getScoId())) {
					return getAdapter().getScoMeeting(meeting, errors);
				}
			}
		}
		return null;
	}
	
	private AdobeConnectSco getPermanentMeetingRoom(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			AdobeConnectErrors errors) {
		List<AdobeConnectMeeting> currentMeetings = getMeetings(entry, subIdent, businessGroup);
		if(currentMeetings != null && !currentMeetings.isEmpty()) {
			for(AdobeConnectMeeting meeting:currentMeetings) {
				if(meeting.isPermanent() && StringHelper.containsNonWhitespace(meeting.getScoId())) {
					return getAdapter().getScoMeeting(meeting, errors);
				}
			}
		}
		return null;
	}
	
	@Override
	public AdobeConnectMeeting updateMeeting(AdobeConnectMeeting meeting, String name, String description, String templateId,
			boolean permanent, Date start, long leadTime, Date end, long followupTime, AdobeConnectErrors errors) {
		if(StringHelper.containsNonWhitespace(meeting.getScoId())) {
			boolean ok = getAdapter().updateScoMeeting(meeting.getScoId(), name, description, templateId, start, end, errors);
			if(ok) {
				meeting = updateMeeting(meeting, name, description, permanent, start, leadTime, end, followupTime);
			}
		} else {
			meeting = updateMeeting(meeting, name, description, permanent, start, leadTime, end, followupTime);
		}
		return meeting;
	}
	
	private AdobeConnectMeeting updateMeeting(AdobeConnectMeeting meeting, String name, String description,
			boolean permanent, Date start, long leadTime, Date end, long followupTime) {
		meeting.setName(name);
		meeting.setDescription(description);
		if(adobeConnectModule.isSingleMeetingMode()) {
			meeting.setPermanent(true);
		} else if(!StringHelper.containsNonWhitespace(meeting.getScoId())) {
			meeting.setPermanent(permanent);
		}
		meeting.setStartDate(start);
		meeting.setLeadTime(leadTime);
		meeting.setEndDate(end);
		meeting.setFollowupTime(followupTime);
		return adobeConnectMeetingDao.updateMeeting(meeting);
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
	public String open(AdobeConnectMeeting meeting, Identity identity, AdobeConnectErrors errors) {
		meeting.setOpened(true);
		meeting = adobeConnectMeetingDao.updateMeeting(meeting);
		dbInstance.commit();
		return join(meeting, identity, true, errors);
	}

	@Override
	public String join(AdobeConnectMeeting meeting, Identity identity, boolean moderator, AdobeConnectErrors errors) {
		String actingUser = getOrCreateUser(identity, true, errors);
		if(actingUser != null) {
			if(moderator) {// make sure the moderator can open the meeting
				getAdapter().setMember(meeting.getScoId(), actingUser, AdobeConnectMeetingPermission.host.permission(), errors);
			}
			
			AdobeConnectSco sco = getAdapter().getScoMeeting(meeting, errors);
			if(sco != null) {
				String urlPath = sco.getUrlPath();
				UriBuilder builder = adobeConnectModule
						.getAdobeConnectHostUriBuilder()
						.path(urlPath);
	
				BreezeSession session = null;
				Authentication authentication = securityManager.findAuthentication(identity, ACONNECT_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
				if(authentication != null) {
					session = getAdapter().commonInfo(authentication, errors);
				}
	
				if(session != null && StringHelper.containsNonWhitespace(session.getSession())) {
					builder.queryParam("session", session.getSession());
				} else {
					String fullName = userManager.getUserDisplayName(identity);
					builder.queryParam("guestName", fullName).build();
				}
				return builder.build().toString();
			}
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
		Authentication authentication = securityManager.findAuthentication(identity, ACONNECT_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
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
		boolean deleteAdobeConnect = canDeleteAdobeMeeting(meeting);
		if(deleteAdobeConnect) {
			AdobeConnectErrors error = new AdobeConnectErrors();
			if(getAdapter().deleteScoMeeting(meeting, error)) {
				AdobeConnectMeeting reloadedMeeting = adobeConnectMeetingDao.loadByKey(meeting.getKey());
				adobeConnectMeetingDao.deleteMeeting(reloadedMeeting);
				deleted = true;
			}
			errors.append(error);
		} else {
			AdobeConnectMeeting reloadedMeeting = adobeConnectMeetingDao.loadByKey(meeting.getKey());
			adobeConnectMeetingDao.deleteMeeting(reloadedMeeting);
			deleted = true;
		}
		return deleted;
	}
	
	private boolean canDeleteAdobeMeeting(AdobeConnectMeeting meeting) {
		List<AdobeConnectMeeting> sharedMeetings;
		if(meeting.getEntry() != null) {
			sharedMeetings = adobeConnectMeetingDao.getMeetings(meeting.getEntry(), meeting.getSubIdent());
		} else if(meeting.getBusinessGroup() != null) {
			sharedMeetings = adobeConnectMeetingDao.getMeetings(meeting.getBusinessGroup());
		} else {
			return true;
		}
		
		boolean foundSharedMeeting = false;
		for(AdobeConnectMeeting sharedMeeting:sharedMeetings) {
			if(!sharedMeeting.equals(meeting)
					&& sharedMeeting.getScoId() != null
					&& sharedMeeting.getScoId().equals(meeting.getScoId())) {
				foundSharedMeeting |= true;
			}
		}
		return !foundSharedMeeting;
	}

	@Override
	public void convert(List<MeetingCompatibilityDate> meetingsData, RepositoryEntry entry, String subIdent) {
		boolean hasMeetings = adobeConnectMeetingDao.hasMeetings(entry, subIdent);
		if(hasMeetings) {
			return; // do the conversion only once
		}
		
		String scoId = null;
		String folderId = null;
		String envName = null;
		
		AdobeConnectErrors errors = new AdobeConnectErrors();
		String roomId = entry.getOlatResource().getResourceableId() + "_" + subIdent;
		List<AdobeConnectSco> meetings = getAdapter().getMeetingByName(roomId, errors);
		if(meetings != null && !meetings.isEmpty()) {
			AdobeConnectSco meeting = meetings.get(0);
			scoId = meeting.getScoId();
			folderId = meeting.getFolderId();
			envName = adobeConnectModule.getBaseUrl();
		}

		for(MeetingCompatibilityDate meetingData:meetingsData) {
			adobeConnectMeetingDao.createMeeting(meetingData.getTitle(), meetingData.getDescription(), true,
					meetingData.getStart(), 15, meetingData.getEnd(), 15, null, scoId, folderId, envName, entry, subIdent, null);
		}
	}

	@Override
	public boolean hasMeetings(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		if(entry != null) {
			return adobeConnectMeetingDao.hasMeetings(entry, subIdent);
		} else if(businessGroup != null) {
			return adobeConnectMeetingDao.hasMeetings(businessGroup);
		}
		return false;
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
			boolean compatible = adobeConnectModule.isLoginCompatibilityMode();
			String login;
			if(compatible) {
				login = "olat-" + identity.getName();
			} else {
				login = identity.getUser().getEmail();
			}
			AdobeConnectPrincipal aUser = getAdapter().getPrincipalByLogin(login, error);
			
			String creds = null;
			if(aUser == null) {
				if( compatible || getAdapter().isManagedPassword()) {
					creds = UUID.randomUUID().toString().replace("-", "");
					if(creds.length() > 32) {
						creds = creds.substring(0, 32);
					}
				}
				aUser = getAdapter().createPrincipal(identity, login, creds, error);
			}
			
			if(aUser != null && StringHelper.containsNonWhitespace(aUser.getPrincipalId())) {
				user = adobeConnectUserDao.createUser(aUser.getPrincipalId(), envName, identity);
				
				Authentication authentication = securityManager.findAuthentication(identity, ACONNECT_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
				if(authentication == null) {
					securityManager.createAndPersistAuthentication(identity, ACONNECT_PROVIDER, BaseSecurity.DEFAULT_ISSUER, login, creds, Encoder.Algorithm.aes);
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
		dbInstance.commit();
		
		boolean allOk = false;
		
		String common = buildUrl(url, null) + "?action=common-info";
		
		BreezeSession session = null;
		HttpGet commonGet = new HttpGet(common);
		try(CloseableHttpClient httpClient = adobeConnectModule.httpClientBuilder().build();
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
		try(CloseableHttpClient httpClient = adobeConnectModule.httpClientBuilder().build();
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
