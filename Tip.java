import java.io.File;

public class Tip {
    final static int typeCard = 0;
    final static int typeCash = 1;
    final static int typeUnknown = 2;
    boolean hasCard = false;
    boolean hasCash = false;
    boolean hasUnknown = false;
    double card = 0.0;
    double cash = 0.0;
    double unknown = 0.0;

    // constructor for when there is no tip
    public Tip() { ; }

    // constructor for a single tip of one type
    public Tip(double amount, int type) {
        if (type == typeCard) {
            this.hasCard = true;
            this.card = amount;
        } else if (type == typeCash) {
            this.hasCash = true;
            this.cash = amount;
        } else if (type == typeUnknown) {
            this.hasUnknown = true;
            this.unknown = amount;
        }
    }

    //  constructor for a tip that has both card and cash
    public Tip(double cardAmount, double cashAmount) {
        this.hasCard = true;
        this.card = cardAmount;
        this.hasCash = true;
        this.cash = cashAmount;
    }

    // constructor for loading already completed orders
    public Tip(double cardAmount, double cashAmount, double unknownAmount) {
        if (cardAmount != 0.0) { this.hasCard = true; this.card = cardAmount; }
        if (cashAmount != 0.0) { this.hasCash = true; this.cash = cashAmount; }
        if (unknownAmount != 0.0) { this.hasUnknown = true; this.unknown = unknownAmount; }
    }

    // methods to check what type/'s of tips are present for this tip/order
    public boolean hasBoth() {
        if (this.hasCard == true & this.hasCash == true) { return true; } else { return false; }
    }

    public boolean hasCard() {
        if (this.hasCard == true) { return true; } else { return false; }
    }

    public boolean hasCash() {
        if (this.hasCash == true) { return true; } else { return false; }
    }

    public boolean hasUnknown() {
        if (this.hasUnknown == true) { return true; } else { return false; }
    }

    public void save(File file) {
        if (file.exists()) {
            Utility.appendData(file,
                String.format("%s,%s,%s", this.card, this.cash, this.unknown),
                "\n");
        } else {
            Utility.writeData(file,
                String.format("%s,%s,%s", this.card, this.cash, this.unknown));
        }

        Utility.writeData(file, String.format("%s,%s,%s", this.card, this.cash, this.unknown));
    }
}