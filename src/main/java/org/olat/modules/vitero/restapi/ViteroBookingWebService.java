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
package org.olat.modules.vitero.restapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.modules.vitero.ViteroModule;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.ErrorCode;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroGroupRoles;
import org.olat.modules.vitero.model.ViteroStatus;
import org.olat.modules.vitero.ui.ViteroBookingsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ViteroBookingWebService {
	
	private static final Logger log = Tracing.createLoggerFor(ViteroBookingWebService.class);
	
	private final String subIdentifier;
	private final OLATResourceable ores;
	
	@Autowired
	private ViteroModule viteroModule;
	@Autowired
	private ViteroManager viteroManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private IdentityPowerSearchQueries identitySearchQueries;
	
	public ViteroBookingWebService(OLATResourceable ores, String subIdentifier) {
		this.ores = ores;
		this.subIdentifier = subIdentifier;
	}

	/**
	 * returns the list of booking of the resource.
	 * 
	 * @response.representation.200.qname {http://www.example.com}viteroBookingVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc This is the list of all bookings of a resource
	 * @response.representation.200.example {@link org.olat.modules.vitero.restapi.Examples#SAMPLE_ViteroBookingVO}
	 * @return The list of vitero booking
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRooms() {
		try {
			List<ViteroBooking> bookings = viteroManager.getBookings(null, ores, subIdentifier);
			ViteroBookingVO[] bookingVos = new ViteroBookingVO[bookings.size()];
			int count = 0;
			for(ViteroBooking booking:bookings) {
				bookingVos[count++] = new ViteroBookingVO(booking);
			}
			return Response.ok(bookingVos).build();
		} catch (VmsNotAvailableException e) {
			log.error("", e);
			return handleNotAvailableException();
		}	
	}
	
	/**
	 * Return the created or updated booking
	 * 
	 * @response.representation.200.qname {http://www.example.com}viteroBookingVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The created booking
	 * @response.representation.200.example {@link org.olat.modules.vitero.restapi.Examples#SAMPLE_ViteroBookingVO}
	 * @return The list of vitero booking
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createRoom(ViteroBookingVO booking) {
		return saveRoom(booking);
	}
	
	/**
	 * Return the created or updated booking
	 * 
	 * @response.representation.200.qname {http://www.example.com}viteroBookingVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The created booking
	 * @response.representation.200.example {@link org.olat.modules.vitero.restapi.Examples#SAMPLE_ViteroBookingVO}
	 * @return The list of vitero booking
	 */
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateRoom(ViteroBookingVO booking) {
		return saveRoom(booking);
	}
	
	private Response saveRoom(ViteroBookingVO booking) {
		try {
			ViteroBooking vBooking = new ViteroBooking();
			vBooking.setBookingId(booking.getBookingId());
			vBooking.setExternalId(booking.getExternalId());
			vBooking.setGroupId(booking.getGroupId());
			vBooking.setGroupName(booking.getGroupName());
			vBooking.setEventName(booking.getEventName());
			vBooking.setStart(booking.getStart());
			vBooking.setStartBuffer(booking.getStartBuffer());
			vBooking.setEnd(booking.getEnd());
			vBooking.setEndBuffer(booking.getEndBuffer());
			vBooking.setRoomSize(booking.getRoomSize());
			vBooking.setAutoSignIn(booking.isAutoSignIn());
			vBooking.setInspire(booking.isInspire());
			vBooking.setTimeZoneId(viteroModule.getTimeZoneId());
			
			ViteroStatus status;
			if(booking.getBookingId() > 0) {
				status = viteroManager.updateVmsBooking(vBooking);
			} else {
				status = viteroManager.createBooking(null, ores, subIdentifier, vBooking);
			}
			
			Response response;
			if(status.isOk()) {
				response = Response.ok(new ViteroBookingVO(vBooking)).build();
			} else {
				response = handleViteroError(status);
			}
			return response;
		} catch (VmsNotAvailableException e) {
			log.error("", e);
			return handleNotAvailableException();
		}	
	}
	
	/**
	 * Delete the booking
	 * 
	 * @response.representation.200.doc The booking is deleted
	 * @return Nothing
	 */
	@DELETE
	@Path("{bookingId}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteRoom(@PathParam("bookingId") int bookingId) {
		try {
			ViteroBooking vBooking = viteroManager.getBookingById(null, ores, subIdentifier, bookingId);
			if(vBooking == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			} else if(viteroManager.deleteBooking(vBooking)) {
				return Response.ok().build();
			} else {
				return Response.serverError().status(500).build();
			}
		} catch (VmsNotAvailableException e) {
			log.error("", e);
			return handleNotAvailableException();
		}	
	}
	
	/**
	 * Returns the list of members of the booking.
	 * 
	 * @response.representation.200.qname {http://www.example.com}viteroGroupMemberVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc This is the list of all bookings of a resource
	 * @response.representation.200.example {@link org.olat.modules.vitero.restapi.Examples#SAMPLE_ViteroGroupMemberVO}
	 * @param bookingId The id of the booking
	 * @return The list of members in the specified booking
	 */
	@GET
	@Path("{bookingId}/members")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMembers(@PathParam("bookingId") int bookingId) {
		try {
			ViteroBooking booking = viteroManager.getBookingById(null, ores, subIdentifier, bookingId);
			if(booking == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			ViteroGroupRoles roles = viteroManager.getGroupRoles(booking.getGroupId());
			if(roles == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			List<String> currentEmails = new ArrayList<>(roles.getEmailsOfParticipants());
			List<ViteroGroupMemberVO> memberList = new ArrayList<>(currentEmails.size());
			for(String email:currentEmails) {
				SearchIdentityParams params = new SearchIdentityParams();
				params.setUserProperties(Collections.singletonMap(UserConstants.EMAIL, email));
				List<Identity> identities = identitySearchQueries.getIdentitiesByPowerSearch(params, 0, 1);
				for(Identity identity:identities) {
					GroupRole role = roles.getEmailsToRole().get(email);
					memberList.add(new ViteroGroupMemberVO(identity.getKey(), role.name()));
				}
			}
			
			ViteroGroupMemberVO[] members = memberList.toArray(new ViteroGroupMemberVO[memberList.size()]);
			return Response.ok(members).build();
		} catch (VmsNotAvailableException e) {
			log.error("", e);
			return handleNotAvailableException();
		}
	}
	
	/**
	 * Update the list of members of the booking, it add and mutates the
	 * members and delete the missing members.
	 * 
	 * @response.representation.200.qname {http://www.example.com}viteroGroupMemberVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc This is the list of all bookings of a resource
	 * @response.representation.200.example {@link org.olat.modules.vitero.restapi.Examples#SAMPLE_ViteroGroupMemberVO}
	 * @param bookingId The id of the booking
	 * @param members The array of members
	 * @return Nothing
	 */
	@POST
	@Path("{bookingId}/members")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addMembers(@PathParam("bookingId") int bookingId, ViteroGroupMemberVO[] members) {
		try {
			ViteroBooking booking = viteroManager.getBookingById(null, ores, subIdentifier, bookingId);
			if(booking == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			ViteroGroupRoles roles = viteroManager.getGroupRoles(booking.getGroupId());
			if(roles == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			List<ViteroErrorVO> errors = new ArrayList<>();

			List<String> currentEmails = new ArrayList<>(roles.getEmailsOfParticipants());
			for(ViteroGroupMemberVO member:members) {
				GroupRole role = GroupRole.valueOf(member.getGroupRole());
				Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
				String currentEmail = identity.getUser().getProperty(UserConstants.EMAIL, null);
				
				GroupRole currentRole = roles.getEmailsToRole().get(currentEmail);
				if(currentRole == null) {
					ViteroStatus status = viteroManager.addToRoom(booking, identity, role);
					if(!status.isOk()) {
						errors.add(viteroErrorVO(status));
					}
				} else if(!currentRole.equals(role)) {
					Integer vmsUserId = roles.getEmailsToVmsUserId().get(currentEmail);
					ViteroStatus status = viteroManager.changeGroupRole(booking.getGroupId(), vmsUserId.intValue(), role.getVmsValue());
					if(!status.isOk()) {
						errors.add(viteroErrorVO(status));
					}
				}
				currentEmails.remove(currentEmail);
			}
			
			for(String email:currentEmails) {
				SearchIdentityParams params = new SearchIdentityParams();
				params.setUserProperties(Collections.singletonMap(UserConstants.EMAIL, email));
				List<Identity> identities = identitySearchQueries.getIdentitiesByPowerSearch(params, 0, 1);
				for(Identity identity:identities) {
					ViteroStatus status = viteroManager.removeFromRoom(booking, identity);
					if(!status.isOk()) {
						errors.add(viteroErrorVO(status));
					}
				}
			}
			return Response.ok().build();
		} catch (VmsNotAvailableException e) {
			log.error("", e);
			return handleNotAvailableException();
		}
	}
	
	private Response handleViteroError(ViteroStatus status) {
		return Response.serverError().entity(viteroErrorVO(status)).status(500).build();
	}
	
	private ViteroErrorVO viteroErrorVO(ViteroStatus status) {
		String msg = "";
		if(status.getError() != null) {
			msg = Util.createPackageTranslator(ViteroBookingsController.class, Locale.ENGLISH)
				.translate(status.getError().i18nKey());
		}
		return new ViteroErrorVO(status, msg);
	}
	
	private Response handleNotAvailableException() {
		ViteroStatus status = new ViteroStatus(ErrorCode.unkown);
		ViteroErrorVO error = new ViteroErrorVO(status, "vitero server is probable not avalailable at this time");
		return Response.serverError().entity(error).status(Status.SERVICE_UNAVAILABLE).build();
	}
}