/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality;

import org.olat.modules.quality.generator.QualityGeneratorRef;

/**
 * 
 * Initial date: 30.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityReportAccessReference {
	
	private final QualityDataCollectionRef dataCollectionRef;
	private final QualityGeneratorRef generatorRef;
	
	public final static QualityReportAccessReference of(QualityDataCollectionRef dataCollectionRef) {
		return new QualityReportAccessReference(dataCollectionRef, null);
	}

	public final static QualityReportAccessReference of(QualityGeneratorRef generatorRef) {
		return new QualityReportAccessReference(null, generatorRef);
	}

	private QualityReportAccessReference(QualityDataCollectionRef dataCollectionRef, QualityGeneratorRef generatorRef) {
		this.dataCollectionRef = dataCollectionRef;
		this.generatorRef = generatorRef;
	}

	public QualityDataCollectionRef getDataCollectionRef() {
		return dataCollectionRef;
	}

	public QualityGeneratorRef getGeneratorRef() {
		return generatorRef;
	}
	
	public boolean isDataCollectionRef() {
		return dataCollectionRef != null;
	}

}
