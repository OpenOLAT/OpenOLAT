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

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateManagedFlag;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 17.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Certificates")
@Component
@Path("users/{identityKey}/certificates")
public class UserCertificationWebService {
	
	private static final Logger log = Tracing.createLoggerFor(UserCertificationWebService.class);
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private OLATResourceManager resourceManager;
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
	public Response getCertificate(@PathParam("identityKey") Long identityKey,
			@QueryParam("managed") Boolean managed, @QueryParam("last") Boolean last,
			@QueryParam("externalId") String externalId, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		final boolean isAdmin = isAdminOf(identity, request);
		if(!isAdmin) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<Certificate> certificates;
		if(managed != null || last != null || StringHelper.containsNonWhitespace(externalId)) {
			certificates = certificatesManager.getCertificates(identity, null, externalId, managed, last);
		} else {
			certificates = certificatesManager.getCertificates(identity, null, null, null, true);
		}
		List<CertificateVO> certificatesVoList = certificates.stream()
				.map(cert -> CertificateVO.valueOf(cert, UserVOFactory.get(cert.getIdentity(), true, isAdmin)))
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
	public Response headCertificateInfos(@PathParam("identityKey") Long identityKey, @PathParam("certificateKey") Long certificateKey,
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
	
	@GET
	@Path("{certificateKey}")
	@Operation(summary = "Return the certificate", description = "Return the certificate")
	@ApiResponse(responseCode = "200", description = "The certificate")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCertificateInfos(@PathParam("identityKey") Long identityKey, @PathParam("certificateKey") Long certificateKey,
			@Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		boolean isAdmin = isAdminOf(identity, request);
		if(!isAdmin) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Certificate certificate = certificatesManager.getCertificateById(certificateKey);
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		} else if(!identityKey.equals(certificate.getIdentity().getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		UserVO user = UserVOFactory.get(certificate.getIdentity(), true, isAdmin);
		CertificateVO certificateVo = CertificateVO.valueOf(certificate, user);
		return Response.ok(certificateVo).build();
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
	public Response getCertificatePdf(@PathParam("identityKey") Long identityKey, @PathParam("certificateKey") Long certificateKey,
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
	
	@DELETE
	@Path("{certificateKey}")
	@Operation(summary = "Delete certificate", description = "Delete certificate")
	@ApiResponse(responseCode = "200", description = "The certificate was deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	public Response deleteCertificateInfo(@PathParam("identityKey") Long identityKey, @PathParam("certificateKey") Long certificateKey,
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
		}
		if(!identityKey.equals(certificate.getIdentity().getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		if(certificate instanceof CertificateImpl && ((CertificateImpl)certificate).getOlatResource() != null) {
			certificatesManager.deleteCertificate(certificate);
		} else {
			Long archivedResourceKey = certificate.getArchivedResourceKey();
			if(archivedResourceKey == null || archivedResourceKey.longValue() <= 0l) {
				certificatesManager.deleteStandalonCertificate(certificate);
			} else {
				OLATResource resource = resourceManager.findResourceById(archivedResourceKey);
				if(resource == null) {
					certificatesManager.deleteStandalonCertificate(certificate);
				} else {
					certificatesManager.deleteCertificate(certificate);
				}
			}
		}
		return Response.ok().build();
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
	@Operation(summary = "Upload a new standalone certificate", description = "Upload a new certificate standalone.",
			requestBody = @RequestBody(description = "", content = {
					@Content(mediaType = MediaType.MULTIPART_FORM_DATA, 
							schema = @Schema(properties= {
									@StringToClassMapItem(key="file", value=File.class),
									@StringToClassMapItem(key="courseTitle", value=String.class),
									@StringToClassMapItem(key="creationDate", value=String.class),
									@StringToClassMapItem(key="nextRecertificationDate", value=String.class),
									@StringToClassMapItem(key="archivedResourceKey", value=String.class),
									@StringToClassMapItem(key="externalId", value=String.class),
									@StringToClassMapItem(key="managedFlags", value=String.class),
							}))
			}))
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
			String nextRecertificationDateStr = partsReader.getValue("nextRecertificationDate");
			String archivedResource = partsReader.getValue("archivedResourceKey");
			String externalId = partsReader.getValue("externalId");
			CertificateManagedFlag[] managedFlags = CertificateManagedFlag.toEnum(partsReader.getValue("managedFlags"));
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
			Date nextRecertificationDate = null;
			if(StringHelper.containsNonWhitespace(nextRecertificationDateStr)) {
				nextRecertificationDate = ObjectFactory.parseDate(nextRecertificationDateStr);
			}
			
			Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
			if(assessedIdentity == null) {
				return Response.serverError().status(Response.Status.NOT_FOUND).build();
			}
			if(!isAdminOf(assessedIdentity, request)) {
				return Response.serverError().status(Status.FORBIDDEN).build();
			}
			Identity actor = getIdentity(request);
			certificatesManager.uploadStandaloneCertificate(assessedIdentity, creationDate,
					externalId, managedFlags, courseTitle, archivedResourceKey,
					nextRecertificationDate, tmpFile, actor);
			return Response.ok().build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}
	
	/**
	 * Update a certificate.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param certificate The certificate metadata.
	 * @param request The request
	 * @return The updated certificate
	 */
	@POST
	@Path("")
	@Operation(summary = "Update an existing certificate", description = "Update an existing certificate.")
	@ApiResponse(responseCode = "200", description = "if the certificate was updated")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postCertificateMetadata(@PathParam("identityKey") Long identityKey, CertificateVO certificate,
			@Context HttpServletRequest request) {
		if(!isAdministrator(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		return updateCertificateMetadata(identityKey, certificate);
	}
	
	/**
	 * Update a certificate.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param certificate The certificate metadata.
	 * @param request The request
	 * @return The updated certificate
	 */
	@PUT
	@Path("")
	@Operation(summary = "Update an existing certificate", description = "Update an existing certificate.")
	@ApiResponse(responseCode = "200", description = "if the certificate was updated")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putCertificateMetadata(@PathParam("identityKey") Long identityKey, CertificateVO certificate,
			@Context HttpServletRequest request) {
		if(!isAdministrator(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		return updateCertificateMetadata(identityKey, certificate);
	}
	
	public Response updateCertificateMetadata(Long identityKey, CertificateVO certificateVo) {
		Certificate certificate = certificatesManager.getCertificateById(certificateVo.getKey());
		if(certificate == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!certificate.getIdentity().getKey().equals(identityKey)) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		if(certificateVo.getNextCertificationDate() != null) {
			certificate.setNextRecertificationDate(certificateVo.getNextCertificationDate());
		}
		
		if(certificate instanceof CertificateImpl impl) {
			if(certificateVo.getExternalId() != null) {
				impl.setExternalId(certificateVo.getExternalId());
			}
			if(certificateVo.getManagedFlags() != null) {
				impl.setManagedFlagsString(certificateVo.getManagedFlags());
			}
			if(impl.getCertificationProgram() != null
				&& impl.getCertificationProgram().isRecertificationEnabled()
				&& impl.getCertificationProgram().isRecertificationWindowEnabled()) {
				if(certificateVo.getRecertificationWindowDate() != null) {
					impl.setRecertificationWindowDate(certificateVo.getRecertificationWindowDate());
				} else if(certificate.getNextRecertificationDate() != null) {
					Date nextCertificationDate = certificateVo.getNextCertificationDate();
					Date windowDate = certificatesManager.getDateWindowRecertification(nextCertificationDate, impl.getCertificationProgram());
					impl.setRecertificationWindowDate(windowDate);
				}
			}
		}
		
		Certificate mergedCertificate = certificatesDao.updateCertificate(certificate);
		UserVO user = UserVOFactory.get(mergedCertificate.getIdentity(), true, true);// Only for admin
		CertificateVO mergedCertificateVo = CertificateVO.valueOf(mergedCertificate, user);
		return Response.ok(mergedCertificateVo).build();
	}
	
	private boolean isAdminOf(Identity assessedIdentity, HttpServletRequest httpRequest) {
		Roles managerRoles = getRoles(httpRequest);
		if(!managerRoles.isUserManager() && !managerRoles.isRolesManager() && !managerRoles.isAdministrator()) {
			return false;
		}
		Roles identityRoles = securityManager.getRoles(assessedIdentity);
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles);
	}
	
	private boolean isAdministrator(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			return (roles.isAdministrator() || roles.isLearnResourceManager());
		} catch (Exception e) {
			return false;
		}
	}
}