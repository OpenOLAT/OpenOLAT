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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.id.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.FileUtils;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  26 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class HistoryManager extends BasicManager {
	
	private static HistoryManager THIS;
	private static XStream historyStream = XStreamHelper.createXStreamInstance();
	static {
		//xstream config
	}
	
	private HistoryManager() {
		THIS = this;
	}
	
	public static HistoryManager getInstance() {
		return THIS;
	}
	
	public void persistHistoryPoint(Identity identity, HistoryPoint historyPoint) {
		try {
			String pathHomePage = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(identity.getName());
			File resumeXml = new File(pathHomePage, "resume.xml");
			if(!resumeXml.getParentFile().exists()) {
				resumeXml.getParentFile().mkdirs();
			}
			FileOutputStream out = new FileOutputStream(resumeXml);
			historyStream.toXML(historyPoint, out);
			FileUtils.closeSafely(out);
		} catch (Exception e) {
			logError("UserSession:::logging off write resume: ", e);
		}
	}
	
	public HistoryPoint readHistoryPoint(Identity identity) {
		try {
			String pathHomePage = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(identity.getName());
			File resumeXml = new File(pathHomePage, "resume.xml");
			if(resumeXml.exists()) {
				FileInputStream in = new FileInputStream(resumeXml);
				HistoryPoint point = (HistoryPoint)historyStream.fromXML(in);
				FileUtils.closeSafely(in);
				return point;
			}
			return null;
		} catch (Exception e) {
			logError("Cannot read resume file: ", e);
			return null;
		}
	}
	
	public void deleteHistory(Identity identity) {
		try {
			String pathHomePage = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(identity.getName());
			File resumeXml = new File(pathHomePage, "resume.xml");
			if(resumeXml.exists()) {
				resumeXml.delete();
			}
		} catch (Exception e) {
			logError("Can not delete history file", e);
		}
	}

	
	

}
