/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.core.gui.components;

import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Empty implementation of the ComponentRenderer
 * 
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public abstract class DefaultComponentRenderer implements ComponentRenderer {

	@Override
	public final void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		
		String layout = layout(source, args);
		switch(layout) {
			case "0_12":
				renderBootstrapLayout(renderer, sb, source, 12, layout, ubu, translator, renderResult, args);
				break;
			case "2_10":
				renderBootstrapLayout(renderer, sb, source, 10, layout, ubu, translator, renderResult, args);
				break;
			case "3_9":
				renderBootstrapLayout(renderer, sb, source, 9, layout, ubu, translator, renderResult, args);
				break;
			case "6_6":
				renderBootstrapLayout(renderer, sb, source, 6, layout, ubu, translator, renderResult, args);
				break;
			case "9_3":
				renderBootstrapLayout(renderer, sb, source, 3, layout, ubu, translator, renderResult, args);
				break;
			case "tr":
				renderTrLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
				break;
			case "vertical":
				renderVerticalLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
				break;
			case "horizontal":
				renderHorizontalLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
				break;
			case "minimal":
				renderMinimalLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
				break;
			case "tablecell":
				renderTableCellLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
				break;
			case "label":
				renderLabel(sb, source, "label", translator, args);
				break;
			case "error":
				renderError(sb, source);
				break;
			case "example":
				renderExample(sb, source);
				break;
			case "inputgroupaddon":
				renderInputGroupAddonLayout(renderer, sb, source, ubu, translator, renderResult, args);
				break;
			default:// nolayout
				renderNoLayout(renderer, sb, source, ubu, translator, renderResult, args);
				break;
		}
	}
	
	public abstract void renderComponent(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args);
	
	protected String layout(Component component, String[] args) {
		boolean hasArgs = (args != null && args.length > 0 && args[0] != null);
		if(hasArgs) {
			String arg = args[0];
			if("label".equals(arg) || "error".equals(arg) || "example".equals(arg)) {
				return arg;
			}
		}
		
		if(component.getLayout() != null) {
			return component.getLayout();
		}

		if(hasArgs) {
			String arg = args[0];
			switch(arg) {
				case "2_10", "3_9", "6_6", "9_3", "tr", "vertical", "horizontal", "minimal", "tablecell", "inputgroupaddon", "nolayout":
					component.setLayout(arg);
					return arg;
				default:
					return "";
			}
		}
		return "";
	}
	
	private void renderInputGroupAddonLayout(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		String elementCssClass = source.getElementCssClass();
		boolean addWrapper = (elementCssClass == null || !elementCssClass.contains("input-group-add"));
		if(addWrapper) {
			sb.append("<span class='input-group-addon'>");
		}
		renderComponent(renderer, sb, source, ubu, translator, renderResult, args);
		if(addWrapper) {
			sb.append("</span>");
		}
	}
	
	private void renderNoLayout(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		renderComponent(renderer, sb, source, ubu, translator, renderResult, args);
	}
	
	protected void renderVerticalLayout(Renderer renderer, StringOutput sb, Component source, String layout,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		renderMinimalLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
	}
	
	protected void renderHorizontalLayout(Renderer renderer, StringOutput sb, Component source, String layout,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		renderMinimalLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
	}
	
	protected void renderTableCellLayout(Renderer renderer, StringOutput sb, Component source, String layout,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		renderMinimalLayout(renderer, sb, source, layout, ubu, translator, renderResult, args);
	}
	
	protected void renderMinimalLayout(Renderer renderer, StringOutput sb, Component source, String layout,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

		Item item = new Item(source);
		String wrapperTagName = renderOpenFormComponent(sb, source, layout, item);

		if(item.hasLabel() && !"tablecell".equals(layout)) {
			renderLabel(sb, (FormBaseComponent)source, layout, translator, new String[] { item.getFormDispatchId() } );
		}
		renderComponent(renderer, sb, source, ubu, translator, renderResult, args);
		renderErrorWarningMarker(sb, item.hasError(), item.hasWarning());
		if(item.hasExample()) {
			sb.append("<div class=\"o_form_example help-block\">");
			renderExample(sb, item.getFormItem());
			sb.append("</div>");
		}
		if(item.hasError() || item.hasWarning()) {
			renderError(sb, item.getFormItem(),  item.hasError(), item.hasWarning());
		} else if (item.isSuccess()) {
			renderSuccess(sb, item.getFormItem(), item.isSuccess());
		}

		renderCloseFormComponent(sb, wrapperTagName);
	}
	
	private void renderTrLayout(Renderer renderer, StringOutput sb, Component source, String layout,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		
		Item item = new Item(source);
		String wrapperTagName = renderOpenFormComponent(sb, source, layout, item);
		
		if(item.hasLabel()) {
			sb.append("<th class='col-left'>");
			renderLabel(sb, (FormBaseComponent)source, layout, translator, new String[] { item.getFormDispatchId() } );
			sb.append("</th>");
		}
		
		sb.append("<td class='col-right'>");
		renderComponent(renderer, sb, source, ubu, translator, renderResult, args);
		renderErrorWarningMarker(sb,  item.hasError(), item.hasWarning());

		// example
		if(item.hasExample()) {
			sb.append("<div class=\"o_form_example help-block\">");
			renderExample(sb, item.getFormItem());
			sb.append("</div>");
		}
		
		if( item.hasError() || item.hasWarning()) {
			sb.append("<div class=\"o_form_example\">");
			renderError(sb, item.getFormItem(),  item.hasError(), item.hasWarning());
			sb.append("</div>");
		}
		sb.append("</td>");
		renderCloseFormComponent(sb, wrapperTagName);
	}

	private void renderBootstrapLayout(Renderer renderer, StringOutput sb, Component source, int fieldWidth, String layout,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		
		Item item = new Item(source);
		int labelWidth = 12 - fieldWidth;
		
		String wrapperTagName = renderOpenFormComponent(sb, source, layout, item);
		
		if(item.hasLabel() && labelWidth > 0) {
			renderLabel(sb, (FormBaseComponent)source, layout, translator, new String[] { item.getFormDispatchId(), "col-sm-" + labelWidth } );
		}

		sb.append("<div class='col-sm-").append(fieldWidth).append(" col-sm-offset-" + labelWidth, !item.hasLabel()).append(" o_form_cell'>");
		renderComponent(renderer, sb, source, ubu, translator, renderResult, args);
		renderErrorWarningMarker(sb,  item.hasError(), item.hasWarning());
		sb.append("</div>");

		// example
		if(item.hasExample()) {
			sb.append("<div class=\"o_form_example help-block col-sm-offset-").append(labelWidth).append(" col-sm-").append(fieldWidth).append("\">");
			renderExample(sb, item.getFormItem());
			sb.append("</div>");
		}
		
		if( item.hasError() || item.hasWarning()) {
			sb.append("<div class=\"o_form_example col-sm-offset-").append(labelWidth).append(" col-sm-").append(fieldWidth).append("\">");
			renderError(sb, item.getFormItem(),  item.hasError(), item.hasWarning());
			sb.append("</div>");
		}
		renderCloseFormComponent(sb, wrapperTagName);
	}
	
	protected String renderOpenFormComponent(StringOutput sb, Component source, String layout, Item item) {
		String tag;
		if("tr".equals(layout)) {
			tag = "tr";
		} else {
			tag = "div";
		}
		return renderOpenFormComponent(sb, tag, source, layout, item.getElementCssClass(), item.hasError(), item.hasWarning(), item.hasFeedback());
	}
	
	protected final String renderOpenFormComponent(StringOutput sb, String tag, Component component,
			String layout, String elementCssClass, boolean hasError, boolean hasWarning, boolean hasFeedback) {
		boolean domReplacementWrapperRequired = component.isDomReplacementWrapperRequired();
		sb.append("<").append(tag);
		if(!domReplacementWrapperRequired) {
			sb.append(" id='o_c").append(component.getDispatchID()).append("'");
		}
		sb.append(" class='");
		if(!layout.equals("tablecell")) {
			sb.append("form-group clearfix");
		}
		if(StringHelper.containsNonWhitespace(elementCssClass)) {
			sb.append(" ").append(elementCssClass);
		}
		if(hasError) {
			sb.append(" has-feedback has-error");
		} else if(hasWarning) {
			sb.append(" has-feedback has-warning");
		} else if(hasFeedback) {
			sb.append(" has-feedback");
		}
		sb.append("'>");
		return tag;
	}
	
	protected void renderCloseFormComponent(StringOutput sb, String tag) {
		sb.append("</").append(tag).append(">");
	}
	
	private void renderExample(StringOutput sb, Component component) {
		if(component instanceof FormBaseComponent fComponent) {
			FormItem item = fComponent.getFormItem();
			renderExample(sb, item);
		}
	}
	
	private void renderExample(StringOutput sb, FormItem item) {
		String text = item.getExampleText();
		if(text != null) {
			sb.append(text);
		}
	}

	private void renderSuccess(StringOutput sb, FormItem item, boolean isSuccess) {
		if (isSuccess) {
			sb.append("<div id='").append(item.getFormDispatchId()).append("_success' class='o_success_with_icon'>")
					.append(item.getSuccessText())
					.append("</div>");
		}
	}
	
	private void renderError(StringOutput sb, Component component) {
		if(component instanceof FormBaseComponent fComponent) {
			FormItem item = fComponent.getFormItem();
			renderError(sb, item, item.hasError(), item.hasWarning()); 
		}
	}
	
	protected void appendErrorAriaDescribedby(StringOutput sb, FormItem item) {
		if(item.hasError() || item.hasWarning() || item.isSuccess()) {
			String suffix;
			if(item.hasError()) {
				suffix = "_error";
			} else if(item.hasWarning()) {
				suffix = "_warning";
			} else if (item.isSuccess()) {
				suffix = "_success";
			} else {
				suffix = "";
			}
			sb.append(" aria-describedby='").append(item.getFormDispatchId()).append(suffix).append("'");
		}
	}
	
	protected void renderError(StringOutput sb, FormItem item, boolean hasError, boolean hasWarning) {
		if(hasError) {
			sb.append("<div id='").append(item.getFormDispatchId()).append("_error' class='o_error'>")
			  .append(item.getErrorText())
			  .append("</div>");
		}
		if(hasWarning) {
			sb.append("<div id='").append(item.getFormDispatchId()).append("_warning' class='o_warning'>")
			  .append(item.getWarningText())
			  .append("</div>");
		}
	}

	private void renderLabel(StringOutput sb, Component component, String layout, Translator translator, String[] args) {
		if(component instanceof FormBaseComponent fComponent) {
			renderLabel(sb, fComponent, layout, translator, args);
		}
	}
	
	protected void renderLabel(StringOutput sb, FormBaseComponent component,
			@SuppressWarnings("unused") String layout, Translator translator, String[] args) {
		renderLabel(sb, "label", component, translator, args);
	}
	
	protected final void renderLabel(StringOutput sb, String tagName, FormBaseComponent component, Translator translator, String[] args) {
		sb.append("<").append(tagName).append(" class='control-label ");
		if (args !=  null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("col-")) {
					sb.append(arg);
				}
			}
		}
		sb.append("' id='o_cl").append(component.getDispatchID()).append("'");
		// add the reference to form element for which this label stands. this is important for screen readers
		if (component.getFormItem() != null) {
			String forId = component.getFormItem().getForId();
			if(forId != null && tagName.equals("label")) {
				sb.append(" for=\"").append(forId).append("\"");
			}
		}
		if (component.getFormItem().isMandatory()) {
			sb.append(" aria-required='true'");
			
		}
		sb.append(">");
		if (component.getFormItem().isMandatory()) {
			String hover = translator.translate("form.mandatory.hover");
			sb.append("<i class='o_icon o_icon_mandatory' title='").append(hover).append("' aria-hidden='true'></i> ");
		}
		
		String text = component.getFormItem().getLabelText();
		if (StringHelper.containsNonWhitespace(text)) {
			sb.append(text);
		}
		
		String helpText = component.getFormItem().getHelpText();
		String helpUrl = component.getFormItem().getHelpUrl();
		
		// component help is optional, can be text or link or both
		if (helpText != null || helpUrl != null) {
			String helpIconId = "o_fh" + component.getDispatchID();
			// Wrap tooltip with link to external url if available
			if (helpUrl != null) {
				sb.append("<a href=\"").append(helpUrl).append("\" target='_blank'>"); 
			}
			// tooltip is bound to this icon
			sb.append("<i class='o_form_chelp o_icon o_icon-fw o_icon_help help-block' id='").append(helpIconId).append("'></i>");
			if (helpUrl != null) {
				sb.append("</a>");
			}			
			// Attach bootstrap tooltip handler to help icon
			boolean right = "vertical".equals(component.getLayout());
			String placement = right ? "right" : "top";
			sb.append("<script>\n")
			  .append("\"use strict\"\n")
			  .append("jQuery(function () {jQuery('#").append(helpIconId).append("').tooltip({placement:\"").append(placement).append("\",container: \"#").append(helpIconId).append("\",html:true,title:\"");
			if (helpText != null) {
				sb.append(StringHelper.escapeJavaScript(helpText));
			}
			if (helpUrl != null) {
				if (text != null) {
					// append spacer between custom and generic link text
					sb.append("<br>");
				}
				sb.append(translator.translate("help.tooltip.link", "<i class='o_icon o_icon-fw o_icon_help'></i>"));					
			}
			sb.append("\"});})</script>");		
		}
		sb.append("</").append(tagName).append(">");
	}
	
	private void renderErrorWarningMarker(StringOutput sb, boolean hasError, boolean hasWarning) {
		if(hasError) {
			sb.append("<span class=\"o_icon o_icon_error form-control-feedback\"></span>");
		} else if(hasWarning) {
			sb.append("<span class=\"o_icon o_icon_warn form-control-feedback\"></span>");
		}
	}
	
	public static class Item {
		
		private final FormItem formItem;
		private final boolean hasError;
		private final boolean hasWarning;
		private final boolean isSuccess;
		private final boolean hasLabel;
		private final boolean hasExample;
		private final boolean hasFeedback;
		private final String elementCssClass;
		
		public Item(Component source) {
			if(source instanceof FormBaseComponent formComponent) {
				formItem = formComponent.getFormItem();
			} else {
				formItem = null;
			}
				
			if(formItem != null) {
				hasError = formItem.hasError();
				hasWarning = formItem.hasWarning();
				isSuccess = formItem.isSuccess();
				hasLabel = formItem.hasLabel();
				hasExample = formItem.hasExample();
				hasFeedback = formItem.hasFeedback();
				elementCssClass = formItem.getElementCssClass();
			} else {
				hasError = false;
				hasWarning = false;
				isSuccess = false;
				hasLabel = false;
				hasExample = false;
				hasFeedback = false;
				elementCssClass = source.getElementCssClass();
			}
		}

		public FormItem getFormItem() {
			return formItem;
		}
		
		public String getFormDispatchId() {
			return formItem == null ? null : formItem.getFormDispatchId();
		}
		
		public String getElementCssClass() {
			return elementCssClass;
		}

		public boolean hasError() {
			return hasError;
		}

		public boolean hasWarning() {
			return hasWarning;
		}

		public boolean isSuccess() {
			return isSuccess;
		}

		public boolean hasLabel() {
			return hasLabel;
		}

		public boolean hasExample() {
			return hasExample;
		}
		
		public boolean hasFeedback() {
			return hasFeedback;
		}
	}
}
