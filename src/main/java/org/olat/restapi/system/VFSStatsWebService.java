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
package org.olat.restapi.system;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.manager.VFSRevisionDAO;
import org.olat.restapi.system.vo.VFSStatsVO;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * 
 * <h3>Description:</h3>
 * 
 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class VFSStatsWebService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSRevisionDAO vfsRevisionDAO;
	
	public VFSStatsWebService() {
		CoreSpringFactory.autowireObject(this);
	}
	
	@GET
	@Operation(summary = "Retrieve threads info", description = "Retrieve information about threads count and number of deamons")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "The infos", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VFSStatsVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = VFSStatsVO.class))) }) })	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRevisionSizeXML() {
		long size = vfsRevisionDAO.calculateRevisionsSize();
		dbInstance.commitAndCloseSession();
		
		VFSStatsVO vo = new VFSStatsVO(size);
		return Response.ok(vo).build();
	}
}