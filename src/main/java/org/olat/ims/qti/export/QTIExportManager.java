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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.export.helper.QTIObjectTreeBuilder;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author Alexander Schneider
 */
public class QTIExportManager {
	
	/**
	 * <code>PACKAGE</code>
	 */
	public static final String PACKAGE = QTIExportManager.class.getPackage().getName();

	private static QTIExportManager instance = new QTIExportManager();

	/**
	 * Constructor for QTIResultManager.
	 */
	private QTIExportManager() {
	//
	}

	/**
	 * @return QTIResultManager
	 */
	public static QTIExportManager getInstance() {
		return instance;
	}
	
	/**
	 * @param locale
	 * @param olatResource
	 * @param shortTitle
	 * @param olatResourceDetail
	 * @param repositoryRef
	 * @param type
	 * @param exportDirectory
	 * @param anonymizerCallback callback that should be used to anonymize the user names or NULL if row counter
	 * should be used (only for type 2 and 3)
	 * @return
	 */
	public boolean selectAndExportResults(QTIExportFormatter qef, Long courseResId, String shortTitle, String olatResourceDetail,
			Long testReKey, File exportDirectory, String charset, String fileNameSuffix) {
		boolean resultsFoundAndExported = false;
		QTIResultManager qrm = QTIResultManager.getInstance();
		List<QTIResult> results = qrm.selectResults(courseResId, olatResourceDetail, testReKey, null, qef.getType());
		if(results.size() > 0){
			QTIResult res0 = results.get(0);
			List<QTIItemObject> qtiItemObjectList = new QTIObjectTreeBuilder().getQTIItemObjectList(new Long(res0.getResultSet().getRepositoryRef()));
			qef.setQTIItemObjectList(qtiItemObjectList);
			if (results.size() > 0) {
				createContentOfExportFile(results,qtiItemObjectList,qef);
				writeContentToFile(shortTitle, exportDirectory, charset, qef, fileNameSuffix);
				resultsFoundAndExported = true;
			}			
		}
		return resultsFoundAndExported;
	}
	
	public boolean selectAndExportResults(QTIExportFormatter qef, Long courseResId, String shortTitle,
			String olatResourceDetail, RepositoryEntry testRe, ZipOutputStream exportStream, String zipPath, 
			Locale locale, String fileNameSuffix) throws IOException {
		boolean resultsFoundAndExported = false;
		QTIResultManager qrm = QTIResultManager.getInstance();
		List<QTIResult> results = qrm.selectResults(courseResId, olatResourceDetail, testRe.getKey(), null, qef.getType());
		if(!results.isEmpty()){
			List<QTIItemObject> qtiItemObjectList = new QTIObjectTreeBuilder().getQTIItemObjectList(testRe);
			qef.setQTIItemObjectList(qtiItemObjectList);
			if (!results.isEmpty()) {
				createContentOfExportFile(results, qtiItemObjectList, qef);
				String targetFileName = ZipUtil.concat(zipPath, getFilename(shortTitle, fileNameSuffix));
				
				exportStream.putNextEntry(new ZipEntry(targetFileName));
				IOUtils.write(qef.getReport(), exportStream, "UTF-8");
				exportStream.closeEntry();
				resultsFoundAndExported = true;
			}			
		} else {
			String targetFileName = ZipUtil.concat(zipPath, getFilename(shortTitle, fileNameSuffix));
			exportStream.putNextEntry(new ZipEntry(targetFileName));
			Translator translator = Util.createPackageTranslator(QTIExportFormatter.class, locale);
			IOUtils.write(translator.translate("archive.noresults.short"), exportStream, "UTF-8");
			exportStream.closeEntry();
			resultsFoundAndExported = true;
		}
		return resultsFoundAndExported;
	}
	
	/**
	 * 
	 * @param qef
	 * @param results
	 * @param qtiItemObjectList
	 * @param shortTitle
	 * @param exportDirectory
	 * @param charset
	 * @param fileNameSuffix
	 * @return
	 */
	public String exportResults(QTIExportFormatter qef, List<QTIResult> results, List<QTIItemObject> qtiItemObjectList, String shortTitle, File exportDirectory, String charset, String fileNameSuffix) {
		String targetFileName = null;

		qef.setQTIItemObjectList(qtiItemObjectList);
		if (results.size() > 0) {
			createContentOfExportFile(results,qtiItemObjectList,qef);
			targetFileName = writeContentToFile(shortTitle, exportDirectory, charset, qef, fileNameSuffix);
		}
		return targetFileName;
	}
	
	public void exportResults(QTIExportFormatter qef, List<QTIResult> results, List<QTIItemObject> qtiItemObjectList,
			OutputStream exportStream)
	throws IOException {
		qef.setQTIItemObjectList(qtiItemObjectList);
		if (results.size() > 0) {
			createContentOfExportFile(results,qtiItemObjectList,qef);
			IOUtils.write(qef.getReport(), exportStream);
		}
	}

	
	/**
	 * @param locale Locale used for export file headers / default values
	 * @param results
	 * @param type
	 * @param anonymizerCallback
	 * @return String
	 */
	private void createContentOfExportFile(List<QTIResult> qtiResults, List<QTIItemObject> qtiItemObjectList, QTIExportFormatter qef) {
		qef.openReport();
		
		//formatter has information about how to format the different qti objects
		Map<Class<?>, QTIExportItemFormatConfig> mapWithConfigs = qef.getMapWithExportItemConfigs();
		QTIExportItemFactory qeif = new QTIExportItemFactory(mapWithConfigs);
				
		while (qtiResults.size() > 0){
			List<QTIResult> assessIDresults = stripNextAssessID(qtiResults);
			qef.openResultSet(new QTIExportSet(assessIDresults.get(0)));
			for(QTIItemObject element:qtiItemObjectList) {
				QTIResult qtir = element.extractQTIResult(assessIDresults);
				qef.visit(qeif.getExportItem(qtir,element));
			}
			qef.closeResultSet();
		}
		qef.closeReport();	
	}

	/**
	 * writes content of all results to a file
	 */
	private String writeContentToFile(String shortTitle, File exportDirectory, String charset, QTIExportFormatter qef, String fileNameSuffix) {
		// defining target filename
		String targetFileName = getFilename(shortTitle, fileNameSuffix);
		File savedFile = ExportUtil.writeContentToFile(targetFileName, qef.getReport(), exportDirectory, charset);
		return savedFile.getName();
	}
	
	private String getFilename(String shortTitle, String fileNameSuffix) {
		StringBuilder tf = new StringBuilder();
		tf.append(Formatter.makeStringFilesystemSave(shortTitle));
		tf.append("_");
		DateFormat myformat = new SimpleDateFormat("yyyy-MM-dd__hh-mm");
		String timestamp = myformat.format(new Date());
		tf.append(timestamp);
		tf.append(fileNameSuffix);
		String targetFileName = tf.toString();
		return targetFileName;
	}
	
	/**
	 * 
	 * @param queryResult
	 * @return List of results with the same assessmentid
	 */		
	private List<QTIResult> stripNextAssessID(List<QTIResult> queryResult){
		List<QTIResult> result = new ArrayList<>();
		
		if (queryResult.size()== 0) return result;
		
		QTIResult qtir = queryResult.remove(0);
		
		long currentAssessmentID = qtir.getResultSet().getAssessmentID();
		result.add(qtir);
		
		while(queryResult.size()>0){
			qtir = queryResult.remove(0);
			if (qtir.getResultSet().getAssessmentID() == currentAssessmentID) result.add(qtir);
			else {
				queryResult.add(0,qtir);
				break;	
			}
		}
		return result;
	}	
	
}
