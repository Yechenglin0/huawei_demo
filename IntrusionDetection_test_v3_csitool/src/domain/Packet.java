package domain;

import domain.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 定义CSI数据包，主要是需要
 */
public class Packet {
    int timestamp_low;
    int bfee_count;
    int Nrx;
    int Ntx;
    int rssi_a;
    int rssi_b;
    int rssi_c;
    int noise;
    int agc;
    int[] perm;

    @Override
    public String toString() {
        return "Packet{" +
                "timestamp_low=" + timestamp_low +
                ", bfee_count=" + bfee_count +
                ", Nrx=" + Nrx +
                ", Ntx=" + Ntx +
                ", rssi_a=" + rssi_a +
                ", rssi_b=" + rssi_b +
                ", rssi_c=" + rssi_c +
                ", noise=" + noise +
                ", agc=" + agc +
                ", perm=" + Arrays.toString(perm) +
                ", rate=" + rate +
                ", csi=" + csi.get(1) +
                '}';
    }

    int rate;
    HashMap<Integer,ArrayList<Complex>> csi;

    public int getTimestamp_low() {
        return timestamp_low;
    }
    public void setTimestamp_low(int timestamp_low) {
        this.timestamp_low = timestamp_low;
    }
    public int getBfee_count() {
        return bfee_count;
    }
    public void setBfee_count(int bfee_count) {
        this.bfee_count = bfee_count;
    }
    public int getNrx() {
        return Nrx;
    }
    public void setNrx(int nrx) {
        Nrx = nrx;
    }
    public int getNtx() {
        return Ntx;
    }
    public void setNtx(int ntx) {
        Ntx = ntx;
    }
    public int getRssi_a() {
        return rssi_a;
    }
    public void setRssi_a(int rssi_a) {
        this.rssi_a = rssi_a;
    }
    public int getRssi_b() {
        return rssi_b;
    }
    public void setRssi_b(int rssi_b) {
        this.rssi_b = rssi_b;
    }
    public int getRssi_c() {
        return rssi_c;
    }
    public void setRssi_c(int rssi_c) {
        this.rssi_c = rssi_c;
    }
    public int getNoise() {
        return noise;
    }
    public void setNoise(int noise) {
        this.noise = noise;
    }
    public int getAgc() {
        return agc;
    }
    public void setAgc(int agc) {
        this.agc = agc;
    }
    public int[] getPerm() {
        return perm;
    }
    public void setPerm(int[] perm) {
        this.perm = perm;
    }
    public int getRate() {
        return rate;
    }
    public void setRate(int rate) {
        this.rate = rate;
    }
    public HashMap<Integer,ArrayList<Complex>> getCsi() {
        return csi;
    }
    public void setCsi(HashMap<Integer,ArrayList<Complex>> csi) {
        this.csi = csi;
    }
}
