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
package org.olat.course.certificate.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 17.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Users")
@Component
@Path("users/{identityKey}/certificates")
public class UserCertificationWebService {
	
	private static final Logger log = Tracing.createLoggerFor(UserCertificationWebService.class);
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CertificatesManager certificatesManager;

	/**
	 * Return a list of certificate's informations.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @param request The request
	 * @return The certificate
	 */
	@GET
	@Path("")
	@Operation(summary = "Return the certificates informations", description = "Return the certificates informations")
	@ApiResponse(responseCode = "200", description = "The certificate as file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CertificateVOes.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CertificateVOes.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCertificate(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(identity);
		List<CertificateVO> certificatesVoList = certificates.stream()
				.map(CertificateVO::new)
				.collect(Collectors.toList());
		CertificateVOes certificateVOes = new CertificateVOes();
		certificateVOes.setCertificates(certificatesVoList);
		return Response.ok(certificateVOes).build();
	}
	
	@HEAD
	@Path("{certificateKey}")
	@Operation(summary = "Return the certificate", description = "Return the certificate ")
	@ApiResponse(responseCode = "200", description = "The certificate")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Produces({"application/pdf"})
	public Response getCertificateInfo(@PathParam("identityKey") Long identityKey, @PathParam("certificateKey") Long certificateKey,
			@Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Certificate certificate = certificatesManager.getCertificateById(certificateKey);
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		} else if(!identityKey.equals(certificate.getIdentity().getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(certificate);
		if(certificateFile == null || !certificateFile.exists()) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok().build();
	}

	/**
	 * Return the certificate as PDF file.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @param request The request
	 * @return The certificate
	 */
	@GET
	@Path("{certificateKey}")
	@Operation(summary = "Return the certificate as PDF file", description = "Return the certificate as PDF file")
	@ApiResponse(responseCode = "200", description = "The certificate as file")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Produces({"application/pdf"})
	public Response getCertificate(@PathParam("identityKey") Long identityKey, @PathParam("certificateKey") Long certificateKey,
			@Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		} 
		
		Certificate certificate = certificatesManager.getCertificateById(certificateKey);
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		} else if(!identityKey.equals(certificate.getIdentity().getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(certificate);
		if(certificateFile == null || !certificateFile.exists()) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(certificateFile.getInputStream()).build();
	}
	
	/**
	 * Upload a new certificate.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @param request The request
	 * @return Nothing special
	 */
	@POST
	@Path("")
	@Operation(summary = "Upload a new standalone certificate", description = "Upload a new certificate standalone.")
	@ApiResponse(responseCode = "200", description = "if the certificate was uploaded")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postCertificate(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest request) {
		MultipartReader partsReader = null;
		try {
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			String courseTitle = partsReader.getValue("courseTitle");
			String creationDateStr = partsReader.getValue("creationDate");
			String archivedResource = partsReader.getValue("archivedResourceKey");
			Long archivedResourceKey = null;
			if(StringHelper.isLong(archivedResource)) {
				archivedResourceKey = Long.valueOf(archivedResource);
			} else {
				log.warn("Upload user certificate, missing archive resource key");
				return Response.serverError().status(Status.CONFLICT).build();
			}
			
			Date creationDate = null;
			if(StringHelper.containsNonWhitespace(creationDateStr)) {
				creationDate = ObjectFactory.parseDate(creationDateStr);
			}

			Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
			if(assessedIdentity == null) {
				return Response.serverError().status(Response.Status.NOT_FOUND).build();
			}
			if(!isAdminOf(assessedIdentity, request)) {
				return Response.serverError().status(Status.FORBIDDEN).build();
			}

			certificatesManager.uploadStandaloneCertificate(assessedIdentity, creationDate, courseTitle, archivedResourceKey, tmpFile);
			return Response.ok().build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}
	
	
	private boolean isAdminOf(Identity assessedIdentity, HttpServletRequest httpRequest) {
		Roles managerRoles = getRoles(httpRequest);
		if(!managerRoles.isUserManager() && !managerRoles.isRolesManager() && !managerRoles.isAdministrator()) {
			return false;
		}
		Roles identityRoles = securityManager.getRoles(assessedIdentity);
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles);
	}
}