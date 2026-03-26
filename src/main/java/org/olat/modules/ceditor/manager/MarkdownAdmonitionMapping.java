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
package org.olat.modules.ceditor.manager;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.BlockQuote;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;
import org.olat.modules.ceditor.model.AlertBoxType;

/**
 * Maps GitHub-style admonition markers ([!TYPE]) in blockquotes
 * to AlertBoxType values and titles.
 *
 * Initial date: 2026-03-16<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownAdmonitionMapping {

	static final Pattern ADMONITION_PATTERN = Pattern.compile("^\\[!(\\w+)]$");

	private static final Map<String, AlertBoxType> TYPE_MAPPING = Map.ofEntries(
		Map.entry("NOTE", AlertBoxType.note),
		Map.entry("TIP", AlertBoxType.tip),
		Map.entry("IMPORTANT", AlertBoxType.important),
		Map.entry("WARNING", AlertBoxType.warning),
		Map.entry("CAUTION", AlertBoxType.error),
		Map.entry("INFO", AlertBoxType.info),
		Map.entry("SUCCESS", AlertBoxType.success),
		Map.entry("ERROR", AlertBoxType.error)
	);

	public record AdmonitionResult(AlertBoxType type) {}

	/**
	 * Detect a GitHub-style admonition marker in a BlockQuote node.
	 * If found, removes the marker text and following SoftLineBreak from the AST
	 * so they don't appear in rendered output.
	 *
	 * @param blockQuote the BlockQuote AST node
	 * @return the detected admonition result, or null if no admonition marker found
	 */
	public static AdmonitionResult detectAdmonition(BlockQuote blockQuote) {
		Node firstChild = blockQuote.getFirstChild();
		if (!(firstChild instanceof Paragraph paragraph)) {
			return null;
		}
		Node firstInline = paragraph.getFirstChild();
		if (!(firstInline instanceof Text text)) {
			return null;
		}

		Matcher matcher = ADMONITION_PATTERN.matcher(text.getLiteral());
		if (!matcher.matches()) {
			return null;
		}

		String typeKey = matcher.group(1).toUpperCase();
		AlertBoxType alertType = TYPE_MAPPING.get(typeKey);
		if (alertType == null) {
			return null;
		}

		// Remove the marker text node and the following SoftLineBreak from the AST
		removeAdmonitionMarker(text);

		return new AdmonitionResult(alertType);
	}

	/**
	 * Remove the admonition marker Text node and its following SoftLineBreak
	 * from the AST.
	 */
	static void removeAdmonitionMarker(Text markerNode) {
		Node next = markerNode.getNext();
		markerNode.unlink();
		if (next instanceof SoftLineBreak) {
			next.unlink();
		}
	}

	private MarkdownAdmonitionMapping() {
		// utility class
	}
}
