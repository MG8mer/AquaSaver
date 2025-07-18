package com.example.aquasaver.dao;

// import important room db libraries

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Import Reports class
import com.example.aquasaver.model.Reports;

// Import list
import java.util.List;

@Dao
public interface ReportsDao
{
    /*

     Insert queries

     */

    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert one water report
    long insertReport(Reports report); // returns ID of newly added report

    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert many water reports
    void insertAllReports(List<Reports> report);

    // Update queries
    @Update
    void updateReport(Reports report); // updates a specific report

    /*

    Delete queries

     */
    @Delete
    void deleteReport(Reports report); // deletes a specific report

    @Query("DELETE FROM reports WHERE user_email = :userEmail")
    int deleteUserReports(String userEmail); // deletes all user reports and return # rows deleted

    /*

    Select queries

     */

    @Query("SELECT * FROM reports WHERE id = :reportId LIMIT 1")
    Reports getReportById(int reportId); // Returns a specific report by Id

    @Query("SELECT * FROM reports WHERE user_email = :userEmail ORDER BY start_date DESC")
    List<Reports> getUserReports(String userEmail); // Returns all reports of a particular user

    @Query("SELECT * FROM reports WHERE user_email = :userEmail AND start_date <= :currentDate AND end_date >= :currentDate")
    List<Reports> getUserReportsByTime(String userEmail, long currentDate); // Returns all reports of a particular user at a specific time
}