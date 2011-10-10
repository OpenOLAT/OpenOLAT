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
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.DisplayPortraitManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frentix.olat.course.nodes.vitero.ViteroBookingConfiguration;
import com.frentix.olat.vitero.ViteroModule;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub.Booking;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub.Bookinglist;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub.Bookingtype;
import com.frentix.olat.vitero.manager.stubs.CustomerServiceStub;
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
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
	
	private static final String VMS_PROVIDER = "VMS";
	private static final String VMS_CATEGORY = "vitero-category";
	private static final String COLLABORATION_TOOL_PROP_NAME = "collaboration-tool";
	
	@Autowired
	private ViteroModule viteroModule;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
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
	
	public List<ViteroBooking> getBookings(BusinessGroup group, OLATResourceable ores, Identity identity) {
		int userId = getVmsUserId(identity);
		Booking[] bookings = getBookingInFutureByCustomerId(userId);
		return convert(bookings);
	}
	
	public String getURLToBooking(Identity identity, ViteroBooking booking) {
		String sessionCode = createSessionCode(identity, booking);
		String url = getStartPoint(sessionCode);
		return url;
	}
	
	public String serializeViteroBooking(ViteroBooking booking) {
		return xStream.toXML(booking);
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
			UserServiceStub userWs = this.getUserWebService();
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

	public boolean createRoom(BusinessGroup group, OLATResourceable ores, Date begin, Date end, ViteroBookingConfiguration config) {
		ViteroBookingConfiguration vConfig = (ViteroBookingConfiguration)config;
		Bookingtype booking = getBookingById(vConfig.getBookingId());
		if(booking != null) {
			logInfo("Booking already exists: " + vConfig.getBookingId());
			return true;
		}

		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.CreateBookingRequest createRequest = new BookingServiceStub.CreateBookingRequest();
			
			BookingServiceStub.Newbookingtype newBooking = new BookingServiceStub.Newbookingtype();
			//mandatory
			newBooking.setStart(format(begin));
			newBooking.setEnd(format(end));
			newBooking.setStartbuffer(15);
			newBooking.setEndbuffer(15);
			newBooking.setGroupid(1);
			newBooking.setRoomsize(20);
			
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
			newBooking.setTimezone("Africa/Ceuta");
			*/
			createRequest.setBooking(newBooking);

			BookingServiceStub.CreateBookingResponse response = bookingWs.createBooking(createRequest);
			boolean bookingCollision = response.getBookingcollision();
			boolean moduleCollision = response.getModulecollision();
			int bookingId = response.getBookingid();
			
			if(!bookingCollision && !moduleCollision) {
				vConfig.setBookingId(bookingId);
				return true;
			}
			return false;
		} catch (RemoteException e) {
			logError("", e);
			return false;
		}
	}
	
	public boolean deleteBooking(ViteroBooking vBooking) {
		try {
			Bookingtype booking = getBookingById(vBooking.getBookingId());
			if(booking == null) {
				//already deleted, do nothing
				return true;
			}

			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.DeleteBookingRequest deleteRequest = new BookingServiceStub.DeleteBookingRequest();
			deleteRequest.setBookingid(vBooking.getBookingId());
			BookingServiceStub.DeleteBookingResponse response = bookingWs.deleteBooking(deleteRequest);
			
			BigInteger state = response.getDeletestate();
			return state != null;
			
		} catch (RemoteException e) {
			logError("", e);
			return false;
		}
	}
	
	public boolean isConfigValid(ViteroBookingConfiguration config) {
		return true;
	}



	public boolean removeTeaRoom(String roomId, ViteroBookingConfiguration config) {
		return false;
	}
	
	protected Booking[] getBookingInFutureByCustomerId(int userId) {
		try {
			BookingServiceStub bookingWs = getBookingWebService();
			BookingServiceStub.GetBookingListByUserInFutureRequest request = new BookingServiceStub.GetBookingListByUserInFutureRequest();
			request.setUserid(userId);
			
			BookingServiceStub.GetBookingListByUserInFutureResponse response = bookingWs.getBookingListByUserInFuture(request);
			Bookinglist bookingList = response.getGetBookingListByUserInFutureResponse();
			
			return bookingList.getBooking();
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
		vb.setBookingId(booking.getBookingid());
		vb.setGroupId(booking.getGroupid());
		vb.setRoomSize(booking.getRoomsize());
		vb.setStart(parse(booking.getStart()));
		vb.setStartBuffer(booking.getEndbuffer());
		vb.setEnd(parse(booking.getEnd()));
		vb.setEndBuffer(booking.getStartbuffer());
		return vb;
	}
	
	protected void createTeaRoom(final BusinessGroup group, final OLATResourceable courseResource) {
		OLATResourceable ores = group == null ? courseResource : group;
		
		
		
		Property prop = coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Property>() {
			public Property execute() {
				Property prop = null;
				return prop;
			}
		});
	}
	
	protected Property getProperty(final BusinessGroup group, final OLATResourceable courseResource, String subName) {
		if(group != null && !StringHelper.containsNonWhitespace(subName)) {
			subName = COLLABORATION_TOOL_PROP_NAME;
		}
		return propertyManager.findProperty(null, group, courseResource, VMS_CATEGORY, subName);
	}
	
	protected Property getOrCreateProperty(final BusinessGroup group, final OLATResourceable courseResource, String subName) {
		Property property = getProperty(group, courseResource, subName);
		
		
		return property;
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
	    builder.queryParam("sessionCode", sessionCode);
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
