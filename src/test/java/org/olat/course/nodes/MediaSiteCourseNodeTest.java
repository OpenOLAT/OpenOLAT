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
package org.olat.course.nodes;

import org.junit.Assert;
import org.junit.Test;
import org.olat.course.editor.StatusDescription;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.LtiVersion;
import org.olat.test.OlatTestCase;

/**
 * Regression test for the OO-9717 "Configuration errors are not detected in the course editor"
 * bullet: a course-local (private login) LTI 1.3 MediaSite element that was saved before its LTI
 * 1.3 connection was fully configured must show an editor error, not fail only at runtime.
 *
 * Initial date: 2026-09-01<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MediaSiteCourseNodeTest extends OlatTestCase {

	@Test
	public void isConfigValid_lti13PrivateLogin_missingToolKey() {
		MediaSiteCourseNode node = new MediaSiteCourseNode();
		ModuleConfiguration config = node.getModuleConfiguration();
		config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, true);
		config.setStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID, "some-module-id");
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI_VERSION, LtiVersion.lti_1_3.name());
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI13_BASE_URL, "https://mediasite.example.com/%s");
		// CONFIG_LTI13_TOOL_KEY intentionally left unset

		StatusDescription status = node.isConfigValid();

		Assert.assertTrue(status.isError());
	}

	@Test
	public void isConfigValid_lti13PrivateLogin_missingBaseUrl() {
		MediaSiteCourseNode node = new MediaSiteCourseNode();
		ModuleConfiguration config = node.getModuleConfiguration();
		config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, true);
		config.setStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID, "some-module-id");
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI_VERSION, LtiVersion.lti_1_3.name());
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI13_TOOL_KEY, "123");
		// CONFIG_LTI13_BASE_URL intentionally left unset

		StatusDescription status = node.isConfigValid();

		Assert.assertTrue(status.isError());
	}

	@Test
	public void isConfigValid_lti13PrivateLogin_complete() {
		MediaSiteCourseNode node = new MediaSiteCourseNode();
		ModuleConfiguration config = node.getModuleConfiguration();
		config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, true);
		config.setStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID, "some-module-id");
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI_VERSION, LtiVersion.lti_1_3.name());
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI13_TOOL_KEY, "123");
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI13_BASE_URL, "https://mediasite.example.com/%s");

		StatusDescription status = node.isConfigValid();

		Assert.assertEquals(StatusDescription.NOERROR, status);
	}

	@Test
	public void isConfigValid_lti11PrivateLogin_ignoresLti13Keys() {
		MediaSiteCourseNode node = new MediaSiteCourseNode();
		ModuleConfiguration config = node.getModuleConfiguration();
		config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, true);
		config.setStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID, "some-module-id");
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI_VERSION, LtiVersion.lti_1_1.name());
		// No LTI 1.3 keys at all - must not trigger the new LTI 1.3 check for an LTI 1.1 node

		StatusDescription status = node.isConfigValid();

		Assert.assertEquals(StatusDescription.NOERROR, status);
	}
}
