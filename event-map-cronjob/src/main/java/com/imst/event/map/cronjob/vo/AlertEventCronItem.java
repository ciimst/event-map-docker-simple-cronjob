package com.imst.event.map.cronjob.vo;

import java.sql.Timestamp;

import javax.persistence.Column;

import com.imst.event.map.hibernate.entity.Alert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AlertEventCronItem {

	private Integer id;
	
	@Column(name = "event.id")
	private Integer eventId;
	
	private String state;
	private Integer retryCount;
	private Timestamp createDate;
	
	public AlertEventCronItem(Integer eventId, String state, Integer retryCount) {
		
		this.eventId = eventId;
		this.state = state;
		this.retryCount = retryCount;
		
	}
		
	
}
