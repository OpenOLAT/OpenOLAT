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

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;

/**
 * 
 * Initial date: 13.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TalkBackMapper implements Mapper, Serializable {

	private static final long serialVersionUID = -8319259842325597955L;

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {	
		StringMediaResource mediares = new StringMediaResource();
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>").append("LTI talk back").append("</title></head><body>")
		  .append("lti_msg: ").append(request.getParameter("lti_msg")).append("<br/>")
		  .append("lti_errormsg: ").append(request.getParameter("lti_errormsg")).append("<br/>")
		  .append("lti_log: ").append(request.getParameter("lti_log")).append("<br/>")
		  .append("lti_errorlog: ").append(request.getParameter("lti_errorlog")).append("<br/>")
		  .append("</body></html>");
		//ServletUtil.printOutRequestParameter(request);
		mediares.setData(sb.toString());
		mediares.setContentType("text/html");
		mediares.setEncoding("UTF-8");
		return mediares;
	}
}