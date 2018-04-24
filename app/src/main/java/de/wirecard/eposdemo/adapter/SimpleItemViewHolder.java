package de.wirecard.eposdemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.wirecard.eposdemo.R;

public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

    private final TextView left;
    private final TextView center;
    private final TextView right;

    public SimpleItemViewHolder(View itemView) {
        super(itemView);
        left = itemView.findViewById(R.id.text1);
        center = itemView.findViewById(R.id.text2);
        right = itemView.findViewById(R.id.text3);
    }

    public TextView getLeft() {
        return left;
    }

    public TextView getCenter() {
        return center;
    }

    public TextView getRight() {
        return right;
    }
}