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
package org.olat.ims.qti.statistics;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.export.QTIArchiveWizardController;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportFormatterCSVType2;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.export.helper.QTIObjectTreeBuilder;

/**
 * 
 * Initial date: 17.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIStatisticsResource implements MediaResource {
	
	private static final OLog log = Tracing.createLoggerFor(QTIStatisticsResource.class);
	
	private final Locale locale;
	private final String encoding = "UTF-8";
	
	private final QTIStatisticResourceResult resourceResult;
	
	public QTIStatisticsResource(QTIStatisticResourceResult resourceResult, Locale locale) {
		this.resourceResult = resourceResult;
		this.locale = locale;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding(encoding);
		} catch (Exception e) {
			log.error("", e);
		}
		CourseNode courseNode = resourceResult.getTestCourseNode();
		String label = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".csv";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try {
			String sep = "\\t"; // fields separated by
			String emb = "\""; // fields embedded by
			String esc = "\\"; // fields escaped by
			String car = "\\r\\n"; // carriage return
			
			sep = QTIArchiveWizardController.convert2CtrlChars(sep);
			car = QTIArchiveWizardController.convert2CtrlChars(car);
			
			int exportType = 1;
			QTIExportFormatter formatter;
			if (QTIType.test.equals(resourceResult.getType())){
				exportType = 1;
				formatter = new QTIExportFormatterCSVType1(locale, sep, emb, esc, car, true);
		  	} else if (QTIType.survey.equals(resourceResult.getType())) {
		  		exportType = 2;
		  		formatter = new QTIExportFormatterCSVType2(locale, null, sep, emb, esc, car, true);
		  	} else {
		  		return;
		  	}

			Long qtiRepoEntryKey = resourceResult.getQTIRepositoryEntry().getKey();
			List<QTIItemObject> itemList = new QTIObjectTreeBuilder().getQTIItemObjectList(resourceResult.getResolver());
			formatter.setMapWithExportItemConfigs(QTIArchiveWizardController.getQTIItemConfigs(itemList));
			
			QTIResultManager qrm = QTIResultManager.getInstance();
			
			QTIStatisticSearchParams params = resourceResult.getSearchParams();
			List<SecurityGroup> limitToSecGroups = params.isMayViewAllUsersAssessments()
					? null : params.getLimitToSecGroups();

			List<QTIResult> results = qrm.selectResults(resourceResult.getCourseOres().getResourceableId(),
					courseNode.getIdent(), qtiRepoEntryKey, limitToSecGroups, exportType);
			
			QTIExportManager.getInstance().exportResults(formatter, results, itemList, hres.getOutputStream());
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
