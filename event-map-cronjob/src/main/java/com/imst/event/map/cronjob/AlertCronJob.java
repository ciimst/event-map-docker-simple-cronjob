package com.imst.event.map.cronjob;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.imst.event.map.cronjob.constants.MultitenantDatabaseE;
import com.imst.event.map.cronjob.constants.Statics;
import com.imst.event.map.cronjob.db.dao.MasterDao;
import com.imst.event.map.cronjob.services.AlertEventCronService;
import com.imst.event.map.cronjob.services.AlertService;
import com.imst.event.map.cronjob.services.CallableAlertService;
import com.imst.event.map.cronjob.services.EventService;
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
import com.imst.event.map.hibernate.entity.Event;
import com.vividsolutions.jts.geom.Point;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AlertCronJob {
	
	@Autowired @Qualifier("masterDataSource") DataSource masterDataSource;
	
	@Autowired private AlertEventCronService alertEventCronService;
	@Autowired private EventService eventService;
	@Autowired private AlertService alertService;
	
	@Autowired private MasterDao masterDao;
	
	@Scheduled(initialDelay = 10000, fixedDelay = 1000) // 1 saniye
	public void alertCheck() {
		
		try {
			
//			if( // bu iplere sahip bilgisayarlarda çalışmaz
//					IPUtils.isIpContaining("177.177.0.123") 
//					|| IPUtils.isIpContaining("192.168.1.10") 
//					|| IPUtils.isIpContaining("177.177.0.183")
//					|| IPUtils.isIpContaining("192.168.17.1")
//					|| IPUtils.isIpContaining("177.177.0.239")
////					|| IPUtils.isIpContaining("177.177.16.17")
//					) {
//				return;
//			}

			// Uygulama açılışında ekstra veritabanları configürasyon dosyasından okunup ayaklandırılmış ve isimleri static bir listede tutulmuştu.
			// bu statik liste kullanılarak her bir veritabanı için bir thread açılır, ve callable classının içerisine yazılmaya başlanır.
			// buradaki işlemlerin çoğu thread içerisinde gerçekleştirileceği için default veritabanı içinde bir thread açılması sağlanmalıdır.
			// thread içerisinde her bir thread kendi veritabanı ismini bildiği için herbiri kendi sorgusunu alert_state tablosuna atar ve son kaldığı idyi öğrenir.
			// Eğer tabloda ilgili veritabanına ait bir kayıt bulunmuyor ise tabloya bir kayıt eklenir ve lastid olarak event tablosundaki en büyük id çekilip buraya yazılır.
			// Bunun amacı yeni eklenen bir veritabanının eski olaylarını önemsemeden burdan sonraki kayıtları için alarm üretilmesi sağlanır.
			// bu durumda elimizde veritabanları için son olay idleri elde etmiş oluruz.
			// daha sonra event tablosundan veritabanlarındaki son idden sonraki olayları sorgulanması sağlanır. 
			// limit olarak 100 - 200 gibi rakamlar kullanılabilir. uzun sürüyor ise azaltılabilir, çünkü diğer threadlerde eleman azsa bu threadi bekliyor olacaklardır.
			// eventler geldiğinde yine callable dosyasının içerisinden devam ediyoruz her bir thread kendi işlemini gerçekleştirir.
			// veritabanındaki alert tablosuna her bir event için sorgu atmaya başlar. bu sorguda elimizde bulunan lat ve long değerlerinden bir polygon oluşturulur. SpatialUtil classı kullanılarak
			// daha sonra bu veri ile beraber spatial sorgu AlertRepository için oluşturulur
			// ve sorgu atılır. Sorgudan gelen cevaplar AlertEvent Tablosuna kaydedilir.
			// sorguda eventin hangi layerda olduğu önemli olabilir. buna göre sorgu atılabilir
			// Bu işlem bütün eventler için uygulanır.
			// işlemler bittikten sonra alert_state tablosuna son işlenen event_id last_id olarak kaydedilir.
			// böylece alert ile ilgili çalışma tamamlanmış olur
			
			ExecutorService executor = Executors.newFixedThreadPool(Statics.tenantDataSourceInfoMap.size() + 1);
			
    		CallableAlertService callableAlertService = new CallableAlertService(new DataSourceInfo(Statics.DEFAULT_DB_NAME, MultitenantDatabaseE.MASTER, masterDataSource) );
	    	executor.submit(callableAlertService);

	    	for (DataSourceInfo dataSourceInfo : Statics.tenantDataSourceInfoMap.values()) {
				
	    		callableAlertService = new CallableAlertService(dataSourceInfo);
		    	executor.submit(callableAlertService);
			}
	    	
	    	executor.shutdown();
	    	executor.awaitTermination(5, TimeUnit.MINUTES);// işlemlerin en fazla 5 dakika sürmesine izin verilir

		}
		catch (Exception e) {
			
			log.catching(e);
		}
		
	}
	
	@Scheduled(initialDelay = 10000, fixedDelay = 10000) // 1 saniye
	public void errorAlertCheck() {
		
		try {
			
			Integer pageLoadLimit = Integer.parseInt(ApplicationContextUtils.getProperty("cronjob.alarm.check.size")); // default 500
			
			PageRequest pageRequest = PageRequest.of(0, pageLoadLimit, Sort.by(Order.asc("id")));
			
			List<AlertEventCron> errorAlertCronList = alertEventCronService.getErrorAlertEventCronList(5);
			
			if (errorAlertCronList.isEmpty()) {
				return;
			}
						
			List<Integer> errorAlertCronIdList = new ArrayList<>();
			
			for (AlertEventCron errorAlertCron : errorAlertCronList) {
				errorAlertCronIdList.add(errorAlertCron.getEvent().getId());
			}
							
			Page<AlertCriteriaEventItem> errorAlertEventList = eventService.findAllProjectedByErrorAlert(masterDao, pageRequest, errorAlertCronIdList);
			
			if(errorAlertEventList.getContent().size() == 0) {
				return;
			}
									
			for (AlertCriteriaEventItem alertCriteriaEventItem : errorAlertEventList) {
				
				Point point = SpatialUtil.getPoint(alertCriteriaEventItem.getLatitude(), alertCriteriaEventItem.getLongitude());
				
				List<Alert> alertList = alertService.findByPolygonContains(point, alertCriteriaEventItem.getLayerId(), alertCriteriaEventItem.getEventTypeId(), alertCriteriaEventItem.getEventGroupId(), "default");
				
				AlertEventCron currentAlertEventCron = alertEventCronService.getAlertEventCronByEventId(alertCriteriaEventItem.getId());
						
				
				try {
				
					List<AlertEvent> alertEventList = new ArrayList<>();
					for (Alert alert : alertList) {
						
						boolean checkExtraCriteriasResult = AlertUtils.checkExtraCriterias(alertCriteriaEventItem, alert);
						if(!checkExtraCriteriasResult) {
							continue;
						}
						
						AlertEvent alertEvent = new AlertEvent();
						alertEvent.setDbName("default");
						
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
				
				}
				catch(Exception e) {
					currentAlertEventCron.setState("ERROR");
					currentAlertEventCron.setRetryCount(currentAlertEventCron.getRetryCount()+1);
					currentAlertEventCron.setUpdateDate(DateUtils.nowT());
					alertEventCronService.saveAlertEventCrons(currentAlertEventCron);
					log.error(e);
					continue;
				}
				
				currentAlertEventCron.setState("FINISHED");
				currentAlertEventCron.setUpdateDate(DateUtils.nowT());
				alertEventCronService.saveAlertEventCrons(currentAlertEventCron);
				
			}

		}
		catch (Exception e) {
			
			log.catching(e);
		}
		
	}
	
	@Scheduled(initialDelay = 10000, fixedDelay = 10000) // 1 saniye
	public void pendingAlertCheck() {
		
		try {
			
			Timestamp oneDayAgo = DateUtils.getFromNow(-1);
			
			Integer pageLoadLimit = Integer.parseInt(ApplicationContextUtils.getProperty("cronjob.alarm.check.size")); // default 500
			
			PageRequest pageRequest = PageRequest.of(0, pageLoadLimit, Sort.by(Order.asc("id")));
			
			List<AlertEventCron> pendingAlertCronList = alertEventCronService.getPendingAlertEventCronList(oneDayAgo);
			
			if (pendingAlertCronList.isEmpty()) {
				return;
			}
						
			List<Integer> pendingAlertCronIdList = new ArrayList<>();
			
			for (AlertEventCron pendingAlertCron : pendingAlertCronList) {
				pendingAlertCronIdList.add(pendingAlertCron.getEvent().getId());
			}
							
			Page<AlertCriteriaEventItem> errorAlertEventList = eventService.findAllProjectedByErrorAlert(masterDao, pageRequest, pendingAlertCronIdList);
			
			if(errorAlertEventList.getContent().size() == 0) {
				return;
			}						
			
			for (AlertCriteriaEventItem alertCriteriaEventItem : errorAlertEventList) {
				
				Point point = SpatialUtil.getPoint(alertCriteriaEventItem.getLatitude(), alertCriteriaEventItem.getLongitude());
				
				List<Alert> alertList = alertService.findByPolygonContains(point, alertCriteriaEventItem.getLayerId(), alertCriteriaEventItem.getEventTypeId(), alertCriteriaEventItem.getEventGroupId(), "default");
				
				AlertEventCron currentAlertEventCron = alertEventCronService.getAlertEventCronByEventId(alertCriteriaEventItem.getId());
						
				
				try {
				
					List<AlertEvent> alertEventList = new ArrayList<>();
					for (Alert alert : alertList) {
						
						boolean checkExtraCriteriasResult = AlertUtils.checkExtraCriterias(alertCriteriaEventItem, alert);
						if(!checkExtraCriteriasResult) {
							continue;
						}
						
						AlertEvent alertEvent = new AlertEvent();
						alertEvent.setDbName("default");
						
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
				
				}
				catch(Exception e) {
					currentAlertEventCron.setState("ERROR");
					currentAlertEventCron.setRetryCount(currentAlertEventCron.getRetryCount()+1);
					currentAlertEventCron.setUpdateDate(DateUtils.nowT());
					alertEventCronService.saveAlertEventCrons(currentAlertEventCron);
					log.error(e);
					continue;
				}
				
				currentAlertEventCron.setState("FINISHED");
				currentAlertEventCron.setUpdateDate(DateUtils.nowT());
				alertEventCronService.saveAlertEventCrons(currentAlertEventCron);
				
			}

		}
		catch (Exception e) {
			
			log.catching(e);
		}
		
	}
}
