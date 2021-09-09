package com.rebel.adapters.viewholders;

import android.view.View;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.rebel.adapters.OnClickListener;

public abstract class ItemViewHolder<T, L extends OnClickListener> extends RecyclerView.ViewHolder {

    public ItemViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBind(T item, @Nullable L listener);
}
