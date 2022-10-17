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
package org.olat.restapi.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Variant;

import org.olat.core.util.StringHelper;

public class MediaTypeVariants {
	
	public static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON_TYPE;
	public static final MediaType APPLICATION_JSON_PAGED;
	public static final MediaType APPLICATION_XML = MediaType.APPLICATION_XML_TYPE;
	public static final MediaType APPLICATION_XML_PAGED;
	
	private static final Variant VARIANT_JSON;
	private static final Variant VARIANT_JSON_PAGED;
	private static final Variant VARIANT_XML;
	private static final Variant VARIANT_XML_PAGED;
	
	private static final List<Variant> variants = new ArrayList<>();
	
	static {
		Map<String, String> pagingSpec = new HashMap<>();
		pagingSpec.put("pagingspec","1.0");
		VARIANT_JSON = new Variant(APPLICATION_JSON, (Locale)null, null);
		APPLICATION_JSON_PAGED = new MediaType("application","json", pagingSpec);
		VARIANT_JSON_PAGED = new Variant(APPLICATION_JSON_PAGED, (Locale)null, null);

		VARIANT_XML = new Variant(APPLICATION_XML, (Locale)null, null);
		APPLICATION_XML_PAGED = new MediaType("application","xml", pagingSpec);
		VARIANT_XML_PAGED = new Variant(APPLICATION_XML_PAGED, (Locale)null, null);
		
		variants.add(VARIANT_JSON);
		variants.add(VARIANT_JSON_PAGED);
		variants.add(VARIANT_XML);
		variants.add(VARIANT_XML_PAGED);
	}
	
	private MediaTypeVariants() {
		//
	}
	
	public static List<Variant> getVariants() {
		return new ArrayList<>(variants);
	}
	
	public static boolean isPaged(HttpServletRequest httpRequest, Request request) {
		String accept = httpRequest.getHeader("Accept");
		if(StringHelper.containsNonWhitespace(accept)) {
			try {
				MediaType requestMediaType = MediaType.valueOf(accept);
				if(APPLICATION_JSON_PAGED.equals(requestMediaType) || APPLICATION_XML_PAGED.equals(requestMediaType)) {
					return true;
				}
			} catch (IllegalArgumentException e) {
				// can fail
			}
		}
		Variant variant = request.selectVariant(variants);
		return (variant != null && (variant.getMediaType().equals(APPLICATION_JSON_PAGED) || variant.getMediaType().equals(APPLICATION_XML_PAGED)));
	}
}