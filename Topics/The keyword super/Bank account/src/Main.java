class BankAccount {

    protected String number;
    protected long balance;

    public BankAccount(String number, long balance) {
        this.number = number;
        number.endsWith("")
        this.balance = balance;
    }
}

class CheckingAccount extends BankAccount {

    protected double fee;
    
    public CheckingAccount(String number, long balance, double fee) {
        super(number, balance);
    
        this.fee = fee;
    }
    
}

class SavingAccount extends BankAccount {

    protected double interestRate;
    
    public SavingAccount(String number, long balance, double interestRate) {
        super(number, balance);
        
        this.interestRate = interestRate;
    }
    
}
