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
package org.olat.core.commons.services.tagging.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tagging.model.TagImpl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * A very simple manager to propose tags.
 * 
 * <P>
 * Initial Date:  19 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("simpleTagProposalManager")
public class SimpleTagProposalManager implements TagProposalManager {

	@Autowired
	private DB dbInstance;

	@Override
	public List<String> proposeTagsForInputText(String referenceText, boolean onlyExisting) {
		List<String> tokens = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(referenceText, " \t\n\r\f.,;:-!?");
		for (; tokenizer.hasMoreTokens();) {
			String next = tokenizer.nextToken().trim();
			next = next.replace(" ", "");
			if (!tokens.contains(next) && StringHelper.containsNonWhitespace(next) && next.length() > 3) tokens.add(next);
			if (tokens.size() == 500) {
				break;
			}
		}

		if (onlyExisting) {
			StringBuilder sb = new StringBuilder(64);
			sb.append("select tag.tag from ").append(TagImpl.class.getName()).append(" tag where tag.tag in (:tokens) group by tag.tag");
			return dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), String.class)
					.setParameter("tokens", tokens)
					.getResultList();
		}
		return tokens;
	}
}
