package bftsmart.demo.csdcoin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.example.csd.Account.Account;
import com.example.csd.Transfer.TokenTransfer;

public class Repository implements Serializable {

    private static final long serialVersionUID = 2485416073664377510L;

    private List<Account> accounts;
    private List<TokenTransfer> transfers;


    public Repository(List<Account> accounts,List<TokenTransfer> transfers){
        this.accounts = accounts;
        this.transfers = transfers;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<TokenTransfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<TokenTransfer> transfers) {
        this.transfers = transfers;
    }
}
