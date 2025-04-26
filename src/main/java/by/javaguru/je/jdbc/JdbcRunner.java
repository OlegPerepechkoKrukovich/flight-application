package by.javaguru.je.jdbc;

import by.javaguru.je.jdbc.dao.FlightDao;
import by.javaguru.je.jdbc.dao.TicketDao;
import by.javaguru.je.jdbc.dto.TicketFilter;



public class JdbcRunner {
    public static void main(String[] args)  {
        var ticketDao = TicketDao.getInstance();
        var filter = new TicketFilter(null, null, 5, 0);
        System.out.println(ticketDao.findALl(filter));
        System.out.println("__________________");
        var flightDao = FlightDao.getInstance();
        System.out.println(flightDao.findAll());


    }
}
