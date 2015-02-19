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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.openmeetings.manager.OpenMeetingsLanguages;


/**
 * 
 * Initial date: 07.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsTest {
	
	@Test
	public void testLanguagesMapping() {
		
		OpenMeetingsLanguages languages = new OpenMeetingsLanguages();
		languages.read();
		
		int id = languages.getLanguageId(Locale.ENGLISH);
		Assert.assertEquals(1, id);
		
		int idFr = languages.getLanguageId(Locale.FRENCH);
		Assert.assertEquals(4, idFr);
		
		int idFrCh = languages.getLanguageId(new Locale("fr", "CH", ""));
		Assert.assertEquals(4, idFrCh);
	}
}
