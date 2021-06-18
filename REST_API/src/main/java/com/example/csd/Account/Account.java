package com.example.csd.Account;




import com.example.csd.Transfer.TokenTransfer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Account implements Serializable{
    private Integer id;
    private String username;
    private String pwd;
    private Float amount;
    private byte[] publickey;
    private byte[] operationHash;


    @OneToMany(targetEntity = TokenTransfer.class ,mappedBy = "from")
    private List<TokenTransfer> sendHistory;// = new ArrayList<>();

    public Account(){

    }

    public Account(String username,String pwd,byte[] publickey,byte[] hash){
        super();
        this.amount=0f;
        this.username=username;
        this.pwd=pwd;
        this.publickey=publickey;
        this.operationHash=hash;
    }

    public byte[] getOperationHash() {
        return operationHash;
    }

    public void setOperationHash(byte[] operationHash) {
        this.operationHash = operationHash;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void deposit(int amount){
        this.amount+=amount;
    }

    public byte[] getPublickey() {
        return publickey;
    }

    public void setPublickey(byte[] publickey) {
        this.publickey = publickey;
    }

    public void makeTransfer(float amount){
        this.amount-=amount;
    }

    public void receiveTransfer(float amount){
        this.amount+=amount;
    }



    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public String getUsername(){
        return username;
    }

    public Float getAmount() {
        return amount;
    }

    public String getPwd() {
        return pwd;
    }

/*
    public void setHistory(List<TokenTransfer> history) {
        this.sendHistory = history;
    }

    public List<TokenTransfer> getHistory() {
        return sendHistory;
    }*/
}
