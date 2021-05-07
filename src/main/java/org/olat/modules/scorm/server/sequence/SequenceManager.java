/**
 * RELOAD TOOLS Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir,
 * Paul Sharples Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: The
 * above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS
 * IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. Project
 * Management Contact: Oleg Liber Bolton Institute of Higher Education Deane
 * Road Bolton BL3 5AB UK e-mail: o.liber@bolton.ac.uk Technical Contact:
 * Phillip Beauvoir e-mail: p.beauvoir@bolton.ac.uk Paul Sharples e-mail:
 * p.sharples@bolton.ac.uk Web: http://www.reload.ac.uk
 */
package org.olat.modules.scorm.server.sequence;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.olat.modules.scorm.SettingsHandler;

/**
 * A class used to figure out the sequencing for a package. It has methods which
 * the LMS frameset can call to find out what the current state of the package
 * is at any one given time. - ie what item comes next?. This class holds a
 * hashtable of ItemSequence (the class used to house each item). It also holds
 * a PrerequisiteManager - the class used to figure out the AICC script which
 * can be found under the prerequisites section of a scorm package.
 * 
 * @author Paul Sharples
 */
public class SequenceManager {

	protected static final String COURSE_COMPLETED = "course_completed";
	/**
	 * Comment for <code>COURSE_COMPLETED_VALUE</code>
	 */
	public static final int COURSE_COMPLETED_VALUE = -1;

	/**
	 * keep a count of the Itemsequences that we have
	 */
	private int _itemCount = COURSE_COMPLETED_VALUE;

	/**
	 * our hashtable of reloaditemsequences
	 */
	private Hashtable<String,ItemSequence> _itemSequences = new Hashtable<>();

	/**
	 * A string to hold our currently loaded sco.
	 */
	private String _currentSco;

	/**
	 * Our prerequisite manager instance
	 */
	private PrerequisiteManager _prereqManager;
	private SettingsHandler settings;

	/**
	 * Default constructor
	 * 
	 * @param org
	 */
	public SequenceManager(String org, SettingsHandler settings) {
		this.settings = settings;
		_prereqManager = new PrerequisiteManager(org, settings);
	}

	/**
	 * Method to figure out, what the first item is to be launched when the
	 * package is played.
	 * 
	 * @return - a string referencing the sco ID
	 */
	public String findFirstItemToLaunch() {
		for (int i = 0; i <= _itemCount; i++) {
			String id = findItemFromIndex(i);
			ItemSequence anItem = _itemSequences.get(id);
			String preReqs = "";
			if (anItem.hasPrerequisites()) {
				preReqs = anItem.getPrerequisites();
			}
			boolean answer = _prereqManager.canLaunchItem(id, preReqs);
			if (answer) {
				// FIXME:gs:b scorm manager is not yet constructed so i do net get the
				return id;
			}
		}
		return COURSE_COMPLETED;
	}

	/**
	 * @return package status
	 */
	public String[][] getPackageStatus() {
		Vector<String[]> v = new Vector<>();
		for (int i = 0; i <= _itemCount; i++) {
			String id = findItemFromIndex(i);
			ItemSequence anItem = _itemSequences.get(id);
			if (!anItem.getLaunchUrl().equals("javascript:void(0)")) {
				//
				String[] itemStatus = { String.valueOf(anItem.getSequence()), _prereqManager.getStatus(id) };
				v.add(itemStatus);
			}
		}
		String[][] result = new String[v.size()][];
		v.copyInto(result);
		return result;
	}

	/**
	 * Method to check if an item has been completed
	 * 
	 * @param scoID
	 * @return true if item is compl.
	 */
	public boolean hasItemBeenCompleted(String scoID) {
		return _prereqManager.hasItemBeenCompleted(scoID);
	}

	/**
	 * Method to ask the prerequisite manager to show the prerequisite table.
	 */
	public void showPreReqTable() {
		_prereqManager.showPreReqTable();
	}

	/**
	 * A method to allow us to check if the current item has its prerequisites
	 * completed.
	 * 
	 * @return true if item has prereq.
	 */
	public boolean checkItemsPrerequisites() {
		if (getItem(_currentSco).hasPrerequisites()) { return _prereqManager.checkPrerequisites(getItem(_currentSco).getPrerequisites()); }
		return true;
	}

	/**
	 * A method to allow us to check if the current item has its prerequisites
	 * completed. Can be passed a item id.
	 * 
	 * @param itemID
	 * @return if item has prereq.
	 */
	public boolean checkItemsPrerequisites(String itemID) {
		if (getItem(itemID).hasPrerequisites()) { return _prereqManager.checkPrerequisites(getItem(itemID).getPrerequisites()); }
		return true;
	}

	/**
	 * Check if an item actaully exists
	 * 
	 * @param item
	 * @return if item exists
	 */
	public boolean doesItemExist(String item) {
		return _prereqManager.doesItemExist(item);
	}

	/**
	 * Method to allow us to update our prerequisite table.
	 * 
	 * @param sco
	 * @param status
	 * @param persist
	 */
	public void addtoPrereqTable(String sco, String status, boolean persist) {
		_prereqManager.updatePrerequisites(sco, status, persist);
	}

	/**
	 * Method to allow us to set the current sco
	 * 
	 * @param _sco
	 */
	public void setCurrentSco(String _sco) {
		_currentSco = _sco;
	}

	/**
	 * Method to allow us to get the current sco ID
	 * 
	 * @return the current sco
	 */
	public String getCurrentSco() {
		return _currentSco;
	}

	/**
	 * A Method to get a itemsequence, based on its scoID (which also happens to
	 * be its key)
	 * 
	 * @param itemId
	 * @return an ItemSequence
	 */
	public ItemSequence getItem(String itemId) {
		ItemSequence anItem = _itemSequences.get(itemId);
		return anItem;
	}

	/**
	 * Check if an item is a sco or not
	 * 
	 * @param itemId
	 * @return true if item is sco
	 */
	public boolean isItemSco(String itemId) {
		ItemSequence itemSequence = getItem(itemId);
		return itemSequence.isItemSco();
	}

	/**
	 * Method to populate a single instance of itemsequence with the information
	 * found within the settings(navigation) xml file. Its then added to the
	 * hastable of itemsequence found above
	 * 
	 * @param scoId - actual scoID
	 * @param launchUrl - the launch
	 * @param sequence - this scos order or sequence
	 * @param scoType - SCO or Asset
	 * @param title - The title of this sco (found in manifest <title>)
	 * @param prereqs - any prerequisites for this sco
	 */
	public void addNewItem(String scoId, String launchUrl, int sequence, String scoType, String title, String prereqs) {
		ItemSequence anItem = new ItemSequence(settings);
		anItem.setItemId(scoId);
		anItem.setLaunchUrl(launchUrl);
		anItem.setSequence(sequence);
		anItem.setScoType(scoType);
		anItem.setTitle(title);
		anItem.setPrerequisites(prereqs);
		_itemSequences.put(scoId, anItem);
		_itemCount++;
	}

	/**
	 * Methos to return the correct launch url for this sco
	 * 
	 * @param scoIdentifier
	 * @return an url string
	 */
	public String getLaunchFromId(String scoIdentifier) {
		ItemSequence anItem = _itemSequences.get(scoIdentifier);
		String theURL = anItem.getLaunchUrl();
		if (theURL == null) theURL = "";
		return theURL;
	}

	/**
	 * Given a identifier - return the items sequence
	 * 
	 * @param scoIdentifier
	 * @return return the items sequence
	 */
	public int getSequenceFromId(String scoIdentifier) {
		if (scoIdentifier.equals(COURSE_COMPLETED)) { return COURSE_COMPLETED_VALUE; }
		ItemSequence anItem = _itemSequences.get(scoIdentifier);
		return anItem.getSequence();
	}

	/**
	 * A method to find a scoID from the itemsequence hashtable, based on an index
	 * (number). Used when we need to find out, what the next item is (in order)
	 * 
	 * @param index (number)
	 * @return a sco ID
	 */
	public String findItemFromIndex(int index) {
		Enumeration<String> keys = _itemSequences.keys();
		while (keys.hasMoreElements()) {
			String scoID = keys.nextElement();
			ItemSequence anItem = _itemSequences.get(scoID);
			if (anItem.getSequence() == index) { return anItem.getItemId(); }
		}
		return null;
	}

	/**
	 * Method to find out if requested sco is the first item in the course
	 * 
	 * @param scoIdentifier
	 * @return true or false
	 */
	public boolean isFirstItem(String scoIdentifier) {
		ItemSequence anItem = _itemSequences.get(scoIdentifier);
		int seqPosition = anItem.getSequence();
		return (seqPosition == 1);
	}

	/**
	 * Method to find out if requested sco is the last item in the course
	 * 
	 * @param scoIdentifier
	 * @return true or false
	 */
	public boolean isLastItem(String scoIdentifier) {
		ItemSequence anItem = _itemSequences.get(scoIdentifier);
		int seqPosition = anItem.getSequence();
		return (seqPosition == _itemSequences.size());
	}

}
