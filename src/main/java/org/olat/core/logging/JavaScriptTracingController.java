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
package org.olat.core.logging;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.HtmlHeaderComponent;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NothingChangedMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * <h3>Description:</h3>
 * The java script tracing controller allows logging java script messages to the
 * server console. This is very handy to not use stupid alert() messages all
 * over your javascript code.
 * <p>
 * In addition, you can leave some helpful debugging messages in the code if you
 * need to do some real-time debugging.
 * <p>
 * When the log4j log level for this class is set to debug, all java script
 * debug messages will be logged to the system console. While development, it
 * can be usefull to set the debug level for this class to debug by default
 * (webapp/WEB-INF/log4j.properties):
 * <p>
 * <code>
 * log4j.logger.org.olat.core.logging.JavaScriptTracingController=DEBUG
 * </code>
 * <p>
 * JS Example:
 * <p>
 * <code>
 * [...]
 * if (jQuery(document).ooLog().isDebugEnabled()) {
 * 	// always test if in log debug before loggin!!
 * 	jQuery(document).ooLog('debug','This is a cool ajaxified debug message', 'myjsfile.js');
 * }
 * [...]
 * </code>
 * <p>
 * Initial Date: 09.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class JavaScriptTracingController extends BasicController {
	/**
	 * Constructor for the javascript tracing. This uses ajax to push js debug
	 * messages to the OLAT console.
	 * <p>
	 * DO NOT USE THIS CONTROLLER! Only the chief conroller should use this
	 * controller. In your code you can use the B_AjaxLogger directly without
	 * dealing with any java code at all!
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public JavaScriptTracingController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// dummy translator, empty
		// dummy velocity container, empty
		VelocityContainer mainVC = createVelocityContainer("JSTracing");

		// The mapper that handels all log postings
		Mapper mapper = new Mapper() {
			public MediaResource handle(String relPath, HttpServletRequest request) {
				// the log message
				String logMsg = request.getParameter("logMsg");
				// optional, the logging file name
				String jsFile = request.getParameter("jsFile");
				// currently only debug level is supported but in the future...
				String level = request.getParameter("level");
				if(logMsg == null) {
					logMsg = "";
				}
				
				if(level != null) {
					if("debug".equals(level)) {
						logDebug(logMsg, jsFile);
					} else if("info".equals(level)) {
						logInfo(logMsg, jsFile);
					} else if("warn".equals(level)) {
						logWarn(logMsg + "/" + jsFile, null);
					} else if("error".equals(level)) {
						logError(logMsg + "/" +  jsFile, null);
					} else if("audit".equals(level)) {
						logAudit(logMsg + "/" +  jsFile, null);
					}
				}
				// sent empty response
				StringMediaResource mediaResource = new StringMediaResource();
				mediaResource.setEncoding("utf-8");
				mediaResource.setContentType("text/javascript;charset=utf-8");
				mediaResource.setData("");
				return mediaResource;
			}
		};
		String mapperUri = registerMapper(ureq, mapper);

		// push some variables to the header that are needed to initialize the
		// JS Tracing
		StringBuilder jsHeader = new StringBuilder();
		jsHeader.append("<script type='text/javascript'>o_info.JSTracingUri='").append(mapperUri)
		        .append("/';o_info.JSTracingLogDebugEnabled=").append(isLogDebugEnabled()).append(";</script>");
		HtmlHeaderComponent JSTracingHeader = new HtmlHeaderComponent("JSTracingHeader", null, jsHeader.toString());
		mainVC.put("JSTracingHeader", JSTracingHeader);

		putInitialPanel(mainVC);
	}

	protected void doDispose() {
		// mapper deregistered by basic controller
	}

	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}
}
