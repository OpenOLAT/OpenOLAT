package org.olat.course.condition.interpreter;

/**
 * 
 * Initial date: 22.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AnyCourseVariable extends AbstractVariable {

	public static final String name = "ANY_COURSE";

	/**
	 * Default constructor to use the current day
	 * @param userCourseEnv
	 */
	public AnyCourseVariable() {
		super(null);
	}
	
	/**
	 * @see com.neemsoft.jmep.VariableCB#getValue()
	 */
	@Override
	public Object getValue() {
		return name;
	}
}