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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.ui.TaxonomyLevelRelationsTableModel.RelationsCols;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelRelationTypeRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelRelationsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private TaxonomyLevelRelationsTableModel tableModel;
	
	private final TaxonomyLevelRef taxonomyLevel;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private QPoolService questionPoolService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	public TaxonomyLevelRelationsController(UserRequest ureq, WindowControl wControl, TaxonomyLevelRef taxonomyLevel) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.taxonomyLevel = taxonomyLevel;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RelationsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationsCols.type,
				new TaxonomyLevelRelationTypeRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationsCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationsCols.externalId));
	
		tableModel = new TaxonomyLevelRelationsTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("table.relations.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "tax-level-relations");
	}
	
	private void loadModel() {
		List<TaxonomyLevelRelationRow> rows = new ArrayList<>();
		List<QuestionItemShort> items = questionPoolService.getItems(taxonomyLevel);
		for(QuestionItemShort item:items) {
			rows.add(new TaxonomyLevelRelationRow(item));
		}
		List<RepositoryEntry> entries = repositoryService.getRepositoryEntryByTaxonomy(taxonomyLevel);
		for(RepositoryEntry entry:entries) {
			rows.add(new TaxonomyLevelRelationRow(entry));
		}
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(taxonomyLevel);
		for(CurriculumElement curriculumElement:curriculumElements) {
			rows.add(new TaxonomyLevelRelationRow(curriculumElement));
		}
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(taxonomyLevel);
		for(LectureBlock lectureBlock:lectureBlocks) {
			rows.add(new TaxonomyLevelRelationRow(lectureBlock));
		}
		
		List<QualityDataCollection> qualityDataCollections = qualityService.loadDataCollectionsByTaxonomyLevel(taxonomyLevel);
		for(QualityDataCollection qualityDataCollection:qualityDataCollections) {
			rows.add(new TaxonomyLevelRelationRow(qualityDataCollection));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
