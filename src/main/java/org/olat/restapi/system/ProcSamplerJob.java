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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.restapi.system.MonitoringService.Statistics;
import org.olat.restapi.system.vo.SessionsVO;
import org.olat.search.SearchServiceStatus;
import org.olat.search.service.SearchServiceFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import net.fortuna.ical4j.util.TimeZones;

/**
 * 
 * Initial date: 27 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class ProcSamplerJob extends QuartzJobBean {

	private static final Logger log = Tracing.createLoggerFor(ProcSamplerJob.class);
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
			Document doc = loadDocument(xmlFile);
			if(doc == null) return;
			
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
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			format.setTimeZone(TimeZones.getUtcTimeZone());
			SearchServiceStatus status = SearchServiceFactory.getService().getStatus();

			String date = format.format(status.getLastFullIndexTime());
			addValue("lastFullIndexTime", date, rootEl, doc);

			//marker
			addValue("lastOpenOLATSampling", format.format(new Date()), rootEl, doc);

			writeDocument(xmlFile, doc);
		} catch(Exception e) {
			log.error("", e);
			
		}
	}
	
	private void writeDocument(File xmlFile, Document doc) {
		// Use a Transformer for output
		try(OutputStream out = new FileOutputStream(xmlFile)) {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(doc), new StreamResult(out));
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private Document loadDocument(File xmlFile) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = null;
			if(xmlFile.exists()) {
				try {
					doc = dBuilder.parse(xmlFile);
				} catch (SAXParseException e) {
					log.error("", e);
				}
			}
			
			if(doc == null) {
				doc = dBuilder.newDocument();
				doc.appendChild(doc.createElement("root"));
			}
			return doc;
		} catch(Exception e) {
			log.error("", e);
			return null;
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