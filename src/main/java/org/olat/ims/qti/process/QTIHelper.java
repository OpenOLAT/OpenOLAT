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

package org.olat.ims.qti.process;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.container.DecimalVariable;
import org.olat.ims.qti.container.Variable;
import org.olat.ims.qti.container.Variables;
import org.olat.ims.qti.process.elements.BooleanEvaluable;
import org.olat.ims.qti.process.elements.ExpressionBuilder;
import org.olat.ims.qti.process.elements.QTI_and;
import org.olat.ims.qti.process.elements.QTI_item;
import org.olat.ims.qti.process.elements.QTI_not;
import org.olat.ims.qti.process.elements.QTI_or;
import org.olat.ims.qti.process.elements.QTI_other;
import org.olat.ims.qti.process.elements.QTI_respcondition;
import org.olat.ims.qti.process.elements.QTI_resprocessing;
import org.olat.ims.qti.process.elements.QTI_varequal;
import org.olat.ims.qti.process.elements.QTI_vargt;
import org.olat.ims.qti.process.elements.QTI_vargte;
import org.olat.ims.qti.process.elements.QTI_varinside;
import org.olat.ims.qti.process.elements.QTI_varlt;
import org.olat.ims.qti.process.elements.QTI_varlte;
import org.olat.ims.qti.process.elements.ScoreBooleanEvaluable;
import org.olat.ims.qti.process.elements.section.QTI_and_selection;
import org.olat.ims.qti.process.elements.section.QTI_and_test;
import org.olat.ims.qti.process.elements.section.QTI_not_selection;
import org.olat.ims.qti.process.elements.section.QTI_not_test;
import org.olat.ims.qti.process.elements.section.QTI_or_selection;
import org.olat.ims.qti.process.elements.section.QTI_or_test;
import org.olat.ims.qti.process.elements.section.QTI_selection_metadata;
import org.olat.ims.qti.process.elements.section.QTI_variable_test;
import org.olat.ims.resources.IMSEntityResolver;

/**
 */
public class QTIHelper {

	private static class QTIDocument {
		
		private final Long date;
		private final byte[] content;
		
		public QTIDocument(Long date, byte[] content) {
			this.date = date;
			this.content = content;
		}
	}
	private static CacheWrapper<String,QTIDocument> ehCachLoadedQTIDocs = CoordinatorManager.getInstance()
			.getCoordinator().getCacher().getCache(QTIHelper.class.getSimpleName(), "QTI_xml_Documents");
	/**
	 * 
	 */
	private static Map<String,BooleanEvaluable> booleanEvals;
	private static Map<String,ScoreBooleanEvaluable> scoreBooleanEvals;
	private static Map<String,ExpressionBuilder> expressionBuilders;

	private final static long sec = 1000;
	private final static long minute = 60 * sec;
	private final static long hour = 60 * minute;
	private final static long day = 24 * hour;
	private final static long year = 365 * day;
	private final static long month = 30 * day;

	static {
		booleanEvals = new HashMap<>();
		booleanEvals.put("and", new QTI_and());
		booleanEvals.put("or", new QTI_or());
		booleanEvals.put("not", new QTI_not());
		booleanEvals.put("varequal", new QTI_varequal());
		booleanEvals.put("vargte", new QTI_vargte());
		booleanEvals.put("vargt", new QTI_vargt());
		booleanEvals.put("varlte", new QTI_varlte());
		booleanEvals.put("varlt", new QTI_varlt());
		booleanEvals.put("varinside", new QTI_varinside());
		booleanEvals.put("other", new QTI_other());

		// section boolean evaluables
		scoreBooleanEvals = new HashMap<>();
		scoreBooleanEvals.put("and_test", new QTI_and_test());
		scoreBooleanEvals.put("or_test", new QTI_or_test());
		scoreBooleanEvals.put("not_test", new QTI_not_test());
		scoreBooleanEvals.put("variable_test", new QTI_variable_test());

		// ims qti sao
		expressionBuilders = new HashMap<>();
		expressionBuilders.put("and_selection", new QTI_and_selection());
		expressionBuilders.put("or_selection", new QTI_or_selection());
		expressionBuilders.put("not_selection", new QTI_not_selection());
		expressionBuilders.put("selection_metadata", new QTI_selection_metadata());
	}

	private static QTI_respcondition respcondition = new QTI_respcondition();
	private static QTI_resprocessing resprocessing = new QTI_resprocessing();
	// private static QTI_or_selection or_selection = new QTI_or_selection();
	private static QTI_item QtiItem = new QTI_item();

	/**
	 * @return
	 */
	public static QTI_resprocessing getQTI_resprocessing() {
		return resprocessing;
	}

	/**
	 * 
	 */
	public static QTI_respcondition getQTI_respcondition() {
		return respcondition;
	}

	/**
	 * 
	 */
	public static QTI_and getQTI_and() {
		return (QTI_and) booleanEvals.get("and");
	}

	/**
	 * @param name
	 * @return
	 */
	public static BooleanEvaluable getBooleanEvaluableInstance(String name) {
		BooleanEvaluable bev = booleanEvals.get(name);
		if (bev == null) throw new RuntimeException("no bev for '<" + name + ">'");
		return bev;
	}

	/**
	 * @param name
	 * @return
	 */
	public static ScoreBooleanEvaluable getSectionBooleanEvaluableInstance(String name) {
		ScoreBooleanEvaluable sbev = scoreBooleanEvals.get(name);
		if (sbev == null) throw new RuntimeException("no section bev for " + name);
		return sbev;
	}

	public static ExpressionBuilder getExpressionBuilder(String name) {
		ExpressionBuilder eb = expressionBuilders.get(name);
		if (eb == null) throw new RuntimeException("no expression builder for " + name);
		return eb;
	}

	/**
	 * @return QTI_item
	 */
	public static QTI_item getQtiItem() {
		return QtiItem;
	}

	/**
	 * Parse ISO8601 duration and return millis equivalent. Durations are
	 * preceeded by a 'P' character. Followed by year(Y), month(M), day(D),
	 * hour(H), minutes(M) and second(S). Time components (HMS) are preceeded by a
	 * 'T' character. (e.g. P0Y0M1DT3H15M2S -> 1 day, 3 hours, 15 minutes, 2
	 * seconds PT15M30S -> 15 minutes, 30 seconds.
	 * 
	 * @return millis representing ISO duration
	 */
	public static long parseISODuration(String iso) {
		String trunc = iso;
		long result = 0;

		if (trunc.charAt(0) != 'P') return -1; // must begin with 'P'
		try {
			// parseIntFromString returns -1 if stop char is not found.
			// return -1 in that case (catch statement).
			trunc = trunc.substring(1, trunc.length()); // truncate 'P'
			int timeComp = trunc.indexOf('T');
			if (timeComp != 0) { // we have a YMD component
				int i = parseIntFromString(trunc, 'Y'); // parse year component
				if (i >= 0) {
					result += i * year;
					trunc = trunc.substring(trunc.indexOf('Y') + 1, trunc.length());
				}

				i = parseIntFromString(trunc, 'M'); // parse month component
				if (i >= 0 && i < timeComp) { // Month component if 'M' before 'T'
					result += i * month;
					trunc = trunc.substring(trunc.indexOf('M') + 1, trunc.length());
				}

				i = parseIntFromString(trunc, 'D');
				if (i >= 0) {
					result += i * day;
					trunc = trunc.substring(trunc.indexOf('D') + 1, trunc.length());
				}
			}

			if (timeComp != -1) {
				// we have a time component
				trunc = trunc.substring(1, trunc.length()); // truncate 'T'
				int i = parseIntFromString(trunc, 'H'); // parse hour component
				if (i >= 0) {
					result += i * hour;
					trunc = trunc.substring(trunc.indexOf('H') + 1, trunc.length());
				}

				i = parseIntFromString(trunc, 'M'); // parse minute component
				if (i >= 0) {
					result += i * minute;
					trunc = trunc.substring(trunc.indexOf('M') + 1, trunc.length());
				}

				i = parseIntFromString(trunc, 'S'); // parse sec component
				if (i >= 0) {
					result += i * sec;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
		return result;
	}

	private static int parseIntFromString(String str, char stopChar) {
		int stopCharPos = str.indexOf(stopChar);
		if (stopCharPos < 0) return -1; // stop char not found
		String val = str.substring(0, stopCharPos);
		try {
			return Integer.parseInt(val);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Return assessment duration in ISO8601 unspecified duration format (e.g.
	 * P0Y0M1DT3H15M2S -> 1 day, 3 hours, 15 minutes, 2 seconds)
	 * 
	 * @return The string representation in ISO8601 format.
	 */
	public static String getISODuration(long duration) {
		String result = "P";
		long rest = duration;

		// years
		long tmp = rest / year;
		result += tmp + "Y";
		rest -= tmp * year;
		// months
		tmp = rest / month;
		result += tmp + "M";
		rest -= tmp * month;
		// days
		tmp = rest / day;
		result += tmp + "DT";
		rest -= tmp * day;
		// hours
		tmp = (int) rest / hour;
		result += tmp + "H";
		rest -= tmp * hour;
		// minutes
		tmp = (int) rest / minute;
		result += tmp + "M";
		rest -= tmp * minute;
		// secs
		tmp = rest / sec;
		result += tmp + "S";
		return result;
	}

	/**
	 * 
	 */
	public static Variables declareVariables(Element el_outcomes) {
		String varName;
		Variables variables = new Variables();

		if (el_outcomes == null) return variables;
		List decvars = el_outcomes.selectNodes("decvar");
		/*
		 * <decvar defaultval = "0" varname = "Var_SumofScores" vartype = "Integer"
		 * minvalue = "-10" maxvalue = "10" cutvalue = "0"/> <decvar minvalue = "0"
		 * maxvalue = "1" defaultval = "0"/>
		 */
		for (Iterator iter = decvars.iterator(); iter.hasNext();) {
			Element decvar = (Element) iter.next();
			varName = decvar.attributeValue("varname"); // dtd CDATA 'SCORE'
			if (varName == null) varName = "SCORE";
			String varType = decvar.attributeValue("vartype");
			if (varType == null) varType = "Integer"; // default
			Variable v = null;
			if (varType.equals("Integer") || varType.equals("Decimal")) {
				String def = decvar.attributeValue("defaultval");
				String min = decvar.attributeValue("minvalue");
				String max = decvar.attributeValue("maxvalue");
				String cut = decvar.attributeValue("cutvalue");
				v = new DecimalVariable(varName, max, min, cut, def);
				variables.setVariable(v);
			} else throw new RuntimeException("vartype " + varType + " not supported (declaration)");

		}
		return variables;
	}

	public static float attributeToFloat(Attribute att) {
		float val = -1;
		if (att != null) {
			String sval = att.getValue();
			sval = sval.trim();
			// assume int value, even so dtd cannot enforce it
			val = Integer.parseInt(sval);
		}
		return val;
	}

	/**
	 * Method getIntAttribute.
	 * 
	 * @param el_outpro
	 * @param string
	 * @param string1
	 * @return int
	 */
	public static int getIntAttribute(Element el_root, String xPath, String attName) {
		int res = -1;
		if (xPath == null) {
			String val = el_root.attributeValue(attName);
			res = Integer.parseInt(val);
		} else {
			Element el_el = (Element) el_root.selectSingleNode(xPath);
			if (el_el != null) {
				String val = el_el.attributeValue(attName);
				res = Integer.parseInt(val);
			}
		}
		return res;
	}

	/**
	 * Method getFloatAttribute.
	 * 
	 * @param el_outpro
	 * @param string
	 * @param string1
	 * @return float
	 */
	public static float getFloatAttribute(Element el_root, String xPath, String attName) {
		float res = -1;
		if (xPath == null) {
			String val = el_root.attributeValue(attName);
			res = Float.parseFloat(val);
		} else {
			Element el_el = (Element) el_root.selectSingleNode(xPath);
			if (el_el != null) {
				String val = el_el.attributeValue(attName);
				res = Float.parseFloat(val);
			}
		}
		return res;
	}

	/**
	 * give the hint if the document should be cached or not.
	 * 
	 * @see QTIHelper#getDocument(LocalFileImpl)
	 * @param pathToXml
	 * @param useCache
	 * @return
	 */
	public static Document getDocument(LocalFileImpl pathToXml) {
		if (pathToXml == null) {
			// xml file does not exist!
			return null;
		}
		
		byte[] doc = null;
		// get lastmodified to see if the file is newer than the cache entry and we thus need to reload it.
		Long lmf = Long.valueOf(pathToXml.getLastModified());
		String key = ((LocalFolderImpl) pathToXml.getParentContainer()).getBasefile().getAbsolutePath();

		QTIDocument tuple = ehCachLoadedQTIDocs.get(key);
		if (tuple != null && tuple.date.compareTo(lmf) == 0) {
			// in cache and not modified
			doc = tuple.content;
		} else {
			// load it: either not in cache anymore or modified in the meantime
			doc = getDocumentAsXML(pathToXml.getInputStream());
			if(doc == null) {
				//the xml file could not be parsed
				return null;
			}
			
			if(tuple == null) {
				QTIDocument cachedTuple = ehCachLoadedQTIDocs.putIfAbsent(key, new QTIDocument(lmf, doc ));
				if(cachedTuple != null) {
					doc = cachedTuple.content;
				}
			} else {
				// we use a putSilent here (no invalidation notifications to other cluster nodes), since
				// we did not generate new data, but simply asked to reload it. 
				ehCachLoadedQTIDocs.update(key, new QTIDocument(lmf, doc ));
			}
		}
		// we do not know if the receiver is destructive -> protect the cached entry
		// return a copy of the doc.
		return getDocument(doc);
	}
	
	public static Document getDocument(Path xmlPath) {
		try(InputStream in=Files.newInputStream(xmlPath)) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			return xmlParser.parse(in, false);
		} catch(IOException e) {
			return null;
		}
	}
	
	public static Document getDocument(byte[] xml) {
		try {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			return xmlParser.parse(new ByteArrayInputStream(xml), false);
		} catch(Exception e) {
			return null;
		}
	}
	
	public static byte[] getDocumentAsXML(InputStream in) {
		try {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			return xmlParser.parse(in, false).asXML().getBytes();
		} catch(Exception e) {
			return null;
		}
	}
}
