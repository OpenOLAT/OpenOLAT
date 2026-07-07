/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.ai.service;

/**
 * Shared prompt fragments for all generative AI features. The constants are
 * compile-time constants so they can be concatenated into the
 * {@code @SystemMessage} annotation values of the LangChain4j AiService
 * interfaces in this package. Every new generative prompt must append
 * {@link #OUTPUT_STYLE_RULES} to its system message.
 *
 * Initial date: 2026-07-06<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public final class AiPromptRules {

	/**
	 * House typography rules for ALL generated text, regardless of feature
	 * and output language. Swiss German spelling: no ß, real umlauts, no
	 * em/en dashes.
	 */
	public static final String OUTPUT_STYLE_RULES = """


			General output style rules, they apply to ALL generated text (titles, descriptions, \
			questions, answers, feedback, metadata):
			- Never use em dashes (—) or en dashes (–). Use a comma, a colon, parentheses or a \
			simple hyphen (-) instead.
			- German text always uses Swiss spelling: never write the character ß, always write \
			ss instead (gross, heisst, Strasse).
			- German text always uses the real umlaut characters ä, ö, ü, Ä, Ö, Ü. Never write \
			the replacement forms ae, oe, ue for an umlaut (Prüfung, not Pruefung).""";

	private AiPromptRules() {
		// constants only
	}
}
