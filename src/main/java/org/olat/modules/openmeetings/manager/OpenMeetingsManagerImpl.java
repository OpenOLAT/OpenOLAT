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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.manager;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.xml.ws.BindingProvider;

import org.apache.logging.log4j.Logger;
import org.apache.openmeetings.axis.services.GetRoomsWithCurrentUsersByListAndType;
import org.apache.openmeetings.axis.services.RoomService;
import org.apache.openmeetings.axis.services.RoomServicePortType;
import org.apache.openmeetings.axis.services.UserService;
import org.apache.openmeetings.axis.services.UserServicePortType;
import org.apache.openmeetings.axis.services.xsd.RoomReturn;
import org.apache.openmeetings.axis.services.xsd.RoomUser;
import org.apache.openmeetings.db.dto.record.xsd.RecordingDTO;
import org.apache.openmeetings.db.dto.room.xsd.RoomDTO;
import org.apache.openmeetings.db.entity.server.xsd.Sessiondata;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.model.OpenMeetingsRecording;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.OpenMeetingsRoomReference;
import org.olat.modules.openmeetings.model.OpenMeetingsUser;
import org.olat.modules.openmeetings.model.RoomReturnInfo;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author srosse, stephae.rosse@frentix.com
 */
@Service
public class OpenMeetingsManagerImpl implements OpenMeetingsManager, UserDataDeletable, DeletableGroupData {
	
	private static final Logger log = Tracing.createLoggerFor(OpenMeetingsManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OpenMeetingsDAO openMeetingsDao;
	@Autowired
	private OpenMeetingsModule openMeetingsModule;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private DisplayPortraitManager portraitManager;

	private CacheWrapper<String,Long> sessionCache;
	private OpenMeetingsLanguages languagesMapping;
	
	@PostConstruct
	public void init() {
		languagesMapping = new OpenMeetingsLanguages();
		languagesMapping.read();

		sessionCache = coordinator.getCoordinator().getCacher().getCache(OpenMeetingsManager.class.getSimpleName(), "session");
	}

	@Override
	public Long getRoomId(BusinessGroup group, OLATResourceable ores, String subIdentifier) {
		OpenMeetingsRoomReference prop = openMeetingsDao.getReference(group, ores, subIdentifier);
		if(prop == null) {
			return null;
		}
		return prop.getRoomId();
	}
	
	@Override
	public List<OpenMeetingsRoom> getOpenOLATRooms() {
		try {
			String adminSID = adminLogin();
			RoomServicePortType roomWs = getRoomWebService();

			GetRoomsWithCurrentUsersByListAndType getRooms = new GetRoomsWithCurrentUsersByListAndType();
			getRooms.setAsc(true);
			getRooms.setExternalRoomType(getOpenOLATExternalType());
			getRooms.setOrderby("name");
			getRooms.setStart(0);
			getRooms.setMax(2000);
			getRooms.setSID(adminSID);
			
			Map<Long,RoomReturnInfo> realRooms = new HashMap<>();
			
			//get rooms on openmeetings
			List<RoomReturn> roomsRet = roomWs.getRoomsWithCurrentUsersByListAndType(adminSID, 0, 2000, "name", true, getOpenOLATExternalType());
			if(roomsRet != null) {
				for(RoomReturn roomRet:roomsRet) {
					RoomReturnInfo info = new RoomReturnInfo();
					info.setName(roomRet.getName());
					info.setRoomId(roomRet.getRoomId());
					int numOfUsers = 0;
					if(roomRet.getRoomUser() != null) {
						for(RoomUser user:roomRet.getRoomUser()) {
							if(user != null) {
								numOfUsers++;
							}
						}
					}
					info.setNumOfUsers(numOfUsers);
					realRooms.put(Long.valueOf(roomRet.getRoomId()), info);
				}
			}

			//get properties saved
			List<OpenMeetingsRoomReference> props = openMeetingsDao.getReferences();
			Map<Long,String> roomIdToResources = getResourceNames(props);

			List<OpenMeetingsRoom> rooms = new ArrayList<>();
			for(OpenMeetingsRoomReference prop:props) {
				Long roomId = Long.valueOf(prop.getRoomId());
				RoomReturnInfo infos = realRooms.get(roomId);
				if(infos != null) {
					OpenMeetingsRoom room = openMeetingsDao.deserializeRoom(prop.getConfig());
					room.setReference(prop);
					room.setName(infos.getName());
					room.setNumOfUsers(infos.getNumOfUsers());
					String resourceName = roomIdToResources.get(roomId);
					if(resourceName != null) {
						room.setResourceName(resourceName);
					}
					rooms.add(room);
				}
			}
			return rooms;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private Map<Long,String> getResourceNames(List<OpenMeetingsRoomReference> properties) {
		Map<Long,String> roomIdToResourceName = new HashMap<>();
		Map<Long,List<Long>> resourceIdToRoomIds = new HashMap<>();
		for(OpenMeetingsRoomReference prop:properties) {
			long roomId = prop.getRoomId();
			if(prop.getGroup() != null) {
				roomIdToResourceName.put(roomId, prop.getGroup().getName());
			} else if("CourseModule".equals(prop.getResourceTypeName())) {
				if(!resourceIdToRoomIds.containsKey(prop.getResourceTypeId())) {
					resourceIdToRoomIds.put(prop.getResourceTypeId(), new ArrayList<Long>());
				}
				resourceIdToRoomIds.get(prop.getResourceTypeId()).add(roomId);
			}
		}
		
		if(!resourceIdToRoomIds.isEmpty()) {
			List<RepositoryEntry> shortRepos = repositoryEntryDao.loadByResourceIds("CourseModule", resourceIdToRoomIds.keySet());
			for(RepositoryEntry repoEntry : shortRepos) {
				List<Long> roomIds = resourceIdToRoomIds.get(repoEntry.getOlatResource().getResourceableId());
				for(Long roomId:roomIds) {
					roomIdToResourceName.put(roomId, repoEntry.getDisplayname());
				}
			}
		}
		
		return roomIdToResourceName;
	}

	@Override
	public String getURL(Identity identity, long roomId, String securedHash, Locale locale) {
		StringBuilder sb = new StringBuilder();
		sb.append(openMeetingsModule.getOpenMeetingsURI().toString());
		if(sb.lastIndexOf("/") != (sb.length() - 1)) {
			sb.append("/");
		}
		sb.append("?secureHash=").append(securedHash)
		  .append("&scopeRoomId=").append(roomId)
		  .append("&language=").append(languagesMapping.getLanguageId(locale))
		  .append("&user_id=").append(getOpenOLATUserExternalId(identity))
		  .append("&wwwroot=").append(Settings.getServerContextPathURI());
		return sb.toString();
	}
	
	@Override
	public String setUserToRoom(Identity identity, long roomId, boolean moderator)
	throws OpenMeetingsException {
		try {
			UserServicePortType userWs = getUserWebService();
			String sid = adminLogin();

			int becomeModeratorAsInt = (moderator ? 1 : 0);
			String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
			String externalUserId = getOpenOLATUserExternalId(identity);
			String externalUserType = getOpenOLATExternalType();
			String firstname = identity.getUser().getProperty(UserConstants.FIRSTNAME, null);
			String lastname = identity.getUser().getProperty(UserConstants.LASTNAME, null);
			String profilePictureUrl = getPortraitURL(identity);
			String username = identity.getName();
			
			String hashedUrl = userWs.setUserObjectAndGenerateRoomHashByURLAndRecFlag(sid,
					username, firstname, lastname, profilePictureUrl, email, externalUserId, externalUserType,
					roomId, becomeModeratorAsInt, 0, 1);
			if(hashedUrl.startsWith("-") && hashedUrl.length() < 5) {
				throw new OpenMeetingsException(parseErrorCode(hashedUrl));
			}
			return hashedUrl;
		} catch(OpenMeetingsException e) {
			log.error("", e);
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}
	
	private long parseErrorCode(String errorCode) {
		try {
			return Integer.parseInt(errorCode);
		} catch (NumberFormatException e) {
			return OpenMeetingsException.UNKOWN;
		}
	}
	
	private String getPortraitURL(Identity identity) {
		File portrait = portraitManager.getBigPortrait(identity);
		if(portrait == null || !portrait.exists()) {
			return "";
		}
		
		String key = UUID.randomUUID().toString().replace("-", "");
		sessionCache.put(key, identity.getKey());
		
		StringBuilder sb = new StringBuilder();
		sb.append(Settings.getServerContextPathURI())
		  .append("/restapi/openmeetings/")
		  .append(key)
		  .append("/portrait");

		return sb.toString();
	}
	
	@Override
	public Long getIdentityKey(String token) {
		return sessionCache.get(token);
	}
	
	@Override
	public String setGuestUserToRoom(String firstname, String lastname, long roomId)
	throws OpenMeetingsException {
		try {
			String adminSessionId = adminLogin();
			String username = UUID.randomUUID().toString().replace("-", "");
			String email = "";
			String externalUserId = getOpenOLATUserExternalId(username);
			String externalUserType = getOpenOLATExternalType();
			String profilePictureUrl = "";
			
			String hashedUrl = getUserWebService()
					.setUserObjectAndGenerateRoomHashByURL(adminSessionId, username, firstname, lastname,
							profilePictureUrl, email, externalUserId, externalUserType, roomId, 0, 0);
			if(hashedUrl.startsWith("-") && hashedUrl.length() < 5) {
				throw new OpenMeetingsException(parseErrorCode(hashedUrl));
			}
			return hashedUrl;
		} catch(OpenMeetingsException e) {
			log.error("", e);
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}

	@Override
	public OpenMeetingsRoom getRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier) 
	throws OpenMeetingsException {
		OpenMeetingsRoomReference prop = openMeetingsDao.getReference(group, ores, subIdentifier);
		if(prop == null) {
			return null;
		}
		
		Long roomId = prop.getRoomId();
		if(roomId != null && roomId.longValue() > 0) {
			try {
				String sessionId = adminLogin();
				OpenMeetingsRoom room = openMeetingsDao.deserializeRoom(prop.getConfig());
				getRoomById(sessionId, room, roomId.longValue());
				return room;
			} catch(OpenMeetingsException e) {
				throw e;
			} catch(Exception e) {
				log.error("", e);
				throw translateException(e, 0);
			}
		}
		return null;
	}
	
	@Override
	public OpenMeetingsRoom getLocalRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier) {
		OpenMeetingsRoomReference ref = openMeetingsDao.getReference(group, ores, subIdentifier);
		if(ref == null) {
			return null;
		}
		OpenMeetingsRoom room = openMeetingsDao.deserializeRoom(ref.getConfig());
		room.setReference(ref);
		return room;
	}

	private OpenMeetingsRoom getRoomById(String sid, OpenMeetingsRoom room, long roomId)
	throws OpenMeetingsException {
		try {
			RoomServicePortType roomWs = getRoomWebService();
			RoomDTO omRoom = roomWs.getRoomById(sid, roomId);
			if(omRoom != null) {
				room.setComment(omRoom.getComment());
				if(omRoom.isModerated() != null) {
					room.setModerated(omRoom.isModerated());
				}
				if(omRoom.isAudioOnly() != null) {
					room.setAudioOnly(omRoom.isAudioOnly());
				}
				room.setName(omRoom.getName());
				if(omRoom.getId() != null) {
					room.setRoomId(omRoom.getId());
				} else {
					room.setRoomId(roomId);
				}
				room.setSize(omRoom.getNumberOfPartizipants());
				room.setType(omRoom.getRoomtype().getRoomtypesId());
				room.setClosed(omRoom.isClosed());
				return room;
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}
	
	private OpenMeetingsException translateException(Exception e, long ret) {
		long type = OpenMeetingsException.UNKOWN;
		if(ret < 0) {
			type = ret;
		} else {
			Throwable cause = e.getCause();
			if(cause instanceof ConnectException
					&& cause.getMessage() != null
					&& cause.getMessage().contains("onnection refused")) {
				type = OpenMeetingsException.SERVER_NOT_AVAILABLE;
			}
		}
		return new OpenMeetingsException(e, type);
	}
	
	public OpenMeetingsRoom openRoom(OpenMeetingsRoom room) throws OpenMeetingsException {
		return closeOpenMeetingsRoom(room, false);
	}
	
	public OpenMeetingsRoom closeRoom(OpenMeetingsRoom room) throws OpenMeetingsException {
		return closeOpenMeetingsRoom(room, true);
	}
	
	/**
	 * In OpenMeetings, close can mean open :-)
	 * @param roomId The room id
	 * @param status false = close, true = open
	 * @throws OpenMeetingsException
	 */
	private OpenMeetingsRoom closeOpenMeetingsRoom(OpenMeetingsRoom room, boolean status) throws OpenMeetingsException {
		int responseCode = 0;
		try {
			String adminSID = adminLogin();

			RoomServicePortType roomWs = getRoomWebService();
			//OpenMeetings doc: false = close, true = open
			log.info(Tracing.M_AUDIT, "Room state changed (true = close, false = open): " + status);
			responseCode = roomWs.closeRoom(adminSID, room.getRoomId(), status);
			if(responseCode < 0) {
				throw new OpenMeetingsException(responseCode);
			}
			return getRoomById(adminSID, room, room.getRoomId());
		} catch(OpenMeetingsException e) {
			log.error("", e);
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, responseCode);
		}
	}
	
	@Override
	public List<OpenMeetingsRecording> getRecordings(long roomId) 
	throws OpenMeetingsException {

		try {
			String adminSID = adminLogin();
			RoomServicePortType roomWs = getRoomWebService();
			List<RecordingDTO> recordings = roomWs.getFlvRecordingByRoomId(adminSID, roomId);

			List<OpenMeetingsRecording> recList = new ArrayList<>();
			if(recordings != null) {
				for(RecordingDTO recording:recordings) {
					if(recording != null) {
						OpenMeetingsRecording rec = new OpenMeetingsRecording();
						rec.setRoomId(recording.getRoomId());
						rec.setRecordingId(recording.getId());
						rec.setFilename(recording.getName());
						rec.setDownloadName(recording.getFlvName());
						rec.setDownloadNameAlt(recording.getAviName());
						// preview image?
						rec.setWidth(recording.getWidth());
						rec.setHeight(recording.getHeight());
						recList.add(rec);
					}
				}
			}
			
			return recList;
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}

	@Override
	public String getRecordingURL(OpenMeetingsRecording recording)
	throws OpenMeetingsException {
		try {
			String sid = adminLogin();
			return UriBuilder.fromUri(openMeetingsModule.getOpenMeetingsURI()).path("DownloadHandler")
				.queryParam("fileName", recording.getDownloadName())
				.queryParam("moduleName", "lzRecorderApp")
				.queryParam("parentPath", "")
				.queryParam("room_id", Long.toString(recording.getRoomId()))
				.queryParam("sid", sid).build().toString();
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}

	@Override
	public OpenMeetingsRoom addRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier, OpenMeetingsRoom room) {
		if(room.getRoomId() < 0) {
			updateRoom(group, ores, subIdentifier, room);
		}
		
		try {
			String sid = adminLogin();
			RoomServicePortType roomWs = getRoomWebService();
			long returned = roomWs.addRoomWithModerationAndRecordingFlags(sid,
					room.getName(), room.getType(), room.getComment(), room.getSize(), false, false,
					false, 0, room.isModerated(), getOpenOLATExternalType(), true,
					room.isAudioOnly(), false, true);
			if(returned >= 0) {
				room.setRoomId(returned);
				log.info(Tracing.M_AUDIT, "Room created");
				OpenMeetingsRoomReference ref = openMeetingsDao.createReference(group, ores, subIdentifier, room);
				room.setReference(ref);
				return room;
			}
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private String getOpenOLATUserExternalId(Identity identity) {
		return getOpenOLATUserExternalId(identity.getName());
	}
	
	private String getOpenOLATUserExternalId(String username) {
		return username + "@" + WebappHelper.getInstanceId();
	}
	
	@Override
	public String getOpenOLATExternalType() {
		return "openolat_" + WebappHelper.getInstanceId();
	}

	@Override
	public OpenMeetingsRoom updateRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier, OpenMeetingsRoom room) {
		try {
			String sid = adminLogin();
			RoomServicePortType roomWs = getRoomWebService();
			long returned = roomWs.updateRoomWithModerationQuestionsAudioTypeAndHideOptions(sid, room.getRoomId(), 
					room.getName(), room.getType(), room.getComment(), room.getSize(), false, false, false, 0, room.isModerated(), 
					false, room.isAudioOnly(), false, false, false, false, false, false, false);
			if(returned >= 0) {
				log.info(Tracing.M_AUDIT, "Room updated");
				openMeetingsDao.updateReference(group, ores, subIdentifier, room);
				return room;
			}
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	@Override
	public boolean deleteRoom(OpenMeetingsRoom room) {
		return deleteRoom(room, false);
	}
	
	private boolean deleteRoom(OpenMeetingsRoom room, boolean force) {
		try {
			String adminSID = adminLogin();
			RoomServicePortType roomWs = getRoomWebService();
			long ret = roomWs.deleteRoom(adminSID, room.getRoomId());
			boolean ok = ret > 0;
			if((ok || force) && room.getReference() != null) {
				openMeetingsDao.delete(room.getReference());
			}
			return ok;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	@Override
	public boolean deleteRecording (OpenMeetingsRecording recording) {
		try {
			String adminSID = adminLogin();
			RoomServicePortType roomWs = getRoomWebService();
			return roomWs.deleteFlvRecording(adminSID, recording.getRecordingId());
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	@Override
	public List<OpenMeetingsUser> getUsersOf(OpenMeetingsRoom room)
	throws OpenMeetingsException {
		try {
			String adminSID = adminLogin();
			RoomServicePortType roomWs = getRoomWebService();
			RoomReturn roomClRet = roomWs.getRoomWithClientObjectsById(adminSID, room.getRoomId());
			if(roomClRet != null) {
				List<RoomUser> userArr = roomClRet.getRoomUser();
				return convert(userArr);
			}
			return Collections.emptyList();
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}
	
	private List<OpenMeetingsUser> convert(List<RoomUser> clients) {
		List<OpenMeetingsUser> users = new ArrayList<>();
		if(clients != null) {
			for(RoomUser client:clients) {
				OpenMeetingsUser user = convert(client);
				if(user != null) {
					users.add(user);
				}
			}
		}
		return users;
	}
	
	private OpenMeetingsUser convert(RoomUser client) {
		if(client == null) {
			return null;
		}
		OpenMeetingsUser user = new OpenMeetingsUser();
		user.setPublicSID(client.getPublicSID());
		user.setFirstName(client.getFirstname());
		user.setLastName(client.getLastname());
		return user;
	}

	@Override
	public boolean removeUser(String publicSID) {
		try {
			String adminSID = adminLogin();
			return getUserWebService().kickUserByPublicSID(adminSID, publicSID);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	@Override
	public boolean removeUsersFromRoom(OpenMeetingsRoom room) {	
		try {
			String adminSID = adminLogin();
			return getRoomWebService().kickUser(adminSID, room.getRoomId());
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private String getSessionID() {
		try {
			Sessiondata getSessionResponse = getUserWebService().getSession();
			return getSessionResponse.getSessionId();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	private String adminLogin()
	throws OpenMeetingsException {
		long returnCode = 0;
		try {
			String sid = getSessionID();
			String username = openMeetingsModule.getAdminLogin();
			String userpass = openMeetingsModule.getAdminPassword();
			returnCode = getUserWebService().loginUser(sid, username, userpass);
			if(returnCode > 0) {
				return sid;
			}
			throw new OpenMeetingsException(returnCode);
		} catch(OpenMeetingsException e) {
			log.error("", e);
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, returnCode);
		}
	}
	
	@Override
	public void deleteAll(BusinessGroup group, OLATResourceable ores, String subIdentifier)
	 throws OpenMeetingsException {
		try {
			Long roomId = getRoomId(group, ores, subIdentifier);
			if(roomId != null) {
				OpenMeetingsRoom room = getRoom(group, ores, subIdentifier);
				if(room != null) {
					deleteRoom(room);
				}
			}
		} catch (OpenMeetingsException e) {
			log.error("", e);
		}
	}

	@Override
	public boolean checkConnection(String url, String login, String password)
	throws OpenMeetingsException {
		long returnCode;
		try {
			UserService ss = new UserService();
			UserServicePortType port = ss.getUserServiceHttpSoap11Endpoint();
			String endPoint = cleanUrl(url) + "/services/UserService?wsdl";
			((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
			
			Sessiondata sessiondata = port.getSession();
			String sid = sessiondata.getSessionId();

			returnCode = getUserWebService().loginUser(sid, login, password);
			if(returnCode > 0) {
				return StringHelper.containsNonWhitespace(sid);
			}
			throw new OpenMeetingsException(returnCode);
		} catch(OpenMeetingsException e) {
			log.error("", e);
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		//
	}

	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		boolean allOk = true;
		OpenMeetingsRoom room = getLocalRoom(group, null, null);
		if(room != null) {
			allOk &= deleteRoom(room, true);
		}
		return allOk;
	}

	private final RoomServicePortType getRoomWebService() {
		dbInstance.commit();// free connection before an HTTP call
		
		RoomService ss = new RoomService();
		RoomServicePortType port = ss.getRoomServiceHttpSoap11Endpoint();
		String endPoint = getOpenMeetingsEndPoint() + "RoomService?wsdl";
		((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
		return port;
	}
	
	private final UserServicePortType getUserWebService() {
		dbInstance.commit();// free connection before an HTTP call
		
		UserService ss = new UserService();
		UserServicePortType port = ss.getUserServiceHttpSoap11Endpoint();
		String endPoint = getOpenMeetingsEndPoint() + "UserService?wsdl";
		((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
		return port;
	}
	
	private String getOpenMeetingsEndPoint() {
		return cleanUrl(openMeetingsModule.getOpenMeetingsURI().toString()) + "/services/";
	}
	
	private String cleanUrl(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}