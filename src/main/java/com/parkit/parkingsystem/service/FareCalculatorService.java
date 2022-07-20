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
		double reduction;

		//Fonctionnalite : 30 premieres minutes gratuites (entrees en constante au cas ou)
		if (durationInMinute <= Fare.NUMBER_OF_MINUTES_OF_FREE_PARK_DURATION) {
			ticket.setPrice(0);
		} else {
			
			//Reduction de 5% pour les usagers deja venu au parking
			reduction = ticket.isARecurrentUser() ? Fare.PCT_DISCOUNT_FOR_CURRENT_USER : 0.0;
			

			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(durationInHour * Fare.CAR_RATE_PER_HOUR * (1-reduction));
				break;
			}
			case BIKE: {
				ticket.setPrice(durationInHour * Fare.BIKE_RATE_PER_HOUR * (1-reduction));
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		}
	}
}