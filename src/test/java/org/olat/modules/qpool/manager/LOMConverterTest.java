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
package org.olat.modules.qpool.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.modules.qpool.model.LOMDuration;
import org.olat.modules.qpool.model.QuestionItemImpl;

/**
 * 
 * Initial date: 11.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LOMConverterTest {
	
	@Test
	public void itemToLom()
	throws IOException, URISyntaxException, JAXBException {
		QuestionItemImpl item = new QuestionItemImpl();
		item.setTitle("Psychologie");
		item.setDescription("Description psychologique");
		item.setLanguage("fr");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new LOMConverter().toLom(item, out);
		out.close();
		System.out.println("LOM: " + new String(out.toByteArray()));
	}
	
	
	@Test
	public void convertDuration_toString()
	throws IOException, URISyntaxException, JAXBException {
		//1h 30m
		String duration1 = LOMConverter.convertDuration(0, 1, 30, 0);
		Assert.assertEquals("PT1H30M", duration1);
		//1m 45s
		String duration2 = LOMConverter.convertDuration(0, 0, 1, 45);
		Assert.assertEquals("PT1M45S", duration2);
	}
	
	@Test
	public void convertDuration_toDuration()
	throws IOException, URISyntaxException, JAXBException {
		//1h 30m
		LOMDuration duration1 = LOMConverter.convertDuration("PT1H30M");
		Assert.assertEquals(0, duration1.getYear());
		Assert.assertEquals(0, duration1.getMonth());
		Assert.assertEquals(0, duration1.getDay());
		Assert.assertEquals(1, duration1.getHour());
		Assert.assertEquals(30, duration1.getMinute());
		Assert.assertEquals(0, duration1.getSeconds());
		
		//1m 45s
		LOMDuration duration2 = LOMConverter.convertDuration("PT1M45S");
		Assert.assertEquals(0, duration2.getYear());
		Assert.assertEquals(0, duration2.getMonth());
		Assert.assertEquals(0, duration2.getDay());
		Assert.assertEquals(0, duration2.getHour());
		Assert.assertEquals(1, duration2.getMinute());
		Assert.assertEquals(45, duration2.getSeconds());

		//2y 3 month and 4h 1minute 35s
		LOMDuration duration3 = LOMConverter.convertDuration("P2Y3MT4H1M35S");
		Assert.assertEquals(2, duration3.getYear());
		Assert.assertEquals(3, duration3.getMonth());
		Assert.assertEquals(0, duration3.getDay());
		Assert.assertEquals(4, duration3.getHour());
		Assert.assertEquals(1, duration3.getMinute());
		Assert.assertEquals(35, duration3.getSeconds());
	}
}