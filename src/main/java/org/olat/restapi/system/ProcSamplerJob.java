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
package org.olat.restapi.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.restapi.system.MonitoringService.Statistics;
import org.olat.restapi.system.vo.SessionsVO;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * Initial date: 27 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProcSamplerJob extends QuartzJobBean {

	private static final OLog log = Tracing.createLoggerFor(ProcSamplerJob.class);
	private final Random random = new Random();

	@Override
	protected void executeInternal(JobExecutionContext context)  {
		MonitoringModule monitoringModule = CoreSpringFactory.getImpl(MonitoringModule.class);
		if(StringHelper.containsNonWhitespace(monitoringModule.getProcFile())) {
			File xmlFile = new File(monitoringModule.getProcFile());
			if(!xmlFile.exists()) {
				File parent = xmlFile.getParentFile();
				if(!parent.exists() || !parent.canWrite()) {
					return;
				}
			} else if(!xmlFile.canWrite()) {
				return;
			}
			jitter();
			writeProcFile(xmlFile);
		}
	}
	
	private void jitter() {
		try {
			double millis = random.nextDouble() * 6000.0d;
			long wait = Math.round(millis);
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			log.error("", e);
		}
	}
	
	public void writeProcFile(File xmlFile) {
		try {
			Statistics statistics = CoreSpringFactory.getImpl(MonitoringService.class).getStatistics();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc;
			if(xmlFile.exists()) {
				doc = dBuilder.parse(xmlFile);
			} else {
				doc = dBuilder.newDocument();
				doc.appendChild(doc.createElement("root"));
			}
			
			Element rootEl = doc.getDocumentElement();
			//sessions
			SessionsVO sessionsVo = statistics.getSessionsVo();
			addValue("secureAuthenticatedCount", sessionsVo.getSecureAuthenticatedCount(), rootEl, doc);
			addValue("secureRestCount", sessionsVo.getSecureRestCount(), rootEl, doc);
			addValue("secureWebdavCount", sessionsVo.getSecureWebdavCount(), rootEl, doc);
			//clicks
			addValue("authenticatedClickCountLastFiveMinutes", sessionsVo.getAuthenticatedClickCountLastFiveMinutes(), rootEl, doc);
			addValue("concurrentDispatchThreads", sessionsVo.getConcurrentDispatchThreads(), rootEl, doc);
			addValue("requestLastFiveMinutes", sessionsVo.getRequestLastFiveMinutes(), rootEl, doc);
			addValue("requestLastMinute", sessionsVo.getRequestLastMinute(), rootEl, doc);
			//openolat
			addValue("activeUserCount", statistics.getActiveUserCount(), rootEl, doc);
			addValue("totalGroupCount", statistics.getTotalGroupCount(), rootEl, doc);
			addValue("publishedCourses", statistics.getPublishedCourses(), rootEl, doc);
			//indexer
			addValue("lastFullIndexTime", statistics.getLastFullIndexTime(), rootEl, doc);
			//marker
			addValue("lastOpenOLATSampling", new Date().toString(), rootEl, doc);

			// Use a Transformer for output
			try(OutputStream out = new FileOutputStream(xmlFile)) {
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
				transformer.transform(new DOMSource(doc), new StreamResult(out));
			} catch(IOException e) {
				log.error("", e);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void addValue(String name, long value, Element rootEl, Document doc) {
		addValue(name, Long.toString(value), rootEl, doc);
	}
	
	private void addValue(String name, String value, Element rootEl, Document doc) {
		NodeList currentEls = rootEl.getElementsByTagName(name);
		if(currentEls.getLength() == 0) {
			Element element = doc.createElement(name);
			element.setAttribute("value", value);
			rootEl.appendChild(element);
		} else {
			Element element = (Element)currentEls.item(0);
			element.setAttribute("value", value);
		}
	}
}