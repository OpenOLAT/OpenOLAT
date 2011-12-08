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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/
package org.olat.modules.scorm.assessment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.scorm.ScormDirectoryHelper;
import org.olat.modules.scorm.server.servermodels.ScoDocument;
import org.olat.modules.scorm.server.servermodels.SequencerModel;

/**
 * <P>
 * Initial Date:  13 august 2009 <br>
 * @author srosse
 */
public class ScormAssessmentManager extends BasicManager {
	
	public static final String RELOAD_SETTINGS_FILE = "reload-settings.xml";
	
	private static final ScormAssessmentManager instance = new ScormAssessmentManager();
	private static final OLog logger = Tracing.createLoggerFor(ScormAssessmentManager.class);
	
	public static ScormAssessmentManager getInstance() {
		return instance;
	}
	
	/**
	 * Load the SequencerModel
	 * @param username
	 * @param courseEnv
	 * @param node
	 * @return can be null if the user hasn't visited the course
	 */
	public SequencerModel getSequencerModel(String username, CourseEnvironment courseEnv, ScormCourseNode node) {
		VFSContainer scoDirectory = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
		if(scoDirectory == null) return null;
		
		VFSItem reloadSettingsFile = scoDirectory.resolve(RELOAD_SETTINGS_FILE);
		if(reloadSettingsFile instanceof LocalFileImpl) {
			LocalFileImpl fileImpl = (LocalFileImpl)reloadSettingsFile;
			return new SequencerModel(fileImpl.getBasefile(), null);
		} else if (reloadSettingsFile != null) {
			throw new OLATRuntimeException(this.getClass(), "Programming error, SCORM results must be file based", null);
		}
		return null;
	}
	
	//fxdiff FXOLAT-108: reset SCORM test
	public boolean deleteResults(String username, CourseEnvironment courseEnv, ScormCourseNode node) {
		VFSContainer scoDirectory = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
		if(scoDirectory == null) return true; //nothing to reset -> ok
		
		return (scoDirectory.delete() == VFSConstants.YES);
	}
	
	//<OLATCE-289>
	/**
	 * Method to get a List of cmi datas for every xml file in the users directory. 
	 * They are ordered in a Map with the lastModifiedDate of the file as Key.
	 * @param username
	 * @param courseEnv
	 * @param node
	 * @return
	 */
	public Map<Date, List<CmiData>> visitScoDatasMultiResults(String username, CourseEnvironment courseEnv, ScormCourseNode node) {
		Map<Date, List<CmiData>> cmiDataObjects = new HashMap<Date, List<CmiData>>();
		VFSContainer scoContainer = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
		if(scoContainer == null) {
			return null;
		}

		Calendar cal = Calendar.getInstance();
		List<VFSItem> contents = scoContainer.getItems(new XMLFilter());
		for(VFSItem file:contents) {
			List<CmiData> item = collectData(file);
			if (item != null) {
				//modified date
				cal.setTimeInMillis(file.getLastModified());
				Collections.sort(item, new CmiDataComparator());
				cmiDataObjects.put(cal.getTime(), item);
			}
		}
		
		return cmiDataObjects;
	}
	
	/**
	 * Collects the cmi data of the given Scorm-file.
	 * @param scoFile
	 * @return
	 */
	private List<CmiData> collectData(VFSItem scoFile) {
		List<CmiData> datas = new ArrayList<CmiData>();
		ScoDocument document = new ScoDocument(null);
		try {
			if(scoFile instanceof LocalFileImpl) {
				document.loadDocument(((LocalFileImpl)scoFile).getBasefile());
			}
			else {
				logger.warn("Cannot use this type of VSFItem to load a SCO Datamodel: " + scoFile.getClass().getName(), null);
				return null;
			}
			
			String fileName = scoFile.getName();
			String itemId = fileName.substring(0, fileName.length() - 4);
			String[][] scoModel = document.getScoModel();
			for(String[] line:scoModel) {
				datas.add(new CmiData(itemId, line[0], line[1]));
			}
		} catch (Exception e) {
			logger.error("Cannot load a SCO Datamodel", e);
		}
		return datas;
	}
	// </OLATCE-289>
	
	/**
	 * Return all the datas in the sco datamodels of a SCORM course
	 * @param username
	 * @param courseEnv
	 * @param node
	 * @return
	 */
	public List<CmiData> visitScoDatas(String username, CourseEnvironment courseEnv, ScormCourseNode node) {
		VFSContainer scoContainer = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
		if(scoContainer == null)
			return Collections.emptyList();
		
		List<CmiData> datas = collectData(scoContainer);
		Collections.sort(datas, new CmiDataComparator());
		return datas;
	}
	
	private List<CmiData> collectData(VFSContainer scoFolder) {
		List<CmiData> datas = new ArrayList<CmiData>();
		
		List<VFSItem> contents = scoFolder.getItems(new XMLFilter());
		for(VFSItem file:contents) {
			ScoDocument document = new ScoDocument(null);
			try {
				if(file instanceof LocalFileImpl) {
					document.loadDocument(((LocalFileImpl)file).getBasefile());
				}
				else {
					logger.warn("Cannot use this type of VSFItem to load a SCO Datamodel: " + file.getClass().getName(), null);
					continue;
				}
				
				String fileName = file.getName();
				String itemId = fileName.substring(0, fileName.length() - 4);
				String[][] scoModel = document.getScoModel();
				for(String[] line:scoModel) {
					datas.add(new CmiData(itemId, line[0], line[1]));
				}
			} catch (Exception e) {
				logger.error("Cannot load a SCO Datamodel", e);
			}
		}
		
		return datas;
	}
	
	public class XMLFilter implements VFSItemFilter {
		public boolean accept(VFSItem file) {
			String name = file.getName();
			if(name.endsWith(".xml") && !(name.equals(RELOAD_SETTINGS_FILE)))
			{
				return true;
			}
			return false;
		}
	}
}