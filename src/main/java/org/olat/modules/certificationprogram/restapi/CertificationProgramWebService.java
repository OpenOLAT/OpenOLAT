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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 19 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Tag(name = "Certification programs")
public class CertificationProgramWebService {

	private final CertificationProgram program;

	@Autowired
	private CertificationProgramService certificationProgramService;

	public CertificationProgramWebService(CertificationProgram program) {
		this.program = program;
	}

	/**
	 * Return the details of this certification program.
	 *
	 * @param request The HTTP request
	 * @return The program VO
	 */
	@GET
	@Path("")
	@Operation(summary = "Return certification program details", description = "Return the details of a single certification program.")
	@ApiResponse(responseCode = "200", description = "The certification program", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CertificationProgramVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CertificationProgramVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getProgram(@Context HttpServletRequest request) {
		Identity identity = getIdentity(request);
		if(!certificationProgramService.canViewCertificationProgram(program, identity)) {
			return Response.status(Status.FORBIDDEN).build();
		}
		return Response.ok(CertificationProgramVO.valueOf(program)).build();
	}

	/**
	 * Sub-resource locator for the certified members of this program.
	 *
	 * @return The certificates sub-resource
	 */
	@Path("certificates")
	public CertificationProgramCertificatesWebService getCertificatesWebService(@Context HttpServletRequest request) {
		Identity identity = getIdentity(request);
		if(!certificationProgramService.canViewCertificationProgram(program, identity)) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		CertificationProgramCertificatesWebService service = new CertificationProgramCertificatesWebService(program);
		CoreSpringFactory.autowireObject(service);
		return service;
	}
}
