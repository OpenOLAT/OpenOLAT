package com.frentix.olat.vc.provider.vitero;

import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.StringHelper;

import com.frentix.olat.vc.provider.vitero.stubs.BookingServiceStub;
import com.frentix.olat.vc.provider.vitero.stubs.BookingServiceStub.Bookingtype;

import de.bps.course.nodes.vc.VCConfiguration;
import de.bps.course.nodes.vc.provider.VCProvider;

/**
 * 
 * Description:<br>
 * Implementation of the Virtual Classroom for the Vitero Booking System
 * 
 * <P>
 * Initial Date:  26 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingProvider extends LogDelegator implements VCProvider {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
	
	private boolean enabled;
	private String providerId;
	private String displayName;
	private String protocol;
	private int port;
	private String baseUrl;
	private String contextPath;
	private String adminLogin;
	private String adminPassword;
	private String customerId;

	@Override
	public VCProvider newInstance() {
		ViteroBookingProvider provider = new ViteroBookingProvider();
		provider.enabled = enabled;
		provider.providerId = providerId;
		provider.displayName = displayName;
		provider.protocol = protocol;
		provider.port = port;
		provider.baseUrl = baseUrl;
		provider.contextPath = contextPath;
		provider.adminLogin = adminLogin;
		provider.adminPassword = adminPassword;
		provider.customerId = customerId;
		return provider;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getProviderId() {
		return providerId;
	}
	
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAdminLogin() {
		return adminLogin;
	}

	public void setAdminLogin(String adminLogin) {
		this.adminLogin = adminLogin;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	@Override
	public Map<String, String> getTemplates() {
		return new HashMap<String,String>();
	}

	@Override
	public boolean isProviderAvailable() {
		return true;
	}

	@Override
	public boolean createClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
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

	@Override
	public boolean updateClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
		return false;
	}

	@Override
	public boolean removeClassroom(String roomId, VCConfiguration config) {
		return false;
	}

	@Override
	public URL createClassroomUrl(String roomId, Identity identity, VCConfiguration config) {
		return null;
	}

	@Override
	public URL createClassroomGuestUrl(String roomId, Identity identity, VCConfiguration config) {
		return null;
	}

	@Override
	public boolean existsClassroom(String roomId, VCConfiguration config) {
		return false;
	}

	@Override
	public boolean login(Identity identity, String password) {
		return false;
	}

	@Override
	public boolean createModerator(Identity identity, String roomId) {
		return false;
	}

	@Override
	public boolean createUser(Identity identity, String roomId) {
		return false;
	}

	@Override
	public boolean createGuest(Identity identity, String roomId) {
		return false;
	}

	@Override
	public Controller createDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description,
			boolean isModerator, VCConfiguration config) {
		return new ViteroDisplayController(ureq, wControl, roomId, name, description, isModerator, (ViteroBookingConfiguration)config, this);
	}

	@Override
	public Controller createConfigController(UserRequest ureq, WindowControl wControl, String roomId, VCConfiguration config) {
		return new ViteroConfigController(ureq, wControl, roomId, this, (ViteroBookingConfiguration)config);
	}

	@Override
	public VCConfiguration createNewConfiguration() {
		return new ViteroBookingConfiguration();
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
	
	protected BookingServiceStub getBookingWebService() {
		try {
			BookingServiceStub bookingWs = new BookingServiceStub(getVmsEndPoint());
			ServiceClient client = bookingWs._getServiceClient();
			OMElement securityEl = SecurityHeader.generateSecurityHeader("admin", "007");
			client.addHeader(securityEl);
			return bookingWs;
		} catch (AxisFault e) {
			logError("Cannot create booking ws.", e);
			return null;
		}
	}

	protected String getVmsEndPoint() {
	    UriBuilder builder = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port);
	    if(StringHelper.containsNonWhitespace(contextPath)) {
	    	builder = builder.path(contextPath);
	    }
	    builder.path("services");
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
