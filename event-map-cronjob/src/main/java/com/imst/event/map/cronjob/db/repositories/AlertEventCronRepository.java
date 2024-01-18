package com.imst.event.map.cronjob.db.repositories;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.imst.event.map.cronjob.db.ProjectionRepository;
import com.imst.event.map.hibernate.entity.AlertEventCron;

@Transactional
public interface AlertEventCronRepository extends ProjectionRepository<AlertEventCron, Integer> {
	
	AlertEventCron findTopByOrderByEventIdDesc();
	
	AlertEventCron findByEventId(Integer eventId);
	
	List<AlertEventCron> findAllByRetryCountLessThanAndState(Integer retryCount, String state);
	
	List<AlertEventCron> findAllByCreateDateLessThanAndState(Timestamp createDate, String state);
	
}


