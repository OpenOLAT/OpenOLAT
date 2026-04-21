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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabImpl;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;

/**
 * Unit tests for the filterStatus method and filterTab integration
 * in CurriculumElementWithViewsDataModel.
 *
 * The rule: a CurriculumElement's effective status for tab filtering is
 * determined by the statuses of its descendant RepositoryEntries. CE rows
 * that have children are excluded from direct filtering and re-included
 * only when a descendant matches (via reconstructParentLine).
 *
 * Initial date: 20.04.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementWithViewsDataModelFilterStatusTest {

	private static final AtomicLong KEY_COUNTER = new AtomicLong(1L);

	private CurriculumElementWithViewsDataModel createModel() {
		return new CurriculumElementWithViewsDataModel(null, Locale.ENGLISH);
	}

	private CurriculumElementImpl mockCE(CurriculumElementStatus status) {
		return mockCE(status, null);
	}

	private CurriculumElementImpl mockCE(CurriculumElementStatus status, CurriculumElementImpl parent) {
		CurriculumElementImpl ce = new CurriculumElementImpl();
		ce.setKey(KEY_COUNTER.getAndIncrement());
		ce.setElementStatus(status);
		ce.setParent(parent);
		return ce;
	}

	private RepositoryEntryMyView mockRE(RepositoryEntryStatusEnum status) {
		return new TestRepositoryEntryView(KEY_COUNTER.getAndIncrement(), status);
	}

	private CurriculumElementWithViewsRow ceCombinedRow(CurriculumElementImpl ce, RepositoryEntryMyView re) {
		return new CurriculumElementWithViewsRow(ce, null, re, true);
	}

	private CurriculumElementWithViewsRow ceOnlyRow(CurriculumElementImpl ce, int entryCount) {
		return new CurriculumElementWithViewsRow(ce, null, entryCount);
	}

	private CurriculumElementWithViewsRow reChildRow(CurriculumElementImpl ce, RepositoryEntryMyView re) {
		return new CurriculumElementWithViewsRow(ce, null, re, false);
	}

	private FlexiFiltersTabImpl tab(String id) {
		return new FlexiFiltersTabImpl(id, id);
	}


	// --- filterStatus unit tests ---

	@Test
	public void filterStatus_emptyStatusList_returnsTrue() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = ceOnlyRow(mockCE(CurriculumElementStatus.preparation), 0);

		boolean result = model.filterStatus(row, List.of(), List.of());

		Assertions.assertThat(result).isTrue();
	}

	@Test
	public void filterStatus_reRow_publishedInActiveEntryStatus_returnsTrue() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = reChildRow(mockCE(CurriculumElementStatus.preparation), mockRE(RepositoryEntryStatusEnum.published));

		boolean result = model.filterStatus(row,
				CurriculumElementListController.ACTIVE_STATUS,
				CurriculumElementListController.ACTIVE_ENTRY_STATUS);

		Assertions.assertThat(result).isTrue();
	}

	@Test
	public void filterStatus_reRow_publishedNotInPreparationEntryStatus_returnsFalse() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = reChildRow(mockCE(CurriculumElementStatus.preparation), mockRE(RepositoryEntryStatusEnum.published));

		boolean result = model.filterStatus(row,
				CurriculumElementListController.PREPARATION_STATUS,
				CurriculumElementListController.PREPARATION_ENTRY_STATUS);

		Assertions.assertThat(result).isFalse();
	}

	@Test
	public void filterStatus_reRow_preparationInPreparationEntryStatus_returnsTrue() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = reChildRow(mockCE(CurriculumElementStatus.active), mockRE(RepositoryEntryStatusEnum.preparation));

		boolean result = model.filterStatus(row,
				CurriculumElementListController.PREPARATION_STATUS,
				CurriculumElementListController.PREPARATION_ENTRY_STATUS);

		Assertions.assertThat(result).isTrue();
	}

	@Test
	public void filterStatus_reRow_closedInFinishedEntryStatus_returnsTrue() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = reChildRow(mockCE(CurriculumElementStatus.active), mockRE(RepositoryEntryStatusEnum.closed));

		boolean result = model.filterStatus(row,
				CurriculumElementListController.FINISHED_STATUS,
				CurriculumElementListController.FINISHED_ENTRY_STATUS);

		Assertions.assertThat(result).isTrue();
	}

	@Test
	public void filterStatus_ceOnlyWithChildren_returnsFalse() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(mockCE(CurriculumElementStatus.active), 0);
		CurriculumElementWithViewsRow childRow = ceOnlyRow(mockCE(CurriculumElementStatus.preparation), 0);
		childRow.setParent(ceRow);

		boolean result = model.filterStatus(ceRow,
				CurriculumElementListController.ACTIVE_STATUS,
				CurriculumElementListController.ACTIVE_ENTRY_STATUS);

		Assertions.assertThat(result).isFalse();
	}

	@Test
	public void filterStatus_ceOnlyWithChildren_member_statusMatches_returnsTrue() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(mockCE(CurriculumElementStatus.active), 0);
		ceRow.setCurriculumMember(true);
		CurriculumElementWithViewsRow childRow = ceOnlyRow(mockCE(CurriculumElementStatus.preparation), 0);
		childRow.setParent(ceRow);

		boolean result = model.filterStatus(ceRow,
				CurriculumElementListController.ACTIVE_STATUS,
				CurriculumElementListController.ACTIVE_ENTRY_STATUS);

		Assertions.assertThat(result).isTrue();
	}

	@Test
	public void filterStatus_ceOnlyWithChildren_member_statusNotMatches_returnsFalse() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(mockCE(CurriculumElementStatus.preparation), 0);
		ceRow.setCurriculumMember(true);
		CurriculumElementWithViewsRow childRow = ceOnlyRow(mockCE(CurriculumElementStatus.active), 0);
		childRow.setParent(ceRow);

		boolean result = model.filterStatus(ceRow,
				CurriculumElementListController.ACTIVE_STATUS,
				CurriculumElementListController.ACTIVE_ENTRY_STATUS);

		Assertions.assertThat(result).isFalse();
	}

	@Test
	public void filterStatus_ceOnlyLeaf_statusMatchesActiveTab_returnsTrue() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = ceOnlyRow(mockCE(CurriculumElementStatus.active), 0);

		boolean result = model.filterStatus(row,
				CurriculumElementListController.ACTIVE_STATUS,
				CurriculumElementListController.ACTIVE_ENTRY_STATUS);

		Assertions.assertThat(result).isTrue();
	}

	@Test
	public void filterStatus_ceOnlyLeaf_statusNotMatchingActiveTab_returnsFalse() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = ceOnlyRow(mockCE(CurriculumElementStatus.preparation), 0);

		boolean result = model.filterStatus(row,
				CurriculumElementListController.ACTIVE_STATUS,
				CurriculumElementListController.ACTIVE_ENTRY_STATUS);

		Assertions.assertThat(result).isFalse();
	}

	@Test
	public void filterStatus_ceOnlyLeaf_preparationStatusMatchesPreparationTab_returnsTrue() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow row = ceOnlyRow(mockCE(CurriculumElementStatus.preparation), 0);

		boolean result = model.filterStatus(row,
				CurriculumElementListController.PREPARATION_STATUS,
				CurriculumElementListController.PREPARATION_ENTRY_STATUS);

		Assertions.assertThat(result).isTrue();
	}


	// --- filterTab integration tests ---

	@Test
	public void filterTab_cePrepSingleRePublished_appearsInActiveTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow ceRow = ceCombinedRow(mockCE(CurriculumElementStatus.preparation), mockRE(RepositoryEntryStatusEnum.published));
		model.setObjects(List.of(ceRow));

		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));

		Assertions.assertThat(model.getObjects()).contains(ceRow);
	}

	@Test
	public void filterTab_cePrepSingleRePublished_notInPreparationTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow ceRow = ceCombinedRow(mockCE(CurriculumElementStatus.preparation), mockRE(RepositoryEntryStatusEnum.published));
		model.setObjects(List.of(ceRow));

		model.filterTab(null, tab(CurriculumElementListController.PREPARATION_TAB));

		Assertions.assertThat(model.getObjects()).isEmpty();
	}

	@Test
	public void filterTab_ceActiveSingleRePreparation_appearsInPreparationTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow ceRow = ceCombinedRow(mockCE(CurriculumElementStatus.active), mockRE(RepositoryEntryStatusEnum.preparation));
		model.setObjects(List.of(ceRow));

		model.filterTab(null, tab(CurriculumElementListController.PREPARATION_TAB));

		Assertions.assertThat(model.getObjects()).contains(ceRow);
	}

	@Test
	public void filterTab_ceActiveSingleRePreparation_notInActiveTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementWithViewsRow ceRow = ceCombinedRow(mockCE(CurriculumElementStatus.active), mockRE(RepositoryEntryStatusEnum.preparation));
		model.setObjects(List.of(ceRow));

		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));

		Assertions.assertThat(model.getObjects()).isEmpty();
	}

	@Test
	public void filterTab_cePrepTwoRePublished_appearsInActiveTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementImpl ce = mockCE(CurriculumElementStatus.preparation);
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(ce, 2);
		CurriculumElementWithViewsRow re1Row = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.published));
		CurriculumElementWithViewsRow re2Row = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.published));
		re1Row.setParent(ceRow);
		re2Row.setParent(ceRow);
		model.setObjects(List.of(ceRow, re1Row, re2Row));

		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));

		List<CurriculumElementWithViewsRow> result = model.getObjects();
		Assertions.assertThat(result).contains(ceRow);
		Assertions.assertThat(result).contains(re1Row);
		Assertions.assertThat(result).contains(re2Row);
	}

	@Test
	public void filterTab_cePrepTwoRePublished_notInPreparationTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementImpl ce = mockCE(CurriculumElementStatus.preparation);
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(ce, 2);
		CurriculumElementWithViewsRow re1Row = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.published));
		CurriculumElementWithViewsRow re2Row = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.published));
		re1Row.setParent(ceRow);
		re2Row.setParent(ceRow);
		model.setObjects(List.of(ceRow, re1Row, re2Row));

		model.filterTab(null, tab(CurriculumElementListController.PREPARATION_TAB));

		Assertions.assertThat(model.getObjects()).isEmpty();
	}

	@Test
	public void filterTab_ceActiveRePreparationAndPublished_appearsInBothTabs() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementImpl ce = mockCE(CurriculumElementStatus.active);
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(ce, 2);
		CurriculumElementWithViewsRow rePrep = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.preparation));
		CurriculumElementWithViewsRow rePub = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.published));
		rePrep.setParent(ceRow);
		rePub.setParent(ceRow);
		model.setObjects(List.of(ceRow, rePrep, rePub));

		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));
		Assertions.assertThat(model.getObjects()).contains(ceRow);

		model.setObjects(List.of(ceRow, rePrep, rePub));
		model.filterTab(null, tab(CurriculumElementListController.PREPARATION_TAB));
		Assertions.assertThat(model.getObjects()).contains(ceRow);
	}

	@Test
	public void filterTab_cePrepRePreparationAndPublished_appearsInBothTabs() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementImpl ce = mockCE(CurriculumElementStatus.preparation);
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(ce, 2);
		CurriculumElementWithViewsRow rePrep = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.preparation));
		CurriculumElementWithViewsRow rePub = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.published));
		rePrep.setParent(ceRow);
		rePub.setParent(ceRow);
		model.setObjects(List.of(ceRow, rePrep, rePub));

		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));
		Assertions.assertThat(model.getObjects()).contains(ceRow);

		model.setObjects(List.of(ceRow, rePrep, rePub));
		model.filterTab(null, tab(CurriculumElementListController.PREPARATION_TAB));
		Assertions.assertThat(model.getObjects()).contains(ceRow);
	}

	@Test
	public void filterTab_ceActiveReClosed_appearsInFinishedTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementImpl ce = mockCE(CurriculumElementStatus.active);
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(ce, 1);
		CurriculumElementWithViewsRow reRow = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.closed));
		reRow.setParent(ceRow);
		model.setObjects(List.of(ceRow, reRow));

		model.filterTab(null, tab(CurriculumElementListController.FINISHED_TAB));

		Assertions.assertThat(model.getObjects()).contains(ceRow);
	}

	@Test
	public void filterTab_ceActiveReClosed_notInActiveTab() {
		CurriculumElementWithViewsDataModel model = createModel();
		CurriculumElementImpl ce = mockCE(CurriculumElementStatus.active);
		CurriculumElementWithViewsRow ceRow = ceOnlyRow(ce, 1);
		CurriculumElementWithViewsRow reRow = reChildRow(ce, mockRE(RepositoryEntryStatusEnum.closed));
		reRow.setParent(ceRow);
		model.setObjects(List.of(ceRow, reRow));

		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));

		Assertions.assertThat(model.getObjects()).doesNotContain(ceRow);
	}

	@Test
	public void filterTab_ceWithBothSubCeAndDirectRe_reIsTakenIntoAccount() {
		CurriculumElementWithViewsDataModel model = createModel();

		CurriculumElementImpl ceTop = mockCE(CurriculumElementStatus.active);
		CurriculumElementImpl ceBug = mockCE(CurriculumElementStatus.active, ceTop);
		CurriculumElementImpl ceSubPrep = mockCE(CurriculumElementStatus.preparation, ceBug);
		CurriculumElementImpl ceOtherPrep = mockCE(CurriculumElementStatus.preparation, ceTop);
		CurriculumElementImpl ceOtherInner = mockCE(CurriculumElementStatus.preparation, ceOtherPrep);

		CurriculumElementWithViewsRow ceTopRow = ceOnlyRow(ceTop, 0);
		CurriculumElementWithViewsRow ceBugRow = ceOnlyRow(ceBug, 1);
		CurriculumElementWithViewsRow ceSubPrepRow = ceCombinedRow(ceSubPrep, mockRE(RepositoryEntryStatusEnum.preparation));
		CurriculumElementWithViewsRow reActiveRow = reChildRow(ceBug, mockRE(RepositoryEntryStatusEnum.published));
		CurriculumElementWithViewsRow ceOtherPrepRow = ceOnlyRow(ceOtherPrep, 0);
		CurriculumElementWithViewsRow ceOtherInnerRow = ceCombinedRow(ceOtherInner, mockRE(RepositoryEntryStatusEnum.published));

		ceBugRow.setParent(ceTopRow);
		ceSubPrepRow.setParent(ceBugRow);
		reActiveRow.setParent(ceBugRow);
		ceOtherPrepRow.setParent(ceTopRow);
		ceOtherInnerRow.setParent(ceOtherPrepRow);

		List<CurriculumElementWithViewsRow> allRows = List.of(
				ceTopRow, ceBugRow, ceSubPrepRow, reActiveRow, ceOtherPrepRow, ceOtherInnerRow);

		model.setObjects(allRows);
		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));
		Assertions.assertThat(model.getObjects()).contains(ceBugRow);
	}

	@Test
	public void filterTab_multilevel_allPublishedRe_allCeInActiveTabOnly() {
		CurriculumElementWithViewsDataModel model = createModel();

		CurriculumElementImpl ce1 = mockCE(CurriculumElementStatus.active);
		CurriculumElementImpl ce2 = mockCE(CurriculumElementStatus.preparation, ce1);
		CurriculumElementImpl inner1 = mockCE(CurriculumElementStatus.preparation, ce2);
		CurriculumElementImpl inner2 = mockCE(CurriculumElementStatus.active, ce2);
		CurriculumElementImpl inner3 = mockCE(CurriculumElementStatus.preparation, ce2);
		CurriculumElementImpl inner4 = mockCE(CurriculumElementStatus.cancelled, ce2);
		CurriculumElementImpl inner5 = mockCE(CurriculumElementStatus.finished, ce2);

		CurriculumElementWithViewsRow ce1Row = ceOnlyRow(ce1, 0);
		CurriculumElementWithViewsRow ce2Row = ceOnlyRow(ce2, 0);
		CurriculumElementWithViewsRow inner1Row = ceCombinedRow(inner1, mockRE(RepositoryEntryStatusEnum.published));
		CurriculumElementWithViewsRow inner2Row = ceCombinedRow(inner2, mockRE(RepositoryEntryStatusEnum.published));
		CurriculumElementWithViewsRow inner3Row = ceCombinedRow(inner3, mockRE(RepositoryEntryStatusEnum.published));
		CurriculumElementWithViewsRow inner4Row = ceCombinedRow(inner4, mockRE(RepositoryEntryStatusEnum.published));
		CurriculumElementWithViewsRow inner5Row = ceCombinedRow(inner5, mockRE(RepositoryEntryStatusEnum.published));

		ce2Row.setParent(ce1Row);
		inner1Row.setParent(ce2Row);
		inner2Row.setParent(ce2Row);
		inner3Row.setParent(ce2Row);
		inner4Row.setParent(ce2Row);
		inner5Row.setParent(ce2Row);

		List<CurriculumElementWithViewsRow> allRows = List.of(
				ce1Row, ce2Row, inner1Row, inner2Row, inner3Row, inner4Row, inner5Row);

		model.setObjects(allRows);
		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));
		Assertions.assertThat(model.getObjects()).contains(ce1Row, ce2Row, inner1Row, inner2Row, inner3Row, inner4Row, inner5Row);

		model.setObjects(allRows);
		model.filterTab(null, tab(CurriculumElementListController.PREPARATION_TAB));
		Assertions.assertThat(model.getObjects()).isEmpty();
	}


	@Test
	public void filterTab_ce1ActiveNotMember_ce2ActiveMember_ce3PrepMember_ce1AndCe2InActiveTab() {
		CurriculumElementWithViewsDataModel model = createModel();

		CurriculumElementImpl ce1 = mockCE(CurriculumElementStatus.active);
		CurriculumElementImpl ce2 = mockCE(CurriculumElementStatus.active, ce1);
		CurriculumElementImpl ce3 = mockCE(CurriculumElementStatus.preparation, ce2);

		CurriculumElementWithViewsRow ce1Row = ceOnlyRow(ce1, 0);
		CurriculumElementWithViewsRow ce2Row = ceOnlyRow(ce2, 0);
		ce2Row.setCurriculumMember(true);
		CurriculumElementWithViewsRow ce3Row = ceOnlyRow(ce3, 0);
		ce3Row.setCurriculumMember(true);

		ce2Row.setParent(ce1Row);
		ce3Row.setParent(ce2Row);

		model.setObjects(List.of(ce1Row, ce2Row, ce3Row));
		model.filterTab(null, tab(CurriculumElementListController.ACTIVE_TAB));

		List<CurriculumElementWithViewsRow> result = model.getObjects();
		Assertions.assertThat(result).contains(ce1Row, ce2Row);
		Assertions.assertThat(result).doesNotContain(ce3Row);
	}

	private static final class TestRepositoryEntryView implements RepositoryEntryMyView {

		private final Long key;
		private final RepositoryEntryStatusEnum entryStatus;

		private TestRepositoryEntryView(Long key, RepositoryEntryStatusEnum entryStatus) {
			this.key = key;
			this.entryStatus = entryStatus;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public RepositoryEntryStatusEnum getEntryStatus() {
			return entryStatus;
		}

		@Override
		public Long getResourceableId() {
			return key;
		}

		@Override
		public String getResourceableTypeName() {
			return "CourseModule";
		}

		@Override
		public String getExternalId() {
			return null;
		}

		@Override
		public String getExternalRef() {
			return null;
		}

		@Override
		public String getDisplayname() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public String getTeaser() {
			return null;
		}

		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public String getAuthors() {
			return null;
		}

		@Override
		public String getLocation() {
			return null;
		}

		@Override
		public String getTechnicalType() {
			return null;
		}

		@Override
		public RepositoryEntryEducationalType getEducationalType() {
			return null;
		}

		@Override
		public String getExpenditureOfWork() {
			return null;
		}

		@Override
		public boolean isPublicVisible() {
			return false;
		}

		@Override
		public OLATResource getOlatResource() {
			return null;
		}

		@Override
		public RepositoryEntryLifecycle getLifecycle() {
			return null;
		}

		@Override
		public Boolean getPassed() {
			return null;
		}

		@Override
		public Float getScore() {
			return null;
		}

		@Override
		public Double getCompletion() {
			return null;
		}

		@Override
		public boolean isMarked() {
			return false;
		}

		@Override
		public Double getAverageRating() {
			return null;
		}

		@Override
		public long getNumOfRatings() {
			return 0;
		}

		@Override
		public long getLaunchCounter() {
			return 0;
		}

		@Override
		public boolean isValidOfferAvailable() {
			return false;
		}

		@Override
		public long getNumOfTaxonomyLevels() {
			return 0;
		}
	}
}
