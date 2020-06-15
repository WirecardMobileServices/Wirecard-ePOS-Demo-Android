package de.wirecard.eposdemo;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.wirecard.epos.InventoryManager;
import de.wirecard.epos.model.inventory.ProductImage;
import de.wirecard.epos.model.with.With;
import de.wirecard.epos.model.with.WithPagination;
import de.wirecard.eposdemo.adapter.simple.SimpleItem;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ProductsFragment extends AbsFragment<RecyclerView> {

    private static final String EPOS_SDK_URI = "eposSKD://productCatalogueImage/";

    private Picasso picasso;

    public ProductsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Picasso.Builder picassoBuilder = new Picasso.Builder(getContext());
        picassoBuilder.addRequestHandler(new EposPicassoRequestHandler());
        picasso = picassoBuilder.build();

        content.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.product_columns)));
        final InventoryManager inventory = EposSdkApplication.getEposSdk().inventory();

        WithPagination filter = With.pagination()
                .page(0)
                .size(20)
                .scheduler(Schedulers.io());
        addDisposable(inventory
                .getCatalogues()
                .map(catalogues -> catalogues.get(0))
                .flatMap(catalogue -> inventory.getProducts(catalogue.getId(), filter))
                .map(products -> Stream.of(products).map(it -> new SimpleItem(it.getName(), it.getCatalogue().getId(), it.getId(), null)).collect(Collectors.toList()))
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
            picasso.load(Uri.parse(EPOS_SDK_URI + item.getText2() + "/" + item.getText3()))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.image_empty)
                    .into(holder.productImage);
            holder.productName.setText(item.getText1());
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

    static class EposPicassoRequestHandler extends RequestHandler {

        @Override
        public boolean canHandleRequest(Request data) {
            return data.uri.toString().startsWith(EPOS_SDK_URI);
        }

        @Nullable
        @Override
        public Result load(Request request, int networkPolicy) throws IOException {
            final List<String> segments = request.uri.getPathSegments();
            String catalogueId = segments.get(0);
            String productId = segments.get(1);

            final ProductImage productImage = EposSdkApplication.getEposSdk()
                    .inventory()
                    .getProductImage(catalogueId, productId)
                    .blockingGet();

            final byte[] data = productImage.getData();
            final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            return new Result(bitmap, Picasso.LoadedFrom.NETWORK);
        }
    }
}
