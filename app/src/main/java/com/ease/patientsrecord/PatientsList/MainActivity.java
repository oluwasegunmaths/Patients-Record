package com.ease.patientsrecord.PatientsList;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ease.patientsrecord.AppExecutors;
import com.ease.patientsrecord.R;
import com.ease.patientsrecord.addNewOrEditPatient.NewOrEditPatientActivity;
import com.ease.patientsrecord.backgroundSync.SyncUtilities;
import com.ease.patientsrecord.data.AppDatabase;
import com.ease.patientsrecord.data.Patient;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PatientsListAdapter.ItemClickListener {
    public static final String PARCEL_TO_INTENT = "parcel to intent";
    private static final int RC_SIGN_IN = 100;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.empty_textView)
    TextView emptyTextView;
    @BindView(R.id.toolbar_edittext)
    EditText toolbarEditText;
    @BindView(R.id.toolbar_main)
    Toolbar toolbar;
    private PatientsListAdapter adapter;
    private AppDatabase mDb;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference patientsDR;
    private PatientsListViewModel viewModel;
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String searchString = toolbarEditText.getText().toString().trim();
            viewModel.searchEditTextString = searchString;
            adapter.setSearchText(searchString);
            viewModel.search(searchString);
            viewModel.searchedListLiveData.observe(MainActivity.this, patients -> {
                if (patients != null && !patients.isEmpty()) {
                    recycler.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                    adapter.setPatients(patients);
                } else {
                    recycler.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText(getString(R.string.no_patients_match_your_search));
                }
            });
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private boolean notAPreviousUser;
    private Observer<? super List<Patient>> observer = new Observer<List<Patient>>() {
        @Override
        public void onChanged(@Nullable List<Patient> patients) {
            if (patients == null || patients.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText(getString(R.string.no_stored_patients_yet));
            } else {
                notAPreviousUser = false;
                emptyTextView.setVisibility(View.GONE);
                adapter.setPatients(patients);
            }
        }
    };

    public static boolean isThereConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initFirebase();
        viewModel = new ViewModelProvider(this).get(PatientsListViewModel.class);
        setSupportActionBar(toolbar);
        setUpRecycler();
        initDatabase();
        observe();
        SyncUtilities.schedulePatientSync(this);
        addToolBarEditTextObserver();
    }

    private void addToolBarEditTextObserver() {
        viewModel.searchEditTextIsVisible.observe(this, aBoolean -> {
            if (aBoolean) {
                toolbarEditText.setVisibility(View.VISIBLE);
                toolbarEditText.setText(viewModel.searchEditTextString);
                viewModel.search(viewModel.searchEditTextString);
                toolbarEditText.addTextChangedListener(textWatcher);
                removeObserver();


                viewModel.searchedListLiveData.observe(MainActivity.this, patients -> {

                    if (patients != null && !patients.isEmpty()) {

                        recycler.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                        adapter.setPatients(patients);

                    } else {
                        recycler.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText(MainActivity.this.getText(R.string.no_patients_match_your_search));


                    }
                });
            } else {
                toolbarEditText.removeTextChangedListener(textWatcher);

                toolbarEditText.setVisibility(View.GONE);
                recycler.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
                observe();


            }
        });
    }

    // this method is only called once after initial log in
    //this method is to download any previously stored patients the user might have stored but got lost due to un-installation of this app or loss of local data
    private void attachChatDatabaseReadListener() {
        ChildEventListener mChatChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                final Patient patient = dataSnapshot.getValue(Patient.class);
                if (patient != null) {
                    notAPreviousUser = false;
                    AppExecutors.getInstance().diskIO().execute(() -> mDb.patientDao().upsert(patient));
                }
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase database", databaseError.getMessage());
            }
        };

        patientsDR.addChildEventListener(mChatChildEventListener);

        patientsDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText(getString(R.string.seems_you_have_never_used_this_app));
                    notAPreviousUser = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase database", databaseError.getMessage());

            }
        });

    }

    private void initFirebase() {

        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(false).setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build())).build(), RC_SIGN_IN);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                if (mFirebaseAuth.getCurrentUser() != null) {
                    //this gives every user a unique node on the fireBase tree according to the user id
                    patientsDR = mFirebaseDatabase.getReference().child("patients").child(mFirebaseAuth.getCurrentUser().getUid());
                    attachChatDatabaseReadListener();
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (isThereConnection(this)) {
                    Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                } else {
                    Toast.makeText(this, "Network required to sign in on first launch", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();


                }
            }
        }

    }

    private void initDatabase() {
        mDb = AppDatabase.getInstance(getApplicationContext());
    }

    private void setUpRecycler() {
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientsListAdapter(this, this);
        recycler.setAdapter(adapter);
    }

    @Override
    public void onItemClick(Patient patient) {
        Intent intent = new Intent(this, NewOrEditPatientActivity.class);
        intent.putExtra(PARCEL_TO_INTENT, patient);
        startActivity(intent);

    }

    public void goToAddNewPatient(View view) {
        Intent intent = new Intent(this, NewOrEditPatientActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (viewModel.searchEditTextIsVisible != null && viewModel.searchEditTextIsVisible.getValue()) {
            menu.findItem(R.id.action_search).setIcon(R.drawable.ic_up);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (notAPreviousUser) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.action_search:

                if (viewModel.searchEditTextIsVisible != null && !viewModel.searchEditTextIsVisible.getValue()) {
                    item.setIcon(R.drawable.ic_up);
                    viewModel.searchEditTextIsVisible.setValue(true);
                    viewModel.editTextHasWatcher = true;
                } else {
                    item.setIcon(R.drawable.ic_search);
                    recycler.setVisibility(View.VISIBLE);
                    toolbarEditText.setText("");
                    viewModel.searchEditTextIsVisible.setValue(false);
                }

                return true;
            case R.id.action_sort:
                removeObserver();
                viewModel.changePatientsSortOrder();
                observe();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void observe() {
        viewModel.listLiveData.observe(this, observer);
    }

    private void removeObserver() {
        viewModel.listLiveData.removeObserver(observer);
    }
}
