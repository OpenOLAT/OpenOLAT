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
package org.olat.core.commons.services.csp;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.csp.model.CSPReport;

/**
 * 
 * Initial date: 11 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSPDispatcherTest {

	@Test
	public void readSafariReport() throws Exception {
		String content = "{\"csp-report\":{\"document-uri\":\"https://www.frentix.com/auth/RepositoryEntry/2816114688/CourseNode/105408669705825\",\"referrer\":\"https://www.frentix.com/auth/RepositoryEntry/2816114688/CourseNode/105408669705825\",\"violated-directive\":\"frame-src 'self' https://player.vimeo.com https://youtu.be https://www.youtube.com https://s.ytimg.com https://onlyoffice.openolat.org https://edusharing.example.org https://vms3.vitero.de https://www.paypal.com https://www.sandbox.paypal.com\",\"effective-directive\":\"frame-src\",\"original-policy\":\"report-uri  /csp/;report-to  /csp/;default-src  'self';connect-src 'self' https://youtu.be https://www.youtube.com https://edusharing.example.org https://www.paypal.com https://www.sandbox.paypal.com;script-src 'unsafe-inline' 'unsafe-eval' 'self' https://player.vimeo.com https://www.youtube.com https://s.ytimg.com https://mathjax.openolat.org https://onlyoffice.openolat.org https://edusharing.example.org https://www.paypal.com https://www.sandbox.paypal.com;style-src  'unsafe-inline' 'self';img-src 'self' data: https://edusharing.example.org;font-src 'self' data: https://edusharing.example.org;worker-src  'self' blob:;frame-src 'self' https://player.vimeo.com https://youtu.be https://www.youtube.com https://s.ytimg.com https://onlyoffice.openolat.org https://edusharing.example.org https://vms3.vitero.de https://www.paypal.com https://www.sandbox.paypal.com;media-src 'self' blob: https://player.vimeo.com https://youtu.be https://www.youtube.com https://edusharing.example.org https://openmeetings.frentix.com;object-src  'self';\",\"blocked-uri\":\"https://blog.openolat.ch\",\"status-code\":0}}";

		CSPReport report = CSPDispatcher.readReport(new ByteArrayInputStream(content.getBytes()));

		Assert.assertNotNull(report);
		Assert.assertEquals("https://www.frentix.com/auth/RepositoryEntry/2816114688/CourseNode/105408669705825", report.getDocumentUri());
		Assert.assertEquals("0", report.getStatusCode());
	}
	
	@Test
	public void readChromeReport() {
		String content = "{\"csp-report\":{\"document-uri\":\"https://www.frentix.com/auth/RepositoryEntry/2816114688/CourseNode/105408669705825\",\"referrer\":\"https://www.frentix.com/auth/RepositoryEntry/2816114688/CourseNode/105408669705825\",\"violated-directive\":\"frame-src\",\"effective-directive\":\"frame-src\",\"original-policy\":\"report-uri  /csp/;default-src  'self';connect-src 'self' https://youtu.be https://www.youtube.com https://edusharing.example.org https://www.paypal.com https://www.sandbox.paypal.com;script-src 'unsafe-inline' 'unsafe-eval' 'self' https://player.vimeo.com https://www.youtube.com https://s.ytimg.com https://mathjax.openolat.org https://onlyoffice.openolat.org https://edusharing.example.org https://www.paypal.com https://www.sandbox.paypal.com;style-src  'unsafe-inline' 'self';img-src 'self' data: https://edusharing.example.org;font-src 'self' data: https://edusharing.example.org;worker-src  'self' blob:;frame-src 'self' https://player.vimeo.com https://youtu.be https://www.youtube.com https://s.ytimg.com https://onlyoffice.openolat.org https://edusharing.example.org https://vms3.vitero.de https://www.paypal.com https://www.sandbox.paypal.com;media-src 'self' blob: https://player.vimeo.com https://youtu.be https://www.youtube.com https://edusharing.example.org https://openmeetings.frentix.com;object-src  'self';\",\"disposition\":\"report\",\"blocked-uri\":\"https://blog.openolat.ch\",\"line-number\":595,\"source-file\":\"https://www.frentix.com/auth/RepositoryEntry/2816114688/CourseNode/105408669705825\",\"status-code\":200,\"script-sample\":\"\"}}";
		
		CSPReport report = CSPDispatcher.readReport(new ByteArrayInputStream(content.getBytes()));

		Assert.assertNotNull(report);
		Assert.assertEquals("https://www.frentix.com/auth/RepositoryEntry/2816114688/CourseNode/105408669705825", report.getDocumentUri());
		Assert.assertEquals("200", report.getStatusCode());
		Assert.assertEquals("595", report.getLineNumber());
	}

}
