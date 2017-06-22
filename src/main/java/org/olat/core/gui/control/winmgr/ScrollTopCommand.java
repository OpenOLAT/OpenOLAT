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
package org.olat.core.gui.control.winmgr;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.logging.AssertException;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScrollTopCommand extends Command {

	/**
	 * Create a command that executes arbitrary JS code
	 * @param javaScriptCode
	 */
	public ScrollTopCommand() {
		super(1); // do not change this command id, it is in js also
		JSONObject subjo = new JSONObject();
		try {
			subjo.put("e", "try { o_scrollToElement('#o_top'); } catch(e){ }");
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
		setSubJSON(subjo);		
	}
}
