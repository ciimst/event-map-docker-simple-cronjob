package com.imst.event.map.cronjob.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.imst.event.map.cronjob.db.repositories.StateRepository;
import com.imst.event.map.hibernate.entity.State;


@Controller
@RequestMapping({"/","home"})
public class HomeController {
	

	@Autowired StateRepository stateRepository;
	
	@GetMapping(value = {"/ready"})
	public ResponseEntity<?> readinessProbe() {
		return ResponseEntity.ok().build();
	}
	
	@GetMapping(value = {"/live"})
	public ResponseEntity<?> livenessProbe() {
		try {
			List<State> cont = null;
			cont = stateRepository.findAll();
			if ((cont != null && cont.isEmpty()) || cont == null) {
				return ResponseEntity.badRequest().build();
			}
		}
		catch(Exception e) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok().build();
	}
	
}
