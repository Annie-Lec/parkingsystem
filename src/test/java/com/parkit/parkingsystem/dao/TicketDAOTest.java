package com.parkit.parkingsystem.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;


class TicketDAOTest {

	public static final String VEHICULE_NEW = "IMMAT-SAUV";

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static TicketDAO ticketDAO;

	private static DataBasePrepareService dataBasePrepareService;


	@BeforeAll
	private static void setUp() throws Exception {
		dataBasePrepareService = new DataBasePrepareService();
		// On nettoie la bdd
		dataBasePrepareService.clearDataBaseEntries();

	}

	@BeforeEach
	public void setUpPerTest() throws Exception {
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;

	}

	@DisplayName("Test d'initialisation d un ticket en BDD test à l'entrée du parking - Immat IMMAT-SAUV")
	@Test
	public void testSaveTicket() {
		// GIVEN : Un ticket CAR sur la place numero 1 avec l immatriculation ABCDEF
		Ticket ticket = new Ticket();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setVehicleRegNumber(VEHICULE_NEW);
		ticket.setInTime(new Date(System.currentTimeMillis()));
		ticket.setOutTime(null);
		ticket.setParkingSpot(parkingSpot);

		// WHEN : sauvegarde d'un ticket dans la bdd
		boolean result = ticketDAO.saveTicket(ticket);

		// THEN : Quand on interroge la bdd, on retrouve le ticket inséré
		// 1- le booleen retourne est true car executeUpdate() renvoie le nombre de
		// ligne inséré = 1
		// Si pas de ligne insérée alors la fonction renvoie false
		assertEquals(true, result);
		// doit produire la meme chose : doit renvoyer true
		assertTrue(ticketDAO.saveTicket(ticket));

	}

	@DisplayName("Test pour recuperer un ticket dans une BDD")
	@Test
	public void testGetTicket() {

		// GIVEN
		// GIVEN : Un ticket BIKE sur la place numero 4 avec l immatriculation ABCDEF
		Ticket ticket = new Ticket();
		ParkingSpot parkingSpot = new ParkingSpot(4, ParkingType.BIKE, false);
		ticket.setVehicleRegNumber("ABCDEF");
		ticket.setInTime(new Date(System.currentTimeMillis()-2500));
		ticket.setOutTime(new Date(System.currentTimeMillis()));
		ticket.setParkingSpot(parkingSpot);

		ticketDAO.saveTicket(ticket);
		
		// WHEN
		ticket = ticketDAO.getTicket("ABCDEF");
		
		// THEN
		assertNotNull(ticket);
		
		assertEquals(ticket.getClass(), ticketDAO.getTicket("ABCDEF").getClass());
		assertEquals("ABCDEF", ticketDAO.getTicket("ABCDEF").getVehicleRegNumber());
		assertEquals(4, ticketDAO.getTicket("ABCDEF").getParkingSpot().getId());
		assertEquals(ParkingType.BIKE, ticketDAO.getTicket("ABCDEF").getParkingSpot().getParkingType());
		assertEquals(false, ticketDAO.getTicket("ABCDEF").getParkingSpot().isAvailable());
		assertEquals(ticket.getInTime(), ticketDAO.getTicket("ABCDEF").getInTime());
		assertEquals(ticket.getOutTime(), ticketDAO.getTicket("ABCDEF").getOutTime());

	}


	@DisplayName("Test de mise à jour d'un ticket en BDD")
	@Test
	public void testUpdateTicket() {

		// GIVEN
		boolean result = false;
		Ticket ticket = new Ticket();

		Date dateTicket = new Date(System.currentTimeMillis());
		ticket.setOutTime(dateTicket);
		ticket.setPrice(12.23);
		ticket.setId(1);

		// WHEN

		result = ticketDAO.updateTicket(ticket);

		// THEN
		// la fonction doit renvoyer true quand passe normalement, quand ne génère pas
		// une exception
		assertEquals(true, result);

	}
	
	@DisplayName("Test de mise à jour d'un ticket en BDD qui plante")
	@Test
	public void testUpdateTicket_quiplante() {

		// GIVEN
		boolean result = false;
		Ticket ticket = new Ticket();
//on ne renseigne pas la date fin pour que ça renvoie false
//		Date dateTicket = new Date(System.currentTimeMillis());
//		ticket.setOutTime(dateTicket);
		ticket.setPrice(12.23);
		ticket.setId(1);

		// WHEN

		result = ticketDAO.updateTicket(ticket);

		// THEN
		// la fonction doit renvoyer true quand passe normalement, quand ne génère pas
		// une exception
		assertEquals(false, result);

	}
}
