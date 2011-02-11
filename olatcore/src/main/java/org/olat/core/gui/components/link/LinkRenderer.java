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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.core.gui.components.link;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;

/**
 * Description: Renders the link component depending of features and style. 
 * Use {@link LinkFactory} to create {@link Link} objects.
 *
 */
public class LinkRenderer implements ComponentRenderer {
	private static Pattern singleQuote = Pattern.compile("\'");
	private static Pattern doubleQutoe = Pattern.compile("\"");

	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		Link link = (Link) source;
		String command = link.getCommand();
		AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
		
		boolean iframePostEnabled = flags.isIframePostEnabled() && link.isAjaxEnabled() && link.getTarget() == null; // a link may force a non ajax-mode and a custom targ

		int presentation = link.getPresentation();
		/*
		 * START && beware! order of this if's are relevant
		 */
		boolean flexiformlink = (presentation - Link.FLEXIBLEFORMLNK) >= 0;
		if (flexiformlink) {
			presentation = presentation - Link.FLEXIBLEFORMLNK;
			if (link.tooltipContent != null) {
				throw new AssertException("Form link does not support long transaction, tooltip yet!");
			}
		}
		boolean nontranslated = (presentation - Link.NONTRANSLATED) >= 0;
		if (nontranslated) {
			presentation = presentation - Link.NONTRANSLATED;
		}
		/*
		 * END && beware! order of this if's are relevant
		 */
		StringBuilder cssSb = new StringBuilder("");
		cssSb.append("class=\"");
		if (!link.isEnabled()) {
			cssSb.append(" b_disabled ");
		}
		if (presentation == Link.BUTTON_XSMALL) {
			cssSb.append("b_button b_xsmall");
		} else if (presentation == Link.BUTTON_SMALL) {
			cssSb.append("b_button b_small");
		} else if (presentation == Link.BUTTON) {
			cssSb.append("b_button");
		} else if (presentation == Link.LINK_BACK) {
			cssSb.append("b_link_back");
		} else if (presentation == Link.TOOLENTRY_DEFAULT) {
			cssSb.append("b_toolbox_link");
		} else if (presentation == Link.TOOLENTRY_CLOSE) {
			cssSb.append("b_toolbox_close");
		} else if (presentation == Link.LINK_CUSTOM_CSS) {
			String customCss = ( link.isEnabled() ? link.getCustomEnabledLinkCSS() : link.getCustomDisabledLinkCSS() );
			cssSb.append( customCss == null ? "" : customCss );
		}
		cssSb.append("\"");

		if (link.isEnabled()) {
			// only set a target on an enabled link, target in span makes no sense
			if (link.getTarget() != null){
				cssSb.append(" target=\""+ link.getTarget() +"\"");
			}	else if (iframePostEnabled && link.isEnabled() && !flexiformlink) {
				//flexi form link is excluded because the form post goes to the
				//iframe
				StringOutput so = new StringOutput();
				ubu.appendTarget(so);
				cssSb.append(so.toString());
			}
		}

		String elementId = link.getElementId();
		
		// String buffer to gather all Javascript stuff with this link
		// there is a var elementId = Ext.get('elementId');
		// allowing to reference the link as an Ext.Element 
		// Optimize initial length based on heuristic measurements of extJsSb
		StringBuilder extJsSb = new StringBuilder(240); 
		extJsSb.append(" <script type=\"text/javascript\">\n/* <![CDATA[ */\n");
		// Execute code within an anonymous function (closure) to not leak
		// variables to global scope (OLAT-5755)
		extJsSb.append("(function(){");
		extJsSb.append("var ");
		extJsSb.append(elementId);
		extJsSb.append(" = Ext.get('");
		extJsSb.append(elementId);
		extJsSb.append("');");
		//

		boolean hasExtJsSb = false;

		String i18n = link.getI18n();
		String title = link.getTitle();
		if(title != null)	title = StringEscapeUtils.escapeHtml(title).toString();
		String customDisplayText = link.getCustomDisplayText();

		// a form link can not have tooltips at the moment
		// tooltip sets its own id into the <a> tag.
		if (link.isEnabled()) {
			if (link.markIt) {
				sb.append("<span style=\"border:2px solid red;\">");
			}

			sb.append("<a ");
			// add layouting
			sb.append(cssSb);
			
			//REVIEW:pb elementId is not null if it is a form link
			//the javascript handler and the link.registerForMousePositionEvent
			//need also access to a created and id set. -> avoid "o_c"+link.getDispatchID()
			// FIXME:pb:a refactor for 5.3
			if (elementId != null) sb.append(" id=\"").append(elementId).append("\" ");

			String accessKey = link.getAccessKey();
			if (accessKey != null) {
				sb.append("accesskey=\"").append(accessKey).append("\" ");
			}
			if (flexiformlink) {
				//no target if flexi form link! because target is set on 
				//post action of form
				Form theForm = (Form)link.getInternalAttachedObject();
				sb.append("href=\"javascript:");
				sb.append(FormJSHelper.getJSFnCallFor(theForm, elementId, 1));
				sb.append("\" ");
			} else {
				sb.append("href=\"");

				ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { command },
						iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
				sb.append("\"");
				//if link only has backgroud image (no translation), we need to set qtip here because of empty span
				if (nontranslated && link.hasTooltip && link.getCustomDisplayText() == null) {
					if (nontranslated) {
						sb.append(" ext:qtip=\"").append(title).append("\"");
					} else {
						sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate(title))).append("\"");
					}
				}
			}

			if (/* !link.isEnabledForLongTransaction && */!flexiformlink) {
				// clash with onclick ... FIXME:pb/as find better solution to solve this
				// problem.
				String clickCmd = (link.isSuppressDirtyFormWarning() ? "o2c=0;return o2cl();" : "return o2cl();");
				// only catch click event - modern browser fire click event even
				// when event was triggered by keyboard
				sb.append(" onclick=\"").append(clickCmd).append("\">");
			} else {
				sb.append(">");
			}
			sb.append("<span "); // inner wrapper for layouting
			
			//tooltips
			if(title != null) {
				if (!link.hasTooltip) {
					sb.append(" title=\"");
					if (nontranslated){
						sb.append(title).append("\"");
					} else {
						sb.append(StringEscapeUtils.escapeHtml(translator.translate(title))).append("\"");
					}
				}
				//tooltips based on the extjs library, see webapp/static/js/ext*
				if (link.hasTooltip) {
					if (nontranslated) {
						sb.append(" ext:qtip=\"").append(title).append("\"");
					} else {
						sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate(title))).append("\"");
					}
				}
				if (link.hasStickyTooltip) {
					//sb.append(" ext:hide=\"user\"");
				}
			}
			sb.append(">");
			
			if (customDisplayText != null) {
				//link is nontranslated but has custom text
				sb.append(customDisplayText);
			}	else if (nontranslated) {
				if (i18n != null) {
					// link name is not a i18n key
					sb.append(i18n);
				} else {
					sb.append("");
				}
			} else {
				// use translator
				sb.append(translator.translate(i18n));
			}
			sb.append("</span></a>");
			if (link.markIt) {
				sb.append("</span>");
			}
			//Event.observe() is part of prototype.js
			if(link.registerForMousePositionEvent) {
				extJsSb.append(" Event.observe(\""+elementId+"\", \"click\", function(event) {");
				extJsSb.append(" var link = $('" + elementId + "');");
				// Uncomment next line for live JS debugging
				//extJsSb.append(" B_AjaxLogger.logDebug(link.getAttribute(\"href\"), 'o_c"+link.getDispatchID()+" - " + elementId + "'); ");
				extJsSb.append(" if (link.getAttribute(\"href\").indexOf(\"/x\") == -1) link.setAttribute(\"href\", link.href+\"x\"+Event.pointerX(event)+\"y\"+Event.pointerY(event)+\"\");");
				extJsSb.append("});");
				hasExtJsSb = true;
				
			}
			/**
			 * TODO:gs:b may be usefull as well
			 * this binds the event to the function call as argument, usefull if event is needed
			 * Event.observe("id", "click", functionName.bindAsEventListener(this));
			 */
			if(link.javascriptHandlerFunction != null) {
				extJsSb.append("  Event.observe(\""+elementId+"\", \""+link.mouseEvent+"\", "+link.javascriptHandlerFunction+");");
				hasExtJsSb = true;
			}
			
			if (link.tooltipContent != null) {
				extJsSb.append("Ext.QuickTips.tips({");
				extJsSb.append("target: '").append(elementId).append("',");
				//FIXME:FG:Check component containing single quotes or line breaks
				String clearedContent = renderer.render(link.tooltipContent).toString().replaceAll("\n", "");
				extJsSb.append("text: '").append(clearedContent).append("',");
				extJsSb.append("autoHide: ").append(String.valueOf(!link.hasStickyTooltip));
				extJsSb.append("});");
				hasExtJsSb=true;
			}
		} else {
			String text;
			if (customDisplayText != null) {
				//link is nontranslated but has custom text
				text = customDisplayText;
			}	else if (nontranslated) {
				// link name is not a i18n key
				text = (i18n == null ? "" : i18n);
			} else {
				text = translator.translate(i18n);
			}
			sb.append("<span ");
			String description = link.getTextReasonForDisabling();
			// fallback to title
			if (description == null) description = link.getTitle();
			if (description != null) {
				Matcher msq = singleQuote.matcher(description);
				description = msq.replaceAll("&#39;");
				Matcher mdq = doubleQutoe.matcher(description);
				description = mdq.replaceAll("\\\\\"");
				sb.append(" ext:qtip=\"").append(description).append("\" ");
			}
			sb.append(cssSb).append(">").append(text).append("</span>");
		}
		if(link.getStartsDownload() || link.getTarget() != null){
			//if the link starts a download -> the o_afterserver is not called in
			//non-ajax mode if a download is started.
			//on click execute the "same" javascript as in o_ainvoke(r,true) for
			//case 3:
			hasExtJsSb = true;
			StringBuilder sbj = new StringBuilder();
			// examples:
			// o_lnk400.on({'click',removeBusyAfterDownload,document,{formId:"ofo_100"}};);
			sbj.append("if (").append(elementId).append(") ").append(elementId).append(".on(\"click");
			sbj.append("\",removeBusyAfterDownload,document,{delay: 1200}); ");
			extJsSb.append(sbj.toString());
		}
		
		//disabled or not, all tags should be closed here
		//now append all gathered javascript stuff if any
		if(hasExtJsSb){
			// Execute anonymous function (closure) now (OLAT-5755)
			extJsSb.append("})();");
			extJsSb.append("\n/* ]]> */\n</script>");
			sb.append(extJsSb);
		}
	}

	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
	// TODO Auto-generated method stub
	}

	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
	// TODO Auto-generated method stub
	}
}
