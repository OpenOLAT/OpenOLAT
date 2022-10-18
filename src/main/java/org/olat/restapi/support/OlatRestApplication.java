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

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

import org.olat.core.CoreSpringFactory;

import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;

/**
 * 
 * Description:<br>
 * REST Application
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class OlatRestApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<>();
		classes.add(JacksonXmlBindJsonProvider.class);	
		classes.addAll(getRestRegistrationService().getClasses());
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		return getRestRegistrationService().getSingletons();
	}
	
	public RestRegistrationService getRestRegistrationService() {
		return (RestRegistrationService)CoreSpringFactory.getBean(RestRegistrationService.class);
	}
}
