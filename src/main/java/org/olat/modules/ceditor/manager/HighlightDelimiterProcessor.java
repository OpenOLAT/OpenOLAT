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

import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

/**
 * Delimiter processor for highlighted text using the {@code ==text==}
 * syntax (Pandoc mark / MkDocs pymdownx.mark / Obsidian).
 *
 * Requires exactly two equal signs on each side. A single {@code =}
 * is left as literal text.
 *
 * Initial date: 2026-04-13<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class HighlightDelimiterProcessor implements DelimiterProcessor {

	@Override
	public char getOpeningCharacter() {
		return '=';
	}

	@Override
	public char getClosingCharacter() {
		return '=';
	}

	@Override
	public int getMinLength() {
		return 2;
	}

	@Override
	public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
		if (openingRun.length() >= 2 && closingRun.length() >= 2) {
			Text opener = openingRun.getOpener();

			Node highlight = new Highlight();
			SourceSpans sourceSpans = new SourceSpans();
			sourceSpans.addAllFrom(openingRun.getOpeners(2));

			for (Node node : Nodes.between(opener, closingRun.getCloser())) {
				highlight.appendChild(node);
				sourceSpans.addAll(node.getSourceSpans());
			}

			sourceSpans.addAllFrom(closingRun.getClosers(2));
			highlight.setSourceSpans(sourceSpans.getSourceSpans());

			opener.insertAfter(highlight);

			return 2;
		}
		return 0;
	}
}
