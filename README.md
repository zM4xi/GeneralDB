# GeneralDB
Java - Allows to connect to 7 different types of database systems with only one connector file

> ### Oracle Database currently not working looking for a way to fix that

Simple as is sounds simply enter the login credentials & the database type for your database and its done!

```java
        GeneralDB database = new GeneralDB(GeneralDB.DBType.MONGODB, "localhost", "3306", "database", "root", "password123");
```

And to execute statments:

```java
        try {
            database.update("INSERT INTO table VALUES (?, ?)", "Mr. User", "Funny comment");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            ResultSet resultSet = database.query("SELECT DISTINCT * FROM table");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
```

