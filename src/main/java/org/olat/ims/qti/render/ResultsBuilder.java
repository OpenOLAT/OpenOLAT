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

package org.olat.ims.qti.render;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.container.DecimalVariable;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.ItemInput;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIHelper;
import org.olat.ims.qti.process.Resolver;

/**
 * @author Felix Jost
 */
public class ResultsBuilder {
	/**
	 * <code>STATICS_PATH</code>
	 */
	public static final String STATICS_PATH = "staticspath";

	/**
	 * Constructor for ResultsRenderer.
	 */
	public ResultsBuilder() {
		super();
	}

	/**
	 * Method getResDoc.
	 * 
	 * @param ai The assessment instance
	 * @param locale The users locale
	 * @param identity
	 * @return Document The XML document
	 */
	public Document getResDoc(AssessmentInstance ai, Locale locale, Identity identity) {
		AssessmentContext ac = ai.getAssessmentContext();
		DocumentFactory df = DocumentFactory.getInstance();
		Document res_doc = df.createDocument();
		Element root = df.createElement("qti_result_report");
		res_doc.setRootElement(root);
		Element result = root.addElement("result");
		Element extension_result = result.addElement("extension_result");

		String baseUrl = ai.getResolver().getStaticsBaseURI() + "/";
		// add items (not qti standard, but nice to display original questions ->
		// put it into extensions)
		//extension_result.
		for (int i = 0; i < ac.getSectionContextCount(); i++) {
			SectionContext sectionCtx = ac.getSectionContext(i);
			for (int j = 0; j < sectionCtx.getItemContextCount(); j++) {
				// OO-148  
				// on some occasions this did throw an IllegalAddException
				// because el_item had already a parent. 
				// make a clone for adding to extension_result
				Element el_item = (Element) sectionCtx.getItemContext(j).getEl_item().clone();
				recurseMattextForMediaURLFiltering(baseUrl, el_item);
				extension_result.add(el_item);
			}
		}

		// add ims cp id for any media references
		addStaticsPath(extension_result, ai);

		// add assessment_result

		// Add User information
		Element context = result.addElement("context");
		User user = identity.getUser();
		String name = user.getProperty(UserConstants.FIRSTNAME, locale) + " " + user.getProperty(UserConstants.LASTNAME, locale);
		String instId = user.getProperty(UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, locale);
		String instName = user.getProperty(UserConstants.INSTITUTIONALNAME, locale);

		if (instId == null) instId = "N/A";
		context.addElement("name").addText(name);

		String institution;
		if (instName == null) {
			institution = "N/A";
		} else {
			institution = instName;
		}
		// Add institutional identifier (e.g. Matrikelnummer)
		Element generic_identifier = context.addElement("generic_identifier");
		generic_identifier.addElement("type_label").addText(institution);
		generic_identifier.addElement("identifier_string").addText(instId);

		// Add start and stop date formatted as datetime
		Element beginDate = context.addElement("date");
		beginDate.addElement("type_label").addText("Start");
		beginDate.addElement("datetime").addText(Formatter.formatDatetime(new Date(ac.getTimeOfStart())));
		Element stopDate = context.addElement("date");
		stopDate.addElement("type_label").addText("Stop");
		stopDate.addElement("datetime").addText(Formatter.formatDatetime(new Date(ac.getTimeOfStop())));

		Element ares = result.addElement("assessment_result");
		ares.addAttribute("ident_ref", ac.getIdent());
		if (ac.getTitle() != null) {
			ares.addAttribute("asi_title", ac.getTitle());
		}

		// process assessment score
		Element a_score = ares.addElement("outcomes").addElement("score");
		a_score.addAttribute("varname", "SCORE");
		String strVal = StringHelper.formatFloat(ac.getScore(), 2);
		a_score.addElement("score_value").addText(strVal);

		strVal = ac.getMaxScore() == -1.0f ? "N/A" : StringHelper.formatFloat(ac.getMaxScore(), 2);
		a_score.addElement("score_max").addText(strVal);

		strVal = ac.getCutvalue() == -1.0f ? "N/A" : StringHelper.formatFloat(ac.getCutvalue(), 2);
		a_score.addElement("score_cut").addText(strVal);

		a_score.addElement("num_nan_score_items").addText(Integer.toString(ac.getNumberOfItemsWithNanValueScore()));

		addElementText(ares, "duration", QTIHelper.getISODuration(ac.getDuration()));
		addElementText(ares, "num_sections", "" + ac.getSectionContextCount());
		addElementText(ares, "num_sections_presented", "0");
		addElementText(ares, "num_items", "" + ac.getItemContextCount());
		addElementText(ares, "num_items_presented", "" + ac.getItemsPresentedCount());
		addElementText(ares, "num_items_attempted", "" + ac.getItemsAttemptedCount());

		// add section_result
		int secnt = ac.getSectionContextCount();
		for (int i = 0; i < secnt; i++) {
			SectionContext secc = ac.getSectionContext(i);
			Element secres = ares.addElement("section_result");
			secres.addAttribute("ident_ref", secc.getIdent());
			if (secc.getTitle() != null) {
				secres.addAttribute("asi_title", secc.getTitle());
			}
			addElementText(secres, "duration", QTIHelper.getISODuration(secc.getDuration()));
			addElementText(secres, "num_items", "" + secc.getItemContextCount());
			addElementText(secres, "num_items_presented", "" + secc.getItemsPresentedCount());
			addElementText(secres, "num_items_attempted", "" + secc.getItemsAttemptedCount());

			// process section score
			Element sec_score = secres.addElement("outcomes").addElement("score");
			sec_score.addAttribute("varname", "SCORE");
			strVal = secc.getScore() == -1.0f ? "N/A" : "" + StringHelper.formatFloat(secc.getScore(), 2);
			sec_score.addElement("score_value").addText(strVal);
			strVal = secc.getMaxScore() == -1.0f ? "N/A" : "" + StringHelper.formatFloat(secc.getMaxScore(), 2);
			sec_score.addElement("score_max").addText(strVal);
			strVal = secc.getCutValue() == -1 ? "N/A" : "" + secc.getCutValue();
			sec_score.addElement("score_cut").addText(strVal);
			sec_score.addElement("num_nan_score_items").addText(Integer.toString(secc.getNumberOfItemsWithNanValueScore()));

			// iterate over all items in this section context
			List<ItemContext> itemsc = secc.getSectionItemContexts();
			for (Iterator<ItemContext> it_it = itemsc.iterator(); it_it.hasNext();) {
				ItemContext itemc = it_it.next();
				Element itres = secres.addElement("item_result");
				itres.addAttribute("ident_ref", itemc.getIdent());
				itres.addAttribute("asi_title", itemc.getEl_item().attributeValue("title"));
				Element it_duration = itres.addElement("duration");
				it_duration.addText(QTIHelper.getISODuration(itemc.getTimeSpent()));

				// process item score
				DecimalVariable scoreVar = (DecimalVariable) (itemc.getVariables().getSCOREVariable());
				Element it_score = itres.addElement("outcomes").addElement("score");
				it_score.addAttribute("varname", "SCORE");
				it_score.addElement("score_value").addText(StringHelper.formatFloat(scoreVar.getTruncatedValue(false), 2));
				strVal = scoreVar.hasMinValue() ? "" + scoreVar.getMinValue() : "0.0";
				it_score.addElement("score_min").addText(strVal);
				strVal = scoreVar.hasMaxValue() ? "" + scoreVar.getMaxValue() : "N/A";
				it_score.addElement("score_max").addText(strVal);
				strVal = scoreVar.hasCutValue() ? "" + scoreVar.getCutValue() : "N/A";
				it_score.addElement("score_cut").addText(strVal);

				Element el_item = itemc.getEl_item();
				Map<String, Element> res_responsehash = new HashMap<>(3);

				// iterate over all responses of this item
				List resps = el_item.selectNodes(".//response_lid|.//response_xy|.//response_str|.//response_num|.//response_grp");
				for (Iterator it_resp = resps.iterator(); it_resp.hasNext();) {
					Element resp = (Element) it_resp.next();
					String ident = resp.attributeValue("ident");
					String rcardinality = resp.attributeValue("rcardinality");
					String rtiming = resp.attributeValue("rtiming");

					// add new response
					Element res_response = itres.addElement("response");
					res_response.addAttribute("ident_ref", ident);
					res_responsehash.put(ident, res_response); // enable lookup of
																										 // @identref of <response>
																										 // (needed with <varequal>
																										 // elements

					// add new response_form
					//<response_lid ident="MR01" rcardinality="Multiple" rtiming="No">
					Element res_responseform = res_response.addElement("response_form");
					res_responseform.addAttribute("cardinality", rcardinality);
					res_responseform.addAttribute("timing", rtiming);
					String respName = resp.getName();
					String type = respName.substring(respName.indexOf("_") + 1);
					res_responseform.addAttribute("response_type", type);

					// add user answer
					ItemInput itemInp = itemc.getItemInput();
					Translator trans = Util.createPackageTranslator(QTIModule.class, locale);
					if (itemInp == null) { // user did not answer this question at all
						res_response.addElement("response_value").addText(trans.translate("ResBuilder.NoAnswer"));
					} else {
						List<String> userAnswer = itemInp.getAsList(ident);
						if (userAnswer == null) { // user did not answer this question at
																			// all
							res_response.addElement("response_value").addText(trans.translate("ResBuilder.NoAnswer"));
						} else { // the user chose at least one option of an answer (did not
										 // simply click send)
							for (Iterator<String> it_ans = userAnswer.iterator(); it_ans.hasNext();) {
								res_response.addElement("response_value").addText(it_ans.next());
							}
						}
					}

				}

				/*
				 * The simple element correct_response can only list correct elements,
				 * that is, no "or" or "and" elements may be in the conditionvar.
				 * Pragmatic solution: if condition has ors or ands, then put whole
				 * conditionvar into <extension_response> (proprietary), and for easier
				 * cases (just "varequal" "not" elements) use correct_response.
				 */

				Map<String,Set<String>> corr_answers = new HashMap<>(); // keys: respIdents, values: HashSet
																					// of correct answers for this
																					// respIdent
				List respconds = el_item.selectNodes(".//respcondition");
				for (Iterator it_respc = respconds.iterator(); it_respc.hasNext();) {
					Element el_respc = (Element) it_respc.next();

					// check for add/set in setvar elements (check for single instance
					// only -> spec allows for multiple instances)
					Element el_setvar = (Element) el_respc.selectSingleNode(".//setvar");
					if (el_setvar == null) continue;
					if (el_setvar.attributeValue("action").equals("Add") || el_setvar.attributeValue("action").equals("Set")) {
						// This resrocessing gives points -> assume correct answer
						float numPoints = 0;
						try {
							numPoints = Float.parseFloat(el_setvar.getTextTrim());
						} catch (NumberFormatException nfe) {
							//  
						}
						if (numPoints <= 0) continue;
						Element conditionvar = (Element) el_respc.selectSingleNode(".//conditionvar");
						// there is an evaluation defined (a "resprocessing" element exists)
						// if (xpath(count(.//varequal) + count(.//not) = count(.//*)) is
						// true, then there are only "not" and "varequal" elements
						XPath xCanHandle = DocumentHelper.createXPath("count(.//varequal) + count(.//not) = count(.//*)");
						boolean canHandle = xCanHandle.matches(conditionvar);
						if (!canHandle) { // maybe we have <condvar> <and> <...>, try again
							Element el_and = (Element) conditionvar.selectSingleNode("and");
							if (el_and != null) {
								canHandle = xCanHandle.matches(el_and);
								if (canHandle) { // simultate the el_and to be the conditionvar
									conditionvar = el_and;
								}
							} else { // and finally, maybe we have an <or> element ..
								Element el_or = (Element) conditionvar.selectSingleNode("or");
								if (el_or != null) {
									canHandle = xCanHandle.matches(el_or);
									if (canHandle) { // simultate the el_and to be the conditionvar
										conditionvar = el_or;
									}
								}
							}
						}

						if (!canHandle) {
							// qti res 1.2.1 can't handle it
							Element condcopy = conditionvar.createCopy();
							itres.addElement("extension_item_result").add(condcopy);
						} else {
							/*
							 * easy case: get all varequal directly under the conditionvar
							 * element and assume the "not" elements do not contain "not"
							 * elements again... <!ELEMENT response (qti_comment? ,
							 * response_form? , num_attempts? , response_value* ,
							 * extension_response?)> <!ELEMENT response_form
							 * (correct_response* , extension_responseform?)> <!ELEMENT
							 * correct_response (#PCDATA)>
							 */
							List vareqs = conditionvar.selectNodes("./varequal");
							for (Iterator it_vareq = vareqs.iterator(); it_vareq.hasNext();) {
								/*
								 * get the identifier of the response, so that we can attach the
								 * <correct_response> to the right <response> element quote: ims
								 * qti asi xml binding :3.6.23.1 <varequal> Element: respident
								 * (required). The identifier of the corresponding
								 * <response_lid>, <response_xy>, etc. element (this was
								 * assigned using its ident attribute).
								 */
								Element vareq = (Element) it_vareq.next();
								String respIdent = vareq.attributeValue("respident");
								Set<String> respIdent_corr_answers = corr_answers.get(respIdent);
								if (respIdent_corr_answers == null) respIdent_corr_answers = new HashSet<>(3);
								respIdent_corr_answers.add(vareq.getText());
								corr_answers.put(respIdent, respIdent_corr_answers);
							} // for varequal
						} // else varequal
					} // add/set setvar
				} // for resprocessing
				Set<String> resp_ids = corr_answers.keySet();
				for (Iterator<String> idents = resp_ids.iterator(); idents.hasNext();) {
					String respIdent = idents.next();
					Set<String> respIdent_corr_answers = corr_answers.get(respIdent);
					Element res_response = res_responsehash.get(respIdent);
					Element res_respform = res_response.element("response_form");
					for (Iterator<String> iter = respIdent_corr_answers.iterator(); iter.hasNext();) {
						String answer = iter.next();
						res_respform.addElement("correct_response").addText(answer);
					}
				}
			} // for response_xy
		}
		return res_doc;
	}
	
	private void recurseMattextForMediaURLFiltering(String baseUrl, Element el) {
		@SuppressWarnings("unchecked")
		List<Element> children = el.elements();
		for(int i=children.size(); i-->0; ) {
			Element child = children.get(i);
			recurseMattextForMediaURLFiltering(baseUrl, child);
			
			String name = child.getName();
			if("mattext".equals(name)) {
				Object cdata = child.getData();
				if(cdata instanceof String) {
					String content = (String)cdata;	
					Filter urlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl);
					String withBaseUrl = urlFilter.filter(content);
					if(!content.equals(withBaseUrl)) {
						child.setText(withBaseUrl);
					}
				}
			}
		}
	}

	/**
	 * Strip extension_result and section_result tags
	 * 
	 * @param doc
	 */
	public static void stripDetails(Document doc) {
		detachNodes("//extension_result", doc);
		detachNodes("//section_result", doc);
	}

	/**
	 * Strip Item-Result tags.
	 * 
	 * @param doc
	 */
	public static void stripItemResults(Document doc) {
		detachNodes("//item_result", doc);
	}

	private void addElementText(Element parent, String child, String text) {
		Element el = parent.addElement(child);
		el.setText(text);
	}

	private static void detachNodes(String xPath, Document doc) {
		List xpathres = doc.selectNodes(xPath);
		for (Iterator iter = xpathres.iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			element.detach();
		}
	}

	private static void addStaticsPath(Element el_in, AssessmentInstance ai) {
		Element el_staticspath = (Element) el_in.selectSingleNode(STATICS_PATH);
		if (el_staticspath == null) {
			DocumentFactory df = DocumentFactory.getInstance();
			el_staticspath = df.createElement(STATICS_PATH);
			Resolver resolver = ai.getResolver();
			el_staticspath.addAttribute("ident", resolver.getStaticsBaseURI());
			el_in.add(el_staticspath);
		}
	}

}