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

package org.olat.ims.qti.export.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.Resolver;
import org.olat.repository.RepositoryEntry;

/**
 * <pre>
 * Description:
 * 
 * method getCSV()
 * ===============
 * example with one item for type 1, 2 and 3:
 * 
 * iqtest (type1):
 * +----------------------------------+---------------------------------+
 * |HeaderRow1Intro                   |HeaderRow1                       |
 * |                                  |itemTitle                        |
 * +----------------------------------+---------------------------------+
 * |HeaderRow2Intro                   |HeaderRow2                       |
 * |# | Name, etc. |starttime|duration|1|...|n|points|starttime|duration|
 * +--+------------+---------+--------+-+---+-+------+---------+--------+
 * |  |            |         |        | |   | |      |         |        |
 * |          proband data            |            results              |
 * ...
 * 
 * iqself (type2):
 * +----------------------------------+---------------------------------+
 * |HeaderRow1Intro                   |HeaderRow1                       |
 * |                                  |itemTitle                        |
 * +----------------------------------+---------------------------------+
 * |HeaderRow2Intro                   |HeaderRow2                       |
 * |# |covernumber |starttime|duration|1|...|n|points|starttime|duration|
 * +--+------------+---------+--------+-+---+-+------+---------+--------+
 * |  |            |         |        | |   | |      |         |        |
 * |      proband data (anonym)       |            results              |
 * ...
 * 
 * iqsurv (type3):
 * +-----------------+-----------+
 * |HeaderRow1Intro  |HeaderRow1 |
 * |                 |itemTitle  | 
 * +-----------------+-----------+
 * |HeaderRow2Intro  |HeaderRow  |
 * |# |  starttime   | 1 |...| n |
 * +--+--------------+---+---+---+
 * |  |              |   |   |   |
 * |     datetime    |  results  |
 * ...
 * </pre>
 * 
 * @author Mike Stock, Alexander Schneider
 */

public class QTIObjectTreeBuilder {

	public List<QTIItemObject> getQTIItemObjectList(Long entryKey) {
		Resolver resolver = new ImsRepositoryResolver(entryKey);
		return getQTIItemObjectList(resolver);
	}
	
	public List<QTIItemObject> getQTIItemObjectList(RepositoryEntry entry) {
		Resolver resolver = new ImsRepositoryResolver(entry);
		return getQTIItemObjectList(resolver);
	}
	
	public final List<QTIItemObject> getQTIItemObjectList(Resolver resolver) {
		Document doc = resolver.getQTIDocument();
		Element root = doc.getRootElement();
		List<Node> items = root.selectNodes("//item");
		List<QTIItemObject> itemList = new ArrayList<>();
		for (Iterator<Node> iter= items.iterator(); iter.hasNext();) {
			Element el_item = (Element)iter.next();
			if (!el_item.selectNodes(".//response_lid").isEmpty()){
				itemList.add(new ItemWithResponseLid(el_item));
			} else if (!el_item.selectNodes(".//response_str").isEmpty()){
				itemList.add(new ItemWithResponseStr(el_item));
			}
		}
		return itemList;
	}
}



