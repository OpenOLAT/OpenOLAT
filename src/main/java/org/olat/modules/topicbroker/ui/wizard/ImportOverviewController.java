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
package org.olat.modules.topicbroker.ui.wizard;


import static org.olat.modules.topicbroker.TopicBrokerExportService.EXPORT_TEASER_IMAGE;
import static org.olat.modules.topicbroker.TopicBrokerExportService.EXPORT_TEASER_VIDEO;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_IMAGE_MAX_SIZE_KB;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_IMAGE_MIME_TYPES;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_VIDEO_MAX_SIZE_KB;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_VIDEO_MIME_TYPES;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerExportService;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.manager.TopicBrokerMediaResource;
import org.olat.modules.topicbroker.model.TBImportTopic;
import org.olat.modules.topicbroker.model.TBTransientTopic;
import org.olat.modules.topicbroker.ui.TBTopicDataModel;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.modules.topicbroker.ui.wizard.ImportOverviewController.TBTopicImportDataModel.TopicImportCols;
import org.springframework.beans.factory.annotation.Autowired;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

/**
 * 
 * Initial date: 17 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImportOverviewController extends StepFormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ImportOverviewController.class);
	
	private TBTopicImportDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private final ImportContext importContext;
	private final List<TBCustomFieldDefinition> definitions;

	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public ImportOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(TBUIFactory.class, getLocale(), getTranslator()));
		
		importContext = (ImportContext)getFromRunContext("importContext");
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(importContext.getBroker());
		definitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams).stream()
				.filter(definition -> importContext.getTempFilesDir() != null? true: TBCustomFieldType.text == definition.getType())
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.toList();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("import.overview.title");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.description));
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(TopicImportCols.minParticipants);
		minParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		minParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(TopicImportCols.maxParticipants);
		maxParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		maxParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.groupRestrictions));
		
		if (importContext.getTempFilesDir() != null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.teaserImage));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.teaserVideo));
		}
		
		int columnIndex = TBTopicDataModel.CUSTOM_FIELD_OFFSET;
		for (TBCustomFieldDefinition customFieldDefinition : definitions) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(null, columnIndex++);
			columnModel.setHeaderLabel(StringHelper.escapeHtml(customFieldDefinition.getName()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		DefaultFlexiColumnModel messageColumn = new DefaultFlexiColumnModel(TopicImportCols.importMessage, new TextFlexiCellRenderer(EscapeMode.none));
		messageColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(messageColumn);
		
		dataModel = new TBTopicImportDataModel(columnsModel, definitions);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		
		
		List<TBImportTopic> topics = getTopics();
		appendFiles(topics);
		importContext.setTopics(topics);
		dataModel.setObjects(topics);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private List<TBImportTopic> getTopics() {
		List<String[]> lines = getLines(importContext.getInput());
		
		Set<String> identifiersInLines = new HashSet<>(lines.size());
		List<TBImportTopic> importTopics = lines.stream()
				.map(line -> toTopic(line, identifiersInLines))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		Set<Long> allGroupRestrictionKeys = importTopics.stream()
				.map(TBImportTopic::getTopic)
				.map(TBTopic::getGroupRestrictionKeys)
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
		Map<Long, String> groupKeyToName = businessGroupService
				.loadShortBusinessGroups(allGroupRestrictionKeys)
				.stream()
				.collect(Collectors.toMap(BusinessGroupShort::getKey, BusinessGroupShort::getName));
		
		for (TBImportTopic importTopic : importTopics) {
			Set<Long> restrictionKeys = importTopic.getTopic().getGroupRestrictionKeys();
			if (restrictionKeys != null && restrictionKeys.isEmpty()) {
				String groupNames = restrictionKeys.stream()
						.map(key -> groupKeyToName.get(key))
						.filter(Objects::nonNull)
						.sorted()
						.map(name -> "<i class=\"o_icon o o_icon-fw o_icon_group\"></i> " + StringHelper.escapeHtml(name))
						.collect(Collectors.joining("&nbsp;&nbsp;"));
				importTopic.setGroupRestrictions(groupNames);
			}
		}
		
		return importTopics;
	}

	private TBImportTopic toTopic(String[] line, Set<String> identifiersInLines) {
		TBTransientTopic topic = new TBTransientTopic();
		TBImportTopic importTopic = new TBImportTopic();
		importTopic.setTopic(topic);
		
		// Last to first, so that the error of the most left column in the row is displayed.
		try {
			if (!definitions.isEmpty()) {
				for (int i = 0; i < definitions.size(); i++) {
					TBCustomFieldDefinition definition = definitions.get(i);
					int column = 6 + i;
					String customFieldValue = line.length > column? line[column]: null;
					if (customFieldValue != null && customFieldValue.isBlank()) {
						customFieldValue = null;
					}
					importTopic.putCustomFieldDefinitionToValue(definition, customFieldValue);
				}
			}
			
			
			String groupRestrictionInput = line.length > 5? line[5]: "";
			if (StringHelper.containsNonWhitespace(groupRestrictionInput)) {
				Set<Long> groupRestrictionKeys = Arrays.stream(groupRestrictionInput.split(","))
						.filter(StringHelper::isLong)
						.map(Long::valueOf)
						.collect(Collectors.toSet());
				groupRestrictionKeys.retainAll(importContext.getGroupRestrictionCandidates().getBusinessGroupKeys());
				topic.setGroupRestrictionKeys(groupRestrictionKeys);
			}
			
			if (line.length > 4) {
				String maxParticipantsInput = line[4];
				if (StringHelper.containsNonWhitespace(maxParticipantsInput)) {
					importTopic.setMaxParticipants(maxParticipantsInput);
					if (StringHelper.isLong(maxParticipantsInput)) {
						Integer maxParticipants = Integer.valueOf(maxParticipantsInput);
						if (maxParticipants >= 0) {
							topic.setMaxParticipants(maxParticipants);
						} else {
							importTopic.setMessage(translate("import.error.positiv.integer.participants.max"));
						}
					} else {
						importTopic.setMessage(translate("import.error.positiv.integer.participants.max"));
					}
				} else {
					importTopic.setMessage(translate("import.error.mandatory.participants.max"));
				}
			} else {
				importTopic.setMessage(translate("import.error.mandatory.participants.max"));
			}
			
			
			if (line.length > 3) {
				String minParticipantsInput = line[3];
				if (StringHelper.containsNonWhitespace(minParticipantsInput)) {
					importTopic.setMinParticipants(minParticipantsInput);
					if (StringHelper.isLong(minParticipantsInput)) {
						Integer minParticipants = Integer.valueOf(minParticipantsInput);
						if (minParticipants >= 0) {
							topic.setMinParticipants(minParticipants);
						} else {
							importTopic.setMessage(translate("import.error.positiv.integer.participants.min"));
						}
					} else {
						importTopic.setMessage(translate("import.error.positiv.integer.participants.min"));
					}
				} else {
					importTopic.setMessage(translate("import.error.mandatory.participants.min"));
				}
			} else {
				importTopic.setMessage(translate("import.error.mandatory.participants.min"));
			}
			
			if (topic.getMinParticipants() != null && topic.getMaxParticipants() != null && topic.getMinParticipants() > topic.getMaxParticipants()) {
				importTopic.setMessage(translate("import.error.participants.max.lower.min"));
			}
			
			
			if (line.length > 2) {
				String description = line[2];
				importTopic.setDescription(description);
				topic.setDescription(description);
			}
			
			
			if (line.length > 1) {
				String title = line[1];
				importTopic.setTitle(title);
				topic.setTitle(title);
				if (!StringHelper.containsNonWhitespace(topic.getTitle())) {
					importTopic.setMessage(translate("import.error.mandatory.title"));
				} else if (title.length() > TBTopic.TITLE_MAX_LENGTH) {
					importTopic.setMessage(translate("import.error.title.too.long", String.valueOf(TBTopic.TITLE_MAX_LENGTH)));
				}
			} else {
				importTopic.setMessage(translate("import.error.mandatory.title"));
			}
			
			String identifier = line[0];
			importTopic.setIdentifier(identifier);
			topic.setIdentifier(identifier);
			if (identifiersInLines.contains(identifier)) {
				importTopic.setMessage(translate("import.error.identifier.multi"));
			} else if (identifier.length() > TBTopic.IDENTIFIER_MAX_LENGTH) {
				importTopic.setMessage(translate("import.error.identifier.too.long", String.valueOf(TBTopic.IDENTIFIER_MAX_LENGTH)));
			} else {
				identifiersInLines.add(identifier);
			}
		} catch (Exception e) {
			importTopic.setMessage(translate("import.error.unknown"));
		}
		
		return importTopic;
	}
	
	private List<String[]> getLines(String input) {
		// Escape quota characters inside multi column lines
		String escapedInput = escapeQuotes(input);
		
		CSVParser parser = new CSVParserBuilder()
				.withSeparator('\t')
				.build();
		
		List<String[]> lines = new ArrayList<>();
		try(CSVReader reader = new CSVReaderBuilder(new StringReader(escapedInput))
					.withCSVParser(parser)
					.build()) {
			
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if(nextLine.length > 0) {
					lines.add(nextLine);
				}
			}
		} catch (IOException | CsvValidationException e) {
			logError("", e);
		}
		return lines;
	}

	private String escapeQuotes(String input) {
		String[] tokens = input.split("\t");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			
			boolean multiline = false;
			if (token.startsWith("\"") && token.contains("\n")) {
				multiline = true;
				// Remove multi line quotes
				token = token.substring(1, token.length() - 1);
			}
			
			// Excel escapes quotes as two quotes
			token = token.replaceAll("\"\"", "\"");
			// Escape quotes inside the token
			token = token.replaceAll("\"", "\\\\\"");
			
			if (multiline) {
				// Add multi line quotes again
				token = "\"" + token + "\"";
			}
			
			tokens[i] = token;
		}
		
		return Arrays.stream(tokens).collect(Collectors.joining("\t"));
	}

	private void appendFiles(List<TBImportTopic> topics) {
		if (importContext.getTempFilesDir() == null || !importContext.getTempFilesDir().exists()) {
			return;
		}
		
		Map<String, TBImportTopic> identifierToTopic = topics.stream()
				.collect(Collectors.toMap(TBImportTopic::getIdentifier, Function.identity()));
		
		Set<String> topicIdentifiers = new HashSet<>(identifierToTopic.keySet());
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(importContext.getBroker());
		topicBrokerService.getTopics(searchParams).stream()
				.map(TBTopic::getIdentifier)
				.forEach(identifier -> topicIdentifiers.add(identifier));
		
		Set<String> validDirIdentifiers = definitions.stream()
				.filter(definition -> TBCustomFieldType.file == definition.getType())
				.map(TBCustomFieldDefinition::getName)
				.collect(Collectors.toSet());
		validDirIdentifiers.add(TopicBrokerExportService.EXPORT_TEASER_IMAGE);
		validDirIdentifiers.add(TopicBrokerExportService.EXPORT_TEASER_VIDEO);
		
		File topicsRoot = getRootFile(importContext.getTempFilesDir());
		for (File topicRoot : topicsRoot.listFiles()) {
			if (topicRoot.isDirectory()) {
				String topicIdentifier = topicRoot.getName();
				// No creation of new topics with only files
				if (topicIdentifiers.contains(topicIdentifier)) {
					TBImportTopic importTopic = identifierToTopic.get(topicIdentifier);
					if (importTopic == null) {
						// Topic not in csv but it exists already in the database
						TBTransientTopic topic = new TBTransientTopic();
						topic.setIdentifier(topicIdentifier);
						importTopic = new TBImportTopic();
						importTopic.setTopic(topic);
						importTopic.setIdentifier(topicIdentifier);
						importTopic.setFilesOnly(true);
					}
					appendFiles(importTopic, topicRoot);
					if (importTopic.isFilesOnly() && importTopic.getIdentifierToFile() != null && !importTopic.getIdentifierToFile().isEmpty()) {
						topics.add(importTopic);
					}
				}
			}
		}
	}
	
	private void appendFiles(TBImportTopic importTopic, File topicRoot) {
		File[] topicFileDirs = topicRoot.listFiles();
		
		File topicFileDir = getTopicFileDir(topicFileDirs, EXPORT_TEASER_IMAGE);
		if (topicFileDir != null) {
			appendFile(importTopic, topicFileDir, TEASER_IMAGE_MIME_TYPES, TEASER_IMAGE_MAX_SIZE_KB, null);
		}
		
		topicFileDir = getTopicFileDir(topicFileDirs, EXPORT_TEASER_VIDEO);
		if (topicFileDir != null) {
			appendFile(importTopic, topicFileDir, TEASER_VIDEO_MIME_TYPES, TEASER_VIDEO_MAX_SIZE_KB, null);
		}
		
		for (TBCustomFieldDefinition definition : definitions) {
			if (TBCustomFieldType.file == definition.getType()) {
				topicFileDir = getTopicFileDir(topicFileDirs, definition.getName());
				if (topicFileDir != null) {
					appendFile(importTopic, topicFileDir, null, TopicBrokerService.CUSTOM_FILE_MAX_SIZE_KB, definition);
				}
			}
		}
	}
	
	private void appendFile(TBImportTopic importTopic, File topicFileDir, Set<String> mimeTypes, int maxSizeKb, TBCustomFieldDefinition definition) {
		List<File > files = getRegularFiles(topicFileDir);
		
		if (files.size() == 0) {
			return;
		}
		
		if (files.size() > 1) {
			if (!StringHelper.containsNonWhitespace(importTopic.getMessage())) {
				importTopic.setMessage(translate("import.error.directory.multi.files", topicFileDir.getName()));
			}
		}
		
		File file = files.get(0);
		
		if (mimeTypes != null && !mimeTypes.isEmpty()) {
			String mimeType = WebappHelper.getMimeType(file.getName());
			if (!StringHelper.containsNonWhitespace(mimeType) || !mimeTypes.contains(mimeType)) {
				if (!StringHelper.containsNonWhitespace(importTopic.getMessage())) {
					importTopic.setMessage(translate("import.error.wrong.mime.type", file.getName()));
				}
			}
		}
		
		if (maxSizeKb < file.length() / 1000) {
			if (!StringHelper.containsNonWhitespace(importTopic.getMessage())) {
				importTopic.setMessage(translate("import.error.file.too.large", file.getName()));
			}
		}
		
		importTopic.putFile(topicFileDir.getName(), file);
		if (definition != null) {
			importTopic.putCustomFieldDefinitionToValue(definition, file.getName());
		}
	}

	private List<File> getRegularFiles(File topicFileDir) {
		try {
			return Files.walk(topicFileDir.toPath(), 1)
					.map(Path::toFile)
					.filter(file -> 
							   file.isFile()
							&& !file.isHidden()
							&& !file.getName().startsWith("__MACOSX")
							&& !file.getName().contains(".DS_Store"))
					.toList();
		} catch (IOException e) {
			log.error("", e);
		}
		return List.of();
	}

	private File getTopicFileDir(File[] topicFileDirs, String exportTeaserImage) {
		for (File topicFileDir : topicFileDirs) {
			if (topicFileDir.isDirectory() && exportTeaserImage.equals(topicFileDir.getName())) {
				return topicFileDir;
			}
		}
		return null;
	}

	private File getRootFile(File tempFilesDir) {
		RootFileFinder rootFileFinder = new RootFileFinder();
		try {
			Files.walkFileTree(tempFilesDir.toPath(), rootFileFinder);
			if (rootFileFinder.getRootFilePath() != null) {
				return rootFileFinder.getRootFilePath().toFile();
			}
		} catch (IOException e) {
			log.error("", e);
		}
		
		// tempFilesDir is already the root dir
		return tempFilesDir;
	}
	
	private static class RootFileFinder implements FileVisitor<Path>{
		
		private Path rootFilePath;
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.getFileName().startsWith("__MACOSX") || dir.getFileName().startsWith(".DS_Store")) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			if (dir.getFileName().startsWith(ImportInputController.FILES_IMPORT_TEMPLATE_NAME)
					|| dir.getFileName().startsWith(TopicBrokerMediaResource.TOPIC_FILES_PATH)) {
				rootFilePath = dir;
				return FileVisitResult.TERMINATE;
			}
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}
		
		Path getRootFilePath() {
			return rootFilePath;
		}
	
	}

	public static class TBTopicImportDataModel extends DefaultFlexiTableDataModel<TBImportTopic> {
		
		public static final int CUSTOM_FIELD_OFFSET = 100;
		private static final TopicImportCols[] COLS = TopicImportCols.values();
		
		private final List<TBCustomFieldDefinition> customFieldDefinitions;

		public TBTopicImportDataModel(FlexiTableColumnModel columnsModel, List<TBCustomFieldDefinition> customFieldDefinitions) {
			super(columnsModel);
			this.customFieldDefinitions = customFieldDefinitions;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			TBImportTopic topic = getObject(row);
			return getValueAt(topic, col);
		}

		public Object getValueAt(TBImportTopic row, int col) {
			if (col >= CUSTOM_FIELD_OFFSET) {
				if (row.getCustomFieldDefinitionToValue() == null) {
					return null;
				}
				TBCustomFieldDefinition customFieldDefinition = customFieldDefinitions.get(col - CUSTOM_FIELD_OFFSET);
				return row.getCustomFieldDefinitionToValue().get(customFieldDefinition);
			}
			
			switch(COLS[col]) {
			case identifier: return row.getIdentifier();
			case title: return row.getTitle();
			case description: return row.getDescription();
			case minParticipants: return row.getMinParticipants();
			case maxParticipants: return row.getMaxParticipants();
			case groupRestrictions: return row.getGroupRestrictions();
			case teaserImage: return row.getIdentifierToFile() != null && row.getIdentifierToFile().containsKey(EXPORT_TEASER_IMAGE)
					? row.getIdentifierToFile().get(EXPORT_TEASER_IMAGE).getName() : null;
			case teaserVideo: return row.getIdentifierToFile() != null && row.getIdentifierToFile().containsKey(EXPORT_TEASER_VIDEO)
					? row.getIdentifierToFile().get(EXPORT_TEASER_VIDEO).getName() : null;
			case importMessage: return StringHelper.containsNonWhitespace(row.getMessage())
					? "<span><i class=\"o_icon o_icon_error\"></i> " + row.getMessage() + "</span>"
					: null;
			default: return null;
			}
		}
		
		public enum TopicImportCols implements FlexiColumnDef {
			identifier("topic.identifier"),
			title("topic.title"),
			description("topic.description"),
			minParticipants("topic.participants.min"),
			maxParticipants("topic.participants.max"),
			groupRestrictions("topic.group.restriction"),
			teaserImage("topic.teaser.image"),
			teaserVideo("topic.teaser.video"),
			importMessage("import.message");
			
			private final String i18nKey;
			
			private TopicImportCols(String i18nKey) {
				this.i18nKey = i18nKey;
			}
			
			@Override
			public String i18nHeaderKey() {
				return i18nKey;
			}
		}
	}

}
