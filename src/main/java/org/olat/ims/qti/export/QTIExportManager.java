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
* <p>
*/ 

package org.olat.ims.qti.export;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.manager.BasicManager;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.Formatter;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.export.helper.QTIObjectTreeBuilder;

/**
 * Description: TODO
 * 
 * @author Alexander Schneider
 */
public class QTIExportManager extends BasicManager{
	
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
	public boolean selectAndExportResults(QTIExportFormatter qef, Long olatResource, String shortTitle, String olatResourceDetail, Long repositoryRef,
		File exportDirectory, String charset, String fileNameSuffix) {
		boolean resultsFoundAndExported = false;
		QTIResultManager qrm = QTIResultManager.getInstance();
		List results = qrm.selectResults(olatResource, olatResourceDetail, repositoryRef, qef.getType());
		if(results.size() > 0){
			QTIResult res0 = (QTIResult) results.get(0);
			
			QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(new Long(res0.getResultSet().getRepositoryRef()));
			
			List qtiItemObjectList = qotb.getQTIItemObjectList();
			qef.setQTIItemObjectList(qtiItemObjectList);
			if (results.size() > 0) {
				createContentOfExportFile(results,qtiItemObjectList,qef);
				writeContentToFile(shortTitle, exportDirectory, charset, qef, fileNameSuffix);
				resultsFoundAndExported = true;
			}			
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
		public String exportResults(QTIExportFormatter qef, List results, List qtiItemObjectList, String shortTitle, File exportDirectory, String charset, String fileNameSuffix) {
			String targetFileName = null;
	
			qef.setQTIItemObjectList(qtiItemObjectList);
			if (results.size() > 0) {
				createContentOfExportFile(results,qtiItemObjectList,qef);
				targetFileName = writeContentToFile(shortTitle, exportDirectory, charset, qef, fileNameSuffix);
			}
			return targetFileName;
		}
	
	
	/**
	 * @param locale Locale used for export file headers / default values
	 * @param results
	 * @param type
	 * @param anonymizerCallback
	 * @return String
	 */
	private void createContentOfExportFile(List qtiResults, List qtiItemObjectList, QTIExportFormatter qef) {
		
		qef.openReport();
		
		//formatter has information about how to format the different qti objects
		Map mapWithConfigs = qef.getMapWithExportItemConfigs();
		QTIExportItemFactory qeif = new QTIExportItemFactory(mapWithConfigs);
				
		while (qtiResults.size() > 0){
			List assessIDresults = stripNextAssessID(qtiResults);
		
			qef.openResultSet(new QTIExportSet((QTIResult)assessIDresults.get(0)));
			
			for (Iterator iter = qtiItemObjectList.iterator(); iter.hasNext();) {
				QTIItemObject element = (QTIItemObject) iter.next();
				
				QTIResult qtir;
				qtir = element.extractQTIResult(assessIDresults);
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
		StringBuilder tf = new StringBuilder();
		tf.append(qef.getFileNamePrefix());
		tf.append(Formatter.makeStringFilesystemSave(shortTitle));
		tf.append("_");
		DateFormat myformat = new SimpleDateFormat("yyyy-MM-dd__hh-mm-ss__SSS");
		String timestamp = myformat.format(new Date());
		tf.append(timestamp);
		tf.append(fileNameSuffix);
		String targetFileName = tf.toString();

		ExportUtil.writeContentToFile(targetFileName, qef.getReport(), exportDirectory, charset);
		
		return targetFileName;
	}
	
	/**
	 * 
	 * @param queryResult
	 * @return List of results with the same assessmentid
	 */		
	private List stripNextAssessID(List queryResult){
		List result = new ArrayList();
		
		if (queryResult.size()== 0) return result;
		
		QTIResult qtir = (QTIResult) queryResult.remove(0);
		
		long currentAssessmentID = qtir.getResultSet().getAssessmentID();
		result.add(qtir);
		
		while(queryResult.size()>0){
			qtir = (QTIResult) queryResult.remove(0);
			if (qtir.getResultSet().getAssessmentID() == currentAssessmentID) result.add(qtir);
			else {
				queryResult.add(0,qtir);
				break;	
			}
		}
		return result;
	}	
	
}
