package com.ease.patientsrecord.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Patient patient);

    @Query("SELECT * from patient_table WHERE hasBeenUploaded=:bool")
    List<Patient> getUnoploadedItems(boolean bool);

    @Query("DELETE FROM patient_table WHERE id=:id")
    void delete(int id);

    @Query("SELECT * FROM patient_table ORDER BY Id DESC")
    LiveData<List<Patient>> getAllPatients();

    @Query("SELECT * FROM patient_table ORDER BY name ASC")
    LiveData<List<Patient>> getAllPatientsOrderByName();

    @Query("SELECT * FROM patient_table where name like :searched OR diagnosis like :searched OR hospitalNumber like :searched ORDER BY name")
    LiveData<List<Patient>> loadSearchedPatients(String searched);

    @Query("UPDATE   patient_table SET hasBeenUploaded =:bool, firebaseKey=:firebaseKey WHERE id=:id")
    int syncDatabaseToShowPatientWithIdIsOnServer(boolean bool, String firebaseKey, int id);

    @Query("UPDATE   patient_table SET hasBeenUploaded =:bool WHERE id=:id")
    void syncDatabaseToShowPatientWithIdIsOnServer(boolean bool, int id);

}
