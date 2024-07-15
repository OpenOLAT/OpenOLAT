/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.project.model;

/**
 * 
 * Initial date: 11 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProjReferenceValues {
	
	private String artefactType;
	private String artefactName;
	private String artefactReferenceType;
	private String artefactReferenceName;
	
	
	
	public ProjReferenceValues(String artefactType, String artefactName, String artefactReferenceType,
			String artefactReferenceName) {
		this.artefactType = artefactType;
		this.artefactName = artefactName;
		this.artefactReferenceType = artefactReferenceType;
		this.artefactReferenceName = artefactReferenceName;
	}

	public String getArtefactType() {
		return artefactType;
	}
	
	public void setArtefactType(String artefactType) {
		this.artefactType = artefactType;
	}
	
	public String getArtefactName() {
		return artefactName;
	}
	
	public void setArtefactName(String artefactName) {
		this.artefactName = artefactName;
	}
	
	public String getArtefactReferenceType() {
		return artefactReferenceType;
	}
	
	public void setArtefactReferenceType(String artefactReferenceType) {
		this.artefactReferenceType = artefactReferenceType;
	}
	
	public String getArtefactReferenceName() {
		return artefactReferenceName;
	}
	
	public void setArtefactReferenceName(String artefactReferenceName) {
		this.artefactReferenceName = artefactReferenceName;
	}
	
}
