package by.javaguru.je.jdbc.service;

import by.javaguru.je.jdbc.dao.TicketDao;
import by.javaguru.je.jdbc.dto.TicketDto;

import java.util.List;
import java.util.stream.Collectors;

public class TicketService {
    private final static TicketService INSTANCE = new TicketService();
    private final static TicketDao ticketDao = TicketDao.getInstance();
    public List<TicketDto> findAllByFlightId(Long flightId) {
        return ticketDao.findAllByFlightId(flightId).stream().map(ticket ->
                new TicketDto(ticket.getId(), ticket.getFlight().getId(), ticket.getSeatNo())
        ).collect(Collectors.toList());
    }

    private TicketService() {
    }
    public static TicketService getInstance() {
        return INSTANCE;
    }
}
