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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSSuccess;
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
		if(reloadSettingsFile instanceof LocalFileImpl fileImpl) {
			return new SequencerModel(fileImpl.getBasefile());
		} else if (reloadSettingsFile != null) {
			throw new OLATRuntimeException(this.getClass(), "Programming error, SCORM results must be file based", null);
		}
		return null;
	}

	public boolean deleteResults(Identity assessedIdentity, CourseEnvironment courseEnv, CourseNode node) {
		return deleteResults(assessedIdentity.getName(), courseEnv, node);
	}
	
	public boolean deleteResults(String username, CourseEnvironment courseEnv, CourseNode node) {
		VFSContainer scoDirectory = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
		if(scoDirectory == null) return true; //nothing to reset -> ok
		
		return (scoDirectory.deleteSilently() == VFSSuccess.SUCCESS);
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
	public List<ScoCmiData> visitScoDatasMultiResults(String username, CourseEnvironment courseEnv, CourseNode node) {
		VFSContainer scoContainer = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
		if(scoContainer == null) {
			return null;
		}
		
		final List<ScoCmiData> cmiDataObjects = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		List<VFSItem> contents = scoContainer.getItems(new XMLFilter());
		for(VFSItem file:contents) {
			if (file instanceof VFSLeaf itemFile) {
				List<CmiData> item = collectData(itemFile);
				if(item != null) {
					//modified date
					cal.setTimeInMillis(file.getLastModified());
					Date date = cal.getTime();
					Sco sco = getScoId(date, item);
					if(sco != null) {// empty file -> no data
						cmiDataObjects.add(new ScoCmiData(sco.id(), sco.last(), date, itemFile, item));
					}
				}
			}
		}
		
		List<ScoCmiData> deduplicatedCmiDataObjects = deduplicate(cmiDataObjects);
		return deduplicatedCmiDataObjects;
	}
	
	private List<ScoCmiData> deduplicate(List<ScoCmiData> dataList) {
		Set<String> scoIds = dataList.stream()
				.map(ScoCmiData::scoId)
				.collect(Collectors.toSet());
		
		List<ScoCmiData> deduplicatedDataList = new ArrayList<>(dataList.size());
		
		for(String scoId:scoIds) {
			List<ScoCmiData> scoDataList = dataList.stream()
					.filter(r -> scoId.equals(r.scoId()))
					.toList();
			if(scoDataList.size() == 1) {
				deduplicatedDataList.addAll(scoDataList);
			} else if(scoDataList.size() > 1) {
				deduplicatedDataList.addAll(deduplicateSco(scoDataList));
			}
		}
		
		return deduplicatedDataList;
	}
	
	private List<ScoCmiData> deduplicateSco(List<ScoCmiData> rows) {
		Optional<ScoCmiData> currentRow = rows.stream().filter(r -> r.current())
				.findFirst();
		List<ScoCmiData> notCurrentRows = rows.stream().filter(r -> !r.current())
				.collect(Collectors.toList());
		if(currentRow.isEmpty() || notCurrentRows.isEmpty()) {
			return rows;
		}
		
		if(notCurrentRows.size() > 1) {
			Collections.sort(notCurrentRows, (r1, r2) -> {
				return r1.date().compareTo(r2.date());
			});
		}
		
		ScoCmiData lastNotCurrentRow = notCurrentRows.get(notCurrentRows.size() - 1);
		if(lastNotCurrentRow.itemFile().getSize() == currentRow.get().itemFile().getSize()) {
			String hashCurrent = VFSManager.checksumSha256(currentRow.get().itemFile());
			String hashNotCurrent = VFSManager.checksumSha256(lastNotCurrentRow.itemFile());
			if(hashCurrent != null && hashNotCurrent != null && hashCurrent.equals(hashNotCurrent)) {
				// The files are the same
				List<ScoCmiData> data = new ArrayList<>(rows);
				data.remove(lastNotCurrentRow);
				return data;
			}
		}
		return rows;
	}
	
	private Sco getScoId(Date date, List<CmiData> cmiObject) {
		if(cmiObject != null && !cmiObject.isEmpty()) {
			CmiData data = cmiObject.get(0);
			long time = date.getTime();
			String itemId = data.getItemId();
			int index = itemId.lastIndexOf('_');
			if(index >= 0) {
				String marker = itemId.substring(index + 1, itemId.length());
				if(StringHelper.isLong(marker)) {
					long longMarker = Long.parseLong(marker);
					long diff = Math.abs(time - longMarker);
					if(diff < 1000) {// less than a second
						return new Sco(itemId.substring(0, index), false);
					}
				}
			}
			return new Sco(data.getItemId(), true);
		}
		return null;
	}
	
	private record Sco(String id, boolean last) {
		//
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
			if(scoFile instanceof LocalFileImpl scoLeaf) {
				document.loadDocument(scoLeaf.getBasefile());
			} else {
				logger.warn("Cannot use this type of VSFItem to load a SCO Datamodel: {}", scoFile.getClass().getName());
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
	
	public static class XMLFilter implements VFSItemFilter {
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
	
	public static class FileDateComparator implements Comparator<VFSItem> {

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