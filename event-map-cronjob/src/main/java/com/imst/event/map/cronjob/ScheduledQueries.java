package com.imst.event.map.cronjob;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.imst.event.map.cronjob.constants.ActionStateE;
import com.imst.event.map.cronjob.constants.SettingsE;
import com.imst.event.map.cronjob.constants.StateE;
import com.imst.event.map.cronjob.db.repositories.BlackListRepository;
import com.imst.event.map.cronjob.db.repositories.DatabaseDumpRepository;
import com.imst.event.map.cronjob.db.repositories.SettingsRepository;
import com.imst.event.map.cronjob.services.BlackListService;
import com.imst.event.map.cronjob.services.DatabaseBackupService;
import com.imst.event.map.cronjob.utils.DateUtils;
import com.imst.event.map.cronjob.utils.SettingsUtil;
import com.imst.event.map.hibernate.entity.BlackList;
import com.imst.event.map.hibernate.entity.DatabaseDump;
import com.imst.event.map.hibernate.entity.Settings;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ScheduledQueries {
	
	@Autowired private BlackListService blackListService;
	@Autowired private BlackListRepository blackListRepository;
	@Autowired private DatabaseDumpRepository databaseDumpRepository;
	@Autowired private SettingsRepository settingsRepository;
	@Autowired private DatabaseBackupService databaseBackupService;	
	
	
	@Scheduled(initialDelay=10000, fixedDelay = 10000)
	public void blackListScheduler() {
		
		try {
			
			//action state pending, running olan ve state değeri true, false olan blacklistler çekiliyor.
			List<BlackList> blackListForAction = blackListRepository.findAllByActionStateIdInAndStateIdIn(Arrays.asList(ActionStateE.PENDING.getValue(), ActionStateE.RUNNING.getValue()), Arrays.asList(StateE.TRUE.getValue(), StateE.FALSE.getValue(), StateE.DELETED.getValue()));

			for (BlackList blackList : blackListForAction) {
				
				blackListService.oldEventUpdateForBlackList(blackList);
			}

		}
		catch (Exception e) {
			
			log.debug(e);
		}
		
	}
	
	@Scheduled(initialDelayString="${settings.update.initial.delay}", fixedDelayString ="${settings.update.interval}")
	public void generalSettings() {
		
		try {
			
			List<Settings> all = settingsRepository.findAll();
			for (Settings settings : all) {
				
				SettingsUtil.settings.put(settings.getSettingsKey(), settings.getSettingsValue());
			}
		}
		catch (Exception e) {
			
			log.debug(e);
		}
		
	}
	
	@Scheduled(cron = "0 0 * * * ?")
	public void databaseDumpScheduler() {
		
		try {
			
			Timestamp nowT = DateUtils.nowT();	
			
			Timestamp temp = DateUtils.nowT();		
			
			Optional<DatabaseDump> mostRecentDump = databaseDumpRepository.findTopByOrderByCreateDateDesc();
			
			if (mostRecentDump.isPresent()) {
			
				Timestamp mostRecentDumpT = mostRecentDump.get().getCreateDate();				
				
				Timestamp mostRecentDumpTFormatted = DateUtils.convertToTimestamp(DateUtils.format(DateUtils.toDate(mostRecentDumpT), "yyyyMMddHH"), "yyyyMMddHH");
				
				long hours = SettingsUtil.getInteger(SettingsE.DATABASE_BACKUP_INTERVAL) * (1000L * 60L * 60L);
				temp.setTime((1000 * (temp.getTime()/ 1000)) - hours);
				
				if (mostRecentDumpTFormatted.before(temp) || mostRecentDumpTFormatted.equals(temp)) {
					
					log.info("Database otomatik dump islemi basladi");
					
					databaseBackupService.backup(false,nowT);
	
				}
			}
			else {
				
				databaseBackupService.backup(false,nowT);
				
			}
			
		}
		catch (Exception e) {
			
			log.debug(e);
		}
		
	}
	
}
