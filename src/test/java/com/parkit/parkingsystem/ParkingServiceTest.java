package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtilMock;
	@Mock
	private static ParkingSpotDAO parkingSpotDAOMock;
	@Mock
	private static TicketDAO ticketDAOMock;

	@BeforeAll
	private static void setUp() throws Exception {
		dataBasePrepareService = new DataBasePrepareService();
		// On nettoie la bdd
		dataBasePrepareService.clearDataBaseEntries();

	}

	@BeforeEach
	private void setUpPerTest() {
		parkingService = new ParkingService(inputReaderUtilMock, parkingSpotDAOMock, ticketDAOMock);
	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {
		// GIVEN pour obtenir le NextParkingNumberIfAvailable on se sert des méthodes :
		// getVehichleType() de la classe à tester faisant appel à
		// inputReaderUtil.readSelection()
		// et getNextAvalableSlot() de parkingSpotDAO
		when(inputReaderUtilMock.readSelection()).thenReturn(1);
		when(parkingSpotDAOMock.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);

		// WHEN
		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		// THEN
		assertNotNull(parkingSpot);
	}

	@Test
	public void testGetNextParkingNumberIfAvailable_returnNoParkingSpot() {
		// GIVEN getNextAvalableSlot() de parkingSpotDAO retourne -1 ou 0 (pas de place)
		ParkingSpot parkingSpot = null;
		when(inputReaderUtilMock.readSelection()).thenReturn(1);
		when(parkingSpotDAOMock.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

		// WHEN
		try {
			parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		} catch (Exception e) {
			// même message que dans le throw new Exception("Error fetching parking number
			// from DB. Parking slots might be full");
			String msgErrorException = "Error fetching parking number from DB. Parking slots might be full";
			// THEN
			assertEquals(msgErrorException, e.getMessage());
			assertNull(parkingSpot);

		}
	}

	@Test
	public void testProcessIncomingVehicle() throws Exception {

		// GIVEN : il faut un parkingspot et un ticket correspondant (normalement
		// appelle getNextAvalaibleParkingspot
		when(inputReaderUtilMock.readSelection()).thenReturn(1);
		when(inputReaderUtilMock.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(parkingSpotDAOMock.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		Ticket ticket = new Ticket();
		ticket.setParkingSpot(parkingSpot);

		// WHEN : quand on appelle la methode processIncomingVehicle
		parkingService.processIncomingVehicle();

		// THEN : alors on verifie que les méthodes constituantes sont appelées au moins
		// 1 fois : on est bien entré dans la méthose
		verify(parkingSpotDAOMock, times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAOMock, times(1)).saveTicket(any(Ticket.class));
		verify(ticketDAOMock, times(1)).getNumberOfTicketAlreadyPaid(anyString());

	}

	@Test
	public void testProcessExitingVehicle() throws Exception {
		// GIVEN
		when(inputReaderUtilMock.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");
		when(ticketDAOMock.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAOMock.updateTicket(any(Ticket.class))).thenReturn(true);
		when(parkingSpotDAOMock.updateParking(any(ParkingSpot.class))).thenReturn(true);

		// WHEN
		parkingService.processExitingVehicle();

		// THEN
		verify(parkingSpotDAOMock, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}
	
	@Test
	public void testProcessExitingVehicle_quiPlanteALUpdate() throws Exception  {
		// GIVEN
		when(inputReaderUtilMock.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");
		when(ticketDAOMock.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAOMock.updateTicket(any(Ticket.class))).thenReturn(false);

		// WHEN
		parkingService.processExitingVehicle();

		// THEN
		verify(parkingSpotDAOMock, Mockito.times(0)).updateParking(any(ParkingSpot.class));
	}
	
	@Test
	public void testProcessExitingVehicle_Exception() throws Exception  {
		// GIVEN
		when(inputReaderUtilMock.readVehicleRegistrationNumber()).thenThrow(IllegalArgumentException.class);
	
		// WHEN
		parkingService.processExitingVehicle();

		// THEN
		verify(ticketDAOMock, Mockito.times(0)).getTicket(anyString());
        verify(ticketDAOMock, Mockito.times(0)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAOMock, Mockito.times(0)).updateParking(any(ParkingSpot.class));
	}


}
