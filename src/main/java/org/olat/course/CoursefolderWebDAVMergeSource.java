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
package org.olat.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.manager.WebDAVMergeSource;
import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementWebDAVInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * This WebDAV provider delivery all folders in courses where the user
 * is owner or is editor via a right group.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class CoursefolderWebDAVMergeSource extends WebDAVMergeSource {
	
	private final IdentityEnvironment identityEnv;
	
	private final WebDAVModule webDAVModule;
	private final RepositoryManager repositoryManager;
	private final CurriculumService curriculumService;
	
	public CoursefolderWebDAVMergeSource(IdentityEnvironment identityEnv) {
		super(identityEnv.getIdentity());
		this.identityEnv = identityEnv;
		webDAVModule = CoreSpringFactory.getImpl(WebDAVModule.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
	}
	
	/*
	 * _others
	 *   _managed
	 *     _closed
	 *   _manually
	 *     _closed
	 * 
	 * (non-Javadoc)
	 * @see org.olat.core.commons.services.webdav.manager.WebDAVMergeSource#loadMergedContainers()
	 */
	@Override
	protected List<VFSContainer> loadMergedContainers() {
		List<VFSContainer> containers = new ArrayList<>();
		Map<String, VFSContainer> terms = new HashMap<>();
	
		boolean useSemestersTerms = webDAVModule.isTermsFoldersEnabled();
		boolean useCurriculumElementsTerms = webDAVModule.isCurriculumElementFoldersEnabled();
		boolean prependReference = webDAVModule.isPrependCourseReferenceToTitle();
		boolean useManaged = webDAVModule.isManagedFoldersEnabled();
		
		NamingAndGrouping namingAndGrouping = new NamingAndGrouping(prependReference, useManaged, useSemestersTerms, useCurriculumElementsTerms);
		if(useCurriculumElementsTerms) {
			namingAndGrouping.setCurriculumElementInfos(getCurriculumElementWebDAVInfosMap());
		}
		
		NoTermContainer noTermContainer = new NoTermContainer(namingAndGrouping);
		
		List<RepositoryEntry> editorEntries = repositoryManager.queryByOwner(getIdentity(), true, null, "CourseModule");
		appendCourses(editorEntries, containers, terms, noTermContainer, namingAndGrouping, true);
		
		//add courses as participant and coaches
		if(webDAVModule.isEnableLearnersParticipatingCourses()) {
			List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsParticipantAndCoach(getIdentity(), "CourseModule");
			appendCourses(entries, containers, terms, noTermContainer, namingAndGrouping, false);
		}
		
		//add bookmarked courses
		if(webDAVModule.isEnableLearnersBookmarksCourse()) {
			List<RepositoryEntry> bookmarkedEntries = repositoryManager.getLearningResourcesAsBookmarkedMember(getIdentity(), identityEnv.getRoles(), "CourseModule", 0, -1);
			appendCourses(bookmarkedEntries, containers, terms, noTermContainer, namingAndGrouping, false);
		}

		if (useSemestersTerms || useCurriculumElementsTerms) {
			// add no-terms folder if any have been found
			if (noTermContainer.hasNoTerms()) {
				addContainerToList(noTermContainer.computeNoTermContainer(), containers);
			}
		} else if(noTermContainer.hasFinished()) {
			addContainerToList(noTermContainer.getFinishedContainer(), containers);
		}

		return containers;
	}
	
	private Map<Long,List<CurriculumElementWebDAVInfos>> getCurriculumElementWebDAVInfosMap() {
		List<CurriculumElementWebDAVInfos> infos = curriculumService.getCurriculumElementInfosForWebDAV(getIdentity());
		Map<Long,List<CurriculumElementWebDAVInfos>> infoMap = new HashMap<>();
		for(CurriculumElementWebDAVInfos info:infos) {
			List<CurriculumElementWebDAVInfos> repoInfos = infoMap
					.computeIfAbsent(info.getRepositoryEntryKey(), i -> new ArrayList<>());
			if(!repoInfos.contains(info)) {
				repoInfos.add(info);
			}
		}
		return infoMap;
	}
	
	private void appendCourses(List<RepositoryEntry> courseEntries, List<VFSContainer> containers,
			Map<String, VFSContainer> terms, NoTermContainer noTermContainer,
			NamingAndGrouping namingAndGrouping, boolean editor) {	
		
		// Add all found repo entries to merge source
		int count = 0;
		for (RepositoryEntry re:courseEntries) {
			if(namingAndGrouping.isDuplicate(re)) {
				continue;
			}

			if(namingAndGrouping.isUseFinished() && re.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
				String courseTitle = getCourseTitle(re, namingAndGrouping.isPrependReference());
				String name = namingAndGrouping.getFinishedUniqueName(courseTitle);
				NamedContainerImpl cfContainer = new CoursefolderWebDAVNamedContainer(name, re, editor ? null : identityEnv);
				noTermContainer.addToFinished(cfContainer);
			} else if (namingAndGrouping.isUseSemesterTerms() || namingAndGrouping.isUseCurriculumElementsTerms()) {
				appendCoursesWithTerms(re, containers, noTermContainer, terms, namingAndGrouping, editor);
			} else {
				String courseTitle = getCourseTitle(re, namingAndGrouping.isPrependReference());
				String name = namingAndGrouping.getContainersUniqueName(courseTitle);
				NamedContainerImpl cfContainer = new CoursefolderWebDAVNamedContainer(name, re, editor ? null : identityEnv);
				addContainerToList(cfContainer, containers);
			}
			if(++count % 5 == 0) {
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
	}
	
	private void appendCoursesWithTerms(RepositoryEntry re, List<VFSContainer> containers, NoTermContainer noTermContainer,
			Map<String, VFSContainer> terms, NamingAndGrouping namingAndGrouping, boolean editor) {
		RepositoryEntryLifecycle lc = re.getLifecycle();
		
		boolean termed = false;
		if (namingAndGrouping.isUseSemesterTerms() && lc != null && !lc.isPrivateCycle()) {
			// when a semester term info is found, add it to corresponding term folder
			String termSoftKey = lc.getSoftKey();
			VFSContainer termContainer = terms.computeIfAbsent(termSoftKey, term -> {
				String normalizedKey = RequestUtil.normalizeFilename(term);
				VirtualContainer container = new VirtualContainer(normalizedKey);
				addContainerToList(container, containers);
				return container;
			});

			String courseTitle = getCourseTitle(re, namingAndGrouping.isPrependReference());
			String name = namingAndGrouping.getTermUniqueName(termSoftKey, courseTitle);
			NamedContainerImpl cfContainer = new CoursefolderWebDAVNamedContainer(name, re, editor ? null : identityEnv);
			termContainer.getItems().add(cfContainer);
			termed = true;
		}
		
		if(namingAndGrouping.isUseManaged() && re.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			String courseTitle = getCourseTitle(re, namingAndGrouping.isPrependReference());
			String name = namingAndGrouping.getNoTermUniqueName(courseTitle);
			NamedContainerImpl cfContainer = new CoursefolderWebDAVNamedContainer(name, re, editor ? null : identityEnv);
			noTermContainer.add(re, cfContainer);
			termed = true;
		} else if(namingAndGrouping.isUseCurriculumElementsTerms() && namingAndGrouping.hasCurriculumElements(re)) {
			List<CurriculumElementWebDAVInfos> elements = namingAndGrouping.getCurriculumElementInfos().get(re.getKey());
			for(CurriculumElementWebDAVInfos element:elements) {
				String termSoftKey = getTermSoftKey(element);
				VFSContainer termContainer = terms.computeIfAbsent(termSoftKey, term -> {
					String normalizedKey = RequestUtil.normalizeFilename(term);
					VirtualContainer container = new VirtualContainer(normalizedKey);
					addContainerToList(container, containers);
					return container;
				});	

				String courseTitle = getCourseTitle(re, false);
				String name = namingAndGrouping.getTermUniqueName(termSoftKey, courseTitle);
				NamedContainerImpl cfContainer = new CoursefolderWebDAVNamedContainer(name, re, editor ? null : identityEnv);
				termContainer.getItems().add(cfContainer);
				termed = true;
			}
		}
		
		if(!termed) {
			// no semester term found, add to no-term folder
			String courseTitle = getCourseTitle(re, namingAndGrouping.isPrependReference());
			String name = namingAndGrouping.getNoTermUniqueName(courseTitle);
			NamedContainerImpl cfContainer = new CoursefolderWebDAVNamedContainer(name, re, editor ? null : identityEnv);
			noTermContainer.add(re, cfContainer);
		}
	}
	
	private String getCourseTitle(RepositoryEntry re, boolean prependReference) {
		String displayName = re.getDisplayname();
		if(prependReference && StringHelper.containsNonWhitespace(re.getExternalRef())) {
			displayName = re.getExternalRef() + " " + displayName;
		}
		return RequestUtil.normalizeFilename(displayName);
	}
	
	private String getTermSoftKey(CurriculumElementWebDAVInfos element) {
		StringBuilder sb = new StringBuilder(64);
		if(StringHelper.containsNonWhitespace(element.getParentCurriculumElementDisplayName())) {
			if(StringHelper.containsNonWhitespace(element.getParentCurriculumElementIdentifier())) {
				sb.append(element.getParentCurriculumElementIdentifier()).append(" ");
			}
			sb.append(element.getParentCurriculumElementDisplayName());
		} else if(StringHelper.containsNonWhitespace(element.getCurriculumElementDisplayName())) {
			if(StringHelper.containsNonWhitespace(element.getCurriculumElementIdentifier())) {
				sb.append(element.getCurriculumElementIdentifier()).append(" ");
			}
			sb.append(element.getCurriculumElementDisplayName());
		}
		return sb.toString();
	}
	
	private static class NoTermContainer {
	
		private final Set<RepositoryEntry> entriesSet = new HashSet<>();
		private final VirtualContainer noTermContainer = new VirtualContainer("_other");
		private final VirtualContainer finishedContainer = new VirtualContainer("_finished");
		

		private VirtualContainer managedContainer;
		private VirtualContainer managedAndClosedContainer;
		private VirtualContainer manuallyContainer;
		private VirtualContainer manuallyAndClosedContainer;
		
		private final NamingAndGrouping namingAndGrouping;
		
		public NoTermContainer(NamingAndGrouping namingAndGrouping) {
			this.namingAndGrouping = namingAndGrouping;
		}
		
		public boolean hasNoTerms() {
			return !noTermContainer.getItems().isEmpty();
		}
		
		public VFSContainer computeNoTermContainer() {
			return noTermContainer;
		}
		
		public boolean hasFinished() {
			return !finishedContainer.getItems().isEmpty();
		}
		
		public VFSContainer getFinishedContainer() {
			return finishedContainer;
		}
		
		public void addToFinished(VFSContainer container) {
			finishedContainer.getItems().add(container);
		}
		
		public void addToManagedAndClosed(VFSContainer container) {
			if(managedAndClosedContainer == null) {
				managedAndClosedContainer = new VirtualContainer("_closed");
				addToManaged(managedAndClosedContainer);
			}
			managedAndClosedContainer.getItems().add(container);
		}
		
		public void addToManaged(VFSContainer container) {
			if(managedContainer == null) {
				managedContainer = new VirtualContainer("_managed");
				noTermContainer.getItems().add(managedContainer);
			}
			managedContainer.getItems().add(container);	
		}
		
		public void addToManuualyAndClosed(VFSContainer container) {
			if(manuallyAndClosedContainer == null) {
				manuallyAndClosedContainer = new VirtualContainer("_closed");
				addToManually(manuallyAndClosedContainer);
			}
			manuallyAndClosedContainer.getItems().add(container);
		}
		
		public void addToManually(VFSContainer container) {
			if(manuallyContainer == null) {
				manuallyContainer = new VirtualContainer("_manually");
				noTermContainer.getItems().add(manuallyContainer);
			}
			manuallyContainer.getItems().add(container);
		}
		
		public void add(RepositoryEntry entry, VFSContainer container) {
			if(entriesSet.contains(entry)) {
				return;
			} else {
				entriesSet.add(entry);
			}
			if(namingAndGrouping.isUseManaged()) {
				if(StringHelper.containsNonWhitespace(entry.getManagedFlagsString())) {
					if(entry.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
						addToManagedAndClosed(container);
					} else {
						addToManaged(container);
					}
				} else if(entry.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
					addToManuualyAndClosed(container);
				} else {
					addToManually(container);
				}
			} else {
				noTermContainer.getItems().add(container);
			}
		}
	}
	
	private static class NamingAndGrouping {
		
		private final boolean useManaged;
		private final boolean prependReference;
		private final boolean useSemesterTerms;
		private final boolean useCurriculumElementsTerms;
		
		private final Set<RepositoryEntry> duplicates = new HashSet<>();
		private final Set<String> containers = new HashSet<>();
		private final Set<String> noTermContainer = new HashSet<>();
		private final Set<String> finishedContainer = new HashSet<>();
		private final Map<String,Set<String>> termContainers = new HashMap<>();
		
		private Map<Long,List<CurriculumElementWebDAVInfos>> curriculumElementInfos;
		
		public NamingAndGrouping(boolean prependReference, boolean useManaged,
				boolean useSemesterTerms, boolean useCurriculumElementsTerms) {
			this.prependReference = prependReference;
			this.useManaged = useManaged;
			this.useSemesterTerms = useSemesterTerms;
			this.useCurriculumElementsTerms = useCurriculumElementsTerms;
		}
		
		public boolean isUseManaged() {
			return useManaged;
		}
		
		public boolean isUseSemesterTerms() {
			return useSemesterTerms;
		}

		public boolean isUseCurriculumElementsTerms() {
			return useCurriculumElementsTerms;
		}
		
		public boolean isPrependReference() {
			return prependReference;
		}
		
		public boolean isUseFinished() {
			return !useSemesterTerms && !useSemesterTerms && !useManaged;
		}

		public Map<Long, List<CurriculumElementWebDAVInfos>> getCurriculumElementInfos() {
			return curriculumElementInfos;
		}

		public void setCurriculumElementInfos(Map<Long, List<CurriculumElementWebDAVInfos>> curriculumElementInfos) {
			this.curriculumElementInfos = curriculumElementInfos;
		}
		
		public boolean hasCurriculumElements(RepositoryEntry re) {
			return curriculumElementInfos != null && curriculumElementInfos.containsKey(re.getKey());
		}

		public boolean isDuplicate(RepositoryEntry re) {
			boolean duplicate = duplicates.contains(re);
			if(!duplicate) {
				duplicates.add(re);
			}
			return duplicate;
		}
		
		private String getTermUniqueName(String term, String courseTitle) {
			String name = courseTitle;
			if(termContainers.containsKey(term)) {
				Set<String> termContainer = termContainers.get(term);
				name = getUniqueName(courseTitle, termContainer);
			} else {
				Set<String> termContainer = new HashSet<>();
				termContainer.add(courseTitle);
				termContainers.put(term, termContainer);
			}
			return name;
		}
		
		private String getNoTermUniqueName(String courseTitle) {
			return getUniqueName(courseTitle, noTermContainer);
		}
		
		private String getFinishedUniqueName(String courseTitle) {
			return getUniqueName(courseTitle, finishedContainer);
		}
		
		private String getContainersUniqueName(String courseTitle) {
			return getUniqueName(courseTitle, containers);
		}
		
		private String getUniqueName(String name, Set<String> set) {
			String uniqueName = name;
			if(set.contains(name)) {
				// attach a serial to the group name to avoid duplicate mount points...
				int serial = 1;
				while (set.contains(name + " " + serial) && serial < 255) {
					serial++;
				}
				uniqueName = name + " " + serial;
			}
			set.add(uniqueName);
			return uniqueName;
		}
		
	}
}