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
package org.olat.modules.video.ui.marker;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.logging.AssertException;
import org.olat.modules.video.ui.VideoDisplayController.Marker;

/**
 * 
 * Initial date: 28 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReloadMarkersCommand extends JSCommand {
	
	public ReloadMarkersCommand(String videoElementId, List<Marker> markers) {
		super("");
		
		String elementId = "o_so_vid" + videoElementId;
		StringBuilder sb = new StringBuilder(512);
		sb.append("try {\n")
		  .append(" var markers = [");
		
		boolean add = false;
		for(Marker marker:markers) {
			if(add) {
				sb.append(",");
			} else {
				add = false;
			}
			// [{  'id' : 'marker-id', 'color': 'pink', 'time': 56, 'action': 'none', 'showInTimeline' : true }]
			sb.append("{'id':'").append(marker.getId()).append("',")
			  .append(" 'color':'").append(marker.getColor()).append("',")
			  .append(" 'time':").append(marker.getTime()).append(",")
			  .append(" 'action':'").append(marker.getAction()).append("',")
			  .append(" 'showInTimeline':").append(marker.isShowInTimeline())
			  .append("}");
		}
		
		sb.append("];")
		  .append(" var player = jQuery('#").append(elementId).append("').data('player');\n")
		  .append(" var loaded = jQuery('#").append(elementId).append("').data('playerloaded');\n")
		  .append(" if(loaded) {\n")
		  .append("  player.pause();\n")
		  .append("  player.clearmarkers(player);\n")
		  .append("  player.rebuildmarkers(player, markers);\n")
		  .append(" } else {")
		  .append("  var metaListener = function(e) {\n")
		  .append("   player.pause();\n")
		  .append("   player.clearmarkers(player);\n")
		  .append("   player.rebuildmarkers(player, markers);\n")
		  .append("   player.media.removeEventListener(metaListener);\n")
		  .append("  };\n")
		  .append("  player.play();")
		  .append("  player.media.addEventListener('loadedmetadata', metaListener);")
		  .append(" }")
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
