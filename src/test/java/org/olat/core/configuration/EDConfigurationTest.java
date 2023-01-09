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
package org.olat.core.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 20 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EDConfigurationTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(EDConfigurationTest.class);

	@Test
	public void writeReadString() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		String key = "dfz46GH38723BGbg";
		String salt = "Something I want to say about something but cannot do without  salt.";
		Cipher cipher = PersistedProperties.newCipher(Cipher.ENCRYPT_MODE, key, salt);
		CipherOutputStream cout =  new CipherOutputStream(out, cipher);
		
		cout.write("Hello world".getBytes("8859_1"));
		cout.flush();
		cout.close();
		
		String encrypted = out.toString("8859_1");
		
		ByteArrayInputStream in = new ByteArrayInputStream(encrypted.getBytes("8859_1"));
		Cipher rcipher = PersistedProperties.newCipher(Cipher.DECRYPT_MODE, key, salt);
				
	
		CipherInputStream cin =  new CipherInputStream(in, rcipher);
		String decrypt = IOUtils.toString(cin, "8859_1");
		Assert.assertEquals("Hello world", decrypt);
	}
	
	@Test
	public void writeReadProperties() {
		String salt = "A grain of salt like others.";
		String filename = "prop-" + CodeHelper.getForeverUniqueID();
		PersistedProperties properties = new PersistedProperties(CoordinatorManager.getInstance(),
				new EDListener(), filename, true, salt);
		properties.init();
		
		String identifier = UUID.randomUUID().toString();
		properties.setStringProperty("an-id", identifier, true);
		Assert.assertEquals(identifier, properties.getStringPropertyValue("an-id", true));
		
		// Read the secured property files a second time
		PersistedProperties reloadProperties = new PersistedProperties(CoordinatorManager.getInstance(),
				new EDListener(), filename, true, salt);
		reloadProperties.init();
		Assert.assertEquals(identifier, reloadProperties.getStringPropertyValue("an-id", true));
	}
	
	@Test
	public void readProperties171() {
		String filename = "prop-171-references";
		File configurationFile = Paths.get(WebappHelper.getUserDataRoot(), "system", "configuration", filename + ".properties").toFile();
		try(InputStream in = EDConfigurationTest.class.getResourceAsStream("prop-171-references")) {
			FileUtils.copyToFile(in, configurationFile, "");
		} catch(IOException e) {
			log.error("", e);
		}
		
		String salt = "A grain of mineral salt from Himalaya.";
		PersistedProperties properties = new PersistedProperties(CoordinatorManager.getInstance(),
				new EDListener(), filename, true, salt);
		properties.init();
		
		String value = "my-securely-saved-value";
		Assert.assertEquals(value, properties.getStringPropertyValue("a-saved-property", true));
	}
	
	private static class EDListener implements GenericEventListener {
		@Override
		public void event(Event event) {
			//
		}
	}
}
