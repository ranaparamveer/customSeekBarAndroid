package com.phappytech.library;

import android.os.Parcel;
import android.os.Parcelable;

public class ProgressSegment implements Parcelable {
    public int color;
    public String name;
    public float progress;

    public ProgressSegment(Parcel in) {
        color = in.readInt();
        name = in.readString();
        progress = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(color);
        dest.writeString(name);
        dest.writeFloat(progress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProgressSegment> CREATOR = new Creator<ProgressSegment>() {
        @Override
        public ProgressSegment createFromParcel(Parcel in) {
            return new ProgressSegment(in);
        }

        @Override
        public ProgressSegment[] newArray(int size) {
            return new ProgressSegment[size];
        }
    };
}
