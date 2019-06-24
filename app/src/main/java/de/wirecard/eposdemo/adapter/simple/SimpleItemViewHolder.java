package de.wirecard.eposdemo.adapter.simple;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.wirecard.eposdemo.R;

public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

    private final TextView text1;
    private final TextView text2;
    private final TextView text3;
    private final TextView text4;

    public SimpleItemViewHolder(View itemView) {
        super(itemView);
        text1 = itemView.findViewById(R.id.text1);
        text2 = itemView.findViewById(R.id.text2);
        text3 = itemView.findViewById(R.id.paymentAmount);
        text4 = itemView.findViewById(R.id.paymentDateTime);
    }

    public TextView getText1() {
        return text1;
    }

    public TextView getText2() {
        return text2;
    }

    public TextView getText3() {
        return text3;
    }

    public TextView getText4() {
        return text4;
    }
}