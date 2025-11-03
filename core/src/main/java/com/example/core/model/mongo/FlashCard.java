package com.example.core.model.mongo;

import android.os.Parcel;
import android.os.Parcelable;

public class FlashCard implements Parcelable {

    private String front;
    private String back;

    // Construtor padrão
    public FlashCard() {}

    // Implementação Parcelable:
    protected FlashCard(Parcel in) {
        front = in.readString();
        back = in.readString();
    }

    public static final Creator<FlashCard> CREATOR = new Creator<FlashCard>() {
        @Override
        public FlashCard createFromParcel(Parcel in) {
            return new FlashCard(in);
        }

        @Override
        public FlashCard[] newArray(int size) {
            return new FlashCard[size];
        }
    };

    // Getters e Setters (Certifique-se de tê-los)
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(front);
        parcel.writeString(back);
    }
}