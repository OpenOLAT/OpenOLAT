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

package org.olat.core.gui.render.velocity;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.helpers.GUISettings;
import org.olat.core.helpers.Settings;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;

/**
 * @author Felix Jost
 */
public class VelocityRenderDecorator implements Closeable {
	
	private VelocityComponent vc;
	private Renderer renderer;
	private final boolean isIframePostEnabled;
	private StringOutput target;
	private HelpModule cachedHelpModule;

	/**
	 * @param renderer
	 * @param vc
	 */
	public VelocityRenderDecorator(Renderer renderer, VelocityComponent vc, StringOutput target) {
		this.renderer = renderer;
		this.vc = vc;
		this.target = target;
		isIframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
	}
	
	public HelpModule getHelpModule() {
		if(cachedHelpModule == null) {
			cachedHelpModule = CoreSpringFactory.getImpl(HelpModule.class);
		}
		return cachedHelpModule;
	}

	@Override
	public void close() throws IOException {
		vc = null;
		target = null;
		renderer = null;
	}
	
	/**
	 * @return The default path /dmz/
	 */
	public String getPathDefault() {
		return DispatcherModule.getPathDefault();
	}

	/**
	 * 
	 * @param prefix e.g. abc for "abc647547326" and so on
	 * @return an prefixed id (e.g. f23748932) which is unique in the current render tree.
	 * 
	 */
	public StringOutput getId(String prefix) {
		StringOutput sb = new StringOutput(16);
		sb.append("o_s").append(prefix).append(vc.getDispatchID());
		return sb;
	}
	
	public static String getId(String prefix, VelocityContainer vc) {
		StringOutput sb = StringOutputPool.allocStringBuilder(24);
		sb.append("o_s").append(prefix).append(vc.getDispatchID());
		return StringOutputPool.freePop(sb);
	}
	
	public String getUniqueId() {
		return Long.toString(CodeHelper.getRAMUniqueID());
	}

	/**
	 * 
	 * @return the componentid (e.g.) o_c32645732
	 */
	public StringOutput getCId() {
		StringOutput sb = new StringOutput(16);
		sb.append("o_c").append(vc.getDispatchID());
		return sb;
	}
	
	public String getUuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 
	 * @param command
	 * @return e.g. /olat/auth/1:2:3:cid:com/
	 */
	public StringOutput commandURIbg(String command) {
		StringOutput sb = new StringOutput(100);
		renderer.getUrlBuilder().buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command }, isIframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		return sb;
	}
	
	/**
	 * @param command
	 * @return
	 */
	public StringOutput commandURI(String command) {
		StringOutput sb = new StringOutput(100);
		renderer.getUrlBuilder().buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command });
		return sb;
	}
	
	public String hrefAndOnclick(String command, boolean dirtyCheck, boolean pushState) {
		renderer.getUrlBuilder().buildHrefAndOnclick(target, null, isIframePostEnabled, dirtyCheck, pushState,
				new NameValuePair(VelocityContainer.COMMAND_ID, command));
		return "";
	}
	
	public String hrefAndOnclick(String command, boolean dirtyCheck, boolean pushState, String key, String value) {
		renderer.getUrlBuilder().buildHrefAndOnclick(target, null, isIframePostEnabled, dirtyCheck, pushState,
				new NameValuePair(VelocityContainer.COMMAND_ID, command),
				new NameValuePair(key, value));
		return "";
	}
	
	public String hrefAndOnclick(String command, boolean dirtyCheck, boolean pushState, String key1, String value1, String key2, String value2) {
		renderer.getUrlBuilder().buildHrefAndOnclick(target, null, isIframePostEnabled, dirtyCheck, pushState,
				new NameValuePair(VelocityContainer.COMMAND_ID, command),
				new NameValuePair(key1, value1),
				new NameValuePair(key2, value2));
		return "";
	}

	/**
	 * Creates a java script fragment to execute a ajax background request.
	 * 
	 * @param command
	 * @return
	 */
	public String javaScriptCommand(String command) {
		renderer.getUrlBuilder().buildXHREvent(target, null, false, false,
				new NameValuePair(VelocityContainer.COMMAND_ID, command));
		return "";
	}
	
	public String javaScriptCommand(String command, String key, String value) {
		renderer.getUrlBuilder().buildXHREvent(target, null, false, false,
				new NameValuePair(VelocityContainer.COMMAND_ID, command),
				new NameValuePair(key, value));
		return "";
	}
	
	public String javaScriptCommand(String command, String key1, String value1, String key2, String value2) {
		renderer.getUrlBuilder().buildXHREvent(target, null, false, false,
				new NameValuePair(VelocityContainer.COMMAND_ID, command),
				new NameValuePair(key1, value1),
				new NameValuePair(key2, value2));
		return "";
	}
	
	public String javaScriptCommand(String command, boolean dirtyCheck, boolean pushState, String key1, String value1, String key2, String value2) {
		renderer.getUrlBuilder().buildXHREvent(target, null, dirtyCheck, pushState,
				new NameValuePair(VelocityContainer.COMMAND_ID, command),
				new NameValuePair(key1, value1),
				new NameValuePair(key2, value2));
		return "";
	}
	
	/**
	 * Creates the start of a java script fragment to execute a background request. It's
	 * up to you to close the javascript call.
	 * 
	 * @param command
	 * @return
	 */
	public String openJavaScriptCommand(String command) {
		renderer.getUrlBuilder().openXHREvent(target, null, false, false,
				new NameValuePair(VelocityContainer.COMMAND_ID, command));
		return "";
	}
	
	public String openJavaScriptCommand(String command, boolean dirtyCheck, boolean pushState) {
		renderer.getUrlBuilder().openXHREvent(target, null, dirtyCheck, pushState,
				new NameValuePair(VelocityContainer.COMMAND_ID, command));
		return "";
	}
	
	public String openNoResponseJavaScriptCommand(String command) {
		renderer.getUrlBuilder().openXHRNoResponseEvent(target, null,
				new NameValuePair(VelocityContainer.COMMAND_ID, command));
		return "";
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 */
	public String backgroundCommand(String command) {
		renderer.getUrlBuilder().getXHRNoResponseEvent(target, null,
				new NameValuePair(VelocityContainer.COMMAND_ID, command));
		return "";
	}
	
	public String backgroundCommand(String command, String key, String value) {
		renderer.getUrlBuilder().getXHRNoResponseEvent(target, null,
				new NameValuePair(VelocityContainer.COMMAND_ID, command),
				new NameValuePair(key, value));
		return "";
	}
	
	public String openBackgroundCommand(String command) {
		renderer.getUrlBuilder().openXHRNoResponseEvent(target, null,
				new NameValuePair(VelocityContainer.COMMAND_ID, command));
		return "";
	}
	
	/**
	 * Use it to create the action for a handmade form in a velocity template,
	 * e.g. '<form method="post" action="$r.formURIgb("viewswitch")">'
	 * @param command
	 * @return
	 */
	public StringOutput formURIbg(String command) {
		StringOutput sb = new StringOutput(100);
		renderer.getUrlBuilder().buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command }, isIframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		return sb;
	}
	
	/**
	 * Use it to create the forced non-ajax action for a handmade form in a velocity template,
	 * e.g. '<form method="post" action="$r.formURIgb("viewswitch")">'
	 * @param command
	 * @return
	 */
	public StringOutput formURI(String command) {
		StringOutput sb = new StringOutput(100);
		renderer.getUrlBuilder().buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command });
		return sb;
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 */
	public StringOutput commandURI(String command, String paramKey, String paramValue) {
		StringOutput sb = new StringOutput(100);
		renderer.getUrlBuilder().buildURI(sb, new String[] { VelocityContainer.COMMAND_ID, paramKey }, new String[] { command, paramValue });
		return sb;
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	public StringOutput commandURIbg(String command, String paramKey, String paramValue) {
		StringOutput sb = new StringOutput(100);
		renderer.getUrlBuilder().buildURI(sb, new String[] { VelocityContainer.COMMAND_ID, paramKey }, new String[] { command, paramValue }, isIframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		return sb;
	}
	
	/**
	 * should be called within the main .html template after the <head>tag. gets
	 * some js/css/onLoad code from the component to render/work correctly.
	 * 
	 * @return
	 */
	public StringOutput renderBodyOnLoadJSFunctionCall() {
		StringOutput sb = new StringOutput(100);
		renderer.renderBodyOnLoadJSFunctionCall(sb, vc);
		return sb;
	}

	/**
	 * should be called within the main .html template after the <head>tag. gets
	 * some js/css/onLoad code from the component to render/work correctly.
	 * 
	 * @return
	 */
	public StringOutput renderHeaderIncludes() {
		StringOutput sb = new StringOutput(100);
		renderer.renderHeaderIncludes(sb, vc);
		return sb;
	}

	/**
	 * Note: use only rarely - e.g. for static redirects to login screen or to a
	 * special dispatcher. Renders a uri which is mounted to the webapp/ directory
	 * of your webapplication.
	 * <p>
	 * For static references (e.g. images which cannot be delivered using css):
	 * use renderStaticURI instead!
	 */
	public StringOutput relLink(String uri) {
		StringOutput sb = new StringOutput(100);
		Renderer.renderNormalURI(sb, uri);
		return sb;
	}

	/**
	 * 
	 * e.g. "images/somethingicannotdowithcss.jpg" -> /olat/raw/61x/images/somethingicannotdowithcss.jpg"
	 * with /olat/raw/61x/ mounted to webapp/static directory of your webapp
	 * 
	 * @param uri
	 * @return
	 */
	public StringOutput staticLink(String uri) {
		StringOutput sb = new StringOutput(100);
		Renderer.renderStaticURI(sb, uri);
		return sb;
	}
	
	public StringOutput staticFullLink(String uri) {
		StringOutput sb = new StringOutput(100);
		sb.append(Settings.createServerURI());
		Renderer.renderStaticURI(sb, uri);
		return sb;
	}
	
	public StringOutput themeFullLink() {
		StringOutput sb = new StringOutput(100);
		sb.append(Settings.createServerURI());	
		GUISettings settings = CoreSpringFactory.getImpl(GUISettings.class);
		Renderer.renderStaticURI(sb, "themes/" + settings.getGuiThemeIdentifyer() + "/theme.css");
		return sb;
	}
	
	public StringOutput mathJaxCdn() {
		StringOutput sb = new StringOutput(100);
		sb.append(WebappHelper.getMathJaxCdn());
		return sb;
	}
	
	public StringOutput mathJaxCdnFullUrl() {
		StringOutput sb = new StringOutput(100);
		if(WebappHelper.getMathJaxCdn().startsWith("http")) {
			sb.append(WebappHelper.getMathJaxCdn());
		} else {
			sb.append("https:").append(WebappHelper.getMathJaxCdn());
		}
		return sb;
	}

	public StringOutput mathJaxConfig() {
		StringOutput sb = new StringOutput(100);
		sb.append(WebappHelper.getMathJaxConfig());
		return sb;
	}
	
	public String logoutUrl() {
		return relLink(Settings.getLoginPath() + "/") + "?logout=true";
	}
	
	public StringOutput contextPath() {
		StringOutput sb = new StringOutput(100);
		sb.append(Settings.getServerContextPath());
		return sb;
	}

	public String mathJax(String targetDomId) {
		return Formatter.elementLatexFormattingScript(targetDomId);
	}
	
	/**
	 * e.g. "/olat/"
	 * @return
	 */
	public String relWinLink() {
		return renderer.getUriPrefix();
	}

	/**
	 * @param componentName
	 * @return
	 */
	public StringOutput render(String componentName) {
		return doRender(componentName, null);
	}
	
	/**
	 * Convenience method, render by component name.
	 * 
	 * @param component
	 * @return
	 */
	public StringOutput render(Component component) {
		if(component == null) return new StringOutput(1);
		return doRender(component.getComponentName(), null);
	}

	/**
	 * Convenience method, render by component name.
	 * 
	 * @param component
	 * @return
	 */
	public StringOutput render(FormItem item) {
		if(item == null) return new StringOutput(1);
		return doRender(item.getComponent().getComponentName(), null);
	}
	
	public StringOutput render(FormItem item, String arg1) {
		if(item == null) return new StringOutput(1);
		return doRender(item.getComponent().getComponentName(), new String[]{ arg1 });
	}
	
	/**
	 * Create a link wrapped with some markup to render a nice help button. The
	 * link points to the corresponding page in the manual.
	 * 
	 * @param page Help page name
	 * @return
	 */
	public StringOutput contextHelpWithWrapper(String page) {
		StringOutput sb = new StringOutput(192);
		if (getHelpModule().isManualEnabled()) {
			Locale locale = renderer.getTranslator().getLocale();
			String url = getHelpModule().getManualProvider().getURL(locale, page);
			if(url != null) {
				String title = StringHelper.escapeHtml(renderer.getTranslator().translate("help.button"));
				sb.append("<span class=\"o_chelp_wrapper\">")
				  .append("<a href=\"").append(url)
				  .append("\" class=\"o_chelp\" target=\"_blank\" title=\"").append(title).append("\"><i class='o_icon o_icon_help'> </i> ")
				  .append(renderer.getTranslator().translate("help"))
				  .append("</a></span>");
			}
		}
		return sb;
	}

	/**
	 * Create a js command to open a specific page in the manual.
	 * 
	 * @param page Help page name
	 * @return
	 */
	public StringOutput contextHelpJSCommand(String page) {
		StringOutput sb = new StringOutput(100);
		if (getHelpModule().isHelpEnabled()) {
			Locale locale = renderer.getTranslator().getLocale();
			String url = getHelpModule().getManualProvider().getURL(locale, page);
			sb.append("contextHelpWindow('").append(url).append("')");
		}
		return sb;
	}

	/**
	 * Create a link to a specific page in the manual. The link points to the
	 * corresponding page in the manual.
	 * 
	 * @param page
	 *            Help page name
	 */
	
	public StringOutput contextHelpLink(String page) {
		StringOutput sb = new StringOutput(100);
		if (getHelpModule().isHelpEnabled()) {
			String url = getHelpModule().getManualProvider().getURL(renderer.getTranslator().getLocale(), page);
			sb.append(url);
		}
		return sb;
	}

	/**
	 * Add some mouse-over help text to an element, ideally an icon
	 * 
	 * @param domElem The DOM id of the element that triggers the mouse-over 
	 * @param i18nKey The text to be displayed (including HTML formatting)
	 * @param position Optional param, values: top, bottom, left right. Default is "top"
	 * @return
	 */
	public StringOutput mouseoverHelp(String... args) {
		String domElem = args[0];
		String i18nKey = args[1];
		String position = "top"; // default
		if (args.length > 2 && args[2] != null) {
			position = args[2];
		}
		StringOutput sb = new StringOutput(100);
		sb.append("<script>jQuery(function () {jQuery('#").append(domElem).append("').tooltip({placement:\"").append(position).append("\",container: \"body\",html:true,title:\"");
		if (i18nKey != null) {
			sb.append(StringHelper.escapeJavaScript(translate(i18nKey)));
		}
		sb.append("\"});})</script>");
		return sb;
	}

	/**
	 * @param componentName
	 * @param arg1
	 * @return
	 */
	public StringOutput render(String componentName, String arg1) {
		return doRender(componentName, new String[] { arg1 });
	}

	/**
	 * @param componentName
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public StringOutput render(String componentName, String arg1, String arg2) {
		return doRender(componentName, new String[] { arg1, arg2 });
	}

	/**
	 * Parametrised translator
	 * 
	 * @param key The translation key
	 * @param arg1 The argument list as a string array
	 * @return
	 */
	public String translate(String key, String[] arg1) {
		Translator trans = renderer.getTranslator();
		if (trans == null) return "{Translator is null: key_to_translate=" + key + "}";
		String res = trans.translate(key, arg1);
		if (res == null) return "?? key not found to translate: key_to_translate=" + key + " / trans info:" + trans + "}";
		return res;
	}

	/**
	 * Wrapper to make it possible to use the parametrised translator from within
	 * the velocity container
	 * 
	 * @param key The translation key
	 * @param arg1 The argument list as a list of strings
	 * @return the translated string
	 */
	public String translate(String key, List<String> arg1) {
		return translate(key, arg1.toArray(new String[arg1.size()]));
	}
	
	/**
	 * Wrapper to make it possible to use the parametrised translator from within
	 * the velocity container
	 * 
	 * @param key The translation key
	 * @param arg1 The argument sd string
	 * @return the translated string
	 */
	public String translate(String key, String arg1) {
		return translate(key, new String[] {arg1});
	}
	
	public String translate(String key, String arg1, String arg2) {
		return translate(key, new String[] {arg1, arg2});
	}
	
	public String translate(String key, String arg1, String arg2, String arg3) {
		return translate(key, new String[] {arg1, arg2, arg3});
	}
	
	public String translate(String key, Integer arg1) {
		return translate(key, new String[] { (arg1 == null ? "" : arg1.toString()) });
	}
	
	public String translate(String key, Integer arg1, Integer arg2) {
		return translate(key, new String[] {
				(arg1 == null ? "" : arg1.toString()),
				(arg2 == null ? "" : arg2.toString())
			});
	}

	/**
	 * Method to translate a key that comes from another package. This should be
	 * used rarely. When a key is used withing multiple packages is is usually
	 * better to use a fallback translator or to move the key to the default
	 * packages.
	 * <p>
	 * Used in context help system
	 * @param bundleName the package name, e.g. 'org.olat.core'
	 * @param key the key, e.g. 'my.key'
	 * @param args optional arguments, null if not used
	 * @return
	 */
	public String translateWithPackage(String bundleName, String key, String[] args) {
		Translator pageTrans = renderer.getTranslator();
		if (pageTrans == null) return "{Translator is null: key_to_translate=" + key + "}";
		Locale locale = pageTrans.getLocale();
		Translator tempTrans = new PackageTranslator(bundleName, locale);
		String result = tempTrans.translate(key, args);
		if (result == null) {
			return "{Invalid bundle name: " + bundleName + " and key: " + key + "}";
		}
		return result;
	}

	/**
	 * Method to translate a key that comes from another package. This should be
	 * used rarely. When a key is used withing multiple packages is is usually
	 * better to use a fallback translator or to move the key to the default
	 * packages.
	 * @param bundleName the package name, e.g. 'org.olat.core'
	 * @param key the key, e.g. 'my.key'
	 * @return
	 */
	public String translateWithPackage(String bundleName, String key) {
		return translateWithPackage(bundleName, key, null);
	}
	
	public String encodeUrl(String url) {
		return renderer.getUrlBuilder().encodeUrl(url);
	}
	
	public String encodeUrlPathSegment(String path) {
		return StringHelper.encodeUrlPathSegment(path);
	}


	/**escapes " entities in \"
	 * @param in the string to convert
	 * @deprecated please use escapeHtml.
	 * @return the escaped string
	 */
	@Deprecated
	public String escapeDoubleQuotes(String in) {
	    return Formatter.escapeDoubleQuotes(in).toString();
	}
	
	/**
	 * Escapes the characters in a String for JavaScript use.
	 */
	public String escapeJavaScript(String str) {
		return StringHelper.escapeJavaScript(str);
	}
	
	/**
	 * Escapes the characters in a String using HTML entities.
	 * @param str
	 * @return
	 */
	public String escapeHtml(String str) {
		if(str == null) {
			return "";
		}
		return StringHelper.escapeHtml(str);
	}
	
	/**
	 * Same as escapeHtml but replace " and ' by their respective entity.
	 * 
	 * @param str The text to escape
	 * @return A string suitable for use in a HTML attribute
	 */
	public String escapeForHtmlAttribute(String str) {
		if(str == null) {
			return "";
		}
		return StringHelper.escapeForHtmlAttribute(str);
	}
	
	public String xssScan(String str) {
		if(str == null) {
			return "";
		}
		OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
		return filter.filter(str);
	}
	
	public String filterHtml(String str) {
		if(str == null) {
			return "";
		}
		return FilterFactory.getHtmlTagsFilter().filter(str);
	}
	
	
	/**
	 * @param key
	 * @return
	 */
	public String translate(String key) {
		Translator trans = renderer.getTranslator();
		if (trans == null) return "{Translator is null: key_to_translate=" + key + "}";
		String res = trans.translate(key);
		if (res == null) return "?? key not found to translate: key_to_translate=" + key + " / trans info:" + trans + "}";
		return res;
	}
	
	/**
	 * Translates and escapesHTML. 
	 * It assumes that the HTML attribute value should be enclosed in double quotes.
	 * @param key
	 * @return
	 */
	public String translateInAttribute(String key) {
		return StringHelper.escapeHtml(translate(key));
	}

	/** 
	 * @return current language code as found in (current)Locale.toString() method
	 */
	public String getLanguageCode() {
		Locale currentLocale = I18nManager.getInstance().getCurrentThreadLocale();
		return currentLocale.toString();
	}

	/**
	 * 
	 * renders the component.
	 * if the component cannot be found, there is no error, but an empty String is returned. Not recommended to use normally, but rather use @see render(String componentName)
	 * 
	 * @param componentName
	 * @return
	 */
	public StringOutput renderForce(String componentName) {
		Component source = renderer.findComponent(componentName);
		StringOutput sb;
		if (source == null) {
			sb = new StringOutput(1);
		} else if (target == null) {
			sb = new StringOutput(10000);
			renderer.render(source, sb, null);
		} else {
			renderer.render(source, target, null);
		}
		return new StringOutput(1);
	}
	
	private StringOutput doRender(String componentName, String[] args) {
		Component source = renderer.findComponent(componentName);
		StringOutput sb;
		if (source == null) {
			sb = new StringOutput(128);
			sb.append(">>>>>>>>>>>>>>>>>>>>>>>>>> component " + componentName + " could not be found to be rendered!");
		} else if (target == null) {
			sb = new StringOutput(10000);
			renderer.render(source, sb, args);
		} else {
			sb = new StringOutput(1);
			renderer.render(source, target, args);
		}
		return sb;
	}
	
	public boolean isTrue(Object obj) {
		if("true".equals(obj)) {
			return true;
		}
		if(obj instanceof Boolean) {
			return ((Boolean)obj).booleanValue();
		}
		return false;
	}
	
	public boolean isFalse(Object obj) {
		if("false".equals(obj)) {
			return true;
		}
		if(obj instanceof Boolean) {
			return !((Boolean)obj).booleanValue();
		}
		return false;
	}
	
	public boolean isNull(Object obj) {
		return obj == null;
	}
	
	public boolean isNotNull(Object obj) {
		return obj != null;
	}
	
	public boolean isEmpty(Object obj) {
		boolean empty;
		if(obj == null) {
			empty = true;
		} else if(obj instanceof String) {
			empty = !StringHelper.containsNonWhitespace((String)obj) || "<p></p>".equals(obj);
		} else if(obj instanceof Collection) {
			empty = ((Collection<?>)obj).isEmpty();
		} else if(obj instanceof Map) {
			empty = ((Map<?,?>)obj).isEmpty();
		} else {
			empty = false;
		}
		return empty;
	}
	
	public boolean isNotEmpty(Object obj) {
		boolean notEmpty;
		if(obj == null) {
			notEmpty = false;
		} else if(obj instanceof String) {
			notEmpty = StringHelper.containsNonWhitespace((String)obj) && !"<p></p>".equals(obj);
		} else if(obj instanceof Collection) {
			notEmpty = !((Collection<?>)obj).isEmpty();
		} else if(obj instanceof Map) {
			notEmpty = !((Map<?,?>)obj).isEmpty();
		} else {
			notEmpty = true;
		}
		return notEmpty;
	}
	
	public boolean isNotZero(Object obj) {
		boolean notZero;
		if(obj == null) {
			notZero = false;
		} else if(obj instanceof Number) {
			notZero = ((Number)obj).intValue() != 0;
		} else if(obj instanceof Collection) {
			notZero = !((Collection<?>)obj).isEmpty();
		} else if(obj instanceof Map) {
			notZero = !((Map<?,?>)obj).isEmpty();
		} else {
			notZero = true;
		}
		return notZero;
	}
	
	public int parseInt(String text) {
		try {
			if(StringHelper.containsNonWhitespace(text)) {
				return Integer.parseInt(text);
			}
			return -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public String replace(String text, String targetString, String replacement) {
		return text.replace(targetString, replacement);
	}
	
	/**
	 * @param componentName
	 * @return true if the component with name componentName is a child of the current container. Used to "if" the render 
	 * instruction "$r.render(componentName)" if it is not known beforehand whether the component is there or not.
	 */
	public boolean available(String componentName) {
		Component source = renderer.findComponent(componentName);
		return (source != null);
	}
	
	/**
	 * @param componentName
	 * @return true if the component with name componentName is a child of the current container and if this
	 * component is visible
	 */
	public boolean visible(String componentName) {
		Component source = renderer.findComponent(componentName);
		return (source != null && source.isVisible());
	}
	
	public boolean visible(Component component) {
		return (component != null && component.isVisible());
	}
	
	public boolean visible(FormItem item) {
		if(item == null) return false;
		return visible(item.getComponent());
	}
	
	/**
	 * @param componentName
	 * @return true if the component with name componentName is a child of the current container and if this
	 * component is visible and enabled
	 */
	public boolean enabled(String componentName) {
		Component source = renderer.findComponent(componentName);
		return (source != null && source.isVisible() && source.isEnabled());
	}
	
	public boolean enabled(Component component) {
		return component != null && component.isVisible() && component.isEnabled();
	}
	
	public boolean enabled(FormItem item) {
		if(item == null) return false;
		return enabled(item.getComponent());
	}
	
	/**
	 * Return the component
	 * @param componentName
	 * @return
	 */
	public Component getComponent(String componentName) {
		return renderer.findComponent(componentName);
	}

	/**
	 * returns an object from the context of velocity
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return vc.getContext().get(key);
	}
	
	public boolean absent(String key) {
		return !vc.getContext().containsKey(key);
	}
	
	public boolean notNull(Object obj) {
		return obj != null;
	}
	
	/**
	 * Formats the given date in a short format, e.g. 05.12.2015 or 12/05/2015
	 * 
	 * @param date the date
	 * @return a String with the formatted date
	 */
	public String formatDate(Date date) {
		if(date == null) return "";
		Formatter f = Formatter.getInstance(renderer.getTranslator().getLocale());
		return f.formatDate(date);
	}
	
	/**
	 * Formats the given date in a medium sized format, e.g. 12. Dezember 2015 or December 12, 2015
	 * 
	 * @param date the date
	 * @return a String with the formatted date
	 */
	public String formatDateLong(Date date) {
		if(date == null) return "";
		Formatter f = Formatter.getInstance(renderer.getTranslator().getLocale());
		return f.formatDateLong(date);
	}
	
	/**
	 * Formats the given date in a medium size with date and time, e.g. 05.12.2015 14:35
	 * 
	 * @param date the date
	 * @return a String with the formatted date and time
	 */
	public String formatDateAndTime(Date date) {
		if(date == null) return "";
		Formatter f = Formatter.getInstance(renderer.getTranslator().getLocale());
		return f.formatDateAndTime(date);
	}

	/**
	 * Formats the given date in a long size with date and time, e.g. Tuesday,
	 * 10. September 2015, 3:48 PM
	 * 
	 * @param date
	 *            the date
	 * @return a String with the formatted date and time
	 */
	public String formatDateAndTimeLong(Date date) {
		if(date == null) return "";
		Formatter f = Formatter.getInstance(renderer.getTranslator().getLocale());
		return f.formatDateAndTimeLong(date);	}

	/**
	 * formats the given time period so it is friendly to read
	 * 
	 * @param d the date
	 * @return a String with the formatted time
	 */
	public String formatTime(Date date) {
		Formatter f = Formatter.getInstance(renderer.getTranslator().getLocale());
		return f.formatTime(date);
	}
	
	/**
	 * format a duration (in milliseconds)
	 * @param durationInMillis
	 * @return
	 */
	public String formatDurationInMillis(long durationInMillis) {
		return Formatter.formatDuration(durationInMillis);
	}
	
	public String formatBytes(long bytes) {
		return Formatter.formatBytes(bytes);
	}
	

	/**
	 * Wrapp given html code with a wrapper an add code to transform latex
	 * formulas to nice visual characters on the client side. The latex formulas
	 * must be within an HTML element that has the class 'math' attached.
	 * 
	 * @param htmlFragment A html element that might contain an element that has a
	 *          class 'math' with latex formulas
	 * @return
	 */
	public static String formatLatexFormulas(String htmlFragment) {
		return Formatter.formatLatexFormulas(htmlFragment);
	}
	
	/**
	 * Search in given text fragment for URL's and surround them with clickable
	 * HTML link objects.
	 * 
	 * @param textFragment
	 * @return text with clickable links
	 */
	public static String formatURLsAsLinks(String textFragment) {
		return Formatter.formatURLsAsLinks(textFragment);
	}	
	
 	/**
	 * Strips all HTML tags from the source string.
	 * 
	 * @param source
	 *            Source
	 * @return Source without HTML tags.
	 */
	public static String filterHTMLTags(String source) {
		return FilterFactory.getHtmlTagsFilter().filter(source);
	}
	
	/**
	 * Get the icon css class that represents the filetype based on the file name
	 * @param filename 
	 * @return The css class for the file or a default css class
	 */
	public static String getFiletypeIconCss(String filename) {
		if(filename == null) return "";
		return CSSHelper.createFiletypeIconCssClassFor(filename);
	}
	
	/**
	 * Returns true when debug mode is configured, false otherwhise
	 * @return
	 */
	public boolean isDebuging() {
		return Settings.isDebuging();
	}
	
	
	public String getVersion() {
		return Settings.getVersion();
	}
	
	public Languages getLanguages() {
		I18nManager i18nMgr = CoreSpringFactory.getImpl(I18nManager.class);
		I18nModule i18nModule = CoreSpringFactory.getImpl(I18nModule.class);
		
		Collection<String> enabledKeysSet = i18nModule.getEnabledLanguageKeys();
		Map<String, String> langNames = new HashMap<>();
		Map<String, String> langTranslators = new HashMap<>();
		String[] enabledKeys = ArrayHelper.toArray(enabledKeysSet);
		String[] names = new String[enabledKeys.length];
		for (int i = 0; i < enabledKeys.length; i++) {
			String key = enabledKeys[i];
			String langName = i18nMgr.getLanguageInEnglish(key, i18nModule.isOverlayEnabled());
			langNames.put(key, langName);
			names[i] = langName;
			String author = i18nMgr.getLanguageAuthor(key);
			langTranslators.put(key, author);
		}
		ArrayHelper.sort(enabledKeys, names, true, true, true);
		return new Languages(enabledKeys, langNames, langTranslators);
	}
	
	public static class Languages {
		private final String[] enabledKeys;
		private final Map<String, String> langNames;
		private final Map<String, String> langTranslators;
		
		public Languages(String[] enabledKeys, Map<String, String> langNames, Map<String, String> langTranslators) {
			this.enabledKeys = enabledKeys;
			this.langNames = langNames;
			this.langTranslators = langTranslators;
		}

		public String[] getEnabledKeys() {
			return enabledKeys;
		}

		public Map<String, String> getLangNames() {
			return langNames;
		}

		public Map<String, String> getLangTranslators() {
			return langTranslators;
		}
	}
}