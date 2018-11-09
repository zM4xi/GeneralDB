package database;
/*
 * MIT License
 *
 * Copyright (c) 2018 Maximilian Oswald
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import lombok.Getter;

import java.sql.*;

public class GeneralDB {

    @Getter
    private DBType dbType;
    @Getter
    private final String HOST, PORT, DATABASE, USERNAME, PASSWORD;
    @Getter
    private Connection connection;

    /**
     *
     * @param type the database system that should be used
     * @param HOST server address or localhost
     * @param PORT service port example database.MySQL Default 3306
     * @param DATABASE a database name to work at
     *
     * CAREFULLY:
     *            ● Using SQLite as type you need to give a path to the database files as database. You can use the placeholder $dir to get the working dir of the executing java instance
     *               Example: $dir/db/accounts.db goes to /path_to_instance/db/accounts.db
     *
     *            ● Using Oracle's SQL Developer you enter the SID or the serviceName as database (WARNING CURRENTLY NOT WORKING).
     *
     * @param USERNAME username with access to given database
     * @param PASSWORD username's password
     */
    public GeneralDB(DBType type, String HOST, String PORT, String DATABASE, String USERNAME, String PASSWORD) {
        this.dbType = type;
        this.HOST = HOST;
        this.PORT = PORT;
        this.DATABASE = DATABASE;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;

        try {
            if(type == DBType.ORACLE) {
                connection = DriverManager.getConnection(type.getURL(this.HOST, this.PORT, this.DATABASE, this.USERNAME, this.PASSWORD), USERNAME, PASSWORD);
            } else {
                connection = DriverManager.getConnection(type.getURL(this.HOST, this.PORT, this.DATABASE, this.USERNAME, this.PASSWORD));
            }
        } catch (SQLException e) {
            connection = null;
        }

        if(isConnected()) {
            return;
        }
    }

    public boolean isConnected() {
        return connection == null ? false : true;
    }

    public boolean addTable(String tablename, String ... parameters) throws SQLException {
        String add = "";
        /*
         * user VARCHAR(8) UNIQUE / password VARCHAR(99) / PRIMARY KEY (`user`)
         * */
        if(parameters != null) {
            for(String st : parameters) {
                add += st + ", ";
            }
            add = add.substring(0, add.length()-2);
        }
        /*
         * user VARCHAR(8) UNIQUE, password VARCHAR(99), PRIMARY KEY (`user`)
         * */
        PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablename + " (" + add + ") ENGINE = InnoDB");
        try {
            ps.executeUpdate();
            return true;
        } finally {
            ps.close();
            return false;
        }
    }

    public ResultSet query(String sql, Object ... values) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        if(values != null) {
            int a = 1;
            for (Object obj : values) {
                ps.setObject(a, obj);
                a++;
            }
        }
        ResultSet rs;
        try {
            rs = ps.executeQuery();
            return rs;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean update(String sql, Object ... values) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        try {
            if(values != null) {
                int x = 1;
                for(Object obj : values) {
                    ps.setObject(x, obj);
                    x++;
                }
            }
            ps.executeUpdate();
            return true;
        } finally {
            ps.close();
            return false;
        }
    }

    public void close() {
        try {
            if (!connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public enum DBType {

        MICROSOFTSQL("Microsoft SQL Server", "jdbc:sqlserver://$host:$port;database=$database;user=$username;password=$password"),
        MYSQL("database.MySQL", "jdbc:mysql://$host:$port/$database?user=$username&password=$password&autoReconnect=true"),
        ORACLE("NOT WORKING", "jdbc:oracle:thin:$username/$password@$host:$port:$database"),
        POSTGRESQL("PostgreSQL", "jdbc:postgresql://$host:$port/$database?user=$username&password=$password"),
        MARIADB("MariaDB", "jdbc:mariadb://$host:$port/$database?user=$username&password=$password"),
        SQLITE("SQLite", "jdbc:sqlite:$database"),
        MONGODB("MongoDB", "hdbc:mongodb://$username:$password@$host:$port/$database");

        @Getter
        private String name;
        @Getter
        private String format;

        DBType(String name, String format) {
            this.name = name;
            this.format = format;
        }

        public String getURL(String HOST, String PORT, String DATABASE, String USERNAME, String PASSWORD) {
            DATABASE = DATABASE.replace("$dir", System.getProperty("user.dir"));
            String url = getFormat().replace("$host", HOST)
                    .replace("$port", PORT)
                    .replace("$database", DATABASE)
                    .replace("$username", USERNAME)
                    .replace("$password", PASSWORD);
            return url;
        }

    }

}
