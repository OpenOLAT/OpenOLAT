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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;

/**
 * Description:<br>
 * The log real time viewer controller offers the possibility to live-view the log file.
 * This can be handy for debugging or to monitor certain actions like a user
 * import.
 * 
 * <P>
 * Initial Date: 22.08.2008 <br>
 * 
 * @author gnaegi
 */
public class LogRealTimeViewerController extends BasicController {
	private final Level level;
	private String loggingPackage;
	private boolean collectLog = true;
	private final String appenderName;
	private final LogWriter writer = new LogWriter();
	
	private Link stopLink;
	private Link startLink;
	private Link clearLink;
	private VelocityContainer logViewerVC;

	/**
	 * Constructor for creating a real time log viewer controller
	 * 
	 * @param ureq The user request object
	 * @param control The window control object
	 * @param loggingPackage The logging package or a specific class. All messages
	 *          from the given package will be displayed
	 * @param level The log level to include in the list. Note that this will not
	 *          change the log level on the packages itself, use the admin console
	 *          to do this. This filters only the messages that are below this
	 *          level
	 */
	public LogRealTimeViewerController(UserRequest ureq, WindowControl control, String loggingPackage, Level level, boolean withTitle) {
		super(ureq, control);
		this.level = level;
		this.loggingPackage = loggingPackage;
		appenderName = "LogRealTimeApppender-" + getIdentity().getKey();
		
		logViewerVC = createVelocityContainer("logviewer");
		logViewerVC.contextPut("loggingPackage", loggingPackage);

		// Add one second interval to update the log view every 5 seconds
		logViewerVC.contextPut("log", writer);
		logViewerVC.contextPut("withTitle", Boolean.valueOf(withTitle));
		
		// Add manual update link in case the automatic refresh does not work
		clearLink = LinkFactory.createButtonSmall("logviewer.link.clear", logViewerVC, this);
		stopLink = LinkFactory.createButtonSmall("logviewer.link.stop", logViewerVC, this);
		
		putInitialPanel(logViewerVC);
		updateConfiguration();
	}
	
	private void updateConfiguration() {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		LoggerConfig currentLoggerConfig = config.getLoggerConfig(loggingPackage);
		List<AppenderRef> currentRefs = currentLoggerConfig.getAppenderRefs();

		PatternLayout layout = PatternLayout.newBuilder()
				.withPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN)
				.build();

		WriterAppender appender = WriterAppender.newBuilder()
				.setLayout(layout)
				.setTarget(writer)
				.setName(appenderName)
				.build();
		appender.start();
		config.addAppender(appender);

		AppenderRef ref = AppenderRef.createAppenderRef("LogRealTime", null, null);
		List<AppenderRef> refList = new ArrayList<>();
		refList.addAll(currentRefs);
		refList.add(ref);
		
		currentLoggerConfig.addAppender(appender, level, null);
		ctx.updateLoggers();
	}

	@Override
	protected void doDispose() {
		removeConfiguration();
        super.doDispose();
	}
	
	private void removeConfiguration() {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		LoggerConfig currentLoggerConfig = config.getLoggerConfig(loggingPackage);
		currentLoggerConfig.removeAppender(appenderName);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == stopLink) {
			doStop();
		} else if (source == startLink) {
			doStart();
		} else if(source == clearLink) {
			doClear();
		}
	}
	
	private void doStart() {
		collectLog = true;
		logViewerVC.remove(startLink);
		stopLink = LinkFactory.createButtonSmall("logviewer.link.stop", logViewerVC, this);
	}
	
	private void doStop() {
		// update viewable links
		collectLog = false;
		logViewerVC.remove(stopLink);
		startLink = LinkFactory.createButtonSmall("logviewer.link.start", logViewerVC, this);
	}
	
	private void doClear() {
		writer.clear();
	}
	
	public class LogWriter extends Writer {

		private StringBuilder sb = new StringBuilder(100000);

		@Override
		public synchronized void write(char[] cbuf, int off, int len) throws IOException {
			if(collectLog && len > 0) {
				sb.append(cbuf, off, len);
				logViewerVC.setDirty(true);
			}
		}

		@Override
		public void flush() throws IOException {
			//
		}

		@Override
		public void close() throws IOException {
			//
		}
		
		public void clear() {
			sb = new StringBuilder(100000);
		}
		
		@Override
		public String toString() {
			String log;
			synchronized(this) {
				log = sb.toString();
			}
			// don't let the writer grow endlessly, reduce to half of size when larger than 100'000 characters (1.6MB)
			if (log.length() > 100000) {
				synchronized(this) {
					int nextLineBreakAfterHalfPos = sb.indexOf("\n", sb.length() / 2);
					String  cut = sb.substring(nextLineBreakAfterHalfPos);
					sb = new StringBuilder(cut);
				}
			}
			return Formatter.escWithBR(log).toString();
		}
	}
}
