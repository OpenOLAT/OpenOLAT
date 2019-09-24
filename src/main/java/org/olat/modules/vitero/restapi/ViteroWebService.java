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
package org.olat.modules.vitero.restapi;


import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 06.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Vitero")
@Path("vitero")
@Component
public class ViteroWebService {
	
	@Path("{resourceName}/{resourceId}/{subIdentifier}")
	public ViteroBookingWebService getBookingWebService(@PathParam("resourceName") String resourceName,
			@PathParam("resourceId") Long resourceId, @PathParam("subIdentifier") String subIdentifier) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resourceName, resourceId);
		ViteroBookingWebService service = new ViteroBookingWebService(ores, subIdentifier);
		CoreSpringFactory.autowireObject(service);
		return service;
	}
	
}
