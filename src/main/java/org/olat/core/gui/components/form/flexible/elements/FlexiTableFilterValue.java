package org.olat.core.gui.components.form.flexible.elements;

/**
 * 
 * Initial date: 10 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableFilterValue {
	
	private String filter;
	private Object value;
	
	public FlexiTableFilterValue(String filter, Object value) {
		this.filter = filter;
		this.value = value;
	}
	
	public static final FlexiTableFilterValue valueOf(Enum<?> filter, Object value) {
		return new FlexiTableFilterValue(filter.name(), value);
	}
	
	public static final FlexiTableFilterValue valueOf(String filter, Object value) {
		return new FlexiTableFilterValue(filter, value);
	}
	
	public String getFilter() {
		return filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}
