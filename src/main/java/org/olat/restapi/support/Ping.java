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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc The version of this specific Web Service
   * @response.representation.200.example 1.0
	 * @return
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Return a string
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc Return a small string
   * @response.representation.200.example Ping
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping() {
		return Response.ok("Ping").build();
	}
	
	/**
	 * Return a concatenation of the string as parameter and Ping
   * @response.representation.doc Send a small string over the connection
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc Return a small string
   * @response.representation.200.example Ping hello
   * @param name a name
	 * @return
	 */
	@POST
	@Path("{name}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping(@PathParam("name") String name) {
		return Response.ok("Ping " + name).build();
	}
}
