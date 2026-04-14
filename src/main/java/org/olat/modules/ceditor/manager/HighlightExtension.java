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

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * CommonMark extension that adds the {@code ==text==} highlight syntax,
 * rendered as {@code <mark>text</mark>}.
 *
 * This syntax is common in MkDocs Material ({@code pymdownx.mark}),
 * Pandoc ({@code mark} extension), Obsidian and Notion.
 *
 * Initial date: 2026-04-13<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class HighlightExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

	private HighlightExtension() {
		//
	}

	public static Extension create() {
		return new HighlightExtension();
	}

	@Override
	public void extend(Parser.Builder parserBuilder) {
		parserBuilder.customDelimiterProcessor(new HighlightDelimiterProcessor());
	}

	@Override
	public void extend(HtmlRenderer.Builder rendererBuilder) {
		rendererBuilder.nodeRendererFactory(HighlightHtmlNodeRenderer::new);
	}
}
