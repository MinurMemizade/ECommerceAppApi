package com.company.ecommercebackend.api.model;

public class DataChange<T> {
    private ChangeType changeType;
    private T data;

    public DataChange(ChangeType changeType, T data) {
        this.changeType = changeType;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public DataChange(ChangeType insert) {
    }

    public enum ChangeType
    {
        INSERT,
        DELETE,
        UPDATE,

    }
}
