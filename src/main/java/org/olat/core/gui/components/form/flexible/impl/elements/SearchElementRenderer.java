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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SearchElement;
import org.olat.core.gui.components.form.flexible.elements.SearchVariant;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Renders the three variants of {@link SearchElement} with a single markup
 * contract: a wrapper carrying role="search", the input, the reset button
 * overlaid on the input, an optional search button, and an optional result
 * count region.
 *
 * Initial date: 2026-08-25<br>
 * @author uhensler, https://www.frentix.com
 */
public class SearchElementRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		SearchElementComponent cmp = (SearchElementComponent)source;
		SearchElementImpl searchEl = (SearchElementImpl)cmp.getFormItem();
		renderSearch(renderer, sb, cmp, searchEl);
	}

	private void renderSearch(Renderer renderer, StringOutput sb, SearchElementComponent cmp, SearchElementImpl searchEl) {

		String variantCss = switch (searchEl.getVariant()) {
			case TYPEAHEAD -> "o_search_typeahead";
			case DEFAULT -> "o_search_default";
			case LARGE -> "o_search_large";
		};

		sb.append("<div class='o_search ").append(variantCss);
		if (StringHelper.containsNonWhitespace(cmp.getElementCssClass())) {
			sb.append(" ").append(cmp.getElementCssClass());
		}
		sb.append("' role='search'>");

		boolean searchButtonVisible = searchEl.isSearchButtonVisible();

		sb.append("<div class='input-group'>");
		if (!searchButtonVisible) {
			sb.append("<span class='input-group-addon'><i class='o_icon o_icon-fw o_icon_search'></i></span>");
		}
		sb.append("<div class='o_search_input_wrapper'>");
		renderer.render(searchEl.getSearchInput().getComponent(), sb, null);
		boolean hasText = StringHelper.containsNonWhitespace(searchEl.getValue());
		sb.append("<span id='").append(cmp.getDispatchID()).append("_resetWrap'");
		if (!hasText) {
			sb.append(" style='display:none'");
		}
		sb.append(">");
		renderer.render(searchEl.getResetButton().getComponent(), sb, null);
		sb.append("</span>");
		sb.append("</div>");
		if (searchButtonVisible) {
			sb.append("<span class='input-group-btn'>");
			renderer.render(searchEl.getSearchButton().getComponent(), sb, null);
			sb.append("</span>");
		}
		sb.append("</div>");

		sb.append("</div>");

		appendKeyScript(sb, cmp, searchEl);
	}

	/**
	 * Enter triggers the search by XHR instead of a form submit; a form with a
	 * single text input submits on Enter by browser default otherwise. Escape
	 * clears the field when the reset button is available.
	 */
	private void appendKeyScript(StringOutput sb, SearchElementComponent cmp, SearchElementImpl searchEl) {
		String inputId = searchEl.getSearchInput().getFormDispatchId();
		String searchBtnId = searchEl.getSearchButton().getFormDispatchId();
		String resetWrapId = cmp.getDispatchID() + "_resetWrap";

		sb.append("<script>'use strict';(function() {")
		  .append("var resetWrap = document.getElementById('").append(resetWrapId).append("');")
		  .append("var input = document.getElementById('").append(inputId).append("');")
		  .append("if (input && resetWrap) {")
		  .append("input.addEventListener('input', function() {")
		  .append("resetWrap.style.display = input.value.trim() ? '' : 'none';")
		  .append("});")
		  .append("}")
		  .append("})();</script>");

		sb.append("<script>'use strict';jQuery('#").append(inputId).append("').on('keydown', function(e) {")
		  .append("if(e.which == 13) { e.preventDefault(); e.stopPropagation();")
		  .append(FormJSHelper.getXHRFnCallFor(searchEl.getRootForm(), searchBtnId, 1, false, false, true))
		  .append("; return false; }");

		if (searchEl.getVariant() == SearchVariant.TYPEAHEAD) {
			String resetBtnId = searchEl.getResetButton().getFormDispatchId();
			sb.append(" else if(e.which == 27) { e.preventDefault(); e.stopPropagation();")
			  .append(FormJSHelper.getXHRFnCallFor(searchEl.getRootForm(), resetBtnId, 1, false, false, true))
			  .append("; return false; }");
		}

		sb.append("});</script>");

		if (searchEl.getVariant() == SearchVariant.TYPEAHEAD) {
			appendDebounceScript(sb, inputId);
		}
	}

	/**
	 * A plain (non-autocompleter) typeahead field fires its inline onkeyup
	 * handler on every keystroke with no coordination between the resulting
	 * requests. Two overlapping requests can have their responses applied out
	 * of order, leaving the reset button, result list or count out of sync
	 * with the text actually in the field. Debouncing to one request per
	 * pause make this race practically unreachable for real typing.
	 */
	private void appendDebounceScript(StringOutput sb, String inputId) {
		sb.append("<script>'use strict';(function() {")
		  .append("var el = document.getElementById('").append(inputId).append("');")
		  .append("if (!el) { return; }")
		  .append("var nativeOnkeyup = el.getAttribute('onkeyup');")
		  .append("if (!nativeOnkeyup) { return; }")
		  .append("el.removeAttribute('onkeyup');")
		  .append("var timer = null;")
		  .append("el.addEventListener('keyup', function(e) {")
		  .append("if (!e.isTrusted) { return; }")
		  .append("if (e.which == 13 || e.which == 27) { return; }")
		  .append("if (timer) { window.clearTimeout(timer); }")
		  .append("timer = window.setTimeout(function() {")
		  .append("timer = null;")
		  .append("el.setAttribute('onkeyup', nativeOnkeyup);")
		  .append("el.dispatchEvent(new KeyboardEvent('keyup', {bubbles: true, cancelable: true}));")
		  .append("el.removeAttribute('onkeyup');")
		  .append("}, 300);")
		  .append("});")
		  .append("})();</script>");
	}

}
