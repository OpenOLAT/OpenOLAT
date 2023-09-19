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
import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection.Layout;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer.FormLayout;
import org.olat.core.gui.components.form.flexible.impl.elements.SingleSelectionComponent.RadioElementComponent;
import org.olat.core.gui.components.util.SelectionValues.Image;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleSelectionRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		SingleSelectionComponent teC = (SingleSelectionComponent) source;
		Layout layout = teC.getFormItem().getLayout();
		if(layout == Layout.vertical) {
			renderVertical(sb, teC);
		} else {
			renderHorizontal(sb, teC, translator);
		}
	}
	
	@Override
	protected String renderOpenFormComponent(StringOutput sb, Component source, String layout, Item item) {
		SingleSelectionImpl stF = (SingleSelectionImpl)item.getFormItem();
		if(stF.singleCheckWithoutValue() || FormLayout.LAYOUT_TABLE_CONDENSED.layout().equals(layout)) {
			return super.renderOpenFormComponent(sb, source, layout, item);
		}
		return renderOpenFormComponent(sb, "fieldset", source, layout, item.getElementCssClass(), item.hasError(), item.hasWarning());
	}

	@Override
	protected void renderLabel(StringOutput sb, FormBaseComponent component, String layout, Translator translator, String[] args) {
		SingleSelectionImpl stF = (SingleSelectionImpl)component.getFormItem();
		if(stF.singleCheckWithoutValue()
				|| FormLayout.LAYOUT_TABLE_CONDENSED.layout().equals(layout)
				|| "label".equals(layout)) {
			super.renderLabel(sb, component, layout, translator, args);
		} else {
			renderLabel(sb, "legend", component, translator, args);
		}
	}
	
	private void renderVertical(StringOutput sb, SingleSelectionComponent source) {
		RadioElementComponent[] radios = source.getRadioComponents();
		String css = source.getElementCssClass();
		SingleSelectionImpl ssF = source.getFormItem();
		boolean hasCss = css != null || ssF.isRenderAsCard() || ssF.isRenderAsButtonGroup();

		if (hasCss) {
			sb.append("<div class=\"")
				.append(css, css != null)
				.append(" o_radio_cards_wrapper", ssF.isRenderAsCard())
				.append(" o_radio_buttons btn-group-vertical", ssF.isRenderAsButtonGroup())
				.append("\"")
				.append("data-toggle=\"buttons\"", ssF.isRenderAsButtonGroup())
				.append(">");
		}
		if (source.getFormItem().isRenderAsCard()) {
			sb.append("<div class=\"o_radio_cards ")
				.append(css, css != null)
				.append("\"")
				.append(">");
		}
		
		for(RadioElementComponent radio:radios) {
			renderRadio(sb, source, radio, false);
		}
		
		if (source.getFormItem().isRenderAsCard()) {
			sb.append("</div>");
		}
		sb.append("</div>", hasCss);
	}
	
	private void renderHorizontal(StringOutput sb, SingleSelectionComponent source, Translator translator) {
		String css = source.getElementCssClass();
		
		String wrapperId = source.getFormDispatchId() + "_wr";
		sb.append("<div id=\"").append(wrapperId).append("\" ")
			.append(" class=\"form-inline ")
			.append("o_radio_cards_wrapper ", source.getFormItem().isRenderAsCard())
			.append("o_radio_cards_unwrapped ", source.getFormItem().isShowMoreCards())
			.append("o_radio_card_top_to_bottom ", source.getFormItem().isShowMoreCards())
			.append("o_radio_buttons btn-group ", source.getFormItem().isRenderAsButtonGroup())
			.append(css, css != null)
			.append("\"")
			.append("data-toggle=\"buttons\"", source.getFormItem().isRenderAsButtonGroup())
			.append(">");
		if (source.getFormItem().isRenderAsCard()) {
			sb.append("<div class=\"o_radio_cards ")
				.append("o_radio_card_num_" + source.getRadioComponents().length + " ", source.getFormItem().isShowMoreCards())
				.append(css, css != null)
				.append("\"")
				.append(">");
		}
		
		RadioElementComponent[] radios = source.getRadioComponents();
		
		for(RadioElementComponent radio:radios) {
			renderRadio(sb, source, radio, true);
		}
		
		if (source.getFormItem().isRenderAsCard()) {
			sb.append("</div>");
			if (source.getFormItem().isShowMoreCards()) {
				sb.append("<div class=\"o_show_more_radios\"><a ");
				sb.append("href=\"javascript:;\" onclick=\"document.getElementById('").append(wrapperId).append("').classList.remove('o_radio_cards_unwrapped');return false;\"");
				sb.append(">").append(translator.translate(source.getFormItem().getShowMoreCardsI18nKey())).append("</a></div>");
			}
		}
		sb.append("</div>");
	}
	
	private void renderRadio(StringOutput sb, SingleSelectionComponent source, RadioElementComponent ssec, boolean inline) {
		String subStrName = "name='" + ssec.getGroupingName() + "'";
		String key = ssec.getKey();
		String value = ssec.getValue();
		String formDispatchId = ssec.getFormDispatchId();
		
		boolean buttonGroupStyle = source.getFormItem().isRenderAsButtonGroup();
		boolean cardStyle = source.getFormItem().isRenderAsCard();
		boolean hasCustomCss = StringHelper.containsNonWhitespace(ssec.getCustomCssClass());
		boolean disabled = !ssec.isEnabled();
		boolean selected = ssec.isSelected();
		
		// read write view
		sb.append("<div class='radio ", !inline) // normal radios need a wrapper (bootstrap) ...
		  .append("btn btn-default ", !inline && buttonGroupStyle)
		  .append(ssec.getCustomCssClass(), !inline && buttonGroupStyle && hasCustomCss)
		  .append(" active", !inline && buttonGroupStyle && selected)
		  .append("' ", !inline)
		  .append("disabled ", !inline && disabled);

		if(!inline && source.getWidthInPercent() > 0) {
			sb.append("style='width:").append(source.getWidthInPercent()).append("%;'");
		}
		sb.append(">", !inline)
		  .append("<label ").append("class='", inline || source.getFormItem().isRenderAsButtonGroup())
		  					.append("radio-inline ", inline)			// ... and inline a class on the label (bootstrap)
		  					.append("btn btn-default ", inline && buttonGroupStyle)
		  					.append(ssec.getCustomCssClass(), inline && (buttonGroupStyle || cardStyle) && hasCustomCss)
		  					.append(" active", inline && buttonGroupStyle && selected)
		  					.append("' ", inline || source.getFormItem().isRenderAsButtonGroup())
		  					.append("disabled ", inline && disabled);
		
		if(inline && source.getWidthInPercent() > 0) {
			sb.append("style='width:").append(source.getWidthInPercent()).append("%;'");
		}
		
		sb.append(" for=\"").append(formDispatchId).append("\"")
		  .append(" onmousedown=\"o_info.lastFormFocusEl='").append(formDispatchId).append("';\"")
		  .append(">");
		sb.append("<input id='").append(formDispatchId).append("' ")
		  .append("type='radio' ").append(subStrName)
		  .append(" value='").append(key).append("' ")
		  .append(" checked='checked' ", selected)
		  .append(" disabled ", disabled);

		if(source.isEnabled()){
			// use the selection elements formDispId instead of the one of this element.
			sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction(), false, null, formDispatchId));
			//TODO editor force focus to move on
			sb.append(" onmousedown=\"o_info.lastFormFocusEl='").append(formDispatchId).append("';\"");
		} else {
			//mark as disabled and do not add javascript
			sb.append(" disabled='disabled' ");
		}
		sb.append(">");
		
		if (source.getFormItem().isRenderAsCard()) {
			// Card style rendering
			sb.append("<span class='o_radio_card'><span class='o_radio_text_wrapper'>");
			if (StringHelper.containsNonWhitespace(value)) {
				sb.append(" <span class='o_radio_label'>");
				if(source.isEscapeHtml()) {
					sb.append(StringHelper.escapeHtml(value));
				} else {
					sb.append(value);
				}
				sb.append(" </span>");
			}
			String desc = ssec.getDescription();
			if (StringHelper.containsNonWhitespace(desc)) {
				sb.append(" <span class='o_radio_desc'>");
				if(source.isEscapeHtml()) {
					sb.append(StringHelper.escapeHtml(desc));
				} else {
					sb.append(desc);
				}
				sb.append(" </span>");
			}
			sb.append("</span>"); // END o_radio_text_wrapper
			String iconCssClass = ssec.getIconCssClass();
			if (StringHelper.containsNonWhitespace(iconCssClass)) {
				sb.append(" <span class='o_radio_icon ").append(iconCssClass).append("'> </span>");
			}
			Image image = ssec.getImage();
			if(image != null) {
				sb.append(" <img src='").append(image.url()).append("' width='").append(image.size().getWidth())
				  .append("' height='").append(image.size().getHeight()).append("'>");
			}
			sb.append("</span>"); // END o_radio_card
			
		} else {
			// Standard radio button rendering
			if (StringHelper.containsNonWhitespace(value)) {
				if(source.isEscapeHtml()) {
					sb.append(StringHelper.escapeHtml(value));
				} else {
					sb.append(value);
				}
			} else if(inline) {
				// at least something in label required for properly aligned rendering, nbsp is important for bootstrap
				sb.append("&nbsp;"); 
			}
		}
		
		if(source.isEnabled()){
			// add set dirty form only if enabled
			// must be placed within label to make multiple radio-inline rules of bootstrap match 
			FormJSHelper.appendFlexiFormDirtyForCheckbox(sb, ssec.getRootForm(), formDispatchId);

			if(ssec.getRootForm().isInlineValidationOn() || source.getFormItem().isInlineValidationOn()) {
				FormJSHelper.appendValidationListeners(sb, ssec.getRootForm(), formDispatchId, source.getFormItem().getFormDispatchId());
			}
		}
		
		sb.append("</label>")
		  .append("</div>", !inline) // normal radios need a wrapper (bootstrap)
		  .append(" ", source.isTrailingSpace());
	}
	
	public void renderSingleRadio(StringOutput sb, Component source, int index) {
		SingleSelectionComponent teC = (SingleSelectionComponent) source;
		RadioElementComponent[] radios = teC.getRadioComponents();
		if(index >= 0 && index < radios.length) {
			RadioElementComponent ssec = radios[index];
			String formDispatchId = ssec.getFormDispatchId();
			sb.append("<input id='").append(formDispatchId).append("'")
			  .append(" type='radio' name='").append(ssec.getGroupingName()).append("'")
			  .append(" value='").append(ssec.getKey()).append("' ")
			  .append(" checked='checked' ", ssec.isSelected())
			  .append(" disabled ", !ssec.isEnabled());
			sb.append(">");
			FormJSHelper.appendFlexiFormDirtyForCheckbox(sb, ssec.getRootForm(), formDispatchId);
		}
	}
}