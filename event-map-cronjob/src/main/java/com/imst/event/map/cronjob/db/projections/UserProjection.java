package com.imst.event.map.cronjob.db.projections;

public interface UserProjection {
	
	Integer getId();
	String getUsername();
	String getName();
	Integer getProfileId();
	String getProfileName();
	Integer getProviderUserId();
}
