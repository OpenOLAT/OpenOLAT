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
package org.olat.restapi.api;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

/**
 * 
 * Description:<br>
 * Service for general informations on the OLAT REST Api.
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("api")
@Component
public class ApiWebService {
	
	private String VERSION = "1.0";
	private String COPYRIGHT = "OpenOLAT - infinite learning\nhttp://www.openolat.org\n\nLicensed under the Apache License, Version 2.0 (the \"License\");\nyou may not use this file except in compliance with the License.\nYou may obtain a copy of the License at\n\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing,\nsoftware distributed under the License is distributed on an \"AS IS\" BASIS,\nWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\\nSee the License for the specific language governing permissions and\\nlimitations under the License.\n\nCopyright (c) frentix GmbH\nhttp://www.frentix.com";
	
	/**
	 * Version number of the whole REST API of OLAT.
	 * @response.representation.200.mediaType text/plain
	 * @response.representation.200.doc Return the version number
	 * @response.representation.200.example 1.0
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	@GET
	@Path("doc")
	@Produces(MediaType.TEXT_HTML)
	public Response getHtmlDoc() {
		InputStream in = ApiWebService.class.getResourceAsStream("_content/application.html");
		if(in == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(in).build();
	}
	
	/**
	 * Returns images for the documentation of OLAT.
   * @response.representation.200.mediaType image/jpeg
   * @response.representation.200.doc Images for the documentation
	 * @return Images
	 */
	@GET
	@Path("doc/{filename}")
	@Produces("image/jpeg")
	public Response getImage1(@PathParam("filename") String filename) {
		InputStream in = ApiWebService.class.getResourceAsStream("_content/" + filename);
		if(in == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(in).build();
	}
	
	/**
	 * Returns images for the documentation of OLAT.
   * @response.representation.200.mediaType image/jpeg
   * @response.representation.200.doc Images for the documentation
	 * @return Images
	 */
	@GET
	@Path("{filename}")
	@Produces("image/jpeg")
	public Response getImage2(@PathParam("filename") String filename) {
		InputStream in = ApiWebService.class.getResourceAsStream("_content/" + filename);
		return Response.ok(in).build();
	}
	
	/**
	 * Returns the copyright of OLAT.
   * @response.representation.200.mediaType text/html, application/xhtml+xml
   * @response.representation.200.doc The copyright of the REST API.
	 * @return The copyright
	 */
	@GET
	@Path("copyright")
	@Produces({MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML})
	public Response getCopyrightXhtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>OLAT Copyright</title></head><body><p>");
		sb.append(COPYRIGHT.replace("\n\n", "</p><p>"));
		sb.append(COPYRIGHT.replace("\n", "<br />"));
		sb.append("</p></body></html>");
		return Response.ok(sb.toString()).build();
	}
	
	/**
	 * Returns the copyright of OLAT.
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc The copyright of the REST API.
	 * @return The copyright
	 */
	@GET
	@Path("copyright")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getCopyrightPlainText() {
		return Response.ok(COPYRIGHT).build();
	}
}