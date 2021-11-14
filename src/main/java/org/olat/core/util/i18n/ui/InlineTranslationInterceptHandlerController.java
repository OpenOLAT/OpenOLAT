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
package org.olat.core.util.i18n.ui;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.delegating.DelegatingComponent;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.intercept.InterceptHandler;
import org.olat.core.gui.render.intercept.InterceptHandlerInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This class acts both as the render intercepter and as the inline translation
 * tool dispatcher. For each detected translated GUI element it will add a hover
 * event which triggers an edit link.
 * <p>
 * When the server is configured as translation server, the inline translation
 * tool will start in language translation mode. Otherwhise it will start in
 * language customizing mode (overlay edit)
 * 
 * <P>
 * Initial Date: 16.09.2008 <br>
 * 
 * @author gnaegi
 */
public class InlineTranslationInterceptHandlerController extends BasicController implements InterceptHandlerInstance, InterceptHandler {
	private static final String SPAN_TRANSLATION_I18NITEM_OPEN = "<span class=\"o_translation_i18nitem\">";
	private static final String SPAN_CLOSE = "</span>";
	private static final String BODY_TAG = "<body";
	private static final String ARG_BUNDLE = "bundle";
	private static final String ARG_KEY = "key";
	private static final String ARG_IDENT = "id";

	private URLBuilder inlineTranslationURLBuilder;
	private DelegatingComponent delegatingComponent;
	private TranslationToolI18nItemEditCrumbController i18nItemEditCtr;
	private CloseableModalController cmc;
	private StackedPanel mainP;

	// patterns to detect localized strings with identifyers
	private static final String decoratedTranslatedPattern = "(" + I18nManager.IDENT_PREFIX + "(.*?)" + I18nManager.IDENT_START_POSTFIX
			+ ").*?(" + I18nManager.IDENT_PREFIX + "\\2" + I18nManager.IDENT_END_POSTFIX + ")";
	private static final Pattern patternLink = Pattern.compile("<a[^>]*?>(?:<span[^>]*?>)*?[^<>]*?" + decoratedTranslatedPattern
			+ "[^<>]*?(?:</span>*?>)*?</a>");
	private static final Pattern patternInput = Pattern.compile("<input[^>]*?" + decoratedTranslatedPattern + ".*?>");
	private static final Pattern patAttribute = Pattern.compile("<[^>]*?" + decoratedTranslatedPattern + "[^>]*?>");

	@Autowired
	private I18nManager i18nMgr;
	@Autowired
	private I18nModule i18nModule;
	
	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param control
	 */
	InlineTranslationInterceptHandlerController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		// the deleagating component is ony used to provide the
		// inlineTranslationURLBuilder to be able to create the translation tool
		// links
		delegatingComponent = new DelegatingComponent("delegatingComponent", new ComponentRenderer() {
			public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderResult renderResult, String[] args) {
				// save urlbuilder for later use (valid only for one
				// request scope thus
				// transient, normally you may not save the url builder
				// for later usage)
				inlineTranslationURLBuilder = ubu;
			}

			public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderingState rstate) {
			// void
			}

			public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
				// trigger js method that adds hover events - in some conditions method is not available (in iframes)
				sb.append("if (jQuery.isFunction(b_attach_i18n_inline_editing)){b_attach_i18n_inline_editing();}");
			}
		});
		delegatingComponent.addListener(this);
		delegatingComponent.setDomReplaceable(false);

		mainP = putInitialPanel(delegatingComponent);
		mainP.setDomReplaceable(false);
	}

	/**
	 * @see org.olat.core.gui.render.intercept.InterceptHandler#createInterceptHandlerInstance()
	 */
	public InterceptHandlerInstance createInterceptHandlerInstance() {
		return this;
	}

	public ComponentRenderer createInterceptComponentRenderer(final ComponentRenderer originalRenderer) {
		return new ComponentRenderer() {
			@Override
			public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderResult renderResult, String[] args) {
				// ------------- show translator keys
				// we must let the original renderer do its work so that the
				// collecting translator is callbacked.
				// we save the result in a new var since it is too early to
				// append it
				// to the 'stream' right now.
				
				try(StringOutput sbOrig = new StringOutput()) {
					originalRenderer.render(renderer, sbOrig, source, ubu, translator, renderResult, args);
					String rendered = sbOrig.toString();
					
					String renderedWithHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(rendered,
							inlineTranslationURLBuilder, getTranslator());
					sb.append(renderedWithHTMLMarkup);
				} catch (Exception e) {
					String emsg = "exception while rendering component '" + source.getComponentName() + "' (" + source.getClass().getName() + ") "
							+ source.getListenerInfo() + "<br />Message of exception: " + e.getMessage();
					sb.append("<span style=\"color:red\">Exception</span><br /><pre>" + emsg + "</pre>");
				}	
			}
			
			@Override
			public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderingState rstate) {
				originalRenderer.renderHeaderIncludes(renderer, sb, source, ubu, translator, rstate);
			}

			@Override
			public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
				originalRenderer.renderBodyOnLoadJSFunctionCall(renderer, sb, source, rstate);
			}
		};
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == delegatingComponent) {
			String bundle = ureq.getParameter(ARG_BUNDLE);
			String key = ureq.getParameter(ARG_KEY);
			// The argument ARG_IDENT is not used for dispatching right now
			if (isLogDebugEnabled()) {
				logDebug("Got event to launch inline translation tool for bundle::" + bundle + " and key::" + key);
			}
			if (StringHelper.containsNonWhitespace(bundle) && StringHelper.containsNonWhitespace(key)) {
				// Get userconfigured reference locale
				Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
				List<String> referenceLangs = i18nModule.getTransToolReferenceLanguages();
				String referencePrefs = (String) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_REFERENCE_LANG, referenceLangs.get(0));
				Locale referenceLocale = i18nMgr.getLocaleOrNull(referencePrefs);
				// Set target local to current user language
				Locale targetLocale = i18nMgr.getLocaleOrNull(ureq.getLocale().toString());
				if (i18nModule.isOverlayEnabled() && !i18nModule.isTransToolEnabled()) {
					// use overlay locale when in customizing mode
					targetLocale = i18nModule.getOverlayLocales().get(targetLocale);	
				}
				List<I18nItem> i18nItems = i18nMgr.findExistingAndMissingI18nItems(referenceLocale, targetLocale, bundle, false);
				if(i18nItems.isEmpty()) {
					logError("Can not launch inline translation tool, bundle or key empty! bundle::" + bundle + " key::" + key, null);
				} else {
					i18nMgr.sortI18nItems(i18nItems, true, true); // sort with priority
					// Initialize inline translation controller
					if (i18nItemEditCtr != null) removeAsListenerAndDispose(i18nItemEditCtr);
					// Disable inline translation markup while inline translation tool is
					// running -
					// must be done before instantiating the translation controller
					i18nMgr.setMarkLocalizedStringsEnabled(ureq.getUserSession(), false);
					i18nItemEditCtr = new TranslationToolI18nItemEditCrumbController(ureq, getWindowControl(), i18nItems, referenceLocale, !i18nModule.isTransToolEnabled());
					listenTo(i18nItemEditCtr);
					// set current key from the package as current translation item
					for (I18nItem item : i18nItems) {
						if (item.getKey().equals(key)) {
							i18nItemEditCtr.initialzeI18nitemAsCurrentItem(ureq, item);
							break;
						}
					}
					// Open in modal window
					if (cmc != null) removeAsListenerAndDispose(cmc);
					cmc = new CloseableModalController(getWindowControl(), "close", i18nItemEditCtr.getInitialComponent());
					listenTo(cmc);
					cmc.activate();
				}
			} else {
				logError("Can not launch inline translation tool, bundle or key empty! bundle::" + bundle + " key::" + key, null);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			// user closed dialog, go back to inline translation mode
			i18nMgr.setMarkLocalizedStringsEnabled(ureq.getUserSession(), true);
		}
	}

	@Override
	protected void doDispose() {
		// controllers autodisposed by basic controller
		inlineTranslationURLBuilder = null;
		delegatingComponent = null;
		i18nItemEditCtr = null;
		cmc = null;
        super.doDispose();
	}

	/**
	 * Helper method to replace the translations that are wrapped with some
	 * identifyer markup by the translator with HTML markup to allow inline
	 * editing.
	 * <p>
	 * This method is public and static to be testable with jUnit.
	 * 
	 * @param stringWithMarkup The text that contains translated elements that are
	 *          wrapped with some identifyers
	 * @param inlineTranslationURLBuilder URI builder used to create the inline
	 *          translation links
	 * @param inlineTrans
	 * @return
	 */
	public static String replaceLocalizationMarkupWithHTML(String stringWithMarkup, URLBuilder inlineTranslationURLBuilder,
			Translator inlineTrans) {
		while (stringWithMarkup.indexOf(I18nManager.IDENT_PREFIX) != -1) {
			// calculate positions of next localization identifyer
			int startSPos = stringWithMarkup.indexOf(I18nManager.IDENT_PREFIX);
			int startPostfixPos = stringWithMarkup.indexOf(I18nManager.IDENT_START_POSTFIX);
			String combinedKey = stringWithMarkup.substring(startSPos + I18nManager.IDENT_PREFIX.length(), startPostfixPos);
			int startEPos = startPostfixPos + I18nManager.IDENT_START_POSTFIX.length();
			String endIdent = I18nManager.IDENT_PREFIX + combinedKey + I18nManager.IDENT_END_POSTFIX;
			int endSPos = stringWithMarkup.indexOf(endIdent);
			int endEPos = endSPos + endIdent.length();
			// Build link for this identifyer
			StringOutput link = new StringOutput();
			// Check if we can parse the combined key
			String[] args = combinedKey.split(":");
			if (args.length == 3) {
				buildInlineTranslationLink(args, link, inlineTrans, inlineTranslationURLBuilder);				
			} else {
				// ups, can not parse combined key? could be for example because
				// ContactList.setName() replaced : with fancy ¦ which got HTML escaped
				// In any case, we can not produce a translation link for this, do nothing
				stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
				continue;
			}

			// Case 1: translated within a 'a' tag. The tag can contain an optional
			// span tag
			// before and after translated link some other content could be
			// No support for i18n text that does contain HTML markup
			Matcher m = patternLink.matcher(stringWithMarkup);
			boolean foundPos = m.find();
			int wrapperOpen = 0;
			int wrapperClose = 0;
			if (foundPos) {
				wrapperOpen = m.start(0);
				wrapperClose = m.end(0);
				// check if found position does belong to start position
				if (wrapperOpen > startSPos) {
					foundPos = false;
				} else {
					// check if link is visible, skip other links
					int skipPos = stringWithMarkup.indexOf("o_skip", wrapperOpen);
					if (skipPos > -1 && skipPos < wrapperClose) {
						stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
						continue;
					}
					// found a valid link pattern, replace it
					stringWithMarkup = replaceItemWithHTMLMarkupSurrounded(stringWithMarkup, link, startSPos, startEPos, endSPos, endEPos,
							wrapperOpen, wrapperClose);
					continue;
				}
			}
			// Case 2: translated within an 'input' tag
			if (!foundPos) {
				m = patternInput.matcher(stringWithMarkup);
				foundPos = m.find();
				if (foundPos) {
					wrapperOpen = m.start(0);
					wrapperClose = m.end(0);
					// check if found position does belong to start position
					if (wrapperOpen > startSPos) foundPos = false;
					else {
						// ignore within a checkbox
						int checkboxPos = stringWithMarkup.indexOf("checkbox", wrapperOpen);
						if (checkboxPos != -1 && checkboxPos < startSPos) {
							stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
							continue;
						}
						// ignore within a radio button
						int radioPos = stringWithMarkup.indexOf("radio", wrapperOpen);
						if (radioPos != -1 && radioPos < startSPos) {
							stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
							continue;
						}
						// found a valid input pattern, replace it
						stringWithMarkup = replaceItemWithHTMLMarkupSurrounded(stringWithMarkup, link, startSPos, startEPos, endSPos, endEPos,
								wrapperOpen, wrapperClose);
						continue;
					}
				}
			}
			// Case 3: translated within a tag attribute of an element - don't offer
			// inline translation
			m = patAttribute.matcher(stringWithMarkup);
			foundPos = m.find();
			if (foundPos) {
				wrapperOpen = m.start(0);
				wrapperClose = m.end(0);
				// check if found position does belong to start position
				if (wrapperOpen > startSPos) foundPos = false;
				else {
					// found a patter in within an attribute, skip this one
					stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
					continue;
				}
			}
			// Case 4: i18n element in html head - don't offer inline translation
			if (startSPos < stringWithMarkup.indexOf(BODY_TAG)) {
				// found a pattern in the HTML head, skip this one
				stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
				continue;
			}

			// Case 4: default case: normal translation, surround with inline
			// translation link
			StringBuilder tmp = new StringBuilder();
			tmp.append(stringWithMarkup.substring(0, startSPos));
			tmp.append(SPAN_TRANSLATION_I18NITEM_OPEN);
			tmp.append(link);
			tmp.append(stringWithMarkup.substring(startEPos, endSPos));
			tmp.append(SPAN_CLOSE);
			tmp.append(stringWithMarkup.substring(endEPos));
			stringWithMarkup = tmp.toString();
		}
		return stringWithMarkup;
	}

	/**
	 * Internal helper to add the html markup surrounding the parent element
	 * 
	 * @param stringWithMarkup
	 * @param link
	 * @param startSPos
	 * @param startEPos
	 * @param endSPos
	 * @param endEPos
	 * @param wrapperOpen
	 * @param wrapperClose
	 * @return
	 */
	private static String replaceItemWithHTMLMarkupSurrounded(String stringWithMarkup, StringOutput link, int startSPos, int startEPos,
			int endSPos, int endEPos, int wrapperOpen, int wrapperClose) {
		StringBuilder tmp = new StringBuilder();
		tmp.append(stringWithMarkup.substring(0, wrapperOpen));
		tmp.append(SPAN_TRANSLATION_I18NITEM_OPEN);
		tmp.append(link);
		tmp.append(stringWithMarkup.substring(wrapperOpen, startSPos));
		tmp.append(stringWithMarkup.substring(startEPos, endSPos));
		tmp.append(stringWithMarkup.substring(endEPos, wrapperClose));
		tmp.append(SPAN_CLOSE);
		tmp.append(stringWithMarkup.substring(wrapperClose));
		return tmp.toString();
	}

	/**
	 * Internal helper to remove the localization identifyers from the code
	 * without adding html markup
	 * 
	 * @param stringWithMarkup
	 * @param startSPos
	 * @param startEPos
	 * @param endSPos
	 * @param endEPos
	 * @return
	 */
	private static String replaceItemWithoutHTMLMarkup(String stringWithMarkup, int startSPos, int startEPos, int endSPos, int endEPos) {
		StringBuilder tmp = new StringBuilder();
		tmp.append(stringWithMarkup.substring(0, startSPos));
		tmp.append(stringWithMarkup.substring(startEPos, endSPos));
		tmp.append(stringWithMarkup.substring(endEPos));
		return tmp.toString();
	}

	/**
	 * Helper method to build the inline translation link.
	 * <p>
	 * Public and static so that it can be used by the jUnit testcase
	 * 
	 * @param arguments e.g. bundle.name:key.name:ramuniqueid
	 * @param link
	 * @param inlineTrans
	 * @param inlineTranslationURLBuilder
	 */
	public static void buildInlineTranslationLink(String[] arguments, StringOutput link, Translator inlineTrans,
			URLBuilder inlineTranslationURLBuilder) {
			link.append("<a class='o_translation_i18nitem_launcher' style='display:none' href=\"");
			inlineTranslationURLBuilder.buildURI(link, new String[] { ARG_BUNDLE, ARG_KEY, ARG_IDENT }, arguments);
			link.append("\" title=\"");
			String combinedKey = arguments[0] + ":" + arguments[1];
			if (CoreSpringFactory.getImpl(I18nModule.class).isTransToolEnabled()) {
				link.appendHtmlEscaped(inlineTrans.translate("inline.translate", new String[] { combinedKey }));
			} else {
				link.appendHtmlEscaped(inlineTrans.translate("inline.customize.translate", new String[] { combinedKey }));			
			}
			link.append("\"><i class='o_icon o_icon_translation_item'> </i></a>");			
	}

}
