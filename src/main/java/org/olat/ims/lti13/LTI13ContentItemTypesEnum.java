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
package org.olat.ims.lti13;

/**
 * 
 * 
 * Initial date: 7 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum LTI13ContentItemTypesEnum {
	
	link,
	/**
	 * @see https://www.imsglobal.org/spec/lti-dl/v2p0#lti-resource-link
	 */
	ltiResourceLink,
	file,
	html,
	image;
	
	public static final LTI13ContentItemTypesEnum secureValueOf(Object val) {
		LTI13ContentItemTypesEnum[] types = values();
		for(LTI13ContentItemTypesEnum type:types) {
			if(type.name().equals(val)) {
				return type;
			}
		}
		return null;
	}
}
