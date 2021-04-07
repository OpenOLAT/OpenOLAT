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
package org.olat.ims.lti13;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * 
 * Initial date: 19 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13JsonUtil {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	private LTI13JsonUtil() {
		//
	}
	
	public static final Double convert(Float value) {
		if(value == null) return null;
		return Double.valueOf(value.doubleValue());
	}

	public static <T> T readValue(String content, Class<T> valueType)
	throws JsonProcessingException {
		return mapper.readValue(content, valueType);
	}
	
	public static String prettyPrint(Object obj)
	throws com.fasterxml.jackson.core.JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		return writer.writeValueAsString(obj);
	}
	
	public static String publicKeysToJwks(List<LTI13Key> keys) {
		JSONArray jar = new JSONArray();
		for(LTI13Key key:keys) {
			RSAPublicKey publicKey = (RSAPublicKey)key.getPublicKey();
			JWK jwk = new RSAKey.Builder(publicKey)
					.algorithm(JWSAlgorithm.parse(key.getAlgorithm()))
				    .keyUse(KeyUse.SIGNATURE)
				    .keyID(key.getKeyId())
				    .build();
			
			String keyStr = jwk.toJSONString();
			JSONObject kobj = new JSONObject(keyStr);
			kobj.put("kid", key.getKeyId());
			kobj.put("use", "sig");
			jar.put(kobj);
		}

		JSONObject keyset = new JSONObject();
		keyset.put("keys", jar);
		return keyset.toString();
	}
	
	public static String publicKeysToJwks(String kid, PublicKey key) {
		RSAPublicKey publicKey = (RSAPublicKey)key;
		JWK jwk = new RSAKey.Builder(publicKey)
				.algorithm(JWSAlgorithm.parse(key.getAlgorithm()))
			    .keyUse(KeyUse.SIGNATURE)
			    .keyID(kid)
			    .build();
		
		String keyStr = jwk.toJSONString();
		JSONObject kobj = new JSONObject(keyStr);
		kobj.put("kid", kid);
		kobj.put("use", "sig");
		return kobj.toString();
	}
}
