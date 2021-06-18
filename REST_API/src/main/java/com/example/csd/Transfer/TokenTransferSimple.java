package com.example.csd.Transfer;

import auxiliary.utils;
import com.example.csd.Account.Account;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

public class TokenTransferSimple implements Serializable {


    private String from;
    private String to;
    private byte[] signature;
    private boolean isEncrypted;
    private Integer id;
    private byte[] amount;


    public TokenTransferSimple(int id, boolean isEncrypted, byte[] amount, String from, String to, byte[] signature){
        this.id=id;
        this.amount=amount;
        this.isEncrypted=isEncrypted;
        this.from=from;
        this.to=to;
        this.signature=signature;
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

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getAmount() {
        return amount;
    }

    public void setAmount(byte[] amount) {
        this.amount = amount;
    }

    public byte[] TransferBlock() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] id = utils.intToByteArray(this.id);
        byte[] fromUs =from.getBytes(StandardCharsets.ISO_8859_1);
        byte[] fromLength= utils.intToByteArray(fromUs.length);
        byte[] toUs =to.getBytes(StandardCharsets.ISO_8859_1);
        byte[] toLength= utils.intToByteArray(toUs.length);
        byte isEncrypted=this.isEncrypted ? (byte) 1:0;
        os.write(id);
        os.write(signature);
        os.write(fromLength);
        os.write(fromUs);
        os.write(toLength);
        os.write(toUs);
        os.write(isEncrypted);
        os.write(amount);
        //os.write(signature);
        return os.toByteArray();
    }
}
