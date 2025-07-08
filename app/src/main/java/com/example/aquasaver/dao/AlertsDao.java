package com.example.aquasaver.dao;

// import important room db libraries

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Import AlertType enum and Alerts class
import com.example.aquasaver.entities.enums.AlertType;
import com.example.aquasaver.entities.Alerts;

// Import list
import java.util.List;

@Dao
public interface AlertsDao
{
    /*

    Insert queries

     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert one alert
    long insertAlert(Alerts alert); // returns ID of newly added alert

    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert many alerts
    void insertAllAlerts(List<Alerts> alert); // nothing returned from this

    // Update queries
    @Update
    void updateAlert(Alerts alert); // update a specific alert

    /*

    Delete queries

     */
    @Delete
    void deleteAlert(Alerts alert); // delete a specific alert

    @Query("DELETE FROM alerts WHERE location = :location")
    int deleteAlertsForLocation(String location); // Deletes alerts based on location and returns # rows deleted

    @Query("DELETE FROM alerts WHERE alert_type = :alert_type")
    int deleteAlertsByType(AlertType alert_type); // Deletes alerts based on alert type and returns # rows deleted

    @Query("DELETE FROM alerts WHERE location = :location AND alert_type = :alert_type") // Corrected line
    int deleteAlertsByLocationAndType(AlertType alert_type, String location); // Deletes alerts based on location and alert type and returns # rows deleted
    /*

    Select queries

     */
    @Query("SELECT * FROM alerts")
    List<Alerts> getAllAlerts(); // returns all alerts

    @Query("SELECT * FROM alerts WHERE id = :alertId LIMIT 1")
    Alerts getAlertById(int alertId); // returns a specific alert

    @Query("SELECT * FROM alerts WHERE location = :location")
    List<Alerts> getAlertsByLocation(String location); // returns a list of alerts for a specific location

    @Query("SELECT * FROM alerts WHERE location = :location AND start_time <= :currentTime AND end_time >= :currentTime")
    List<Alerts> getActiveAlertsByLocation(String location, long currentTime); // returns a list of ACTIVE alerts for a specific location

    @Query("SELECT * FROM alerts WHERE alert_type = :alert_type")
    List<Alerts> getAlertsByType(AlertType alert_type); // returns all alerts that are of a specific type

    @Query("SELECT * FROM alerts WHERE alert_type = :alert_type AND start_time <= :currentTime AND end_time >= :currentTime")
    List<Alerts> getActiveAlertsByType(AlertType alert_type, long currentTime); // returns all ACTIVE alerts that are of a specific type

    @Query("SELECT * FROM alerts WHERE location = :location AND alert_type = :alert_type")
    List<Alerts> getAlertsByLocationAndType(String location, AlertType alert_type); // returns all alerts for a specific location of a certain type

    @Query("SELECT * FROM alerts WHERE location = :location AND alert_type = :alert_type AND start_time <= :currentTime AND end_time >= :currentTime")
    List<Alerts> getActiveAlertsByLocationAndType(String location, AlertType alert_type, long currentTime); // returns all ACTIVE alerts for a specific location of a certain type
}