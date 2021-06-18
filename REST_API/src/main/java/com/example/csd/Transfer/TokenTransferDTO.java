package com.example.csd.Transfer;

import java.io.Serializable;
import java.sql.Timestamp;

public class TokenTransferDTO implements Serializable {

    private Timestamp time;
    private String from;
    private String to;
    private float amount;
    private byte[] signature;

    public TokenTransferDTO(float amount, String from, String to, Timestamp time,byte[] signature){
        super();
        this.time=time;
        this.amount=amount;
        this.from=from;
        this.to=to;
        this.signature=signature;
    }

    public TokenTransferDTO(float amount, String from, String to, Timestamp time){
        super();
        this.time=time;
        this.amount=amount;
        this.from=from;
        this.to=to;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
