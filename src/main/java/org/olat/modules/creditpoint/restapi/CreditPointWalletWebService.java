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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.id.Roles;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 21 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointWalletWebService {
	
	private final CreditPointSystem creditPointSystem;
	
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointWalletWebService(CreditPointSystem creditPointSystem) {
		this.creditPointSystem = creditPointSystem;
	}
	
	/**
	 * Return the credit point wallets.
	 * 
	 * @param httpRequest  The HTTP request
	 * @return An array of wallets
	 */
	@GET
	@Operation(summary = "Return the credit points wallet of the all users of the specified credit point system",
		description = "Return the credit points wallet of the all users of the specified credit point system")
	@ApiResponse(responseCode = "200", description = "An array of wallets",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CreditPointWalletVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CreditPointWalletVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCreditPointWallets(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager() && !roles.isLearnResourceManager() && !roles.isAuthor()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<CreditPointWallet> wallets = creditPointService.getWallets(creditPointSystem);
		CreditPointWalletVO[] voes = wallets.stream()
				.map(CreditPointWalletVO::valueOf)
				.toArray(CreditPointWalletVO[]::new);
		return Response.ok(voes).build();
	}
	
	/**
	 * Return the credit point wallet of a specific user.
	 * 
	 * @param httpRequest  The HTTP request
	 * @return An array of wallets
	 */
	@GET
	@Path("{identityKey}")
	@Operation(summary = "Return the credit points wallet of the specified user of the specified credit point system",
		description = "Return the credit points wallet of the specified user of the specified credit point system")
	@ApiResponse(responseCode = "200", description = "A wallet",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CreditPointWalletVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CreditPointWalletVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCreditPointWallet(@PathParam("identityKey") Long identityKey,  @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager() && !roles.isLearnResourceManager() && !roles.isAuthor()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		IdentityRef identity = new IdentityRefImpl(identityKey);
		CreditPointWallet wallet = creditPointService.getWallet(identity, creditPointSystem);
		if(wallet != null) {
			return Response.ok(CreditPointWalletVO.valueOf(wallet)).build();
		}
		return Response.ok(CreditPointWalletVO.emptyVO(identity, creditPointSystem)).build();
	}
}
