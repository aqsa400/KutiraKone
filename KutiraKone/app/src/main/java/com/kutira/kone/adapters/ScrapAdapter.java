package com.kutira.kone.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kutira.kone.R;
import com.kutira.kone.activities.ScrapDetailActivity;
import com.kutira.kone.models.FabricScrap;
import java.util.List;

public class ScrapAdapter extends RecyclerView.Adapter<ScrapAdapter.ViewHolder> {

    private List<FabricScrap> scrapList;
    private Context context;

    public ScrapAdapter(List<FabricScrap> scrapList, Context context) {
        this.scrapList = scrapList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_scrap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FabricScrap scrap = scrapList.get(position);

        holder.tvTitle.setText(scrap.getTitle());
        holder.tvMaterial.setText(scrap.getMaterialType());
        holder.tvSize.setText(scrap.getSize());

        Glide.with(context)
                .load(scrap.getImageUrl())
                .placeholder(R.drawable.ic_fabric_placeholder)
                .centerCrop()
                .into(holder.ivImage);

        // Color-code by material
        int bgColor;
        switch (scrap.getMaterialType().toLowerCase()) {
            case "silk": bgColor = 0xFFE91E63; break;
            case "cotton": bgColor = 0xFF4CAF50; break;
            case "wool": bgColor = 0xFFFF9800; break;
            case "polyester": bgColor = 0xFF2196F3; break;
            case "denim": bgColor = 0xFF3F51B5; break;
            default: bgColor = 0xFF9C27B0; break;
        }
        holder.tvMaterialBadge.setBackgroundColor(bgColor);
        holder.tvMaterialBadge.setText(scrap.getMaterialType());

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ScrapDetailActivity.class);
            intent.putExtra(ScrapDetailActivity.EXTRA_SCRAP_ID, scrap.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return scrapList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivImage;
        TextView tvTitle, tvMaterial, tvSize, tvMaterialBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMaterial = itemView.findViewById(R.id.tvMaterial);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvMaterialBadge = itemView.findViewById(R.id.tvMaterialBadge);
        }
    }
}
