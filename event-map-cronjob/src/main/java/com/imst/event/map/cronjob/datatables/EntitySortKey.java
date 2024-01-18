package com.imst.event.map.cronjob.datatables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EntitySortKey {
	
	/**
	 * Entity field name for sort
	 */
	String value() default "";
	
	boolean sortable() default true;
}
