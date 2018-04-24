package de.wirecard.eposdemo.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.wirecard.eposdemo.R;

public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemViewHolder> {

    @NonNull
    private List<SimpleItem> values;
    @Nullable
    private OnItemClickListener onItemClickListener;

    private boolean selectable;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public SimpleItemRecyclerViewAdapter(@NonNull List<SimpleItem> values, @Nullable OnItemClickListener onItemClickListener) {
        this.values = values;
        this.onItemClickListener = onItemClickListener;
        selectable = false;
    }

    public SimpleItemRecyclerViewAdapter(@NonNull List<SimpleItem> values) {
        this.values = values;
        selectable = true;
    }

    @NonNull
    @Override
    public SimpleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item_3, parent, false);
        return new SimpleItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleItemViewHolder holder, int position) {
        final SimpleItem item = values.get(position);
        changeVisibilityAndSetText(holder.getLeft(), item.getLeft());
        changeVisibilityAndSetText(holder.getCenter(), item.getCenter());
        changeVisibilityAndSetText(holder.getRight(), item.getRight());
        holder.itemView.setSelected(selectedPosition == position);
        if (selectable) {
            holder.itemView.setOnClickListener(v -> {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);
            });
        }
        else {
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(v, position);
            });
        }
    }

    private void changeVisibilityAndSetText(TextView view, String value) {
        view.setVisibility(value != null ? View.VISIBLE : View.GONE);
        view.setText(value);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
