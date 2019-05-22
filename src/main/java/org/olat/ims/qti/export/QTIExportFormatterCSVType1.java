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

package org.olat.ims.qti.export;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date: May 23, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class QTIExportFormatterCSVType1 extends QTIExportFormatter {
	private String	fileNamePrefix	= "TEST_";
	private int		type			= 1;
	// Delimiters and file name suffix for the export file
	private String sep; // fields separated by
	private String emb; // fields embedded by
	private String car; // carriage return
	// Author can export the mattext without HTML tags
	// especially used for the results export of matrix questions created by QANT
	private boolean	tagless;
	private int		row_counter		= 1;

	// CELFI#107 (Header question max lenght)
	private int		cut				= 30;
	// user properties
	private List<UserPropertyHandler> userPropertyHandlers;
	private boolean isAnonymous;
	/**
	 * @param locale
	 * @param type
	 * @param anonymizerCallback
	 * @param delimiter
	 * @param Map
	 *            qtiExportFormatConfig with (QTIExportItemXYZ.class,IQTIExportItemFormatConfig)
	 */
	
	public QTIExportFormatterCSVType1(Locale locale, String sep, String emb, String car, boolean tagless) {
		super(locale, null);
		this.sep = sep;
		this.emb = emb;
		this.car = car;
		this.tagless = tagless;
		// initialize user property handlers
		UserManager um = UserManager.getInstance();
		userPropertyHandlers = um.getUserPropertyHandlersFor(QTIArchiver.TEST_USER_PROPERTIES, true);
		translator = um.getPropertyHandlerTranslator(translator);	

	}
	
	public void openReport() {
		if (qtiItemObjectList == null) { throw new OLATRuntimeException(null, "Can not format report when qtiItemObjectList is null", null); }

		if (mapWithExportItemConfigs == null) {
			// while deleting a test node the formatter has no config consiting of user input
			setDefaultQTIItemConfigs();
		}
		QTIExportItemFactory qeif = new QTIExportItemFactory(mapWithExportItemConfigs);
		
		// // // Preparing HeaderRow 1 and HeaderRow2
		StringBuilder hR1 = new StringBuilder();
		StringBuilder hR2 = new StringBuilder();
		
		int i = 1;
		for (Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
			QTIItemObject item = iter.next();

			if (displayItem(qeif.getExportItemConfig(item))) {
				hR1.append(emb);
				hR1.append(escape(item.getItemTitle()));

				// CELFI#107
				String question = item.getQuestionText();
				//question = FilterFactory.getHtmlTagsFilter().filter(question);
				question = FilterFactory.getXSSFilter(-1).filter(question);
				question = FilterFactory.getHtmlTagsFilter().filter(question);
				
				if (question.length() > cut) {
					question = question.substring(0, cut) + "...";
				}
				question = StringHelper.unescapeHtml(question);
				hR1.append(": " + escape(question));
				// CELFI#107 END

				hR1.append(emb);

				if (qeif.getExportItemConfig(item).hasResponseCols()) {
					List<String> responseColumnHeaders = item.getResponseColumnHeaders();
					for (Iterator<String> iterator = responseColumnHeaders.iterator(); iterator.hasNext();) {
						// HeaderRow1
						hR1.append(sep);
						// HeaderRow2
						String columnHeader = iterator.next();
						hR2.append(i);
						hR2.append("_");
						hR2.append(columnHeader);
						hR2.append(sep);
					}
				}

				if (qeif.getExportItemConfig(item).hasPositionsOfResponsesCol()) {
					// HeaderRow1
					hR1.append(sep);
					// HeaderRow2
					hR2.append(i);
					hR2.append("_");
					hR2.append(translator.translate("item.positions"));
					hR2.append(sep);
				}

				if (qeif.getExportItemConfig(item).hasPointCol()) {
					// HeaderRow1
					hR1.append(sep);
					// HeaderRow2
					hR2.append(i);
					hR2.append("_");
					hR2.append(translator.translate("item.score"));
					hR2.append(sep);
				}

				if (qeif.getExportItemConfig(item).hasTimeCols()) {
					// HeaderRow1
					hR1.append(sep);
					if (!isAnonymous) {
						hR1.append(sep);
					}

					// HeaderRow2
					if (!isAnonymous) {
						hR2.append(i);
						hR2.append("_");
						hR2.append(translator.translate("item.start"));
						hR2.append(sep);
					}
					
					hR2.append(i);
					hR2.append("_");
					hR2.append(translator.translate("item.duration"));
					hR2.append(sep);
				}
				i++;
			}
		}
		// // // HeaderRow1Intro
		sb.append(createHeaderRow1Intro());
		
		// // // HeaderRow1
		sb.append(hR1.toString());
		sb.append(car);
		
		// // // HeaderRow2Intro
		sb.append(createHeaderRow2Intro());
		
		// // // HeaderRow2
		sb.append(hR2.toString());
		sb.append(car);
		
	}

	public void openResultSet(QTIExportSet set) {
		// header for personalized download (iqtest)
		sb.append(row_counter);
		sb.append(sep);

		// add configured user properties
		if (isAnonymous) {
			sb.append(row_counter);
			sb.append(sep);			
		} else {
			User user = set.getIdentity().getUser();
			for (UserPropertyHandler userPropertyHandler : this.userPropertyHandlers) {
				if (userPropertyHandler == null) {
					continue;
				}
				String property = userPropertyHandler.getUserProperty(user, translator.getLocale());
				if (!StringHelper.containsNonWhitespace(property)) {
					property = translator.translate("column.field.notavailable");
				}
				sb.append(property);
				sb.append(sep);			
			}			
			// add other user and session information
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(set.getIdentity());
			String homepage = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
			sb.append(homepage);
			sb.append(sep);
		}
		float assessPoints = set.getScore();
		sb.append(assessPoints);
		sb.append(sep);
		boolean isPassed = set.getIsPassed();					
		sb.append(isPassed);
		sb.append(sep);

		if (!isAnonymous) {
			sb.append(set.getIp());
			sb.append(sep);
			// datatime
			Date date = set.getLastModified();
			sb.append(Formatter.formatDatetime(date));
			sb.append(sep);
		}
		Long assessDuration = set.getDuration();
		// since there are resultsets created before alter table adding the field duration
		if (assessDuration != null) {
			sb.append(Math.round(assessDuration.longValue() / 1000));
		} else {
			sb.append("n/a");
		}
		sb.append(sep);
		
	}

	public void visit(QTIExportItem eItem) {
		List<String> responseColumns = eItem.getResponseColumns();
		QTIExportItemFormatConfig itemFormatConfig = eItem.getConfig();

		if (displayItem(itemFormatConfig)) {

			if (itemFormatConfig.hasResponseCols()) {
				for (Iterator<String> iter = responseColumns.iterator(); iter.hasNext();) {
					String responseColumn = iter.next();
					sb.append(emb);
					sb.append(escape(responseColumn));
					sb.append(emb);
					sb.append(sep);
				}
			}

			if (itemFormatConfig.hasPositionsOfResponsesCol()) {
				sb.append(eItem.getPositionsOfResponses());
				sb.append(sep);
			}

			if (eItem.hasResult()) {
				if (itemFormatConfig.hasPointCol()) {
					// points
					sb.append(eItem.getScore());
					sb.append(sep);
				}
				if (itemFormatConfig.hasTimeCols()) {
					// startdatetime
					if (!isAnonymous) {
						if (eItem.getTimeStamp().getTime() > 0) {
							sb.append(Formatter.formatDatetime(eItem.getTimeStamp()));
						} else {
							sb.append("n/a");
						}
						sb.append(sep);
					}

					// column duration
					Long itemDuration = eItem.getDuration();

					if (itemDuration != null) {
						sb.append(itemDuration.longValue() / 1000);
					} else {
						sb.append("n/a");
					}
					sb.append(sep);
				}
			} else {
				// points
				if (itemFormatConfig.hasPointCol()) sb.append(sep);
				// startdatetime, column duration
				if (itemFormatConfig.hasTimeCols()) {
					sb.append(sep);
					if (!isAnonymous) {
						sb.append(sep);
					}
				}
			}
		}
	}

	public void closeResultSet() {
		sb.append(car);
		row_counter++;
	}
	
	public void closeReport() {
		if (qtiItemObjectList == null) { throw new OLATRuntimeException(null, "Can not format report when qtiItemObjectList is null", null); }
		String legend = translator.translate("legend");
		sb.append(car + car);
		sb.append(legend);
		sb.append(car + car);
		int y = 1;
		for (Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
			QTIItemObject element = iter.next();
			
			sb.append(element.getItemIdent());
			sb.append(sep);
			sb.append(emb);
			sb.append(escape(element.getItemTitle()));
			sb.append(emb);
			sb.append(car);

			sb.append(sep + sep);
			sb.append("minValue");
			sb.append(sep);
			sb.append(element.getItemMinValue());
			sb.append(car);

			sb.append(sep + sep);
			sb.append("maxValue");
			sb.append(sep);
			sb.append(element.getItemMaxValue());
			sb.append(car);

			sb.append(sep + sep);
			sb.append("cutValue");
			sb.append(sep);
			sb.append(element.getItemCutValue());
			sb.append(car);

			// CELFI#107
			sb.append(sep + sep + sep + sep);
			String question = element.getQuestionText();
			if (tagless) {
				question = FilterFactory.getXSSFilter(-1).filter(question);
				question = FilterFactory.getHtmlTagsFilter().filter(question);
			}
			question = StringHelper.unescapeHtml(question);
			sb.append(question);
			sb.append(car);
			// CELFI#107 END

			List<String> responseLabelMaterials = element.getResponseLabelMaterials();

			for (int i = 0; i < element.getResponseIdentifier().size(); i++) {
				sb.append(sep + sep);
				sb.append(y);
				sb.append("_");
				sb.append(element.getItemType());
				sb.append(i + 1);
				sb.append(sep);
				sb.append(element.getResponseIdentifier().get(i));
				sb.append(sep);

				if (responseLabelMaterials != null) {
					String s = responseLabelMaterials.get(i);
					s = StringHelper.unescapeHtml(s);
					if (tagless) {
						s = s.replaceAll("\\<.*?\\>", "");
					}
					sb.append(Formatter.stripTabsAndReturns(s));
				}
				sb.append(car);
			}
			y++;
		}

		sb.append(car + car);
		sb.append("SCQ");
		sb.append(sep);
		sb.append("Single Choice Question");
		sb.append(car);
		sb.append("MCQ");
		sb.append(sep);
		sb.append("Multiple Choice Question");
		sb.append(car);
		sb.append("FIB");
		sb.append(sep);
		sb.append("Fill in the blank");
		sb.append(car);
		sb.append("ESS");
		sb.append(sep);
		sb.append("Essay");
		sb.append(car);
		sb.append("KPR");
		sb.append(sep);
		sb.append("Kprim (K-Type)");

		sb.append(car + car);
		sb.append("R:");
		sb.append(sep);
		sb.append("Radio button (SCQ)");
		sb.append(car);
		sb.append("C:");
		sb.append(sep);
		sb.append("Check box (MCQ or KPR)");
		sb.append(car);
		sb.append("B:");
		sb.append(sep);
		sb.append("Blank (FIB)");
		sb.append(car);
		sb.append("A:");
		sb.append(sep);
		sb.append("Area (ESS)");

		sb.append(car + car);
		sb.append("x_Ry");
		sb.append(sep);
		sb.append("Radio Button y of SCQ x, e.g. 1_R1");
		sb.append(car);
		sb.append("x_Cy");
		sb.append(sep);
		sb.append("Check Box y of MCQ x or two Radio Buttons y of KPR x, e.g. 3_C2");
		sb.append(car);
		sb.append("x_By");
		sb.append(sep);
		sb.append("Blank y of FIB x, e.g. 17_B2");
		sb.append(car);
		sb.append("x_Ay");
		sb.append(sep);
		sb.append("Area y of ESS x, e.g. 4_A1");

		sb.append(car + car);
		sb.append("Kprim:");
		sb.append(sep);
		sb.append("'+' = yes");
		sb.append(sep);
		sb.append("'-' = no");
		sb.append(sep);
		sb.append("'.' = no answer");
		sb.append(sep);

	}

	public String getReport() {
		return sb.toString();
	}

	private String createHeaderRow1Intro() {
		StringBuilder hr1Intro = new StringBuilder();
		hr1Intro.append(sep);//seqnum
		if (isAnonymous) {
			sb.append(sep);
		} else {
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler != null) {
					hr1Intro.append(sep);
				}
			}
			hr1Intro.append(sep);
		}
		hr1Intro.append(sep);
		hr1Intro.append(sep);
		if (!isAnonymous) {
			hr1Intro.append(sep);// header ip address
			hr1Intro.append(sep);// header date
		}
		hr1Intro.append(sep);
		return hr1Intro.toString();//  + sep + sep + sep + sep + sep + sep + sep + sep + sep;
	}

	/**
	 * Creates header line for all types
	 * 
	 * @param theType
	 * @return header line for download
	 */
	private String createHeaderRow2Intro() {
		
		StringBuilder hr2Intro = new StringBuilder(); 

		// header for personalized download (iqtest)
		String sequentialNumber = translator.translate("column.header.seqnum");
		hr2Intro.append(sequentialNumber);
		hr2Intro.append(sep);

		// add configured user properties
		if (isAnonymous) {
			String num = translator.translate("column.header.number");
			hr2Intro.append(num);
			hr2Intro.append(sep);
		} else {
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler == null) {
					continue;
				}
				String header = translator.translate(userPropertyHandler.i18nFormElementLabelKey());
				hr2Intro.append(header);
				hr2Intro.append(sep);			
			}
			// add other user and session information
			String homepage = translator.translate("column.header.homepage");
			hr2Intro.append(homepage);
			hr2Intro.append(sep);
		}
		String assessPoint = translator.translate("column.header.assesspoints");
		hr2Intro.append(assessPoint);
		hr2Intro.append(sep);
		String passed = translator.translate("column.header.passed");
		hr2Intro.append(passed);
		hr2Intro.append(sep);
		if (!isAnonymous) {
			String ipAddress = translator.translate("column.header.ipaddress");
			hr2Intro.append(ipAddress);
			hr2Intro.append(sep);
			String date = translator.translate("column.header.date");
			hr2Intro.append(date);
			hr2Intro.append(sep);
		}
		String duration = translator.translate("column.header.duration");
		hr2Intro.append(duration);
		hr2Intro.append(sep);

		return hr2Intro.toString();
	}

	public String getFileNamePrefix() {
		return fileNamePrefix;
	}

	/*
	 * (non-Javadoc)
	 * @see org.olat.ims.qti.export.QTIExportFormatter#getType()
	 */
	public int getType() {
		return this.type;
	}

	private boolean displayItem(QTIExportItemFormatConfig c) {
		return !(!c.hasResponseCols() && !c.hasPointCol() && !c.hasTimeCols() && !c.hasPositionsOfResponsesCol());
	}

	private String escape(String s) {
		return s.replaceAll(emb, emb + emb);
	}

	private void setDefaultQTIItemConfigs() {
		Map<Class<?>, QTIExportItemFormatConfig> itConfigs = new HashMap<>();
  	
		for (Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
			QTIItemObject item = iter.next();
			if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)) {
				if (itConfigs.get(QTIExportSCQItemFormatConfig.class) == null) {
					QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)) {
				if (itConfigs.get(QTIExportMCQItemFormatConfig.class) == null) {
					QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
				if (itConfigs.get(QTIExportKPRIMItemFormatConfig.class) == null) {
					QTIExportKPRIMItemFormatConfig confKPRIM = new QTIExportKPRIMItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportKPRIMItemFormatConfig.class, confKPRIM);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)) {
				if (itConfigs.get(QTIExportEssayItemFormatConfig.class) == null) {
					QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
					itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)) {
				if (itConfigs.get(QTIExportFIBItemFormatConfig.class) == null) {
					QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
					itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
				}
			}
			// if cannot find the type via the ItemParser, look for the QTIItemObject type
			else if (item.getItemType().equals(QTIItemObject.TYPE.A)) {
				QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
				itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.R)) {
				QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.C)) {
				QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.B)) {
				QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
				itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
			} else {
				throw new OLATRuntimeException(null, "Can not resolve QTIItem type='" + item.getItemType() + "'", null);
			}
		}
		mapWithExportItemConfigs = itConfigs;
	}

	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	
}
