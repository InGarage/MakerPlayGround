/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.helper;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by tanyagorn on 9/8/2017.
 */
public class SingletonConnectDB {
    private static SingletonConnectDB INSTANCE = null;
    private static final String azureConnectionString = "jdbc:sqlserver://makerplayground.database.windows.net:1433;database=makerplayground-log;user=makerplayground;password=Visionear#2017;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

    private Connection sqliteConnection = null;
    private String uuid = "";
    private Executor executor = Executors.newSingleThreadExecutor();
    private List<String> tableName = new ArrayList<>(Arrays.asList("WiringDiagram", "UtilTools", "UploadClick", "Tutorial",
                            "Launching", "Graph", "Error",
                            "ConfigDevice", "EditSceneName" ,"CopyCode", "ClickorDragBeginButton",
                            "ClickURL", "ChangeDeviceName", "Canvas", "AddorDelDevice"));

    private Timer timer;

    private SingletonConnectDB() {

        try {
            sqliteConnection = DriverManager.getConnection("jdbc:sqlite::resource:log.db");

            // read user's uuid from the database
            ResultSet rs = sqliteConnection.createStatement().executeQuery("SELECT uuid FROM UUID");
            if (rs.next()) {
                uuid = rs.getString("uuid");
            } else {
                uuid = UUID.randomUUID().toString().replace("-", "");
                sqliteConnection.createStatement().execute("INSERT INTO UUID VALUES (\"" + uuid + "\")");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Write to azure every 10 minutes
//        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                executor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//                            // loop through every table
//                            for (String s : tableName) {
//                                try (Statement stmt = sqliteConnection.createStatement()) {
//                                    // Get data from the source table as a ResultSet.
//                                    try (ResultSet rsSourceData = stmt.executeQuery(
//                                            "SELECT * FROM " + s)) {
//                                        sqliteConnection.createStatement().execute("Delete FROM " + s);
//                                        // Open the destination connection.
//                                        try (Connection azureConnection =
//                                                     DriverManager.getConnection(azureConnectionString)) {
//                                            try (SQLServerBulkCopy bulkCopy =
//                                                         new SQLServerBulkCopy(azureConnection)) {
//                                                bulkCopy.setDestinationTableName(s);
//                                                // Write from the source to the destination.
//                                                //bulkCopy.writeToServer(rsSourceData);
//                                            }
//                                        }
//                                    } catch (Exception e ) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        } catch (Exception e ) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        }, 10000, 10000);
    }

    public void close() {
        //timer.cancel();
//        executor.execute(() -> {
//            if (sqliteConnection != null) {
//                try {
//                    sqliteConnection.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    public String getUuid() {
        return uuid;
    }

    public static SingletonConnectDB getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new SingletonConnectDB();
        }

        return INSTANCE;
    }

    public void execute(String command) {
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    sqliteConnection.createStatement().execute(command);
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }
}
