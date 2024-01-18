package com.imst.event.map.cronjob.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.imst.event.map.cronjob.db.dao.MasterDao;
import com.imst.event.map.cronjob.db.repositories.EventRepository;
import com.imst.event.map.cronjob.db.specifications.EventSpecifications;
import com.imst.event.map.cronjob.vo.AlertCriteriaEventItem;
import com.imst.event.map.cronjob.vo.EventItem;
import com.imst.event.map.hibernate.entity.Event;

//@Log4j2
@Service
public class EventService {
	
	@Autowired MasterDao masterDao;
	@Autowired EventRepository eventRepository;
	
	@Autowired EventMediaService eventMediaService;
	@Autowired AlertEventService alertEventService;
	

	public Page<AlertCriteriaEventItem> findAllProjectedByAlert(MasterDao masterDao, PageRequest pageRequest, Integer lastId) {
		
		Page<AlertCriteriaEventItem> alertCriteriaEventList = masterDao.findAll(EventSpecifications.alertSpecification(lastId), pageRequest);
		return alertCriteriaEventList;
	}
	
	public Page<AlertCriteriaEventItem> findAllProjectedByErrorAlert(MasterDao masterDao, PageRequest pageRequest, List<Integer> errorIdList) {
		
		Page<AlertCriteriaEventItem> alertCriteriaEventList = masterDao.findAll(EventSpecifications.errorAlertSpecification(errorIdList), pageRequest);
		return alertCriteriaEventList;
	}

	public Event getEventById(Integer Id) {
		
		Optional<Event> oldestEvent = eventRepository.findById(Id);
		return oldestEvent.get();
	}
	
	public Event getOldestEvent() {
		
		Page<Event> oldestEvent = eventRepository.findAll(PageRequest.of(0, 1, Direction.ASC, "eventDate"));
		return oldestEvent.toList().get(0);
	}
	
	public AlertCriteriaEventItem getLastEventById(MasterDao masterDao) {
		
		Page<AlertCriteriaEventItem> oldestEvent = masterDao.findAll(EventSpecifications.lastEventSpecification(), PageRequest.of(0, 1, Direction.DESC, "id"));
		return oldestEvent.toList().get(0);
	}
	
	
}
