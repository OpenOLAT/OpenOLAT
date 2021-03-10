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
package org.olat.course.nodes.livestream.paella;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * based on https://github.com/polimediaupv/paella-opencast/blob/master/src/main/paella-opencast/ui/embed.html
 * 
 * Initial date: 11 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PaellaMapper implements Mapper {

	private static final Logger log = Tracing.createLoggerFor(PaellaMapper.class);
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private final Streams streams;
	private final PlayerProfile playerProfile;
	
	public PaellaMapper(Streams streams, PlayerProfile playerProfile) {
		this.streams = streams;
		this.playerProfile = playerProfile;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		StringMediaResource smr = new StringMediaResource();
		
		String encoding = StandardCharsets.ISO_8859_1.name();
		String mimetype = "text/html;charset=" + StringHelper.check4xMacRoman(encoding);
		smr.setContentType(mimetype);
		smr.setEncoding(encoding);
		
		String content = createContent();
		smr.setData(content);
		return smr;
	}
	
	private String createContent() {
		StringOutput sb = new StringOutput();
		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8;\">");
		sb.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
		appendStaticJs(sb, "js/paella/player/javascript/hls.min.js");
		appendStaticJs(sb, "js/paella/player/javascript/jquery.min.js");
		appendStaticJs(sb, "js/paella/player/javascript/lunr.min.js");
		appendStaticJs(sb, "js/paella/player/javascript/paella_player_es2015.js");
		sb.append("</head>");
		sb.append("<body id=\"body\" onload=\"");
		sb.append("paella.baseUrl='");
		StaticMediaDispatcher.renderStaticURI(sb, "js/paella/player/");
		sb.append("';");
		sb.append("paella.load('playerContainer', {");
		sb.append(" config: ");
		appendPlayerConfig(sb);
		sb.append(" ,");
		sb.append(" data:");
		sb.append(objectToJson(streams));
		sb.append("}");
		sb.append(");\">");
		sb.append("<div id=\"playerContainer\" style=\"display:block;width:100%\">");
		sb.append("</div>");
		sb.append("</body>");
		sb.append("</html>");
		
		String html = sb.toString();
		log.debug(html);
		return html;
	}

	private void appendStaticJs(StringOutput sb, String javascript) {
		sb.append("<script src=\"");
		StaticMediaDispatcher.renderStaticURI(sb, javascript);
		sb.append("\"></script>");
	}
	
	private void appendPlayerConfig(StringOutput sb) {
		sb.append("{");
		sb.append("  'player':{");
		sb.append("    'accessControlClass':'paella.AccessControl',");
		sb.append("        'profileFrameStrategy': 'paella.ProfileFrameStrategy',");
		sb.append("    'videoQualityStrategy': 'paella.LimitedBestFitVideoQualityStrategy',");
		sb.append("    'videoQualityStrategyParams':{ 'maxAutoQualityRes':720 },");
		sb.append("    'reloadOnFullscreen': true,");
		sb.append("    'videoZoom': {");
		sb.append("      'enabled':false,");
		sb.append("      'max':800");
		sb.append("    },");
		sb.append("    'deprecated-methods':[{'name':'streaming','enabled':true},");
		sb.append("           {'name':'html','enabled':true},");
		sb.append("           {'name':'flash','enabled':true},");
		sb.append("		   {'name':'image','enabled':true}],");
		sb.append("    'methods':[");
		sb.append("      { 'factory':'ChromaVideoFactory', 'enabled':true },");
		sb.append("      { 'factory':'WebmVideoFactory', 'enabled':true },");
		sb.append("      { 'factory':'Html5VideoFactory', 'enabled':true },");
		sb.append("      { 'factory':'MpegDashVideoFactory', 'enabled':true },");
		sb.append("      {");
		sb.append("        'factory':'HLSVideoFactory',");
		sb.append("        'enabled':true,");
		sb.append("        'config': {");
		sb.append("          'maxBufferLength': 30,");
		sb.append("		  'maxMaxBufferLength': 600,");
		sb.append("		  'maxBufferSize': 60000000,");
		sb.append("		  'maxBufferHole': 0.5,");
		sb.append("		  'lowBufferWatchdogPeriod': 0.5,");
		sb.append("          'highBufferWatchdogPeriod': 3");
		sb.append("        },");
		sb.append("        'iOSMaxStreams': 2,");
		sb.append("        'androidMaxStreams': 2,");
		sb.append("        'initialQualityLevel': 2");
		sb.append("      }");
		sb.append("    ],");
		sb.append("    'audioMethods':[");
		sb.append("      { 'factory':'MultiformatAudioFactory', 'enabled':true }");
		sb.append("    ],");
		sb.append("    'defaultAudioTag': '',");
		sb.append("    'slidesMarks':{");
		sb.append("      'enabled':true,");
		sb.append("      'color':'gray'");
		sb.append("    }");
		sb.append("  },");
		sb.append("  'data':{");
		sb.append("    'enabled':true,");
		sb.append("    'dataDelegates':{");
		sb.append("      'trimming':'CookieDataDelegate',");
		sb.append("      'metadata':'VideoManifestMetadataDataDelegate',");
		sb.append("      'cameraTrack':'TrackCameraDataDelegate'");
		sb.append("    }");
		sb.append("  },");
		sb.append("  'folders': {");
		sb.append("    'profiles': 'config/profiles',");
		sb.append("    'resources': 'resources',");
		sb.append("    'skins': 'resources/style'");
		sb.append("  },");
		sb.append("  'experimental':{");
		sb.append("    'autoplay':true");
		sb.append("  },");
		sb.append("  'plugins':{");
		sb.append("    'enablePluginsByDefault': false,");
		sb.append("    'list':{");
		sb.append("      'edu.harvard.dce.paella.flexSkipPlugin': {'enabled':true, 'direction': 'Rewind', 'seconds': 10, 'minWindowSize': 510 },");
		sb.append("      'edu.harvard.dce.paella.flexSkipForwardPlugin': {'enabled':true, 'direction': 'Forward', 'seconds': 30},");
		sb.append("      'es.upv.paella.captionsPlugin': {'enabled':true, 'searchOnCaptions':true},");
		sb.append("      'es.upv.paella.frameControlPlugin':  {'enabled': true, 'showFullPreview': 'auto', 'showCaptions':true, 'minWindowSize': 450 },");
		sb.append("      'es.upv.paella.fullScreenButtonPlugin': {'enabled':true, 'reloadOnFullscreen':{ 'enabled':true, 'keepUserSelection':true }},");
		sb.append("      'es.upv.paella.playPauseButtonPlugin': {'enabled':true},");
		sb.append("      'es.upv.paella.themeChooserPlugin':  {'enabled':true, 'minWindowSize': 600},");
		sb.append("      'es.upv.paella.viewModePlugin': { 'enabled': true, 'minWindowSize': 300 },");
		sb.append("      'es.upv.paella.volumeRangePlugin':{'enabled':true, 'showMasterVolume': true, 'showSlaveVolume': false },");
		sb.append("      'es.upv.paella.pipModePlugin': { 'enabled':true },");
		sb.append("      'es.upv.paella.audioSelector': { 'enabled':true, 'minWindowSize': 400 },");
		sb.append("      'es.upv.paella.airPlayPlugin': { 'enabled':true },");
		sb.append("      'es.upv.paella.liveStreamingIndicatorPlugin':  { 'enabled': true },");
		sb.append("      'es.upv.paella.showEditorPlugin':{'enabled':true,'alwaysVisible':true},");
		sb.append("      'es.upv.paella.videoDataPlugin': {");
		sb.append("        'enabled': true,");
		sb.append("        'excludeLocations':[");
		sb.append("          'paellaplayer.upv.es'");
		sb.append("        ],");
		sb.append("        'excludeParentLocations':[");
		sb.append("          'localhost:8000'");
		sb.append("        ]");
		sb.append("      },");
		sb.append("      'es.upv.paella.blackBoardPlugin': {'enabled': true},");
		sb.append("      'es.upv.paella.breaksPlayerPlugin': {'enabled': true},");
		sb.append("      'es.upv.paella.overlayCaptionsPlugin': {'enabled': true},");
		sb.append("      'es.upv.paella.playButtonOnScreenPlugin': {'enabled':true},");
		sb.append("      'es.upv.paella.translecture.captionsPlugin': {'enabled':true},");
		sb.append("      'es.upv.paella.trimmingPlayerPlugin': {'enabled':true},");
		sb.append("      'es.upv.paella.windowTitlePlugin': {'enabled': true},");
		playerProfile.appendPlayerConfig(sb);
		// The last plugin must not have a comma at the and of the configs.
		sb.append("      'es.upv.paella.windowTitlePlugin': {'enabled': true}");
		sb.append("    }");
		sb.append("  },");
		sb.append("  'defaultProfile':'slide_over_professor',");
		sb.append("  'standalone' : {");
		sb.append("    'repository': '../repository/'");
		sb.append("  },");
		sb.append("  'skin': {");
		sb.append("    'available': [");
		sb.append("      'dark',");
		sb.append("      'dark_small',");
		sb.append("      'light',");
		sb.append("      'light_small'");
		sb.append("    ]");
		sb.append("  }");
		sb.append("}");
	}
	
	private String objectToJson(Object o)  {
		String json = null;
		try {
			json = mapper.writeValueAsString(o);
		} catch (Exception e) {
			json = "{}";
		}
		json = json.replace("\"", "'");
		log.debug(json);
		return json;
	}

}
