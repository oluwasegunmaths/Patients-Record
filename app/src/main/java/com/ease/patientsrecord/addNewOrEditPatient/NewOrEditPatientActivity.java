package com.ease.patientsrecord.addNewOrEditPatient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ease.patientsrecord.AppExecutors;
import com.ease.patientsrecord.PatientsList.MainActivity;
import com.ease.patientsrecord.R;
import com.ease.patientsrecord.data.AppDatabase;
import com.ease.patientsrecord.data.Patient;
import com.ease.patientsrecord.widget.WidgetUpdateService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewOrEditPatientActivity extends AppCompatActivity {
    @BindView(R.id.nameOfPatient)
    EditText nameEditText;
    @BindView(R.id.sex)

    EditText sexEditText;
    @BindView(R.id.age)

    EditText ageEditText;
    @BindView(R.id.hospital_number_of_patient)

    EditText hospitalNoEditText;
    @BindView(R.id.occupation)

    EditText occupationEditText;
    @BindView(R.id.marital_status)

    EditText maritalStatusEditText;
    @BindView(R.id.address)

    EditText addressText;
    @BindView(R.id.religion)

    EditText religionEditText;
    @BindView(R.id.state_of_origin)

    EditText stateEditText;
    @BindView(R.id.diagnosis_of_patient)

    EditText diagnosisEditText;
    @BindView(R.id.details)

    EditText clinicalDetailsEditText;
    private Patient patientToBeEdited;
    private AppDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_or_edit_patient);
        ButterKnife.bind(this);

        initDatabase();
        if (getIntent().hasExtra(MainActivity.PARCEL_TO_INTENT)) {
            patientToBeEdited = getIntent().getParcelableExtra(MainActivity.PARCEL_TO_INTENT);
            bindDataToEditTexts();
            setTitle(getString(R.string.edit_patient));

        } else {
            setTitle(getString(R.string.add_new_patient));
        }
    }

    private void initDatabase() {
        mDb = AppDatabase.getInstance(getApplicationContext());
    }

    private void bindDataToEditTexts() {
        nameEditText.setText(patientToBeEdited.getName());
        sexEditText.setText(patientToBeEdited.getSex());
        ageEditText.setText(patientToBeEdited.getAge());
        hospitalNoEditText.setText(patientToBeEdited.getHospitalNumber());
        occupationEditText.setText(patientToBeEdited.getOccupation());
        maritalStatusEditText.setText(patientToBeEdited.getMaritalStatus());
        addressText.setText(patientToBeEdited.getAddressAndPhone());
        religionEditText.setText(patientToBeEdited.getReligion());
        stateEditText.setText(patientToBeEdited.getStateOfOrigin());
        diagnosisEditText.setText(patientToBeEdited.getDiagnosis());
        clinicalDetailsEditText.setText(patientToBeEdited.getFullClinicalDetails());

    }

    public void savePatient(View view) {
        if (nameEditText.getText().toString().isEmpty() || sexEditText.getText().toString().isEmpty() || ageEditText.getText().toString().isEmpty() || hospitalNoEditText.getText().toString().isEmpty() || occupationEditText.getText().toString().isEmpty() || maritalStatusEditText.getText().toString().isEmpty() || addressText.getText().toString().isEmpty() || religionEditText.getText().toString().isEmpty() || stateEditText.getText().toString().isEmpty() || diagnosisEditText.getText().toString().isEmpty() || clinicalDetailsEditText.getText().toString().isEmpty()) {

            showIncompletePatientDialog();

        } else {

            AppExecutors.getInstance().diskIO().execute(() -> {
                if (patientToBeEdited != null) {
                    int id = patientToBeEdited.getId();
                    mDb.patientDao().upsert(new Patient(id, nameEditText.getText().toString(), sexEditText.getText().toString(), ageEditText.getText().toString(), hospitalNoEditText.getText().toString(), occupationEditText.getText().toString(), maritalStatusEditText.getText().toString(), addressText.getText().toString(), religionEditText.getText().toString(), stateEditText.getText().toString(), diagnosisEditText.getText().toString(), clinicalDetailsEditText.getText().toString(), false, patientToBeEdited.getFirebaseKey()));
                } else {
                    mDb.patientDao().upsert(new Patient(nameEditText.getText().toString(), sexEditText.getText().toString(), ageEditText.getText().toString(), hospitalNoEditText.getText().toString(), occupationEditText.getText().toString(), maritalStatusEditText.getText().toString(), addressText.getText().toString(), religionEditText.getText().toString(), stateEditText.getText().toString(), diagnosisEditText.getText().toString(), clinicalDetailsEditText.getText().toString(), false));
                }
                updateSharedPreferencesToStoreDetailsOfThisPatientSoAsToShowInWidget();
                WidgetUpdateService.startActionUpdatePatientDetails(NewOrEditPatientActivity.this, clinicalDetailsEditText.getText().toString());

                NewOrEditPatientActivity.this.finish();


            });


        }

    }

    private void updateSharedPreferencesToStoreDetailsOfThisPatientSoAsToShowInWidget() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewOrEditPatientActivity.this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(WidgetUpdateService.PATIENT_DETAILS, clinicalDetailsEditText.getText().toString());


        preferencesEditor.apply();


    }

    private void showIncompletePatientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(NewOrEditPatientActivity.this.getString(R.string.please_fill_in_all_patients_details));
        builder.setNegativeButton(NewOrEditPatientActivity.this.getString(R.string.ok), (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (patientToBeEdited != null) {
            getMenuInflater().inflate(R.menu.menu_detail, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteDialog();

                return true;
            case R.id.action_share:
                if (!nameEditText.getText().toString().isEmpty() && !clinicalDetailsEditText.getText().toString().isEmpty()) {
                    String intentContent = String.format("%s%s%s%s", NewOrEditPatientActivity.this.getString(R.string.deatails_of), nameEditText.getText().toString(), NewOrEditPatientActivity.this.getString(R.string.are), clinicalDetailsEditText.getText().toString());
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/html");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, intentContent);
                    startActivity(Intent.createChooser(shareIntent, NewOrEditPatientActivity.this.getString(R.string.select_an_app_to_share_with)));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(NewOrEditPatientActivity.this.getString(R.string.are_you_sure_you_want_to_delete_this_patient));
        builder.setNegativeButton(NewOrEditPatientActivity.this.getString(R.string.cancel), (dialog, id) -> {
            // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the pet.
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(NewOrEditPatientActivity.this.getString(R.string.delete), (dialogInterface, i) -> AppExecutors.getInstance().diskIO().execute(() -> {
            int id = patientToBeEdited.getId();
            mDb.patientDao().delete(id);


            NewOrEditPatientActivity.this.finish();


        }));
        builder.create().show();

    }
}
