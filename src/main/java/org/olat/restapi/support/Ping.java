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
package org.olat.restapi.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Description:<br>
 * Ping to test the presence of the REST Api
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Ping")
@Path("ping")
public class Ping {
	
	private static final String VERSION = "1.0";
	
	/**
	 * The version of the Ping Web Service
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the Ping Web Service", description = "The version of the Ping Web Service")
	@ApiResponse(responseCode = "200", description = "Return the version number")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Return a string
	 * 
	 * @return
	 */
	@GET
	@Operation(summary = "Return a string", description = "Return a string")
	@ApiResponse(responseCode = "200", description = "Ping")
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping() {
		return Response.ok("Ping").build();
	}
	
	/**
	 * Return a concatenation of the string as parameter and Ping
	 * 
   * @param name a name
	 * @return
	 */
	@POST
	@Path("{name}")
	@Operation(summary = "Return a concatenation of the string as parameter and Ping", description = "Return a concatenation of the string as parameter and Ping")
	@ApiResponse(responseCode = "200", description = "Return a small string")	
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping(@PathParam("name") String name) {
		return Response.ok("Ping " + name).build();
	}
}
