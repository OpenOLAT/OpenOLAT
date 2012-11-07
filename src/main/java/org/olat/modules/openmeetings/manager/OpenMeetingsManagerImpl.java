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

import java.io.StringWriter;
import java.util.Locale;

import javax.annotation.PostConstruct;

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
import org.olat.core.util.xml.XStreamHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.UserDataDeletable;
import org.openmeetings.app.persistence.beans.rooms.xsd.Rooms;
import org.openmeetings.axis.services.AddRoomWithModerationAndExternalType;
import org.openmeetings.axis.services.AddRoomWithModerationAndExternalTypeResponse;
import org.openmeetings.axis.services.GetRoomById;
import org.openmeetings.axis.services.GetRoomByIdResponse;
import org.openmeetings.axis.services.GetRoomsPublic;
import org.openmeetings.axis.services.GetRoomsPublicResponse;
import org.openmeetings.axis.services.GetSession;
import org.openmeetings.axis.services.GetSessionResponse;
import org.openmeetings.axis.services.LoginUser;
import org.openmeetings.axis.services.LoginUserResponse;
import org.openmeetings.axis.services.SetUserObjectAndGenerateRoomHashByURL;
import org.openmeetings.axis.services.SetUserObjectAndGenerateRoomHashByURLResponse;
import org.openmeetings.axis.services.UpdateRoomWithModeration;
import org.openmeetings.axis.services.UpdateRoomWithModerationResponse;
import org.openmeetings.stubs.RoomServiceStub;
import org.openmeetings.stubs.UserServiceStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;


/**
 * 
 * @author srosse, stephae.rosse@frentix.com
 */
@Service
public class OpenMeetingsManagerImpl implements OpenMeetingsManager, UserDataDeletable {
	
	private final static OLog log = Tracing.createLoggerFor(OpenMeetingsManagerImpl.class);
	
	private final static String OM_CATEGORY = "openmeetings_room";
	
	@Autowired
	private OpenMeetingsModule openMeetingsModule;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private UserDeletionManager userDeletionManager;

	private XStream xStream;
	private OpenMeetingsLanguages languagesMapping;
	
	@PostConstruct
	public void init() {
		xStream = XStreamHelper.createXStreamInstance();
		xStream.alias("room", OpenMeetingsRoom.class);
		
		userDeletionManager.registerDeletableUserData(this);
		
		languagesMapping = new OpenMeetingsLanguages();
		languagesMapping.read();
	}

	@Override
	public Long getRoomId(BusinessGroup group, OLATResourceable ores, String subIdentifier) {
		Property prop = getProperty(group, ores, subIdentifier);
		if(prop == null) {
			return null;
		}
		return prop.getLongValue();
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
	public String setUser(Identity identity, long roomId) {
		try {
			UserServiceStub userWs = getUserWebService();
			String adminSessionId = adminLogin();

			SetUserObjectAndGenerateRoomHashByURL userObj = new SetUserObjectAndGenerateRoomHashByURL();
			userObj.setBecomeModeratorAsInt(0);
			userObj.setEmail(identity.getUser().getProperty(UserConstants.EMAIL, null));
			userObj.setExternalUserId(getOpenOLATUserExternalId(identity));
			userObj.setExternalUserType(getOpenOLATExternalType());
			userObj.setFirstname(identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
			userObj.setLastname(identity.getUser().getProperty(UserConstants.LASTNAME, null));
			userObj.setProfilePictureUrl("");
			userObj.setRoom_id(roomId);
			userObj.setShowAudioVideoTestAsInt(0);
			userObj.setSID(adminSessionId);
			userObj.setUsername(identity.getName());
			
			SetUserObjectAndGenerateRoomHashByURLResponse response = userWs.setUserObjectAndGenerateRoomHashByURL(userObj);
			String hashedUrl = response.get_return();
			return hashedUrl;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public OpenMeetingsRoom getRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier) {
		Property prop = getProperty(group, ores, subIdentifier);
		if(prop == null) {
			return null;
		}
		
		Long roomId = prop.getLongValue();
		if(roomId != null && roomId.longValue() > 0) {
			try {
				String sessionId = adminLogin();
				OpenMeetingsRoom room = deserializeRoom(prop.getTextValue());
				getRoomById(sessionId, room, roomId.longValue());
				return room;
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		return null;
	}
	
	private OpenMeetingsRoom getRoomById(String sid, OpenMeetingsRoom room, long roomId) throws Exception {
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
			return room;
		} else {
			return null;
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
			AddRoomWithModerationAndExternalType omRoom = new AddRoomWithModerationAndExternalType();
			omRoom.setAppointment(false);
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

			AddRoomWithModerationAndExternalTypeResponse addRoomResponse = roomWs.addRoomWithModerationAndExternalType(omRoom);
			long returned = addRoomResponse.get_return();
			if(returned >= 0) {
				room.setRoomId(returned);
				log.audit("Room created");
				Property prop = createProperty(group, ores, subIdentifier, room);
				propertyManager.saveProperty(prop);
				return room;
			}
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private String getOpenOLATUserExternalId(Identity identity) {
		return identity.getName() + "@" + WebappHelper.getInstanceId();
	}
	
	private String getOpenOLATExternalType() {
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
			omRoom.setRoomtypes_id(room.getType());
			omRoom.setSID(sessionId);

			UpdateRoomWithModerationResponse updateRoomResponse = roomWs.updateRoomWithModeration(omRoom);
			long returned = updateRoomResponse.get_return();
			if(returned >= 0) {
				log.audit("Room updated");
				updateProperty(group, ores, subIdentifier, room);
				return room;
			}
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
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

	private String adminLogin() {
		try {
			String sessionId = getSessionID() ;
			LoginUser adminUser = new LoginUser();
			adminUser.setSID(sessionId);
			adminUser.setUsername(openMeetingsModule.getAdminLogin());
			adminUser.setUserpass(openMeetingsModule.getAdminPassword());
			LoginUserResponse loginResponse = getUserWebService().loginUser(adminUser);
			long login = loginResponse.get_return();
			return login > 0 ? sessionId : null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public void getRooms(String sessionId) {
		try {
			RoomServiceStub roomsWs = getRoomWebService();
			
			GetRoomsPublic getRooms = new GetRoomsPublic();
			getRooms.setSID(sessionId);

			GetRoomsPublicResponse getRoomsResponse = roomsWs.getRoomsPublic(getRooms);
			Rooms[] rooms = getRoomsResponse.get_return();
			if(rooms != null) {
				System.out.println(rooms.length);
				for(Rooms room : rooms) {
					if(room == null) {
						System.out.println("Room is null");
					} else {
						System.out.println(room.getName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteAll(BusinessGroup group, OLATResourceable ores, String subIdentifier) {
		//
	}

	@Override
	public boolean checkConnection(String url, String login, String password)
	throws OpenMeetingsNotAvailableException {
		try {
			String endPoint = cleanUrl(url) + "/services/UserService?wsdl";
			UserServiceStub userWs = new UserServiceStub(endPoint);
			
			GetSession  getSession = new GetSession();
			GetSessionResponse getSessionResponse = userWs.getSession(getSession);
			String sessionId = getSessionResponse.get_return().getSession_id();
			return StringHelper.containsNonWhitespace(sessionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		//
	}

	//Properties
	private final Property getProperty(BusinessGroup group, OLATResourceable courseResource, String subIdentifier) {
		return propertyManager.findProperty(null, group, courseResource, OM_CATEGORY, subIdentifier);
	}
	
	private final Property updateProperty(BusinessGroup group, OLATResourceable courseResource, String subIdentifier, OpenMeetingsRoom room) {
		Property property = getProperty(group, courseResource, subIdentifier);
		if(property == null) {
			property = createProperty(group, courseResource, subIdentifier, room);
			propertyManager.saveProperty(property);
		} else {
			String serialized = serializeRoom(room);
			property.setTextValue(serialized);
			propertyManager.updateProperty(property);
		}
		return property;
	}
	
	private final Property createProperty(final BusinessGroup group, final OLATResourceable courseResource, String subIdentifier, OpenMeetingsRoom room) {
		String serialized = serializeRoom(room);
		long roomId = room.getRoomId();
		return propertyManager.createPropertyInstance(null, group, courseResource, OM_CATEGORY, subIdentifier, null, roomId, null, serialized);
	}
	
	private final OpenMeetingsRoom deserializeRoom(String room) {
		return (OpenMeetingsRoom)xStream.fromXML(room);
	}
	
	private final String serializeRoom(OpenMeetingsRoom room) {
		StringWriter writer = new StringWriter();
		xStream.marshal(room, new CompactWriter(writer));
		writer.flush();
		return writer.toString();
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
