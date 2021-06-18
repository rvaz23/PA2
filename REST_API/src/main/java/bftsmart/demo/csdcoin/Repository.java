package bftsmart.demo.csdcoin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.example.csd.Account.Account;
import com.example.csd.BlockChain.Block;
import com.example.csd.CoinBase.CoinBase;
import com.example.csd.Transfer.TokenTransfer;

public class Repository implements Serializable {

    private static final long serialVersionUID = 2485416073664377510L;

    private List<Account> accounts;
    private List<TokenTransfer> transfers;
    private List<Block> chain;
    private List<CoinBase> coinBases;


    public Repository(List<Account> accounts,List<TokenTransfer> transfers,List<Block> chain,List<CoinBase> coinBases){
        this.accounts = accounts;
        this.transfers = transfers;
        this.chain=chain;
        this.coinBases=coinBases;
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

    public List<Block> getChain() {
        return chain;
    }

    public void setChain(List<Block> chain) {
        this.chain = chain;
    }

    public List<CoinBase> getCoinBases() {
        return coinBases;
    }

    public void setCoinBases(List<CoinBase> coinBases) {
        this.coinBases = coinBases;
    }
}
