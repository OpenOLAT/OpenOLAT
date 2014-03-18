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
package org.olat.modules.scorm.server.beans;

import java.util.Map;

/**
 * 
 * The "input" bean - used as part of struts to allow the updating
 * frame in the Scorm frameset to send form information back to 
 * an action class
 * 
 * @author Paul Sharples
 */
public class LMSDataFormBean {

    private String itemID;
    private String data;
    private Map<String,String> dataMap;
    private String lmsAction;
    private String nextAction;
    
    /**
     * @return the itemID
     */
    public String getItemID() {
        return (itemID);
    }

    /**
     * the itemID is and int(String) which points to the position in the navigation tree.
     * 
     * |0
     * |----1
     * ¦----2
     * |----|3
     * |	|----4
     * |	|----5
     * |
     * |----6
     * 
     * The sketch above means that the numbering is flat and the brach node have a number also.
     * @param itemID
     */
    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
    
    /**
     * @return the whole data as a String
     */
    public String getData() {
        return data;
    }

    /**
     * @param data
     */
    public void setData(String data) {
        this.data = data;
    }
    
    /**
     * @return the LMSAction
     */
    public String getLmsAction() {
        return lmsAction;
    }

    /**
     * the action String has to be one of
     * "get" get the dataModel for an itemId
     * "update" update the dataModel for an itemId
     * "boot" get the dataModel for the poistion the user left the course last time
     *  or the first item to lauch if the course has not yet been started by the user. 
     * @param lmsAction
     */
    public void setLmsAction(String lmsAction) {
        this.lmsAction = lmsAction;
    }
    
    /**
     * @return the nextItem
     */
    public String getNextAction() {
        return nextAction;
    }

    /**
     * NextAction points to the next itemID in the numbered item tree and has to be
     * a valid numer as String or "none"
     * @param nextAction
     */
    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

	/**
	 * @return data as map
	 */
	public Map<String,String> getDataAsMap() {
		return dataMap;
	}
	/**
	 * set the cmi data as map
	 */
	public void setDataAsMap(Map<String,String> dataMap) {
		this.dataMap = dataMap;
	}
}