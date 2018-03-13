package com.readboy.watch.speech;

import java.util.List;

/**
 * Created by oubin on 2018/3/10.
 */

public class ObjectA {


    /**
     * r : settings
     * o : 0663666329settings
     * data : {"mode":0,"notrack":0,"volte":0,"wifiapps":[],"nofind":0,"power":0,"nostrangercall":0,"track":420,"wifiapp":0,"dial":0}
     * v : 1
     */

    private String r;
    private String o;
    private Data data;
    private int v;

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public static class Data {
        /**
         * mode : 0
         * notrack : 0
         * volte : 0
         * wifiapps : []
         * nofind : 0
         * power : 0
         * nostrangercall : 0
         * track : 420
         * wifiapp : 0
         * dial : 0
         */

        private int mode;
        private int notrack;
        private int volte;
        private int nofind;
        private int power;
        private int nostrangercall;
        private int track;
        private int wifiapp;
        private int dial;
        private List<?> wifiapps;

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public int getNotrack() {
            return notrack;
        }

        public void setNotrack(int notrack) {
            this.notrack = notrack;
        }

        public int getVolte() {
            return volte;
        }

        public void setVolte(int volte) {
            this.volte = volte;
        }

        public int getNofind() {
            return nofind;
        }

        public void setNofind(int nofind) {
            this.nofind = nofind;
        }

        public int getPower() {
            return power;
        }

        public void setPower(int power) {
            this.power = power;
        }

        public int getNostrangercall() {
            return nostrangercall;
        }

        public void setNostrangercall(int nostrangercall) {
            this.nostrangercall = nostrangercall;
        }

        public int getTrack() {
            return track;
        }

        public void setTrack(int track) {
            this.track = track;
        }

        public int getWifiapp() {
            return wifiapp;
        }

        public void setWifiapp(int wifiapp) {
            this.wifiapp = wifiapp;
        }

        public int getDial() {
            return dial;
        }

        public void setDial(int dial) {
            this.dial = dial;
        }

        public List<?> getWifiapps() {
            return wifiapps;
        }

        public void setWifiapps(List<?> wifiapps) {
            this.wifiapps = wifiapps;
        }
    }
}
