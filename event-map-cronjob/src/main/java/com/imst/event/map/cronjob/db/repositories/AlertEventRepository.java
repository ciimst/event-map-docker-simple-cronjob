package com.imst.event.map.cronjob.db.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.imst.event.map.cronjob.db.ProjectionRepository;
import com.imst.event.map.hibernate.entity.AlertEvent;
import com.imst.event.map.hibernate.entity.User;

@Transactional
public interface AlertEventRepository extends ProjectionRepository<AlertEvent, Integer> {
	
	
	long countByUserAndReadStateIsFalse(User user);
	
	long countByUser(User user);
	
	
	@Modifying
	@Query("delete from AlertEvent alertEvent where alertEvent.alert.id = :alertId")
	void alertEventByAlertIdDeleted(@Param("alertId") Integer alertId);
	
}


