package ru.jooogle.sunshine.admin_pc.sunshine_reborn.presenter;

public interface Presenter<V> {
    void attachView(V view);

    void detachView();
}
