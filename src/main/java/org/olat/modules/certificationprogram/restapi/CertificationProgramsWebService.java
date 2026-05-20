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
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
@Path("certificationprograms")
public class CertificationProgramsWebService {

	@Autowired
	private CertificationProgramService certificationProgramService;

	/**
	 * List all certification programs the caller is allowed to see.
	 *
	 * @param request The HTTP request
	 * @return List of certification programs
	 */
	@GET
	@Path("")
	@Operation(summary = "List certification programs", description = "List all certification programs that the authenticated user may see.")
	@ApiResponse(responseCode = "200", description = "The list of certification programs", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CertificationProgramVOes.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CertificationProgramVOes.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getPrograms(@Context HttpServletRequest request) {
		Identity identity = getIdentity(request);

		List<CertificationProgram> programs = certificationProgramService.getCertificationPrograms(identity);
		List<CertificationProgramVO> voList = programs.stream()
				.map(CertificationProgramVO::valueOf)
				.toList();

		CertificationProgramVOes voEs = new CertificationProgramVOes();
		voEs.setPrograms(voList);
		return Response.ok(voEs).build();
	}

	/**
	 * Sub-resource locator for a single certification program.
	 *
	 * @param programKey The primary key of the certification program
	 * @param request    The HTTP request
	 * @return The program sub-resource
	 */
	@Path("{programKey}")
	public CertificationProgramWebService getProgramWebService(@PathParam("programKey") Long programKey,
			@Context HttpServletRequest request) {
		CertificationProgram program = certificationProgramService
				.getCertificationProgram((CertificationProgramRef)() -> programKey);
		if(program == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		Identity identity = getIdentity(request);
		boolean canView =  certificationProgramService.canViewCertificationProgram(program, identity);
		if(!canView) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		CertificationProgramWebService service = new CertificationProgramWebService(program);
		CoreSpringFactory.autowireObject(service);
		return service;
	}
}
