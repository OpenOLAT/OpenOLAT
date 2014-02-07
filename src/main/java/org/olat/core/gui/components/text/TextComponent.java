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
package org.olat.core.gui.components.text;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * The text component can be used to display a text message. The message can be
 * either a string or an i18n key that is automatically translated during render
 * time. Optionally a CSS class can be provided.
 * <p>
 * By default, the text component renders in a span tag. If you want this span
 * to be changed to a div tag, use the setSpanAsDomReplaceable() method.
 * 
 * <P>
 * Initial Date: 10.11.2009 <br>
 * 
 * @author gnaegi
 */
public class TextComponent extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new TextComponentRenderer();
	private String text; // a plain vanilla string, will not be translated
	private String i18nKey; // a i18n key that will be translated with
	// getTranslator()
	private String cssClass; // optional css class name(s)

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * Constructor to create a text component with a translated plain vanilla text string. The
	 * text is displayed as is and not translated at render time.
	 * 
	 * @param name the name of the component
	 * @param text the plain vanilla text
	 * @param cssClass the css class(es) to be used or NULL if not available
	 * @param renderAsSpan true: use a span to wrap the text; false: use a div to
	 *          wrap the text
	 */
	TextComponent(String name, String text, String cssClass, boolean renderAsSpan) {
		super(name);
		setText(text);
		setCssClass(cssClass);
		setSpanAsDomReplaceable(renderAsSpan);
	}

	/**
	 * Constructor to create a text component with an untranslated i18n key. The
	 * i18n key is first translated with the given translator and then displayed.
	 * 
	 * @param name the name of the component
	 * @param i18nKey the i18n key to be translated during render time
	 * @param translator the translator that can translate the i18n key
	 * @param cssClass the css class(es) to be used or NULL if not available
	 * @param renderAsSpan true: use a span to wrap the text; false: use a div to
	 *          wrap the text
	 */
	TextComponent(String name, String i18nKey, Translator translator, String cssClass, boolean renderAsSpan) {
		super(name, translator);
		setI18nKey(i18nKey);
		setCssClass(cssClass);
		setSpanAsDomReplaceable(renderAsSpan);
	}

	/**
	 * Set a new i18n key. Translator must be available.
	 * 
	 * @param i18nKey the key or NULL to reset
	 */
	public void setI18nKey(String i18nKey) {
		if (i18nKey != null && getTranslator() == null) { throw new AssertException(
				"Can not set i18n key when no translator is available. Use setTranslator() first."); }
		this.i18nKey = i18nKey;
		this.setDirty(true);
	}

	/**
	 * Set a new text that will not be translated. Overrides any previously set
	 * i18n keys.
	 * 
	 * @param text the text or NULL to reset.
	 */
	public void setText(String text) {
		this.text = text;
		this.setDirty(true);
	}

	/**
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
	// nothing to dispatch
	}

	/**
	 * Get the display text for this component. If an explicit text is setted,
	 * this one will be displayed. If not, the i18n key will be translated.
	 * 
	 * @return The translated text or NULL if not set
	 */
	public String getDisplayText() {
		// explicit setted text will override i18n keys
		if (text != null) { return text; }
		if (i18nKey != null && getTranslator() != null) { return getTranslator().translate(i18nKey); }
		return null;
	}

	/**
	 * Set an optional CSS class(es). Use setSpanAsDomReplaceable() to decide if a
	 * span or div tag should be used to wrap the text.
	 * 
	 * @param cssClass
	 */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
		this.setDirty(true);
	}

	/**
	 * Get the optional CSS class(es)
	 * 
	 * @return the class(es) or NULL
	 */
	public String getCssClass() {
		return cssClass;
	}
}
