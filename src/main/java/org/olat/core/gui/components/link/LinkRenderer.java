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

package org.olat.core.gui.components.link;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Description: Renders the link component depending of features and style. 
 * Use {@link LinkFactory} to create {@link Link} objects.
 *
 */
public class LinkRenderer extends DefaultComponentRenderer {
	private static final Logger log = Tracing.createLoggerFor(LinkRenderer.class);
	private static final Pattern singleQuote = Pattern.compile("\'");
	private static final Pattern doubleQutoe = Pattern.compile("\"");

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		Link link = (Link) source;
		
		int presentation = link.getPresentation();
		/*
		 * START && beware! order of this if's are relevant
		 */
		boolean flexiformlink = (presentation - Link.FLEXIBLEFORMLNK) >= 0;
		if (flexiformlink) {
			presentation = presentation - Link.FLEXIBLEFORMLNK;
		}
		boolean nontranslated = (presentation - Link.NONTRANSLATED) >= 0;
		if (nontranslated) {
			presentation = presentation - Link.NONTRANSLATED;
		}
		/*
		 * END && beware! order of this if's are relevant
		 */
		StringBuilder cssSb = new StringBuilder(64);
		cssSb.append("class=\"");
		if (!link.isEnabled()) {
			cssSb.append(" o_disabled  disabled ");
		}
		if (link.isActive()) {
			cssSb.append(" active ");
		}
		if (presentation == Link.BUTTON_XSMALL) {
			cssSb.append("btn btn-xs ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
			cssSb.append(link.isGhost()? " o_button_ghost": "");
		} else if (presentation == Link.BUTTON_SMALL) {
			cssSb.append("btn btn-sm ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
			cssSb.append(link.isGhost()? " o_button_ghost": "");
		} else if (presentation == Link.BUTTON) {
			cssSb.append("btn ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
			cssSb.append(link.isGhost()? " o_button_ghost": "");
		} else if (presentation == Link.BUTTON_LARGE) {
			cssSb.append("btn btn-lg ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
			cssSb.append(link.isGhost()? " o_button_ghost": "");
		} else if (presentation == Link.LINK_BACK) {
			cssSb.append("o_link_back");
		} else if (presentation == Link.TOOLENTRY_DEFAULT) {
			cssSb.append("o_toolbox_link");
		} else if (presentation == Link.TOOLENTRY_CLOSE) {
			cssSb.append("o_toolbox_close");
		} else if (presentation == Link.LINK_CUSTOM_CSS) {
			String customCss = ( link.isEnabled() ? link.getCustomEnabledLinkCSS() : link.getCustomDisabledLinkCSS() );
			cssSb.append( customCss == null ? "" : customCss );
		}
		if(StringHelper.containsNonWhitespace(link.getElementCssClass())) {
			cssSb.append(" ").append(link.getElementCssClass());
		}
		if(args != null && args.length > 0 && StringHelper.containsNonWhitespace(args[0])) {
			cssSb.append(" ").append(args[0]);
		}
		cssSb.append("\"");

		String elementId = link.getElementId();
		
		// String buffer to gather all Javascript stuff with this link
		// there is a var elementId = jQuery('#elementId');
		// allowing to reference the link as an Ext.Element 
		// Optimize initial length based on heuristic measurements of extJsSb
		StringBuilder jsSb = new StringBuilder(240); 
		boolean inForm = isInForm(args);

		// Init the text values as non-translated text...
		String title = link.getTitle();
		String linkText = (link.getCustomDisplayText() != null ? link.getCustomDisplayText() : link.getI18n());
		String ariaLabel = link.getAriaLabel();
		// .. and translate the text values if of i18n type. The values are either all
		// translated or all non-translated
		if (!nontranslated){
			if (link.getCustomDisplayText() == null) {
				linkText = (link.getI18n() == null ? null : (translator == null ? link.getI18n() : translator.translate(link.getI18n())));				
			}
			title = (link.getTitle() == null ? null : (translator == null ? link.getTitle() : translator.translate(link.getTitle())));
			ariaLabel = (link.getAriaLabel() == null ? null : (translator == null ? link.getAriaLabel() : translator.translate(link.getAriaLabel())));			
		}
		// Calculate flag for pure icons links without link text or link render component
		boolean isIconLink = (!StringHelper.containsNonWhitespace(linkText) && link.getInnerComponent() == null);
		
		// a form link can not have tooltips at the moment
		// tooltip sets its own id into the <a> tag.
		if (link.isEnabled()) {
			sb.append("<p class='form-control-static'>", inForm)
			  .append("<a ")
			  // add layouting
			  .append(cssSb);
			
			if (link.getTarget() != null) {
				sb.append(" target=\"").append(link.getTarget()).append("\"");
			}
			
			//REVIEW:pb elementId is not null if it is a form link
			//the javascript handler and the link.registerForMousePositionEvent
			//need also access to a created and id set. -> avoid "o_c"+link.getDispatchID()
			if (elementId != null) {
				sb.append(" id=\"").append(elementId).append("\" ");
			}

			String accessKey = link.getAccessKey();
			if (accessKey != null) {
				sb.append("accesskey=\"").append(accessKey).append("\" ");
			}
			if (flexiformlink) {
				renderHrefAndOnclickFormLink(renderer, sb, link.getFormItem(), ubu);
			} else {
				renderHrefAndOnclickLink(renderer, sb, link, ubu);
			}
			
			sb.append(" draggable=\"false\"");
			
			// title / tooltips, but only if not redundant with link text
			if(StringHelper.containsNonWhitespace(title) && !title.equals(linkText) && !isIconLink) {
				sb.append(" title=\"").appendHtmlAttributeEscaped(title).append("\"");
			}
			// A11y: aria-label for screen-readers, but only if not redundant with link text
			if(ariaLabel != null && !ariaLabel.equals(linkText) && !ariaLabel.equals(title)) {
				sb.append(" aria-label=\"").appendHtmlAttributeEscaped(ariaLabel).append("\"");
			}
			// A11y: Override the linke role for links that are actually buttons
			if(link.getAriaRole() != null) {
				sb.append(" role=\"").appendHtmlEscaped(link.getAriaRole()).append("\"");
			} else {
				int linkStyle = presentation;
				if ((linkStyle - Link.FLEXIBLEFORMLNK) >= 0) {
					linkStyle = linkStyle - Link.FLEXIBLEFORMLNK;
				}
				if ((linkStyle - Link.NONTRANSLATED) >= 0) {
					linkStyle = linkStyle - Link.NONTRANSLATED;
				}
				if (linkStyle >= Link.BUTTON_XSMALL && linkStyle <= Link.BUTTON_LARGE) {
					sb.append(" role=\"").appendHtmlEscaped(Link.ARIA_ROLE_BUTTON).append("\"");
				}
			}
			sb.append(">");
			
			// LEFT CSS icon
			if (link.getIconLeftCSS() != null || presentation == Link.LINK_BACK) {
				sb.append("<i class='");
				if (link.getIconLeftCSS() != null) {
					sb.append(link.getIconLeftCSS());
				} else {
					sb.append("o_icon o_icon_back o_icon-fw");
				}
				sb.append("' aria-hidden='true'");
				if(title != null && isIconLink) {
					// For icon-links add the title for non-screen-readers here so that it is not
					// read by the screenreader and add the text for screen readers in a visually
					// hidden element
					sb.append(" title=\"").appendHtmlAttributeEscaped(title).append("\"");
				}
				sb.append("></i> "); // one space needed
			}
			
			sb.append("<span"); // inner wrapper for layouting
			if(StringHelper.containsNonWhitespace(link.getLabelCSS())) {
				sb.append(" class='").append(link.getLabelCSS()).append("'");
			}
			sb.append(">");
			
			if(link.getInnerComponent() != null) {
				renderer.render(link.getInnerComponent(), sb, args);
			} else {
				if (StringHelper.containsNonWhitespace(linkText)) {
					sb.append(linkText);					
				} else {
					// a11y: try adding something invisible but speakable for screenreaders, but only if not already added during 
					if(isIconLink && StringHelper.containsNonWhitespace(title)) {
						sb.append("<span class='sr-only'>").appendHtmlEscaped(title).append("</span>");
					}
				}
			}
			sb.append("</span>");
			
			// RIGHT CSS icon
			if (link.getIconRightCSS() != null) {
				sb.append(" <i class='").append(link.getIconRightCSS()).append("'"); // one space needed
				if(StringHelper.containsNonWhitespace(title) && isIconLink) {
					// For icon-links add the title for non-screen-readers here so that it is not
					// read by the screenreader and add the text for screen readers in a visually
					// hidden element
					sb.append(" title=\"").appendHtmlAttributeEscaped(title).append("\"");
				}
				sb.append(" aria-hidden='true'></i> "); 
			}
						
			if(link.getBadge() != null) {
				renderer.render(link.getBadge(), sb, args);
			}
			sb.append("</a>").append("</p>", inForm);
			
			//on click() is part of prototype.js
			if(link.isRegisterForMousePositionEvent()) {
				jsSb.append(elementId).append(".click(function(event) {")
			       .append(" jQuery('#").append(elementId).append("').each(function(index, el) {;")
			       .append("  var href = jQuery(el).attr('href');")
			       .append("    if(href.indexOf('x') == -1) jQuery(el).attr('href',href+'x'+event.pageX+'y'+event.pageY+'');")
			       .append(" });});");
			}
			/**
			 * this binds the event to the function call as argument, useful if event is needed
			 * Event.observe("id", "click", functionName.bindAsEventListener(this));
			 */
			if(link.getJavascriptHandlerFunction() != null) {
				jsSb.append(elementId).append(".on('").append(link.getMouseEvent()).append("', ").append(link.getJavascriptHandlerFunction()).append(");");
			}	
			
			/**
			 * Focus link so that it can be invoked using the enter key using a keyboard. 
			 */
			if(link.isFocus()) {								
				jsSb.append("document.getElementById('").append(elementId).append("').focus(");
				if(link.isPreventFocusScroll()) {
					jsSb.append("{preventScroll:true}");
				}
				jsSb.append(");");
			}
		} else {
			// DISABLED rendering 
			sb.append("<a ");
			if (elementId != null) sb.append(" id=\"").append(elementId).append("\" ");
			
			String description = link.getTextReasonForDisabling();
			// fallback to title
			if (description == null) description = link.getTitle();
			if (description != null) {
				Matcher msq = singleQuote.matcher(description);
				description = msq.replaceAll("&#39;");
				Matcher mdq = doubleQutoe.matcher(description);
				description = mdq.replaceAll("\\\\\"");
				sb.append(" title=\"").append(description).append("\" ");
			}
			sb.append(cssSb).append(" href='#' onclick='return false;'>");

			// CSS icon
			if (link.getIconLeftCSS() != null) {
				sb.append("<i class='").append(link.getIconLeftCSS()).append("'");
				sb.append(" aria-hidden='true'></i> "); // one space needed
			}			

			if (StringHelper.containsNonWhitespace(linkText)) {				
				sb.append("<span>").append(linkText).append("</span>");
			}
			
			// CSS icon
			if (link.getIconRightCSS() != null) {
				sb.append(" <i class='").append(link.getIconRightCSS()).append("'"); // one space needed
				sb.append(" aria-hidden='true'></i> "); 
			}			

			sb.append("</a>");
		}
		
		if(link.getTarget() != null){
			//if the link starts a download -> the o_afterserver is not called in
			//non-ajax mode if a download is started.
			//on click execute the "same" javascript as in o_ainvoke(r,true) for
			//case 3:
			jsSb.append("if (").append(elementId).append(") ")
		        .append(elementId).append(".click(function() {setTimeout(removeBusyAfterDownload,1200)});");
		}

		//disabled or not, all tags should be closed here
		//now append all gathered javascript stuff if any
		if(jsSb.length() > 0) {
			// Execute code within an anonymous function (closure) to not leak
			// variables to global scope (OLAT-5755)
			sb.append(" <script>\n")
			  .append("(function(){ var ").append(elementId).append(" = jQuery('#").append(elementId).append("');")
			  .append(jsSb).append("})();")
		      .append("\n</script>");
		}
		
		if (elementId != null && link.hasTooltip() && StringHelper.containsNonWhitespace(title)) {
			sb.append("<script>")
			  .append("\"use strict\";\n")
			  .append("jQuery(function() {\n")
			  .append("  jQuery('#").append(elementId).append(" i", isIconLink).append("').tooltip({\n")
			  .append("    html: true,\n")
			  .append("    placement: '").append(link.getTooltipPosition()).append("',\n")
			  .append("    container: 'body',\n")
			  .append("    title: \"").appendHtmlEscaped(title).append("\" \n")
			  .append("  });\n")
			  .append("});")
			  .append("</script>");
		}
		
		// Dev alert for missing a11y text
		if (Settings.isDebuging()) {
			if (isIconLink && !StringHelper.containsNonWhitespace(title)) {
				log.warn("A11y issue: icon-only links must always have a title, please fix your code for link:: " + link.getComponentName() + " with URL::" + link.getUrl() + " and icon::" + link.getIconLeftCSS());
			}
			
		}
	}
	
	private void renderHrefAndOnclickLink(Renderer renderer, StringOutput sb, Link link, URLBuilder ubu) {
		String command = link.getCommand();
		if(link.isPopup()) {
			try(StringOutput href = new StringOutput()) {
				LinkPopupSettings popup = link.getPopup();
				ubu.buildURI(href, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command }, null, AJAXFlags.MODE_NORMAL);
				boolean hasUrl = StringHelper.containsNonWhitespace(link.getUrl());
				String hrefUrl = hasUrl ? link.getUrl() : "javascript:;";
				sb.append("href=\"").append(hrefUrl).append("\" onclick=\"o_openPopUp('").append(href).append("','")
				  .append(popup.getTarget()).append("',").append(popup.getWidth())
				  .append(",").append(popup.getHeight()).append("); return false;\" ");
			} catch(IOException e) {
				log.error("", e);
			}
		} else if(link.isNewWindow()) {
			if(link.isNewWindowAfterDispatchUrl()) {
				AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
				boolean iframePostEnabled = flags.isIframePostEnabled() && link.isAjaxEnabled();
				String target = StringHelper.containsNonWhitespace(link.getTarget()) ? link.getTarget() : "_blank";	
				ubu.buildHrefAndOnclick(sb, link.getUrl(), null, iframePostEnabled, !link.isSuppressDirtyFormWarning(), false,
						new NameValuePair(VelocityContainer.COMMAND_ID, command),
						new NameValuePair("oo-opennewwindow-oo", "true"),
						new NameValuePair("oo-opennewwindow-target", target));
			} else {
				try(StringOutput href = new StringOutput()) {
					ubu.buildURI(href, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command }, null, AJAXFlags.MODE_NORMAL);
					sb.append("href=\"javascript:;\" onclick=\"o_openTab('").append(href).append("'); return false;\" ");
				} catch(IOException e) {
					log.error("", e);
				}
			}
		} else {
			AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
			 // a link may force a non ajax-mode and a custom targ
			boolean iframePostEnabled = flags.isIframePostEnabled() && link.isAjaxEnabled() && link.getTarget() == null;
			ubu.buildHrefAndOnclick(sb, link.getUrl(), null, iframePostEnabled, !link.isSuppressDirtyFormWarning(), true,
					new NameValuePair(VelocityContainer.COMMAND_ID, command));
		}
	}
	
	private void renderHrefAndOnclickFormLink(Renderer renderer, StringOutput sb, FormLink flexiLink, URLBuilder ubu) {
		Link link = flexiLink.getComponent();
		String elementId = link.getElementId();
		if(flexiLink.isNewWindow()) {
			if(flexiLink.isNewWindowAfterDispatchUrl()) {
				String target = StringHelper.containsNonWhitespace(link.getTarget()) ? link.getTarget() : "_blank";	
				boolean hasUrl = StringHelper.containsNonWhitespace(link.getUrl());
				String href = hasUrl ? link.getUrl() : "javascript:;";
				sb.append("href=\"").append(href).append("\" onclick=\"")
				  .append(FormJSHelper.getXHRFnCallFor(flexiLink, false, false, flexiLink.isNewWindowWithSubmit(),
						  new NameValuePair("oo-opennewwindow-oo", "true"),
						  new NameValuePair("oo-opennewwindow-target", target)))
				  .append("; return false;\"");
			} else {
				String dispatchUri = flexiLink.getFormDispatchId();
				URLBuilder subu = ubu.createCopyFor(flexiLink.getRootForm().getInitialComponent());
				try(StringOutput href = new StringOutput()) {
					subu.buildURI(href, AJAXFlags.MODE_NORMAL,
							new NameValuePair("dispatchuri", dispatchUri),
							new NameValuePair("dispatchevent", "2"),
							new NameValuePair(Form.FORM_CSRF, renderer.getCsrfToken()));
					sb.append("href=\"javascript:;\" onclick=\"o_openTab('").append(href).append("'); return false;\" ");
				} catch(IOException e) {
					log.error("", e);
				}
			}
		} else if(flexiLink.isPopup()) {
			LinkPopupSettings popup = link.getPopup();
			String dispatchUri = flexiLink.getFormDispatchId();
			URLBuilder subu = ubu.createCopyFor(flexiLink.getRootForm().getInitialComponent());
			try(StringOutput href = new StringOutput()) {
				subu.buildURI(href, AJAXFlags.MODE_NORMAL,
						new NameValuePair("dispatchuri", dispatchUri),
						new NameValuePair("dispatchevent", "2"),
						new NameValuePair(Form.FORM_CSRF, renderer.getCsrfToken()));
				sb.append("href=\"javascript:;\" onclick=\"o_openPopUp('").append(href).append("','")
				  .append(popup.getTarget()).append("',").append(popup.getWidth())
				  .append(",").append(popup.getHeight()).append(",").append(popup.isMenuBar())
				  .append("); return false;\" ");
			} catch(IOException e) {
				log.error("", e);
			}
		} else if(link.isForceFlexiDirtyFormWarning()) {
			sb.append("href=\"javascript:")
			  .append(FormJSHelper.getJSFnCallFor(flexiLink.getRootForm(), elementId, 1))
			  .append(";\" ")
			  .append("onclick=\"return o2cl_dirtyCheckOnly();\" ");
		} else {
			boolean hasUrl = StringHelper.containsNonWhitespace(link.getUrl());
			String href = hasUrl ? link.getUrl() : "javascript:;";
			// Prevent 2 submits by onchange and click (button or submit) events
			sb.append("href=\"").append(href).append("\" onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" onclick=\"")
			  .append(FormJSHelper.getJSFnCallFor(flexiLink.getRootForm(), elementId, 1))
			  .append("; ").append(" return false;", hasUrl).append("\" ");
		}
	}
	
	private final boolean isInForm(String[] args) {
		boolean embedded = false;
		if(args != null && args.length > 0) {
			for(String arg:args) {
				if("form".equals(arg)) {
					embedded = true;
				}
			}
		}
		return embedded;
	}
}
