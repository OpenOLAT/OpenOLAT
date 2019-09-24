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

package org.olat.shibboleth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.registration.AbstractUserNameCreationInterceptor;

/**
 * 
 * Description:<br>
 * An implementation of the UserNameCreationInterceptor which generate
 * a username from a Shibboleth attribute. The attribute is configurable
 * in olat.properties (registration.preset.username.shibbolethAttribute) and
 * the possibility to change it too (registration.preset.username.allowChanges)
 * 
 * <P>
 * Initial Date:  5 mars 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ShibbolethUserNameFromAttributeUserNameCreationInterceptor extends AbstractUserNameCreationInterceptor {

	private String shibUsernameAttr;
	private List<String> regexp;
	private List<Pattern> regexpPatterns;
	
	public ShibbolethUserNameFromAttributeUserNameCreationInterceptor() {
		//
	}
	
	public String getShibUsernameAttr() {
		return shibUsernameAttr;
	}

	public void setShibUsernameAttr(String shibUsernameAttr) {
		this.shibUsernameAttr = shibUsernameAttr;
	}

	public List<String> getRegexp() {
		return regexp;
	}

	public void setRegexp(List<String> regexp) {
		this.regexp = regexp;
	}

	@Override
	public String getUsernameFor(Map<String, String> userAttributes) {
		String proposedUsername = userAttributes.get(shibUsernameAttr);
		if(proposedUsername == null) {
			shibUsernameAttr = CoreSpringFactory.getImpl(ShibbolethModule.class).getAttributeTranslator().translateAttribute(shibUsernameAttr);
			proposedUsername = userAttributes.get(shibUsernameAttr);
		}
		
		if(!StringHelper.containsNonWhitespace(proposedUsername)) {
			return null;
		}
		
		if(regexp != null && !regexp.isEmpty()) {
			if(regexpPatterns == null) {
				regexpPatterns = new ArrayList<>();
				for(String regex:regexp) {
					regexpPatterns.add(Pattern.compile(regex));
				}
			}

			for(Pattern regexPattern:regexpPatterns) {
				Matcher m = regexPattern.matcher(proposedUsername);
				if(m.find()) {
					proposedUsername = m.group();
					if(!StringHelper.containsNonWhitespace(proposedUsername)) {
						proposedUsername = null;
						break;
					}
				}
			}
		}
		
		if(isMakeUniqueProposal()) {
			return makeUniqueProposal(proposedUsername);
		}
		return proposedUsername;
	}
}