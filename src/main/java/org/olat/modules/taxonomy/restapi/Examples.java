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
package org.olat.modules.taxonomy.restapi;

/**
 * 
 * Initial date: 6 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Examples {
	
	public static final TaxonomyVO SAMPLE_TAXONOMYVO = new TaxonomyVO();
	public static final TaxonomyLevelVO SAMPLE_TAXONOMYLEVELVO = new TaxonomyLevelVO();
	public static final TaxonomyLevelTypeVO SAMPLE_TAXONOMYLEVELTYPEVO = new TaxonomyLevelTypeVO();
	public static final TaxonomyCompetenceVO SAMPLE_TAXONOMYCOMPETENCEVO = new TaxonomyCompetenceVO();
	
	static {
		SAMPLE_TAXONOMYVO.setKey(1l);
		SAMPLE_TAXONOMYVO.setIdentifier("ID-Taxonomy");
		SAMPLE_TAXONOMYVO.setDisplayName("Taxonomy");
		SAMPLE_TAXONOMYVO.setDescription("A taxonomy");
		SAMPLE_TAXONOMYVO.setExternalId("EXT-ID-Taxonomy");
		
		SAMPLE_TAXONOMYLEVELVO.setKey(2l);
		SAMPLE_TAXONOMYLEVELVO.setIdentifier("ID-Level-Taxonomy");
		SAMPLE_TAXONOMYLEVELVO.setDisplayName("A taxonomy level");
		SAMPLE_TAXONOMYLEVELVO.setDescription("A taxonomy level with a parent");
		SAMPLE_TAXONOMYLEVELVO.setExternalId("EXT-ID-Level-Taxonomy");
		SAMPLE_TAXONOMYLEVELVO.setParentKey(300l);
		SAMPLE_TAXONOMYLEVELVO.setTypeKey(301l);
		
		SAMPLE_TAXONOMYLEVELTYPEVO.setKey(3l);
		SAMPLE_TAXONOMYLEVELTYPEVO.setIdentifier("ID-Taxonomy-Level-Type");
		SAMPLE_TAXONOMYLEVELTYPEVO.setDisplayName("Taxonomy level type");
		SAMPLE_TAXONOMYLEVELTYPEVO.setDescription("Settings for a taxonomy level");
		SAMPLE_TAXONOMYLEVELTYPEVO.setExternalId("EXT-ID-Taxonomy-Level-Type");
		
		SAMPLE_TAXONOMYCOMPETENCEVO.setKey(4l);
		SAMPLE_TAXONOMYCOMPETENCEVO.setIdentityKey(400l);
		SAMPLE_TAXONOMYCOMPETENCEVO.setTaxonomyLevelKey(2l);
		SAMPLE_TAXONOMYCOMPETENCEVO.setTaxonomyCompetenceType("teach");
	}

}
