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

package org.olat.commons.info.restapi;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.commons.info.InfoMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Description:<br>
 * Resource for Info Message
 * 
 * <P>
 * Initial Date:  29 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessageWebService {
	
	private InfoMessage msg;
	
	public InfoMessageWebService(InfoMessage msg) {
		this.msg = msg;
	}
	
	/**
	 * Get an new info message by key
	 * 
   * @param infoMessageKey The key
   * @param request The HTTP request
	 * @return It returns the newly info message
	 */
	@GET
	@Operation(summary = "Get an new info message by key", description = "Get an new info message by key")
	@ApiResponse(responseCode = "200", description = "The info message",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = InfoMessageVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = InfoMessageVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not Found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMessage() {
		InfoMessageVO msgVO = new InfoMessageVO(msg);
		return Response.ok(msgVO).build();
	}
}
