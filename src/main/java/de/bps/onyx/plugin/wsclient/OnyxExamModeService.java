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
package de.bps.onyx.plugin.wsclient;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import de.bps.onyx.plugin.wsserver.MapWrapper;
import de.bps.onyx.plugin.wsserver.StudentIdsWrapper;

@WebService(name = "OnyxTestService", targetNamespace = "http://server.webservice.plugin.bps.de/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface OnyxExamModeService {

	@WebMethod(operationName = "registerTest")
	public Long registerTest(@WebParam(name = "testSessionId") final Long testSessionId, @WebParam(name = "providerId") final String providerId,
			@WebParam(name = "contentPackage") final byte[] contentPackage, @WebParam(name = "parameters") final MapWrapper parameters);

	@WebMethod(operationName = "registerStudent")
	public Long registerStudent(@WebParam(name = "testSessionId", partName = "testSessionId") final Long testSessionId,
			@WebParam(name = "studentId", partName = "studentId") final Long studentId, @WebParam(name = "payload", partName = "payload") final byte[] payload,
			@WebParam(name = "parameters") final MapWrapper parameters);

	@WebMethod(operationName = "testControl")
	public Long testControl(@WebParam(name = "testSessionId") final Long testSessionId, @WebParam(name = "studentIds") final StudentIdsWrapper students,
			@WebParam(name = "status") final Integer status, @WebParam(name = "parameters") final MapWrapper parameters);
}
/*
history:

$Log: OnyxExamModeService.java,v $
Revision 1.5  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/