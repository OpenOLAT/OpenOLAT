/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.commons.servlets.pathhandlers;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.olat.commons.servlets.util.ResourceDescriptor;


/**
 * Initial Date:  16.06.2003
 *
 * @author Mike Stock
 * 
 * Comment:  
 * @deprecated Please use GlobalMapperRegistry if you need to provide an url for e.g. static resources which are shared by all users
 * 
 */
public interface PathHandler {

	/**
	 * Called upon initialization of OLAT.
	 * @param config
	 * @deprecated Please use GlobalMapperRegistry if you need to provide an url for e.g. static resources which are shared by all users
	 */
	public void init(String path);
	
	/**
	 * Get a resource descriptor for the selected path.
	 * @param request
	 * @param relPath
	 * @return A resource descriptor, describing the resource, or null if the handler decides it will not serve the resource. in the latter case the servlet will then send a HttpServletResponse.SC_NOT_FOUND (404) error.
	 * @deprecated Please use GlobalMapperRegistry if you need to provide an url for e.g. static resources which are shared by all users
	 */
	public ResourceDescriptor getResourceDescriptor(HttpServletRequest request, String relPath);
	
	/**
	 * Get an input stream for the given resource descriptor.
	 * @param request
	 * @param rd
	 * @return InputStream.
	 * @deprecated Please use GlobalMapperRegistry if you need to provide an url for e.g. static resources which are shared by all users
	 */
	public InputStream getInputStream(HttpServletRequest request, ResourceDescriptor rd);
}
