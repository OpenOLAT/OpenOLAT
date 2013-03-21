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

package org.olat.ims.qti;

/**
 * Initial Date:  04.04.2003
 *
 * @author Mike Stock
 */
public class QTIConstants {

	// Navigator status
	public static final int STATUS_NONE = -1;
	public static final int ASSESSMENT_RUNNING = 7;
	public static final int ASSESSMENT_FINISHED = 8;
	public static final int ASSESSMENT_CANCELED = 9;

	// Navigator successes
	public static final int ITEM_SUBMITTED = 5;
	public static final int SECTION_SUBMITTED = 6;
	
	// we have just submitted an answer, and depending on the navigator, are shown a new item or a "in between screen" with some
	// confirmative message
	public static final int MESSAGE_NONE = -1;
	public static final int MESSAGE_ITEM_SUBMITTED = 10;
	public static final int MESSAGE_SECTION_SUBMITTED = 11;
	public static final int MESSAGE_SECTION_INFODEMANDED = 12;
	public static final int MESSAGE_ASSESSMENT_SUBMITTED = 13;
	public static final int MESSAGE_ASSESSMENT_CANCELED = 14;
	public static final int MESSAGE_ASSESSMENT_INFODEMANDED = 15;
	
	// Navigator errors
	public static final int ERROR_NONE = -1;
	public static final int ERROR_SUBMITTEDITEM_OUTOFTIME = 1;
	public static final int ERROR_SUBMITTEDITEM_TOOMANYATTEMPTS = 2;
	public static final int ERROR_ITEM_OUTOFTIME = 12;
	public static final int ERROR_SUBMITTEDSECTION_OUTOFTIME = 3;
	public static final int ERROR_SECTION_OUTOFTIME = 13;
	public static final int ERROR_ASSESSMENT_OUTOFTIME = 4;
	public static final int ERROR_SECTION_PART_OUTOFTIME = 16;

	// workflow commands
	public static final String QTI_WF_START = "start";
	public static final String QTI_WF_PREVIEW = "preview";
	public static final String QTI_WF_SUBMIT = "sas";
	public static final String QTI_WF_CANCEL = "cancel";
	public static final String QTI_WF_SUSPEND = "suspend";
	public static final String QTI_WF_ID = "aiid";

	// attributes
	public static final String ATTR_RESULT = "result";
	public static final String ATTR_DLPTR = "dlptr";
	public static final String ATTR_AIID = "aiid";
	public static final String ATTR_PREVIEWDOC = "previewDoc";
	
	//xml doctype
	public static final String XML_DOCUMENT_ROOT = "questestinterop";
	public static final String XML_DOCUMENT_DTD = "ims_qtiasiv1p2p1.dtd";
	
	//metadata
	public static final String META_LEVEL_OF_DIFFICULTY = "qmd_levelofdifficulty";
	public static final String META_ITEM_TYPE = "qmd_itemtype";
	public static final String META_TOOLVENDOR = "qmd_toolvendor";
	
	// question pool
	public static final String QTI_12_FORMAT = "IMS QTI 1.2";
	
}
