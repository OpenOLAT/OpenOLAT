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
package org.olat.core.gui.components.helpTooltip;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.util.StringHelper;

/**
 * This component displays a little help tooltip with an optional link to the
 * manual. Can be used to place some on-spot help. Use rarely.
 * 
 * Initial date: 05.11.2015<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 */
public class HelpTooltip extends AbstractComponent {

	private static final ComponentRenderer RENDERER = new HelpTooltipRenderer();
	private static final HelpLinkSPI helpProvider;

	static {
		// load help provider only once if enabled
		HelpModule helpModule = CoreSpringFactory.getImpl(HelpModule.class);
		if (helpModule.isManualEnabled()) {
			helpProvider = helpModule.getManualProvider();
		} else {
			helpProvider = null;
		}
	}

	private String text;
	private String url;

	/**
	 * Constructor to create a tooltip with some text to be displayed.
	 * 
	 * @param name
	 *            The name of the component
	 * @param helpText
	 *            The text to be displayed on hover
	 */
	public HelpTooltip(String name, String helpText) {
		this(name, helpText, null, null);
	}

	/**
	 * Constructor to create a tooltip with a link to the manual to be displayed
	 * 
	 * @param name
	 *            The name of the component
	 * @param helpPage
	 *            The page identifier in the manual
	 * @param locale
	 *            The users locale
	 */
	public HelpTooltip(String name, String helpPage, Locale locale) {
		this(name, null, helpPage, locale);
	}

	/**
	 * Constructor to create a tooltip with some text to be displayed together
	 * with a link to the manual with more information.
	 * 
	 * @param name
	 *            The name of the component
	 * @param helpText
	 *            The text to be displayed on hover
	 * @param helpPage
	 *            The page identifier in the manual
	 * @param locale
	 *            The users locale
	 */
	public HelpTooltip(String name, String helpText, String helpPage, Locale locale) {
		super(name);
		setHelpPage(helpPage, locale);
		setHelpText(helpText);
		setDomReplacementWrapperRequired(false);
	}

	/**
	 * Set the page of the manual to be linked
	 * 
	 * @param locale
	 *            The users locale
	 * @param helpPage
	 *            The page identifier in the manual or NULL to disable this
	 *            function
	 */
	public void setHelpPage(String helpPage, Locale locale) {
		if (helpProvider != null && StringHelper.containsNonWhitespace(helpPage) && locale != null) {
			url = helpProvider.getURL(locale, helpPage);
		} else {
			url = null;
		}
	}

	/**
	 * Set the display text of the tooltip
	 * 
	 * @param helpText
	 *            The text to be displayed on hover or NULL to disable this
	 *            function
	 */
	public void setHelpText(String helpText) {
		if (StringHelper.containsNonWhitespace(helpText)) {
			text = helpText;
		} else {
			text = null;
		}
	}

	/**
	 * @return The help text or NULL if not set
	 */
	public String getHelpText() {
		return text;
	}

	/**
	 * @return The URL to the manual or NULL if not set
	 */
	public String getHelpUrl() {
		return url;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// nothing to do
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
