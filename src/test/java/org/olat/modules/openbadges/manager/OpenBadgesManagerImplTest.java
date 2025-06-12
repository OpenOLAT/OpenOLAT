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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.BadgeVerification;
import org.olat.modules.openbadges.OpenBadgesBakeContext;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.OpenBadgesModule;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.criteria.CourseElementPassedCondition;
import org.olat.modules.openbadges.criteria.CoursePassedCondition;
import org.olat.modules.openbadges.criteria.CoursesPassedCondition;
import org.olat.modules.openbadges.criteria.GlobalBadgesEarnedCondition;
import org.olat.modules.openbadges.criteria.OtherBadgeEarnedCondition;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	OpenBadgesModule openBadgesModule;

	@Autowired
	OpenBadgesManager openBadgesManager;

	@Autowired
	BadgeAssertionDAO badgeAssertionDAO;

	@Autowired
	BadgeClassDAO badgeClassDAO;

	@Autowired
	BadgeEntryConfigurationDAO badgeEntryConfigurationDAO;
	
	@Autowired
	DB dbInstance;
	
	@Autowired
	AssessmentEntryDAO assessmentEntryDAO;
	
	@Autowired
	GroupDAO groupDAO;

	@Autowired
	RepositoryEntryRelationDAO repositoryEntryRelationDAO;

	@After
	public void tearDown() throws Exception {
		List<BadgeAssertion> globalBadgeAssertions = badgeAssertionDAO.getBadgeAssertions(null, null, true);
		for (BadgeAssertion globalBadgeAssertion : globalBadgeAssertions) {
			badgeAssertionDAO.deleteBadgeAssertion(globalBadgeAssertion);
		}

		List<BadgeClass> globalBadgeClasses = badgeClassDAO.getBadgeClasses(null, false, false, true);
		for (BadgeClass globalBadgeClass : globalBadgeClasses) {
			badgeClassDAO.deleteBadgeClass(globalBadgeClass);
		}
	}

	@Test
	public void bakePngHosted() throws IOException, JOSEException {
		BadgeVerification badgeVerification = openBadgesModule.getVerification();
		openBadgesModule.setVerification(BadgeVerification.hosted);

		// Arrange

		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-assertion-doer");

		String uuid = OpenBadgesFactory.createIdentifier();
		BadgeAssertion badgeAssertion = createBadgeAssertion(uuid);
		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		String textValue = ((OpenBadgesManagerImpl) openBadgesManager).createPngText(badgeAssertion, doer);
		String tmpFileName = uuid + ".png";

		InputStream inputStream = OpenBadgesManagerImplTest.class.getResourceAsStream("test_badge.png");
		IIOImage iioImage = OpenBadgesManagerImpl.readIIOImage(inputStream);
		IIOMetadata metadata = iioImage.getMetadata();

		// Act

		OpenBadgesManagerImpl.addNativePngTextEntry(metadata, "openbadges", textValue);
		IIOImage bakedImage = new IIOImage(iioImage.getRenderedImage(), null, metadata);
		File tmpFile = new File(WebappHelper.getTmpDir(), tmpFileName);
		FileOutputStream fileOutputStream = new FileOutputStream(tmpFile, false);
		OpenBadgesManagerImpl.writeImageIOImage(bakedImage, fileOutputStream);

		// Assert

		InputStream bakedInputStream = new FileInputStream(tmpFile);
		NamedNodeMap attributes = OpenBadgesManagerImpl.findOpenBadgesTextChunk(bakedInputStream);

		Assert.assertNotNull(attributes);

		OpenBadgesBakeContext bakeContext = new OpenBadgesBakeContext(attributes, BadgeVerification.hosted);
		Assertion assertion = bakeContext.getTextAsAssertion();
		Badge badge = assertion.getBadge();

		Assert.assertNotNull(assertion);
		Assert.assertTrue(assertion.getId().endsWith(uuid));
		Assert.assertEquals(badgeClass.getSalt(), assertion.getRecipient().getSalt());
		Assert.assertTrue(badge.getImage().endsWith(badgeClass.getUuid()));

		openBadgesModule.setVerification(badgeVerification);
	}

	@Test
	public void bakePngSigned() throws IOException, JOSEException {
		BadgeVerification badgeVerification = openBadgesModule.getVerification();
		openBadgesModule.setVerification(BadgeVerification.signed);

		// Arrange

		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-assertion-doer");

		String uuid = OpenBadgesFactory.createIdentifier();
		BadgeAssertion badgeAssertion = createBadgeAssertion(uuid);
		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		String textValue = ((OpenBadgesManagerImpl) openBadgesManager).createPngText(badgeAssertion, doer);
		String tmpFileName = uuid + ".png";

		InputStream inputStream = OpenBadgesManagerImplTest.class.getResourceAsStream("test_badge.png");
		IIOImage iioImage = OpenBadgesManagerImpl.readIIOImage(inputStream);
		IIOMetadata metadata = iioImage.getMetadata();

		// Act

		OpenBadgesManagerImpl.addNativePngTextEntry(metadata, "openbadges", textValue);
		IIOImage bakedImage = new IIOImage(iioImage.getRenderedImage(), null, metadata);
		File tmpFile = new File(WebappHelper.getTmpDir(), tmpFileName);
		FileOutputStream fileOutputStream = new FileOutputStream(tmpFile, false);
		OpenBadgesManagerImpl.writeImageIOImage(bakedImage, fileOutputStream);

		// Assert

		InputStream bakedInputStream = new FileInputStream(tmpFile);
		NamedNodeMap attributes = OpenBadgesManagerImpl.findOpenBadgesTextChunk(bakedInputStream);

		Assert.assertNotNull(attributes);

		OpenBadgesBakeContext bakeContext = new OpenBadgesBakeContext(attributes, BadgeVerification.signed);
		String text = bakeContext.getText();
		Assert.assertNotNull(text);
		String[] parts = text.split("\\.");
		Assert.assertEquals(3, parts.length);
		JSONObject header = new JSONObject(new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8));
		Assert.assertEquals("RS256", header.getString("alg"));
		JSONObject payload = new JSONObject(new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8));
		Assert.assertTrue(payload.getString("id").endsWith(uuid));
		JSONObject verification = payload.getJSONObject("verification");
		Assert.assertEquals("signed", verification.getString("type"));
		
		openBadgesModule.setVerification(badgeVerification);
	}

	private BadgeAssertion createBadgeAssertion(String uuid) {
		BadgeClassImpl badgeClassImpl = BadgeTestData.createTestBadgeClass("PNG badge", "image.png", null);
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsUser("badgeRecipient");
		String recipientObject = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeClassImpl.getSalt());
		String verification = ((OpenBadgesManagerImpl) openBadgesManager).createVerificationObject();
		Date issuedOn = new Date();

		return badgeAssertionDAO.createBadgeAssertion(uuid, recipientObject, badgeClassImpl,
				verification, issuedOn, recipient, null);
	}

	@Test
	public void bakeSvgHosted() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
		BadgeVerification badgeVerification = openBadgesModule.getVerification();
		openBadgesModule.setVerification(BadgeVerification.hosted);

		String svg = createTestSvg();
		String verifyValue = "https://test.openolat.org/badge/assertion/123";
		String assertionUuid = OpenBadgesFactory.createIdentifier();
		BadgeAssertion badgeAssertion = createBadgeAssertion(assertionUuid);
		Assertion assertion = new Assertion(badgeAssertion);
		String assertionJson = assertion.asJsonObject().toString();

		OpenBadgesManagerImpl managerImpl = (OpenBadgesManagerImpl) openBadgesManager;
		String mergedSvg = managerImpl.mergeSvg(svg, verifyValue, assertionJson);
		XPath xPath = XPathFactory.newInstance().newXPath();
		String xPathString = "//svg";
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(mergedSvg)));
		NodeList nodeList = (NodeList) xPath.compile(xPathString).evaluate(document, XPathConstants.NODESET);
		
		Assert.assertEquals(1, nodeList.getLength());
		
		Node openBadgesNode = findFirstNonTextNode(nodeList.item(0));
		Assert.assertNotNull(openBadgesNode);
		Assert.assertEquals("openbadges:assertion", openBadgesNode.getNodeName());
		
		// We expect the <openbadges:assertion> element to have a single attribute "verify" and to have one child: the assertion data
		Node cdataNode = findFirstNonTextNode(openBadgesNode);
		Assert.assertNotNull(cdataNode);
		Assert.assertTrue(cdataNode instanceof CDATASection);
		Assert.assertTrue(((CDATASection) cdataNode).getData().contains(assertionUuid));
		String verify = ((Element) openBadgesNode).getAttribute("verify");
		Assert.assertNotNull(verify);
		Assert.assertEquals(verifyValue, verify);

		openBadgesModule.setVerification(badgeVerification);
	}
	
	private Node findFirstNonTextNode(Node node) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() != Node.TEXT_NODE) {
				return childNode;
			}
		}
		return null;
	}
	
	private String createTestSvg() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
				"<svg width=\"100%\" height=\"100%\" viewBox=\"0 0 200 201\" version=\"1.1\"\n" +
				"     xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"\n" +
				"     xmlns:serif=\"http://www.serif.com/\"\n" +
				"     style=\"fill-rule:evenodd;clip-rule:evenodd;stroke-linecap:round;stroke-linejoin:round;stroke-miterlimit:1.5;\">\n" +
				"    <g transform=\"matrix(1,0,0,1,-651,-428)\">\n" +
				"    </g>\n" +
				"</svg>";
	}

	@Test
	public void bakeSvgSigned() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, JOSEException {
		
		// arrange
		BadgeVerification badgeVerification = openBadgesModule.getVerification();
		openBadgesModule.setVerification(BadgeVerification.signed);

		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-assertion-doer");
		OpenBadgesManagerImpl managerImpl = (OpenBadgesManagerImpl) openBadgesManager;;
		
		String svg = createTestSvg();
		
		String assertionUuid = OpenBadgesFactory.createIdentifier(); 
		BadgeAssertion badgeAssertion = createBadgeAssertion(assertionUuid);
		String verifyValue = managerImpl.createBadgeSignature(badgeAssertion, doer);
		
		// act
		
		String mergedSvg = managerImpl.mergeSvg(svg, verifyValue, null);
		
		// assert
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		String xPathString = "//svg";
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(mergedSvg)));
		NodeList nodeList = (NodeList) xPath.compile(xPathString).evaluate(document, XPathConstants.NODESET);

		Assert.assertEquals(1, nodeList.getLength());
		
		Node firstNonTextNode = findFirstNonTextNode(nodeList.item(0));
		Assert.assertNotNull(firstNonTextNode);
		Assert.assertEquals("openbadges:assertion", firstNonTextNode.getNodeName());
		
		// We expect the <openbadges:assertion> element to be an element with one single attribute and no children 
		Assert.assertEquals(0, firstNonTextNode.getChildNodes().getLength());

		Element openBadgeAssertionElement = (Element) firstNonTextNode;
		String verify = openBadgeAssertionElement.getAttribute("verify");
		Assert.assertNotNull(verify);
		String[] parts = verify.split("\\.");
		Assert.assertEquals(3, parts.length);
		JSONObject header = new JSONObject(new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8));
		Assert.assertEquals("RS256", header.getString("alg"));
		JSONObject payload = new JSONObject(new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8));
		String id = payload.getString("id");
		Assert.assertTrue(id.endsWith(assertionUuid));
		JSONObject verification = payload.getJSONObject("verification");
		Assert.assertEquals("signed", verification.getString("type"));
		
		openBadgesModule.setVerification(badgeVerification);
	}

	@Test
	public void copyConfigurationAndBadgeClasses() {
		// arrange
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-class-author-1");
		RepositoryEntry sourceEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		BadgeClassImpl originalBadgeClass = BadgeTestData.createTestBadgeClass("Course badge", "image.png", sourceEntry);
		BadgeEntryConfiguration badgeEntryConfiguration = badgeEntryConfigurationDAO.createConfiguration(sourceEntry);
		badgeEntryConfiguration.setAwardEnabled(true);
		badgeEntryConfiguration.setOwnerCanAward(true);
		badgeEntryConfiguration.setCoachCanAward(true);
		badgeEntryConfigurationDAO.updateConfiguration(badgeEntryConfiguration);

		// act
		RepositoryEntry targetEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		openBadgesManager.copyConfigurationAndBadgeClasses(sourceEntry, targetEntry, author);

		// assert
		List<BadgeClass> originalBadgeClasses = badgeClassDAO.getBadgeClasses(sourceEntry);
		List<BadgeClass> copiedBadgeClasses = badgeClassDAO.getBadgeClasses(targetEntry);
		BadgeEntryConfiguration originalConfiguration = badgeEntryConfigurationDAO.getConfiguration(sourceEntry);
		BadgeEntryConfiguration copiedConfiguration = badgeEntryConfigurationDAO.getConfiguration(targetEntry);

		Assert.assertEquals(1, originalBadgeClasses.size());
		Assert.assertEquals(1, copiedBadgeClasses.size());
		Assert.assertNotNull(originalConfiguration);
		Assert.assertNotNull(copiedConfiguration);

		Assert.assertEquals(originalBadgeClass.getUuid(), originalBadgeClasses.get(0).getUuid());
		Assert.assertNotEquals(originalBadgeClass.getUuid(), copiedBadgeClasses.get(0).getUuid());
		Assert.assertEquals(originalBadgeClass.getName(), copiedBadgeClasses.get(0).getName());

		Assert.assertEquals(originalConfiguration.isAwardEnabled(), copiedConfiguration.isAwardEnabled());
		Assert.assertEquals(originalConfiguration.isCoachCanAward(), copiedConfiguration.isCoachCanAward());
		Assert.assertTrue(copiedConfiguration.isOwnerCanAward());
	}
	
	@Test
	public void getRuleEarnedBadgeAssertions() {
		
		// arrange
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("author-1");
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-assertion-recipient-1");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author);
		String verification = "{\"type\":\"hosted\"}";

		// badge A: directly related to course node A
		String courseNodeIdentA = OpenBadgesFactory.createIdentifier();
		BadgeClassImpl badgeA = BadgeTestData.createTestBadgeClass("Badge A", "image.png", course);
		BadgeCriteria badgeCriteriaA = new BadgeCriteria();
		badgeCriteriaA.setAwardAutomatically(true);
		BadgeCondition badgeConditionA = new CourseElementPassedCondition(courseNodeIdentA, "Course node A");
		badgeCriteriaA.getConditions().add(badgeConditionA);
		badgeA.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteriaA));
		badgeClassDAO.updateBadgeClass(badgeA);
		String uuidA = OpenBadgesFactory.createIdentifier();
		String recipientObjectA = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeA.getSalt());
		BadgeAssertion badgeAssertionA = badgeAssertionDAO.createBadgeAssertion(uuidA, recipientObjectA, badgeA,
				verification, new Date(), recipient, null);

		// badge B: indirectly related to course node A
		String courseNodeIdentB = OpenBadgesFactory.createIdentifier();
		BadgeClassImpl badgeB = BadgeTestData.createTestBadgeClass("Badge B", "image.png", course);
		BadgeCriteria badgeCriteriaB = new BadgeCriteria();
		badgeCriteriaB.setAwardAutomatically(true);
		BadgeCondition badgeConditionB1 = new CourseElementPassedCondition(courseNodeIdentB, "Course node B");
		BadgeCondition badgeConditionB2 = new OtherBadgeEarnedCondition(badgeA.getRootId());
		badgeCriteriaB.getConditions().add(badgeConditionB1);
		badgeCriteriaB.getConditions().add(badgeConditionB2);
		badgeB.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteriaB));
		badgeClassDAO.updateBadgeClass(badgeB);
		String uuidB = OpenBadgesFactory.createIdentifier();
		String recipientObjectB = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeB.getSalt());
		BadgeAssertion badgeAssertionB = badgeAssertionDAO.createBadgeAssertion(uuidB, recipientObjectB, badgeB,
				verification, new Date(), recipient, null);

		// badge C: not related to course node
		String courseNodeIdentC = OpenBadgesFactory.createIdentifier();
		BadgeClassImpl badgeC = BadgeTestData.createTestBadgeClass("Badge C", "image.png", course);
		BadgeCriteria badgeCriteriaC = new BadgeCriteria();
		badgeCriteriaC.setAwardAutomatically(true);
		BadgeCondition badgeConditionC = new CourseElementPassedCondition(courseNodeIdentC, "Course node C");
		badgeCriteriaC.getConditions().add(badgeConditionC);
		badgeC.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteriaC));
		badgeClassDAO.updateBadgeClass(badgeC);
		String uuidC = OpenBadgesFactory.createIdentifier();
		String recipientObjectC = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeC.getSalt());
		BadgeAssertion badgeAssertionC = badgeAssertionDAO.createBadgeAssertion(uuidC, recipientObjectC, badgeC,
				verification, new Date(), recipient, null);


		// act: 
		// 
		// Return the badge assertions that the recipient obtained and that are due to rules related to
		// course 'course' and course node 'courseNodeIdentA'.
		//
		List<BadgeAssertion> badgeAssertions = openBadgesManager.getRuleEarnedBadgeAssertions(recipient, course, courseNodeIdentA);
		
		// assert
		
		Assert.assertEquals(2, badgeAssertions.size());
		Assert.assertTrue(badgeAssertions.contains(badgeAssertionA));
		Assert.assertTrue(badgeAssertions.contains(badgeAssertionB));
		Assert.assertFalse(badgeAssertions.contains(badgeAssertionC));
	}
	
	@Test
	public void issueBadgeManually() {

		// arrange

		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badge-manually-doer-1");
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badge-manually-recipient-1");
		String badgeAUuid = OpenBadgesFactory.createIdentifier();

		BadgeClassImpl badgeD = BadgeTestData.createTestBadgeClass("Badge 1: D", "image.png", null);
		BadgeClassImpl badgeA = BadgeTestData.createTestBadgeClass("Badge 2: A", "image.png", null);
		BadgeClassImpl badgeE = BadgeTestData.createTestBadgeClass("Badge 3: E", "image.png", null);
		BadgeClassImpl badgeB = BadgeTestData.createTestBadgeClass("Badge 4: B", "image.png", null);
		BadgeClassImpl badgeC = BadgeTestData.createTestBadgeClass("Badge 5: C", "image.png", null);
		BadgeClassImpl badgeF = BadgeTestData.createTestBadgeClass("Badge 6: F", "image.png", null);

		RepositoryEntry courseC = JunitTestHelper.deployBasicCourse(doer);
		passCourse(courseC, recipient);

		makeManual(badgeA);
		makeManual(badgeE);
		
		makeAutomaticAndGloballyDependent(badgeB, badgeA);

		makeAutomaticAndGloballyDependent(badgeC, courseC);
		makeAutomaticAndGloballyDependent(badgeC, badgeB);
		
		makeAutomaticAndGloballyDependent(badgeD, courseC);
		makeAutomaticAndGloballyDependent(badgeD, badgeC);
		makeAutomaticAndGloballyDependent(badgeD, badgeB);
		makeAutomaticAndGloballyDependent(badgeD, badgeA);
		makeAutomaticAndGloballyDependent(badgeF, badgeB);
		
		badgeF.setStatus(BadgeClass.BadgeClassStatus.revoked);
		badgeClassDAO.updateBadgeClass(badgeF);

		dbInstance.commit();
		
		// act:
		//
		// Issue 'badgeA' manually. Due to down-stream badge dependencies, this should automatically issue another
		// badge B and another badge C.
		//

		openBadgesManager.issueBadgeManually(badgeAUuid, badgeA, recipient, doer);
		dbInstance.commit();
		
		// assert
		
		BadgeAssertion badgeAssertionA = badgeAssertionDAO.getBadgeAssertion(recipient, badgeA);
		BadgeAssertion badgeAssertionB = badgeAssertionDAO.getBadgeAssertion(recipient, badgeB);
		BadgeAssertion badgeAssertionC = badgeAssertionDAO.getBadgeAssertion(recipient, badgeC);
		BadgeAssertion badgeAssertionD = badgeAssertionDAO.getBadgeAssertion(recipient, badgeD);
		BadgeAssertion badgeAssertionE = badgeAssertionDAO.getBadgeAssertion(recipient, badgeE);
		BadgeAssertion badgeAssertionF = badgeAssertionDAO.getBadgeAssertion(recipient, badgeF);
		List<BadgeAssertion> badgeAssertions = badgeAssertionDAO.getBadgeAssertions(recipient);
		
		Assert.assertNotNull(badgeAssertionA);
		Assert.assertNotNull(badgeAssertionB);
		Assert.assertNotNull(badgeAssertionC);
		Assert.assertNotNull(badgeAssertionD);
		Assert.assertNull(badgeAssertionE);
		Assert.assertNull(badgeAssertionF);
		Assert.assertEquals(4, badgeAssertions.size());
	}

	private void passCourse(RepositoryEntry course, Identity participant) {
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry assessmentEntry = assessmentEntryDAO.createAssessmentEntry(participant, null,
				course, subIdent, true, null);
		assessmentEntry.setPassed(true);
		assessmentEntryDAO.updateAssessmentEntry(assessmentEntry);
	}

	private void makeManual(BadgeClassImpl badge) {
		BadgeCriteria badgeCriteria = new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(false);
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}
	
	private void makeAutomaticAndGloballyDependent(BadgeClassImpl badge, BadgeClassImpl badgeDependency) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new GlobalBadgesEarnedCondition(List.of(badgeDependency.getRootId())));
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}

	private void makeAutomaticAndGloballyDependent(BadgeClassImpl badge, RepositoryEntry course) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new CoursesPassedCondition(List.of(course.getKey())));
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}
	
	@Test
	public void issueBadgesAutomatically() {
		
		// arrange:

		Identity ownerAB = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badges-automatically-owner-1");
		Identity ownerC = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badges-automatically-owner-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badges-automatically-participant");
		
		RepositoryEntry courseA = JunitTestHelper.deployBasicCourse(ownerAB);
		BadgeEntryConfiguration configA = badgeEntryConfigurationDAO.createConfiguration(courseA);
		configA.setAwardEnabled(true);
		badgeEntryConfigurationDAO.updateConfiguration(configA);
		BadgeClassImpl badgeA = BadgeTestData.createTestBadgeClass("Badge A", "image.png", courseA);
		
		RepositoryEntry courseB = JunitTestHelper.deployBasicCourse(ownerAB);
		BadgeEntryConfiguration configB = badgeEntryConfigurationDAO.createConfiguration(courseB);
		configB.setAwardEnabled(true);
		badgeEntryConfigurationDAO.updateConfiguration(configB);
		BadgeClassImpl badgeB = BadgeTestData.createTestBadgeClass("Badge B", "image.png", courseB);
		
		RepositoryEntry courseC = JunitTestHelper.deployBasicCourse(ownerC);
		BadgeEntryConfiguration configC = badgeEntryConfigurationDAO.createConfiguration(courseC);
		configC.setAwardEnabled(true);
		badgeEntryConfigurationDAO.updateConfiguration(configC);
		BadgeClassImpl badgeC = BadgeTestData.createTestBadgeClass("Badge C", "image.png", courseC);
		
		Group group = groupDAO.createGroup();
		repositoryEntryRelationDAO.createRelation(group, courseA);
		repositoryEntryRelationDAO.createRelation(group, courseB);
		groupDAO.addMembershipOneWay(group, ownerAB, "owner");

		makeAutomaticOnPass(badgeA);
		makeAutomaticOnPass(badgeB);
		makeAutomaticOnPass(badgeC);
		makeAutomaticAndDependent(badgeA, badgeB);

		passCourse(courseA, participant);
		passCourse(courseB, participant);
		passCourse(courseC, participant);
		
		dbInstance.commit();
		
		// act:
		//
		// Issue badges of 'courseA' automatically (simulating that courseA has been passed and that 
		// the openBadgesManager is called as a result of this). Should lead to a chain reaction.
		//

		openBadgesManager.issueBadgesAutomatically(courseA, ownerAB);
		dbInstance.commit();
		
		// assert:
		
		BadgeAssertion badgeAssertionA = badgeAssertionDAO.getBadgeAssertion(participant, badgeA);
		BadgeAssertion badgeAssertionB = badgeAssertionDAO.getBadgeAssertion(participant, badgeB);
		BadgeAssertion badgeAssertionC = badgeAssertionDAO.getBadgeAssertion(participant, badgeC);

		Assert.assertNotNull(badgeAssertionA);
		Assert.assertNotNull(badgeAssertionB);
		Assert.assertNull(badgeAssertionC);
	}

	private void makeAutomaticAndDependent(BadgeClassImpl badge, BadgeClassImpl badgeDependency) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new OtherBadgeEarnedCondition(badgeDependency.getRootId()));
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}
	
	private void makeAutomaticOnPass(BadgeClassImpl badge) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new CoursePassedCondition());
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}

	@Test
	public void createNewBadgeClassVersionBasic() {

		// arrange
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("doer-1");
		BadgeClassImpl badgeA1 = BadgeTestData.createTestBadgeClass("Badge A", "image.png", null);

		// act
		openBadgesManager.createNewBadgeClassVersion(badgeA1.getKey(), doer);
		
		// assert
		BadgeClass badgeA2 = badgeClassDAO.getCurrentBadgeClass(badgeA1.getRootId());
		BadgeClass badgeA1Reloaded = badgeClassDAO.getBadgeClassByKey(badgeA1.getKey());

		Assert.assertNotNull(badgeA2);
		Assert.assertEquals(badgeA1Reloaded.getRootId(), badgeA2.getRootId());
		Assert.assertNotEquals(badgeA1Reloaded.getUuid(), badgeA2.getUuid());
		Assert.assertEquals(OpenBadgesFactory.getDefaultVersion(), badgeA1Reloaded.getVersion());
		Assert.assertEquals("2", badgeA2.getVersion());
		Assert.assertEquals(badgeA1Reloaded, badgeA2.getPreviousVersion());
		Assert.assertNull(badgeA2.getNextVersion());
		Assert.assertNull(badgeA1.getPreviousVersion());
		Assert.assertEquals(badgeA2,  badgeA1.getNextVersion());
	}
	
	private static final String BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n";
	private static final String END_PUBLIC_KEY = "\n-----END PUBLIC KEY-----";
	
	@Test
	public void signBadgePrep() throws Exception {
		// arrange

		// create a private / public key
		RSAKey rsaKey = new RSAKeyGenerator(2048)
				.algorithm(JWSAlgorithm.RS256)
				.generate();
		
		// create a PEM encoded string with the public key
		byte[] data = rsaKey.toPublicKey().getEncoded();
		String base64encodedPublicKey = new String(Base64.getEncoder().encode(data));
		String publicKeyPem = BEGIN_PUBLIC_KEY + base64encodedPublicKey + END_PUBLIC_KEY;

		// create a signer 
		JWSSigner jwsSigner = new RSASSASigner(rsaKey);
		
		// create a public key set and write it to a file for debugging:
		JWKSet publicJwsSet = new JWKSet(List.of(rsaKey.toPublicJWK()));
		String publicJwsSetString = publicJwsSet.toString();
		String rid = UUID.randomUUID().toString();
		File tmpSet = File.createTempFile(rid, "jwks-1", new File(WebappHelper.getTmpDir()));
		FileUtils.writeStringToFile(tmpSet, publicJwsSetString, StandardCharsets.UTF_8);

		// create a JWS object with a simple data similar to a badge assertion.
		JWSObject jwsObject = new JWSObject(
				new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
				new Payload(Map.of(
						"id", "https://billy.frentix.com/badge/assertion/1234", 
						"type", "Assertion")
				)
		);
		
		// now sign it
		jwsObject.sign(jwsSigner);
		String s = jwsObject.serialize();

		// now parse it again
		SignedJWT signedJWT = SignedJWT.parse(s);

		// create a RSA key from the PEM
		PublicKey publicKey = CryptoUtil.string2PublicKey(publicKeyPem);
		if (publicKey instanceof RSAPublicKey rsaPublicKey) {
			RSASSAVerifier verifier = new RSASSAVerifier(rsaPublicKey);
			boolean success = signedJWT.verify(verifier);
		}
	}

	@Test
	public void signBadge() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-admin-1");
		OpenBadgesManagerImpl openBadgesManagerImpl = (OpenBadgesManagerImpl)openBadgesManager;

		PrivateKey privateKey = openBadgesManagerImpl.getPrivateKey(author);
		JWSSigner jwsSigner = new RSASSASigner(privateKey);

		String uuid = OpenBadgesFactory.createIdentifier();
		BadgeAssertion badgeAssertion = createBadgeAssertion(uuid);
		Assertion assertion = new Assertion(badgeAssertion);
		JSONObject assertionJsonObject = assertion.asJsonObject();
		
		JWSObject jwsObject = new JWSObject(
				new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
				new Payload(assertionJsonObject.toString())
		);

		jwsObject.sign(jwsSigner);
		String s = jwsObject.serialize();

		SignedJWT signedJWT = SignedJWT.parse(s);

		boolean success = false;

		PublicKey publicKey = openBadgesManagerImpl.getPublicKey(author);
		if (publicKey instanceof RSAPublicKey rsaPublicKey) {
			RSASSAVerifier verifier = new RSASSAVerifier(rsaPublicKey);
			success = signedJWT.verify(verifier);
		}

		Assert.assertTrue(success);
	}

}