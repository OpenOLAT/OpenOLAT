/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 21 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Tag(name = "Credit points")
@Component
@Path("creditpoints/system")
public class CreditPointSystemWebService {

	@Autowired
	private CreditPointService creditPointService;
	
	/**
	 * Return the credit point systems a manager user is allowed to see.
	 * 
	 * @param httpRequest  The HTTP request
	 * @return An array of credit point systems
	 */
	@GET
	@Operation(summary = "Return the credit points system a manager user is allowed to see",
		description = "Return the credit points system a manager user is allowed to see")
	@ApiResponse(responseCode = "200", description = "An array of credit points system",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CreditPointSystemVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CreditPointSystemVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCreditPointSystems(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager() && !roles.isLearnResourceManager() && !roles.isAuthor()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<CreditPointSystem> systems = creditPointService.getCreditPointSystems(roles);
		CreditPointSystemVO[] voes = systems.stream()
				.filter(sys -> sys.getStatus() != CreditPointSystemStatus.deleted)
				.map(CreditPointSystemVO::valueOf)
				.toArray(CreditPointSystemVO[]::new);
		return Response.ok(voes).build();
	}

	@Path("{systemKey}/wallets")
	public CreditPointWalletWebService getWalletsWebservice(@PathParam("systemKey") Long systemKey,
			@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager() && !roles.isLearnResourceManager() && !roles.isAuthor()) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		CreditPointSystem system = creditPointService.getCreditPointSystem(roles, systemKey);
		if(system == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		CreditPointWalletWebService walletsWebService = new CreditPointWalletWebService(system);
		CoreSpringFactory.autowireObject(walletsWebService);
		return walletsWebService;
	}
	
	@Path("{systemKey}/transactions")
	public CreditPointTransactionWebService getTransactionsWebService(@PathParam("systemKey") Long systemKey,
			@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager() && !roles.isLearnResourceManager() && !roles.isAuthor()) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		CreditPointSystem system = creditPointService.getCreditPointSystem(roles, systemKey);
		if(system == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		CreditPointTransactionWebService walletsWebService = new CreditPointTransactionWebService(system);
		CoreSpringFactory.autowireObject(walletsWebService);
		return walletsWebService;
	}
}
