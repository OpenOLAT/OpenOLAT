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
 * if (B_AjaxLogger.isDebugEnabled()) {
 * 	// always test if in log debug before loggin!!
 * 	B_AjaxLogger.logDebug("This is a cool ajaxified debug message", "myjsfile.js");
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
		Translator trans = Util.createPackageTranslator(
				JavaScriptTracingController.class, ureq.getLocale());
		// dummy velocity container, empty
		VelocityContainer mainVC = new VelocityContainer("JSTracing", Util
				.getPackageVelocityRoot(JavaScriptTracingController.class)
				+ "/JSTracing.html", trans, this);

		// The javascript ajax tracing library, attached to the dummy velocity
		// container so that it gets included
		JSAndCSSComponent tmJs = new JSAndCSSComponent("js",
				JavaScriptTracingController.class,
				new String[] { "JSTracing.js" }, null, false);
		mainVC.put("js", tmJs);

		// The mapper that handels all log postings
		Mapper mapper = new Mapper() {
			public MediaResource handle(String relPath,
					HttpServletRequest request) {
				// the log message
				String logMsg = request.getParameter("logMsg");
				// optional, the logging file name
				String jsFile = request.getParameter("jsFile");
				// currently only debug level is supported but in the future...
				String level = request.getParameter("level");
				if (level.equals("debug")) {
					// log to standard OLAT logging system
					logDebug(logMsg, jsFile);
				}
				// sent empty response
				StringMediaResource smr = new StringMediaResource();
				smr.setContentType("application/javascript");
				smr.setEncoding("utf-8");
				smr.setData("");
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
		String jsHeader = "<script type='text/javascript'>o_info.JSTracingUri='"
				+ mapperUri
				+ "/';o_info.JSTracingLogDebugEnabled="
				+ isLogDebugEnabled()
				+ ";</script>";
		HtmlHeaderComponent JSTracingHeader = new HtmlHeaderComponent(
				"JSTracingHeader", null, jsHeader);
		mainVC.put("JSTracingHeader", JSTracingHeader);

		putInitialPanel(mainVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// mapper deregistered by basic controller
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

}
