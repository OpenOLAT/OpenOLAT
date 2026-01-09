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

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Certificates")
@Component
@Path("repo/courses/{resourceKey}/certificates")
public class CourseCertificationWebService {

	@Autowired
	private RepositoryService repositoryService;
	
	@Path("")
	@Operation(summary = "To get the web service for the certificates",
		description = "To get the web service for the certificates")
	@ApiResponse(responseCode = "200", description = "The web service for certificates")	
	public RepositoryEntryCertificationWebService getCertificatesWebService(@PathParam("resourceKey") Long resourceKey) {
		
		RepositoryEntry entry = repositoryService.loadByResourceKey(resourceKey);
		if(entry == null) {
	    	throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		RepositoryEntryCertificationWebService service = new RepositoryEntryCertificationWebService(entry);
		CoreSpringFactory.autowireObject(service);
		return service;
	}

}
