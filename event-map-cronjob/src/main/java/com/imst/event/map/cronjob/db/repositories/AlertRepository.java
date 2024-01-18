package com.imst.event.map.cronjob.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.imst.event.map.cronjob.db.ProjectionRepository;
import com.imst.event.map.hibernate.entity.Alert;
import com.vividsolutions.jts.geom.Geometry;


public interface AlertRepository extends ProjectionRepository<Alert, Integer> {
	
	@Query(value = "select a from Alert a WHERE (contains(a.polygonCoordinate, :point) = true or polygon_coordinate is null) and (fk_event_type_id = :eventTypeId or fk_event_type_id is null) and"
			+ " ((fk_event_group_id = :eventGroupId) or fk_event_group_id is null) and a.layer.id = :layerId")
	List<Alert> findByPolygonContains(Geometry point, Integer layerId, Integer eventTypeId, Integer eventGroupId);
	
	
}
