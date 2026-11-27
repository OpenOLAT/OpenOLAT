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
package org.olat.modules.edusharing;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Delivers the unchanged edu-sharing HTML of one item as a page of its own on the
 * content domain (display mode ORIGINAL_IFRAME). The page reports its height to the
 * main window with iframeResizer.
 *
 * Initial date: 27.11.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdusharingIFrameMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(EdusharingIFrameMapper.class);
	
	private final Identity viewer;
	private final EdusharingUsage usage;
	private final String version;
	private final String width;
	private final String height;
	private final String language;
	private final String goToUrl;
	private final boolean hideLicense;
	private final boolean hideInfos;
	
	public EdusharingIFrameMapper(Identity viewer, EdusharingUsage usage, String version, String width, String height,
			String language, String goToUrl, boolean hideLicense, boolean hideInfos) {
		this.viewer = viewer;
		this.usage = usage;
		this.version = version;
		this.width = width;
		this.height = height;
		this.language = language;
		this.goToUrl = goToUrl;
		this.hideLicense = hideLicense;
		this.hideInfos = hideInfos;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if (!"/frame.html".equals(relPath)) {
			return new NotFoundMediaResource();
		}
		EdusharingService edusharingService = CoreSpringFactory.getImpl(EdusharingService.class);
		try (EdusharingResponse edusharingResponse = edusharingService.getRendered(viewer, usage, version, width, height, language)) {
			if (!edusharingResponse.hasContent()) {
				return new NotFoundMediaResource();
			}
			String html = new String(edusharingResponse.getContent().readAllBytes(), StandardCharsets.UTF_8)
					.replace(EdusharingDispatcher.LMS_INLINE_HELPER_SCRIPT, goToUrl);
			StringMediaResource resource = new StringMediaResource();
			resource.setContentType("text/html");
			resource.setEncoding(StandardCharsets.UTF_8.name());
			resource.setData(toPage(html));
			return resource;
		} catch (Exception e) {
			log.error("edu-sharing frame failed. identifier={}", usage.getIdentifier(), e);
			return new NotFoundMediaResource();
		}
	}
	
	private String toPage(String html) {
		StringOutput sb = new StringOutput(html.length() + 1024);
		sb.append("<!DOCTYPE html>\n<html lang=\"").append(StringHelper.escapeHtml(language)).append("\"><head>\n")
		  .append("<meta charset=\"utf-8\">\n")
		  .append("<base target=\"_blank\">\n");
		if (hideLicense) {
			sb.append("<style>.license { display: none; }</style>\n");
		}
		if (hideInfos) {
			sb.append("<style>.edusharing_metadata_wrapper { display: none; }</style>\n");
		}
		sb.append("<script>window.iFrameResizer = { targetOrigin: '")
		  .append(StringHelper.escapeJavaScript(Settings.createServerURI())).append("' };</script>\n")
		  .append("<script src=\"");
		StaticMediaDispatcher.renderStaticURI(sb, "js/iframeResizer/iframeResizer.contentWindow.min.js");
		sb.append("\"></script>\n<script src=\"");
		StaticMediaDispatcher.renderStaticURI(sb, "js/edusharing/gotobridge.js");
		sb.append("\"></script>\n<script>o_registerEdusharingGoToBridge('")
		  .append(StringHelper.escapeJavaScript(Settings.createServerURI())).append("','")
		  .append(StringHelper.escapeJavaScript(EdusharingDispatcher.getGoToPath())).append("');</script>\n")
		  .append("</head><body style=\"margin:0\">\n")
		  .append(html)
		  .append("\n</body></html>");
		return sb.toString();
	}
	
}
