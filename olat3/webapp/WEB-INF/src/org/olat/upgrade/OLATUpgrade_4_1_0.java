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


package org.olat.upgrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.filters.VFSContainerFilter;
import org.olat.fileresource.types.FileResource;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br> - Creates all efficiency statements for all users for all
 * courses
 * <P>
 * Initial Date: 15.08.2005 <br>
 * 
 * @author gnaegi
 */
public class OLATUpgrade_4_1_0 extends OLATUpgrade {
	private static final String TASK_CHECK_OPEN_QTI_EDITOR_SESSIONS = "check open qti editor sessions";
	private static final String VERSION = "OLAT_4.1.0";
	private static final String TASK_CLEAN_UP_MSGREAD_PROPERTIES_DONE = "unused message properties deleted";
	private static final String TASK_REPLACE_OLDINTERNALLINKS = "replace internal links with new form";
	
	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		// nothing to do here
		return false;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}

		// each message generates a property entry for each user if he read a
		// message in a forum. The deletion of the message did not delete the
		// property entry.
		cleanupUnusedMessageProperties(upgradeManager, uhd);
		
		// the qti editor creates a persistent lock with the help of the
		// repository entry metadata. This upgrade method searches the
		// olatdata/tmp/qtieditor folder for open qti editor sessions and
		// creates the needed changelog folder andalso the metadata lock.
		checkForOpenQTIEditorSessions(upgradeManager, uhd);
				
		//
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		return true;
	}

	private void checkForOpenQTIEditorSessions(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		//the qti editor creates a persistent lock with the help of the
		// repository entry metadata. This upgrade method searches the
		// olatdata/tmp/qtieditor folder for open qti editor sessions and
		// creates the needed changelog folder andalso the metadata lock.
		if (!uhd.getBooleanDataValue(TASK_CHECK_OPEN_QTI_EDITOR_SESSIONS)) {
			Tracing.logAudit("+------------------------------------------+",this.getClass());
			Tracing.logAudit("+... LOCKS FOR OPEN QTI EDITOR SESSIONS ...+",this.getClass());
			Tracing.logAudit("+------------------------------------------+",this.getClass());
			//
			BaseSecurity manager = BaseSecurityManager.getInstance();
			RepositoryManager rm = RepositoryManager.getInstance();
			RepositoryEntry myEntry;
			HashMap logmsg = new HashMap();
			VFSContainer qtiTmpDir = new LocalFolderImpl(QTIEditorPackage.getTmpBaseDir());
			VFSContainerFilter foldersOnly = new VFSContainerFilter();
			//folders in ../tmp/qtieditor hold the usernames
			List foldersUsername = qtiTmpDir.getItems(foldersOnly);
			for (Iterator iter = foldersUsername.iterator(); iter.hasNext();) {
				VFSContainer folderOfUser = (VFSContainer) iter.next();
				//the users folders holds folders with ids of OLATResourceable's
				List oResFolders = folderOfUser.getItems(foldersOnly);
				for (Iterator resources = oResFolders.iterator(); resources.hasNext();) {
					VFSContainer folderOfResource = (VFSContainer) resources.next();
					folderOfResource.createChildContainer(QTIEditorPackage.FOLDERNAMEFOR_CHANGELOG);
					
					//these are eiterh surveys or tests
					//try it as testresource then as survey, after this give up
					Long oresId = new Long(folderOfResource.getName());
					FileResource fr = new TestFileResource();
					fr.overrideResourceableId(oresId);
					myEntry = rm.lookupRepositoryEntry(fr,false);
					if(myEntry==null){
						//no qti test found, try the qti survey
						fr = new SurveyFileResource();
						fr.overrideResourceableId(oresId);
						myEntry = rm.lookupRepositoryEntry(fr,false);
					}
					//
					if(myEntry!=null){
						List identites = manager.getVisibleIdentitiesByPowerSearch(folderOfUser.getName(),null,false, null,null,null,null,null);
						if(identites!=null && identites.size()==1){
							//found exact one user, which is the expected case
							//a qti resource was found, update its metadata entry to generate a lock
							String repoEntry = myEntry.getDisplayname(); 
							String oresIdS = myEntry.getOlatResource().getResourceableId().toString();
							String oresIdT = myEntry.getOlatResource().getResourceableTypeName();
							if(logmsg.containsKey(oresIdS)){
								//collision! two or more sessions open on same resource!
								String users = (String)logmsg.get(oresIdS);
								logmsg.put(oresIdS,users+", "+folderOfUser.getName());
							}else{
								//mde = new MetaDataElement("editedBy",folderOfUser.getName());
								//myEntry.getMetaDataElements().add(mde);
								//rm.updateRepositoryEntry(myEntry);
								addQTIEditorSessionLock(fr,(Identity)identites.get(0));
								Tracing.logAudit("created persistent lock for user <"+folderOfUser.getName()+"> <"+repoEntry+" [ references "+oresIdS+" of type:"+oresIdT+"]>",this.getClass());
								logmsg.put(oresIdS,"[ "+repoEntry+"] "+folderOfUser.getName());
							}
						}else if(identites!=null && identites.size()>1){
							//found more then one user?? for the userlogin??
							Tracing.logAudit("\t*** NO *** persistent lock for user <"+folderOfUser.getName()+"> and entry <"+oresId.toString()+"> ! Cause: Found more then one identity for user!",this.getClass());
						}else{
							//found not user with given login??? as far as user deletion is not implemented, this will never happen.
							Tracing.logAudit("\t*** NO *** persistent lock for user <"+folderOfUser.getName()+"> and entry <"+oresId.toString()+"> ! Cause: User not found!",this.getClass());
						}
					}else{
						//no qti resource found?! deleted already
						Tracing.logAudit("\t*** NO *** persistent lock for user <"+folderOfUser.getName()+"> and entry <"+oresId.toString()+"> ! Cause: Entry not found!",this.getClass());
					}
				}
			}
			//write to the audit log which qti editor sessions are problematic
			Set keys = logmsg.keySet();
			if(keys!=null && keys.size()>0){
				Tracing.logAudit("List of (colliding) QTI Editor Sessions.",this.getClass());
				Tracing.logAudit("(colliding if more then one user is listed on the same resource)",this.getClass());
				Tracing.logAudit("\tQTI Resource id\t[Repository entry ] <list of users, where the first one holds a lock now>",this.getClass());
				for (Iterator iter = keys.iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					Tracing.logAudit("\t"+key+"\t"+(String)logmsg.get(key),this.getClass());
				}
			}else{
				Tracing.logAudit("No colliding qti editor sessions detected.",this.getClass());
			}

			Tracing.logAudit("+----------------------------------------+",this.getClass());
			Tracing.logAudit("+----------------------------------------+",this.getClass());
			Tracing.logAudit("+----------------------------------------+",this.getClass());
			
			uhd.setBooleanDataValue(TASK_CHECK_OPEN_QTI_EDITOR_SESSIONS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
	}

	private void cleanupUnusedMessageProperties(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		// BEGIN MSG CLEAN UP
		// each message generates a property entry for each user if he read a
		// message in a forum. The deletion of the message did not delete the
		// property entry.
		if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_MSGREAD_PROPERTIES_DONE)) {
			String query = "select o_property.id "+
										 "from o_property LEFT JOIN o_message "+
										 "ON o_property.longvalue=o_message.message_id "+
										 "where o_message.message_id is NULL "+
										 "AND o_property.category='rvst' "+
										 "AND o_property.resourcetypename='Forum'; ";
			
			
			try {
				Connection con = upgradeManager.getDataSource().getConnection();
				Statement stmt = con.createStatement();
				ResultSet results = stmt.executeQuery(query);
				
				//delete each property and do logging
				query = "delete from o_property where id = ?";
				PreparedStatement deleteStmt = con.prepareStatement(query);
				while (results.next()) {
			    long id = results.getLong("id");
			    Tracing.logAudit("Deleting unused property (see: bugs.olat.org/jira/browse/OLAT-1273) from table (o_property) with id = "+id, OLATUpgrade_4_1_0.class);
			    deleteStmt.setLong(1, id);
			    deleteStmt.execute();
			  }
				
				con.close();
				con = null;
			} catch (SQLException e) {
				Tracing.logWarn("Could not execute system upgrade sql query. Query:"+ query, e, OLATUpgrade_4_1_0.class);
				throw new StartupException("Could not execute system upgrade sql query. Query:"+ query, e);
			}
			uhd.setBooleanDataValue(TASK_CLEAN_UP_MSGREAD_PROPERTIES_DONE, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}

	private void addQTIEditorSessionLock(FileResource fr, Identity user){
		PropertyManager pm = PropertyManager.getInstance();
		String derivedLockString = OresHelper.createStringRepresenting(fr);
		Property newp = pm.createPropertyInstance(null, null, null, "o_lock", derivedLockString, null, user.getKey(), null, null);
		pm.saveProperty(newp);
	}
	
	
	public String getVersion() {
		return VERSION;
	}
	
	/**
	 * 
	 * @see org.olat.upgrade.OLATUpgrade#getAlterDbStatements(r)
	 */
	public String getAlterDbStatements() {
		return null; //till 6.1 was manual upgrade
	}

}
