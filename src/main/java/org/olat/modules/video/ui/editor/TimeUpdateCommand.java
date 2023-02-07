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
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.logging.AssertException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Initial date: 2023-02-06<br>
 *
 * Sends a timeupdate event to the media player itself to update its own UI and play/pause the media player
 * to trigger a timeupdate event to the listeners. Used for media elements in combination with youtube.
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimeUpdateCommand extends JSCommand {
	public TimeUpdateCommand(String videoElementId, long delayInMillis) {
		super("");

		String timeUpdatePart = "setTimeout(() => { player.pause(); player.media.dispatchEvent(mejs.Utils.createEvent('timeupdate', player.media)); }, " +
				delayInMillis + ");";

		String elementId = "o_so_vid" + videoElementId;
		StringBuilder sb = new StringBuilder(512);
		sb
				.append("try {\n")
				.append("  var player = jQuery('#").append(elementId).append("').data('player');\n")
				.append("  var loaded = jQuery('#").append(elementId).append("').data('playerloaded');\n")
				.append("  if (loaded) {\n")
				.append("    player.play();\n")
				.append("    ").append(timeUpdatePart).append("\n")
				.append("  } else {\n")
				.append("    var metaListener = function(e) {\n")
				.append("      ").append(timeUpdatePart).append("\n")
				.append("      player.media.removeEventListener(metaListener);\n")
				.append("    };\n")
				.append("    player.play();\n")
				.append("    player.media.addEventListener('loadedmetadata', metaListener);\n")
				.append("  }\n")
				.append("} catch(e) {\n")
				.append("  if (window.console) console.log(e);\n")
				.append("}");

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("e", sb.toString());
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
		setSubJSON(jsonObject);
	}
}
