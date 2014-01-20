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

package org.olat.core.gui.render.velocity;

import org.olat.core.configuration.Initializable;

/**
 * Initial Date: Apr 29, 2004
 * @author Mike Stock Comment:
 */
public class VelocityModule implements Initializable {

	private static final String DEFAULT_ENCODING = "UTF-8";

	private static String inputEncoding = DEFAULT_ENCODING;
	private static String outputEncoding = DEFAULT_ENCODING;
	private static String parserPoolSize = "20";
	
	/**
	 * [spring]
	 */
	private VelocityModule() {
		// called by spring
	}

	/**
	 * @return Returns the inputEncoding.
	 */
	public static String getInputEncoding() {
		return inputEncoding;
	}

	/**
	 * @return Returns the outputEncoding.
	 */
	public static String getOutputEncoding() {
		return outputEncoding;
	}
	
	public void init() {
		VelocityHelper.getInstance();
	}

	/**
	 * @return the parser pool size for the velocity parser pool configuration
	 */
	public static String getParserPoolSize() {
		return parserPoolSize;
	}
	
	/**
	 * [SPRING]
	 * @param newParserPoolSize Set the new parser pool size
	 */
	public void setParserPoolSize(String newParserPoolSize) {
		parserPoolSize = newParserPoolSize;
	}



}