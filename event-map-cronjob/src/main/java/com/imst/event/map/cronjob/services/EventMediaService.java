package com.imst.event.map.cronjob.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.imst.event.map.cronjob.db.dao.MasterDao;
import com.imst.event.map.cronjob.db.specifications.EventMediaSpecifications;
import com.imst.event.map.cronjob.vo.SidebarEventMediaItem;

@Service
public class EventMediaService {

	@Autowired MasterDao masterDao;

	public List<SidebarEventMediaItem> findAllProjectedBySidebar(MasterDao masterDao, Sort sort, List<Integer> eventIdList) {
		
		List<SidebarEventMediaItem> eventSidebarList = masterDao.findAll(EventMediaSpecifications.sidebarSpecification(eventIdList), sort);
		return eventSidebarList;
	}
}
