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
package org.olat.modules.vitero.manager;

import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriBuilder;

import org.apache.axis2.AxisFault;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.vitero.ViteroModule;
import org.olat.modules.vitero.manager.stubs.BookingServiceStub;
import org.olat.modules.vitero.manager.stubs.BookingServiceStub.Booking;
import org.olat.modules.vitero.manager.stubs.BookingServiceStub.Bookinglist;
import org.olat.modules.vitero.manager.stubs.BookingServiceStub.Bookingtype;
import org.olat.modules.vitero.manager.stubs.GroupServiceStub;
import org.olat.modules.vitero.manager.stubs.GroupServiceStub.Completegrouptype;
import org.olat.modules.vitero.manager.stubs.LicenceServiceStub;
import org.olat.modules.vitero.manager.stubs.LicenceServiceStub.Rooms_type0;
import org.olat.modules.vitero.manager.stubs.MtomServiceStub;
import org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub;
import org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.Codetype;
import org.olat.modules.vitero.manager.stubs.UserServiceStub;
import org.olat.modules.vitero.manager.stubs.UserServiceStub.Userid;
import org.olat.modules.vitero.manager.stubs.UserServiceStub.Userlist;
import org.olat.modules.vitero.manager.stubs.UserServiceStub.Usertype;
import org.olat.modules.vitero.model.ErrorCode;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroGroup;
import org.olat.modules.vitero.model.ViteroGroupRoles;
import org.olat.modules.vitero.model.ViteroStatus;
import org.olat.modules.vitero.model.ViteroUser;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

/**
 * 
 * Description:<br>
 * Implementation of the Virtual Classroom for the Vitero Booking System
 * 
 * <P>
 * Initial Date:  26 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ViteroManager extends BasicManager implements UserDataDeletable {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
	
	private static final String VMS_PROVIDER = "VMS";
	private static final String VMS_CATEGORY = "vitero-category";
	private static final String VMS_CATEGORY_ZOMBIE = "vitero-category-zombie";
	
	@Autowired
	private ViteroModule viteroModule;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserDeletionManager userDeletionManager;
	
	private XStream xStream;

	public ViteroManager() {
		//make Spring happy
	}
	
	@PostConstruct
	public void init() {
		xStream = XStreamHelper.createXStreamInstance();
		xStream.alias("vBooking", ViteroBooking.class);
		xStream.omitField(ViteroBooking.class, "property");
		
		userDeletionManager.registerDeletableUserData(this);
	}
	
	public void setViteroModule(ViteroModule module) {
		this.viteroModule = module;
	}
	
	public List<ViteroBooking> getBookingByDate(Date start, Date end) 
	throws VmsNotAvailableException {
		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.GetBookingListByDateRequest dateRequest = new BookingServiceStub.GetBookingListByDateRequest();
			dateRequest.setStart(format(start));
			dateRequest.setEnd(format(end));
			dateRequest.setTimezone(viteroModule.getTimeZoneId());
			dateRequest.setCustomerid(viteroModule.getCustomerId());
			BookingServiceStub.GetBookingListByDateResponse response = bookingWs.getBookingListByDate(dateRequest);
			
			BookingServiceStub.Bookinglist bookingList = response.getGetBookingListByDateResponse();
			Booking[] bookings = bookingList.getBooking();
			return convert(bookings);
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot get the list of bookings by date.", f);
			}
			return Collections.emptyList();
		} catch (RemoteException e) {
			logError("Cannot get bookings by date", e);
			return Collections.emptyList();
		}
	}
	
	public boolean canGoBooking(ViteroBooking booking) {
		Date now = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(booking.getStart());
		cal.add(Calendar.MINUTE, -booking.getStartBuffer());
		Date start = cal.getTime();
		cal.setTime(booking.getEnd());
		cal.add(Calendar.MINUTE, booking.getEndBuffer());
		Date end = cal.getTime();
		
		if(start.before(now) && end.after(now)) {
			return true;
		}
		return false;
	}
	
	public String getURLToBooking(Identity identity, ViteroBooking booking)
	throws VmsNotAvailableException {
		String sessionCode = createPersonalBookingSessionCode(identity, booking);
		String url = getStartPoint(sessionCode);
		return url;
	}
	
	public String getURLToGroup(Identity identity, ViteroBooking booking)
	throws VmsNotAvailableException {
		String sessionCode = createVMSSessionCode(identity);
		String url = getGroupURL(sessionCode, booking.getGroupId());
		return url;
	}
	
	/**
	 * Create a session code with a one hour expiration date
	 * @param identity
	 * @param booking
	 * @return
	 */
	protected String createVMSSessionCode(Identity identity)
	throws VmsNotAvailableException {
		try {
			int userId = getVmsUserId(identity, true);
			
			//update user information
			try {
				updateVmsUser(identity, userId);
				storePortrait(identity, userId);
			} catch (Exception e) {
				logError("Cannot update user on vitero system:" + identity.getName(), e);
			}

			SessionCodeServiceStub sessionCodeWs = getSessionCodeWebService();
			SessionCodeServiceStub.CreateVmsSessionCodeRequest codeRequest = new SessionCodeServiceStub.CreateVmsSessionCodeRequest();
			
			SessionCodeServiceStub.Sessioncode_type1 code = new SessionCodeServiceStub.Sessioncode_type1();
			code.setUserid(userId);
			code.setTimezone(viteroModule.getTimeZoneId());
		
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, 1);
			code.setExpirationdate(format(cal.getTime()));

			codeRequest.setSessioncode(code);
			
			SessionCodeServiceStub.CreateVmsSessionCodeResponse response = sessionCodeWs.createVmsSessionCode(codeRequest);
			SessionCodeServiceStub.Codetype myCode = response.getCreateVmsSessionCodeResponse();
			return myCode.getCode();
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case userDoesntExist: logError("User does not exist.", f); break;
				case userNotAssignedToGroup: logError("User not assigned to group.", f); break;
				case invalidAttribut: logError("Invalid attribute.", f); break; 
				case invalidTimezone: logError("Invalid time zone.", f); break;
				case bookingDoesntExist:
				case bookingDoesntExistPrime: logError("Booking does not exist.", f); break;
				default: logAxisError("Cannot create session code.", f);
			}
			return null;
		} catch (RemoteException e) {
			logError("Cannot create session code.", e);
			return null;
		}
	}
	
	/**
	 * Create a session code with a one hour expiration date
	 * @param identity
	 * @param booking
	 * @return
	 */
	protected String createPersonalBookingSessionCode(Identity identity, ViteroBooking booking)
	throws VmsNotAvailableException {
		try {
			int userId = getVmsUserId(identity, true);
			
			//update user information
			try {
				updateVmsUser(identity, userId);
				storePortrait(identity, userId);
			} catch (Exception e) {
				logError("Cannot update user on vitero system:" + identity.getName(), e);
			}

			SessionCodeServiceStub sessionCodeWs = getSessionCodeWebService();
			SessionCodeServiceStub.CreatePersonalBookingSessionCodeRequest codeRequest = new SessionCodeServiceStub.CreatePersonalBookingSessionCodeRequest();
			
			SessionCodeServiceStub.Sessioncode_type2 code = new SessionCodeServiceStub.Sessioncode_type2();
			code.setBookingid(booking.getBookingId());
			code.setUserid(userId);
			code.setTimezone(viteroModule.getTimeZoneId());
		
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, 1);
			code.setExpirationdate(format(cal.getTime()));

			codeRequest.setSessioncode(code);
			
			SessionCodeServiceStub.CreatePersonalBookingSessionCodeResponse response = sessionCodeWs.createPersonalBookingSessionCode(codeRequest);
			Codetype myCode = response.getCreatePersonalBookingSessionCodeResponse();
			return myCode.getCode();
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case userDoesntExist: logError("User does not exist.", f); break;
				case userNotAssignedToGroup: logError("User not assigned to group.", f); break;
				case invalidAttribut: logError("Invalid attribute.", f); break; 
				case invalidTimezone: logError("Invalid time zone.", f); break;
				case bookingDoesntExist:
				case bookingDoesntExistPrime: logError("Booking does not exist.", f); break;
				default: logAxisError("Cannot create session code.", f);
			}
			return null;
		} catch (RemoteException e) {
			logError("Cannot create session code.", e);
			return null;
		}
	}
	
	public ViteroGroupRoles getGroupRoles(int id)
	throws VmsNotAvailableException {
		try {
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.GetGroupRequest getRequest = new GroupServiceStub.GetGroupRequest();
			GroupServiceStub.Groupid groupId = new GroupServiceStub.Groupid();
			groupId.setGroupid(id);
			getRequest.setGetGroupRequest(groupId);
			
			GroupServiceStub.GetGroupResponse response = groupWs.getGroup(getRequest);
			GroupServiceStub.Group group = response.getGetGroupResponse();
			GroupServiceStub.Completegrouptype groupType = group.getGroup();
			GroupServiceStub.Participant_type0[] participants = groupType.getParticipant();
			int numOfParticipants = participants == null ? 0 : participants.length;

			ViteroGroupRoles groupRoles = new ViteroGroupRoles();
			if(numOfParticipants > 0) {
				Map<Integer,String> idToEmails = new HashMap<Integer,String>();
				Usertype[] vmsUsers = getVmsUsersByGroup(id);
				if(vmsUsers != null) {
					for(Usertype vmsUser:vmsUsers) {
						Integer userId = new Integer(vmsUser.getId());
						String email = vmsUser.getEmail();
						groupRoles.getEmailsOfParticipants().add(email);
						idToEmails.put(userId, email);
					}	
				}
				
				for(int i=0; i<numOfParticipants; i++) {
					GroupServiceStub.Participant_type0 participant = participants[i];
					Integer userId = new Integer(participant.getUserid());
					String email = idToEmails.get(userId);
					if(email != null) {
						GroupRole role = GroupRole.valueOf(participant.getRole());
						groupRoles.getEmailsToRole().put(email, role);
					}
				}
			}

			return groupRoles;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot get group roles",f);
			}
			return null;
		} catch (RemoteException e) {
			logError("Cannot get group roles.", e);
			return null;
		}	
	}
	
	public boolean isUserOf(ViteroBooking booking, Identity identity)
	throws VmsNotAvailableException {
		boolean member = false;
		int userId = getVmsUserId(identity, false);
		if(userId > 0) {
			Usertype[] users = getVmsUsersByGroup(booking.getGroupId());
			if(users != null) {
				for(Usertype user:users) {
					if(userId == user.getId()) {
						member = true;
					}
				}
			}
		}
		return member;
	}
	
	public List<ViteroUser> getUsersOf(ViteroBooking booking) 
	throws VmsNotAvailableException {
		return convert(getVmsUsersByGroup(booking.getGroupId()));
	}
	
	protected Usertype[] getVmsUsersByGroup(int groupId)
	throws VmsNotAvailableException {
		try {
			UserServiceStub userWs = getUserWebService();
			UserServiceStub.GetUserListByGroupRequest listRequest = new UserServiceStub.GetUserListByGroupRequest();
			listRequest.setGroupid(groupId);
			UserServiceStub.GetUserListByGroupResponse response = userWs.getUserListByGroup(listRequest);
			Userlist userList = response.getGetUserListByGroupResponse();
			Usertype[] userTypes = userList.getUser();
			return userTypes;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot get the list of users in group: " + groupId, f);
			}
			return null;
		} catch (RemoteException e) {
			logError("Cannot get the list of users in group: " + groupId, e);
			return null;
		}
	}
	
	protected int getVmsUserId(Identity identity, boolean create) 
	throws VmsNotAvailableException {
		int userId;
		Authentication authentication = securityManager.findAuthentication(identity, VMS_PROVIDER);
		if(authentication == null) {
			if(create) {
				userId =  createVmsUser(identity);
				if(userId > 0) {
					securityManager.createAndPersistAuthentication(identity, VMS_PROVIDER, Integer.toString(userId), "");
				}
			} else {
				userId = -1;
			}
		} else {
			userId = Integer.parseInt(authentication.getAuthusername());
		}
		return userId;
	}
	
	protected boolean updateVmsUser(Identity identity, int vmsUserId)
	throws VmsNotAvailableException {
		try {
			UserServiceStub userWs = getUserWebService();
			UserServiceStub.UpdateUserRequest updateRequest = new UserServiceStub.UpdateUserRequest();
			UserServiceStub.Completeusertype user = new UserServiceStub.Completeusertype();
			user.setId(vmsUserId);
			
			//mandatory
			User olatUser = identity.getUser();
			user.setUsername("olat." + WebappHelper.getInstanceId() + "." + identity.getName());
			user.setSurname(olatUser.getProperty(UserConstants.LASTNAME, null));
			user.setFirstname(olatUser.getProperty(UserConstants.FIRSTNAME, null));
			user.setEmail(olatUser.getProperty(UserConstants.EMAIL, null));

			//optional
			String language = identity.getUser().getPreferences().getLanguage();
			if(StringHelper.containsNonWhitespace(language) && language.startsWith("de")) {
				user.setLocale("de");
			} else {
				user.setLocale("en");
			}
			user.setPcstate("NOT_TESTED");
			user.setTimezone(viteroModule.getTimeZoneId());
			
			String street = olatUser.getProperty(UserConstants.STREET, null);
			if(StringHelper.containsNonWhitespace(street)) {
				user.setStreet(street);
			}
			String zip = olatUser.getProperty(UserConstants.ZIPCODE, null);
			if(StringHelper.containsNonWhitespace(zip)) {
				user.setZip(zip);
			}
			String city = olatUser.getProperty(UserConstants.CITY, null);
			if(StringHelper.containsNonWhitespace(city)) {
				user.setCity(city);
			}
			String country = olatUser.getProperty(UserConstants.COUNTRY, null);
			if(StringHelper.containsNonWhitespace(country)) {
				user.setCountry(country);
			}
			
			String mobile = olatUser.getProperty(UserConstants.TELMOBILE, null);
			if(StringHelper.containsNonWhitespace(mobile)) {
				user.setMobile(mobile);
			}
			String phonePrivate = olatUser.getProperty(UserConstants.TELPRIVATE, null);
			if(StringHelper.containsNonWhitespace(phonePrivate)) {
				user.setPhone(phonePrivate);
			}
			String phoneOffice = olatUser.getProperty(UserConstants.TELOFFICE, null);
			if(StringHelper.containsNonWhitespace(phoneOffice)) {
				user.setPhone(phoneOffice);
			}
			String institution = olatUser.getProperty(UserConstants.INSTITUTIONALNAME, null);
			if(StringHelper.containsNonWhitespace(institution)) {
				user.setCompany(institution);
			}
			
			updateRequest.setUser(user);
			userWs.updateUser(updateRequest);
			return true;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot create vms user.", f);
			}
			return true;
		} catch (RemoteException e) {
			logError("Cannot create vms user.", e);
			return true;
		}
	}
	
	protected int createVmsUser(Identity identity)
	throws VmsNotAvailableException {
		try {
			UserServiceStub userWs = getUserWebService();
			UserServiceStub.CreateUserRequest createRequest = new UserServiceStub.CreateUserRequest();
			UserServiceStub.Newusertype user = new UserServiceStub.Newusertype();
			
			//mandatory
			User olatUser = identity.getUser();
			user.setUsername("olat." + WebappHelper.getInstanceId() + "." + identity.getName());
			user.setSurname(olatUser.getProperty(UserConstants.LASTNAME, null));
			user.setFirstname(olatUser.getProperty(UserConstants.FIRSTNAME, null));
			user.setEmail(olatUser.getProperty(UserConstants.EMAIL, null));
			user.setPassword("changeme");
			
			UserServiceStub.Idlist customerIds = new UserServiceStub.Idlist();
			customerIds.set_int(new int[]{viteroModule.getCustomerId()});
			user.setCustomeridlist(customerIds);

			//optional
			String language = identity.getUser().getPreferences().getLanguage();
			if(StringHelper.containsNonWhitespace(language) && language.startsWith("de")) {
				user.setLocale("de");
			} else {
				user.setLocale("en");
			}
			user.setPcstate("NOT_TESTED");
			user.setTimezone(viteroModule.getTimeZoneId());
			
			String street = olatUser.getProperty(UserConstants.STREET, null);
			if(StringHelper.containsNonWhitespace(street)) {
				user.setStreet(street);
			}
			String zip = olatUser.getProperty(UserConstants.ZIPCODE, null);
			if(StringHelper.containsNonWhitespace(zip)) {
				user.setZip(zip);
			}
			String city = olatUser.getProperty(UserConstants.CITY, null);
			if(StringHelper.containsNonWhitespace(city)) {
				user.setCity(city);
			}
			String country = olatUser.getProperty(UserConstants.COUNTRY, null);
			if(StringHelper.containsNonWhitespace(country)) {
				user.setCountry(country);
			}
			
			String mobile = olatUser.getProperty(UserConstants.TELMOBILE, null);
			if(StringHelper.containsNonWhitespace(mobile)) {
				user.setMobile(mobile);
			}
			String phonePrivate = olatUser.getProperty(UserConstants.TELPRIVATE, null);
			if(StringHelper.containsNonWhitespace(phonePrivate)) {
				user.setPhone(phonePrivate);
			}
			String phoneOffice = olatUser.getProperty(UserConstants.TELOFFICE, null);
			if(StringHelper.containsNonWhitespace(phoneOffice)) {
				user.setPhone(phoneOffice);
			}
			String institution = olatUser.getProperty(UserConstants.INSTITUTIONALNAME, null);
			if(StringHelper.containsNonWhitespace(institution)) {
				user.setCompany(institution);
			}
			/*
			user.setTitle("");
			*/
			user.setTechnicalnote("Generated by OpenOLAT");
			
			createRequest.setUser(user);
			UserServiceStub.CreateUserResponse response = userWs.createUser(createRequest);
			Userid userId = response.getCreateUserResponse();
			
			storePortrait(identity, userId.getUserid());
			return userId.getUserid();
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot create vms user.", f);
			}
			return -1;
		} catch (RemoteException e) {
			logError("Cannot create vms user.", e);
			return -1;
		}
	}
	
	protected boolean storePortrait(Identity identity, int userId)
	throws VmsNotAvailableException {
		try {
			File portrait = DisplayPortraitManager.getInstance().getBigPortrait(identity);
			if(portrait != null && portrait.exists()) {
				MtomServiceStub mtomWs = getMtomWebService();
				
				MtomServiceStub.StoreAvatarRequest request = new MtomServiceStub.StoreAvatarRequest();
				MtomServiceStub.CompleteAvatarWrapper avatar = new MtomServiceStub.CompleteAvatarWrapper();
				
				avatar.setType(BigInteger.ZERO);
				avatar.setUserid(BigInteger.valueOf(userId));
				avatar.setFilename(portrait.getName());
				
				DataHandler portraitHandler = new DataHandler(new FileDataSource(portrait));
				avatar.setFile(portraitHandler);

				request.setStoreAvatarRequest(avatar);
				mtomWs.storeAvatar(request);
				return true;
			}
			return false;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot store the portrait of " + userId, f);
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot store the portrait of " + userId, e);
			return false;
		}
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		if(!viteroModule.isDeleteVmsUserOnUserDelete()) return;
		
		try {
			int userId = getVmsUserId(identity, false);
			if(userId > 0) {
				deleteVmsUser(userId);
			}
		} catch (VmsNotAvailableException e) {
			logError("Cannot delete a vms user after a OLAT user deletion.", e);
		}
	}
	
	protected void deleteVmsUser(int userId) 
	throws VmsNotAvailableException {
		try {
			UserServiceStub userWs = getUserWebService();
			UserServiceStub.DeleteUserRequest delRequest = new UserServiceStub.DeleteUserRequest();
			UserServiceStub.Userid userIdType = new UserServiceStub.Userid();
			userIdType.setUserid(userId);
			delRequest.setDeleteUserRequest(userIdType);
			userWs.deleteUser(delRequest);
			
		} catch (AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot delete vms user: " + userId, f);
			}
		} catch(RemoteException e) {
			logError("Cannot delete vms user: " + userId, e);
		}
	}

	public List<Integer> getLicencedRoomSizes() 
	throws VmsNotAvailableException {
		List<Integer> roomSizes = new ArrayList<Integer>();
		try {
			LicenceServiceStub licenceWs = getLicenceWebService();
			LicenceServiceStub.GetModulesForCustomerRequest licenceRequest = new LicenceServiceStub.GetModulesForCustomerRequest();
			licenceRequest.setCustomerid(viteroModule.getCustomerId());
			
			LicenceServiceStub.GetModulesForCustomerResponse response = licenceWs.getModulesForCustomer(licenceRequest);
			LicenceServiceStub.Modulestype modules = response.getGetModulesForCustomerResponse();
			LicenceServiceStub.Modules_type0 modulesType = modules.getModules();
			for(LicenceServiceStub.Module_type0 module:modulesType.getModule()) {
				if("ROOM".equals(module.getType())) {
					Integer roomSize = module.getRoomsize();
					if(!roomSizes.contains(roomSize)) {
						roomSizes.add(roomSize);
					}
				}
			}
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case invalidAttribut: logError("ids <=0 or invalid attributs", f); break;
				default: logAxisError("Cannot get licence for customer: " + viteroModule.getCustomerId(), f);
			}
		} catch (RemoteException e) {
			logError("Cannot get licence for customer: " + viteroModule.getCustomerId(), e);
		}
		return roomSizes;
	}
	
	public List<Integer> getLicenceForAvailableRooms(Date begin, Date end) 
	throws VmsNotAvailableException {
		List<Integer> roomSizes = new ArrayList<Integer>();
		try {
			LicenceServiceStub licenceWs = getLicenceWebService();
			LicenceServiceStub.GetBookableRoomsForGroupRequest request = new LicenceServiceStub.GetBookableRoomsForGroupRequest();
			LicenceServiceStub.Grouprequesttype groupRequest = new LicenceServiceStub.Grouprequesttype();
			groupRequest.setStart(format(begin));
			groupRequest.setEnd(format(end));
			
			request.setGetBookableRoomsForGroupRequest(groupRequest);

			LicenceServiceStub.GetBookableRoomsForGroupResponse response = licenceWs.getBookableRoomsForGroup(request);
			Rooms_type0 rooms = response.getRooms();
			for(int roomSize : rooms.getRoomsize()) {
				if(!roomSizes.contains(roomSize)) {
					roomSizes.add(roomSize);
				}
			}
			
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot get licence for available room by dates.", f);
			}
		} catch (RemoteException e) {
			logError("Cannot get licence for available room by dates.", e);
		}
		return roomSizes;
	}
	
	public int createGroup(String groupName)
	throws VmsNotAvailableException {
		try {
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.CreateGroupRequest createRequest = new GroupServiceStub.CreateGroupRequest();
			GroupServiceStub.Groupnamecustomerid groupInfos = new GroupServiceStub.Groupnamecustomerid();
			groupInfos.setGroupname(groupName + "_OLAT_" + UUID.randomUUID().toString().replace("-", ""));
			groupInfos.setCustomerid(viteroModule.getCustomerId());
			createRequest.setGroup(groupInfos);
			
			GroupServiceStub.CreateGroupResponse response = groupWs.createGroup(createRequest);
			GroupServiceStub.Groupid groupId = response.getCreateGroupResponse();
			
			return groupId.getGroupid();
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot create a group",f);
			}
			return -1;
		} catch (RemoteException e) {
			logError("Cannot create a group.", e);
			return -1;
		}
	}
	
	public ViteroGroup getGroup(int id)
	throws VmsNotAvailableException {
		try {
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.GetGroupRequest getRequest = new GroupServiceStub.GetGroupRequest();
			GroupServiceStub.Groupid groupId = new GroupServiceStub.Groupid();
			groupId.setGroupid(id);
			getRequest.setGetGroupRequest(groupId);
			
			GroupServiceStub.GetGroupResponse response = groupWs.getGroup(getRequest);
			GroupServiceStub.Group group = response.getGetGroupResponse();
			GroupServiceStub.Completegrouptype groupType = group.getGroup();
			
			return convert(groupType);
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				default: logAxisError("Cannot create a group",f);
			}
			return null;
		} catch (RemoteException e) {
			logError("Cannot create a group.", e);
			return null;
		}
	}
	
	public boolean deleteGroup(ViteroBooking vBooking)
	throws VmsNotAvailableException {
		try {
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.DeleteGroupRequest deleteRequest = new GroupServiceStub.DeleteGroupRequest();
			GroupServiceStub.Groupid groupId = new GroupServiceStub.Groupid();
			groupId.setGroupid(vBooking.getGroupId());
			deleteRequest.setDeleteGroupRequest(groupId);
			groupWs.deleteGroup(deleteRequest);
			return true;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case groupDoesntExist: logError("Group doesn't exist!", f); break;
				case invalidAttribut: logError("Group id <= 0!", f);
				default: logAxisError("Cannot delete group: " + vBooking.getGroupId(), f);
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot delete group: " + vBooking.getGroupId(), e);
			return false;
		}
	}
	
	public boolean addToRoom(ViteroBooking booking, Identity identity, GroupRole role)
	throws VmsNotAvailableException {
		try {
			int userId = getVmsUserId(identity, true);
			if(userId < 0) {
				return false;
			}
			
			//update user information
			try {
				updateVmsUser(identity, userId);
				storePortrait(identity, userId);
			} catch (Exception e) {
				logError("Cannot update user on vitero system:" + identity.getName(), e);
			}
			
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.AddUserToGroupRequest addRequest = new GroupServiceStub.AddUserToGroupRequest();
			GroupServiceStub.Groupiduserid groupuserId = new GroupServiceStub.Groupiduserid();
			groupuserId.setGroupid(booking.getGroupId());
			groupuserId.setUserid(userId);

			addRequest.setAddUserToGroupRequest(groupuserId);
			groupWs.addUserToGroup(addRequest);
			
			if(role != null) {
				groupWs = getGroupWebService();
				GroupServiceStub.ChangeGroupRoleRequest roleRequest = new GroupServiceStub.ChangeGroupRoleRequest();
				roleRequest.setGroupid(booking.getGroupId());
				roleRequest.setUserid(userId);
				roleRequest.setRole(role.getVmsValue());
				groupWs.changeGroupRole(roleRequest);
			}
			
			return true;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case userDoesntExist: logError("The user doesn ́t exist!", f); break;
				case userNotAttachedToCustomer: logError("The user is not attached to the customer (to which this group belongs)", f); break;
				case groupDoesntExist: logError("The group doesn ́t exist", f); break;
				case invalidAttribut: logError("An id <= 0", f); break;
				default: logAxisError("Cannot add an user to a group", f);
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot add an user to a group", e);
			return false;
		}
	}
	
	public boolean removeFromRoom(ViteroBooking booking, Identity identity)
	throws VmsNotAvailableException {
		int userId = getVmsUserId(identity, true);
		if(userId < 0) {
			return true;//nothing to remove
		}
		return removeFromRoom(booking, userId);
	}
	
	public boolean removeFromRoom(ViteroBooking booking, int userId)
	throws VmsNotAvailableException {
		try {
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.RemoveUserFromGroupRequest removeRequest = new GroupServiceStub.RemoveUserFromGroupRequest();
			GroupServiceStub.Groupiduserid groupuserId = new GroupServiceStub.Groupiduserid();
			groupuserId.setGroupid(booking.getGroupId());
			groupuserId.setUserid(userId);
			removeRequest.setRemoveUserFromGroupRequest(groupuserId);
			groupWs.removeUserFromGroup(removeRequest);
			return true;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case userDoesntExist: logError("The user doesn ́t exist!", f); break;
				case groupDoesntExist: logError("The group doesn ́t exist", f); break;
				case invalidAttribut: logError("An id <= 0", f); break;
				default: logAxisError("Cannot remove an user from a group", f);
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot remove an user from a group", e);
			return false;
		}
	}
	
	public ViteroBooking createBooking(String resourceName)
	throws VmsNotAvailableException {
		ViteroBooking booking = new ViteroBooking();
		
		booking.setBookingId(-1);
		booking.setGroupId(-1);
		booking.setResourceName(resourceName);
		
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		if(minute < 10) {
			cal.set(Calendar.MINUTE, 15);
		} else if (minute < 25) {
			cal.set(Calendar.MINUTE, 30);
		} else if (minute < 40) {
			cal.set(Calendar.MINUTE, 45);
		} else {
			cal.add(Calendar.HOUR, 1);
			cal.set(Calendar.MINUTE, 0);
		}

		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		booking.setStart(cal.getTime());
		booking.setStartBuffer(15);
		cal.add(Calendar.HOUR, 1);
		booking.setEnd(cal.getTime());
		booking.setEndBuffer(15);
		
		List<Integer> roomSizes = getLicencedRoomSizes();
		if(!roomSizes.isEmpty()) {
			booking.setRoomSize(roomSizes.get(0));
		}
		return booking;
	}

	public ViteroStatus createBooking(BusinessGroup group, OLATResourceable ores, ViteroBooking vBooking)
	throws VmsNotAvailableException {
		Bookingtype booking = getBookingById(vBooking.getBookingId());
		if(booking != null) {
			logInfo("Booking already exists: " + vBooking.getBookingId());
			return new ViteroStatus();
		}

		try {
			//a group per meeting
			String groupName = vBooking.getGroupName();
			int groupId = createGroup(groupName);
			if(groupId < 0) {
				return new ViteroStatus(ErrorCode.unkown);
			}
			vBooking.setGroupId(groupId);
			
			//create the meeting with the new group
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.CreateBookingRequest createRequest = new BookingServiceStub.CreateBookingRequest();
			BookingServiceStub.Newbookingtype newBooking = new BookingServiceStub.Newbookingtype();
			//mandatory
			newBooking.setStart(format(vBooking.getStart()));
			newBooking.setEnd(format(vBooking.getEnd()));
			newBooking.setStartbuffer(vBooking.getStartBuffer());
			newBooking.setEndbuffer(vBooking.getEndBuffer());
			newBooking.setGroupid(groupId);
			newBooking.setRoomsize(vBooking.getRoomSize());
			
			//optional
			/*
			newBooking.setIgnorefaults(false);
			newBooking.setCafe(false);
			newBooking.setCapture(false);
			//phone
			BookingServiceStub.Phonetype phone = new BookingServiceStub.Phonetype();
			phone.setDialout(false);
			phone.setPhoneconference(false);
			phone.setShowdialogue(false);
			newBooking.setPhone(phone);

			newBooking.setPcstateokrequired(false);
			newBooking.setRepetitionpattern("once");
			newBooking.setRepetitionenddate("");
			*/
			newBooking.setTimezone(viteroModule.getTimeZoneId());
			
			createRequest.setBooking(newBooking);

			BookingServiceStub.CreateBookingResponse response = bookingWs.createBooking(createRequest);
			boolean bookingCollision = response.getBookingcollision();
			boolean moduleCollision = response.getModulecollision();
			int bookingId = response.getBookingid();
			
			if(bookingCollision) {
				return new ViteroStatus(ErrorCode.bookingCollision);
			} else if(moduleCollision) {
				return new ViteroStatus(ErrorCode.moduleCollision);
			}
			vBooking.setBookingId(bookingId);
			getOrCreateProperty(group, ores, vBooking);
			return new ViteroStatus();
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case invalidTimezone: logError("Invalid time zone!", f); break;
				case bookingCollision: logError("Booking collision!", f); break;
				case moduleCollision: logError("Invalid module selection!", f); break;
				case bookingInPast: logError("Booking in the past!", f); break;
				case licenseExpired: logError("License/customer expired!", f); break;
				default: logAxisError("Cannot create a booking.", f);
			}
			return new ViteroStatus(code);
		} catch (RemoteException e) {
			logError("Cannot create a booking.", e);
			return new ViteroStatus(ErrorCode.remoteException);
		}
	}
	
	/**
	 * There is not update on vms. We can only update some OLAT specific options.
	 * @param group
	 * @param ores
	 * @param vBooking
	 * @return
	 * @throws VmsNotAvailableException
	 */
	public ViteroBooking updateBooking(BusinessGroup group, OLATResourceable ores, ViteroBooking vBooking)
	throws VmsNotAvailableException {
		Bookingtype bookingType = getBookingById(vBooking.getBookingId());
		if(bookingType == null) {
			logInfo("Booking doesn't exist: " + vBooking.getBookingId());
			return null;
		}
		
		Booking booking = bookingType.getBooking();
		//set the vms values
		update(vBooking, booking);
		//update the property
		updateProperty(group, ores, vBooking);
		return vBooking;
	}
	
	public boolean deleteBooking(ViteroBooking vBooking)
	throws VmsNotAvailableException {
		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.DeleteBookingRequest deleteRequest = new BookingServiceStub.DeleteBookingRequest();
			deleteRequest.setBookingid(vBooking.getBookingId());

			BookingServiceStub.DeleteBookingResponse response = bookingWs.deleteBooking(deleteRequest);
			BigInteger state = response.getDeletestate();
			deleteGroup(vBooking);
			deleteProperty(vBooking);
			return state != null;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case bookingDoesntExist:
				case bookingDoesntExistPrime: {
					deleteGroup(vBooking);
					deleteProperty(vBooking);
					return true;//ok, vms deleted, group deleted...
				}
				default: {
					logAxisError("Cannot delete a booking.", f);
				}
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot delete a booking.", e);
			return false;
		}
	}
	
	public void deleteAll(BusinessGroup group, OLATResourceable ores) {
		try {
			List<Property> properties = propertyManager.listProperties(null, group, ores, VMS_CATEGORY, null);
			for(Property property:properties) {
				String bookingStr = property.getTextValue();
				ViteroBooking booking = deserializeViteroBooking(bookingStr);
				deleteBooking(booking);
			}
		} catch (VmsNotAvailableException e) {
			logError("", e);
			markAsZombie(group, ores);
		}
	}
	
	private final void markAsZombie(BusinessGroup group, OLATResourceable ores) {
		List<Property> properties = propertyManager.listProperties(null, group, ores, VMS_CATEGORY, null);
		for(Property property:properties) {
			property.setName(VMS_CATEGORY_ZOMBIE);
			propertyManager.updateProperty(property);
		}
	}
	
	public void slayZombies() {
		List<Property> properties = propertyManager.listProperties(null, null, null, VMS_CATEGORY_ZOMBIE, null);
		for(Property property:properties) {
			try {
				String bookingStr = property.getTextValue();
				ViteroBooking booking = deserializeViteroBooking(bookingStr);
				deleteBooking(booking);
			} catch (VmsNotAvailableException e) {
				//try later
				logDebug("Cannot clean-up vitero room, vms not available");
			} catch (Exception e) {
				logError("", e);
			}
		}
	}
	
	public List<ViteroBooking> getBookingInFutures(Identity identity)
	throws VmsNotAvailableException {
		int userId = getVmsUserId(identity, false);
		if(userId > 0) {
			Booking[] bookings = getBookingInFutureByUserId(userId);
			return convert(bookings);
		}
		return Collections.emptyList();
	}
	
	/**
	 * Return the 
	 * @param group The group (optional)
	 * @param ores The OLAT resourceable (of the course) (optional)
	 * @return
	 */
	public List<ViteroBooking> getBookings(BusinessGroup group, OLATResourceable ores)
	throws VmsNotAvailableException {
		List<Property> properties = propertyManager.listProperties(null, group, ores, VMS_CATEGORY, null);
		List<ViteroBooking> bookings = new ArrayList<ViteroBooking>();
		for(Property property:properties) {
			String bookingStr = property.getTextValue();
			ViteroBooking booking = deserializeViteroBooking(bookingStr);
			Bookingtype bookingType = getBookingById(booking.getBookingId());
			if(bookingType != null) {
				Booking vmsBooking = bookingType.getBooking();
				booking.setProperty(property);
				update(booking, vmsBooking);
				bookings.add(booking);
			}
		}
		return bookings;
	}
	
	protected Booking[] getBookingInFutureByUserId(int userId)
	throws VmsNotAvailableException {
		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.GetBookingListByUserInFutureRequest request = new BookingServiceStub.GetBookingListByUserInFutureRequest();
			request.setUserid(userId);
			request.setTimezone(viteroModule.getTimeZoneId());
			
			BookingServiceStub.GetBookingListByUserInFutureResponse response = bookingWs.getBookingListByUserInFuture(request);
			Bookinglist bookingList = response.getGetBookingListByUserInFutureResponse();
			
			return bookingList.getBooking();
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case userDoesntExist: logError("The user does not exist!", f); break;
				case invalidAttribut: logError("ids <= 0!", f); break;
				case invalidTimezone: logError("Invalid time zone!", f); break;
				default: logAxisError("Cannot get booking in future for user: " + userId, f);
			}
			return null;
		} catch (RemoteException e) {
			logError("Cannot get booking in future for custom: " + userId, e);
			return null;
		}
	}

	protected Bookingtype getBookingById(int id)
	throws VmsNotAvailableException {
		if(id < 0) return null;
		
		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.GetBookingByIdRequest bookingByIdRequest = new BookingServiceStub.GetBookingByIdRequest();
			BookingServiceStub.Bookingid bookingId = new BookingServiceStub.Bookingid();
			bookingId.setBookingid(id);
			bookingByIdRequest.setGetBookingByIdRequest(bookingId);
			BookingServiceStub.GetBookingByIdResponse response = bookingWs.getBookingById(bookingByIdRequest);
			Bookingtype booking = response.getGetBookingByIdResponse();
			return booking;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case invalidAttribut: logError("ids <= 0", f); break;
				case bookingDoesntExist: logError("The booking does not exist", f); break;
				default: logAxisError("Cannot get booking by id: " + id, f);
			}
			return null;
		} catch (RemoteException e) {
			logError("Cannot get booking by id: " + id, e);
			return null;
		}
	}
	
	public boolean checkConnection() {
		try {
			return checkConnection(viteroModule.getVmsURI().toString(), viteroModule.getAdminLogin(), 
					viteroModule.getAdminPassword(), viteroModule.getCustomerId());
		} catch (VmsNotAvailableException e) {
			return false;
		}
	}
	
	public boolean checkConnection(String url, String login, String password, int customerId)
	throws VmsNotAvailableException {
		try {
			LicenceServiceStub licenceWs = new LicenceServiceStub(url + "/services");
			SecurityHeader.addAdminSecurityHeader(login, password, licenceWs);
			
			LicenceServiceStub.GetModulesForCustomerRequest licenceRequest = new LicenceServiceStub.GetModulesForCustomerRequest();
			licenceRequest.setCustomerid(viteroModule.getCustomerId());
			
			LicenceServiceStub.GetModulesForCustomerResponse response = licenceWs.getModulesForCustomer(licenceRequest);
			LicenceServiceStub.Modulestype modules = response.getGetModulesForCustomerResponse();
			LicenceServiceStub.Modules_type0 modulesType = modules.getModules();
			return modulesType != null;
		} catch(AxisFault f) {
			ErrorCode code = handleAxisFault(f);
			switch(code) {
				case unsufficientRights: logError("Unsufficient rights", f); break;
			}
			return false;
		} catch (Exception e) {
			logWarn("Error checking connection", e);
			return false;
		}
	}
	
	//Utilities
	private final ErrorCode handleAxisFault(final AxisFault f) 
	throws VmsNotAvailableException {
		if(f.getFaultDetailElement() != null) {
			String msg = f.getFaultDetailElement().toString();
			int beginIndex = msg.indexOf("<errorCode>");
			int endIndex = msg.indexOf("</errorCode>");
			String errorCode = msg.substring(beginIndex + "<errorCode>".length(), endIndex);
			int code = Integer.parseInt(errorCode);
			return ErrorCode.find(code);
		} else if (f.getCause() instanceof ConnectTimeoutException) {
			throw new VmsNotAvailableException(f);
		}
		return ErrorCode.unkown;
	}
	
	private void logAxisError(String message, AxisFault f) {
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(message)) {
			sb.append(message);
		}
		
		if(f.getFaultDetailElement() != null) {
			if(sb.length() > 0) sb.append(" -> ");
			sb.append(f.getFaultDetailElement().toString());
		}
		
		if(StringHelper.containsNonWhitespace(f.getMessage())) {
			if(sb.length() > 0) sb.append(" -> ");
			sb.append(f.getMessage());
		}

		logError(sb.toString(), f);
	}
	
	private final List<ViteroBooking> convert(Booking[] bookings) {
		List<ViteroBooking> viteroBookings = new ArrayList<ViteroBooking>();
		
		if(bookings != null && bookings.length > 0) {
			for(Booking b:bookings) {
				viteroBookings.add(convert(b));
			}
		}

		return viteroBookings;
	}
	
	private final ViteroBooking convert(Booking booking) {
		ViteroBooking vb = new ViteroBooking();
		return update(vb, booking);
	}
	
	private final ViteroBooking update(ViteroBooking vb, Booking booking) {
		vb.setBookingId(booking.getBookingid());
		vb.setGroupId(booking.getGroupid());
		vb.setRoomSize(booking.getRoomsize());
		vb.setStart(parse(booking.getStart()));
		vb.setStartBuffer(booking.getStartbuffer());
		vb.setEnd(parse(booking.getEnd()));
		vb.setEndBuffer(booking.getEndbuffer());
		return vb;
	}
	
	private final ViteroGroup convert(Completegrouptype groupType) {
		ViteroGroup vg = new ViteroGroup();
		vg.setGroupId(groupType.getId());
		vg.setName(groupType.getName());
		int numOfParticipants = groupType.getParticipant() == null ? 0 : groupType.getParticipant().length;
		vg.setNumOfParticipants(numOfParticipants);
		return vg;
	}
	
	private final List<ViteroUser> convert(Usertype[] userTypes) {
		List<ViteroUser> vUsers = new ArrayList<ViteroUser>();
		if(userTypes != null) {
			for(Usertype userType:userTypes) {
				vUsers.add(convert(userType));
			}
		}
		return vUsers;
	}
	
	private final ViteroUser convert(Usertype userType) {
		ViteroUser vu = new ViteroUser();
		vu.setUserId(userType.getId());
		vu.setFirstName(userType.getFirstname());
		vu.setLastName(userType.getSurname());
		vu.setEmail(userType.getEmail());
		return vu;
	}
	
	//Properties
	private final Property getProperty(final BusinessGroup group, final OLATResourceable courseResource, final ViteroBooking booking) {
		String propertyName = Integer.toString(booking.getBookingId());
		return propertyManager.findProperty(null, group, courseResource, VMS_CATEGORY, propertyName);
	}
	
	private final Property getOrCreateProperty(final BusinessGroup group, final OLATResourceable courseResource, final ViteroBooking booking) {
		Property property = getProperty(group, courseResource, booking);
		if(property == null) {
			property = createProperty(group, courseResource, booking);
			propertyManager.saveProperty(property);
		}
		return property;
	}
	
	private final Property updateProperty(final BusinessGroup group, final OLATResourceable courseResource, ViteroBooking booking) {
		Property property = getProperty(group, courseResource, booking);
		if(property == null) {
			property = createProperty(group, courseResource, booking);
			propertyManager.saveProperty(property);
		} else {
			String serialized = serializeViteroBooking(booking);
			property.setTextValue(serialized);
			propertyManager.updateProperty(property);
		}
		return property;
	}
	
	private final Property createProperty(final BusinessGroup group, final OLATResourceable courseResource, ViteroBooking booking) {
		String serialized = serializeViteroBooking(booking);
		String bookingId = Integer.toString(booking.getBookingId());
		Long groupId = new Long(booking.getGroupId());
		return propertyManager.createPropertyInstance(null, group, courseResource, VMS_CATEGORY, bookingId, null, groupId, null, serialized);
	}
	
	private final void deleteProperty(ViteroBooking vBooking) {
		String bookingId = Integer.toString(vBooking.getBookingId());
		propertyManager.deleteProperties(null, null, null, VMS_CATEGORY, bookingId);
	}
	
	private final String serializeViteroBooking(ViteroBooking booking) {
		StringWriter writer = new StringWriter();
		xStream.marshal(booking, new CompactWriter(writer));
		writer.flush();
		return writer.toString();
	}
	
	private final ViteroBooking deserializeViteroBooking(String booking) {
		return (ViteroBooking)xStream.fromXML(booking);
	}
	
	//Factories for service stubs
	private final  BookingServiceStub getBookingWebService() 
	throws AxisFault {
		BookingServiceStub bookingWs = new BookingServiceStub(getVmsEndPoint());
		SecurityHeader.addAdminSecurityHeader(viteroModule, bookingWs);
		return bookingWs;
	}
	
	private final LicenceServiceStub getLicenceWebService()
	throws AxisFault {
		LicenceServiceStub licenceWs = new LicenceServiceStub(getVmsEndPoint());
		SecurityHeader.addAdminSecurityHeader(viteroModule, licenceWs);
		return licenceWs;
	}
	
	private final GroupServiceStub getGroupWebService()
	throws AxisFault {
		GroupServiceStub groupWs = new GroupServiceStub(getVmsEndPoint());
		SecurityHeader.addAdminSecurityHeader(viteroModule, groupWs);
		return groupWs;
	}
	
	private final UserServiceStub getUserWebService()
	throws AxisFault {
		UserServiceStub userWs = new UserServiceStub(getVmsEndPoint());
		SecurityHeader.addAdminSecurityHeader(viteroModule, userWs);
		return userWs;
	}
	
	private final MtomServiceStub getMtomWebService() 
	throws AxisFault {
		MtomServiceStub mtomWs = new MtomServiceStub(getVmsEndPoint());
		SecurityHeader.addAdminSecurityHeader(viteroModule, mtomWs);
		return mtomWs;
	}
	
	private final SessionCodeServiceStub getSessionCodeWebService()
	throws AxisFault {
		SessionCodeServiceStub sessionCodeWs = new SessionCodeServiceStub(getVmsEndPoint());
		SecurityHeader.addAdminSecurityHeader(viteroModule, sessionCodeWs);
		return sessionCodeWs;
	}

	private final String getVmsEndPoint() {
	    UriBuilder builder = UriBuilder.fromUri(viteroModule.getVmsURI());
	    builder.path("services");
	    return builder.build().toString();
	}
	
	private final String getStartPoint(String sessionCode) {
		UriBuilder builder = UriBuilder.fromUri(viteroModule.getVmsURI() );
	    builder.path("start.htm");
	    if(StringHelper.containsNonWhitespace(sessionCode)) {
	    	builder.queryParam("sessionCode", sessionCode);
	    }
	    return builder.build().toString();
	}
	
	private final String getGroupURL(String sessionCode, int groupId) {
		UriBuilder builder = UriBuilder.fromUri(viteroModule.getVmsURI() );
	    builder.path("/user/cms/groupfolder.htm");
	    builder.queryParam("code", sessionCode);
    	builder.queryParam("fl", "1");
    	builder.queryParam("groupId", Integer.toString(groupId));
	    return builder.build().toString();
	}
	
	private final synchronized String format(Date date) {
		return dateFormat.format(date);
	}
	
	private final synchronized Date parse(String dateString) {
		try {
			return dateFormat.parse(dateString);
		} catch (ParseException e) {
			logError("Cannot parse a date: " + dateString, e);
			return null;
		}
	}
}
