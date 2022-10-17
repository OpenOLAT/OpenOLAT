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

import jakarta.servlet.http.HttpServletRequest;

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
	private final String paellaConfig;
	
	public PaellaMapper(Streams streams, String paellaConfig) {
		this.streams = streams;
		this.paellaConfig = paellaConfig;
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
		sb.append("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\">");
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
		sb.append(" config: ").append(paellaConfig);
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
