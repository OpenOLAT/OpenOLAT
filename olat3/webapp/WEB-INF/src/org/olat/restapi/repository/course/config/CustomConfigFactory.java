package org.olat.restapi.repository.course.config;

import org.olat.repository.RepositoryEntry;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService.CustomConfigDelegate;

public class CustomConfigFactory {
	
	static ICustomConfigCreator creator = null;
	
	public CustomConfigFactory(ICustomConfigCreator creator) {
		CustomConfigFactory.creator = creator;
	}
	
	public static CustomConfigDelegate getTestCustomConfig(RepositoryEntry repoEntry) {
		return CustomConfigFactory.creator.getTestCustomConfig(repoEntry);
	}
	
	public static CustomConfigDelegate getSurveyCustomConfig(RepositoryEntry repoEntry) {
		return CustomConfigFactory.creator.getSurveyCustomConfig(repoEntry);
	}
	
	public interface ICustomConfigCreator {
		public CustomConfigDelegate getTestCustomConfig(RepositoryEntry repoEntry);
		public CustomConfigDelegate getSurveyCustomConfig(RepositoryEntry repoEntry);
	}
}