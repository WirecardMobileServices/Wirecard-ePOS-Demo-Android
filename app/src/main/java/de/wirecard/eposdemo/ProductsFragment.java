package de.wirecard.eposdemo;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;

import java.util.List;

import de.wirecard.epos.InventoryManager;
import de.wirecard.epos.model.inventory.Filter;
import de.wirecard.eposdemo.adapter.SimpleItem;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class ProductsFragment extends AbsFragment<RecyclerView> {

    public ProductsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        content.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.product_columns)));
        final InventoryManager inventory = EposSdkApplication.getEposSdk().inventory();
        addDisposable(inventory
                .getCatalogues()
                .map(catalogues -> catalogues.get(0))
                .flatMap(catalogue -> inventory.getProducts(catalogue.getId(), new Filter()))
                .map(products -> Stream.of(products).map(it -> new SimpleItem(it.getName(), it.getCatalogue().getId(), it.getId())).collect(Collectors.toList()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(products -> loadingFinishedAndShowRecycler(new ProductAdapter(products)), showErrorInsteadContent())
        );
    }

    class ProductAdapter extends RecyclerView.Adapter<ProductHolder> {

        private List<SimpleItem> values;

        public ProductAdapter(List<SimpleItem> values) {
            this.values = values;
        }

        @NonNull
        @Override
        public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ProductHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_product_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
            final SimpleItem item = values.get(position);
            Glide.with(ProductsFragment.this).clear(holder.productImage);
            holder.productImage.setImageResource(R.drawable.image_empty);

            addDisposable(EposSdkApplication.getEposSdk()
                    .inventory()
                    .getProductImage(item.getCenter(), item.getRight())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(productImage -> {
                        if (holder.productImage.getVisibility() == View.VISIBLE)
                            Glide.with(ProductsFragment.this)
                                    .load(productImage.getData())
                                    .into(holder.productImage);
                    }, throwable -> {
                    })
            );
            holder.productName.setText(item.getLeft());
        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }

    class ProductHolder extends RecyclerView.ViewHolder {

        private final ImageView productImage;
        private final TextView productName;

        public ProductHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
        }

        public ImageView getProductImage() {
            return productImage;
        }

        public TextView getProductName() {
            return productName;
        }
    }
}
