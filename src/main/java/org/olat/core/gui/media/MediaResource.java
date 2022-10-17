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

package org.olat.core.gui.media;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Felix Jost
 */
public interface MediaResource {
	
	public boolean acceptRanges();

	/**
	 * @return The mime type
	 */
	public String getContentType();

	/**
	 * @return The size or null if unknown
	 */
	public Long getSize();

	/**
	 * @return The stream or null
	 */
	public InputStream getInputStream();

	/**
	 * @return The last modification date or null
	 */
	public Long getLastModified();
	
	/**
	 * 
	 * @return The cache duration in seconds. If 0, all the headers
	 * 			to prevent caching will be added.
	 */
	public long getCacheControlDuration();

	/**
	 * @param hres
	 */
	public void prepare(HttpServletResponse hres);

	/**
	 * 
	 */
	public void release();

}