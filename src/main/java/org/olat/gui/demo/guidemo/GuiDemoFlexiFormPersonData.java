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
package org.olat.gui.demo.guidemo;

import java.io.File;

/**
 * 
 * Description:<br>
 * simple domain model class used in gui demo for flexi form
 * 
 * <P>
 * Initial Date:  06.09.2007 <br>
 * @author patrickb
 */
class GuiDemoFlexiFormPersonData {
	private String firstName1 = "";
	private String lastName1 = "";
	private String institution1 = "";
	private boolean readOnly1 = false;
	private File file1 = null;
	
	public GuiDemoFlexiFormPersonData(String firstName2, String lastName2, String institution2, boolean readOnly2, File file2){
		firstName1 = firstName2;
		lastName1 = lastName2;
		institution1 = institution2;
		readOnly1 = readOnly2;
		file1 = file2;
	}
	
	public GuiDemoFlexiFormPersonData() {
		//just a default constructor for empty data
	}

	/**
	 * @return Returns the readOnly.
	 */
	public boolean isReadOnly() {
		return readOnly1;
	}
	/**
	 * @param readOnly The readOnly to set.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly1 = readOnly;
	}
	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName() {
		return firstName1;
	}
	/**
	 * @param firstName The firstName to set.
	 */
	public void setFirstName(String firstName) {
		this.firstName1 = firstName;
	}
	/**
	 * @return Returns the institution.
	 */
	public String getInstitution() {
		return institution1;
	}
	/**
	 * @param institution The institution to set.
	 */
	public void setInstitution(String institution) {
		this.institution1 = institution;
	}
	/**
	 * @return Returns the lastName.
	 */
	public String getLastName() {
		return lastName1;
	}
	/**
	 * @param lastName The lastName to set.
	 */
	public void setLastName(String lastName) {
		this.lastName1 = lastName;
	}

	/**
	 * @return the file or NULL if not set
	 */
	public File getFile() {
		return file1;
	}

	/**
	 * The file or NULL if not set
	 * @param file2
	 */
	public void setFile(File file2) {
		this.file1 = file2;
	}
}