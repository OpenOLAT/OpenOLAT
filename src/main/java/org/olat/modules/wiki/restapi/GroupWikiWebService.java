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
package org.olat.modules.wiki.restapi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.group.BusinessGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * The Group Wiki Webservice<br />
 * allows the export of group wikis
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
public class GroupWikiWebService {
	

	private BusinessGroup learningGroup;

	public GroupWikiWebService(BusinessGroup group) {
		learningGroup = group;
	}

	/**
	 * will export the wiki from the current group to a CP and serve as
	 * zip-file.<br />
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Operation(summary = "will export the wiki", description = "will export the wiki from the current group to a CP and serve as\n" + 
			" zip-file.<br>")
	@ApiResponse(responseCode = "200", description = "wiki expoted")
	@Produces({ "application/zip", MediaType.APPLICATION_OCTET_STREAM })
	public Response exportWiki(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		if (learningGroup == null)
			return Response.serverError().status(Status.BAD_REQUEST).build();

		return WikiWebServiceHelper.serve(learningGroup, request, response);
	}

}
