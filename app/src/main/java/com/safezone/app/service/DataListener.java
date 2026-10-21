package com.safezone.app.service;

public interface DataListener<T> {
    void onData(T data);
    void onError(Exception e);
}
