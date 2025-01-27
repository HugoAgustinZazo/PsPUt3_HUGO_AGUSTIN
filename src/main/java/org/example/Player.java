package org.example;

public class Player {
    private String name;
    private int pv;
    private int money;
    private String color;

    public Player(String name,int pv, int money,String color) {
        this.name = name;
        this.pv=pv;
        this.money=money;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String nombre) {
        this.name = nombre;
    }

    public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "=== Tus datos ===\n" +
                "PV: "+pv+"\n" +
                "Dinero: "+money;
    }
}
