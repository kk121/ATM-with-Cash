package com.paisewalaatm.paisewalaatm;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by krishna on 14/11/16.
 */

public class AtmResponse {
    @SerializedName("AtmObject")
    public List<AtmObject> AtmObjectList;

    public static class AtmObject {
        @SerializedName("bankName")
        public String bankName;
        @SerializedName("time")
        public String time;
        @SerializedName("location")
        public String location;

        public AtmObject(String bankName, String time, String location) {
            this.bankName = bankName;
            this.time = time;
            this.location = location;
        }

        @Override
        public String toString() {
            return "AtmObject{" +
                    "bankName='" + bankName + '\'' +
                    ", time='" + time + '\'' +
                    ", location='" + location + '\'' +
                    '}';
        }
    }
}
