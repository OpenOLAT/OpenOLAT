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
 * This can replace the method setInfo of WindowControl if you want
 * to download a file at the same time.
 * 
 * Initial date: 18 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShowInfoCommand extends Command {

	/**
	 * Show the blue box info message. The texts are not
	 * escaped.
	 * 
	 * @param title The title
	 * @param content The content
	 */
	public ShowInfoCommand(String title, String content) {
		super(1); // do not change this command id, it is in js also
		JSONObject subjo = new JSONObject();
		try {
			StringBuilder sb = new StringBuilder(512);
			sb.append("setTimeout(function() {\n")
			  .append(" try {\n")
			  .append("  showInfoBox(\"").append(title).append("\",\"").append(content).append("\");\n")
			  .append(" } catch(e){ }\n")
			  .append("}, 100);");
			subjo.put("e", sb.toString());
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
		setSubJSON(subjo);		
	}
}
