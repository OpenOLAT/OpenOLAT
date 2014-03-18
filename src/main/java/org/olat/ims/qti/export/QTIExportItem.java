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

package org.olat.ims.qti.export;

import java.util.Date;
import java.util.List;

import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.export.helper.QTIItemObject;

/**
 * Description: Takes one QTIItemObject and one QTIResult together to make the live of the formatter
 * easier.
 * 
 * @author Alexander Schneider
 */
public class QTIExportItem {

	private QTIResult qtir;
	private QTIItemObject item;
	private QTIExportItemFormatConfig config;
	
	public QTIExportItem(QTIResult qtir, QTIItemObject item) {
		this.qtir = qtir;
		this.item = item;
	}
	
	public List<String> getResponseColumns(){
		return item.getResponseColumns(qtir);
	}
	
	public boolean hasResult(){
		return this.qtir != null;
	}
	
	public String getScore(){
		return String.valueOf(this.qtir.getScore());
	}

	public Date getTimeStamp(){
		return this.qtir.getTstamp();
	}
	
	public Long getDuration(){
		return this.qtir.getDuration();
	}
	
	public boolean hasPositionsOfResponses(){
		return this.item.hasPositionsOfResponses();
	}
	
	public String getPositionsOfResponses(){
		return this.item.getPositionsOfResponses();
	}
	
	public void setConfig(QTIExportItemFormatConfig formatConfig){
		this.config = formatConfig;
	}
	
	public QTIExportItemFormatConfig getConfig(){
		return this.config;
	}
}
