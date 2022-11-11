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
package org.olat.ims.lti.ui;

import java.io.Serializable;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 13.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TalkBackMapper implements Mapper, Serializable {

	private static final long serialVersionUID = -8319259842325597955L;
	private static final Logger log = Tracing.createLoggerFor(TalkBackMapper.class);
	
	private Translator trans;
	private String themeBaseUri;

	public TalkBackMapper(Locale loc, String themeBaseUri) {
		this.trans = Util.createPackageTranslator(this.getClass(), loc);		
		this.themeBaseUri = themeBaseUri;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {	
		StringMediaResource mediares = new StringMediaResource();
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head>")
			.append("\n<link rel=\"stylesheet\" id=\"ocontentcss\" href=\"").append(themeBaseUri).append("content.css\" />\n")
			.append("<title>").append(trans.translate("talkback.title")).append("</title></head><body>")
			.append("<div class='o_lti_talkback'>")
			.append("<h1>").append(trans.translate("talkback.title")).append("</h1>")
			.append("<p class='o_hint'>").append(trans.translate("talkback.info")).append("</p>");
		String lti_msg = request.getParameter("lti_msg");
		if (StringHelper.containsNonWhitespace(lti_msg)) {
			sb.append("<div class='o_note'><h3>").append(trans.translate("talkback.msg")).append("</h3><p>")
				.append(request.getParameter("lti_msg")).append("</p></div>");			
		}
		String lti_errormsg = request.getParameter("lti_errormsg");
		if (StringHelper.containsNonWhitespace(lti_errormsg)) {
			sb.append("<div class='o_error'><h3>").append(trans.translate("talkback.errormsg")).append("</h3><p>")
				.append(lti_errormsg).append("</p></div>");			
		}
		String lti_log = request.getParameter("lti_log");
		if (StringHelper.containsNonWhitespace(lti_log)) {
			sb.append("<h3>").append(trans.translate("talkback.log")).append("</h3><pre>\n")
				.append(lti_log).append("\n</pre>");			
		}
		String lti_errorlog = request.getParameter("lti_errorlog");
		if (StringHelper.containsNonWhitespace(lti_errorlog)) {
			sb.append("<h3>").append(trans.translate("talkback.errorlog")).append("</h3><pre>\n")
				.append(lti_errorlog).append("\n</pre>");			
		}
		sb.append("</div></body></html>");
		
		if (log.isDebugEnabled()) {
			ServletUtil.printOutRequestParameters(request);
			
		}
		mediares.setData(sb.toString());
		mediares.setContentType("text/html");
		mediares.setEncoding("UTF-8");
		return mediares;
	}
}