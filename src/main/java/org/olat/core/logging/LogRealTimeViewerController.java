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
import java.io.StringWriter;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.jobs.NoOpJob;

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
public class LogRealTimeViewerController extends BasicController implements JobListener {
	private static final String LOG_DISPLAYER_GROUP = "LogDisplayer_Group";
	private static final Pattern logNoiseReducePattern = Pattern.compile("(.*) \\[.*%\\^(.*) .*%\\^(.*)");
	private VelocityContainer logViewerVC;
	private Logger log4JLogger;
	private WriterAppender writerAppender;
	private StringWriter writer;
	private JobDetail jobDetail;
	private String jobName;
	private Link updateLink, startLink, stopLink;
	private boolean removeLogNoise;

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
	 * @param removeLogNoise true: remove a lot of brasato specific stuff that is
	 *          normally not interesting; false: show log entries as they are
	 *          logged. Example if set to true: <br>
	 *          <code>2008-08-22 11:20:40 [QuartzScheduler_Worker-1] INFO  LDAPUserSynchronizerJob  - OLAT::INFO ^%^ I441 ^%^ org.olat.ldap ^%^ n/a ^%^ n/a ^%^ n/a ^%^ n/a ^%^ n/a ^%^ Starting LDAP user synchronize job</code>
	 *          <br>will become<br>
	 *          <code>2008-08-22 11:20:40 - n/a - Starting LDAP user synchronize job</code>
	 */
	public LogRealTimeViewerController(UserRequest ureq, WindowControl control, String loggingPackage, Level level, boolean removeLogNoise) {
		super(ureq, control);
		this.removeLogNoise = removeLogNoise;
		logViewerVC = createVelocityContainer("logviewer");
		logViewerVC.contextPut("loggingPackage", loggingPackage);
		// Create logger for requested package and add a string writer appender
		log4JLogger = Logger.getLogger(loggingPackage);
		writer = new StringWriter();
		Layout layout = new PatternLayout("%d{HH:mm:ss} %-5p [%t]: %m%n");
		writerAppender = new WriterAppender(layout, writer);
		writerAppender.setThreshold(level);
		log4JLogger.addAppender(writerAppender);
		updateLogViewFromWriter();
		// Add job to read from the string writer every second
		try {
			jobName = "Log_Displayer_Job_" + this.hashCode();
			jobDetail = new JobDetail(jobName, LOG_DISPLAYER_GROUP, NoOpJob.class);
			jobDetail.addJobListener(jobName);
			CronTrigger trigger = new CronTrigger();
			trigger.setName(jobName);
			trigger.setGroup(LOG_DISPLAYER_GROUP);
			trigger.setCronExpression("* * * * * ?");
			// Schedule job now
			Scheduler scheduler = (Scheduler) CoreSpringFactory.getBean("schedulerFactoryBean");
			scheduler.addJobListener(this);
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (ParseException e) {
			logError("Can not parse log viewer cron expression", e);
		} catch (SchedulerException e) {
			logError("Problem when creating log viewer scheduler", e);
		}
		// Add one second interval to update the log view every second
		JSAndCSSComponent jsc = new JSAndCSSComponent("intervall", this.getClass(), 3000);
		jsc.requireFullPageRefresh(); // interval not working otherwise
		logViewerVC.put("updatecontrol", jsc);
		// Add manual update link in case the automatic refresh does not work
		updateLink = LinkFactory.createButtonSmall("logviewer.link.update", logViewerVC, this);
		stopLink = LinkFactory.createButtonSmall("logviewer.link.stop", logViewerVC, this);
		
		putInitialPanel(logViewerVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		
		if (logViewerVC != null) { // don't clean up twice
			Scheduler scheduler = (Scheduler) CoreSpringFactory.getBean("schedulerFactoryBean");
			// remove scheduler job first
			try {
				scheduler.deleteJob(jobName, LOG_DISPLAYER_GROUP);
				scheduler.removeJobListener(jobName);
			} catch (SchedulerException e) {
				logError("Can not delete log viewer job", e);
			}
			// remove logger appender and release StringWriter
			log4JLogger.removeAppender(writerAppender);
			log4JLogger = null;
			writerAppender.close();
			writerAppender = null;
			try {
				writer.close();
			} catch (IOException e) {
				logError("Error while closing log viewer string writer", e);
			}
			synchronized(this) {
				writer = null;
				updateLink = null;
				logViewerVC = null;
			}
		}
	}

	//cluster_OK (it only snyc the disposed for logViewerVC)
	private synchronized void updateLogViewFromWriter() {
		if(logViewerVC == null) return;
		
		StringBuffer sb = writer.getBuffer();
		String log = sb.toString();
		if (removeLogNoise) {
			Matcher m = logNoiseReducePattern.matcher(log);
			log = m.replaceAll("$1 - $2 - $3");
		}
		logViewerVC.contextPut("log", Formatter.escWithBR(log));
		// don't let the writer grow endlessly, reduce to half of size when larger than 100'000 characters (1.6MB)
		if (sb.length() > 100000) {
			int nextLineBreakAfterHalfPos = sb.indexOf("\n", sb.length() / 2);
			sb.delete(0, nextLineBreakAfterHalfPos);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == updateLink) {
			updateLogViewFromWriter();
		}
		if (source == stopLink) {
			// update viewable links
			logViewerVC.remove(stopLink);
			logViewerVC.remove(updateLink);
			startLink = LinkFactory.createButtonSmall("logviewer.link.start", logViewerVC, this);
			// remove logger appender
			log4JLogger.removeAppender(writerAppender);
			// pause log update trigger job
			try {
				Scheduler scheduler = (Scheduler) CoreSpringFactory.getBean("schedulerFactoryBean");
				scheduler.pauseJob(jobName, LOG_DISPLAYER_GROUP);
			} catch (SchedulerException e) {
				logError("Can not pause log viewer job", e);
			}
		}
		if (source == startLink) {
			// update viewable links
			logViewerVC.remove(startLink);
			updateLink = LinkFactory.createButtonSmall("logviewer.link.update", logViewerVC, this);
			stopLink = LinkFactory.createButtonSmall("logviewer.link.stop", logViewerVC, this);
			// re-add appender to logger
			log4JLogger.addAppender(writerAppender);
			// resume trigger job
			try {
				Scheduler scheduler = (Scheduler) CoreSpringFactory.getBean("schedulerFactoryBean");
				scheduler.resumeJob(jobName, LOG_DISPLAYER_GROUP);
			} catch (SchedulerException e) {
				logError("Can not resume log viewer job", e);
			}
		}		
	}

	/**
	 * @see org.quartz.JobListener#getName()
	 */
	public String getName() {
		return jobName;
	}

	/**
	 * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
	 */
	@Override
	public void jobExecutionVetoed(JobExecutionContext arg0) {
	// nothing to do, see jobWasExecuted()
	}

	/**
	 * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
	 */
	@Override
	public void jobToBeExecuted(JobExecutionContext arg0) {
	// nothing to do, see jobWasExecuted()
	}

	/**
	 * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext,
	 *      org.quartz.JobExecutionException)
	 */
	@Override
	public void jobWasExecuted(JobExecutionContext arg0, JobExecutionException arg1) {
		updateLogViewFromWriter();
	}
}
