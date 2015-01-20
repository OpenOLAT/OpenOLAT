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

package org.olat.core.gui.control.winmgr;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * Initial Date:  22.03.2006 <br>
 *
 * @author Felix Jost
 */
public class JSCommand extends Command {

	/**
	 * Create a command that executes arbitrary JS code
	 * @param javaScriptCode
	 */
	public JSCommand(String javaScriptCode) {
		super(1); // do not change this command id, it is in js also
		JSONObject subjo = new JSONObject();
		try {
			subjo.put("e", javaScriptCode);
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
		setSubJSON(subjo);		
	}
}
