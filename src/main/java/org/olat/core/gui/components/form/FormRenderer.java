/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.form;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.formelements.CheckBoxElement;
import org.olat.core.gui.formelements.FormElement;
import org.olat.core.gui.formelements.HTMLTextAreaElement;
import org.olat.core.gui.formelements.LinkElement;
import org.olat.core.gui.formelements.MultipleSelectionElement;
import org.olat.core.gui.formelements.PasswordElement;
import org.olat.core.gui.formelements.PopupData;
import org.olat.core.gui.formelements.RadioButtonGroupElement;
import org.olat.core.gui.formelements.SelectionElement;
import org.olat.core.gui.formelements.SingleSelectionElement;
import org.olat.core.gui.formelements.SpacerElement;
import org.olat.core.gui.formelements.StaticHTMLTextElement;
import org.olat.core.gui.formelements.StaticTextElement;
import org.olat.core.gui.formelements.TextAreaElement;
import org.olat.core.gui.formelements.TextElement;
import org.olat.core.gui.formelements.TitleElement;
import org.olat.core.gui.formelements.VisibilityDependsOnSelectionRule;
import org.olat.core.gui.formelements.WikiMarkupTextAreaElement;
import org.olat.core.gui.formelements.WikiMarkupTextAreaElementAutoSize;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Formatter;
import org.olat.core.util.i18n.I18nManager;

/**
 * enclosing_type Description:<br>
 * 
 * @author Felix Jost
 */
public class FormRenderer implements ComponentRenderer {
	private static final String READONLYA = "<div class=\"b_form_disabled\">";
	private static final String READONLYB = "</div>";

	// this variables are used in functions.js - do not change
	/** html form id prependix * */
	public static final String JSFORMID = "bfo_";
	/** html element id prependix * */
	public static final String JSELEMENTID = "bel_";
	/** html row id prependix * */
	public static final String JSELEMENTROWID = "ber_";

	/**
	 * This is a singleton! so it may not change instance variables Constructor
	 * for FormRenderer. There must be an empty contructor for the Class.forName()
	 * call
	 */
	public FormRenderer() {
		super();
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		if (translator == null) { // missing translator
			throw new RuntimeException("no translator");
		}
		boolean doNotWrapSubmit = (args != null && args[0].equals("donotwrapsubmit"));

		// precompile form elements into sting buffer
		StringOutput formElementsSO = new StringOutput(4096);
		Form f = (Form) source;
		boolean error = false;
		renderFormHeader(f, formElementsSO, ubu, renderer.getGlobalSettings().getAjaxFlags());		
		
		Iterator it_names = f.getNameIterator();
		while (it_names.hasNext()) {
			String name = (String) it_names.next();
			FormElement fe = f.getFormElement(name);
			if (fe.isError()) {
				error = true;
			}
			renderFormElement(f, fe, formElementsSO, translator, ubu, args);
		}

		if (!f.isDisplayOnly()) {
			if (doNotWrapSubmit) {
				renderFormSubmit(f, formElementsSO, translator);
			} else {
				formElementsSO.append("<div class=\"b_form_element_wrapper b_clearfix\"><div class=\"b_form_element\"><div class=\"b_button_group\">");
				renderFormSubmit(f, formElementsSO, translator);
				formElementsSO.append("</div></div></div>");
			}
			
			//check form
			formElementsSO.append("<script type=\"text/javascript\">\n/* <![CDATA[ */\n");
			formElementsSO.append("function checkform").append(f.getComponentName()).append("(){\n");
			renderElementVisibilityDependencyRules(f, formElementsSO);
			formElementsSO.append("}\n/* ]]> */\n</script>\n");
			
		} 
		renderFormFooter(f, formElementsSO, translator);
		// end precompiling form elements
		
		// now build form wrapper together with errors
		target.append("<div class=\"b_form\">"); // Open form wrapper
		if (error) {
			target.append("<div class=\"b_form_general_error\">" + translator.translate("form.general.error") + "</div>");
		}
		target.append(formElementsSO);
		target.append("</div>"); // Close form wrapper 
	}

	/**
	 * Method renderFormHeader.
	 * 
	 * @param f
	 * @param sb
	 */
	private void renderFormHeader(Form f, StringOutput sb, URLBuilder ubu, AJAXFlags flags) {
		// form header
		sb.append("<form method=\"post\" name=\"");
		sb.append(f.getComponentName());
		sb.append("\" action=\"");

		boolean iframePostEnabled = flags.isIframePostEnabled();

		ubu.buildURI(sb, null, null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\" id=\"");
		sb.append(JSFORMID);
		sb.append(f.hashCode());
		sb.append("\"");
		if (iframePostEnabled) {
			ubu.appendTarget(sb);
		}
		sb.append(" onsubmit=\"if(o_info.linkbusy) return false; else o_beforeserver(); return true;\">");
	}

	/**
	 * @param f
	 * @param sb
	 */
	private void renderFormSubmit(Form f, StringOutput sb, Translator translator) {
		// Submit buttons
		if (f.getSubmitKeysi18n().size() == 0) throw new OLATRuntimeException(null,
				"Submit key in form is undefined. Use setSubmitKey(\"save\"); to set key in the form.", null);
		int counter = 0;
		for (Iterator iter_I18nKeys = f.getSubmitKeysi18n().iterator(); iter_I18nKeys.hasNext();) {
			String i18nKey = (String) iter_I18nKeys.next();
			String name = Form.SUBMIT_IDENTIFICATION + "_" + counter;
			if(f.markSubmit){
				sb.append("<span style=\"border:2px solid red;\">");
			}
			
			sb.append("<input type=\"submit\" name=\"").append(name).append("\" class=\"b_button\" value=\"")
						.append(StringEscapeUtils.escapeHtml(translator.translate(i18nKey))).append("\" />");
			if(f.markSubmit){
				sb.append("</span>");
			}
			
			counter++;
		}
		
		// Cancel buttons
		if (f.getCancelKeyi18n() != null) {
			if(f.markCancel){
				sb.append("<span style=\"border:2px solid red;\">");
			}

			sb.append("<input type=\"submit\" name=\"").append(Form.CANCEL_IDENTIFICATION).append("\" class=\"b_button\" value=\"")
						.append(StringEscapeUtils.escapeHtml(translator.translate(f.getCancelKeyi18n()))).append("\" />");
			
			if(f.markCancel){
				sb.append("</span>");
			}
		}
	}

	/**
	 * Method renderFormFooter.
	 * 
	 * @param f
	 * @param sb
	 * @param translator
	 */
	private void renderFormFooter(Form f, StringOutput sb, Translator translator) {
		sb.append("</form>");
	}

	/**
	 * Method renderFormElement.
	 * 
	 * @param fe
	 * @param sb
	 * @param args
	 */
	private void renderFormElement(Form f, FormElement fe, StringOutput sb, Translator translator, URLBuilder ubu, String[] args) {
		sb.append("<div class=\"b_form_element_wrapper b_clearfix");
		if (fe.isError()) {
			sb.append(" b_form_error");
		}
		if (fe instanceof TextAreaElement) {
//			sb.append(" b_form_element_wrapper_textarea");
		}
		// row id used from within function.js, don't remove this!
		sb.append("\" id=\"").append(JSELEMENTROWID).append(fe.getName() + f.getComponentName()).append("\">");
		
		// deal with special cases first
		if (fe instanceof TitleElement) {
			TitleElement te = (TitleElement) fe;
			sb.append("<h4>").append(translator.translate(te.getTitleKey())).append("</h4>");
			
		} else if (fe instanceof SpacerElement) {
			SpacerElement spe = (SpacerElement) fe;
			if (spe.isHr()) sb.append("<hr class=\"b_form_spacer\" />");
			if (spe.isBr()) sb.append("<div class=\"b_form_spacer\" />");
			
		} else {
			// real form field follow now
			sb.append("<div class=\"b_form_element_label \">");
			
			if (! (fe instanceof RadioButtonGroupElement && ((RadioButtonGroupElement)fe).isNoLabel() )) {
				sb.append("<label for=\"").append(JSELEMENTID).append(fe.hashCode()).append("\">");				
				renderLabel(fe, sb, translator);
				sb.append("</label>");				
			}

			// add icon for mandatory rows
			if (!f.isDisplayOnly()) {
				if (fe.isMandatory()) {
					sb.append("<span class=\"b_form_mandatory\" title=\"");
					sb.append(StringEscapeUtils.escapeHtml(translator.translate("form.mandatory.hover")));
					sb.append("\">&nbsp;</span>");
				}
			}
			sb.append("</div><div class=\"b_form_element");
			if (f.isDisplayOnly()) {
				sb.append(" b_form_element_disponly");
			}
			sb.append("\">");				

			if(fe.getVisualMarked()){
				sb.append("<span style=\"border:2px solid red;\">");
			}
			
			
			if (fe instanceof WikiMarkupTextAreaElement) {
				WikiMarkupTextAreaElement te = (WikiMarkupTextAreaElement) fe;
				renderWikiMarkupTextAreaElementAutoSize(f, te, sb, false);
			} else if (fe instanceof HTMLTextAreaElement) { // must be checked before TextAreaElement !!
				TextAreaElement te = (TextAreaElement) fe;
				renderTextAreaElement(f, te, sb, true, args);
			} else if (fe instanceof WikiMarkupTextAreaElementAutoSize) { 
				TextAreaElement te = (TextAreaElement) fe;
				renderWikiMarkupTextAreaElementAutoSize(f, te, sb, true);
			} else if (fe instanceof TextAreaElement) {
				TextAreaElement te = (TextAreaElement) fe;
				renderTextAreaElement(f, te, sb, false, args);
			} else if (fe instanceof StaticTextElement) {
				StaticTextElement ste = (StaticTextElement) fe;
				renderStaticTextElement(ste, sb);
			} else if (fe instanceof StaticHTMLTextElement) {
				StaticHTMLTextElement ste = (StaticHTMLTextElement) fe;
				renderStaticHTMLTextElement(ste, sb);
			} else if (fe instanceof PasswordElement) {
				PasswordElement pe = (PasswordElement) fe;
				renderPasswordElement(f, pe, sb);
			} else if (fe instanceof TextElement) {
				renderTextElement(f, (TextElement) fe, sb, ubu);
			} else if (fe instanceof CheckBoxElement) {
				CheckBoxElement cbe = (CheckBoxElement) fe;
				renderCheckBox(f, cbe, sb, translator);
			} else if (fe instanceof RadioButtonGroupElement) {
				RadioButtonGroupElement rbe = (RadioButtonGroupElement) fe;
				renderRadioButtonGroup(f, rbe, translator, sb);
			} else if (fe instanceof SingleSelectionElement) {
				SingleSelectionElement se = (SingleSelectionElement) fe;
				renderDropDown(f, se, sb);
			} else if (fe instanceof MultipleSelectionElement) {
				MultipleSelectionElement me = (MultipleSelectionElement) fe;
				renderCheckBoxes(f, me, translator, sb);
			} else if (fe instanceof LinkElement) {
				LinkElement le = (LinkElement) fe;
				renderLink(f, le, translator, sb);
			} else {
				throw new OLATRuntimeException("Unknown for element::" + fe.getName() + " " + fe.getClass().getCanonicalName(), null);
			}

			if(fe.getVisualMarked()){
				sb.append("</span\">");
			}

			if (!fe.isReadOnly() && fe.getExample() != null) {
				sb.append("<div class=\"b_form_example\">(");
				sb.append(fe.getExample());
				sb.append(")</div>");
			}
			
			if (fe.isError()) {
				sb.append("<div class=\"b_form_error_msg\">");
				sb.append(fe.getError(translator)); 
				sb.append("</div>");
			}
			
			sb.append("</div>"); // close b_form_element
		}
		
		sb.append("</div>"); // close b_form_element_wrapper
	}

	
	/**
	 * @Deprecated The wiki markup area is no longer supported. In the legacy form
	 *             infrastructure it's still there, but it won't be available in the
	 *             new flexi forms. In flexi forms use the RichTextElement instead.
	 */
	@Deprecated
	private void renderWikiMarkupTextAreaElementAutoSize(Form f, TextAreaElement tea, StringOutput sb, boolean resizable) {
		String val = tea.getValue();
		if (val == null) val = "";
		if (f.isDisplayOnly()) {
			sb.append(Formatter.formatWikiMarkup(val));
		} else if (tea.isReadOnly()) {
			appendReadOnly(Formatter.formatWikiMarkup(val), sb);
		} else {
			String uniqueId = JSELEMENTID+tea.hashCode();
			sb.append("<textarea onchange=\"return setFormDirty('")
			  .append(JSFORMID)
			  .append(f.hashCode())
			  .append("')\" onclick=\"return setFormDirty('")
			  .append(JSFORMID)
			  .append(f.hashCode())
			  .append("')\" cols=\"")
			  .append(tea.getCols())
			  .append("\" rows=\"")
			  .append(tea.getRows())
			  .append("\" name=\"")
			  .append(tea.getName())
			  .append("\" id=\"")
			  .append(uniqueId)
			  .append("\"");
			sb.append(">")
			  .append(StringEscapeUtils.escapeHtml(val))
			  .append("</textarea>");
			
			// wiki syntax icon
			sb.append("<br /><a href=\"javascript:contextHelpWindow('");
			Renderer.renderNormalURI(sb, "help/");
			String pack = this.getClass().getName().substring(0, this.getClass().getName().lastIndexOf("."));
			sb.append(I18nManager.getInstance().getLocaleKey(f.getTranslator().getLocale())).append("/").append(pack).append("/").append("wiki-format.html");
			sb.append("')\" class=\"b_form_wikitext\">");
			sb.append(f.getTranslator().translate("form.wiki.hover"));
			sb.append("</a>");
			
			if(resizable){
				sb.append("<script type=\"text/javascript\">")
					.append("b_form_resizeTextarea('").append(uniqueId).append("');")
					.append("</script>");
			}
		}
	}



	/**
	 * @param f
	 * @param tea
	 * @param sb
	 * @param allowHTML
	 */
	private void renderTextAreaElement(Form f, TextAreaElement tea, StringOutput sb, boolean allowHTML, String[] args) {
		String val = tea.getValue();
		if (val == null) val = "";
		if (f.isDisplayOnly()) {
			sb.append(allowHTML ? val.toString() : Formatter.escWithBR(val).toString());
		} else if (tea.isReadOnly()) {
			appendReadOnly(allowHTML ? val.toString() : Formatter.escWithBR(val).toString(), sb);
		} else {
			boolean wrapoff = (args != null && args[0].equals("wrapoff"));
			String uniqueId = JSELEMENTID+tea.hashCode();
			sb.append("<textarea onchange=\"return setFormDirty('")
			  .append(JSFORMID)
			  .append(f.hashCode())
			  .append("')\" onclick=\"return setFormDirty('")
			  .append(JSFORMID)
			  .append(f.hashCode())
			  .append("')\" cols=\"")
			  .append(tea.getCols())
			  .append("\" rows=\"")
			  .append(tea.getRows())
			  .append("\" name=\"")
			  .append(tea.getName())
			  .append("\" id=\"")
			  .append(uniqueId)
			  .append("\"");
			if (wrapoff) sb.append(" wrap=\"off\"");
			sb.append(">")
			  .append(allowHTML ? val : StringEscapeUtils.escapeHtml(val).toString())
			  .append("</textarea>");
		}
	}

	/**
	 * Method renderTextElement.
	 * 
	 * @param textElement
	 * @param sb
	 */
	private void renderTextElement(Form f, TextElement textElement, StringOutput sb, URLBuilder ubu) {
		String val = textElement.getValue();
		if (val == null) val = "";
		String htmlVal = StringEscapeUtils.escapeHtml(val).toString();
		if (f.isDisplayOnly()) {
			sb.append(htmlVal);
		} else if (textElement.isReadOnly()) {
			appendReadOnly(htmlVal, sb);
		} else {
			PopupData popupData = textElement.getPopupData();
			String cId = String.valueOf(textElement.hashCode());
			sb.append("<input type=\"text\" id=\"").append(textElement.getName())
					.append("\" name=\"").append(textElement.getName())
					.append("\" value=\"").append(htmlVal).append("\" size=\"").append(textElement.getSize()).append("\" maxlength=\"")
					.append(textElement.getMaxLength()).append("\" ")
					.append("onkeypress=\"return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')\" ")
					.append("onclick=\"return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')\" ").append("id=\"")
					.append(JSELEMENTID).append(cId).append("\" />");
			if (textElement.isUseDateChooser()) {
				// date chooser button
				sb.append("<span class=\"b_form_datechooser\" id=\"trigger_").append(textElement.getName()).append("\" title=\"").append(StringEscapeUtils.escapeHtml(f.getTranslator().translate("calendar.choose"))).append("\">&nbsp;</span>");
				// date chooser javascript
				sb.append("<script type=\"text/javascript\">").append("Calendar.setup({").append("inputField:\"").append(textElement.getName()).append("\",").append("ifFormat:\"");
				if (textElement.getDateChooserDateFormat() == null) {
					// use default format from default locale file
					Formatter formatter = Formatter.getInstance(f.getTranslator().getLocale());
					if (textElement.isDateChooserTimeEnabled()) sb.append(formatter.getSimpleDatePatternForDateAndTime());
					else sb.append(formatter.getSimpleDatePatternForDate());

				} else {
					// use custom date format
					sb.append(textElement.getDateChooserDateFormat());
				}
				sb.append("\",").append("button:\"trigger_").append(textElement.getName()).append("\",").append("align:\"Tl\",").append(
						"singleClick:false,");
				if (textElement.isDateChooserTimeEnabled()) {
					sb.append("showsTime:true,");
					sb.append("timeFormat:\"24\",");
				}
				sb.append("cache:true,").append("firstDay:1,").append("showOthers:true,");
				// Call on change method on input field to trigger dirty button
				sb.append("onUpdate:function(){setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}");
				// Finish js code				
				sb.append("});").append("</script>");
			}
			if (popupData != null) {
				String text = StringEscapeUtils.escapeHtml(f.getTranslator().translate(popupData.getButtonlabelkey()));
				// javascript:{win=window.open('../media/script/Popup/06_01Strassen.html?olatraw=true','webclass_popup','toolbar=no,location=no,directories=0,status=no,menubar=0,scrollbars=yes,resizable=yes,width=650,height=550');win.focus();}
				sb.append("<a href=\"javascript:{win=window.open('");
				ubu.buildURI(sb, new String[] { Form.ELEM_BUTTON_COMMAND_ID }, new String[] { popupData.getButtonaction() });
				sb.append("','olatpopup','toolbar=no,location=no,directories=0,status=no,menubar=0,scrollbars=yes,resizable=yes,width=")
				  .append(popupData.getPopupwidth()).append(",height=").append(popupData.getPopupheight())
				  .append("');win.focus();}\" title=\"").append(text).append("\" class=\"b_form_genericchooser\"></a>");
			}
		}
	}

	/**
	 * Method renderPasswordElement.
	 * 
	 * @param pe
	 * @param sb
	 */
	private void renderPasswordElement(Form f, PasswordElement pe, StringOutput sb) {
		String val = new String(pe.getValue().replaceAll(".", "*"));
		if (f.isDisplayOnly()) {
			sb.append(val);
		} else if (pe.isReadOnly()) {
			appendReadOnly(val, sb);
		} else {
			String uniqueId = JSELEMENTID+pe.hashCode();
			sb.append("<input type=\"password\" name=\"").append(pe.getName()).append("\" size=\"").append(pe.getSize()).append("\" id=\"").append(uniqueId)
					.append("\" maxlength=\"").append(pe.getMaxLength()).append("\" onchange=\"return setFormDirty('").append(JSFORMID).append(
							f.hashCode()).append("')\" onclick=\"return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')\" />");
		}
	}

	private void renderLink(Form f, LinkElement le, Translator translator, StringOutput sb) {
		StringBuffer linkSB = new StringBuffer();
		String url = le.getURL();
		if (url == null) {
			url = "http://";
		} else if (!url.startsWith("http")) {
			url = "http://" + le.getURL();
		}
		String uniqueId = JSELEMENTID+le.hashCode();
		linkSB.append("<a href=\"").append(url).append("\" class=\"b_link_extern\" target=\"_blank\">");
		linkSB.append(le.getLinkName());
		linkSB.append("</a>");		
		if (f.isDisplayOnly()) {
			sb.append(linkSB.toString());
		} else if (le.isReadOnly()) {
			appendReadOnly(linkSB.toString(), sb);
		} else {
			sb.append("<input name=\"").append(le.getName()).append("\" size=\"").append(32)
			.append("\" maxlength=\"").append(265).append("\" onchange=\"return setFormDirty('").append(JSFORMID).append(
					f.hashCode()).append("')\" onclick=\"return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')\" value=\"");
			sb.append(url);
			sb.append("\" id=\"").append(uniqueId).append("\"/>");
		}
	}

	
	
	private void appendReadOnly(String text, StringOutput sb) {
		sb.append(READONLYA);
		sb.append(text);
		sb.append(READONLYB);
	}

	private void renderStaticTextElement(StaticTextElement staticTextElement, StringOutput sb) {
		String val = staticTextElement.getValue();
		sb.append(StringEscapeUtils.escapeHtml(val)); // 

	}

	private void renderStaticHTMLTextElement(StaticHTMLTextElement staticTextElement, StringOutput sb) {
		sb.append(staticTextElement.getValue());

	}

	private void renderLabel(FormElement formElement, StringOutput sb, Translator translator) {
		String labelKey = formElement.getLabelKey();
		if (labelKey != null) {
			sb.append(translator.translate(labelKey) + ":");
		}
	}

	private void renderRadioButtonGroup(Form f, RadioButtonGroupElement rbge, Translator translator, StringOutput sb) {
		int cnt = rbge.getSize();
		if (rbge.getSelected() == -1) throw new AssertException("no selection at render time");
		sb.append("<div id=\"").append(JSELEMENTID+rbge.hashCode()).append("\">");
		if (f.isDisplayOnly()) {
			int sel = rbge.getSelected();
			String val = rbge.getValue(sel);
			if (val == null) throw new AssertException("value in readonly mode of radiobutton group (" + rbge.getName() + ") was null, selPos = "
					+ rbge.getSelected());
			sb.append(StringEscapeUtils.escapeHtml(val).toString());
			
		} else {
			String subStrName = "name=\"" + rbge.getName() + "\"";
			for (int i = 0; i < cnt; i++) {
				String key = rbge.getKey(i);
				String value = rbge.getValue(i);
				boolean selected = rbge.isSelected(i);
				sb.append("<input type=\"radio\" class=\"b_radio\" " + subStrName + " value=\"");
				sb.append(key);
				sb.append("\"");
				if (selected) sb.append(" checked=\"checked\"");
				if (rbge.isReadOnly()) {
					sb.append(" disabled=\"disabled\"");
				} else {
					sb.append(" onclick=\"{checkform");
					sb.append(f.getComponentName());
					sb.append("();");
					sb.append("return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\" onchange=\"{checkform");
					sb.append(f.getComponentName());
					sb.append("();");
					sb.append("return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\"");
				}
				sb.append(" />");
				if (!rbge.isHTMLIsAllowed()) {
				sb.append(StringEscapeUtils.escapeHtml(value));
				} else {
					sb.append(value);
				}
				sb.append("&nbsp;&nbsp;");
				if (rbge.renderVertical()) sb.append("<br />");
			}
		}
		sb.append("</div>");
	}

	private void renderDropDown(Form f, SingleSelectionElement sse, StringOutput sb) {
		int cnt = sse.getSize();
		sb.append("<div id=\"").append(JSELEMENTID+sse.hashCode()).append("\">");
		if (f.isDisplayOnly()) {
			int sel = sse.getSelected();
			String val = sse.getValue(sel);
			if (val == null) throw new AssertException("value in readonly mode of drop-down list (" + sse.getName() + ") was null, selPos = "
					+ sse.getSelected());
			sb.append(StringEscapeUtils.escapeHtml(val).toString());
			
		} else {
			sb.append("<select name=\"");
			sb.append(sse.getName());
			sb.append("\"");
			if (sse.isReadOnly()) {
				sb.append(" disabled=\"disabled\"");
			} else {
				sb.append(" onchange=\"{checkform");
				sb.append(f.getComponentName());
				sb.append("();");
				sb.append("return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\"");
			}
			sb.append(">");
			for (int i = 0; i < cnt; i++) {
				String key = sse.getKey(i);
				String value = sse.getValue(i);
				boolean selected = sse.isSelected(i);
				sb.append("<option value=\"");
				sb.append(key);
				sb.append("\" ");
				if (selected) sb.append("selected=\"selected\"");
				sb.append(">");
				sb.append(StringEscapeUtils.escapeHtml(value));
				sb.append("</option>");
			}
			sb.append("</select>");
		}
		sb.append("</div>");
	}

	/*
	 * private void renderRadioButtons(SelectionElement se, StringOutput sb) {
	 * String name = se.getName(); int cnt = se.getSize(); for (int i = 0; i <
	 * cnt; i++) { String key = se.getKey(i); String value = se.getValue(i);
	 * boolean selected = se.isSelected(i); sb.append("<input type=\"radio\"
	 * name=\""); sb.append(name); sb.append("\" value=\""); sb.append(key);
	 * sb.append("\""); if (selected) { sb.append(" checked=\"checked\""); }
	 * sb.append("> "); sb.append(value); sb.append("<br />"); } }
	 */

	private void renderCheckBox(Form f, CheckBoxElement cbe, StringOutput sb, Translator translator) {
		String labelKey = cbe.getLabelKey();
		String label = translator.translate(labelKey);
		boolean checked = cbe.isChecked();
		sb.append("<input type=\"checkbox\" class=\"b_checkbox\" name=\"");
		sb.append(cbe.getName());
		sb.append("\" value=\"");
		sb.append(StringEscapeUtils.escapeHtml(label));
		sb.append("\"");
		if (checked) sb.append(" checked=\"checked\"");		
		if (cbe.isReadOnly()) {
			sb.append(" disabled=\"disabled\"");
		} else {
			sb.append(" onchange=\"{checkform");
			sb.append(f.getComponentName());
			sb.append("();");
			sb.append("return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\" onclick=\"{checkform");
			sb.append(f.getComponentName());
			sb.append("();");
			sb.append("return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\" ");
		}
		sb.append(" id=\"").append(JSELEMENTID+cbe.hashCode()).append("\"/>");
	}

	/**
	 * Method renderCheckBoxes.
	 * 
	 * @param me
	 * @param sb
	 */
	private void renderCheckBoxes(Form f, MultipleSelectionElement me, Translator translator, StringOutput sb) {
		String name = me.getName();
		int cnt = me.getSize();
		sb.append("<div id=\"").append(JSELEMENTID+me.hashCode()).append("\">");
		for (int i = 0; i < cnt; i++) {
			String key = me.getKey(i);
			String value = me.getValue(i);
			boolean selected = me.isSelected(i);
			sb.append("<input type=\"checkbox\" class=\"b_checkbox\" name=\"");
			sb.append(name);
			sb.append("\" value=\"");
			sb.append(key);
			sb.append("\"");
			if (selected) sb.append(" checked=\"checked\"");
			if (me.isReadOnly()) {
				sb.append(" disabled=\"disabled\"");
			} else {
				sb.append(" onchange=\"{checkform");
				sb.append(f.getComponentName());
				sb.append("();");
				sb.append("return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\" onclick=\"{checkform");
				sb.append(f.getComponentName());
				sb.append("();");
				sb.append("return setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\"");
			}
			sb.append(" /> ");
			sb.append(StringEscapeUtils.escapeHtml(value));
			sb.append("<br />");
		}
		if (me.enableCheckAll() && !me.isReadOnly()) { // add check/uncheck link
			sb.append("<div class=\"b_form_togglecheck\">");
			sb.append("<a href=\"#\" onclick=\"javascript:{b_form_toggleCheck(document.");
			sb.append(f.getComponentName() + "." + name);
			sb.append(", true);setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\"><input type=\"checkbox\" class=\"b_checkbox\" checked=\"checked\" disabled=\"disabled\" />&nbsp;");
			sb.append(translator.translate("form.checkall"));
			sb.append("</a>");

			sb.append("<br /><a href=\"#\" onclick=\"javascript:{b_form_toggleCheck(document.");
			sb.append(f.getComponentName() + "." + name);
			sb.append(", false);setFormDirty('").append(JSFORMID).append(f.hashCode()).append("')}\"><input type=\"checkbox\" class=\"b_checkbox\" disabled=\"disabled\" />&nbsp;");
			sb.append(translator.translate("form.uncheckall"));
			sb.append("</a>");
			sb.append("</div>");
		}
		sb.append("</div>");
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator transll,
			RenderingState rstate) {
		/*
		 * no longer needed here as js gets included by Form component for ajax compatibilty
		
		// FIXME:fj:a remove translator from interface, use source.getTranslator
		// instead
		Form f = (Form) source;
		// add date chooser java script when in edit only mode
		if (!f.isDisplayOnly()) {
			Iterator it_formelemnames = f.getNameIterator();
			while (it_formelemnames.hasNext()) {
				String name = (String) it_formelemnames.next();
				FormElement fe = f.getFormElement(name);
				// only check on elements that are not set to readonly and
				// are of type text element
				if (!fe.isReadOnly() && fe instanceof TextElement) {
					TextElement tel = (TextElement) fe;
					// if this text element uses a date chooser add date chooser java
					// script libraries
					if (tel.isUseDateChooser()) {

						// only include this css and the .js once (loading the same .js file
						// more than once is unnecessary and gives javascript errors
						Object alreadyIncluded = rstate.getRenderInfo(this, "date_js");
						if (alreadyIncluded == null) {
							// the first time -> include
							rstate.putRenderInfo(this, "date_js", new Object());

							sb.append("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"");
							String fontSize = renderer.getGlobalSettings().getFontSize();
							Renderer.renderNormalURI(sb, "raw/css/jscalendar-" + fontSize + ".css");
							sb.append("\" title=\"jscalendarcss\">\n").append("<script type=\"text/javascript\" src=\"");
							Renderer.renderNormalURI(sb, "raw/js/jscalendar/calendar.js");
							sb.append("\"></script>\n").append("<script type=\"text/javascript\" src=\"");
							Renderer.renderNormalURI(sb, "raw/js/jscalendar/lang/calendar-");
							sb.append(source.getTranslator().getLocale().getLanguage()).append(".js\"></script>\n").append(
									"<script type=\"text/javascript\" src=\"");
							Renderer.renderNormalURI(sb, "raw/js/jscalendar/calendar-setup.js");
							sb.append("\"></script>\n");
							// finished, date chooser is initialized, skip all other elements
							break;
						} // else omit output of css and js includes
					}
				}
			}
			if (!f.isDisplayOnly()) {
				sb.append("<script type=\"text/javascript\">\n");
				sb.append("function checkform").append(f.getComponentName()).append("(){\n");
				renderElementVisibilityDependencyRules(f, sb);
				sb.append("}</script>\n");

			}

		}
		
		*/
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		StringBuffer tmp = new StringBuffer();
		Form f = (Form) source;
		String foName = f.getComponentName();
		FormElement focusElem = null, mandatoryElem = null, firstElem = null;
		// PRE: f.isVisible() ensured by renderer
		if (!f.isValid() && !f.isDisplayOnly()) {
			// focus on first error, or on the first element if there is no error
			Iterator it_formelemnames = f.getNameIterator();
			while (it_formelemnames.hasNext()) { // guaranteed correct order since
																						// linkedhashmap
				String name = (String) it_formelemnames.next();
				FormElement fe = f.getFormElement(name);
				if (!fe.isReadOnly()) {
					if (firstElem == null) firstElem = fe;
					if (fe.isError()) {
						focusElem = fe;
						break;
					} else if (fe.isMandatory()) {
						if (mandatoryElem == null) mandatoryElem = fe; // focus first
																														// mandatory element
					}
				}
			}
			if (focusElem == null) {
				// no error, set focus on mandatory element
				focusElem = (mandatoryElem == null ? firstElem : mandatoryElem);
			}
			// may still be null if a form has no formelements (e.g. msg plus ok
			// button)
			if (focusElem != null) {
				String feName = focusElem.getName();
				tmp.append("\nvar form =  document.forms[\"").append(foName).append("\"]; if (form) form.elements[\"").append(feName).append("\"]");
				if (focusElem instanceof RadioButtonGroupElement) tmp.append("[0]");
				//on array elements select first entry
				tmp.append(".focus();\n");
			}
		} // else: do not focus on valid forms

		// Init visibility rules on all selection elements if form not in display
		// only mode
		if (!f.isDisplayOnly()) {
			tmp.append("checkform").append(f.getComponentName()).append("();\n");
		}
		
		// Add try catch - maybe form not found or other error
		if (tmp.length() > 0) {
			sb.append("try{");
			sb.append(tmp.toString());
			sb.append("}catch(e){");
			sb.append("if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug(e.message, 'FormRenderer:renderBodyOnLoadJSFunctionCall');");
			sb.append("}");	
		}
	}

	private void renderElementVisibilityDependencyRules(Form f, StringOutput sb) {
		List rules = f.getVisibilityDependsOnSelectionRules();
		Iterator iter = rules.iterator();
		while (iter.hasNext()) {
			VisibilityDependsOnSelectionRule rule = (VisibilityDependsOnSelectionRule) iter.next();
			FormElement dependentElement = rule.getDependentElement();
			SelectionElement selection = rule.getSelectionElement();
			String resetValueOrig = rule.getResetValue();
			String resetValue = null;
			if (resetValueOrig != null) {
				// quote return caracters to fill into js variable
				resetValue = resetValueOrig.replaceAll("\n", "\\\\n");
				resetValue = resetValue.replaceAll("\r", "\\\\n");
			}
			sb.append("b_form_updateFormElementVisibility('")
			.append(f.getComponentName())
			.append("','")
			.append(selection.getName())
			.append("','")
			.append(dependentElement.getName())
			.append("','")
			.append(rule.getVisibilityRuleValue())
			.append("',")
			.append(String.valueOf(rule.isVisibilityRuleResult()))
			.append(",\"")
			.append(resetValue)
			.append("\",")
			.append(String.valueOf(rule.isHideDisabledElements()))
			.append(",")
			.append(String.valueOf(rule.isPreventOppositeAction()))
			.append(");\n");
		}
	}

}
