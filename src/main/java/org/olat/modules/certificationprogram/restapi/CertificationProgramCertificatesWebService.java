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
package org.olat.modules.certificationprogram.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.io.File;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberWithInfos;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 20 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramCertificatesWebService {

	private static final Logger log = Tracing.createLoggerFor(CertificationProgramCertificatesWebService.class);

	private final CertificationProgram program;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CertificationProgramService certificationProgramService;

	public CertificationProgramCertificatesWebService(CertificationProgram program) {
		this.program = program;
	}

	/**
	 * List certified members of the program with optional filtering.
	 *
	 * @param status Filter by member status: active, expired, recertification
	 * @param expirationBefore Filter members whose certificate expires before this date (ISO-8601)
	 * @param expirationAfter  Filter members whose certificate expires after this date (ISO-8601)
	 * @param identityKey Filter by a specific identity key
	 * @param request The HTTP request
	 * @return List of certified members
	 */
	@GET
	@Path("")
	@Operation(summary = "List certified members of a program", description = "List certified members of a certification program with optional filters.")
	@ApiResponse(responseCode = "200", description = "The list of certified members", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CertificationProgramMemberVOes.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CertificationProgramMemberVOes.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The certification program cannot be found")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMembers(
			@QueryParam("status") String status,
			@QueryParam("expirationBefore") String expirationBefore,
			@QueryParam("expirationAfter") String expirationAfter,
			@QueryParam("identityKey") Long identityKey,
			@Context HttpServletRequest request) {
		if(!canView(request)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Date expirationBeforeDate = ObjectFactory.parseDate(expirationBefore);
		Date expirationAfterDate = ObjectFactory.parseDate(expirationAfter);
		Date referenceDate = new Date();
		
		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(program);
		if("recertification".equals(status)) {
			searchParams.setType(CertificationProgramMemberSearchParameters.Type.CERTIFYING);	
		} else if("expired".equals(status)) {
			searchParams.setType(CertificationProgramMemberSearchParameters.Type.REMOVED);
		} else {
			searchParams.setType(CertificationProgramMemberSearchParameters.Type.CERTIFIED);
		}
		searchParams.setIdentityKey(identityKey);
		searchParams.setExpirationAfter(expirationAfterDate);
		searchParams.setExpirationBefore(expirationBeforeDate);

		List<CertificationProgramMemberWithInfos> members = certificationProgramService.getMembers(searchParams, referenceDate, -1);
		List<CertificationProgramMemberVO> voList = members.stream()
				.map(mem -> CertificationProgramMemberVO.valueOf(mem, referenceDate))
				.toList();

		CertificationProgramMemberVOes voEs = new CertificationProgramMemberVOes();
		voEs.setMembers(voList);
		return Response.ok(voEs).build();
	}

	/**
	 * Import an existing certificate for a user into this program.
	 *
	 * @param request The HTTP request (multipart/form-data)
	 * @return 200 if imported
	 */
	@POST
	@Path("")
	@Operation(summary = "Import a certificate for a user", description = "Import an existing certificate PDF for a user into this certification program.",
		requestBody = @RequestBody(description = "Certificate import data", content = {
			@Content(mediaType = MediaType.MULTIPART_FORM_DATA,
				schema = @Schema(properties = {
					@StringToClassMapItem(key = "file", value = File.class),
					@StringToClassMapItem(key = "identityKey", value = String.class),
					@StringToClassMapItem(key = "issuedDate", value = String.class),
					@StringToClassMapItem(key = "nextRecertificationDate", value = String.class),
					@StringToClassMapItem(key = "externalId", value = String.class),
				}))
		}))
	@ApiResponse(responseCode = "200", description = "The certificate was imported", content = {
		@Content(mediaType = "application/json", schema = @Schema(implementation = CertificationProgramMemberVO.class)),
		@Content(mediaType = "application/xml", schema = @Schema(implementation = CertificationProgramMemberVO.class)) })
	@ApiResponse(responseCode = "400", description = "Missing or invalid identityKey")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or certification program cannot be found")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response importCertificate(@Context HttpServletRequest request) {
		if(!canManage(request)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		MultipartReader partsReader = null;
		try {
			partsReader = new MultipartReader(request);

			String identityKeyStr = partsReader.getValue("identityKey");
			if(!StringHelper.isLong(identityKeyStr)) {
				return Response.status(Status.BAD_REQUEST).build();
			}
			Identity assessedIdentity = securityManager.loadIdentityByKey(Long.valueOf(identityKeyStr));
			if(assessedIdentity == null) {
				return Response.status(Status.NOT_FOUND).build();
			}

			String issuedDateStr = partsReader.getValue("issuedDate");
			Date issuedDate = StringHelper.containsNonWhitespace(issuedDateStr)
					? ObjectFactory.parseDate(issuedDateStr)
					: new Date();

			String nextRecertDateStr = partsReader.getValue("nextRecertificationDate");
			Date nextRecertDate = StringHelper.containsNonWhitespace(nextRecertDateStr)
					? ObjectFactory.parseDate(nextRecertDateStr)
					: null;

			String externalId = partsReader.getValue("externalId");
			File tmpFile = partsReader.getFile();
			Identity actor = getIdentity(request);

			Certificate certificate = certificatesManager.uploadCertificate(assessedIdentity, issuedDate, externalId, null,
					program, program.getResource(), nextRecertDate, tmpFile, actor);
			
			CreditPointWallet wallet = program.getCreditPointSystem() != null
					? creditPointService.getWallet(assessedIdentity, program.getCreditPointSystem())
					: null;
			
			Date referenceDate = new Date();
			CertificationProgramMemberVO vo = CertificationProgramMemberVO.valueOf(certificate, wallet, referenceDate);
			return Response.ok(vo).build();
		} catch (Exception e) {
			log.error("", e);
			return Response.serverError().build();
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}

	/**
	 * Get the current certificate status of a specific user in this program.
	 *
	 * @param identityKey The identity key of the user
	 * @param request     The HTTP request
	 * @return The member VO or 404
	 */
	@GET
	@Path("{identityKey}")
	@Operation(summary = "Get certificate status of a user in the program", description = "Get the current certificate status for a specific user in this certification program.")
	@ApiResponse(responseCode = "200", description = "The certificate status of the user", content = {
		@Content(mediaType = "application/json", schema = @Schema(implementation = CertificationProgramMemberVO.class)),
		@Content(mediaType = "application/xml", schema = @Schema(implementation = CertificationProgramMemberVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or certification program cannot be found")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMemberByIdentity(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest request) {
		if(!canView(request)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		if(assessedIdentity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		Certificate certificate = certificatesDao.getLastCertificate(assessedIdentity, program);
		if(certificate == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		CreditPointWallet wallet = program.getCreditPointSystem() != null
				? creditPointService.getWallet(assessedIdentity, program.getCreditPointSystem())
				: null;
		
		Date referenceDate = new Date();
		CertificationProgramMemberVO vo = CertificationProgramMemberVO.valueOf(certificate, wallet, referenceDate);
		return Response.ok(vo).build();
	}

	/**
	 * Revoke / remove an imported certificate from the program.
	 *
	 * @param certificateKey The key of the certificate to remove
	 * @param request        The HTTP request
	 * @return 200 if deleted, 404 if not found, 409 if it does not belong to this program
	 */
	@DELETE
	@Path("{certificateKey}")
	@Operation(summary = "Revoke or remove a certificate from the program", description = "Revoke or remove an imported certificate from this certification program.")
	@ApiResponse(responseCode = "200", description = "The certificate was removed")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The certificate cannot be found")
	@ApiResponse(responseCode = "409", description = "The certificate does not belong to this program")
	public Response deleteCertificate(@PathParam("certificateKey") Long certificateKey,
			@Context HttpServletRequest request) {
		if(!canManage(request)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Certificate certificate = certificatesManager.getCertificateById(certificateKey);
		if(certificate == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if(certificate.getCertificationProgram() == null
				|| !program.getKey().equals(certificate.getCertificationProgram().getKey())) {
			return Response.status(Status.CONFLICT).build();
		}

		certificatesManager.deleteCertificate(certificate);
		return Response.ok().build();
	}

	private boolean canView(HttpServletRequest httpRequest) {
		try {
			Identity caller = getIdentity(httpRequest);
			return certificationProgramService.canViewCertificationProgram(program, caller);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private boolean canManage(HttpServletRequest httpRequest) {
		try {
			Roles roles = getRoles(httpRequest);
			List<Organisation> organisations = certificationProgramService.getOrganisations(program);
			if(roles.hasRole(organisations, OrganisationRoles.administrator)
					|| roles.hasRole(organisations, OrganisationRoles.curriculummanager)) {
				return true;
			}
			Identity identity = getIdentity(httpRequest);
			List<Identity> owners = certificationProgramService.getCertificationProgramOwners(program);
			return owners.stream().anyMatch(o -> o.getKey().equals(identity.getKey()));
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
}
