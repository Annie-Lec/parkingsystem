package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.sql.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		dataBasePrepareService.clearDataBaseEntries();
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);

		
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		// TODO: check that a ticket is actualy saved in DB and Parking table is updated  with availability

		// in DB
		Ticket ticketInBDD = ticketDAO.getTicket("ABCDEF");

		assertNotNull(ticketInBDD);
		assertEquals("ABCDEF", ticketInBDD.getVehicleRegNumber());
		assertEquals(ParkingType.CAR, ticketInBDD.getParkingSpot().getParkingType());
		assertNotNull(ticketInBDD.getInTime());
		// lors de la phase d'entrée, l'horaire de sortie n'est pas renseigné
		assertNull(ticketInBDD.getOutTime());
		// pour une voiture, la première place de parking à prendre est la numéro 1.
		// Donc la 1 vient dêtre prise, elle n'est plus libre ...available
		assertNotEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
		// sera la place 2 la prochaine libre
		assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
		assertEquals(false, ticketInBDD.getParkingSpot().isAvailable());

	}

	@Test
	public void testParkingLotExit() {
		
		// GIVEN ETANT DONNE
		
		Ticket ticket = new Ticket();
		ParkingSpot parkingSpot = new ParkingSpot (1 , ParkingType.CAR, false);
		ticket.setVehicleRegNumber("ABCDEF");
		
		// la voiture sera entrée une heure avant le calcul donc
		// on s'attend à avoir à la sortie le tarif pour une heure (60 minutes) de stationnement pour une voiture
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		
		// ticket.setOutTime; on ne met pas à jour la date de fin qui DOIT etre lancée par la méthode processExitingVehicle()
		
		ticket.setParkingSpot(parkingSpot);
		//Pour initialiser les données en base:  
		ticketDAO.saveTicket(ticket); // insère les données en base : donc on initialise les données obligatoire avant
										// l'appel de la méthode

		// WHEN
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();
		
		// TODO: check that the fare generated and out time are populated correctly in the database
		//On crée une nouvelle variable Date qui ne va être lancée en mm temps que la proc de sortie 
		//donc que l'on comparera à outtime de ticketInBDDAfterExiting 
		Date jourEtHeure = new Date(System.currentTimeMillis());
		
		
		// THEN

		Ticket ticketInBDDAfterExiting = ticketDAO.getTicket("ABCDEF");

		assertNotNull(ticketInBDDAfterExiting.getPrice());
		assertNotNull(ticketInBDDAfterExiting.getOutTime());
		//On a une heure de parking pour une voiture
		assertEquals(Math.round(Fare.CAR_RATE_PER_HOUR/100)*100, Math.round(ticketInBDDAfterExiting.getPrice()/100)*100);
		//les 2 dates sont normalement topée en mm temps : à la milli-seconde près :
		assertEquals(Math.round(jourEtHeure.getTime()/1000)*1000, Math.round(ticketInBDDAfterExiting.getOutTime().getTime()/1000)*1000);

	}

}
