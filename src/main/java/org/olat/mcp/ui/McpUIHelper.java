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
package org.olat.mcp.ui;

import java.util.Collection;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpModule;

/**
 * Builds the texts the administration and the personal settings show to
 * connect an MCP client: the endpoint URL and the Claude Code command.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public final class McpUIHelper {

	/** Placeholder of the token in the command of the administration */
	public static final String TOKEN_PLACEHOLDER = "<token>";

	private McpUIHelper() {
		//
	}

	/**
	 * @return The absolute URL of the MCP endpoint of this instance
	 */
	public static String getEndpointUrl() {
		return Settings.getServerContextPathURI() + McpModule.MCP_ENDPOINT;
	}

	/**
	 * @param token The plaintext token or {@link #TOKEN_PLACEHOLDER}
	 * @return The command to register this instance in Claude Code
	 */
	public static String getClaudeCodeCommand(String token) {
		return "claude mcp add --transport http openolat " + getEndpointUrl()
			+ " --header \"Authorization: Bearer " + token + "\"";
	}

	/**
	 * Validates a visible text or integer element that holds a whole number
	 * in a range. A hidden element is valid, it is not saved.
	 *
	 * @param el The element
	 * @param min The minimum, inclusive
	 * @param max The maximum, inclusive
	 * @param mandatory true if an empty value is an error
	 * @return true if the value is valid
	 */
	public static boolean validateInteger(TextElement el, int min, int max, boolean mandatory) {
		el.clearError();
		if(!el.isVisible()) {
			return true;
		}
		String value = el.getValue();
		if(!StringHelper.containsNonWhitespace(value)) {
			if(mandatory) {
				el.setErrorKey("form.legende.mandatory");
				return false;
			}
			return true;
		}
		try {
			int intValue = Integer.parseInt(value.trim());
			if(intValue >= min && intValue <= max) {
				return true;
			}
		} catch (NumberFormatException e) {
			// the error below explains the expected value
		}
		el.setErrorKey("form.error.nointeger.between", Integer.toString(min), Integer.toString(max));
		return false;
	}

	/**
	 * @param el A validated element
	 * @return The whole number or null if the element is empty
	 */
	public static Integer getInteger(TextElement el) {
		String value = el.getValue();
		if(!StringHelper.containsNonWhitespace(value)) {
			return null;
		}
		return Integer.valueOf(value.trim());
	}

	/**
	 * @param organisations The organisations, may be null
	 * @return The display names, sorted and separated by commas, or null
	 */
	public static String getOrganisationNames(Collection<? extends Organisation> organisations) {
		if(organisations == null || organisations.isEmpty()) {
			return null;
		}
		return organisations.stream()
				.map(Organisation::getDisplayName)
				.filter(StringHelper::containsNonWhitespace)
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.joining(", "));
	}
}
