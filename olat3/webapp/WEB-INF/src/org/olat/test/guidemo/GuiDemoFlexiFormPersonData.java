package org.olat.test.guidemo;

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