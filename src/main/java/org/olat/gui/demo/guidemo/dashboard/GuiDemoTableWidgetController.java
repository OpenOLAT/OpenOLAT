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
package org.olat.gui.demo.guidemo.dashboard;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.figurecards.FigureCardsFactory;
import org.olat.core.gui.components.figurecards.FigureCardsItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.DashboardUIFactory;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigProvider;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.gui.demo.guidemo.GuiDemoFlexiTablesController;
import org.olat.gui.demo.guidemo.dashboard.GuiDemoIndicatorsController.LabelFigure;

/**
 * 
 * Initial date: Oct 27, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoTableWidgetController extends TableWidgetController
		implements TableWidgetConfigProvider, FlexiTableComponentDelegate {
	
	private static final String CMD_INDICATOR = "indicator";
	private static final String[] ICONS = { null, "o_icon_user", "o_icon_pencil", "o_icon_objectives" };
	private static final String[] COLORS = { null, "o_figure_card_icon_blue", null, "o_figure_card_icon_red" };
	
	private SampleFlexiTableModel dataModel;
	private FlexiTableElement tableEl;
	private FormLink showAllLink;

	private final String title;
	private final boolean showHeader;
	private final boolean linkCells;
	private final boolean listView;
	private final String prefsId;
	private final int numFigures;
	private final SelectionValues figureValues;


	protected GuiDemoTableWidgetController(UserRequest ureq, WindowControl wControl, String title, boolean showHeader,
			boolean linkCells, boolean listView, String prefsId, int numFigures) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(GuiDemoFlexiTablesController.class, getLocale(), getTranslator()));
		this.title = title;
		this.showHeader = showHeader;
		this.linkCells = linkCells;
		this.listView = listView;
		this.prefsId = prefsId;
		this.numFigures = numFigures;
		
		figureValues = new SelectionValues();
		for (int i = 1; i <= numFigures; i++) {
			figureValues.add(SelectionValues.entry(String.valueOf(i), getFigureLabel(i)));
		}
		
		initForm(ureq);
		setFooterText(dataModel.getRowCount(), 195, getFigureLabel(1));
	}

	@Override
	protected String getTitle() {
		return title;
	}

	@Override
	protected String createTopArea(FormLayoutContainer widgetCont) {
		FigureCardsItem figureCardsItem = FigureCardsFactory.createItem("figures", widgetCont);
		
		List<FormLink> cards = IntStream.rangeClosed(1, numFigures)
				.mapToObj(i -> createCardLink(widgetCont, i))
				.toList();
		figureCardsItem.setCards(cards);
		
		return figureCardsItem.getComponent().getComponentName();
	}
	
	private String getFigureLabel(int i) {
		return i == 8? translate("select.long"): translate("select." + i);
	}

	private FormLink createCardLink(FormLayoutContainer widgetCont, int i) {
		String label = getFigureLabel(i);
		String figure = String.valueOf(i == 1? 195: i * 7);
		FormLink cardLink = FigureCardsFactory.createCardLink("figure_" + i, CMD_INDICATOR, widgetCont);
		cardLink.setI18nKey(FigureCardsFactory.createCardText(ICONS[(i - 1) % ICONS.length],
				COLORS[(i - 1) % COLORS.length], figure, label));
		cardLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
		cardLink.setUserObject(new LabelFigure(label, figure));
		return cardLink;
	}

	@Override
	protected String createTable(FormLayoutContainer widgetCont){
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		FlexiCellRenderer renderer = new TextFlexiCellRenderer();
		if (linkCells) {
			renderer = wrapCellLink(renderer);
		}
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header1", 0, renderer));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header2", 1, renderer));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header3", 2, renderer));
		
		dataModel = new SampleFlexiTableModel(tableColumnModel);
		List<Row> rows = IntStream.rangeClosed(1, 5)
				.boxed()
				.map(Row::new)
				.toList();
		dataModel.setObjects(rows);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 5, true, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		if (!showHeader) {
			tableEl.setCssDelegate(new HideHeaderDelegate());
		}
		if (listView) {
			tableEl.setRendererType(FlexiTableRendererType.custom);
			VelocityContainer rowVC = createVelocityContainer("table_row");
			rowVC.setDomReplacementWrapperRequired(false);
			tableEl.setRowRenderer(rowVC, this);
		}
		
		return tableEl.getComponent().getComponentName();
	}

	@Override
	public boolean isRowClickEnabled() {
		return true;
	}

	@Override
	public boolean isRowClickButton() {
		return true;
	}

	@Override
	protected String createShowAll(FormLayoutContainer widgetCont) {
		showAllLink = DashboardUIFactory.createShowAllLink(widgetCont);
		return showAllLink.getComponent().getComponentName();
	}

	@Override
	protected TableWidgetConfigProvider getConfigProvider() {
		return this;
	}

	@Override
	public String getId() {
		return prefsId;
	}

	@Override
	public SelectionValues getFigureValues() {
		return figureValues;
	}

	@Override
	public TableWidgetConfigPrefs getDefault() {
		TableWidgetConfigPrefs prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKey("1");
		prefs.setFocusFigureKeys(Set.of(figureValues.keys()));
		prefs.setNumRows(10);
		return prefs;
	}

	@Override
	public void update(TableWidgetConfigPrefs prefs) {
		// It's just a demo.
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				doShowRowClicked(se.getIndex());
			}
		} else if (source == showAllLink) {
			doShowAll();
		} else if (source instanceof FormLink link) {
			if (CMD_INDICATOR.equals(link.getCmd())) {
				if (link.getUserObject() instanceof LabelFigure labelFigure) {
					doShowIndicatorMessage(labelFigure);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doShowIndicatorMessage(LabelFigure labelFigure) {
		showInfo("show.label.value", new String[] { StringHelper.blankIfNull(labelFigure.label()), StringHelper.blankIfNull(labelFigure.figure()) } );
	}
	
	private void doShowRowClicked(int index) {
		Row row = dataModel.getObject(index);
		showInfo("table.row.clicked", new String[] { String.valueOf(index), row.getCol3()} );
	}
	
	private void doShowAll() {
		showInfo("show.all.message");
	}
	
	private class SampleFlexiTableModel extends DefaultFlexiTableDataModel<Row> {
		
		public SampleFlexiTableModel(FlexiTableColumnModel tableColumnModel) {
			super(tableColumnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			Row entry = getObject(row);
			return switch (col) {
			case 0 -> entry.getCol1();
			case 1 -> entry.getCol2();
			case 2 -> entry.getCol3();
			default -> null;
			};
		}
	}
	
	public class Row {
		
		private final String col1;
		private final String col2;
		private final String col3;
		
		public Row(int i) {
			col1 = String.valueOf(i);
			col2 = translate("select." + i);
			col3 = IntStream.rangeClosed(1, i).boxed().map(index -> col2).collect(Collectors.joining(" "));
		}
		
		public String getCol1() {
			return col1;
		}
		
		public String getCol2() {
			return col2;
		}

		public String getCol3() {
			return col3;
		}
	}
}
