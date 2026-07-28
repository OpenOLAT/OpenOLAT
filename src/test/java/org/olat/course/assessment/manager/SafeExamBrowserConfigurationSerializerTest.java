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
package org.olat.course.assessment.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.Encoder;
import org.olat.core.util.FileUtils;
import org.olat.core.util.xml.PList;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Initial date: 22 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserConfigurationSerializerTest {

	/**
	 * Load an unencrypted SEB config, file and calculate the Hash Key
	 * 
	 * @throws IOException
	 */
	@Test
	public void fromPListToHashKey() throws IOException {
		final String referenceHash = "3cd36cce3565525eb575456a5d49bf938197cf3914bcbebb7aa5150cef358672";
		
		InputStream in = SafeExamBrowserConfigurationSerializerTest.class.getResourceAsStream("OpenOlatSebPList.seb");
		String plistText = FileUtils.load(in, "UTF-8");
		in.close();
		
		PList plist = PList.valueOf(plistText);
		Assert.assertNotNull(plist);
		
		String jsonText = SafeExamBrowserConfigurationSerializer.toJSON(plist);
		String safeExamBrowserKey = Encoder.sha256Exam(jsonText);

		Assert.assertEquals(referenceHash, safeExamBrowserKey);
	}
	
	@Test
	public void fromPListToSEBValidation() {
		InputStream in = SafeExamBrowserConfigurationSerializerTest.class.getResourceAsStream("SEBClientSettingsPListConfig.seb");
		String plistText = FileUtils.load(in, "UTF-8");
		PList plist = PList.valueOf(plistText);
		Assert.assertNotNull(plist);
		
		String jsonText = SafeExamBrowserConfigurationSerializer.toJSON(plist);
		String safeExamBrowserKey = Encoder.sha256Exam(jsonText);

		String url = "http://kivik.frentix.com";
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("kivik.frentix.com");
		request.setScheme("http");
		request.addHeader("x-safeexambrowser-configkeyhash", hash);
		request.setRequestURI("");
		
		boolean allowed = SafeExamBrowserValidator.isSafelyAllowed(request, null, safeExamBrowserKey);
		Assert.assertTrue(allowed);
	}

	@Test
	public void toJSON() {
		InputStream in = SafeExamBrowserConfigurationSerializerTest.class.getResourceAsStream("SebClientClearPList.seb");
		String plistText = FileUtils.load(in, "UTF-8");
		PList plist = PList.valueOf(plistText);
		Assert.assertNotNull(plist);

		String json = SafeExamBrowserConfigurationSerializer.toJSON(plist);
		Assert.assertNotNull(json);

		// The result is a valid JSON object
		JsonObject object = JsonParser.parseString(json).getAsJsonObject();

		// A few values converted to their JSON type
		Assert.assertEquals("https://kivik.frentix.com", object.get("startURL").getAsString());
		Assert.assertTrue(object.get("allowQuit").getAsBoolean());
		Assert.assertFalse(object.get("allowWlan").getAsBoolean());
		Assert.assertEquals(40, object.get("taskBarHeight").getAsInt());
		Assert.assertEquals(0.1d, object.get("batteryChargeThresholdCritical").getAsDouble(), 0.0001d);

		// Keys are ordered alphabetically, case-insensitively, in the root
		// dictionary as well as in nested dictionaries
		assertKeysSorted(object);
	}

	private void assertKeysSorted(JsonObject object) {
		List<String> keys = new ArrayList<>(object.keySet());
		List<String> sorted = new ArrayList<>(keys);
		sorted.sort(String.CASE_INSENSITIVE_ORDER);
		Assert.assertEquals(sorted, keys);

		for(String key:keys) {
			JsonElement value = object.get(key);
			if(value.isJsonObject()) {
				assertKeysSorted(value.getAsJsonObject());
			} else if(value.isJsonArray()) {
				JsonArray array = value.getAsJsonArray();
				for(JsonElement element:array) {
					if(element.isJsonObject()) {
						assertKeysSorted(element.getAsJsonObject());
					}
				}
			}
		}
	}
}
