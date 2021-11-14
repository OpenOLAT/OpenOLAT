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
package org.olat.modules.qpool.ui.metadata;

import java.util.List;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.rating.RatingCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.ui.QuestionsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RatingMetadataController extends FormBasicController {
	
	private RatingInfosDataModel ratingInfosModel;
	private FlexiTableElement ratingInfosTable;
	
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	public RatingMetadataController(UserRequest ureq, WindowControl wControl, QuestionItemShort item) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel ratingInfosColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		ratingInfosColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("rating.creation.date", 0));
		ratingInfosColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("rating", 1, new RatingCellRenderer(5)));
		ratingInfosModel = new RatingInfosDataModel(ratingInfosColumnsModel);
		ratingInfosTable = uifactory.addTableElement(getWindowControl(), "details_ratings", ratingInfosModel, getTranslator(), formLayout);
		ratingInfosTable.setCustomizeColumns(false);
		ratingInfosTable.setEmptyTableMessageKey("rating.empty.table");

	}
	
	public void setItem(QuestionItemShort item) {
		List<UserRating> allRatings = commentAndRatingService.getAllRatings(item, null);
		ratingInfosModel.setObjects(allRatings);
		ratingInfosTable.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private static class RatingInfosDataModel extends  DefaultFlexiTableDataModel<UserRating> {
		
		public RatingInfosDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public DefaultFlexiTableDataModel<UserRating> createCopyWithEmptyList() {
			return new RatingInfosDataModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			UserRating rating = getObject(row);
			switch(col) {
				case 0: return rating.getCreationDate();
				case 1: return rating;
				default: return "";
			}
		}
	}
}
