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
package org.olat.course.certificate.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateWorkUnit;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 6 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificateWorkUnitXStreamTest extends OlatTestCase {
	
	@Test
	public void serializationRoundTrip() {
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(true)
				.withCertificationProgramMailType(CertificationProgramMailType.certificate_issued)
				.build();
		
		CertificateWorkUnit workUnit = new CertificateWorkUnit();
		workUnit.setTemplateKey(312l);
		workUnit.setPrintTemplate(true);
		workUnit.setPrintTemplateKey(1024l);
		workUnit.setScore(64f);
		workUnit.setMaxScore(127f);
		workUnit.setPassed(true);
		workUnit.setCompletion(100d);
		workUnit.setConfig(config);
		workUnit.setGrade("A");
		workUnit.setDoerKey(100l);
		
		String xml = CertificateWorkUnitXStream.toXml(workUnit);
		
		CertificateWorkUnit deserializedUnit = CertificateWorkUnitXStream.fromXml(xml);
		Assert.assertNotNull(deserializedUnit);
		Assert.assertEquals(Long.valueOf(312l), deserializedUnit.getTemplateKey());
		Assert.assertEquals(Long.valueOf(1024l), deserializedUnit.getPrintTemplateKey());
		Assert.assertEquals(64.0f, deserializedUnit.getScore().floatValue(), 0.0001);
		Assert.assertEquals(127.0f, deserializedUnit.getMaxScore().floatValue(), 0.0001);
		Assert.assertEquals(100.0d, deserializedUnit.getCompletion().floatValue(), 0.0001);
		Assert.assertEquals("A", deserializedUnit.getGrade());
		
		CertificateConfig deserializedConfig = deserializedUnit.getConfig();
		Assert.assertNotNull(deserializedConfig);
		Assert.assertTrue(deserializedConfig.isSendEmail());
		Assert.assertEquals(CertificationProgramMailType.certificate_issued, deserializedConfig.getMailType());
	}
}
