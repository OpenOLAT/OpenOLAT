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

/**
 *  RELOAD TOOLS
 *
 *  Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir, Paul Sharples
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Project Management Contact:
 *
 *  Oleg Liber
 *  Bolton Institute of Higher Education
 *  Deane Road
 *  Bolton BL3 5AB
 *  UK
 *
 *  e-mail:   o.liber@bolton.ac.uk
 *
 *
 *  Technical Contact:
 *
 *  Phillip Beauvoir
 *  e-mail:   p.beauvoir@bolton.ac.uk
 *
 *  Paul Sharples
 *  e-mail:   p.sharples@bolton.ac.uk
 *
 *  Web:      http://www.reload.ac.uk
 *
 */
package org.olat.modules.scorm.server.sequence;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.modules.scorm.SettingsHandler;
import org.olat.modules.scorm.contentpackaging.SCORM12_Core;
import org.olat.modules.scorm.server.servermodels.ScoDocument;

/**
 * A class used to house a single item from a scorm package.  It should
 * only contain items that actually reference something. Here we store
 * data about this particular item - its launch, its title, its ID, etc
 * Also this class associates a ScoDocument with this item, but only if
 * its a sco, not for assets.  A ScoDocument looks after the persisting
 * of the CMI datamodel back to xml, and also reading it back into the javascript
 * server model.
 * 
 * @author Paul Sharples
 */
public class ItemSequence {

    // Here we store some information about this item
    private boolean _hasPrerequisites;
    private String _prerequisites;
    private String _scoitemID;
    private String _title;
    private String _launchUrl;
    private int _sequence;
    private String _scormType;
    // If this item is a sco, then we will associate it with a ScoDocument
    private ScoDocument _scoDataModel;
	private final SettingsHandler settings;

    /**
     * Constructor
     */
    public ItemSequence(SettingsHandler settings) {
    	this.settings = settings;
    }
           
    /**
     * Accessor method - set the title
     * @param title
     */    
    public void setTitle(String title){
         _title = title;
    }

    /**
     * Accessor method - get the title
     * @return title String
     */
    public String getTitle(){
         return _title;
    }

    /**
     * Accessor method set the launch
     * @param id
     */
    public void setLaunchUrl(String id){
        //_launchUrl = "../" + id;
        _launchUrl = id;
    }

    /**
     * Accessor method - get the launch
     * @return String launch uri
     */
    public String getLaunchUrl(){
        return _launchUrl;
    }

    /**
     * Accessor - set the sequence number
     * @param seq
     */
    public void setSequence(int seq){
        _sequence = seq;
    }

    /***
     * Accessor - get the sequence number
     * @return int
     */

    public int getSequence(){
        return _sequence;
    }

    /**
     * Accessor method - to return the full cmi datamodel as a 2-d string array
     * @return String[][]
     */
    public String[][] getScoModel(){
        return _scoDataModel.getScoModel();
    }

    /**
     * Method to allow us to update the cmi datamodel (persist it back to disk)
     * @param entries - the name/value pairs
     */
    public void updateClientModel(String[][] entries){
			_scoDataModel.doLmsCommit(entries);
    }
    
  	// <OLATCE-289>
    /**
     * This method copies the current cmi-file to a new file with timestamp so that all attempts
     * can be evaluated by the assessment tool.
     * @return
     */
    public boolean archiveScoData() {
    	File currentCmiFile = _scoDataModel.getFile().getAbsoluteFile();
    	LocalFileImpl currentCmiFileVFS = new LocalFileImpl(_scoDataModel.getFile().getAbsoluteFile());

    	String suffix = "." + FileUtils.getFileSuffix(currentCmiFile.getName());
    	
    	String newFileName = currentCmiFile.getName().substring(0, currentCmiFile.getName().indexOf(suffix))
    												+ "_" + String.valueOf(System.currentTimeMillis()
    												+ suffix);
    		
    	File outf = new File(currentCmiFile.getParentFile(), newFileName);
			OutputStream os = null;
			try {
				os = new FileOutputStream(outf);
			} catch (FileNotFoundException e) {
				return false;
			}
			InputStream is = currentCmiFileVFS.getInputStream();
			FileUtils.copy(is, os);
			FileUtils.closeSafely(os);
			FileUtils.closeSafely(is);
    	
    	return true;
    }
  	// </OLATCE-289>

    /**
     * Method to set the sco type for this class and if its an
     * sco, then load in the xml cmi model for it.
     * @param scoType string which is the ID of the item
     */
    public void setScoType(String scoType){
        _scormType = scoType;
    }

    /**
     * Method to associate and load in the cmi model for this sco (if it is one)
     */
    public void loadInModel() {
        // if this is a sco then we need to access the
        // cmi data model we created when importing the package
        if (isItemSco()) {
            if (_scoitemID != null) {
                ScoDocument anSco = new ScoDocument(settings);
                anSco.loadDocument(_scoitemID);
                _scoDataModel = anSco;
            }
        }
    }

    /**
     * Method to ascertain if an item is a SCO or not
     * @return true/false
     */
    public boolean isItemSco(){
        return (_scormType.equals(SCORM12_Core.SCO));
    }

    /**
     * Accessor method - to set this items ID
     * @param id
     */
    public void setItemId(String id){
        _scoitemID = id;
    }

    /**
     * Accessor method to allow us to get this item ID
     * @return String
     */
    public String getItemId(){
        return _scoitemID;
    }

    /**
     * Accessor method to allow us to return any prerequisites on this item
     * @return String
     */
    public String getPrerequisites(){
        return _prerequisites;
    }

    /**
     * Method to allow us to set any prerequisites for this item.
     * If there is a string of chars found in the prerequisistes slot, then
     * we parse it (using the static method in PrerequisiteManager), to ensure
     * that the string is a valid prerequisite string and not just rubbish.
     * If the string does not make sense, then it is junked!
     * @param aprereq
     */
    public void setPrerequisites(String aprereq){
        if (!aprereq.equals("")){
            // get rid of any spaces
            String pre = aprereq.replaceAll(" ", "");
            // check the prerequisites for legality
            if (PrerequisiteManager.isValid(pre)){
                _prerequisites = pre;
                _hasPrerequisites = true;
            }
            // if they are not legal, then junk them...
            else{
                _hasPrerequisites = false;
            }
        }
        else{
            _hasPrerequisites = false;
        }
    }

    /**
     * Accessor method to find aout if there are any prerequisistes attached
     * to this item.
     * @return - boolean
     */
    public boolean hasPrerequisites(){
        return _hasPrerequisites;
    }

}
