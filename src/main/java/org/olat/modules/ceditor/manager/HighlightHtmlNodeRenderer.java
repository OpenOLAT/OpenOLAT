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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

/**
 * Renders {@link Highlight} nodes as {@code <mark>...</mark>}.
 *
 * Initial date: 2026-04-13<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class HighlightHtmlNodeRenderer implements NodeRenderer {

	private final HtmlNodeRendererContext context;
	private final HtmlWriter html;

	public HighlightHtmlNodeRenderer(HtmlNodeRendererContext context) {
		this.context = context;
		this.html = context.getWriter();
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return Collections.singleton(Highlight.class);
	}

	@Override
	public void render(Node node) {
		Map<String, String> attributes = context.extendAttributes(node, "mark", Map.of());
		html.tag("mark", attributes);
		Node child = node.getFirstChild();
		while (child != null) {
			Node next = child.getNext();
			context.render(child);
			child = next;
		}
		html.tag("/mark");
	}
}
