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

import org.junit.Test;
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
}
