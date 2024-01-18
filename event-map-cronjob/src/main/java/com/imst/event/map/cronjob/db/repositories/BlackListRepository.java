package com.imst.event.map.cronjob.db.repositories;

import java.util.List;

import com.imst.event.map.cronjob.db.ProjectionRepository;
import com.imst.event.map.hibernate.entity.BlackList;

public interface BlackListRepository extends ProjectionRepository<BlackList, Integer> {
	
//	Page<BlackListProjection> findAllProjectedBy(Pageable pageable);
//	BlackListProjection findProjectedById(Integer id);
//	BlackListProjection findProjectedByIdAndStateIdIn(Integer id, List<Integer> stateIds);
//	
//	List<BlackListProjection> findAllProjectedBy();
//	
////	List<BlackList> findAllByActionStateAndStateId(Boolean actionState, Integer stateId);
//	
	List<BlackList> findAllByActionStateIdInAndStateIdIn(List<Integer> actionStateIds, List<Integer> stateIds);
//	
//	Page<BlackListProjection> findAllProjectedByEventGroupIdInOrEventGroupLayerIdIn(Pageable pageable, List<Integer> eventGroupIdList, List<Integer> layerIdList);
//	
//	Page<BlackListProjection> findAllProjectedByStateIdInAndEventGroupIdInOrStateIdInAndEventGroupLayerIdIn(Pageable pageable, List<Integer> stateIds, List<Integer> eventGroupIdList, List<Integer> stateIds2,List<Integer> layerIdList);
//	
//	BlackList findByIdAndStateIdIn(Integer id, List<Integer> stateIds);
//	
//	long countByEventGroupIdAndStateIdNot(Integer eventGroupId, Integer stateId);
//	
//	long deleteByEventGroupIdAndStateId(Integer eventGroupId, Integer stateId);
	
}
