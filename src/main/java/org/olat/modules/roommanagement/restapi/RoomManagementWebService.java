/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.roommanagement.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Initial date: 18 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Tag(name = "Room Management")
@Component
@Path("rm")
public class RoomManagementWebService {

	private static final String VERSION = "1.0";

	@Autowired
	private RoomManagementModule roomManagementModule;
	@Autowired
	private RoomManagementService roomManagementService;

	@GET
	@Path("version")
	@Operation(summary = "The version of the Room Management Web Service", description = "The version of the Room Management Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}

	@GET
	@Operation(summary = "Search buildings", description = "Search room management buildings")
	@ApiResponse(responseCode = "200", description = "An array of buildings",
		content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BuildingVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = BuildingVO.class)))
		})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("buildings")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getBuildings(
			@QueryParam("search") String search,
			@QueryParam("externalId") String externalId,
			@QueryParam("externalRef") String externalRef,
			@QueryParam("status") @DefaultValue("active") String statusParam,
			@QueryParam("organisationKey") Long organisationKey,
			@QueryParam("pageNumber") @DefaultValue("0") int pageNumber,
			@QueryParam("pageSize") @DefaultValue("25") int pageSize,
			@Context HttpServletRequest httpRequest) {

		if (!roomManagementModule.isEnabled()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Roles roles = getRoles(httpRequest);
		if (!isAuthorised(roles)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = getIdentity(httpRequest);

		SearchBuildingParameters params = new SearchBuildingParameters();
		if (StringHelper.containsNonWhitespace(search)) {
			params.setSearchString(search);
		}
		if (StringHelper.containsNonWhitespace(externalId)) {
			params.setExactExternalId(externalId);
		}
		if (StringHelper.containsNonWhitespace(externalRef)) {
			params.setExactExternalRef(externalRef);
		}
		params.setStatus(parseStatus(statusParam));
		if (organisationKey != null) {
			params.setOrganisations(List.of(() -> organisationKey));
		}
		params.setIdentity(identity);
		int effectivePageSize = Math.min(pageSize, 200);
		params.setFirstResult(firstResult(pageNumber, effectivePageSize));
		params.setMaxResults(effectivePageSize);

		List<Building> buildings = roomManagementService.searchBuildings(params, roles);

		SearchBuildingParameters countParams = new SearchBuildingParameters();
		countParams.setSearchString(params.getSearchString());
		countParams.setExactExternalId(params.getExactExternalId());
		countParams.setExactExternalRef(params.getExactExternalRef());
		countParams.setStatus(params.getStatus());
		countParams.setOrganisations(params.getOrganisations());
		countParams.setIdentity(params.getIdentity());

		BuildingVO[] vos = buildings.stream()
				.map(b -> BuildingVO.valueOf(b, roomManagementService.getOrganisations(b), roles))
				.toArray(BuildingVO[]::new);

		return Response.ok(vos)
				.header("X-Total-Count", roomManagementService.countBuildings(countParams))
				.build();
	}

	@GET
	@Operation(summary = "Get a building", description = "Get a single room management building by key")
	@ApiResponse(responseCode = "200", description = "The building",
		content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = BuildingVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = BuildingVO.class))
		})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The building was not found or is not visible to the caller")
	@Path("buildings/{buildingKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getBuilding(@PathParam("buildingKey") Long buildingKey, @Context HttpServletRequest httpRequest) {
		if (!roomManagementModule.isEnabled()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Roles roles = getRoles(httpRequest);
		if (!isAuthorised(roles)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = getIdentity(httpRequest);

		Building building = roomManagementService.getBuilding(() -> buildingKey);
		if (building == null || building.getStatus() == RoomStatus.deleted) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (!roomManagementService.isVisibleBuilding(building, roles, identity)) {
			return Response.status(Status.NOT_FOUND).build();
		}

		List<Organisation> organisations = roomManagementService.getOrganisations(building);
		BuildingVO vo = BuildingVO.valueOf(building, organisations, roles);
		return Response.ok(vo).build();
	}

	@GET
	@Operation(summary = "Search rooms", description = "Search room management rooms")
	@ApiResponse(responseCode = "200", description = "An array of rooms",
		content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoomVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = RoomVO.class)))
		})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("rooms")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRooms(
			@QueryParam("search") String search,
			@QueryParam("externalId") String externalId,
			@QueryParam("externalRef") String externalRef,
			@QueryParam("status") @DefaultValue("active") String statusParam,
			@QueryParam("buildingKey") Long buildingKey,
			@QueryParam("organisationKey") Long organisationKey,
			@QueryParam("seatsMin") Integer seatsMin,
			@QueryParam("seatsMax") Integer seatsMax,
			@QueryParam("availableFrom") String availableFromStr,
			@QueryParam("availableTo") String availableToStr,
			@QueryParam("pageNumber") @DefaultValue("0") int pageNumber,
			@QueryParam("pageSize") @DefaultValue("25") int pageSize,
			@Context HttpServletRequest httpRequest) {

		if (!roomManagementModule.isEnabled()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Roles roles = getRoles(httpRequest);
		if (!isAuthorised(roles)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = getIdentity(httpRequest);

		SearchRoomParameters params = new SearchRoomParameters();
		if (StringHelper.containsNonWhitespace(search)) {
			params.setSearchString(search);
		}
		if (StringHelper.containsNonWhitespace(externalId)) {
			params.setExactExternalId(externalId);
		}
		if (StringHelper.containsNonWhitespace(externalRef)) {
			params.setExactExternalRef(externalRef);
		}
		params.setStatus(parseStatus(statusParam));
		if (buildingKey != null) {
			params.setBuilding(() -> buildingKey);
		}
		if (organisationKey != null) {
			params.setOrganisationKey(organisationKey);
		}
		if (seatsMin != null) {
			params.setMinSeats(seatsMin);
		}
		if (seatsMax != null) {
			params.setMaxSeats(seatsMax);
		}
		params.setAvailableFrom(parseDate(availableFromStr));
		params.setAvailableTo(parseDate(availableToStr));
		params.setIdentity(identity);
		int effectivePageSize = Math.min(pageSize, 200);
		params.setFirstResult(firstResult(pageNumber, effectivePageSize));
		params.setMaxResults(effectivePageSize);

		List<Room> rooms = roomManagementService.searchRooms(params, roles);

		SearchRoomParameters countParams = new SearchRoomParameters();
		countParams.setSearchString(params.getSearchString());
		countParams.setExactExternalId(params.getExactExternalId());
		countParams.setExactExternalRef(params.getExactExternalRef());
		countParams.setStatus(params.getStatus());
		countParams.setBuilding(params.getBuilding());
		countParams.setOrganisationKey(params.getOrganisationKey());
		countParams.setMinSeats(params.getMinSeats());
		countParams.setMaxSeats(params.getMaxSeats());
		countParams.setAvailableFrom(params.getAvailableFrom());
		countParams.setAvailableTo(params.getAvailableTo());
		countParams.setIdentity(params.getIdentity());

		RoomVO[] vos = rooms.stream()
				.map(r -> RoomVO.valueOf(r, roles))
				.toArray(RoomVO[]::new);

		return Response.ok(vos)
				.header("X-Total-Count", roomManagementService.countRooms(countParams))
				.build();
	}

	@GET
	@Operation(summary = "Get a room", description = "Get a single room management room by key")
	@ApiResponse(responseCode = "200", description = "The room",
		content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RoomVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RoomVO.class))
		})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The room was not found or is not visible to the caller")
	@Path("rooms/{roomKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRoom(@PathParam("roomKey") Long roomKey, @Context HttpServletRequest httpRequest) {
		if (!roomManagementModule.isEnabled()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Roles roles = getRoles(httpRequest);
		if (!isAuthorised(roles)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = getIdentity(httpRequest);

		Room room = roomManagementService.getRoom(() -> roomKey);
		if (room == null || room.getStatus() == RoomStatus.deleted) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (!roomManagementService.isVisibleBuilding(room.getBuilding(), roles, identity)) {
			return Response.status(Status.NOT_FOUND).build();
		}

		RoomVO vo = RoomVO.valueOf(room, roles);
		return Response.ok(vo).build();
	}

	private int firstResult(int pageNumber, int pageSize) {
		long first = (long) Math.max(0, pageNumber) * pageSize;
		return first > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) first;
	}

	private boolean isAuthorised(Roles roles) {
		return roles.isAdministrator() || roles.isSystemAdmin()
				|| roles.isLearnResourceManager() || roles.isLectureManager() || roles.isAuthor();
	}

	private List<RoomStatus> parseStatus(String statusParam) {
		if (!StringHelper.containsNonWhitespace(statusParam)) {
			return List.of(RoomStatus.active);
		}
		List<RoomStatus> statuses = new ArrayList<>();
		for (String s : statusParam.split(",")) {
			String trimmed = s.trim();
			try {
				RoomStatus status = RoomStatus.valueOf(trimmed);
				if (status != RoomStatus.deleted) {
					statuses.add(status);
				}
			} catch (IllegalArgumentException e) {
				// ignore unknown values
			}
		}
		return statuses.isEmpty() ? List.of(RoomStatus.active) : statuses;
	}

	private Date parseDate(String str) {
		if (!StringHelper.containsNonWhitespace(str)) return null;
		try {
			return Date.from(Instant.parse(str));
		} catch (Exception e) {
			return null;
		}
	}
}
