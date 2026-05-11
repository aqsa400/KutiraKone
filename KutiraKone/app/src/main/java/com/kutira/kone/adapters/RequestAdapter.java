package com.kutira.kone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kutira.kone.R;
import com.kutira.kone.models.SwapRequest;
import com.kutira.kone.utils.FirebaseHelper;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private List<SwapRequest> requestList;
    private Context context;
    private boolean isOwner;
    private FirebaseHelper firebaseHelper;

    public RequestAdapter(List<SwapRequest> requestList, Context context, boolean isOwner) {
        this.requestList = requestList;
        this.context = context;
        this.isOwner = isOwner;
        this.firebaseHelper = FirebaseHelper.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SwapRequest request = requestList.get(position);

        holder.tvScrapTitle.setText(request.getScrapTitle());
        holder.tvRequester.setText(isOwner ?
                "From: " + request.getRequesterName() :
                "Your request");
        holder.tvMessage.setText(request.getMessage());
        holder.tvStatus.setText("Status: " + request.getStatus().toUpperCase());

        Glide.with(context).load(request.getScrapImageUrl())
                .placeholder(R.drawable.ic_fabric_placeholder)
                .into(holder.ivImage);

        // Color status
        int statusColor;
        switch (request.getStatus()) {
            case "accepted": statusColor = 0xFF4CAF50; break;
            case "rejected": statusColor = 0xFFF44336; break;
            default: statusColor = 0xFFFF9800; break;
        }
        holder.tvStatus.setTextColor(statusColor);

        if (isOwner && request.getStatus().equals("pending")) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);

            holder.btnAccept.setOnClickListener(v -> updateRequestStatus(request, "accepted", position));
            holder.btnReject.setOnClickListener(v -> updateRequestStatus(request, "rejected", position));
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
    }

    private void updateRequestStatus(SwapRequest request, String status, int position) {
        firebaseHelper.getFirestore()
                .collection(FirebaseHelper.COLLECTION_REQUESTS)
                .document(request.getId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    request.setStatus(status);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Request " + status, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvScrapTitle, tvRequester, tvMessage, tvStatus;
        Button btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvScrapTitle = itemView.findViewById(R.id.tvScrapTitle);
            tvRequester = itemView.findViewById(R.id.tvRequester);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
