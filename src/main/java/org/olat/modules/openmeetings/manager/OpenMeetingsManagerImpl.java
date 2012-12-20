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
import javax.ws.rs.core.UriBuilder;

import org.apache.axis2.AxisFault;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.model.OpenMeetingsRecording;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.OpenMeetingsRoomReference;
import org.olat.modules.openmeetings.model.OpenMeetingsUser;
import org.olat.modules.openmeetings.model.RoomReturnInfo;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryShortImpl;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserDataDeletable;
import org.openmeetings.app.persistence.beans.flvrecord.xsd.FlvRecording;
import org.openmeetings.app.persistence.beans.rooms.xsd.Rooms;
import org.openmeetings.axis.services.AddRoomWithModerationAndRecordingFlags;
import org.openmeetings.axis.services.AddRoomWithModerationAndRecordingFlagsResponse;
import org.openmeetings.axis.services.CloseRoom;
import org.openmeetings.axis.services.CloseRoomResponse;
import org.openmeetings.axis.services.DeleteFlvRecording;
import org.openmeetings.axis.services.DeleteFlvRecordingResponse;
import org.openmeetings.axis.services.DeleteRoom;
import org.openmeetings.axis.services.DeleteRoomResponse;
import org.openmeetings.axis.services.GetFlvRecordingByRoomId;
import org.openmeetings.axis.services.GetFlvRecordingByRoomIdResponse;
import org.openmeetings.axis.services.GetRoomById;
import org.openmeetings.axis.services.GetRoomByIdResponse;
import org.openmeetings.axis.services.GetRoomWithClientObjectsById;
import org.openmeetings.axis.services.GetRoomWithClientObjectsByIdResponse;
import org.openmeetings.axis.services.GetRoomsWithCurrentUsersByListAndType;
import org.openmeetings.axis.services.GetRoomsWithCurrentUsersByListAndTypeResponse;
import org.openmeetings.axis.services.GetSession;
import org.openmeetings.axis.services.GetSessionResponse;
import org.openmeetings.axis.services.KickUser;
import org.openmeetings.axis.services.KickUserByPublicSID;
import org.openmeetings.axis.services.KickUserByPublicSIDResponse;
import org.openmeetings.axis.services.KickUserResponse;
import org.openmeetings.axis.services.LoginUser;
import org.openmeetings.axis.services.LoginUserResponse;
import org.openmeetings.axis.services.SetUserObjectAndGenerateRoomHashByURL;
import org.openmeetings.axis.services.SetUserObjectAndGenerateRoomHashByURLAndRecFlag;
import org.openmeetings.axis.services.SetUserObjectAndGenerateRoomHashByURLAndRecFlagResponse;
import org.openmeetings.axis.services.SetUserObjectAndGenerateRoomHashByURLResponse;
import org.openmeetings.axis.services.UpdateRoomWithModeration;
import org.openmeetings.axis.services.UpdateRoomWithModerationResponse;
import org.openmeetings.axis.services.xsd.RoomReturn;
import org.openmeetings.axis.services.xsd.RoomUser;
import org.openmeetings.stubs.RoomServiceStub;
import org.openmeetings.stubs.UserServiceStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author srosse, stephae.rosse@frentix.com
 */
@Service
public class OpenMeetingsManagerImpl implements OpenMeetingsManager, UserDataDeletable {
	
	private final static OLog log = Tracing.createLoggerFor(OpenMeetingsManagerImpl.class);
	
	@Autowired
	private OpenMeetingsDAO openMeetingsDao;
	@Autowired
	private OpenMeetingsModule openMeetingsModule;
	@Autowired
	private UserDeletionManager userDeletionManager;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private RepositoryManager repositoryManager;

	private CacheWrapper<String,Long> sessionCache;
	private OpenMeetingsLanguages languagesMapping;
	
	@PostConstruct
	public void init() {
		userDeletionManager.registerDeletableUserData(this);
		
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
			RoomServiceStub roomWs = getRoomWebService();

			GetRoomsWithCurrentUsersByListAndType getRooms = new GetRoomsWithCurrentUsersByListAndType();
			getRooms.setAsc(true);
			getRooms.setExternalRoomType(getOpenOLATExternalType());
			getRooms.setOrderby("name");
			getRooms.setStart(0);
			getRooms.setMax(2000);
			getRooms.setSID(adminSID);
			
			Map<Long,RoomReturnInfo> realRooms = new HashMap<Long,RoomReturnInfo>();
			
			//get rooms on openmeetings
			GetRoomsWithCurrentUsersByListAndTypeResponse getRoomsResponse = roomWs.getRoomsWithCurrentUsersByListAndType(getRooms);
			RoomReturn[] roomsRet = getRoomsResponse.get_return();
			if(roomsRet != null) {
				for(RoomReturn roomRet:roomsRet) {
					RoomReturnInfo info = new RoomReturnInfo();
					info.setName(roomRet.getName());
					info.setRoomId(roomRet.getRoom_id());
					int numOfUsers = 0;
					if(roomRet.getRoomUser() != null) {
						for(RoomUser user:roomRet.getRoomUser()) {
							if(user != null) {
								numOfUsers++;
							}
						}
					}
					info.setNumOfUsers(numOfUsers);
					realRooms.put(new Long(roomRet.getRoom_id()), info);
				}
			}

			//get properties saved
			List<OpenMeetingsRoomReference> props = openMeetingsDao.getReferences();
			Map<Long,String> roomIdToResources = getResourceNames(props);

			List<OpenMeetingsRoom> rooms = new ArrayList<OpenMeetingsRoom>();
			for(OpenMeetingsRoomReference prop:props) {
				Long roomId = new Long(prop.getRoomId());
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
		Map<Long,String> roomIdToResourceName = new HashMap<Long,String>();
		Map<Long,List<Long>> resourceIdToRoomIds = new HashMap<Long,List<Long>>();
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
			List<RepositoryEntryShortImpl> shortRepos = repositoryManager.loadRepositoryEntryShortsByResource(resourceIdToRoomIds.keySet(), "CourseModule");
			for(RepositoryEntryShortImpl repoEntry : shortRepos) {
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
			UserServiceStub userWs = getUserWebService();
			String adminSessionId = adminLogin();

			SetUserObjectAndGenerateRoomHashByURLAndRecFlag userObj = new SetUserObjectAndGenerateRoomHashByURLAndRecFlag();
			userObj.setBecomeModeratorAsInt(moderator ? 1 : 0);
			userObj.setEmail(identity.getUser().getProperty(UserConstants.EMAIL, null));
			userObj.setExternalUserId(getOpenOLATUserExternalId(identity));
			userObj.setExternalUserType(getOpenOLATExternalType());
			userObj.setFirstname(identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
			userObj.setLastname(identity.getUser().getProperty(UserConstants.LASTNAME, null));
			userObj.setProfilePictureUrl(getPortraitURL(identity));
			userObj.setRoom_id(roomId);
			userObj.setShowAudioVideoTestAsInt(0);
			userObj.setSID(adminSessionId);
			userObj.setUsername(identity.getName());
			userObj.setAllowRecording(1);
			
			SetUserObjectAndGenerateRoomHashByURLAndRecFlagResponse response = userWs.setUserObjectAndGenerateRoomHashByURLAndRecFlag(userObj);
			String hashedUrl = response.get_return();
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
		File portrait = DisplayPortraitManager.getInstance().getBigPortrait(identity);
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
	public String setGuestUserToRoom(String firstName, String lastName, long roomId)
	throws OpenMeetingsException {
		try {
			UserServiceStub userWs = getUserWebService();
			String adminSessionId = adminLogin();
			
			String username = UUID.randomUUID().toString().replace("-", "");
			SetUserObjectAndGenerateRoomHashByURL userObj = new SetUserObjectAndGenerateRoomHashByURL();
			userObj.setBecomeModeratorAsInt(0);
			userObj.setEmail("");
			userObj.setExternalUserId(getOpenOLATUserExternalId(username));
			userObj.setExternalUserType(getOpenOLATExternalType());
			userObj.setFirstname(firstName);
			userObj.setLastname(lastName);
			userObj.setProfilePictureUrl("");
			userObj.setRoom_id(roomId);
			userObj.setShowAudioVideoTestAsInt(0);
			userObj.setSID(adminSessionId);
			userObj.setUsername(username);
			
			SetUserObjectAndGenerateRoomHashByURLResponse response = userWs.setUserObjectAndGenerateRoomHashByURL(userObj);
			String hashedUrl = response.get_return();
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
			RoomServiceStub roomWs = getRoomWebService();
			GetRoomById getRoomById = new GetRoomById();
			getRoomById.setSID(sid);
			getRoomById.setRooms_id(roomId);
			GetRoomByIdResponse getRoomResponse = roomWs.getRoomById(getRoomById);
			Rooms omRoom = getRoomResponse.get_return();
			if(omRoom != null) {
				room.setComment(omRoom.getComment());
				room.setModerated(omRoom.getIsModeratedRoom());
				room.setName(omRoom.getName());
				room.setRoomId(omRoom.getRooms_id());
				room.setSize(omRoom.getNumberOfPartizipants());
				room.setType(omRoom.getRoomtype().getRoomtypes_id());
				room.setClosed(omRoom.getIsClosed());
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
		} else if(e instanceof AxisFault) {
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

			RoomServiceStub roomWs = getRoomWebService();
			CloseRoom closeRoom = new CloseRoom();
			closeRoom.setRoom_id(room.getRoomId());
			closeRoom.setSID(adminSID);
			closeRoom.setStatus(status);
			//OpenMeetings doc: false = close, true = open
			log.audit("Room state changed (true = close, false = open): " + status);
			
			CloseRoomResponse closeResponse = roomWs.closeRoom(closeRoom);
			responseCode = closeResponse.get_return();
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
			
			RoomServiceStub roomWs = getRoomWebService();
			GetFlvRecordingByRoomId recordingByRoom = new GetFlvRecordingByRoomId();
			recordingByRoom.setRoomId(roomId);
			recordingByRoom.setSID(adminSID);
			GetFlvRecordingByRoomIdResponse recordingResponse = roomWs.getFlvRecordingByRoomId(recordingByRoom);
			FlvRecording[] recordings = recordingResponse.get_return();

			List<OpenMeetingsRecording> recList = new ArrayList<OpenMeetingsRecording>();
			if(recordings != null) {
				for(FlvRecording recording:recordings) {
					if(recording != null) {
						OpenMeetingsRecording rec = new OpenMeetingsRecording();
						rec.setRoomId(recording.getRoom_id());
						rec.setRecordingId(recording.getFlvRecordingId());
						rec.setFilename(recording.getFileName());
						rec.setDownloadName(recording.getFileHash());
						rec.setDownloadNameAlt(recording.getAlternateDownload());
						rec.setPreviewImage(recording.getPreviewImage());
						rec.setWidth(recording.getFlvWidth());
						rec.setHeight(recording.getFlvHeight());
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

			String url = UriBuilder.fromUri(openMeetingsModule.getOpenMeetingsURI()).path("DownloadHandler")
				.queryParam("fileName", recording.getDownloadName())
				.queryParam("moduleName", "lzRecorderApp")
				.queryParam("parentPath", "")
				.queryParam("room_id", Long.toString(recording.getRoomId()))
				.queryParam("sid", sid).build().toString();
			return url;
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
			String sessionId = adminLogin();

			RoomServiceStub roomWs = getRoomWebService();
			AddRoomWithModerationAndRecordingFlags omRoom = new AddRoomWithModerationAndRecordingFlags();
			omRoom.setAppointment(false);
			omRoom.setAllowRecording(room.isRecordingAllowed());
			omRoom.setAllowUserQuestions(true);
			omRoom.setComment(room.getComment());
			omRoom.setDemoTime(0);
			omRoom.setExternalRoomType(getOpenOLATExternalType());
			omRoom.setIsDemoRoom(false);
			omRoom.setIsModeratedRoom(room.isModerated());
			omRoom.setIspublic(false);
			omRoom.setName(room.getName());
			omRoom.setNumberOfPartizipants(room.getSize());
			omRoom.setRoomtypes_id(room.getType());
			omRoom.setSID(sessionId);
			omRoom.setWaitForRecording(false);

			AddRoomWithModerationAndRecordingFlagsResponse addRoomResponse = roomWs.addRoomWithModerationAndRecordingFlags(omRoom);
			long returned = addRoomResponse.get_return();
			if(returned >= 0) {
				room.setRoomId(returned);
				log.audit("Room created");
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
			String sessionId = adminLogin();

			RoomServiceStub roomWs = getRoomWebService();
			UpdateRoomWithModeration omRoom = new UpdateRoomWithModeration();
			omRoom.setAppointment(false);
			omRoom.setComment(room.getComment());
			omRoom.setDemoTime(0);
			omRoom.setIsDemoRoom(false);
			omRoom.setIsModeratedRoom(room.isModerated());
			omRoom.setIspublic(false);
			omRoom.setName(room.getName());
			omRoom.setNumberOfPartizipants(room.getSize());
			omRoom.setRoom_id(room.getRoomId());
			omRoom.setRoomtypes_id(room.getType());
			omRoom.setSID(sessionId);

			UpdateRoomWithModerationResponse updateRoomResponse = roomWs.updateRoomWithModeration(omRoom);
			long returned = updateRoomResponse.get_return();
			if(returned >= 0) {
				log.audit("Room updated");
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
		try {
			String adminSID = adminLogin();
			RoomServiceStub roomWs = getRoomWebService();
			DeleteRoom getRoomCl = new DeleteRoom();
			getRoomCl.setRooms_id(room.getRoomId());
			getRoomCl.setSID(adminSID);
			DeleteRoomResponse deleteRoomResponse = roomWs.deleteRoom(getRoomCl);

			long ret = deleteRoomResponse.get_return();
			boolean ok = ret > 0;
			if(ok && room.getReference() != null) {
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
			RoomServiceStub roomWs = getRoomWebService();
			DeleteFlvRecording deleteRecordingCl = new DeleteFlvRecording();
			deleteRecordingCl.setFlvRecordingId(recording.getRecordingId());
			deleteRecordingCl.setSID(adminSID);
			DeleteFlvRecordingResponse resp = roomWs.deleteFlvRecording(deleteRecordingCl);
			return resp.get_return();
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
			RoomServiceStub roomWs = getRoomWebService();
			GetRoomWithClientObjectsById getRoomCl = new GetRoomWithClientObjectsById();
			getRoomCl.setRooms_id(room.getRoomId());
			getRoomCl.setSID(adminSID);
			GetRoomWithClientObjectsByIdResponse getRoomClResponse = roomWs.getRoomWithClientObjectsById(getRoomCl);

			RoomReturn roomClRet = getRoomClResponse.get_return();
			if(roomClRet != null) {
				RoomUser[] userArr = roomClRet.getRoomUser();
				return convert(userArr);
			}
			return Collections.emptyList();
		} catch (Exception e) {
			log.error("", e);
			throw translateException(e, 0);
		}
	}
	
	private List<OpenMeetingsUser> convert(RoomUser[] clients) {
		List<OpenMeetingsUser> users = new ArrayList<OpenMeetingsUser>();
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
			UserServiceStub userWs = getUserWebService();
			KickUserByPublicSID kickUser = new KickUserByPublicSID();
			kickUser.setSID(adminSID);
			kickUser.setPublicSID(publicSID);
			KickUserByPublicSIDResponse kickResponse = userWs.kickUserByPublicSID(kickUser);
			return kickResponse.get_return();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	@Override
	public boolean removeUsersFromRoom(OpenMeetingsRoom room) {	
		try {
			String adminSID = adminLogin();
			RoomServiceStub roomWs = getRoomWebService();
			KickUser kickUser = new KickUser();
			kickUser.setRoom_id(room.getRoomId());
			kickUser.setSID_Admin(adminSID);
			KickUserResponse kickResponse = roomWs.kickUser(kickUser);
			return kickResponse.get_return();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private String getSessionID() {
		try {
			GetSession  getSession = new GetSession();
			GetSessionResponse getSessionResponse = getUserWebService().getSession(getSession);
			String sessionId = getSessionResponse.get_return().getSession_id();
			return sessionId;
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
			LoginUser adminUser = new LoginUser();
			adminUser.setSID(sid);
			adminUser.setUsername(openMeetingsModule.getAdminLogin());
			adminUser.setUserpass(openMeetingsModule.getAdminPassword());
			LoginUserResponse loginResponse = getUserWebService().loginUser(adminUser);
			returnCode = loginResponse.get_return();
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
			String endPoint = cleanUrl(url) + "/services/UserService?wsdl";
			UserServiceStub userWs = new UserServiceStub(endPoint);
			
			GetSession  getSession = new GetSession();
			GetSessionResponse getSessionResponse = userWs.getSession(getSession);
			String sid = getSessionResponse.get_return().getSession_id();
			
			LoginUser adminUser = new LoginUser();
			adminUser.setSID(sid);
			adminUser.setUsername(login);
			adminUser.setUserpass(password);
			LoginUserResponse loginResponse = getUserWebService().loginUser(adminUser);
			
			returnCode = loginResponse.get_return();
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

	private final RoomServiceStub getRoomWebService()
	throws AxisFault {
		String endPoint = getOpenMeetingsEndPoint() + "RoomService?wsdl";
		RoomServiceStub roomWs = new RoomServiceStub(endPoint);
		return roomWs;
	}
	
	private final UserServiceStub getUserWebService()
	throws AxisFault {
		String endPoint = getOpenMeetingsEndPoint() + "UserService?wsdl";
		UserServiceStub roomWs = new UserServiceStub(endPoint);
		return roomWs;
	}
	
	private String getOpenMeetingsEndPoint() {
		return cleanUrl(openMeetingsModule.getOpenMeetingsURI().toString()) + "/services/";
	}
	
	private String cleanUrl(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}