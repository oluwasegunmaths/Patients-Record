package com.ease.patientsrecord.PatientsList;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ease.patientsrecord.R;
import com.ease.patientsrecord.data.Patient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PatientsListAdapter extends RecyclerView.Adapter<PatientsListAdapter.ViewHolder> {

    private final Context context;
    private final ItemClickListener itemClickListener;
    private List<Patient> patientList;
    private String searchText;

    PatientsListAdapter(Context c, ItemClickListener ClickListener) {
        context = c;
        this.itemClickListener = ClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.patient_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.name.setText(patientList.get(position).getName());
        holder.diagnosis.setText(patientList.get(position).getDiagnosis());
        holder.hospitalNumber.setText(patientList.get(position).getHospitalNumber());
        if (searchText != null && !searchText.isEmpty()) {
            //this gives a yellow background to any occurrences of the searchText present in the views
            highlightString(holder.name);
            highlightString(holder.diagnosis);
            highlightString(holder.hospitalNumber);


        }

    }

    @Override
    public int getItemCount() {
        if (null == patientList) return 0;

        return patientList.size();
    }

    void setPatients(List<Patient> patients) {
        patientList = patients;
        notifyDataSetChanged();
    }

    void setSearchText(String searchString) {
        searchText = searchString;
    }

    private void highlightString(TextView textView) {
//Get the text from text view and create a spannable string

        SpannableString spannableString = new SpannableString(textView.getText());
//Get the previous spans and remove them
        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);

        for (BackgroundColorSpan span : backgroundSpans) {
            spannableString.removeSpan(span);
        }

//Search for all occurrences of the keyword in the string
        String ss = spannableString.toString();
        int indexOfKeyword = ss.toLowerCase().indexOf(searchText.toLowerCase());

        while (indexOfKeyword >= 0) {
            //Create a background yellow color span on the keyword

            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + searchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            //Get the next index of the keyword
            indexOfKeyword = ss.toLowerCase().indexOf(searchText.toLowerCase(), indexOfKeyword + searchText.length());

        }

//Set the final text on TextView

        textView.setText(spannableString);

    }

    public interface ItemClickListener {
        void onItemClick(Patient patient);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.diagnosis_of_patient)

        TextView diagnosis;
        @BindView(R.id.hospital_number_of_patient)

        TextView hospitalNumber;

        ViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(patientList.get(getAdapterPosition()));
        }
    }
}
