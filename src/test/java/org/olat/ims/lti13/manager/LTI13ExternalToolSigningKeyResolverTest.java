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
package org.olat.ims.lti13.manager;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.model.JwtToolBundle;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

/**
 * 
 * Initial date: 27 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ExternalToolSigningKeyResolverTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13ExternalToolSigningKeyResolverTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13Service lti13Service;
	
	@Test
	public void resolveWithKid() throws Exception {
		// prepare the public key
		RSAKey rsaJWK = new RSAKeyGenerator(2048)
		    .keyID("public-key-1")
		    .algorithm(JWSAlgorithm.RS256)
		    .generate();
		RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();

		// Create RSA-signer with the private key
		JWSSigner signer = new RSASSASigner(rsaJWK);

		JWKSet publicKeys = new JWKSet(List.of(rsaPublicJWK));
		String jwksSetString = publicKeys.toString();

		// configure a tool
		String rid = UUID.randomUUID().toString();
		File tmpSet = File.createTempFile(rid, "jwks-1", new File(WebappHelper.getTmpDir()));
		FileUtils.writeStringToFile(tmpSet, jwksSetString, StandardCharsets.UTF_8);
		
		LTI13Tool tool = lti13Service.createExternalTool("JWKS-1", "https://www.openolat.org", rid, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKeyUrl(tmpSet.toURI().toString());
		tool.setPublicKeyTypeEnum(PublicKeyType.URL);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(tool);
	
		// Prepare JWS object with simple string as payload
		JWSObject jwsObject = new JWSObject(
		    new JWSHeader.Builder(JWSAlgorithm.RS256)
		    	.keyID(rsaJWK.getKeyID()).build(),
		    new Payload(Map.of("iss", "https://www.openolat.org", "sub", rid, "kid", "123", "secret", "top")));

		jwsObject.sign(signer);
		String s = jwsObject.serialize();

		// Verify the JWT with the file public key
		LTI13ExternalToolSigningKeyResolver signingResolver = new LTI13ExternalToolSigningKeyResolver();
		Jwt<?,?> jwt = Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parse(s);
		log.debug("Token: {}", jwt);
		Claims body = (Claims)jwt.getBody();
		Assert.assertEquals("top", body.get("secret"));
	}
	
	/**
	 * Generate a JWK Set without Key ID and search without it.
	 * 
	 * @throws Exception
	 */
	@Test
	public void resolveWithoutKid() throws Exception {
		// prepare the public key
		RSAKey rsaJWK = new RSAKeyGenerator(2048)
		    .algorithm(JWSAlgorithm.RS256)
		    .generate();
		RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();

		// Create RSA-signer with the private key
		JWSSigner signer = new RSASSASigner(rsaJWK);
		JWKSet publicKeys = new JWKSet(List.of(rsaPublicJWK));

		String rid = UUID.randomUUID().toString();
		File tmpSet = File.createTempFile(rid, "jwks-1", new File(WebappHelper.getTmpDir()));
		FileUtils.writeStringToFile(tmpSet, publicKeys.toString(), StandardCharsets.UTF_8);
		
		LTI13Tool tool = lti13Service.createExternalTool("JWKS-2", "https://www.openolat.org", rid, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKeyUrl(tmpSet.toURI().toString());
		tool.setPublicKeyTypeEnum(PublicKeyType.URL);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(tool);
	
		// Prepare JWS object with simple string as payload
		JWSObject jwsObject = new JWSObject(
		    new JWSHeader.Builder(JWSAlgorithm.RS256)
		    	.keyID(rsaJWK.getKeyID()).build(),
		    new Payload(Map.of("iss", "https://www.openolat.org/auth/", "sub", rid, "secret", "tip")));

		jwsObject.sign(signer);
		String s = jwsObject.serialize();
		
		LTI13ExternalToolSigningKeyResolver signingResolver = new LTI13ExternalToolSigningKeyResolver();
		Jwt<?,?> jwt = Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parse(s);
		log.debug("Token: {}", jwt);
		Claims body = (Claims)jwt.getBody();
		Assert.assertEquals("tip", body.get("secret"));
	}
	
	/**
	 * Generate a JWK Set with Key ID and search without it. Feature of the service.
	 * 
	 * @throws Exception
	 */
	@Test
	public void resolveByServiceWithoutKid() throws Exception {
		// prepare the public key
		RSAKey rsaJWK = new RSAKeyGenerator(2048)
			.keyID("public-key-1234")
		    .algorithm(JWSAlgorithm.RS256)
		    .generate();
		RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();

		// Create RSA-signer with the private key
		JWSSigner signer = new RSASSASigner(rsaJWK);
		JWKSet publicKeys = new JWKSet(List.of(rsaPublicJWK));

		String rid = UUID.randomUUID().toString();
		File tmpSet = File.createTempFile(rid, "jwks-3", new File(WebappHelper.getTmpDir()));
		FileUtils.writeStringToFile(tmpSet, publicKeys.toString(), StandardCharsets.UTF_8);
		
		LTI13Tool tool = lti13Service.createExternalTool("JWKS-3", "https://www.openolat.org", rid, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKeyUrl(tmpSet.toURI().toString());
		tool.setPublicKeyTypeEnum(PublicKeyType.URL);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(tool);
	
		// Prepare JWS object with simple string as payload
		JWSObject jwsObject = new JWSObject(
		    new JWSHeader.Builder(JWSAlgorithm.RS256)
		    	.keyID(rsaJWK.getKeyID()).build(),
		    new Payload(Map.of("iss", "https://www.openolat.org/auth/", "sub", rid, "secret", "tip")));

		jwsObject.sign(signer);
		String s = jwsObject.serialize();
		
		JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(s);
		Claims body = (Claims)bundle.getJwt().getBody();
		Assert.assertEquals("tip", body.get("secret"));
	}
	
	/**
	 * Loop through a bunch of potential public keys.
	 * 
	 * @throws Exception
	 */
	@Test
	public void resolveByService() throws Exception {
		// prepare the public key
		List<JWK> jwks = new ArrayList<>();
		for(int i=0; i<10; i++) {
			RSAKey altRsaJWK = new RSAKeyGenerator(2048)
					.keyID("public-key-3-" + i)
				    .algorithm(JWSAlgorithm.RS384)
				    .generate();
			jwks.add(altRsaJWK.toPublicJWK());
		}
		
		RSAKey rsa1JWK = new RSAKeyGenerator(2048)
				.keyID("public-key-4")
			    .algorithm(JWSAlgorithm.RS384)
			    .generate();
		jwks.add(rsa1JWK.toPublicJWK());
		
		// Create RSA-signer with the private key
		JWSSigner signer = new RSASSASigner(rsa1JWK);
		JWKSet publicKeys = new JWKSet(jwks);

		String rid = UUID.randomUUID().toString();
		File tmpSet = File.createTempFile(rid, "jwks-3", new File(WebappHelper.getTmpDir()));
		FileUtils.writeStringToFile(tmpSet, publicKeys.toString(), StandardCharsets.UTF_8);
		
		LTI13Tool tool = lti13Service.createExternalTool("JWKS-3", "https://www.openolat.org", rid, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKeyUrl(tmpSet.toURI().toString());
		tool.setPublicKeyTypeEnum(PublicKeyType.URL);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(tool);
	
		// Prepare JWS object with simple string as payload
		JWSObject jwsObject = new JWSObject(
		    new JWSHeader.Builder(JWSAlgorithm.RS384)
		    	.build(),
		    new Payload(Map.of("iss", "https://www.openolat.org/auth/", "sub", rid, "secret", "tip")));

		jwsObject.sign(signer);
		String s = jwsObject.serialize();
		
		JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(s);
		Claims body = (Claims)bundle.getJwt().getBody();
		Assert.assertEquals("tip", body.get("secret"));
	}
	
	/**
	 * The key to sign the JWT is not in the public key set. 
	 * 
	 * @throws Exception
	 */
	@Test
	public void resolveByServiceNegativeTest() throws Exception {
		// prepare the public key
		RSAKey rsaJWK = new RSAKeyGenerator(2048)
				.keyID("real-one-1")
			    .algorithm(JWSAlgorithm.RS384)
			    .generate();
		
		JWKSet publicKeys = new JWKSet(List.of(rsaJWK.toPublicJWK()));
		
		String rid = UUID.randomUUID().toString();
		File tmpSet = File.createTempFile(rid, "jwks-3", new File(WebappHelper.getTmpDir()));
		FileUtils.writeStringToFile(tmpSet, publicKeys.toString(), StandardCharsets.UTF_8);
		
		LTI13Tool tool = lti13Service.createExternalTool("JWKS-3", "https://www.openolat.org", rid, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKeyUrl(tmpSet.toURI().toString());
		tool.setPublicKeyTypeEnum(PublicKeyType.URL);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(tool);
		
		RSAKey rogueRsaJWK = new RSAKeyGenerator(2048)
				.keyID("rogue-one-1")
			    .algorithm(JWSAlgorithm.RS384)
			    .generate();
		
		// Create RSA-signer with the private key which is not in the JWKs
		JWSSigner rogueSigner = new RSASSASigner(rogueRsaJWK);
	
		// Prepare JWS object with simple string as payload
		JWSObject jwsObject = new JWSObject(
		    new JWSHeader.Builder(JWSAlgorithm.RS384)
		    	.build(),
		    new Payload(Map.of("iss", "https://www.openolat.org/auth/", "sub", rid, "secret", "tip")));

		jwsObject.sign(rogueSigner);
		String s = jwsObject.serialize();
		
		JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(s);
		Assert.assertNull(bundle);
	}
	
	/**
	 * A first set is used to verify a JWT, the set is enhanced with anew public key
	 * which is used by a new JWT. This test is all about the caching mechanism used
	 * to store the public keys.
	 */
	@Test
	public void resolveByServiceWithNewPublicKey() throws Exception {
		// prepare the public key
		List<JWK> jwks = new ArrayList<>();
		
		RSAKey rsa1JWK = new RSAKeyGenerator(2048)
				.keyID("publickey-5-21")
			    .algorithm(JWSAlgorithm.RS384)
			    .generate();
		jwks.add(rsa1JWK.toPublicJWK());
		
		// Create RSA-signer with the private key
		JWSSigner signer = new RSASSASigner(rsa1JWK);
		JWKSet publicKeys = new JWKSet(jwks);

		String rid = UUID.randomUUID().toString();
		File tmpSet = File.createTempFile(rid, "jwks-5", new File(WebappHelper.getTmpDir()));
		FileUtils.writeStringToFile(tmpSet, publicKeys.toString(), StandardCharsets.UTF_8);
		
		LTI13Tool tool = lti13Service.createExternalTool("JWKS-5", "https://www.openolat.org", rid, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKeyUrl(tmpSet.toURI().toString());
		tool.setPublicKeyTypeEnum(PublicKeyType.URL);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(tool);
	
		// Prepare JWS object with simple string as payload
		JWSObject jwsObject = new JWSObject(
		    new JWSHeader.Builder(JWSAlgorithm.RS384)
		    	.build(),
		    new Payload(Map.of("iss", "https://www.openolat.org/auth/", "sub", rid, "secret", "tip")));

		jwsObject.sign(signer);
		String s = jwsObject.serialize();
		
		JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(s);
		Claims body = (Claims)bundle.getJwt().getBody();
		Assert.assertEquals("tip", body.get("secret"));
		
		RSAKey rsa2JWK = new RSAKeyGenerator(2048)
				.keyID("tpublickey-5-26")
			    .algorithm(JWSAlgorithm.RS384)
			    .generate();
		jwks.add(rsa2JWK.toPublicJWK());
		
		publicKeys = new JWKSet(List.of(rsa1JWK, rsa2JWK));
		FileUtils.writeStringToFile(tmpSet, publicKeys.toString(), StandardCharsets.UTF_8);
		
		// Prepare JWS object with simple string as payload
		JWSObject jws2Object = new JWSObject(
		    new JWSHeader.Builder(JWSAlgorithm.RS384)
		    	.build(),
		    new Payload(Map.of("iss", "https://www.openolat.org/auth/", "sub", rid, "secret", "very")));

		JWSSigner signer2 = new RSASSASigner(rsa2JWK);
		jws2Object.sign(signer2);
		String s2 = jws2Object.serialize();
		
		JwtToolBundle bundle2 = lti13Service.getAndVerifyClientAssertion(s2);
		Claims body2 = (Claims)bundle2.getJwt().getBody();
		Assert.assertEquals("very", body2.get("secret"));
	}
}
