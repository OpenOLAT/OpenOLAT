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

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.olat.core.logging.LogFileParser;
import org.olat.core.util.vfs.VFSLeaf;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Description:<br>
 * This web service returns logFiles
 * 
 * <P>
 * Initial Date:  23.12.2011 <br>
 * @author strentini, sergio.trentini@frentix.com, www.frentix.com
 */
public class LogWebService {

	private static final String VERSION = "1.0";

	public static final CacheControl cc = new CacheControl();

	static {
		cc.setMaxAge(-1);
	}

	/**
	 * The version of the Log Web Service
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the Log Web Service", description = "The version of the Log Web Service")
	@ApiResponse(responseCode = "200", description = "Return the version number")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}

	@GET
	@Path("{date}")
	@Operation(summary = "Get old version", description = "Get the version from a specific date")
	@ApiResponse(responseCode = "200", description = "Return the version number")
	@Produces({ "text/plain", MediaType.APPLICATION_OCTET_STREAM })
	public Response getLogFileByDate(@PathParam("date") String dateString) {
		VFSLeaf logFile;
		try {
			logFile = logFileFromParam(dateString);
		} catch (ParseException e) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		if (logFile == null)
			return Response.serverError().status(Status.NOT_FOUND).build();

		InputStream is = logFile.getInputStream();
		if (is == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		return Response.ok(is).cacheControl(cc).build(); // success
	}
	
	@GET
	@Operation(summary = "Returns the correct LogFile as VFSLeaf or null", description = "Returns the correct LogFile as VFSLeaf or null.<br>\n" + 
			"\n" + 
			" dateString can be: <br />\n" + 
			" <ul>\n" + 
			"  <li>\"today\" : will return the current Logfile if it exists</li>\n" + 
			"  <li>a two digit number, representing a day of the month : will return the\n" + 
			"   logFile of the given day (of the current month)</li>\n" + 
			"  <li>A Date-String of the form :  yyyy-MM-dd</li>\n" + 
			" </ul>\n" + 
			" \n" + 
			" will return null if the given String is not valid or the resulting\n" + 
			" logfile is not found")
	@ApiResponse(responseCode = "200", description = "the requested LogFile as VFSLeaf or null")
	@Produces({ "text/plain", MediaType.APPLICATION_OCTET_STREAM })
	public Response getCurrentLogFile() {
		return getLogFileByDate(null);
	}

	/**
	 * returns the correct LogFile as VFSLeaf or null.<br />
	 * 
	 * dateString can be: <br />
	 * <ul>
	 * <li>"today" : will return the current Logfile if it exists</li>
	 * <li>a two digit number, representing a day of the month : will return the
	 * logFile of the given day (of the current month)</li>
	 * <li>A Date-String of the form :  yyyy-MM-dd</li>
	 * </ul>
	 * 
	 * will return null if the given String is not valid or the resulting
	 * logfile is not found
	 * 
	 * @param dateString
	 *            a two digit number (dayOfTheMonth) or "today"
	 * @return the requested LogFile as VFSLeaf or null
	 * @throws ParseException
	 */
	private static VFSLeaf logFileFromParam(String dateString) throws ParseException {
		VFSLeaf logFile;
		if (StringUtils.isBlank(dateString) || "today".equals(dateString)) {
			logFile = LogFileParser.getLogfilePath(null);
		} else if(dateString.length() == 2){
			DateFormat formatter = new SimpleDateFormat("dd");
			Calendar calParam = Calendar.getInstance();
			calParam.setTime(formatter.parse(dateString));
			Calendar calFile = Calendar.getInstance();
			calFile.set(Calendar.DAY_OF_MONTH, calParam.get(Calendar.DAY_OF_MONTH));
			logFile = LogFileParser.getLogfilePath(calFile.getTime());
		}else{
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			logFile = LogFileParser.getLogfilePath(formatter.parse(dateString));
		}
		return logFile;
	}

}
