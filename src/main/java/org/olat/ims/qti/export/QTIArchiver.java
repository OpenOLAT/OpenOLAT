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
package org.olat.ims.qti.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.QTIResourceTypeModule;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.export.helper.QTIObjectTreeBuilder;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIArchiver {
	
	public static final String TEST_USER_PROPERTIES = QTIExportFormatterCSVType1.class.getName();
	
	private static final Logger log = Tracing.createLoggerFor(QTIArchiver.class);

	private CourseNode courseNode;
	private AssessmentNodeData data;
	
	private final Locale locale;
	private final OLATResourceable courseOres;
	
	private boolean participants = true;
	private boolean allUsers = true;
	private boolean anonymUsers = true;
	
	private Boolean results;
	private List<QTIItemObject> qtiItemObjectList;
	private Map<Class<?>, QTIExportItemFormatConfig> qtiItemConfigs;
	
	private final QTIResultManager qrm;
	private final QTIExportManager qem;
	
	private Type type;
	
	public enum Type {
		qti12,
		qti21
	}
	
	public QTIArchiver(OLATResourceable courseOres, Locale locale) {
		this.locale = locale;
		this.courseOres = courseOres;
		qem = QTIExportManager.getInstance();
		qrm = CoreSpringFactory.getImpl(QTIResultManager.class);
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}

	public boolean isParticipants() {
		return participants;
	}

	public void setParticipants(boolean participants) {
		this.participants = participants;
	}

	public boolean isAllUsers() {
		return allUsers;
	}

	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}

	public boolean isAnonymUsers() {
		return anonymUsers;
	}

	public void setAnonymUsers(boolean anonymUsers) {
		this.anonymUsers = anonymUsers;
	}

	public AssessmentNodeData getData() {
		return data;
	}

	public void setData(AssessmentNodeData data) {
		this.type = null;
		this.results = null;
		this.data = data;
		ICourse course = CourseFactory.loadCourse(courseOres);
		courseNode = course.getRunStructure().getNode(data.getIdent());
		getQTIItemConfigs();
		getType();
	}
	
	public Type getType() {
		if (type == null) {
			RepositoryEntry testRe = courseNode.getReferencedRepositoryEntry();
			if (ImsQTI21Resource.TYPE_NAME.equals(testRe.getOlatResource().getResourceableTypeName())) {
				type = Type.qti21;
			} else {
				type = Type.qti12;
			}
		}
		return type;
	}
	
	public boolean hasResults() {
		if (results != null)
			return results.booleanValue();

		ICourse course = CourseFactory.loadCourse(courseOres);
		RepositoryEntry testRe = courseNode.getReferencedRepositoryEntry();

		boolean success = false;
		if (ImsQTI21Resource.TYPE_NAME.equals(testRe.getOlatResource().getResourceableTypeName())) {
			type = Type.qti21;
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(testRe, courseEntry,
					courseNode.getIdent(), allUsers, anonymUsers);
			success = new QTI21ArchiveFormat(locale, searchParams).hasResults();
		} else {
			type = Type.qti12;
			success = qrm.hasResultSets(courseOres.getResourceableId(), courseNode.getIdent(), testRe.getKey());
		}

		results = Boolean.valueOf(success);
		return success;
	}
	
	public List<QTIItemObject> getQtiItemObjectList() {
		if(qtiItemObjectList == null) {
			RepositoryEntry testRe = courseNode.getReferencedRepositoryEntry();
			qtiItemObjectList = new QTIObjectTreeBuilder().getQTIItemObjectList(testRe.getKey());
		}
		return qtiItemObjectList;
	}
	
	public Map<Class<?>, QTIExportItemFormatConfig> getQTIItemConfigs() {
		if(qtiItemConfigs == null) {
			RepositoryEntry testRe = courseNode.getReferencedRepositoryEntry();
			if(QTIResourceTypeModule.isOnyxTest(testRe.getOlatResource())) {
				qtiItemConfigs = Collections.emptyMap();
			} else if(ImsQTI21Resource.TYPE_NAME.equals(testRe.getOlatResource().getResourceableTypeName())) {
				qtiItemConfigs = Collections.emptyMap();
			} else {
				qtiItemConfigs = getQTIItemConfigs(getQtiItemObjectList());
			}
		}
		return qtiItemConfigs;
	}
	
	public MediaResource export() {
		switch(type) {
			case qti12: return exportQTI12();
			case qti21: return exportQTI21();
			default: return null;
		}
	}
	
	public MediaResource exportQTI21() {
		ICourse course = CourseFactory.loadCourse(courseOres);
		RepositoryEntry testRe = courseNode.getReferencedRepositoryEntry();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(testRe, courseEntry,
				courseNode.getIdent(), allUsers, anonymUsers);
		return new QTI21ArchiveFormat(locale, searchParams).exportCourseElement();
	}
	
	public MediaResource exportQTI12() {
		RepositoryEntry testRe = courseNode.getReferencedRepositoryEntry();

		String sep = "\\t"; // fields separated by
		String emb = "\""; // fields embedded by
		String car = "\\r\\n"; // carriage return

		sep = convert2CtrlChars(sep);
		car = convert2CtrlChars(car);
		boolean tagLess = true;
		
		QTIExportFormatter formatter = getFormatter(sep, emb, car, tagLess);
		formatter.setMapWithExportItemConfigs(qtiItemConfigs);

		return new DefaultMediaResource() {
			
			@Override
			public String getContentType() {
				return "text/csv";
			}

			@Override
			public void prepare(HttpServletResponse hres) {
				try {
					hres.setCharacterEncoding("UTF-8");
				} catch (Exception e) {
					log.error("", e);
				}
				
				String label = courseNode.getType() + "_"
						+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
						+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
						+ ".csv";
				
				String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
				hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
				hres.setHeader("Content-Description", urlEncodedLabel);
				
				try(OutputStream out = hres.getOutputStream()) {
					List<QTIResult> qtiResults = qrm.selectResults(courseOres.getResourceableId(), courseNode.getIdent(), testRe.getKey(), null, 5);
					qem.exportResults(formatter, qtiResults, qtiItemObjectList, out);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		};
	}
	
	private QTIExportFormatter getFormatter(String se, String em, String ca, boolean tagless){
		QTIExportFormatter frmtr = null;
		if (courseNode instanceof IQTESTCourseNode){
			frmtr = new QTIExportFormatterCSVType1(locale, se, em, ca, tagless);
		} else if (courseNode instanceof IQSELFCourseNode){
			frmtr = new QTIExportFormatterCSVType1(locale, se, em, ca, tagless);
			((QTIExportFormatterCSVType1)frmtr).setAnonymous(true);
		} else { // type == 3
			frmtr = new QTIExportFormatterCSVType3(locale, null, se, em, ca, tagless);
		}
		return frmtr;
	}
	
	public static String convert2CtrlChars(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '\\':
					// check on \\ first
					if (i < len - 1 && cs[i + 1] == 't') { // we have t as next char
						sb.append("\t");
						i++;
					}else if (i < len - 1 && cs[i + 1] == 'r') { // we have r as next char
						sb.append("\r");
						i++;
					}else if (i < len - 1 && cs[i + 1] == 'n') { // we have n as next char
						sb.append("\n");
						i++;
					} else {
						sb.append("\\");
					}
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	public static Map<Class<?>, QTIExportItemFormatConfig> getQTIItemConfigs(List<QTIItemObject> itemList){
		Map<Class<?>, QTIExportItemFormatConfig> itConfigs = new HashMap<>();
  	
		for (Iterator<QTIItemObject> iter = itemList.iterator(); iter.hasNext();) {
			QTIItemObject item = iter.next();
			if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)){
				if (itConfigs.get(QTIExportSCQItemFormatConfig.class) == null){
					QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)){
				if (itConfigs.get(QTIExportMCQItemFormatConfig.class) == null){
					QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ );
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)){
				if (itConfigs.get(QTIExportKPRIMItemFormatConfig.class) == null){
					QTIExportKPRIMItemFormatConfig confKPRIM = new QTIExportKPRIMItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportKPRIMItemFormatConfig.class, confKPRIM);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)){
				if (itConfigs.get(QTIExportEssayItemFormatConfig.class) == null){
					QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
					itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)){
				if (itConfigs.get(QTIExportFIBItemFormatConfig.class) == null){
					QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
					itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
				}
			}
			//if cannot find the type via the ItemParser, look for the QTIItemObject type
			else if (item.getItemType().equals(QTIItemObject.TYPE.A)){
				QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
				itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.R)){
				QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.C)){
				QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ );
			} else if (item.getItemType().equals(QTIItemObject.TYPE.B)){
				QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
				itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
			} else {
				throw new OLATRuntimeException(null,"Can not resolve QTIItem type", null);
			}
		}
		return itConfigs;
	}
}
