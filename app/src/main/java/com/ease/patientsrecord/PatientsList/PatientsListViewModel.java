package com.ease.patientsrecord.PatientsList;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ease.patientsrecord.data.AppDatabase;
import com.ease.patientsrecord.data.Patient;

import java.util.List;

public class PatientsListViewModel extends AndroidViewModel {
    private final AppDatabase mDb;
    boolean editTextHasWatcher;
    LiveData<List<Patient>> listLiveData;
    MutableLiveData<Boolean> searchEditTextIsVisible = new MutableLiveData<>(false);
    String searchEditTextString = "";
    LiveData<List<Patient>> searchedListLiveData;
    private boolean isSortingByName;

    public PatientsListViewModel(Application application) {
        super(application);
        mDb = AppDatabase.getInstance(application);
        listLiveData = mDb.patientDao().getAllPatients();


    }

    void search(String string) {
        searchedListLiveData = mDb.patientDao().loadSearchedPatients("%" + string + "%");

    }

    void changePatientsSortOrder() {
        if (isSortingByName) {
            listLiveData = mDb.patientDao().getAllPatients();
            isSortingByName = false;
        } else {
            listLiveData = mDb.patientDao().getAllPatientsOrderByName();
            isSortingByName = true;
        }

    }
}
