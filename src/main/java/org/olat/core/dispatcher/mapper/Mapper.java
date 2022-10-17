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
* <p>
*/ 

package org.olat.core.dispatcher.mapper;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.gui.media.MediaResource;

/**
 * Description:<br>
 * Mapper can map a http request to a mediaresource-response. should be
 * stateless or at least not use too much memory, since registered mappers are
 * held in the user's session till the session expires.
 * <P>
 * Initial Date: 10.06.2005 <br>
 * 
 * @author Felix Jost
 */
public interface Mapper {

	/**
	 * @param relPath
	 * @param request
	 * @return MediaResource
	 */
	public MediaResource handle(String relPath, HttpServletRequest request);

}
