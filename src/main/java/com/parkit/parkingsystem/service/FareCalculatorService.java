package com.parkit.parkingsystem.service;

import java.time.temporal.ChronoUnit;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}
		//Duree du parking : difference entre deux Times en minutes (nombre base 01/01/1970 : nombre de minutes écoulees depuis le 01011970) et non pas 2 heures sans ref de date
		float durationInMinute = ChronoUnit.MINUTES.between(ticket.getInTime().toInstant(),
				ticket.getOutTime().toInstant());
		float durationInHour = durationInMinute / 60;

		//Fonctionnalite : 30 premieres minutes gratuites
		if (durationInMinute <= 30) {
			ticket.setPrice(0);
		} else {

			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(durationInHour * Fare.CAR_RATE_PER_HOUR);
				break;
			}
			case BIKE: {
				ticket.setPrice(durationInHour * Fare.BIKE_RATE_PER_HOUR);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		}
	}
}