package by.javaguru.je.jdbc.dao;

import by.javaguru.je.jdbc.entity.Flight;
import by.javaguru.je.jdbc.entity.FlightStatus;
import by.javaguru.je.jdbc.exception.DaoException;
import by.javaguru.je.jdbc.utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlightDao implements Dao<Long, Flight> {
    private final static FlightDao INSTANCE = new FlightDao();

    private static final String SAVE_SQL = """
            INSERT INTO flight(flight_no, departure_date, departure_airport_code, arrival_date, arrival_airport_code, aircraft_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)""";

    private static final String FIND_ALL_SQL = """
            SELECT id, flight_no, departure_date, departure_airport_code, arrival_date, arrival_airport_code, aircraft_id, status
            FROM flight;
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE id = ?;
            """;
    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM flight WHERE id = ?;
            """;

    @Override
    public boolean update(Flight flight) {
        return false;
    }

    @Override
    public List<Flight> findAll() {
        List<Flight> flights = new ArrayList<>();

        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            var result = statement.executeQuery();
            while (result.next()) {
                flights.add(flightBuilder(result));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return flights;
    }

    @Override
    public Optional<Flight> findById(Long id) {
        try (var connection = ConnectionManager.get()){
            return findById(id, connection);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private Flight flightBuilder(ResultSet result) throws SQLException {
        return new Flight(
                result.getLong("id"),
                result.getString("flight_no"),
                result.getTimestamp("departure_date").toLocalDateTime(),
                result.getString("departure_airport_code"),
                result.getTimestamp("arrival_date").toLocalDateTime(),
                result.getString("arrival_airport_code"),
                result.getInt("aircraft_id"),
                FlightStatus.valueOf(result.getString("status"))
        );
    }


    public Optional<Flight> findById(Long id, Connection connection) {
        try (var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, id);
            var result = statement.executeQuery();

            Flight flight = null;
            if (result.next()) {
                flight = flightBuilder(result);
            }
            return Optional.ofNullable(flight);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public Flight save(Flight flight) {
        try (var connection = ConnectionManager.get();
        var statement = connection.prepareStatement(SAVE_SQL,Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, flight.getFlightNo());
            statement.setTimestamp(2, Timestamp.valueOf(flight.getDepartureDate()));
            statement.setString(3, flight.getDepartureAirportCode());
            statement.setTimestamp(4, Timestamp.valueOf(flight.getArrivalDate()));
            statement.setString(5, flight.getArrivalAirportCode());
            statement.setInt(6, flight.getAircraftId());
            statement.setString(7, flight.getStatus().name());
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            if (keys.next()) {
                flight.setId(keys.getLong("id"));
            }

            return flight;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            statement.setLong(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private FlightDao() {
    }

    public static FlightDao getInstance() {
        return INSTANCE;
    }
}
