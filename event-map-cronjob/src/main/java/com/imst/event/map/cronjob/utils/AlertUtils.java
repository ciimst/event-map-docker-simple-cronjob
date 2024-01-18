package com.imst.event.map.cronjob.utils;

import org.apache.commons.lang3.StringUtils;

import com.imst.event.map.cronjob.vo.AlertCriteriaEventItem;
import com.imst.event.map.hibernate.entity.Alert;

public class AlertUtils {
	
	public static boolean checkExtraCriterias(AlertCriteriaEventItem eventItem, Alert alert) {
		
		boolean queryContains = StringUtils.isEmpty(alert.getQuery()); // Alarm kriterinde herhangi bir kriter belirtilmemiş ise bu kriteri default olarak sağlıyor demektir. true kabul edilir
		if(!queryContains) { // query kriteri title, spot ve description alanları içerisinde OR'lanarak aranır.
			queryContains |= MyStringUtils.containsString(eventItem.getTitle(), alert.getQuery());
			queryContains |= MyStringUtils.containsString(eventItem.getSpot(), alert.getQuery());
			//queryContains |= MyStringUtils.containsString(eventItem.getDescription(), alert.getQuery());
		}
		
		boolean eventTypeResult = alert.getEventType() == null; // Alarm kriterinde EventType belirtilmemiş ise bu kriteri default olarak sağlıyor demektir
		if(!eventTypeResult) {
			eventTypeResult = eventItem.getEventTypeId().equals(alert.getEventType().getId());
		}
		
		boolean eventGroupResult = alert.getEventGroup() == null;
		if(!eventGroupResult) {
			eventGroupResult = eventItem.getEventGroupId().equals(alert.getEventGroup().getId());	
//			 && "default".equals(alert.getEventGroupDbName()
		}
		
		boolean reservedKeyResult = StringUtils.isEmpty(alert.getReservedKey());
		if(!reservedKeyResult) {
			reservedKeyResult = MyStringUtils.containsString(eventItem.getReservedKey(), alert.getReservedKey());
		}
		
		boolean reservedTypeResult = StringUtils.isEmpty(alert.getReservedType());
		if(!reservedTypeResult) {
			reservedTypeResult = MyStringUtils.containsString(eventItem.getReservedType(), alert.getReservedType());
		}
		
		boolean reservedIdResult = StringUtils.isEmpty(alert.getReservedId());
		if(!reservedIdResult) {
			reservedIdResult = MyStringUtils.containsString(eventItem.getReservedId(), alert.getReservedId());
		}
		
		boolean reservedLinkResult = StringUtils.isEmpty(alert.getReservedLink());
		if(!reservedLinkResult) {
			reservedLinkResult = MyStringUtils.containsString(eventItem.getReservedLink(), alert.getReservedLink());
		}
		
		boolean result = queryContains && eventTypeResult && eventGroupResult && reservedKeyResult && reservedTypeResult && reservedIdResult && reservedLinkResult;
		
		return result;
	}

}
