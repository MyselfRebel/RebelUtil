package com.rebel.adapters;

public interface OnItemClickListener<T> extends OnClickListener {
    void onItemClick(T item);
}
