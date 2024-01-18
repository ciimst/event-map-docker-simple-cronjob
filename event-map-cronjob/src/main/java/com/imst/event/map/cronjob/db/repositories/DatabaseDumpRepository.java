package com.imst.event.map.cronjob.db.repositories;

import java.util.Optional;

import com.imst.event.map.cronjob.db.ProjectionRepository;
import com.imst.event.map.hibernate.entity.DatabaseDump;

public interface DatabaseDumpRepository extends ProjectionRepository<DatabaseDump, Integer> {
	
	Optional<DatabaseDump> findByName(String name);
	
	Optional<DatabaseDump> findTopByOrderByCreateDateDesc();

}
