package com.parkit.parkingsystem.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;

@ExtendWith(MockitoExtension.class)
class ParkingSpotDAOTest {
	
	@Mock
	private static DataBaseTestConfig dataBaseTestConfigMock;
	@Mock
	private Connection conMock;
	@Mock
	private PreparedStatement preparedStatementMock;
	@Mock
	private ResultSet resultSetMock;
	
	//On ne mocke pas PArkingSpot car classe des données
	ParkingSpot parkingSpot;
	//On ne mocke pas parkingSpotDAO car classe à tester
	private static ParkingSpotDAO parkingSpotDAO;


	@BeforeEach
	void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfigMock;

		when(dataBaseTestConfigMock.getConnection()).thenReturn(conMock);
		when(conMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
	}
	
	@DisplayName("Test de recherche du prochain spot libre :  le Spot 2") 
	@Test
	void testGetNextAvailableSlot_SpotAvailable() throws SQLException  {
		//GIVEN
		when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
		//il y a une place libre
		when(resultSetMock.next()).thenReturn(true);
		//la place libre sera l'emplacement 2
		when(resultSetMock.getInt(1)).thenReturn(2);
		
		//WHEN
		final int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		
		//THEN
		assertEquals(2,result);
	}
	
	@DisplayName("Test de recherche du prochain spot libre :  aucune place de libre") 
	@Test
	void testGetNextAvailableSlot_SpotNotAvailable() throws SQLException  {
		//GIVEN
		when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
		//il n'y a pas de place libre !!
		when(resultSetMock.next()).thenReturn(false);
		
		//WHEN
		final int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		
		//THEN
		//par defaut dans parkingspotdao.getNextAvalableSlot, result = -1 , on n'entre pas dans la boucle rs.next()
		assertEquals(-1,result);
	}
	
	@DisplayName("Test de mise à jour du statut occupé pour une place de parking")
	@Test
	void testUpdateParking_SpotBusy() throws SQLException {
		//GIVEN
		//on initialse un Parkinspot à place 3 pour une voiture occupée
		parkingSpot = new ParkingSpot(3, ParkingType.CAR, false);
		//le executeUpdate du ps fonctionne : doit retourner 1 enreg donc
		when(preparedStatementMock.executeUpdate()).thenReturn(1);
		
		//WHEN
		final boolean result = parkingSpotDAO.updateParking(parkingSpot);
		
		//THEN
		//quand la methode passe alors renvoie true
		assertEquals(true, result);
		//pour une place de parking occupée
		assertEquals(false, parkingSpot.isAvailable());
	}
	
	@DisplayName("Test d'erreur de mise à jour du statut occupé pour une place de parking car destinée normalement à un vélo")
	@Test
	void testUpdateParking_mightGenerateError() throws SQLException {
		//GIVEN
		//on initialse un ParkinPpot à place 4 réservee aux cycle pour une voiture 
		parkingSpot = new ParkingSpot(4, ParkingType.CAR, false);
		//on devrait generer une exception
		when(preparedStatementMock.executeUpdate()).thenThrow(SQLException.class);
		
		//WHEN
		final boolean result = parkingSpotDAO.updateParking(parkingSpot);
		
		//THEN
		//quand la methode passe alors renvoie true donc quand elle ne passe pas, renvoie false
		assertEquals(false, result);
		//on verifie que l'execute update a été lancé une fois
		verify(preparedStatementMock, times(1)).executeUpdate();
	}
}
