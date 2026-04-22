package com.spectrasonic.economySystem.cache;

public class CacheEntry {

    private volatile double balance;
    private volatile boolean dirty;
    private volatile long lastAccess;
    private final String uuid;

    public CacheEntry(String uuid, double balance) {
        this.uuid = uuid;
        this.balance = balance;
        this.dirty = false;
        this.lastAccess = System.currentTimeMillis();
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = Math.max(0, balance);
        this.dirty = true;
        this.lastAccess = System.currentTimeMillis();
    }

    public boolean isDirty() {
        return dirty;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public String getUuid() {
        return uuid;
    }

    public synchronized void addBalance(double amount) {
        this.balance = Math.max(0, this.balance + amount);
        this.dirty = true;
        this.lastAccess = System.currentTimeMillis();
    }

    public synchronized void removeBalance(double amount) {
        this.balance = Math.max(0, this.balance - amount);
        this.dirty = true;
        this.lastAccess = System.currentTimeMillis();
    }

    public void markClean() {
        this.dirty = false;
        this.lastAccess = System.currentTimeMillis();
    }

    public void updateAccessTime() {
        this.lastAccess = System.currentTimeMillis();
    }
}
