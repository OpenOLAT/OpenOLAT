/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.manager;

import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriBuilder;

import org.apache.axis2.AxisFault;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.DisplayPortraitManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frentix.olat.vitero.ViteroModule;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub.Booking;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub.Bookinglist;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub.Bookingtype;
import com.frentix.olat.vitero.manager.stubs.CustomerServiceStub;
import com.frentix.olat.vitero.manager.stubs.GroupServiceStub;
import com.frentix.olat.vitero.manager.stubs.LicenceServiceStub;
import com.frentix.olat.vitero.manager.stubs.LicenceServiceStub.Rooms_type0;
import com.frentix.olat.vitero.manager.stubs.MtomServiceStub;
import com.frentix.olat.vitero.manager.stubs.SessionCodeServiceStub;
import com.frentix.olat.vitero.manager.stubs.SessionCodeServiceStub.Codetype;
import com.frentix.olat.vitero.manager.stubs.UserServiceStub;
import com.frentix.olat.vitero.manager.stubs.UserServiceStub.Userid;
import com.frentix.olat.vitero.model.ViteroBooking;
import com.ibm.icu.util.Calendar;
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
public class ViteroManager extends BasicManager {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
	
	private static final String VMS_PROVIDER = "VMS";
	private static final String VMS_CATEGORY = "vitero-category";
	
	@Autowired
	private ViteroModule viteroModule;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private BaseSecurity securityManager;
	
	private XStream xStream;

	public ViteroManager() {
		//make Spring happy
	}
	
	@PostConstruct
	public void init() {
		xStream = XStreamHelper.createXStreamInstance();
		xStream.alias("vBooking", ViteroBooking.class);
	}
	
	public void setViteroModule(ViteroModule module) {
		this.viteroModule = module;
	}
	
	public void getLicence(Date begin, Date end) {
		try {
			LicenceServiceStub licenceWs = getLicenceWebService();
			LicenceServiceStub.GetBookableRoomsForGroupRequest request = new LicenceServiceStub.GetBookableRoomsForGroupRequest();
			LicenceServiceStub.Grouprequesttype groupRequest = new LicenceServiceStub.Grouprequesttype();
			groupRequest.setStart(format(begin));
			groupRequest.setEnd(format(end));
			
			request.setGetBookableRoomsForGroupRequest(groupRequest);

			LicenceServiceStub.GetBookableRoomsForGroupResponse response = licenceWs.getBookableRoomsForGroup(request);
			Rooms_type0 rooms = response.getRooms();
			int[] roomSize = rooms.getRoomsize();
			System.out.println(roomSize);
		} catch (RemoteException e) {
			logError("", e);
		}
	}
	
	public List<ViteroBooking> getBookingByDate(Date start, Date end) {
		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.GetBookingListByDateRequest dateRequest = new BookingServiceStub.GetBookingListByDateRequest();
			dateRequest.setStart(format(start));
			dateRequest.setEnd(format(end));
			BookingServiceStub.GetBookingListByDateResponse response = bookingWs.getBookingListByDate(dateRequest);
			
			BookingServiceStub.Bookinglist bookingList = response.getGetBookingListByDateResponse();
			Booking[] bookings = bookingList.getBooking();
			return convert(bookings);
		} catch(AxisFault f) {
			String msg = f.getFaultDetailElement().toString();
			logError(msg, f);
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
	
	public List<ViteroBooking> getBookingInFutures(Identity identity) {
		int userId = getVmsUserId(identity);
		Booking[] bookings = getBookingInFutureByCustomerId(userId);
		return convert(bookings);
	}
	
	public List<ViteroBooking> getBookings() {
		List<Property> properties = propertyManager.listProperties(null, null, null, VMS_CATEGORY, null);
		List<ViteroBooking> bookings = new ArrayList<ViteroBooking>();
		for(Property property:properties) {
			String bookingStr = property.getTextValue();
			ViteroBooking booking = deserializeViteroBooking(bookingStr);
			bookings.add(booking);
		}
		return bookings;
	}
	
	public List<ViteroBooking> getBookings(BusinessGroup group, OLATResourceable ores) {
		List<Property> properties = propertyManager.listProperties(null, group, ores, VMS_CATEGORY, null);
		List<ViteroBooking> bookings = new ArrayList<ViteroBooking>();
		for(Property property:properties) {
			String bookingStr = property.getTextValue();
			ViteroBooking booking = deserializeViteroBooking(bookingStr);
			bookings.add(booking);
		}
		return bookings;
	}
	
	public String getURLToBooking(Identity identity, ViteroBooking booking) {
		String sessionCode = createSessionCode(identity, booking);
		String url = getStartPoint(sessionCode);
		return url;
	}
	
	public String serializeViteroBooking(ViteroBooking booking) {
		StringWriter writer = new StringWriter();
		xStream.marshal(booking, new CompactWriter(writer));
		writer.flush();
		return writer.toString();
	}
	
	public ViteroBooking deserializeViteroBooking(String booking) {
		return (ViteroBooking)xStream.fromXML(booking);
	}
	
	/**
	 * Create a session code with a one hour expiration date
	 * @param identity
	 * @param booking
	 * @return
	 */
	protected String createSessionCode(Identity identity, ViteroBooking booking) {
		try {
			int userId = getVmsUserId(identity);
			SessionCodeServiceStub sessionCodeWs = this.getSessionCodeWebService();
			SessionCodeServiceStub.CreatePersonalBookingSessionCodeRequest codeRequest = new SessionCodeServiceStub.CreatePersonalBookingSessionCodeRequest();
			
			SessionCodeServiceStub.Sessioncode_type2 code = new SessionCodeServiceStub.Sessioncode_type2();
			code.setBookingid(booking.getBookingId());
			code.setUserid(userId);
		
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, 1);
			code.setExpirationdate(format(cal.getTime()));

			codeRequest.setSessioncode(code);
			
			SessionCodeServiceStub.CreatePersonalBookingSessionCodeResponse response = sessionCodeWs.createPersonalBookingSessionCode(codeRequest);
			Codetype myCode = response.getCreatePersonalBookingSessionCodeResponse();
			return myCode.getCode();
		} catch(AxisFault f) {
			String msg = f.getFaultDetailElement().toString();
			if(msg.contains("<errorCode>303</errorCode>")) {
				logError("Invalid attribute", f);
			} else if(msg.contains("<errorCode>304</errorCode>")) {
				logError("Invalid time zone", f);
			} else if(msg.contains("<errorCode>53</errorCode>")) {
				logError("User does not exist", f);
			} else if(msg.contains("<errorCode>506</errorCode>") || msg.contains("<errorCode>509</errorCode>")) {
				logError("Booking does not exist", f);
			} else if(msg.contains("<errorCode>153</errorCode>")) {
				logError("User not assigned to group!", f);
			} else {
				logError(msg, f);
			}
			logError(msg, f);
			return null;
		} catch (RemoteException e) {
			logError("", e);
			return null;
		}
	}
	
	public int getVmsUserId(Identity identity) {
		int userId;
		Authentication authentication = securityManager.findAuthentication(identity, VMS_PROVIDER);
		if(authentication == null) {
			userId =  createVmsUser(identity);
			if(userId > 0) {
				securityManager.createAndPersistAuthentication(identity, VMS_PROVIDER, Integer.toString(userId), "");
			}
		} else {
			userId = Integer.parseInt(authentication.getAuthusername());
		}
		return userId;
	}
	
	protected int createVmsUser(Identity identity) {
		try {
			UserServiceStub userWs = getUserWebService();
			UserServiceStub.CreateUserRequest createRequest = new UserServiceStub.CreateUserRequest();
			UserServiceStub.Newusertype user = new UserServiceStub.Newusertype();
			
			//mandatory
			User olatUser = identity.getUser();
			user.setUsername("olat." + identity.getName());
			user.setSurname(olatUser.getProperty(UserConstants.LASTNAME, null));
			user.setFirstname(olatUser.getProperty(UserConstants.FIRSTNAME, null));
			user.setEmail(olatUser.getProperty(UserConstants.EMAIL, null));
			user.setPassword("changeme");
			
			UserServiceStub.Idlist customerIds = new UserServiceStub.Idlist();
			customerIds.set_int(new int[]{viteroModule.getCustomerId()});
			user.setCustomeridlist(customerIds);

			//optional
			user.setLocale("en");
			user.setPcstate("NOT_TESTED");
			
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
			
			/*
			user.setTitle("");
			user.setCompany("");
			user.setTimezone("");
			*/
			TimeZone.getDefault().getID();
			
			
			user.setTechnicalnote("Generated by OpenOLAT");
			
			createRequest.setUser(user);
			UserServiceStub.CreateUserResponse response = userWs.createUser(createRequest);
			Userid userId = response.getCreateUserResponse();
			
			storePortrait(identity, userId.getUserid());

			return userId.getUserid();
		} catch (RemoteException e) {
			logError("Cannot create vms user.", e);
			return -1;
		}
	}
	
	protected boolean storePortrait(Identity identity, int userId) {
		try {

			File portraitDir = DisplayPortraitManager.getInstance().getPortraitDir(identity);
			File portrait = new File(portraitDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME);
			if(portrait.exists()) {
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
		} catch (RemoteException e) {
			logError("", e);
			return false;
		}
	}
	
	public ViteroBooking createBooking() {
		ViteroBooking booking = new ViteroBooking();
		
		booking.setBookingId(-1);
		booking.setGroupId(-1);
		
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
		booking.setStartBuffer(0);
		cal.add(Calendar.HOUR, 1);
		booking.setEnd(cal.getTime());
		booking.setEndBuffer(0);
		
		List<Integer> roomSizes = getLicencedRoomSizes();
		if(!roomSizes.isEmpty()) {
			booking.setRoomSize(roomSizes.get(0));
		}
		return booking;
	}
	
	public List<Integer> getLicencedRoomSizes() {
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
		} catch (RemoteException e) {
			logError("Cannot get licence for customer: " + viteroModule.getCustomerId(), e);
		}
		return roomSizes;
	}
	
	public int createGroup() {
		try {
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.CreateGroupRequest createRequest = new GroupServiceStub.CreateGroupRequest();
			GroupServiceStub.Groupnamecustomerid groupInfos = new GroupServiceStub.Groupnamecustomerid();
			groupInfos.setGroupname("OLAT-" + UUID.randomUUID().toString());
			groupInfos.setCustomerid(viteroModule.getCustomerId());
			createRequest.setGroup(groupInfos);
			
			GroupServiceStub.CreateGroupResponse response = groupWs.createGroup(createRequest);
			GroupServiceStub.Groupid groupId = response.getCreateGroupResponse();
			
			return groupId.getGroupid();
		} catch (RemoteException e) {
			logError("Cannot create a group.", e);
			return -1;
		}
	}
	
	public boolean addToRoom(ViteroBooking booking, Identity identity) {
		try {
			int userId = getVmsUserId(identity);
			if(userId < 0) {
				return false;
			}
			
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.AddUserToGroupRequest addRequest = new GroupServiceStub.AddUserToGroupRequest();
			GroupServiceStub.Groupiduserid groupuserId = new GroupServiceStub.Groupiduserid();
			groupuserId.setGroupid(booking.getGroupId());
			groupuserId.setUserid(userId);
			
			addRequest.setAddUserToGroupRequest(groupuserId);
			groupWs.addUserToGroup(addRequest);
			return true;
		} catch(AxisFault f) {
			String msg = f.getFaultDetailElement().toString();
			if(msg.contains("<errorCode>53</errorCode>")) {
				logError("The user doesn ́t exist!", f);
			} else if(msg.contains("<errorCode>103</errorCode>")) {
				logError("The user is not attached to the customer (to which this group belongs)", f);
			} else if(msg.contains("<errorCode>151</errorCode>")) {
				logError("The group doesn ́t exist", f);
			}  else if(msg.contains("<errorCode>303</errorCode>")) {
				logError("An id <= 0", f);
			} else {
				logError(msg, f);
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot add an user to a group", e);
			return false;
		}
	}
	
	public boolean removeFromRoom(ViteroBooking booking, Identity identity) {
		try {
			int userId = getVmsUserId(identity);
			if(userId < 0) {
				return true;//nothing to remove
			}
			
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.RemoveUserFromGroupRequest removeRequest = new GroupServiceStub.RemoveUserFromGroupRequest();
			GroupServiceStub.Groupiduserid groupuserId = new GroupServiceStub.Groupiduserid();
			groupuserId.setGroupid(booking.getGroupId());
			groupuserId.setUserid(userId);
			removeRequest.setRemoveUserFromGroupRequest(groupuserId);
			groupWs.removeUserFromGroup(removeRequest);
			return true;
		} catch(AxisFault f) {
			String msg = f.getFaultDetailElement().toString();
			if(msg.contains("<errorCode>53</errorCode>")) {
				logError("The user doesn ́t exist!", f);
			} else if(msg.contains("<errorCode>151</errorCode>")) {
				logError("The group doesn ́t exist", f);
			}  else if(msg.contains("<errorCode>303</errorCode>")) {
				logError("An id <= 0", f);
			} else {
				logError(msg, f);
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot add an user to a group", e);
			return false;
		}
	}
	
	public ViteroBooking updateBooking(BusinessGroup group, OLATResourceable ores, ViteroBooking vBooking) {
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

	public boolean createBooking(BusinessGroup group, OLATResourceable ores, ViteroBooking vBooking) {
		Bookingtype booking = getBookingById(vBooking.getBookingId());
		if(booking != null) {
			logInfo("Booking already exists: " + vBooking.getBookingId());
			return true;
		}

		try {
			//a group per meeting
			int groupId = createGroup();
			if(groupId < 0) {
				return false;
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
			//newBooking.setTimezone("Africa/Ceuta");
			newBooking.setTimezone("Africa/Ceuta");
			
			createRequest.setBooking(newBooking);

			BookingServiceStub.CreateBookingResponse response = bookingWs.createBooking(createRequest);
			boolean bookingCollision = response.getBookingcollision();
			boolean moduleCollision = response.getModulecollision();
			int bookingId = response.getBookingid();
			
			if(!bookingCollision && !moduleCollision) {
				vBooking.setBookingId(bookingId);
				getOrCreateProperty(group, ores, vBooking);
				return true;
			}
			return false;
		} catch(AxisFault f) {
			String msg = f.getFaultDetailElement().toString();
			if(msg.contains("<errorCode>502</errorCode>")) {
				logError("Invalid module selection!", f);
			} else if(msg.contains("<errorCode>505</errorCode>")) {
				logError("Booking in the past!", f);
			} else if(msg.contains("<errorCode>505</errorCode>")) {
				logError("Booking in the past!", f);
			} else if(msg.contains("<errorCode>501</errorCode>")) {
				logError("Booking collision!", f);
			} else if(msg.contains("<errorCode>703</errorCode>")) {
				logError("License/customer expired!", f);
			} else if(msg.contains("<errorCode>304</errorCode>")) {
				logError("Invalid time zone!", f);
			} else {
				logError(msg, f);
			}
			return false;
		} catch (RemoteException e) {
			logError("", e);
			return false;
		}
	}
	
	public boolean deleteBooking(ViteroBooking vBooking) {
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
			String msg = f.getFaultDetailElement().toString();
			if(msg.contains("<errorCode>509</errorCode>") || msg.contains("<errorCode>506</errorCode>")) {
				//why is vms sending this error if it deletes the room???
				deleteGroup(vBooking);
				deleteProperty(vBooking);
			} else {
				logError(f.getFaultDetailElement().toString(), f);
			}
			return false;
		} catch (RemoteException e) {
			logError("", e);
			return false;
		}
	}
	
	public boolean deleteGroup(ViteroBooking vBooking) {
		try {
			GroupServiceStub groupWs = getGroupWebService();
			GroupServiceStub.DeleteGroupRequest deleteRequest = new GroupServiceStub.DeleteGroupRequest();
			GroupServiceStub.Groupid groupId = new GroupServiceStub.Groupid();
			groupId.setGroupid(vBooking.getGroupId());
			deleteRequest.setDeleteGroupRequest(groupId);
			groupWs.deleteGroup(deleteRequest);
			return true;
		} catch(AxisFault f) {
			String msg = f.getFaultDetailElement().toString();
			if(msg.contains("<errorCode>151</errorCode>")) {
				logError("Group doesn't exist!", f);
			} else if(msg.contains("<errorCode>303</errorCode>")) {
				logError("Group id <= 0!", f);
			} else {
				logError(msg, f);
			}
			return false;
		} catch (RemoteException e) {
			logError("Cannot delete group: " + vBooking.getGroupId(), e);
			return false;
		}
		
	}
	
	public void deleteBookings(BusinessGroup group, OLATResourceable ores) {
		
	}
	
	protected void deleteProperty(ViteroBooking vBooking) {
		String bookingId = Integer.toString(vBooking.getBookingId());
		propertyManager.deleteProperties(null, null, null, VMS_CATEGORY, bookingId);
	}
	
	public String getTimeZoneId() {
		return "Africa/Ceuta";
	}
	
	protected Booking[] getBookingInFutureByCustomerId(int userId) {
		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.GetBookingListByUserInFutureRequest request = new BookingServiceStub.GetBookingListByUserInFutureRequest();
			request.setUserid(userId);
			request.setTimezone(getTimeZoneId());
			
			BookingServiceStub.GetBookingListByUserInFutureResponse response = bookingWs.getBookingListByUserInFuture(request);
			Bookinglist bookingList = response.getGetBookingListByUserInFutureResponse();
			
			return bookingList.getBooking();
		} catch(AxisFault f) {
			String msg = f.getFaultDetailElement().toString();
			if(msg.contains("<errorCode>304</errorCode>")) {
				logError("Invalid time zone!", f);
			} else if(msg.contains("<errorCode>53</errorCode>")) {
				logError("The user does not exist!", f);
			} else if(msg.contains("<errorCode>303</errorCode>")) {
				logError("ids <= 0!", f);
			} else {
				logError(msg, f);
			}
			logError(msg, f);
			return null;
		} catch (RemoteException e) {
			logError("", e);
			return null;
		}
	}
	
	protected List<ViteroBooking> convert(Booking[] bookings) {
		List<ViteroBooking> viteroBookings = new ArrayList<ViteroBooking>();
		
		if(bookings != null && bookings.length > 0) {
			for(Booking b:bookings) {
				viteroBookings.add(convert(b));
			}
		}

		return viteroBookings;
	}
	
	protected ViteroBooking convert(Booking booking) {
		ViteroBooking vb = new ViteroBooking();
		return update(vb, booking);
	}
	
	protected ViteroBooking update(ViteroBooking vb, Booking booking) {
		vb.setBookingId(booking.getBookingid());
		vb.setGroupId(booking.getGroupid());
		vb.setRoomSize(booking.getRoomsize());
		vb.setStart(parse(booking.getStart()));
		vb.setStartBuffer(booking.getEndbuffer());
		vb.setEnd(parse(booking.getEnd()));
		vb.setEndBuffer(booking.getStartbuffer());
		return vb;
	}
	
	protected Property getProperty(final BusinessGroup group, final OLATResourceable courseResource, final ViteroBooking booking) {
		String propertyName = Integer.toString(booking.getBookingId());
		return propertyManager.findProperty(null, group, courseResource, VMS_CATEGORY, propertyName);
	}
	
	protected Property getOrCreateProperty(final BusinessGroup group, final OLATResourceable courseResource, final ViteroBooking booking) {
		Property property = getProperty(group, courseResource, booking);
		if(property == null) {
			property = createProperty(group, courseResource, booking);
			propertyManager.saveProperty(property);
		}
		return property;
	}
	
	protected Property updateProperty(final BusinessGroup group, final OLATResourceable courseResource, ViteroBooking booking) {
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
	
	protected Property createProperty(final BusinessGroup group, final OLATResourceable courseResource, ViteroBooking booking) {
		String serialized = serializeViteroBooking(booking);
		String bookingId = Integer.toString(booking.getBookingId());
		Long groupId = new Long(booking.getGroupId());
		return propertyManager.createPropertyInstance(null, group, courseResource, VMS_CATEGORY, bookingId, null, groupId, null, serialized);
	}
	
	protected Bookingtype getBookingById(int id) {
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
		} catch (RemoteException e) {
			logError("Cannot get booking by id: " + id, e);
			return null;
		}
	}
	
	public boolean checkConnection(String url, String login, String password, int customerId) {
		try {
			CustomerServiceStub customerWs = new CustomerServiceStub(url + "/services");
			SecurityHeader.addAdminSecurityHeader(login, password, customerWs);
			
			CustomerServiceStub.GetCustomerRequest cRequest = new CustomerServiceStub.GetCustomerRequest();
			CustomerServiceStub.Customerid id = new CustomerServiceStub.Customerid();
			id.setCustomerid(customerId);
			cRequest.setGetCustomerRequest(id);
			CustomerServiceStub.GetCustomerResponse response = customerWs.getCustomer(cRequest);
			if(response == null) return false;
			CustomerServiceStub.Customer customer = response.getGetCustomerResponse();
			if(customer == null) return false;
			CustomerServiceStub.Customertype customerType = customer.getCustomer();
			if(customerType == null) return false;
			return customerType.getId() > -1;
		} catch (Exception e) {
			logWarn("Error checking connection", e);
			return false;
		}
	}
	
	protected BookingServiceStub getBookingWebService() {
		try {
			BookingServiceStub bookingWs = new BookingServiceStub(getVmsEndPoint());
			SecurityHeader.addAdminSecurityHeader(viteroModule, bookingWs);
			return bookingWs;
		} catch (AxisFault e) {
			logError("Cannot create booking ws.", e);
			return null;
		}
	}
	
	protected LicenceServiceStub getLicenceWebService() {
		try {
			LicenceServiceStub licenceWs = new LicenceServiceStub(getVmsEndPoint());
			SecurityHeader.addAdminSecurityHeader(viteroModule, licenceWs);
			return licenceWs;
		} catch (AxisFault e) {
			logError("Cannot create licence ws.", e);
			return null;
		}
	}
	
	protected GroupServiceStub getGroupWebService() {
		try {
			GroupServiceStub groupWs = new GroupServiceStub(getVmsEndPoint());
			SecurityHeader.addAdminSecurityHeader(viteroModule, groupWs);
			return groupWs;
		} catch (AxisFault e) {
			logError("Cannot create group ws.", e);
			return null;
		}
	}
	
	protected UserServiceStub getUserWebService() {
		try {
			UserServiceStub userWs = new UserServiceStub(getVmsEndPoint());
			SecurityHeader.addAdminSecurityHeader(viteroModule, userWs);
			return userWs;
		} catch (AxisFault e) {
			logError("Cannot create user ws.", e);
			return null;
		}
	}
	
	protected MtomServiceStub getMtomWebService() {
		try {
			MtomServiceStub mtomWs = new MtomServiceStub(getVmsEndPoint());
			SecurityHeader.addAdminSecurityHeader(viteroModule, mtomWs);
			return mtomWs;
		} catch (AxisFault e) {
			logError("Cannot create user ws.", e);
			return null;
		}
	}
	
	protected SessionCodeServiceStub getSessionCodeWebService() {
		try {
			SessionCodeServiceStub sessionCodeWs = new SessionCodeServiceStub(getVmsEndPoint());
			SecurityHeader.addAdminSecurityHeader(viteroModule, sessionCodeWs);
			return sessionCodeWs;
		} catch (AxisFault e) {
			logError("Cannot create user ws.", e);
			return null;
		}
	}

	protected String getVmsEndPoint() {
	    UriBuilder builder = UriBuilder.fromUri(viteroModule.getVmsURI());
	    builder.path("services");
	    return builder.build().toString();
	}
	
	protected String getStartPoint(String sessionCode) {
		UriBuilder builder = UriBuilder.fromUri(viteroModule.getVmsURI() );
	    builder.path("start.html");
	    if(StringHelper.containsNonWhitespace(sessionCode)) {
	    	builder.queryParam("sessionCode", sessionCode);
	    }
	    return builder.build().toString();
	}
	
	protected synchronized String format(Date date) {
		return dateFormat.format(date);
	}
	
	protected synchronized Date parse(String dateString) {
		try {
			return dateFormat.parse(dateString);
		} catch (ParseException e) {
			logError("Cannot parse a date: " + dateString, e);
			return null;
		}
	}
}
