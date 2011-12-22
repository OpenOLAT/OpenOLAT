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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.webservices.clients.onyxreporter;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

//<ONYX-705>

@WebService(name = "ReporterWSService", targetNamespace = "http://server.webservice.plugin.bps.de/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
interface OnyxReporterServices {

	/**
	 * @param version
	 * @param userId
	 * @param role
	 * @param secret
	 * @param lastName
	 * @param firstName
	 * @param additionalParams
	 */
	@WebMethod(operationName = "armSite")
	 String armSite(
		@WebParam(name = "version") final Integer version,
		@WebParam(name = "userId") final String userId,
		@WebParam(name = "optionalRole") final Integer role,
		@WebParam(name = "secretToShare") final String secretToShare,
		@WebParam(name = "optionalUserLastName") final String userLastName,
		@WebParam(name = "optionalUserFirstName") final String userFirstName,
		@WebParam(name = "additionalParams") final HashMapWrapper additionalParams
	);
    
    @WebMethod(operationName = "initiateSite")
     String initiateSite(
    	@WebParam(name = "version") final Integer version,
    	@WebParam(name = "sessionId") final String sessionId,
    	@WebParam(name = "secretToShare") final String secretToShare,
    	@WebParam(name = "students") final ResultsForStudentsWrapper students,
    	//@WebParam(name = "students") final ArrayList<ResultsForStudent> students,
    	@WebParam(name = "optionalContentPackage") final byte[] contentPackage,
    	@WebParam(name = "additionalParams") final HashMapWrapper additionalParams
    );
    
    @WebMethod(operationName = "disarmSite")
     Boolean disarmSite(
    	@WebParam(name = "version") final Integer version,
    	@WebParam(name = "sessionId") final String sessionId,
    	@WebParam(name = "secretToShare") final String secretToShare,
    	@WebParam(name = "additionalParams") final HashMapWrapper additionalParams
    );
    
    @WebMethod(operationName = "getResultValues")
     HashMapWrapper getResultValues(
    	@WebParam(name = "version") final Integer version,
    	@WebParam(name = "sessionId") final String sessionId,
    	@WebParam(name = "secretToShare") final String secretToShare,
    	@WebParam(name = "requestedValues") final HashMapWrapper requestedValues,
    	@WebParam(name = "additionalParams", partName = "additionalParams") final HashMapWrapper additionalParams
    );
    
    @WebMethod(operationName = "getResultVariables")
	 HashMapWrapper getResultVariables(
			@WebParam(name = "version") final Integer version,
			@WebParam(name = "contentPackage") final byte[] contentPackage,
			@WebParam(name = "additionalParams") final HashMapWrapper additionalParams
	);
}
