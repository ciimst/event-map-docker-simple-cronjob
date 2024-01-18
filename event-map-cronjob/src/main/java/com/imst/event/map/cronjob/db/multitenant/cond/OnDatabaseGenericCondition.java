package com.imst.event.map.cronjob.db.multitenant.cond;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.imst.event.map.cronjob.config.MultitenantConfiguration;
import com.imst.event.map.cronjob.constants.MultitenantDatabaseE;
import com.imst.event.map.cronjob.constants.Statics;
import com.imst.event.map.cronjob.vo.DataSourceInfo;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class OnDatabaseGenericCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		
		MultitenantConfiguration.getInstance();
		
		for (DataSourceInfo dataSourceInfo : Statics.tenantDataSourceInfoMap.values()) {
			if (dataSourceInfo.getMultitenantDatabaseE() == MultitenantDatabaseE.GENERIC) {				
				log.info("Generic multitenant config exist");
				return true;
			}
		}
		
		log.warn("Generic multitenant config not exist");
		return false;
	}
}
