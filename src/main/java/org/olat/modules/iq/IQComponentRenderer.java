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
*/

package org.olat.modules.iq;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Element;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.Output;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.container.qtielements.GenericQTIElement;
import org.olat.ims.qti.container.qtielements.Hint;
import org.olat.ims.qti.container.qtielements.Item;
import org.olat.ims.qti.container.qtielements.ItemFeedback;
import org.olat.ims.qti.container.qtielements.Material;
import org.olat.ims.qti.container.qtielements.Objectives;
import org.olat.ims.qti.container.qtielements.RenderInstructions;
import org.olat.ims.qti.container.qtielements.Solution;
import org.olat.ims.qti.navigator.Info;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.Resolver;

/**
 *
 * @author Felix Jost
 */
public class IQComponentRenderer implements ComponentRenderer {
	
	/**
	 * Default constructor
	 */
	public IQComponentRenderer() {
		super();
	}
	
	/**
	 * Render the QTI form
	 * @param comp
	 * @param translator
	 * @param renderer
	 * @return rendered form
	 */
	public StringOutput buildForm(IQComponent comp, Translator translator, Renderer renderer, URLBuilder ubu) {
		StringOutput sb = new StringOutput();
		Info info = comp.getAssessmentInstance().getNavigator().getInfo();
		AssessmentInstance ai = comp.getAssessmentInstance();
		AssessmentContext act = ai.getAssessmentContext();
		boolean displaySingleItemFeedback = false;

		// first treat messages and errors
		if (info.containsMessage()) {
			switch (info.getMessage()) {
				case QTIConstants.MESSAGE_ITEM_SUBMITTED :					
					//item hints?
					if (info.isHint()) {
						Hint el_hint = info.getCurrentOutput().getHint();
						if (el_hint.getFeedbackstyle() == Hint.FEEDBACKSTYLE_INCREMENTAL) {
							// increase the hint level so we know which hint to display
							ItemContext itc = act.getCurrentSectionContext().getCurrentItemContext();
							int nLevel = itc.getHintLevel() + 1;
							int numofhints = el_hint.getChildCount();
							if (nLevel > numofhints) nLevel = numofhints;
							itc.setHintLevel(nLevel);
							//<!ELEMENT hint (qticomment? , hintmaterial+)>
							
							displayFeedback(sb, (GenericQTIElement)el_hint.getChildAt(nLevel-1), ai, translator.getLocale());
						} else {
							displayFeedback(sb, el_hint, ai, translator.getLocale());
						}
					}
					//item solution?
					if (info.isSolution()) {
						Solution el_solution = info.getCurrentOutput().getSolution();
						displayFeedback(sb, el_solution, ai, translator.getLocale());
					}
					// item fb?
					displaySingleItemFeedback = renderFeedback(comp, info, sb, ai, translator);
					
					if(!comp.getMenuDisplayConf().isEnabledMenu() && comp.getMenuDisplayConf().isItemPageSequence() && !info.isRenderItems()) {
						//if item was submitted and sequence is pageSequence and menu not enabled and isRenderItems returns false show section info
					  SectionContext sc = ai.getAssessmentContext().getCurrentSectionContext();
					  displaySectionInfo(sb, sc, ai, comp, ubu, translator);
					}
					break;

				case QTIConstants.MESSAGE_SECTION_SUBMITTED :
					//	provide section feedback if enabled and existing
					//SectionContext sc = act.getCurrentSectionContext();					
					if (info.isFeedback()) {
						Output outp = info.getCurrentOutput();
						GenericQTIElement el_feedback = outp.getEl_response();
						if (el_feedback != null) {
							displayFeedback(sb, el_feedback, ai, translator.getLocale());
						} else {
							displaySingleItemFeedback = renderFeedback(comp, info, sb, ai, translator);
						}
					}
					if(!comp.getMenuDisplayConf().isEnabledMenu() && !comp.getMenuDisplayConf().isItemPageSequence()) {
					  SectionContext sc = ai.getAssessmentContext().getCurrentSectionContext();
					  displaySectionInfo(sb, sc, ai, comp, ubu, translator);
					}
					break;

				case QTIConstants.MESSAGE_ASSESSMENT_SUBMITTED :
					//	provide assessment feedback if enabled and existing
					if (info.isFeedback()) {
						Output outp = info.getCurrentOutput();
						GenericQTIElement el_feedback = outp.getEl_response();
						if (el_feedback != null) displayFeedback(sb, el_feedback, ai, translator.getLocale());
					}
					break;

				case QTIConstants.MESSAGE_SECTION_INFODEMANDED : // for menu item navigator
					// provide some stats maybe
					SectionContext sc = ai.getAssessmentContext().getCurrentSectionContext();
					displaySectionInfo(sb, sc, ai, comp, ubu, translator);
					break;

				case QTIConstants.MESSAGE_ASSESSMENT_INFODEMANDED : // at the start of the test
					displayAssessmentInfo(sb, act, ai, comp, ubu, translator);
					break;
			}
		}

		if (info.isRenderItems()) {
			boolean displayForm = true;
			// First check wether we need to render a form.
			// No form is needed if the current item has a matapplet object to be displayed.
			// Matapplets will send their response back directly.
			SectionContext sct = act.getCurrentSectionContext();
			ItemContext itc = null;
			if (sct != null && !ai.isSectionPage()) {
				itc = sct.getCurrentItemContext();
				if (itc != null) {
					Item item = itc.getQtiItem();
					if (item.getQTIIdent().startsWith("QTIEDIT:FLA:")) displayForm = false;
				}
			}
			
			//do not display form with button in case no more item is open
			if (sct != null && ai.isSectionPage()) {
				displayForm = sct.getItemsOpenCount() > 0;
			}
			
			sb.append("<form action=\"");
			ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "sitse" });
			
			sb.append("\" id=\"ofo_iq_item\" method=\"post\">");

			String memoId = null;
			String memoTx = "";
			boolean memo = comp.provideMemoField();
			
			if (!ai.isSectionPage()) {
				if (itc != null) {
					displayItem(sb, renderer, ubu, itc, ai, displaySingleItemFeedback);
					if (memo) {
						memoId = itc.getIdent();
						memoTx = ai.getMemo(memoId);
					}
				}
			} else {
				if (sct != null && sct.getItemContextCount() != 0) {
					displayItems(sb, renderer, ubu, sct, ai, displaySingleItemFeedback);
					if (memo) {
						memoId = sct.getIdent();
						memoTx = ai.getMemo(memoId);
					}
				}
			}
			
			boolean isDefaultMemo = false;
			if (memo) {
				if (memoTx == null) {
					isDefaultMemo = true;
					memoTx = translator.translate("qti.memofield.text");
				}
			}
			
			sb.append("<div class=\"row\">");
			sb.append("<div class='o_button_group'>");
			
			if (!displaySingleItemFeedback) {
				// render submit button
				sb.append("<input class=\"btn btn-primary\" type=\"submit\" name=\"olat_fosm\" value=\"");
				if (ai.isSectionPage()) {
					sb.append(StringEscapeUtils.escapeHtml(translator.translate("submitMultiAnswers")));
				} else {
					sb.append(StringEscapeUtils.escapeHtml(translator.translate("submitSingleAnswer")));
				}
				sb.append("\"");
				if (!displayForm) {
					sb.append(" style=\"display: none;\"");
				}
				sb.append(" />").append("</div><div class='col-md-10'>");
			} else {
				// render "next" button to proceed with subsequent item
				sb.append("<a class=\"btn btn-primary\" onclick=\"return o2cl()\" href=\"");
				ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "gitnext" });
				final String title = translator.translate("next");
				sb.append("\" title=\"" + StringEscapeUtils.escapeHtml(title) + "\">");
				sb.append("<span>").append(title).append("</title>");
				sb.append("</a>");
			}
			sb.append("</div><div class='col-md-10'>");

			if (memo && memoId != null) {
				sb.append("<div class=\"o_qti_item_note_box\">");
				sb.append("<label class=\"control-label\" for=\"o_qti_item_note\">").append(translator.translate("qti.memofield")).append("</label>");
				sb.append("<textarea id=\"o_qti_item_note\" class=\"form-control\" rows=\"4\" spellcheck=\"false\" onchange=\"memo('");
				sb.append(memoId);
				sb.append("', this.value);\" onkeyup=\"resize(this);\" onmouseup=\"resize(this);\"");
				if (isDefaultMemo) {
					sb.append(" onfocus=\"clrMemo(this);\"");
				}
				sb.append(">")
				  .append(memoTx)
				  .append("</textarea>")
				  .append("</div>");
			}
			
			sb.append("</div>")//end memo
			  .append("</div>")//end row
			  .append("</form>");
		}
		
		if (info.getStatus() == QTIConstants.ASSESSMENT_FINISHED) {
			if (info.isFeedback()) {
				Output outp = info.getCurrentOutput();
				GenericQTIElement el_feedback = outp.getEl_response();
				if (el_feedback != null) {
					displayFeedback(sb, el_feedback, ai, null);
				} else {
					displaySingleItemFeedback = renderFeedback(comp, info, sb, ai, translator);
					
					//add the next button
					sb.append("<a class=\"btn btn-primary\" onclick=\"return o2cl()\" href=\"");
					ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "sitsec" });
					String title = translator.translate("next"); 
					sb.append("\" title=\"" + StringHelper.escapeHtml(title) + "\">");
					sb.append("<span>").append(title).append("</span>");
					sb.append("</a>");
				}
			}
		}
		return sb;
	}

	/**
	 *
	 * @param comp
	 * @param info
	 * @param sb
	 * @param ai
     * @param translator
	 * @return displaySingleItemFeedback
     */
	protected boolean renderFeedback(IQComponent comp, Info info, StringOutput sb, AssessmentInstance ai, Translator translator) {
		if (info.isFeedback() && info.getCurrentOutput().hasItem_Responses()) {
			final int fbcount = info.getCurrentOutput().getFeedbackCount();
			int i=0;
			while (i < fbcount) {
				Element elemAnswerChosen = info.getCurrentOutput().getItemAnswerChosen(i);
				if (elemAnswerChosen != null) {
					sb.append("<br /><br /><i>");
					displayFeedback(sb, new Material(elemAnswerChosen), ai, translator.getLocale());
					sb.append("</i>");
				}
				Element elemFeedback = info.getCurrentOutput().getItemFeedback(i);
				displayFeedback(sb, new ItemFeedback(elemFeedback), ai, translator.getLocale());
				i++;
			}

			// if Menu not visible or if visible but not selectable and itemPage sequence (one question per page)
			// display feedback for CURRENT item and render "next" button to proceed with subsequent item
			final IQMenuDisplayConf menuDisplayConfig = comp.getMenuDisplayConf();
			if (!menuDisplayConfig.isEnabledMenu() && menuDisplayConfig.isItemPageSequence()) {
				return true;
			}
		}
		return false;
	}

	protected static String getFormattedLimit(long millis) {
		long sSec = millis / 1000;
		long sMin = sSec / 60;
		sSec = sSec - (sMin * 60);
		StringBuilder sb = new StringBuilder();
		sb.append(sMin).append("'&nbsp;").append(sSec).append("\"");
		return sb.toString();
	}
	
	private StringOutput addItemLink(Renderer r, URLBuilder ubu ,Formatter formatter, AssessmentInstance ai, ItemContext itc, int sectionPos, int itemPos,
	 boolean clickable, boolean active, boolean info) {
		StringOutput sb = new StringOutput();

		sb.append("<td>");
		String titleNotEscaped = itc.getEl_item().attributeValue("title", "no title");
		String titleShort = StringHelper.escapeHtml(Formatter.truncate(titleNotEscaped, 27));
		long maxdur = itc.getDurationLimit();
		long start = itc.getTimeOfStart();
		long due = start + maxdur;
		boolean started = (start != -1);
		boolean timelimit = (maxdur != -1);
		String fdue = (started && timelimit ? formatter.formatTimeShort(new Date(due)) : null);
		if (active) {
			sb.append("<div class=\"o_qti_menu_item_active\">");
		} else {
			if (itc.isOpen() && clickable) {
				sb.append("<div class=\"o_qti_menu_item\">");
			} else {
				sb.append("<div class=\"o_qti_menu_item_inactive\">");
			}
		}
		
		if (clickable) {
			sb.append("<a onclick=\"return o2cl();\" href=\"");
			ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "git" });
			sb.append("?itid="	+ itemPos	+ "&seid=" + sectionPos);
			sb.append("\" class=\"o_sel_qti_menu_item\" title=\"" + StringHelper.escapeHtml(titleNotEscaped) + "\">");
		}
		
		sb.append("<b>" + (sectionPos + 1) + "." + (itemPos + 1) + ".</b>&nbsp;");	
		sb.append(titleShort);
			
		if (clickable) {
			sb.append("</a>");
		}
		
		sb.append("</div>");
		sb.append("</td>");

		
		if (!itc.isOpen()) {
			sb.append("<td></td>"); // no time limit symbol
			// add lock image
			sb.append("<td>");
			sb.append("<div class='o_qti_closed_icon' title=\"");
			sb.appendHtmlEscaped(r.getTranslator().translate("itemclosed"));
			sb.append("\"><i class='o_icon o_icon_locked'> </i></div>");
			sb.append("</td>");
		} else if (info) {
			// max duration info
			sb.append("<td>");
			if (maxdur != -1) {
					sb.append("<div class='o_qti_timelimit_icon' title=\"");
					if (!itc.isStarted()) {
						sb.appendHtmlEscaped(r.getTranslator().translate("timelimit.initial", new String[] {getFormattedLimit(maxdur)}));
					} else  {
						sb.appendHtmlEscaped(r.getTranslator().translate("timelimit.running", new String[] {fdue}));
					}
					sb.append("\" ><i class='o_icon o_icon_timelimit'> </i></div>");
			}
			sb.append("</td>");
			
			sb.append("<td>");
			// attempts info
			int maxa = itc.getMaxAttempts();
			int attempts = itc.getTimesAnswered();
			if (maxa != -1) { // only limited times of answers
				sb.append("<div class='o_qti_attemptslimit_icon' title=\"");
				sb.appendHtmlEscaped(r.getTranslator().translate("attemptsleft", new String[] {"" + (maxa - attempts)}));
				sb.append("\" ><i class='o_icon o_icon_attempt_limit'> </i></div>");
			}
			sb.append("</td>");
		}
		
		
		sb.append("<td>");
		sb.append("<div id=\""+itc.getIdent()+"\" class=\"o_qti_menu_item_attempts");
		String t = Integer.toString(itc.getTimesAnswered());
		String n = r.getTranslator().translate("qti.marker.title", new String[]{t});
		String m = r.getTranslator().translate("qti.marker.title.marked", new String[]{t});
		if (ai.isMarked(itc.getIdent())) {
			sb.append("_marked");
		}
		sb.append("\" onclick=\"mark(this, '").append(n).append("','").append(m).append("')\" ");
		sb.append("title=\"");
		sb.append(ai.isMarked(itc.getIdent()) ? m : n);
		sb.append("\">");
		sb.append(t);
		sb.append(" </div></td>");
		
		return sb;
	}

	// menu stuff
	private StringOutput addSectionLink(Renderer r, URLBuilder ubu, Formatter formatter, SectionContext sc, int sectionPos, boolean clickable, boolean active, boolean pagewise) {
		StringOutput sb = new StringOutput();

		// section link
		sb.append("<td>");
		String title = StringHelper.escapeHtml(Formatter.truncate(sc.getTitle(), 30));
		long maxdur = sc.getDurationLimit();
		long start = sc.getTimeOfStart();
		long due = start + maxdur;
		boolean started = (start != -1);
		boolean timelimit = (maxdur != -1);
		String fdue = (started && timelimit ? formatter.formatTimeShort(new Date(due)) : null);
		
		if (!sc.isOpen()) clickable = false;
		
		if (active) {
			if(pagewise) {
				sb.append("<div class=\"o_qti_menu_section_active\">");
			} else {
				sb.append("<div class=\"o_qti_menu_section\">");
			}
		} else {
			if (pagewise) {
				sb.append("<div class=\"o_qti_menu_section_clickable\">");
			} else {
				sb.append("<div class=\"o_qti_menu_section\">");
			}
		}
		
		if (clickable) {
			sb.append("<a onclick=\"return o2cl()\" href=\"");
			ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "gse" });
			sb.append("?seid=" + sectionPos);
			sb.append("\" title=\"" + StringHelper.escapeHtml(sc.getTitle()) + "\">");
		}
		sb.append("<b>" + (sectionPos + 1) + ".</b>&nbsp;");
		sb.append(title);
		if (clickable) {
			sb.append("</a>");
		}
		sb.append("</div>");
		sb.append("</td>");
		
		sb.append("<td>");
		if (!sc.isOpen()) {
			sb.append("<div class='o_qti_closed_icon' title=\"");
			sb.appendHtmlEscaped(r.getTranslator().translate("itemclosed"));
			sb.append("\"><i class='o_icon o_icon_locked'> </i></div>");
		} else {
			// max duration info
			if (maxdur != -1) {
					sb.append("<div class='o_qti_timelimit_icon' title=\"");
					if (!sc.isStarted()) {
						sb.appendHtmlEscaped(r.getTranslator().translate("timelimit.initial", new String[] {getFormattedLimit(maxdur)}));
					} else  {
						sb.appendHtmlEscaped(r.getTranslator().translate("timelimit.running", new String[] {fdue}));
					}
					sb.append("\" ><i class='o_icon o_icon_timelimit'> </i></div>");
			}
		}
		sb.append("</td>");
		
		sb.append("<td colspan=\"2\"></td>");
		
		return sb;
	}

	/**
	 * Method buildMenu.
	 *
	 * @return DOCUMENT ME!
	 */
	private StringOutput buildMenu(IQComponent comp, Translator translator, Renderer r, URLBuilder ubu) {
		StringOutput sb = new StringOutput();
		AssessmentInstance ai = comp.getAssessmentInstance();
		AssessmentContext ac = ai.getAssessmentContext();
		boolean renderSectionTitlesOnly = comp.getMenuDisplayConf().isRenderSectionsOnly();

		sb.append("<div id=\"o_qti_menu\">");
		sb.append("<h4>");
		sb.append(StringHelper.escapeHtml(ac.getTitle()));
		sb.append("</h4>");

		sb.append("<table border=0 width=\"100%\">");

		// append assessment navigation
		Formatter formatter = Formatter.getInstance(translator.getLocale());
		int scnt = ac.getSectionContextCount();
		for (int i = 0; i < scnt; i++) {
			SectionContext sc = ac.getSectionContext(i);
			boolean clickable = (ai.isSectionPage() && sc.isOpen()) || (!ai.isSectionPage());
			clickable = clickable && !ai.isClosed();
			clickable = clickable && ai.isMenu();
			
			sb.append("<tr>");
			sb.append(addSectionLink(r, ubu, formatter, sc, i, clickable, ac.getCurrentSectionContextPos() == i, ai.isSectionPage()));
			sb.append("</tr>");
			
			if (!renderSectionTitlesOnly) {
				//not only sections, but render questions to
				int icnt = sc.getItemContextCount();
				for (int j = 0; j < icnt; j++) {
					ItemContext itc = sc.getItemContext(j);
					clickable = !ai.isSectionPage() && sc.isOpen() && itc.isOpen();
					clickable = clickable && !ai.isClosed();
					clickable = clickable && ai.isMenu();
					sb.append("<tr>");
					sb.append(addItemLink(r, ubu, formatter, ai, itc, i, j, clickable,
							(ac.getCurrentSectionContextPos() == i && sc.getCurrentItemContextPos() == j), !ai.isSurvey()));
					sb.append("</tr>");
				}
			}
		}
		sb.append("</table>");
		sb.append("</div>");
		return sb;
	}

	private void displayItems(StringOutput sb, Renderer renderer, URLBuilder ubu, SectionContext sc, AssessmentInstance ai, boolean readOnly) {
		// display the whole current section on one page
		List<ItemContext> items = sc.getItemContextsToRender();
		for (ItemContext itc:items) {
			if (itc.isOpen()) {
			  displayItem(sb, renderer, ubu, itc, ai, readOnly);
			} else {
				displayItemClosed(sb,renderer);
			}
		}
	}

	/**
	 * Display message : Item is closed, could not be displayed.
	 * @param sb
	 * @param renderer
	 * @param itc
	 */
	private void displayItemClosed(StringOutput sb, Renderer renderer) {
		StringBuilder buffer = new StringBuilder(100);		
		buffer.append("<div class=\"o_warning\"><strong>").append(renderer.getTranslator().translate("couldNotDisplayItem")).append("</strong></div>");
		sb.append(buffer);
	}
	
	private void displayItem(StringOutput sb, Renderer renderer, URLBuilder ubu, ItemContext itc, AssessmentInstance ai, boolean readOnly) {
		StringBuilder buffer = new StringBuilder(1000);
		Resolver resolver = ai.getResolver();
		RenderInstructions ri = new RenderInstructions();
		ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
		ri.put(RenderInstructions.KEY_LOCALE, renderer.getTranslator().getLocale());
		StringOutput soCommandURI = new StringOutput(50);
		ubu.buildURI(soCommandURI, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "sflash" });
		ri.put(RenderInstructions.KEY_APPLET_SUBMIT_URI, soCommandURI.toString());
		if (itc.getItemInput() != null)
			ri.put(RenderInstructions.KEY_ITEM_INPUT, itc.getItemInput());
		ri.put(RenderInstructions.KEY_RENDER_TITLE, Boolean.valueOf(ai.isDisplayTitles()));
		
		if (ai.isAutoEnum()) {
			String k = renderer.getTranslator().translate("choices.autoenum.keys");
			if (k!=null) {
				ri.put(RenderInstructions.KEY_RENDER_AUTOENUM_LIST, k);
			}
		}
		ri.put(RenderInstructions.KEY_RENDER_MODE, readOnly ? RenderInstructions.RENDER_MODE_STATIC : RenderInstructions.RENDER_MODE_FORM);
		itc.getQtiItem().render(buffer, ri);
		sb.append(buffer);
	}
	
	private void displaySectionInfo(StringOutput sb, SectionContext sc, AssessmentInstance ai, IQComponent comp, URLBuilder ubu, Translator translator) {
		// display the sectionInfo
		if (sc == null) return;
		if (ai.isDisplayTitles())
			sb.append("<h3>" + sc.getTitle() + "</h3>");
		Objectives objectives = sc.getObjectives();
		if (objectives != null) {
			StringBuilder sbTmp = new StringBuilder();
			Resolver resolver = ai.getResolver();
			RenderInstructions ri = new RenderInstructions();
			ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
			objectives.render(sbTmp, ri);
			sb.append(sbTmp);
		}
    // if Menu not visible, or if visible but not selectable, and itemPage sequence (one question per page)  
		// show button to navigate to the first question of the current section			
		IQMenuDisplayConf menuDisplayConfig = comp.getMenuDisplayConf();
		if (!menuDisplayConfig.isEnabledMenu() && menuDisplayConfig.isItemPageSequence()) {
			sb.append("<a class=\"btn btn-default\" onclick=\"return o2cl()\" href=\"");
			ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "git" });
			AssessmentContext ac = ai.getAssessmentContext();
			int sectionPos = ac.getCurrentSectionContextPos();
			sb.append("?itid=" + 0 + "&seid=" + sectionPos);
			String title = translator.translate("next"); 
			sb.append("\" title=\"" + StringHelper.escapeHtml(title) + "\">");
			sb.append("<span>").append(title).append("</span>");
			sb.append("</a>");
		}		
	}

	private void displayAssessmentInfo(StringOutput sb, AssessmentContext ac, AssessmentInstance ai, IQComponent comp, URLBuilder ubu, Translator translator) {
		Objectives objectives = ac.getObjectives();
		if (objectives != null) {
			StringBuilder sbTmp = new StringBuilder();
			Resolver resolver = ai.getResolver();
			RenderInstructions ri = new RenderInstructions();
			ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
			objectives.render(sbTmp, ri);
			sb.append(sbTmp);
		}
		//if Menu not visible, or if visible but not selectable show button to navigate to the first section panel			
		IQMenuDisplayConf menuDisplayConfig = comp.getMenuDisplayConf();
		if (!menuDisplayConfig.isEnabledMenu()) {
			sb.append("<a class=\"btn btn-default\" onclick=\"return o2cl()\" href=\"");
			ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "gse" });
			sb.append("?seid=" + 0);				
			String title = translator.translate("next"); 
			sb.append("\" title=\"" + StringHelper.escapeHtml(title) + "\">");	
			sb.append("<span>").append(title).append("</span>");
			sb.append("</a>");
		}				
	}

	private void displayFeedback(StringOutput sb, GenericQTIElement feedback, AssessmentInstance ai, Locale locale) {
		StringBuilder sbTmp = new StringBuilder();
		Resolver resolver = ai.getResolver();
		RenderInstructions ri = new RenderInstructions();
		ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
		ri.put(RenderInstructions.KEY_LOCALE, locale);
		feedback.render(sbTmp, ri);
		sb.append(sbTmp);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

		IQComponent qticomp = (IQComponent)source;

		if (args[0].equals("menu")) { // render the menu
			target.append(buildMenu(qticomp, translator, renderer, ubu));
		} else if (args[0].equals("qtiform")) { // render the content
			target.append(buildForm(qticomp, translator, renderer, ubu));
		}		
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		//
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		//
	}
}
