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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.apache.logging.log4j.Logger;
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
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		Link link = (Link) source;
		String command = link.getCommand();
		AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
		
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
			cssSb.append(" o_disabled ");
		}
		if (link.isActive()) {
			cssSb.append(" active ");
		}
		if (presentation == Link.BUTTON_XSMALL) {
			cssSb.append("btn btn-xs ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
		} else if (presentation == Link.BUTTON_SMALL) {
			cssSb.append("btn btn-sm ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
		} else if (presentation == Link.BUTTON) {
			cssSb.append("btn ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
		} else if (presentation == Link.BUTTON_LARGE) {
			cssSb.append("btn btn-lg ");
			cssSb.append(link.isPrimary() ? "btn-primary" : "btn-default");
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
		cssSb.append("\"");

		if (link.isEnabled()) {
			// only set a target on an enabled link, target in span makes no sense
			if (link.getTarget() != null){
				cssSb.append(" target=\"").append(link.getTarget()).append("\"");
			}
		}

		String elementId = link.getElementId();
		
		// String buffer to gather all Javascript stuff with this link
		// there is a var elementId = jQuery('#elementId');
		// allowing to reference the link as an Ext.Element 
		// Optimize initial length based on heuristic measurements of extJsSb
		StringBuilder jsSb = new StringBuilder(240); 
		boolean inForm = isInForm(args);

		String i18n = link.getI18n();
		String title = link.getTitle();
		String customDisplayText = link.getCustomDisplayText();
		
		// a form link can not have tooltips at the moment
		// tooltip sets its own id into the <a> tag.
		if (link.isEnabled()) {
			sb.append("<p class='form-control-static'>", inForm)
			  .append("<a ")
			  // add layouting
			  .append(cssSb);
			
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
				FormLink flexiLink = link.getFlexiForm();
				if(flexiLink.isNewWindow()) {
					String dispatchUri = flexiLink.getFormDispatchId();
					URLBuilder subu = ubu.createCopyFor(flexiLink.getRootForm().getInitialComponent());
					try(StringOutput href = new StringOutput()) {
						subu.buildURI(href, AJAXFlags.MODE_NORMAL,
								new NameValuePair("dispatchuri", dispatchUri),
								new NameValuePair("dispatchevent", "2"));
						sb.append("href=\"javascript:;\" onclick=\"o_openTab('").append(href).append("'); return false;\" ");
					} catch(IOException e) {
						log.error("", e);
					}
				} else if(flexiLink.isPopup()) {
					LinkPopupSettings popup = link.getPopup();
					String dispatchUri = flexiLink.getFormDispatchId();
					URLBuilder subu = ubu.createCopyFor(flexiLink.getRootForm().getInitialComponent());
					try(StringOutput href = new StringOutput()) {
						subu.buildURI(href, AJAXFlags.MODE_NORMAL,
								new NameValuePair("dispatchuri", dispatchUri),
								new NameValuePair("dispatchevent", "2"));
						sb.append("href=\"javascript:;\" onclick=\"o_openPopUp('").append(href).append("','")
						  .append(popup.getTarget()).append("',").append(popup.getWidth())
						  .append(",").append(popup.getHeight()).append("); return false;\" ");
					} catch(IOException e) {
						log.error("", e);
					}
				} else {
					sb.append("href=\"javascript:")
					  .append(FormJSHelper.getJSFnCallFor(flexiLink.getRootForm(), elementId, 1))
					  .append(";\" ");
					if(link.isForceFlexiDirtyFormWarning()) {
						sb.append("onclick=\"return o2cl_dirtyCheckOnly();\" ");
					}
				}
			} else if(link.isPopup()) {
				try(StringOutput href = new StringOutput()) {
					LinkPopupSettings popup = link.getPopup();
					ubu.buildURI(href, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command }, null, AJAXFlags.MODE_NORMAL);
					sb.append("href=\"javascript:;\" onclick=\"o_openPopUp('").append(href).append("','")
					  .append(popup.getTarget()).append("',").append(popup.getWidth())
					  .append(",").append(popup.getHeight()).append("); return false;\" ");
				} catch(IOException e) {
					log.error("", e);
				}
			} else if(link.isNewWindow()) {
				try(StringOutput href = new StringOutput()) {
					ubu.buildURI(href, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command }, null, AJAXFlags.MODE_NORMAL);
					sb.append("href=\"javascript:;\" onclick=\"o_openTab('").append(href).append("'); return false;\" ");
				} catch(IOException e) {
					log.error("", e);
				}
			} else {
				 // a link may force a non ajax-mode and a custom targ
				boolean iframePostEnabled = flags.isIframePostEnabled() && link.isAjaxEnabled() && link.getTarget() == null;
				ubu.buildHrefAndOnclick(sb, null, iframePostEnabled, !link.isSuppressDirtyFormWarning(), true,
						new NameValuePair(VelocityContainer.COMMAND_ID, command));
			}
			
			//tooltips
			if(title != null) {
				if (!link.isHasTooltip()) {
					sb.append(" title=\"");
					if (nontranslated){
						sb.appendHtmlEscaped(title).append("\"");
					} else {
						sb.appendHtmlEscaped(translator.translate(title)).append("\"");
					}
				}
				//tooltips based on the extjs library, see webapp/static/js/ext*
				if (link.isHasTooltip()) {
					String text;
					if (nontranslated) {
						text = title;
					} else {
						text = translator.translate(title);
					}
					sb.append(" title=\"").appendHtmlEscaped(text).append("\"");
				}
			}
			sb.append(">");
			
			// CSS icon
			if (link.getIconLeftCSS() != null) {
				sb.append("<i class='").append(link.getIconLeftCSS()).append("'");
				sb.append("></i> "); // one space needed
			} else if (presentation == Link.LINK_BACK) {
				sb.append("<i class='o_icon o_icon_back'> </i> "); // one space needed				
			}
			
			sb.append("<span>"); // inner wrapper for layouting
			if (customDisplayText != null) {
				//link is not translated but has custom text
				sb.append(customDisplayText);
			}	else if (nontranslated) {
				if (i18n != null) {
					// link name is not a i18n key
					sb.append(i18n);
				}
			} else {
				// use translator
				if(translator == null) {
					sb.append("Ohoho");
				} else {
					sb.append(translator.translate(i18n));
				}
			}
			sb.append("</span>");
			
			// CSS icon
			if (link.getIconRightCSS() != null) {
				sb.append(" <i class='").append(link.getIconRightCSS()).append("'"); // one space needed
				sb.append("></i> "); 
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
			       .append(" 	if(href.indexOf('x') == -1) jQuery(el).attr('href',href+'x'+event.pageX+'y'+event.pageY+'');")
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
				jsSb.append(elementId).append(".focus();");
			}
		} else {
			String text;
			if (customDisplayText != null) {
				//link is not translated but has custom text
				text = customDisplayText;
			}	else if (nontranslated) {
				// link name is not a i18n key
				text = (i18n == null ? "" : i18n);
			} else {
				text = translator.translate(i18n);
			}
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
				sb.append("></i> "); // one space needed
			}			

			sb.append("<span>").append(text).append("</span>");
			
			// CSS icon
			if (link.getIconRightCSS() != null) {
				sb.append(" <i class='").append(link.getIconRightCSS()).append("'"); // one space needed
				sb.append("></i> "); 
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
