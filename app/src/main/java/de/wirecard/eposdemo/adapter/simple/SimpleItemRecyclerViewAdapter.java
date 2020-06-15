package de.wirecard.eposdemo.adapter.simple;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import de.wirecard.eposdemo.R;

public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<de.wirecard.eposdemo.adapter.simple.SimpleItemViewHolder> {

    @NonNull
    private List<SimpleItem> values;
    @Nullable
    private OnItemClickListener onItemClickListener;

    private boolean selectable;
    private int selectedPosition = RecyclerView.NO_POSITION;

    @Nullable
    private Coloring coloring;

    public SimpleItemRecyclerViewAdapter(@NonNull List<SimpleItem> values, boolean selectable, @Nullable Coloring coloring) {
        this.values = values;
        this.selectable = selectable;
        this.coloring = coloring;
    }

    public SimpleItemRecyclerViewAdapter(@NonNull List<SimpleItem> values) {
        this.values = values;
        selectable = true;
    }

    public SimpleItemRecyclerViewAdapter(@NonNull List<SimpleItem> values, @Nullable OnItemClickListener onItemClickListener, @Nullable Coloring coloring) {
        this.values = values;
        this.onItemClickListener = onItemClickListener;
        selectable = false;
        this.coloring = coloring;
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleItemViewHolder holder, int position) {
        final SimpleItem item = values.get(position);
        changeVisibilityAndSetText(holder.getText1(), item.getText1());
        changeVisibilityAndSetText(holder.getText2(), item.getText2());
        changeVisibilityAndSetText(holder.getText3(), item.getText3());
        changeVisibilityAndSetText(holder.getText4(), item.getText4());

        if (coloring != null) {
            holder.getText1().setTextColor(coloring.getText1Color(item.getText1()));
            holder.getText2().setTextColor(coloring.getText2Color(item.getText2()));
            holder.getText3().setTextColor(coloring.getText3Color(item.getText3()));
            holder.getText4().setTextColor(coloring.getText4Color(item.getText4()));
        }

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


    @NonNull
    @Override
    public SimpleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item, parent, false);
        return new SimpleItemViewHolder(view);
    }

    public interface Coloring {
        int getText1Color(String text);

        int getText2Color(String text);

        int getText3Color(String text);

        int getText4Color(String text);
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
