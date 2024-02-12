/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.winmgr.functions;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory.InvokeIdentifier;
import org.olat.core.logging.AssertException;
import org.olat.modules.video.ui.VideoDisplayController.Marker;

import net.minidev.json.JSONArray;

/**
 * 
 * Initial date: 9 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoCommands extends Command {
	
	public enum Functions {
		
		VIDEO_CONTINUE("videocontinue"),
		VIDEO_CONTINUE_AT("videocontinueat"),
		VIDEO_PAUSE("videopause"),
		VIDEO_SELECT_TIME("videoselecttime"),
		VIDEO_TIME_UPDATE("videotimeupdate"),
		VIDEO_RELOAD_MARKER("videoreloadmarkers"),
		VIDEO_SHOW_HIDE_PROGRESS_TOOLTIP("videoshowhideprogresstooltip"),
		VIDEO_MARK_SELECT("videomarkselect"),
		VIDEO_MARK_TYPEONLY("videomarktypeonly"),
		;
		
		private String feature;
		
		private Functions(String feature) {
			this.feature = feature;
		}
		
		public String feature() {
			return feature;
		}
	}
	
	/**
	 * Create a command that executes arbitrary JS code
	 * @param javaScriptCode
	 */
	private VideoCommands(String function, JSONObject parameters) {
		super(InvokeIdentifier.VIDEO); // do not change this command id, it is in js also
		JSONObject subjo = new JSONObject();
		try {
			subjo.put("func", function);
			subjo.put("fparams", parameters);
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
		setSubJSON(subjo);		
	}
	
	public static VideoCommands valueOf(Functions function, JSONObject parameters) {
		return new VideoCommands(function.feature(), parameters);
	}
	
	public static VideoCommands valueOf(Functions function, String key, String value) {
		JSONObject parameters = new JSONObject();
		parameters.put(key, value);
		return new VideoCommands(function.feature(), parameters);
	}
	
	public static VideoCommands videoContinue(String elementId) {
		JSONObject parameters = new JSONObject();
		parameters.put("elementId", "o_so_vid" + elementId);
		return valueOf(Functions.VIDEO_CONTINUE, parameters);
	}
	
	public static VideoCommands continueAt(String elementId, long timeInSeconds) {
		JSONObject parameters = new JSONObject();
		parameters.put("elementId", "o_so_vid" + elementId);
		parameters.put("timeInSeconds", timeInSeconds);
		return valueOf(Functions.VIDEO_CONTINUE_AT, parameters);
	}
	
	public static VideoCommands pause(String elementId, long timeInSeconds) {
		JSONObject parameters = new JSONObject();
		parameters.put("elementId", "o_so_vid" + elementId);
		parameters.put("timeInSeconds", timeInSeconds);
		return valueOf(Functions.VIDEO_PAUSE, parameters);
	}
	
	public static VideoCommands selectTime(String elementId, long timeInSeconds) {
		JSONObject parameters = new JSONObject();
		parameters.put("elementId", "o_so_vid" + elementId);
		parameters.put("timeInSeconds", timeInSeconds);
		return valueOf(Functions.VIDEO_SELECT_TIME, parameters);
	}
	
	public static VideoCommands timeUpdate(String elementId, long delayInMillis) {
		JSONObject parameters = new JSONObject();
		parameters.put("elementId", "o_so_vid" + elementId);
		parameters.put("delayInMillis", delayInMillis);
		return valueOf(Functions.VIDEO_TIME_UPDATE, parameters);
	}
	
	public static VideoCommands reloadMarkers(String elementId, List<Marker> markers) {
		JSONArray markersArray = new JSONArray();
		
		for(Marker marker:markers) {
			JSONObject m = new JSONObject();
			m.put("id", marker.getId());
			m.put("color", marker.getColor());
			m.put("time", marker.getTime());
			m.put("action", marker.getAction());
			m.put("showInTimeline", marker.isShowInTimeline());
			markersArray.add(m);
		}
		
		JSONObject parameters = new JSONObject();
		parameters.put("elementId", "o_so_vid" + elementId);
		parameters.put("markers", markersArray);
		return valueOf(Functions.VIDEO_RELOAD_MARKER, parameters);
	}
	
	public static VideoCommands showHideProgressTooltip(String elementId, boolean show) {
		JSONObject parameters = new JSONObject();
		parameters.put("elementId", "o_so_vid" + elementId);
		parameters.put("show", show);
		return valueOf(Functions.VIDEO_SHOW_HIDE_PROGRESS_TOOLTIP, parameters);
	}
	
	public static VideoCommands markSelect(String id, String typeClass) {
		JSONObject parameters = new JSONObject();
		parameters.put("id", id);
		parameters.put("typeClass", typeClass);
		return valueOf(Functions.VIDEO_MARK_SELECT, parameters);
	}
	
	public static VideoCommands markTypeOnly(String typeClass) {
		JSONObject parameters = new JSONObject();
		parameters.put("typeClass", typeClass);
		return valueOf(Functions.VIDEO_MARK_SELECT, parameters);
	}

}
