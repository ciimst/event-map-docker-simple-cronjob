package com.imst.event.map.cronjob.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.imst.event.map.cronjob.db.dao.MasterDao;
import com.imst.event.map.cronjob.db.repositories.AlertEventCronRepository;
import com.imst.event.map.cronjob.db.repositories.EventRepository;
import com.imst.event.map.cronjob.db.specifications.EventSpecifications;
import com.imst.event.map.cronjob.utils.DateUtils;
import com.imst.event.map.cronjob.vo.AlertCriteriaEventItem;
import com.imst.event.map.hibernate.entity.AlertEventCron;

@Service

public class AlertEventCronService {

	@Autowired MasterDao masterDao;
	@Autowired AlertEventCronRepository alertEventCronRepository;
	@Autowired EventRepository eventRepository;
	
	@Autowired EventService eventService;
	
	
	public AlertEventCron getLatestAlertEventCron() {
		
		AlertEventCron lastAlertEventCron = alertEventCronRepository.findTopByOrderByEventIdDesc();
				
		return lastAlertEventCron;
	}
	
	public AlertCriteriaEventItem getLastEventById(MasterDao masterDao) {
		
		Page<AlertCriteriaEventItem> oldestEvent = masterDao.findAll(EventSpecifications.lastEventSpecification(), PageRequest.of(0, 1, Direction.DESC, "id"));
		return oldestEvent.toList().get(0);
	}
	
	public void saveAlertEventCrons(List<AlertEventCron> alertEventCronList) {
		
		alertEventCronRepository.saveAll(alertEventCronList);
	}
	
	public void saveAlertEventCrons(AlertEventCron alertEventCronList) {
		
		alertEventCronRepository.save(alertEventCronList);
	}
	
	public List<AlertEventCron> getErrorAlertEventCronList(Integer retryCount) {
		
		List<AlertEventCron> errorAlertList = alertEventCronRepository.findAllByRetryCountLessThanAndState(retryCount, "ERROR");
		
		return errorAlertList;
	}
	
	public List<AlertEventCron> getPendingAlertEventCronList(Timestamp createDate) {
		
		List<AlertEventCron> errorAlertList = alertEventCronRepository.findAllByCreateDateLessThanAndState(createDate, "PENDING");
		
		return errorAlertList;
	}
	
	public AlertEventCron getAlertEventCronByEventId(Integer eventId) {
		
		AlertEventCron currentAlertEventCron = alertEventCronRepository.findByEventId(eventId);
		
		if (currentAlertEventCron == null) {		
			currentAlertEventCron = new AlertEventCron();
			currentAlertEventCron.setEvent(eventService.getEventById(eventId));
			currentAlertEventCron.setState("PENDING");
			currentAlertEventCron.setRetryCount(0);
			currentAlertEventCron.setCreateDate(DateUtils.nowT());
			currentAlertEventCron.setUpdateDate(DateUtils.nowT());
			return currentAlertEventCron;
		}
				
		return currentAlertEventCron;
	}	
	
	
}
