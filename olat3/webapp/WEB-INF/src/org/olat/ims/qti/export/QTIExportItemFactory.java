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

package org.olat.ims.qti.export;

import java.util.Map;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.export.helper.QTIItemObject;

/**
 * Initial Date: May 31, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class QTIExportItemFactory {
	private Map configs;
	
	public QTIExportItemFactory(Map mapWithConfigs){
		this.configs = mapWithConfigs;
	}
	
	public QTIExportItem getExportItem(QTIResult qtir, QTIItemObject item){
		QTIExportItem eItem = null;
		if(item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)){
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportSCQItemFormatConfig) configs.get(QTIExportSCQItemFormatConfig.class));	
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)){ // Checkbox
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportMCQItemFormatConfig) configs.get(QTIExportMCQItemFormatConfig.class));
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)){ // Kprim
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportKPRIMItemFormatConfig) configs.get(QTIExportKPRIMItemFormatConfig.class));
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)){ // Area
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportEssayItemFormatConfig) configs.get(QTIExportEssayItemFormatConfig.class));
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)){ // Blank
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportFIBItemFormatConfig) configs.get(QTIExportFIBItemFormatConfig.class));
		}
    //	if cannot find the type via the ItemParser, look for the QTIItemObject type
		else if (item.getItemType().equals(QTIItemObject.TYPE.A)){ 
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportEssayItemFormatConfig) configs.get(QTIExportEssayItemFormatConfig.class));
		}
		else if (item.getItemType().equals(QTIItemObject.TYPE.R)){ 
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportSCQItemFormatConfig) configs.get(QTIExportSCQItemFormatConfig.class));
		}
		else if (item.getItemType().equals(QTIItemObject.TYPE.C)){
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportMCQItemFormatConfig) configs.get(QTIExportMCQItemFormatConfig.class));
		}
		else if (item.getItemType().equals(QTIItemObject.TYPE.B)){
			eItem = new QTIExportItem(qtir, item);
			eItem.setConfig((QTIExportFIBItemFormatConfig) configs.get(QTIExportFIBItemFormatConfig.class));
		}			
		else{
			throw new OLATRuntimeException(null,"Can not resolve QTIItem type", null);
		}
		return eItem;
	}
	
	public QTIExportItemFormatConfig getExportItemConfig(QTIItemObject item){
		QTIExportItemFormatConfig config = null;
		if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)){ // Radio
			config = (QTIExportSCQItemFormatConfig) configs.get(QTIExportSCQItemFormatConfig.class);
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)){ // Checkbox
			config = (QTIExportMCQItemFormatConfig) configs.get(QTIExportMCQItemFormatConfig.class);
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)){ // KPRIM
			config = (QTIExportKPRIMItemFormatConfig) configs.get(QTIExportKPRIMItemFormatConfig.class);
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)){ // Area
			config = (QTIExportEssayItemFormatConfig) configs.get(QTIExportEssayItemFormatConfig.class);
		}
		else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)){ // Blank
			config = (QTIExportFIBItemFormatConfig) configs.get(QTIExportFIBItemFormatConfig.class);
		}
    //	if cannot find the type via the ItemParser, look for the QTIItemObject type
		else if (item.getItemType().equals(QTIItemObject.TYPE.A)){
			config = (QTIExportEssayItemFormatConfig) configs.get(QTIExportEssayItemFormatConfig.class);
		}
		else if (item.getItemType().equals(QTIItemObject.TYPE.R)){
			config = (QTIExportSCQItemFormatConfig) configs.get(QTIExportSCQItemFormatConfig.class);
		}
		else if (item.getItemType().equals(QTIItemObject.TYPE.C)){
			config = (QTIExportMCQItemFormatConfig) configs.get(QTIExportMCQItemFormatConfig.class);
		}
		else if (item.getItemType().equals(QTIItemObject.TYPE.B)){
			config = (QTIExportFIBItemFormatConfig) configs.get(QTIExportFIBItemFormatConfig.class);
		}		
		else{
			throw new OLATRuntimeException(null,"Can not resolve QTIItem type", null);
		}
		return config;
	}
	
}
