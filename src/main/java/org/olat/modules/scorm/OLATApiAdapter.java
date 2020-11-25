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
* <p>
*/
package org.olat.modules.scorm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.scorm.manager.ScormManager;
import org.olat.modules.scorm.server.beans.LMSDataFormBean;
import org.olat.modules.scorm.server.beans.LMSDataHandler;
import org.olat.modules.scorm.server.beans.LMSResultsBean;
import org.olat.modules.scorm.server.sequence.ItemSequence;

import ch.ethz.pfplms.scorm.api.ApiAdapter;

/**
 * OLATApiAdapter implements the ApiAdapter Interface from the pfplms code which was initially
 * designed for applet use. For the 'Backend' it uses portions of the code developed for the reload
 * scorm player.
 * see: http://www.scorm.com/scorm-explained/technical-scorm/run-time/run-time-reference/ for an nice overview of the datamodel
 *
 * @author guido
 */
public	class OLATApiAdapter implements ch.ethz.pfplms.scorm.api.ApiAdapterInterface {

	private static final Logger log = Tracing.createLoggerFor(OLATApiAdapter.class);
	
	private final ApiAdapter core; 

	private Hashtable<String,String> olatScoCmi = new Hashtable<>();

	private String  olatStudentId;
	private String  olatStudentName;
	//was used as reference id like out repo id
	
	//the sco id
	private String  olatScoId;

	private boolean isLaunched  = false;
	private boolean isLaunching = false;
	
	private LMSDataHandler odatahandler;
	private ScormManager scormManager;
	private SettingsHandlerImpl scormSettingsHandler;
	private final List<ScormAPICallback> apiCallbacks = new ArrayList<>(2);
	// 
	private Properties scoresProp; // keys: sahsId; values = raw score of an sco
	private Properties lessonStatusProp;
	
	private static final String SCORE_IDENT = "cmi.core.score.raw";
	private static final String LESSON_STATUS_IDENT = "cmi.core.lesson_status";
	private File scorePropsFile;
	private File lessonStatusPropsFile;
	
	/**
	 * creates a new API adapter
	 */
	OLATApiAdapter () {
		core = new ApiAdapter();
	}
	
	public void addAPIListener(ScormAPICallback apiCallback) {
		if(apiCallback != null) {
			apiCallbacks.add(apiCallback);
		}
	}

	/**
	 * @param cpRoot
	 * @param repoId
	 * @param courseId
	 * @param userPath
	 * @param studentId - the olat username
	 * @param studentName - the students name
	 * @param isVerbose prints out what is going on inside the scorm RTE
	 */
	public	final void init (File cpRoot, String repoId, String courseId, String storagePath, String studentId, String studentName, String lesson_mode, String credit_mode, int controllerHashCode)
	throws IOException {
		this.olatStudentId   = studentId;
		this.olatStudentName = studentName;
		say ("cmi.core.student_id=" +olatStudentId);
		say ("cmi.core.student_name=" +olatStudentName);
		scormSettingsHandler = new SettingsHandlerImpl(cpRoot.getAbsolutePath(), repoId, courseId, storagePath, studentName, studentId, lesson_mode, credit_mode, controllerHashCode);
		
		// get a path for the scores per sco
		String savePath = scormSettingsHandler.getFilePath();
		scorePropsFile = new File(savePath, "_olat_score.properties");
		scoresProp = new Properties();
		if (scorePropsFile.exists()) {
			try(InputStream is = new BufferedInputStream(new FileInputStream(scorePropsFile))) {
				scoresProp.load(is);
			} catch (IOException e) {
				throw e;
			}
		}
		
		lessonStatusPropsFile = new File(savePath, "_olat_lesson_status.properties");
		lessonStatusProp = new Properties();
		if (lessonStatusPropsFile.exists()) {
			try(InputStream is = new BufferedInputStream(new FileInputStream(lessonStatusPropsFile))) {
				lessonStatusProp.load(is);
			} catch (IOException e) {
				throw e;
			}
		}
		
		scormManager = new ScormManager(cpRoot.getAbsolutePath(), true, true, true, scormSettingsHandler);
	}
	
	public String getCreditMode() {
		return scormSettingsHandler.getCreditMode();
	}
	
	public String getLessonMode() {
		return scormSettingsHandler.getLessonMode();
	}
	
	public int getNumOfSCOs() {
		return scormManager.getNumOfSCOs();
	}

	private final void say (String s) {
		
			log.debug("core: {}", s);
		
	}

	/**
	 * @param sahs_id
	 */
	public	final void launchItem (String scoId) {
		
		if (isLaunching) {
			say ("SCO " +olatScoId +" is launching.");
			return;
		}
		if (isLaunched && scoId.equals(olatScoId)) {
			say ("SCO " +scoId +" is already running.");
			return;
		}
		olatScoCmi.clear();

		say ("Launching sahs " +scoId);

		if (isLaunched) {
			say ("SCO "+olatScoId +" will be unloaded.");

		} else {
			
			isLaunching = true;
			olatScoId     = scoId;

			//putting all cmi from the olat storage to the local storage
			LMSDataFormBean lmsDataBean = new LMSDataFormBean();
			lmsDataBean.setItemID(scoId);
			lmsDataBean.setLmsAction("get");
			odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
			LMSResultsBean lmsBean = odatahandler.getResultsBean();
			olatScoCmi.clear();
			String[][] strArr = lmsBean.getCmiStrings();
			String key = "";
			String value = "";
			
			if(strArr != null){
				for(int i=0;i<strArr.length;i++){
						key = strArr[i][0];
						value = strArr[i][1];
						olatScoCmi.put(key, value);
						log.debug("passing cmi data to api adapter: "+key +": "+ value);
		
				}
			}
		}
	}

	public	final void olatSetValue (String l, String r) {
		if (r == null) r = ""; // MSIE bug
		say ("OlatSetValue("+l+"="+r+")");
		if (l != null) olatScoCmi.put (l, r);
	}

	public	final void olatAbortSco (String scoId) {
		if (!olatScoId.equals (scoId)) return;
		isLaunching = false;
		if (!isLaunched) return;
		say ("Warning: sco " +scoId +" did not call LMSFinish()");
		olatFinish (false);
		core.reset();
	}

	private	final void olatInitialize () {
		
		isLaunching = false;
		core.sysPut ("cmi.core.student_id",   olatStudentId);
		core.sysPut ("cmi.core.student_name", olatStudentName);
		core.sysPut (olatScoCmi);
		core.transBegin();
		isLaunched  = true;
	}

	private	final void olatFinish (boolean commit) {
		if (!isLaunched) return;
		isLaunched = false;
		if (commit) olatCommit(false); // Stupid "implicit commit"
		// <OLATCE-289>
		archiveScoData();
		// </OLATCE-289>
	}

	/**
	 * 
	 * @param isACommit true, if the call comes from a lmscommit, false if it comes from a lmsfinish
	 * @return
	 */
	private final String olatCommit (boolean isACommit) {
		if (olatScoId == null) return "false";

		core.transEnd();
		
		@SuppressWarnings("unchecked")
		Hashtable <String,String>ins = core.getTransNew ();
		@SuppressWarnings("unchecked")
		Hashtable <String,String>mod = core.getTransMod ();
		core.transBegin();
		
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(olatScoId);
		lmsDataBean.setNextAction("5");
		lmsDataBean.setLmsAction("update");
		Map <String,String>cmiData = new HashMap<>();
		
		if (ins.size() > 0){
			Set <String> set = ins.keySet();
			for(Iterator<String> it = set.iterator();it.hasNext();){
				String cmi = it.next();
				olatScoCmi.remove(cmi);
				olatScoCmi.put(cmi,ins.get(cmi));
			}
		}
		if(mod.size() > 0){
			Set <String>set = mod.keySet();
			for(Iterator <String>it = set.iterator();it.hasNext();){
				String cmi = it.next();
				olatScoCmi.remove(cmi);
				olatScoCmi.put(cmi,mod.get(cmi));
			}
		}
		cmiData.putAll(olatScoCmi);
		
		//work around for missing cmi's (needed by reload code, but not used in ilias code)
		if(cmiData.get("cmi.interactions._count") != null && cmiData.get("cmi.interactions._count") != "0"){
			int count = Integer.parseInt(cmiData.get("cmi.interactions._count"));
			for(int i=0;i<count;i++){
				//OLAT-4271: check first if cmi.interactions.n.objectives._count exist before putting a default one
				String objectivesCount = cmiData.get("cmi.interactions."+ i +".objectives._count");
				if(!StringHelper.containsNonWhitespace(objectivesCount)) {
					cmiData.put("cmi.interactions."+ i +".objectives._count","0");
				}
			}
		}
		if (isACommit) {
			String rawScore = cmiData.get(SCORE_IDENT);
			String lessonStatus = cmiData.get(LESSON_STATUS_IDENT);
			if (StringHelper.containsNonWhitespace(rawScore) || StringHelper.containsNonWhitespace(lessonStatus)) {
				// we have a score set in this sco.
				// persist
				
				// to prevent problems with bad xmlhttprequest timings
				synchronized(this) { //o_clusterOK by:fj: instance is spawned by the ScormAPIandDisplayController
					if(StringHelper.containsNonWhitespace(rawScore)) {
						scoresProp.put(olatScoId, rawScore);
						try(OutputStream os = new BufferedOutputStream(new FileOutputStream(scorePropsFile))) {
							scoresProp.store(os, null);
						} catch (IOException e) {
							throw new OLATRuntimeException(this.getClass(), "could not save scorm-properties-file: "+scorePropsFile.getAbsolutePath(), e);
						}
					}

					if(StringHelper.containsNonWhitespace(lessonStatus)) {
						lessonStatusProp.put(olatScoId, lessonStatus);
						try(OutputStream os = new BufferedOutputStream(new FileOutputStream(lessonStatusPropsFile))) {
							lessonStatusProp.store(os, null);
						} catch (IOException e) {
							throw new OLATRuntimeException(this.getClass(), "could not save scorm-properties-file: "+scorePropsFile.getAbsolutePath(), e);
						}
					}
					// notify
					if (!apiCallbacks.isEmpty()) {
						for(ScormAPICallback apiCallback:apiCallbacks) {
							apiCallback.lmsCommit(olatScoId, scoresProp, lessonStatusProp);
						}
					}
				}
			}
		// <OLATCE-289>
		}else{
				//if "isACommit" is false, this is a lmsFinish and the apiCallback shall save the points an passed information
				if (!apiCallbacks.isEmpty()) {
					String rawScore = cmiData.get(SCORE_IDENT);
					if (rawScore != null && !rawScore.equals("")) {
						scoresProp.put(olatScoId, rawScore);
					}

					String lessonStatus = cmiData.get(LESSON_STATUS_IDENT);
					if (StringHelper.containsNonWhitespace(lessonStatus)) {
						lessonStatusProp.put(olatScoId, lessonStatus);
					}
					
					for(ScormAPICallback apiCallback:apiCallbacks) {
						apiCallback.lmsFinish(olatScoId, scoresProp, lessonStatusProp);
					}
				}
			// </OLATCE-289>
		}
		
		try {
			lmsDataBean.setDataAsMap(cmiData);
			odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
			odatahandler.updateCMIData(olatScoId);
			return "true";
		} catch (Exception e) {
			log.error("Error during commit", e);
			return "false";
		}
	}
	
	/**
	 * @return a String that points to the last accessed sco itemId
	 */
	public String getScormLastAccessedItemId(){
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setLmsAction("boot");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		return lmsBean.getItemID();
	}
	
	//<OLATCE-289>
	/**
	 * Archive the current SCORM CMI Data, see ItemSequence.archiveScoData
	 * @return
	 */
	public boolean archiveScoData() {
		boolean success = false;
		try {
			String itemId = scormManager.getSequence().findItemFromIndex(Integer.valueOf(olatScoId));
			ItemSequence item = scormManager.getSequence().getItem(itemId);
			if (item != null) {
				success = item.archiveScoData();
			}
		} catch (Exception e) {
			log.error("Error at OLATApiAdapter.archiveScoData(): ", e);
		}
		return success;
	}
	// </OLATCE-289>
	
	/**
	 * @param itemId
	 * @return true if the item is completed
	 */
	public boolean isItemCompleted(String itemId){
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(itemId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		return lmsBean.getIsItemCompleted().equals("true");
	}
	
	/**
	 * @param itemId
	 * @return true if item has any not fullfilled preconditions
	 */
	public boolean hasItemPrerequisites(String itemId) {
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(itemId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		return lmsBean.getHasPrerequisites().equals("true");
	}
	
	/**
	 * @return Map containing the recent sco items status
	 */
	public Map <String,String>getScoItemsStatus(){
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setLmsAction("boot");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		String[][] preReqTbl = lmsBean.getPreReqTable();
		Map <String,String>itemsStatus = new HashMap<>();
		//put table into map 
		for(int i=0; i < preReqTbl.length; i++){
			if(preReqTbl[i][1].equals("not attempted")) preReqTbl[i][1] ="not_attempted";
			itemsStatus.put(preReqTbl[i][0], preReqTbl[i][1]);
		}
		return itemsStatus;	
	}

	/**
	 * @param recentId
	 * @return the previos Sco itemId
	 */
	public Integer getPreviousSco(String recentId) {
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(recentId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		String[][] pretable = lmsBean.getPreReqTable();
		String previousNavScoId = "-1";
		for(int i=0; i < pretable.length; i++){
			if(pretable[i][0].equals(recentId) &&  (i != 0 )){
				previousNavScoId =  pretable[--i][0];
				break;
			}
		}
		return Integer.valueOf(previousNavScoId);
	}

	/**
	 * @param recentId
	 * @return the next Sco itemId
	 */
	public Integer getNextSco(String recentId) {
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(recentId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		String[][] pretable = lmsBean.getPreReqTable();
		String nextNavScoId = "-1";
		for(int i=0; i < pretable.length; i++){
			if(pretable[i][0].equals(recentId) && (i != pretable.length-1)){
				nextNavScoId =  pretable[++i][0];
				break;
			}
		}
		return Integer.valueOf(nextNavScoId);
	}
	
	/****************************************************************************************
	 * The API functions that an Scorm SCO can call
	 * 
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSInitialize(java.lang.String)
	 */
	public final String LMSInitialize (String s) { 
		String rv = core.LMSInitialize(s);
		say(" ----------------- ");
		say ("LMSInitialize("+s+")="+rv);
		if (rv.equals("false")) return rv;
		core.reset();
		rv = core.LMSInitialize(s);
		olatInitialize ();
		return rv;
	}
	
	/**
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSCommit(java.lang.String)
	 */
	public final String LMSCommit (String s) {
		try {
			String rv = core.LMSCommit(s);
			if (rv.equals("false")) return rv;
			rv = olatCommit(true); 
			say ("LMSCommit("+s+")="+rv);
			return rv;
		} catch (Exception e) {
			log.error("LMSCommit failed: " + s, e);
			return "false";
		}
	}
	
	/**
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSFinish(java.lang.String)
	 */
	public final String LMSFinish (String s) {
		try {
			String rv = core.LMSFinish(s);
			say ("LMSFinish("+s+")="+rv);
			say(" ----------------- ");
			if (rv.equals("false")) return rv;
			olatFinish(true);
			core.reset();
			return rv;
		} catch (Exception e) {
			log.error("LMSFinish failed: " + s, e);
			return "false";
		}
	}
	
	/**
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSGetDiagnostic(java.lang.String)
	 */
	public final String LMSGetDiagnostic (String e) {
		String rv = core.LMSGetDiagnostic (e);
		say ("LMSGetDiagnostic("+e+")="+rv);
		return rv;
	}
	
	/**
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSGetErrorString(java.lang.String)
	 */
	public final String LMSGetErrorString (String e) {
		String rv = core.LMSGetErrorString (e);
		say ("LMSGetErrorString("+e+")="+rv);
		return rv;
	}
	
	/**
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSGetLastError()
	 */
	public final String LMSGetLastError () {
		String rv = core.LMSGetLastError ();
		say ("LMSLastError()="+rv);
		return rv;
	}
	
	/**
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSGetValue(java.lang.String)
	 */
	public final String LMSGetValue (String l) {
		String rv = core.LMSGetValue (l);
		say ("LMSGetValue("+l+")="+rv);
		return rv;
	}
	
	/**
	 * @see ch.ethz.pfplms.scorm.api.ApiAdapterInterface#LMSSetValue(java.lang.String, java.lang.String)
	 */
	public final String LMSSetValue (String l, String r) {
		String rv = core.LMSSetValue (l, r);
		say ("LMSSetValue("+l+"="+r+")="+rv);
		return rv;
	}
}

