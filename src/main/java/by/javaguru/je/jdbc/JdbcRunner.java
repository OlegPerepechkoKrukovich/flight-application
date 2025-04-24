package by.javaguru.je.jdbc;

import by.javaguru.je.jdbc.utils.ConnectionManager;


import java.sql.SQLException;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {
        String sql = """
                SELECT * FROM ticket;
                """;
        try (var connection = ConnectionManager.get(); var statement = connection.createStatement()) {
//            System.out.println(statement.executeUpdate(sql));
              var result = statement.executeQuery(sql);
              while (result.next()) {
                  System.out.println(result.getLong("id"));
                  System.out.println(result.getString("passenger_name"));
                  System.out.println("-----------------");
              }
        }


    }
}
