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
package org.olat.modules.video.ui.component;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.logging.AssertException;

/**
 * 
 * Initial date: 7 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContinueAtCommand extends JSCommand {
	
	public ContinueAtCommand(String videoElementId, long timeInSeconds) {
		super(""); // do not change this command id, it is in js also

		String elementId = "o_so_vid" + videoElementId;
		StringBuilder sb = new StringBuilder(512);
		sb.append("try {\n")
		  .append(" var player = jQuery('#").append(elementId).append("').data('player');\n")
		  .append(" player.play();\n")
		  .append(" player.setCurrentTime(").append(timeInSeconds).append(");\n")
		  .append(" jQuery('#' + player.id + ' .mejs__overlay-play')\n")
		  .append("   .show()\n")
		  .append("   .css('width', '100%');\n")
		  .append("} catch(e) {\n")
		  .append("  if(window.console) console.log(e);\n")
		  .append("}");
		JSONObject subjo = new JSONObject();
		try {
			subjo.put("e", sb.toString());
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
		setSubJSON(subjo);	
	}
}
