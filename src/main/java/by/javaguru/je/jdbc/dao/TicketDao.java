package by.javaguru.je.jdbc.dao;

import by.javaguru.je.jdbc.dto.TicketFilter;
import by.javaguru.je.jdbc.entity.Flight;
import by.javaguru.je.jdbc.entity.FlightStatus;
import by.javaguru.je.jdbc.entity.Ticket;
import by.javaguru.je.jdbc.exception.DaoException;
import by.javaguru.je.jdbc.utils.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TicketDao implements Dao<Long, Ticket> {
    private final static TicketDao INSTANCE = new TicketDao();
    private final static FlightDao flightDao = FlightDao.getInstance();

    private final static String SAVE_SQL = """
            INSERT INTO ticket (passport_no, passenger_name, flight_id, seat_no, cost)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final static String DELETE_SQL = """
            DELETE FROM ticket
            WHERE id = ?
            """;

    private final static String FIND_ALL_SQL = """
            SELECT t.id, t.passport_no, t.passenger_name, t.flight_id, t.seat_no, t.cost,
            f.flight_no, f.departure_date, f.departure_airport_code, f.arrival_date, f.arrival_airport_code, f.aircraft_id, f.status
            FROM ticket t
            JOIN flight f ON t.flight_id = f.id
            """;
    private final static String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE t.id = ?
            """;

    private final static String UPDATE_SQL = """
            UPDATE ticket
            SET
                passport_no = ?,
                passenger_name = ?,
                flight_id = ?,
                seat_no = ?,
                cost = ?
            WHERE id = ?
            """;

    private final static String FIND_ALL_BY_FLIGHT_ID_SQL = FIND_BY_ID_SQL + """
            WHERE flight_id = ?
            """;
    public List<Ticket> findAllByFlightId(Long id) throws DaoException {
        try (var connection = ConnectionManager.get();
        var statement = connection.prepareStatement(FIND_ALL_BY_FLIGHT_ID_SQL);) {
            List<Ticket> ticketsByFlight = new ArrayList<>();
            statement.setLong(1, id);
            var result = statement.executeQuery();
            while (result.next()) {
                ticketsByFlight.add(buildTicket(result));
            }
            return ticketsByFlight;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public List<Ticket> findALl(TicketFilter filter) {
        List<Object> params = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.passengerName() != null) {
            params.add(filter.passengerName());
            whereSql.add("passenger_name = ?");
        }
        if (filter.seatNo() != null) {
            params.add("%" + filter.seatNo() + "%");
            whereSql.add("seat_no like ?");
        }
        params.add(filter.limit());
        params.add(filter.offset());
        var where = whereSql.stream().collect(Collectors.joining(
                " AND ",
                params.size() > 2 ? " WHERE " : "",
                " LIMIT  ? OFFSET ?"
        ));

        String sql = FIND_ALL_SQL + where;

        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(sql)) {
            List<Ticket> tickets = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            System.out.println(statement);
            var result = statement.executeQuery();
            while (result.next()) {
                tickets.add(buildTicket(result));
            }
            return tickets;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(Ticket ticket) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, ticket.getPassportNo());
            statement.setString(2, ticket.getPassengerName());
            statement.setLong(3, ticket.getFlight().getId());
            statement.setString(4, ticket.getSeatNo());
            statement.setBigDecimal(5, ticket.getCost());
            statement.setLong(6, ticket.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Ticket> findById(Long id) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, id);
            var result = statement.executeQuery();
            Ticket ticket = null;
            if (result.next()) {
                ticket = buildTicket(result);
            }
            return Optional.ofNullable(ticket);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static Ticket buildTicket(ResultSet result) throws SQLException {
        return new Ticket(result.getLong("id"),
                result.getString("passport_no"),
                result.getString("passenger_name"),
                flightDao.findById(
                        result.getLong("flight_id"),
                        result.getStatement().getConnection()

                        ).orElse(null),
                result.getString("seat_no"),
                result.getBigDecimal("cost")
        );
    }

    public List<Ticket> findAll() {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<Ticket> tickets = new ArrayList<>();
            var result = statement.executeQuery();
            while (result.next()) {
                tickets.add(buildTicket(result));
            }
            return tickets;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ticket save(Ticket ticket) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, ticket.getPassportNo());
            statement.setString(2, ticket.getPassengerName());
            statement.setLong(3, ticket.getFlight().getId());
            statement.setString(4, ticket.getSeatNo());
            statement.setBigDecimal(5, ticket.getCost());

            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            if (keys.next()) {
                ticket.setId(keys.getLong("id"));
            }

            return ticket;
        } catch (SQLException e) {
            throw new DaoException(e);
        }


    }

    public boolean delete(Long id) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static TicketDao getInstance() {
        return INSTANCE;
    }

    private TicketDao() {

    }
}
