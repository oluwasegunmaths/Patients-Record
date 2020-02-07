package com.ease.patientsrecord.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "patient_table")
public class Patient implements Parcelable {


    public static final Creator<Patient> CREATOR = new Creator<Patient>() {
        @Override
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String sex;
    private String age;
    private String hospitalNumber;
    private String occupation;
    private String maritalStatus;
    private String addressAndPhone;
    private String religion;
    private String stateOfOrigin;
    private String diagnosis;
    private String fullClinicalDetails;
    private String firebaseKey = "";
    private boolean hasBeenUploaded;

    public Patient(int id, String name, String sex, String age, String hospitalNumber, String occupation, String maritalStatus, String addressAndPhone, String religion, String stateOfOrigin, String diagnosis, String fullClinicalDetails, boolean hasBeenUploaded, String firebaseKey) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.hospitalNumber = hospitalNumber;
        this.occupation = occupation;
        this.maritalStatus = maritalStatus;
        this.addressAndPhone = addressAndPhone;
        this.religion = religion;
        this.stateOfOrigin = stateOfOrigin;
        this.diagnosis = diagnosis;
        this.fullClinicalDetails = fullClinicalDetails;
        this.hasBeenUploaded = hasBeenUploaded;
        this.firebaseKey = firebaseKey;

    }

    @Ignore

    public Patient(String name, String sex, String age, String hospitalNumber, String occupation, String maritalStatus, String addressAndPhone, String religion, String stateOfOrigin, String diagnosis, String fullClinicalDetails, boolean hasBeenUploaded) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.hospitalNumber = hospitalNumber;
        this.occupation = occupation;
        this.maritalStatus = maritalStatus;
        this.addressAndPhone = addressAndPhone;
        this.religion = religion;
        this.stateOfOrigin = stateOfOrigin;
        this.diagnosis = diagnosis;
        this.fullClinicalDetails = fullClinicalDetails;
        this.hasBeenUploaded = hasBeenUploaded;
    }


    @Ignore
    public Patient() {
    }

    protected Patient(Parcel in) {
        id = in.readInt();
        name = in.readString();
        sex = in.readString();
        age = in.readString();
        hospitalNumber = in.readString();
        occupation = in.readString();
        maritalStatus = in.readString();
        addressAndPhone = in.readString();
        religion = in.readString();
        stateOfOrigin = in.readString();
        diagnosis = in.readString();
        fullClinicalDetails = in.readString();
        firebaseKey = in.readString();
        hasBeenUploaded = in.readByte() != 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHospitalNumber() {
        return hospitalNumber;
    }

    public void setHospitalNumber(String hospitalNumber) {
        this.hospitalNumber = hospitalNumber;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getAddressAndPhone() {
        return addressAndPhone;
    }

    public void setAddressAndPhone(String addressAndPhone) {
        this.addressAndPhone = addressAndPhone;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getStateOfOrigin() {
        return stateOfOrigin;
    }

    public void setStateOfOrigin(String stateOfOrigin) {
        this.stateOfOrigin = stateOfOrigin;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getFullClinicalDetails() {
        return fullClinicalDetails;
    }

    public void setFullClinicalDetails(String fullClinicalDetails) {
        this.fullClinicalDetails = fullClinicalDetails;
    }

    public boolean isHasBeenUploaded() {
        return hasBeenUploaded;
    }

    public void setHasBeenUploaded(boolean hasBeenUploaded) {
        this.hasBeenUploaded = hasBeenUploaded;
    }


    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(sex);
        dest.writeString(age);
        dest.writeString(hospitalNumber);
        dest.writeString(occupation);
        dest.writeString(maritalStatus);
        dest.writeString(addressAndPhone);
        dest.writeString(religion);
        dest.writeString(stateOfOrigin);
        dest.writeString(diagnosis);
        dest.writeString(fullClinicalDetails);
        dest.writeString(firebaseKey);
        dest.writeByte((byte) (hasBeenUploaded ? 1 : 0));
    }
}
