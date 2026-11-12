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

package org.olat.gui.demo.guidemo;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.Marker;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;
import org.olat.core.gui.components.progressbar.ProgressBarCellRenderer;
import org.olat.core.gui.components.progressbar.ProgressBarFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Description:<br>
 * Shows usage of the progress bar component
 * <P>
 * Initial Date:  31 jan 2020<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 */
public class GuiDemoProgressBarController extends FormBasicController {

	public GuiDemoProgressBarController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "guidemo-progressBar");
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createProgressBar("base", 25, null);
		
		createProgressBar("format-none", 40, null).setLabelFormat(LabelFormat.none);
		createProgressBar("format-percent", 40, null).setLabelFormat(LabelFormat.percent);
		createProgressBar("format-value", 40, null).setLabelFormat(LabelFormat.value);
		createProgressBar("format-both", 40, null).setLabelFormat(LabelFormat.percentAndValue);
		ProgressBar unitBar = createProgressBar("format-unit", 40, null);
		unitBar.setMax(125);
		unitBar.setUnitLabel("MB");
		unitBar.setInfo("Disk usage");
		unitBar.setLabelFormat(LabelFormat.percentAndValue);
		ProgressBar noMaxBar = createProgressBar("format-nomax", 40, null);
		noMaxBar.setNoMax(true);
		noMaxBar.setUnitLabel("MB");
		noMaxBar.setLabelFormat(LabelFormat.value);
		
		for (RenderSize size : RenderSize.values()) {
			for (RenderStyle style : RenderStyle.values()) {
				ProgressBar progressBar = createProgressBar(style.name() + "-" + size.name(), 65, null);
				progressBar.setRenderStyle(style);
				progressBar.setRenderSize(size);
				progressBar.setLabelFormat(LabelFormat.percent);
			}
		}
		
		for (BarColor color : BarColor.values()) {
			createProgressBar("hor-" + color.name(), 65, color);
			ProgressBar radialBar = createProgressBar("rad-" + color.name(), 65, color);
			radialBar.setRenderStyle(RenderStyle.radial);
			radialBar.setRenderSize(RenderSize.small);
		}
		flc.contextPut("colors", BarColor.values());
		
		ProgressBar stackedBar = createProgressBar("stacked", 0, null);
		stackedBar.setMax(10);
		stackedBar.setAriaLabel("Members");
		stackedBar.setLabelFormat(LabelFormat.value);
		stackedBar.setSegments(List.of(
				new Segment(6, BarColor.success, "active"),
				new Segment(2, BarColor.warning, "pending")));
		
		ProgressBar markersBar = createProgressBar("markers", 0, null);
		markersBar.setMax(20);
		markersBar.setAriaLabel("Members");
		markersBar.setLabelFormat(LabelFormat.value);
		markersBar.setSegments(List.of(
				new Segment(6, BarColor.success, "active"),
				new Segment(2, BarColor.warning, "pending")));
		markersBar.setMarkers(List.of(new Marker(10, BarColor.neutral, "minimum"), new Marker(16, BarColor.danger, "maximum")));
		
		ProgressBar fullBar = createProgressBar("markers-full", 0, null);
		fullBar.setMax(10);
		fullBar.setAriaLabel("Members");
		fullBar.setRenderSize(RenderSize.small);
		fullBar.setSegments(List.of(
				new Segment(7, BarColor.success, "active"),
				new Segment(3, BarColor.warning, "pending")));
		fullBar.setMarkers(List.of(new Marker(5, BarColor.neutral, "minimum")));
		
		createRadialMarkersBar("markers-radial", RenderStyle.radial, RenderSize.medium);
		createRadialMarkersBar("markers-pie", RenderStyle.pie, RenderSize.medium);
		createRadialMarkersBar("markers-radial-small", RenderStyle.radial, RenderSize.small);
		createRadialMarkersBar("markers-radial-inline", RenderStyle.radial, RenderSize.inline);
		createRadialSegmentsBar("segments-radial", RenderStyle.radial, RenderSize.medium);
		createRadialSegmentsBar("segments-pie", RenderStyle.pie, RenderSize.medium);
		createRadialSegmentsBar("segments-radial-small", RenderStyle.radial, RenderSize.small);
		createRadialSegmentsBar("segments-radial-inline", RenderStyle.radial, RenderSize.inline);
		
		createProgressBar("aria-label", 40, null).setAriaLabel("Course progress");
		ProgressBar ariaHiddenBar = createProgressBar("aria-hidden", 40, null);
		ariaHiddenBar.setAriaHidden(true);
		
		createProgressBar("hor-anim", 65, null).setProgressAnimationEnabled(true);
		ProgressBar radialAnimBar = createProgressBar("rad-anim", 65, null);
		radialAnimBar.setRenderStyle(RenderStyle.radial);
		radialAnimBar.setRenderSize(RenderSize.small);
		radialAnimBar.setProgressAnimationEnabled(true);
		
		initTable(formLayout);
	}
	
	private void initTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("progress.table.col.name", 0));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("progress.table.col.completion", 1,
				new ProgressBarCellRenderer(RenderStyle.horizontal, RenderSize.small, LabelFormat.percent, BarColor.neutral)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("progress.table.col.radial", 1,
				new ProgressBarCellRenderer(RenderStyle.radial, RenderSize.inline, LabelFormat.none, BarColor.success)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("progress.table.col.lectures", 2,
				new LecturesCellRenderer()));
		
		DemoTableModel tableModel = new DemoTableModel(columnsModel);
		tableModel.setObjects(List.of(
				new DemoRow("Ada", 0.25, 8, 1, 1),
				new DemoRow("Grace", 0.8, 5, 0, 5),
				new DemoRow("Linus", 1.0, 10, 0, 0)));
		uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
	}
	
	private ProgressBar createProgressBar(String name, float value, BarColor color) {
		ProgressBar progressBar = ProgressBarFactory.createProgressBar(name, getLocale());
		flc.put(name, progressBar);
		progressBar.setMax(100);
		ProgressBarFactory.setValue(progressBar, value, color == null ? BarColor.neutral : color);
		return progressBar;
	}
	
	private void createRadialMarkersBar(String name, RenderStyle style, RenderSize size) {
		ProgressBar progressBar = createProgressBar(name, 65, BarColor.success);
		progressBar.setRenderStyle(style);
		progressBar.setRenderSize(size);
		progressBar.setLabelFormat(LabelFormat.percent);
		progressBar.setMarkers(List.of(new Marker(50, BarColor.neutral, "minimum"), new Marker(80, BarColor.danger, "maximum")));
	}
	
	private void createRadialSegmentsBar(String name, RenderStyle style, RenderSize size) {
		ProgressBar progressBar = createProgressBar(name, 0, null);
		progressBar.setMax(20);
		progressBar.setAriaLabel("Members");
		progressBar.setRenderStyle(style);
		progressBar.setRenderSize(size);
		progressBar.setLabelFormat(LabelFormat.value);
		progressBar.setSegments(List.of(
				new Segment(9, BarColor.success, "active"),
				new Segment(4, BarColor.warning, "pending"),
				new Segment(2, BarColor.danger, "cancelled")));
		progressBar.setMarkers(List.of(new Marker(10, BarColor.neutral, "minimum"), new Marker(16, BarColor.danger, "maximum")));
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to do
	}
	
	private static class LecturesCellRenderer extends ProgressBarCellRenderer {
	
		public LecturesCellRenderer() {
			super(RenderStyle.horizontal, RenderSize.small, LabelFormat.none, BarColor.neutral);
		}
		
		@Override
		protected ProgressBar createProgressBar(Object cellValue, Translator translator) {
			if (cellValue instanceof DemoRow row) {
				ProgressBar progressBar = createProgressBar(translator.getLocale());
				progressBar.setMax(row.attended() + row.absent() + row.excused());
				progressBar.setAriaLabel("Lectures");
				progressBar.setSegments(List.of(
						new Segment(row.attended(), BarColor.success, "attended"),
						new Segment(row.excused(), BarColor.info, "excused"),
						new Segment(row.absent(), BarColor.danger, "absent")));
				return progressBar;
			}
			return null;
		}
	}
	
	private static class DemoTableModel extends DefaultFlexiTableDataModel<DemoRow> {
	
		public DemoTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			DemoRow demoRow = getObject(row);
			return switch (col) {
				case 0 -> demoRow.name();
				case 1 -> Double.valueOf(demoRow.completion());
				default -> demoRow;
			};
		}
	}
	
	private record DemoRow(String name, double completion, int attended, int absent, int excused) {
		//
	}
}
