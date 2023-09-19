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
package org.olat.modules.openbadges.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;

import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesBakeContext;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NamedNodeMap;

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
public class OpenBadgesManagerImplTest extends OlatTestCase {

	@Autowired
	OpenBadgesManager openBadgesManager;

	@Autowired
	BadgeAssertionDAO badgeAssertionDAO;

	@Autowired
	BadgeClassDAO badgeClassDAO;

	@After
	public void tearDown() throws Exception {
		List<BadgeAssertion> globalBadgeAssertions = badgeAssertionDAO.getBadgeAssertions(null, null, true);
		for (BadgeAssertion globalBadgeAssertion : globalBadgeAssertions) {
			badgeAssertionDAO.deleteBadgeAssertion(globalBadgeAssertion);
		}

		List<BadgeClass> globalBadgeClasses = badgeClassDAO.getBadgeClasses(null, false);
		for (BadgeClass globalBadgeClass : globalBadgeClasses) {
			badgeClassDAO.deleteBadgeClass(globalBadgeClass);
		}
	}

	@Test
	public void testBakePng() throws IOException {

		// Arrange

		String uuid = OpenBadgesUIFactory.createIdentifier();
		BadgeAssertion badgeAssertion = createBadgeAssertion(uuid);
		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		String jsonString = OpenBadgesManagerImpl.createBakedJsonString(badgeAssertion);
		String tmpFileName = uuid + ".png";

		InputStream inputStream = OpenBadgesManagerImplTest.class.getResourceAsStream("test_badge.png");
		IIOImage iioImage = OpenBadgesManagerImpl.readIIOImage(inputStream);
		IIOMetadata metadata = iioImage.getMetadata();

		// Act

		OpenBadgesManagerImpl.addNativePngTextEntry(metadata, "openbadges", jsonString);
		IIOImage bakedImage = new IIOImage(iioImage.getRenderedImage(), null, metadata);
		File tmpFile = new File(WebappHelper.getTmpDir(), tmpFileName);
		FileOutputStream fileOutputStream = new FileOutputStream(tmpFile, false);
		OpenBadgesManagerImpl.writeImageIOImage(bakedImage, fileOutputStream);

		// Assert

		InputStream bakedInputStream = new FileInputStream(tmpFile);
		NamedNodeMap attributes = OpenBadgesManagerImpl.extractAssertionJsonStringFromPng(bakedInputStream);

		Assert.assertNotNull(attributes);

		OpenBadgesBakeContext bakeContext = new OpenBadgesBakeContext(attributes);
		Assertion assertion = bakeContext.getTextAsAssertion();
		Badge badge = assertion.getBadge();

		Assert.assertNotNull(assertion);
		Assert.assertTrue(assertion.getId().endsWith(uuid));
		Assert.assertEquals(badgeClass.getSalt(), assertion.getRecipient().getSalt());
		Assert.assertTrue(badge.getImage().endsWith(badgeClass.getUuid()));
	}

	private BadgeAssertion createBadgeAssertion(String uuid) {
		BadgeClassImpl badgeClassImpl = BadgeTestData.createTestBadgeClass("PNG badge", "image.png", null);
		badgeClassDAO.createBadgeClass(badgeClassImpl);

		Identity recipient = JunitTestHelper.createAndPersistIdentityAsUser("badgeRecipient");
		String recipientObject = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeClassImpl.getSalt());
		String verification = "{\"type\":\"hosted\"}";
		Date issuedOn = new Date();

		BadgeAssertion badgeAssertion = badgeAssertionDAO.createBadgeAssertion(uuid, recipientObject, badgeClassImpl,
				verification, issuedOn, recipient, null);

		return badgeAssertion;
	}

	@Test
	public void testInsertingJsonIntoSvg() {
		String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
				"<svg width=\"100%\" height=\"100%\" viewBox=\"0 0 200 201\" version=\"1.1\"\n" +
				"     xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"\n" +
				"     xmlns:serif=\"http://www.serif.com/\"\n" +
				"     style=\"fill-rule:evenodd;clip-rule:evenodd;stroke-linecap:round;stroke-linejoin:round;stroke-miterlimit:1.5;\">\n" +
				"    <g transform=\"matrix(1,0,0,1,-651,-428)\">\n" +
				"    </g>\n" +
				"</svg>";

		String json = "{\n" +
				" \"@context\": \"https://w3id.org/openbadges/v2\",\n" +
				" \"id\": \"https://example.org/assertions/123\",\n" +
				" \"type\": \"Assertion\"\n" +
				"}";

		if (openBadgesManager instanceof OpenBadgesManagerImpl managerImpl) {
			String mergedSvg = managerImpl.mergeAssertionJson(svg, json, "https://test.openolat.org/badge/assertion/123");
			System.err.println(mergedSvg);
		}
	}
}