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
package org.olat.modules.scorm.assessment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.nodes.CourseNode;
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
public class ScormAssessmentManager {
	
	public static final String RELOAD_SETTINGS_FILE = "reload-settings.xml";
	
	private static final ScormAssessmentManager instance = new ScormAssessmentManager();
	private static final Logger logger = Tracing.createLoggerFor(ScormAssessmentManager.class);
	
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
			return new SequencerModel(fileImpl.getBasefile());
		} else if (reloadSettingsFile != null) {
			throw new OLATRuntimeException(this.getClass(), "Programming error, SCORM results must be file based", null);
		}
		return null;
	}
	
	//fxdiff FXOLAT-108: reset SCORM test
	public boolean deleteResults(String username, CourseEnvironment courseEnv, CourseNode node) {
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
	public Map<Date, List<CmiData>> visitScoDatasMultiResults(String username, CourseEnvironment courseEnv, CourseNode node) {
		Map<Date, List<CmiData>> cmiDataObjects = new HashMap<>();
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
		List<CmiData> datas = new ArrayList<>();
		ScoDocument document = new ScoDocument(null);
		try {
			if(scoFile instanceof LocalFileImpl) {
				document.loadDocument(((LocalFileImpl)scoFile).getBasefile());
			}
			else {
				logger.warn("Cannot use this type of VSFItem to load a SCO Datamodel: " + scoFile.getClass().getName());
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
	
	public String getLastLessonStatus(String username, CourseEnvironment courseEnv, ScormCourseNode node) {
		List<CmiData> scoDatas = visitScoDatas(username, courseEnv, node);
		for(CmiData scoData:scoDatas) {
			if("cmi.core.lesson_status".equals(scoData.getKey())) {
				return scoData.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Return all the datas in the sco datamodels of a SCORM course
	 * @param username
	 * @param courseEnv
	 * @param node
	 * @return
	 */
	public List<CmiData> visitScoDatas(String username, CourseEnvironment courseEnv, ScormCourseNode node) {
		VFSContainer scoContainer = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
		if(scoContainer == null) {
			return Collections.emptyList();
		}
		
		List<VFSItem> contents = scoContainer.getItems(new XMLFilter());
		if(contents.isEmpty()) {
			return Collections.emptyList();
		}
		
		if(contents.size() > 1) {
				Collections.sort(contents, new FileDateComparator());
		}
		VFSItem file = contents.get(0);
		List<CmiData> datas = collectData(file);
		return datas;
	}
	
	public class XMLFilter implements VFSItemFilter {
		@Override
		public boolean accept(VFSItem file) {
			String name = file.getName();
			if(name.endsWith(".xml") && !(name.equals(RELOAD_SETTINGS_FILE)))
			{
				return true;
			}
			return false;
		}
	}
	
	public class FileDateComparator implements Comparator<VFSItem> {

		@Override
		public int compare(VFSItem f1, VFSItem f2) {
			if(f1 == null) return -1;
			if(f2 == null) return 1;
			long l1 = f1.getLastModified();
			long l2 = f2.getLastModified();
			if(l1 < l2) {
				return 1;
			}
			if (l1 == l2) {
				return 0;
			}
			return -1;
		}
	}
}