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
package org.olat.course.certificate.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateManagedFlag;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 8 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Tag(name = "Certificates")
public class RepositoryEntryCertificationWebService {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	
	private final RepositoryEntry entry;
	
	public RepositoryEntryCertificationWebService(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	@GET
	@Path("")
	@Operation(summary = "Return the certificates informations", description = "Return the certificates metadata")
	@ApiResponse(responseCode = "200", description = "The certificate metadata")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCertificate(@QueryParam("managed") Boolean managed, @QueryParam("last") Boolean last,
			@QueryParam("externalId") String externalId,
			@Context HttpServletRequest request) {

		if(!isAdminOf(entry, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<Certificate> certificates = certificatesManager.getCertificates(null, entry.getOlatResource(), externalId, managed, last);
		List<CertificateVO> certificatesVoList = certificates.stream()
				.map(cert -> CertificateVO.valueOf(cert, UserVOFactory.get(cert.getIdentity(), true, true)))
				.collect(Collectors.toList());
		CertificateVOes certificateVOes = new CertificateVOes();
		certificateVOes.setCertificates(certificatesVoList);
		return Response.ok(certificateVOes).build();
	}
	
	@HEAD
	@Path("{identityKey}")
	@Operation(summary = "Return the certificate", description = "Return the certificate ")
	@ApiResponse(responseCode = "200", description = "The certificate")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Produces({"application/pdf"})
	public Response headCertificateInfo(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Certificate certificate = certificatesManager.getLastCertificate(identity, entry.getOlatResource().getKey());
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
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
	@Path("{identityKey}")
	@Operation(summary = "Return the certificate as PDF file", description = "Return the certificate as PDF file")
	@ApiResponse(responseCode = "200", description = "The certificate as file")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Produces({"application/pdf"})
	public Response getCertificate(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Certificate certificate = certificatesManager.getLastCertificate(identity, entry.getOlatResource().getKey());
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}

		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(certificate);
		if(certificateFile == null || !certificateFile.exists()) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(certificateFile.getInputStream()).build();
	}
	
	@DELETE
	@Path("{identityKey}")
	@Operation(summary = "Delete certificate", description = "Delete certificate")
	@ApiResponse(responseCode = "200", description = "The certificate was deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	public Response deleteCertificateInfo(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Certificate certificate = certificatesManager.getLastCertificate(identity, entry.getOlatResource().getKey());
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		
		certificatesManager.deleteCertificate(certificate);
		return Response.ok().build();
	}
	
	/**
	 * Generate a new certificate.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @param score The score which appears in the certificate
	 * @param passed The passed/failed which appears in the certificate (true/false)
	 * @param creationDate The date of the certification
	 * @param request The request
	 * @return Nothing special
	 */
	@PUT
	@Path("{identityKey}")
	@Operation(summary = "Generate a new certificate", description = "Generate a new certificate")
	@ApiResponse(responseCode = "200", description = "If the certificate was created ")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or the resource cannot be found")
	@ApiResponse(responseCode = "500", description = "An unexpected error happened during the creation of the certificate")
	public Response putCertificate(@PathParam("identityKey") Long identityKey,
			@QueryParam("score")@Parameter(description = "The score which appears in the certificate") Float score,
			@QueryParam("maxScore")@Parameter(description = "The max score which appears in the certificate") Float maxScore,
			@QueryParam("passed") @Parameter(description = "The passed/failed which appears in the certificate (true/false)") Boolean passed,
			@QueryParam("completion") @Parameter(description = "The completion (progress) which appears in the certificate") Double completion,
			@QueryParam("grade") @Parameter(description = "The grade which appears in the certificate") String grade,
			@QueryParam("creationDate") @Parameter(description = "The date of the certification") String creationDate,
			@QueryParam("externalId") @Parameter(description = "The date of the certification") String externalId,
			@Context HttpServletRequest request) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		if(assessedIdentity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		boolean isAdmin = isAdminOf(assessedIdentity, request);
		if(!isAdmin) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		RepositoryEntryCertificateConfiguration certificateConfig = certificatesManager.getConfiguration(entry);
		CertificateTemplate template = certificateConfig.getTemplate();
		
		Identity doer = getIdentity(request);
		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, score, maxScore, passed, completion, grade, doer);
		if(StringHelper.containsNonWhitespace(creationDate)) {
			Date date = ObjectFactory.parseDate(creationDate);
			certificateInfos.setCreationDate(date);
		}
		if(StringHelper.containsNonWhitespace(externalId)) {
			certificateInfos.setExternalId(externalId);
		}
		
		CertificateConfig config = CertificateConfig.builder()
				.withCustom1(certificateConfig.getCertificateCustom1())
				.withCustom2(certificateConfig.getCertificateCustom2())
				.withCustom3(certificateConfig.getCertificateCustom3())
				.withSendEmailBcc(false)
				.withSendEmailLinemanager(false)
				.withSendEmailIdentityRelations(false)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, template, config);
		if(certificate != null) {
			UserVO user = UserVOFactory.get(assessedIdentity, true, isAdmin);
			return Response.ok(CertificateVO.valueOf(certificate, user)).build();
		}
		return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
	@Path("{identityKey}")
	@Operation(summary = "Upload a new certificate", description = "Upload a new certificate")
	@ApiResponse(responseCode = "200", description = "if the certificate was uploaded")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The owner or the certificate cannot be found")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postCertificate(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		MultipartReader partsReader = null;
		try {
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			String externalId = partsReader.getValue("externalId");
			CertificateManagedFlag[] managedFlags = CertificateManagedFlag.toEnum(partsReader.getValue("managedFlags"));
			
			String creationDateStr = partsReader.getValue("creationDate");
			Date creationDate = null;
			if(StringHelper.containsNonWhitespace(creationDateStr)) {
				creationDate = ObjectFactory.parseDate(creationDateStr);
			}

			String nextRecertificationDateStr = partsReader.getValue("nextRecertificationDate");
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
			
			certificatesManager.uploadCertificate(assessedIdentity, creationDate,
				externalId, managedFlags, entry.getOlatResource(), nextRecertificationDate, tmpFile);
			return Response.ok().build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}	finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}
	
	private boolean isAdminOf(RepositoryEntryRef entry, HttpServletRequest request) {
		try {
			Identity identity = getUserRequest(request).getIdentity();
			return repositoryService.hasRoleExpanded(identity, entry, OrganisationRoles.administrator.name(),
					OrganisationRoles.learnresourcemanager.name());
		} catch (Exception e) {
			return false;
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