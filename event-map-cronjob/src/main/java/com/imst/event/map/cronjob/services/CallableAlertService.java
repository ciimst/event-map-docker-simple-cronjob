package com.imst.event.map.cronjob.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.imst.event.map.cronjob.constants.MultitenantDatabaseE;
import com.imst.event.map.cronjob.constants.Statics;
import com.imst.event.map.cronjob.db.dao.MasterDao;
import com.imst.event.map.cronjob.db.multitenant.conf.TenantContext;
import com.imst.event.map.cronjob.utils.AlertUtils;
import com.imst.event.map.cronjob.utils.ApplicationContextUtils;
import com.imst.event.map.cronjob.utils.DateUtils;
import com.imst.event.map.cronjob.utils.IPUtils;
import com.imst.event.map.cronjob.utils.SpatialUtil;
import com.imst.event.map.cronjob.vo.AlertCriteriaEventItem;
import com.imst.event.map.cronjob.vo.DataSourceInfo;
import com.imst.event.map.hibernate.entity.Alert;
import com.imst.event.map.hibernate.entity.AlertEvent;
import com.imst.event.map.hibernate.entity.AlertEventCron;
import com.imst.event.map.hibernate.entity.AlertState;
import com.imst.event.map.hibernate.entity.Event;
import com.vividsolutions.jts.geom.Point;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CallableAlertService  implements Callable<String>{
	
	
	private String tenantName;
	private MultitenantDatabaseE multitenantDatabaseE;
	
	private Integer pageLoadLimit;
	
	private AlertService alertService;
	private EventService eventService;
	private AlertEventCronService alertEventCronService;
	
	private MasterDao masterDao;
	
	
	public CallableAlertService(DataSourceInfo dataSourceInfo) {

		this.tenantName = dataSourceInfo.getName();
		this.multitenantDatabaseE = dataSourceInfo.getMultitenantDatabaseE();
		
		this.pageLoadLimit = Integer.parseInt(ApplicationContextUtils.getProperty("cronjob.alarm.check.size")); // default 500
		
		this.alertService = ApplicationContextUtils.getBean(AlertService.class);
		this.eventService = ApplicationContextUtils.getBean(EventService.class);
		this.alertEventCronService = ApplicationContextUtils.getBean(AlertEventCronService.class);
		
		this.masterDao = multitenantDatabaseE.getMasterDAOBean();
	}
	
	@Override
	public String call() throws Exception {


		AlertState alertState = alertService.findOneAlertStateByDbName(tenantName);
		if(alertState == null) {
			
			Integer lastEventId = 0;
			
			TenantContext.setCurrentTenant(this.tenantName);
			//-------------------Tenant-------------------------------
			AlertCriteriaEventItem oldestEvent = eventService.getLastEventById(masterDao);
			if(oldestEvent != null) {
				lastEventId = oldestEvent.getId();
			}
			//---------------------Tenant End--------------------------------
			
			TenantContext.setCurrentTenant(Statics.DEFAULT_DB_NAME);	
			
			alertState = new AlertState();
			alertState.setDbName(tenantName);
			alertState.setLastId(lastEventId);
			alertState.setCreateDate(DateUtils.nowT());
			
			alertService.saveAlertState(alertState);
			
			log.info(String.format("A new DB added to the system. Db name : %s - Last event id : %s", tenantName, lastEventId));
		}
		
		AlertEventCron alertEventCron = alertEventCronService.getLatestAlertEventCron();
		
		TenantContext.setCurrentTenant(this.tenantName);
		//-------------------Tenant-------------------------------
		PageRequest pageRequest = PageRequest.of(0, pageLoadLimit, Sort.by(Order.asc("id")));			
		Page<AlertCriteriaEventItem> eventList = eventService.findAllProjectedByAlert(masterDao, pageRequest, alertState.getLastId());		
		
		if (alertEventCron != null) {
			eventList = eventService.findAllProjectedByAlert(masterDao, pageRequest, alertEventCron.getEvent().getId());
		}
						
		if(eventList.getContent().size() == 0) {
			return tenantName;
		}
		//---------------------Tenant End--------------------------------
		
//		log.info(String.format("AlertCronjob Basladi. Son event id: %s , size : %s",alertState.getLastId(), eventList.getContent().size()));
	
		List<AlertEventCron> alertEventCronList = new ArrayList<>();
		
		for (AlertCriteriaEventItem alertCriteriaEventItem : eventList) {
			
			alertEventCron = new AlertEventCron();
			alertEventCron.setEvent(eventService.getEventById(alertCriteriaEventItem.getId()));
			alertEventCron.setState("PENDING");
			alertEventCron.setCreateDate(DateUtils.nowT());
			alertEventCron.setUpdateDate(DateUtils.nowT());
			alertEventCron.setRetryCount(0);
			
			alertEventCronList.add(alertEventCron);
			
		}
		try {
			
			alertEventCronService.saveAlertEventCrons(alertEventCronList);			
		} catch (Exception e) {
			log.debug(e.getMessage());
			return tenantName;
		}
				
		TenantContext.setCurrentTenant(Statics.DEFAULT_DB_NAME);
		
		for (AlertCriteriaEventItem alertCriteriaEventItem : eventList) {
			
			Point point = SpatialUtil.getPoint(alertCriteriaEventItem.getLatitude(), alertCriteriaEventItem.getLongitude());
			
			List<Alert> alertList = alertService.findByPolygonContains(point, alertCriteriaEventItem.getLayerId(), alertCriteriaEventItem.getEventTypeId(), alertCriteriaEventItem.getEventGroupId(),this.tenantName);
			
			AlertEventCron currentAlertEventCronItem = alertEventCronService.getAlertEventCronByEventId(alertCriteriaEventItem.getId());
							
			try {
			
				List<AlertEvent> alertEventList = new ArrayList<>();
				for (Alert alert : alertList) {
					
					boolean checkExtraCriteriasResult = AlertUtils.checkExtraCriterias(alertCriteriaEventItem, alert);
					if(!checkExtraCriteriasResult) {
						continue;
					}
					
					AlertEvent alertEvent = new AlertEvent();
					alertEvent.setDbName(tenantName);
					
					Event event = new Event();
					event.setId(alertCriteriaEventItem.getId());
					alertEvent.setEvent(event);
					alertEvent.setUser(alert.getUser());
					alertEvent.setAlert(alert);
					alertEvent.setCreateDate(DateUtils.nowT());
					alertEvent.setEventIdDbName(String.format("%s_%s", alertEvent.getEvent().getId(), alertEvent.getDbName()));
					alertEvent.setReadState(false);
					
					String ip = IPUtils.getIpAddress();
					alertEvent.setIp(ip);
					
					
					alertEventList.add(alertEvent);
				}
				
				if(alertEventList.size() > 0) {				
					alertService.saveAlertEvents(alertEventList);
				}
				
				if(alertState.getLastId() < alertCriteriaEventItem.getId()) {
					alertState.setLastId(alertCriteriaEventItem.getId());
				}
			
			}
			catch(Exception e) {
				currentAlertEventCronItem.setState("ERROR");
				currentAlertEventCronItem.setRetryCount(currentAlertEventCronItem.getRetryCount()+1);
				currentAlertEventCronItem.setUpdateDate(DateUtils.nowT());
				alertEventCronService.saveAlertEventCrons(currentAlertEventCronItem);
				log.error(e);
				continue;
			}
			
			currentAlertEventCronItem.setState("FINISHED");
			currentAlertEventCronItem.setUpdateDate(DateUtils.nowT());
			alertEventCronService.saveAlertEventCrons(currentAlertEventCronItem);
			
		}
		
		
		alertState.setUpdateDate(DateUtils.nowT());
		alertService.saveAlertState(alertState);
		
		return tenantName;
	}
	
}
