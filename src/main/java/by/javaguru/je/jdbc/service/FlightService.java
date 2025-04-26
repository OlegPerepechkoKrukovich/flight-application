package by.javaguru.je.jdbc.service;

import by.javaguru.je.jdbc.dao.FlightDao;
import by.javaguru.je.jdbc.dto.FlightDto;

import java.util.List;
import java.util.stream.Collectors;

public class FlightService {
    private static final FlightService INSTANCE = new FlightService();
    private final FlightDao flightDao = FlightDao.getInstance();

    public List<FlightDto> findAll() {
        return flightDao.findAll().stream().map(flight ->
                new FlightDto(flight.getId(),
                        "%s - %s - %s".formatted(
                                flight.getArrivalAirportCode(),
                                flight.getDepartureAirportCode(),
                                flight.getStatus()
                        ))).collect(Collectors.toList());
    }

    private FlightService() {
    }

    public static FlightService getInstance() {
        return INSTANCE;
    }
}
