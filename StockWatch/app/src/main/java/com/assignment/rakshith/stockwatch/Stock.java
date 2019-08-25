package com.assignment.rakshith.stockwatch;

public class Stock {

    String stockSymbol;
    String companyName;
    double price;
    double changePrice;
    double PercentChange;

    public Stock(String stockSymbol,String companyName,double price, double changePrice, double PercentChange){
        this.stockSymbol=stockSymbol;
        this.companyName=companyName;
        this.price=price;
        this.changePrice=changePrice;
        this.PercentChange=PercentChange;
    }


    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceChange() {
        return changePrice;
    }

    public void setPriceChange(double changePrice) {
        this.changePrice = changePrice;
    }

    public double getPercentChange() {
        return PercentChange;
    }

    public void setPercentageChange(double PercentChange) {
        this.PercentChange = PercentChange;
    }

    public Stock(){}




}
