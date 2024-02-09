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

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory.InvokeIdentifier;
import org.olat.core.logging.AssertException;

/**
 * This is the list of commands which are executed natively by o_aexecute() method
 * in functions.js. The is only the name of the command and the parameters to
 * execute it.
 * 
 * Initial date: 9 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FunctionCommand extends Command {
	
	public enum Functions {
		ADD_CLASS_BOODY("addclassbody"),
		REMOVE_CLASS_BOODY("removeclassbody"),
		SHOW_INFO_MESSAGE("showinfomessage"),
		CLOSE_LIGHTBOX("closelightbox"),
		CLOSE_DIALOG("closedialog"),
		DISPOSE_AUDIO_RECORDER("disposeaudiorecorder"),
		DISPOSE_VIDEO_RECORDER("disposevideorecorder"),
		
		SET_DOCUMENT_TITLE("setdocumenttitle"),
		SET_BUSINESS_PATH("setbusinesspath"),
		SET_BODY_DATA_RESOURCE("setbodydataresource"),
		SET_COURSE_DATA_ATTRIBUTES("setcoursedataattributes"),
		
		FLEXITABLE_UPDATE_CHECKALL("tableupdatecheckkallmenu"),
		RELOAD_WINDOW("reloadWindow"),
		PRINT("print"),
		
		INIT_CAMERAANDSCANNER("initcameraandscanner"),
		CLEANUP_CAMERAANDSCANNER("cleanupcameraandscanner"),
		TINYMCE_WRITE_LINK_TO_SELECTION("tinywritelinkselection"),
		TINYMCE_EXEC_COMMAND("tinyexec"),
		START_LTI_13("startlti13"),
		UNLOAD_SCO("unloadsco"),
		ANALYTICS("analytics"),
		
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
	private FunctionCommand(String function, JSONObject parameters) {
		super(InvokeIdentifier.FUNCTION); // do not change this command id, it is in js also
		JSONObject subjo = new JSONObject();
		try {
			subjo.put("func", function);
			subjo.put("fparams", parameters);
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
		setSubJSON(subjo);		
	}
	
	public static FunctionCommand valueOf(Functions function, JSONObject parameters) {
		return new FunctionCommand(function.feature(), parameters);
	}
	
	public static FunctionCommand valueOf(Functions function, String key, String value) {
		JSONObject parameters = new JSONObject();
		parameters.put(key, value);
		return new FunctionCommand(function.feature(), parameters);
	}
	
	public static FunctionCommand addClassToBody(String cssClass) {
		return valueOf(Functions.ADD_CLASS_BOODY, "class", cssClass);
	}
	
	public static FunctionCommand removeClassToBody(String cssClass) {
		return valueOf(Functions.REMOVE_CLASS_BOODY, "class", cssClass);
	}
	
	public static FunctionCommand showInfoMessage(String title, String message) {
		JSONObject parameters = new JSONObject();
		parameters.put("title", title);
		parameters.put("message", message);
		return valueOf(Functions.SHOW_INFO_MESSAGE, parameters);
	}
	
	public static FunctionCommand reloadWindow() {
		return valueOf(Functions.RELOAD_WINDOW, null);
	}
	
	public static FunctionCommand closeDialog(String boxId) {
		return valueOf(Functions.CLOSE_DIALOG, "dialogid", boxId);
	}
	
	public static FunctionCommand closeLightBox(String boxId) {
		return valueOf(Functions.CLOSE_LIGHTBOX, "boxid", boxId);
	}
	
	public static FunctionCommand disposeAudioRecorder() {
		return valueOf(Functions.DISPOSE_AUDIO_RECORDER, null);
	}
	
	public static FunctionCommand disposeVideoRecorder() {
		return valueOf(Functions.DISPOSE_VIDEO_RECORDER, null);
	}
	
	public static FunctionCommand setDocumentTitle(String title) {
		return valueOf(Functions.SET_DOCUMENT_TITLE, "title", title);
	}
	
	public static FunctionCommand setBusinessPath(String url) {
		return valueOf(Functions.SET_BUSINESS_PATH, "url", url);
	}
	
	public static FunctionCommand setBodyDataResource(String restype, String resid, String repoentryid) {
		JSONObject parameters = new JSONObject();
		parameters.put("restype", restype);
		parameters.put("resid", resid);
		parameters.put("repoentryid", repoentryid);
		return valueOf(Functions.SET_BODY_DATA_RESOURCE, parameters);
	}
	
	public static FunctionCommand setCourseDataAttributes(String nodeId, JSONObject nodeInfos) {
		JSONObject parameters = new JSONObject();
		parameters.put("nodeid", nodeId);
		parameters.put("nodeinfos", nodeInfos);
		return valueOf(Functions.SET_COURSE_DATA_ATTRIBUTES, parameters);
	}
	
	public static FunctionCommand print(String url) {
		return valueOf(Functions.PRINT, "url", url);
	}
	
	public static FunctionCommand tableUpdateCheckAllMenu(String dispatchId, boolean showSelectAll, boolean showDeselectAll,
			String selectedEntriesInfo) {
		JSONObject parameters = new JSONObject();
		parameters.put("did", dispatchId);
		parameters.put("showSelectAll", showSelectAll);
		parameters.put("showDeselectAll", showDeselectAll);
		parameters.put("infos", selectedEntriesInfo);
		return valueOf(Functions.FLEXITABLE_UPDATE_CHECKALL, parameters);
	}
	
	public static FunctionCommand writeLinkToTinyMCESelection(String url, Integer width, Integer height) {
		JSONObject parameters = new JSONObject();
		parameters.put("url", url);
		parameters.put("width", width);
		parameters.put("height", height);
		return valueOf(Functions.TINYMCE_WRITE_LINK_TO_SELECTION, parameters);
	}
	
	public static FunctionCommand tinyMCEExec(String command, JSONObject params) {
		JSONObject parameters = new JSONObject();
		parameters.put("tcommand", command);
		parameters.put("tparams", params);
		return valueOf(Functions.TINYMCE_EXEC_COMMAND, parameters);
	}

	public static FunctionCommand initCameraAndScanner() {
		return valueOf(Functions.INIT_CAMERAANDSCANNER, null);
	}
	
	public static FunctionCommand cleanUpCameraAndScanner() {
		return valueOf(Functions.CLEANUP_CAMERAANDSCANNER, null);
	}
	
	public static FunctionCommand startLti13(String frmConnectId) {
		return valueOf(Functions.START_LTI_13, "frmConnectId", frmConnectId);
	}
	
	public static FunctionCommand unloadSco(String scoCommand, String currentScoId, String nextScoId) {
		JSONObject parameters = new JSONObject();
		parameters.put("scoCommand", scoCommand);
		parameters.put("currentSco", currentScoId);
		parameters.put("nextSco", nextScoId);
		return valueOf(Functions.UNLOAD_SCO, parameters);
	}
	
	public static FunctionCommand analytics(String type, String url, String title) {
		JSONObject parameters = new JSONObject();
		parameters.put("type", type);
		parameters.put("url", url);
		parameters.put("title", title);
		return valueOf(Functions.ANALYTICS, parameters);
	}
}
