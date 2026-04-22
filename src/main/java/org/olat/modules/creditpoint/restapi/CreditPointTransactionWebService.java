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

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionAndWallet;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 21 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointTransactionWebService {
	
	private final CreditPointSystem creditPointSystem;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointTransactionWebService(CreditPointSystem creditPointSystem) {
		this.creditPointSystem = creditPointSystem;
	}
	
	/**
	 * Add a deposit to the wallet specified by the credit point system and user.
	 * 
	 * @param identityKey The user
	 * @param transaction The details of the deposit
	 * @param httpRequest The request
	 * @return The details of the transaction
	 */
	@PUT
	@Path("{identityKey}")
	@Operation(summary = "Add a deposit to the wallet specified by the credit point system and user",
		description = "Add a deposit to the wallet specified by the credit point system and user")
	@ApiResponse(responseCode = "200", description = "The transaction details",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CreditPointTransactionVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CreditPointTransactionVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "User not found")
	@ApiResponse(responseCode = "406", description = "The credit point system is inactive or the transaction type is not supported.")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putTransaction(@PathParam("identityKey") Long identityKey,
			CreditPointTransactionVO transaction, @Context HttpServletRequest httpRequest) {
		return addTransaction(identityKey, transaction, httpRequest);
	}
	
	/**
	 * Add a deposit to the wallet specified by the credit point system and user.
	 * 
	 * @param identityKey The user
	 * @param transaction The details of the deposit
	 * @param httpRequest The request
	 * @return The details of the transaction
	 */
	@POST
	@Path("{identityKey}")
	@Operation(summary = "Add a deposit to the wallet specified by the credit point system and user",
		description = "Add a deposit to the wallet specified by the credit point system and user")
	@ApiResponse(responseCode = "200", description = "The transaction details",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CreditPointTransactionVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CreditPointTransactionVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "User not found")
	@ApiResponse(responseCode = "406", description = "The credit point system is inactive or the transaction type is not supported.")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postTransaction(@PathParam("identityKey") Long identityKey,
			CreditPointTransactionVO transaction, @Context HttpServletRequest httpRequest) {
		return addTransaction(identityKey, transaction, httpRequest);
	}
	
	private Response addTransaction(Long identityKey, CreditPointTransactionVO transaction, HttpServletRequest httpRequest) {
		Identity actor = getIdentity(httpRequest);
		
		if(creditPointSystem.getStatus() != CreditPointSystemStatus.active) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		CreditPointTransactionType type = CreditPointTransactionType.secureValueOf(transaction.getType());
		BigDecimal amount = transaction.getAmount();
		String note = StringHelper.xssScan(transaction.getNote());
		if(type == null || amount == null || BigDecimal.ZERO.compareTo(amount) >= 0
				|| (note != null && note.length() > 4000)) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(identity, creditPointSystem);
		if(type == CreditPointTransactionType.deposit) {
			CreditPointTransactionAndWallet result = creditPointService.createCreditPointTransaction(type, amount, null,
					note, wallet, actor, null, null, null, null, null);
			return Response.ok(CreditPointTransactionVO.valueOf(result.transaction())).build();
		}
		return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
	}
}
