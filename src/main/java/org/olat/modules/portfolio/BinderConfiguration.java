package org.olat.modules.portfolio;

import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderConfiguration {
	
	private final boolean withScore;
	private final boolean withPassed;
	private final boolean assessable;
	
	public BinderConfiguration(boolean assessable, boolean withScore, boolean withPassed) {
		this.assessable = assessable;
		this.withScore = withScore;
		this.withPassed = withPassed;
	}
	
	public boolean isAssessable() {
		return assessable;
	}

	public boolean isWithScore() {
		return withScore;
	}

	public boolean isWithPassed() {
		return withPassed;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("binderconfig[assessable=").append(assessable).append(";")
		  .append("withScore=").append(withScore).append(";")
		  .append("withPassed=").append(withPassed).append("]");
		return sb.toString() + super.toString();
	}
	
	public static BinderConfiguration createTemplateConfig() {
		return new BinderConfiguration(false, false, false);
	}
	
	public static BinderConfiguration createMyPagesConfig() {
		return new BinderConfiguration(false, false, false);
	}

	public static BinderConfiguration createConfig(Binder binder) {
		boolean withScore = false;
		boolean withPassed = false;
		boolean assessable = false;
		
		RepositoryEntry entry = binder.getEntry();
		if(binder.getSubIdent() != null) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
			if(courseNode instanceof PortfolioCourseNode) {
				PortfolioCourseNode pfNode = (PortfolioCourseNode)courseNode;
				withScore = pfNode.hasScoreConfigured();
				withPassed = pfNode.hasPassedConfigured();
				assessable = withPassed || withScore;
			} else {
				withPassed = true;
				withScore = false;
				assessable = true;
			}
		} else if(entry != null) {
			withPassed = true;
			withScore = false;
			assessable = true;
		} else {
			withPassed = withScore = assessable = false;
		}
		return new BinderConfiguration(assessable, withScore, withPassed);
	}
}
