
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

import java.util.Set;

/**
 * 
 * Description:<br>
 * This service configure the REST Api and give the list of
 * singletons, classes for resource, web services and providers
 * and the list of types for the standard context resolver (mapping xml,
 * json to java objects). All this configuration MUST be done before the
 * REST Servlet starts. After are all configurations needless.
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface RestRegistrationService {
	
	/**
	 * Resource or provider classes for the REST Api
	 * @return
	 */
	public Set<Class<?>> getClasses();
	
	/**
	 * Add a resource or provider class
	 * @param cl
	 */
	public void addClass(Class<?> cl);
	
	/**
	 * Remove a resource or provider class
	 * @param cl
	 */
	public void removeClass(Class<?> cl);

	/**
	 * Singletons used as resource for the REST Api
	 * @return
	 */
	public Set<Object> getSingletons();
	
	/**
	 * Add a singleton to the REST Api
	 * @param singleton
	 */
	public void addSingleton(Object singleton);
	
	
	/**
	 * Remove a singleton from the REST Api
	 * @param singleton
	 */
	public void removeSingleton(Object singleton);
}
