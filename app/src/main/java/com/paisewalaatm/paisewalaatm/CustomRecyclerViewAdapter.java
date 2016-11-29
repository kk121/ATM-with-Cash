package com.paisewalaatm.paisewalaatm;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by krishna on 14/11/16.
 */

public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder> implements View.OnClickListener {
    private final LayoutInflater mLayoutInflater;
    private final List<AtmResponse.AtmObject> dataList;
    private String location;
    private ItemClickListener itemClickListener;

    @Override
    public void onClick(View view) {

    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBankName;
        private final TextView tvDistance;
        private final TextView tvTime;
        private View layout;

        public CustomViewHolder(View view) {
            super(view);
            this.layout = view;
            this.tvBankName = (TextView) view.findViewById(R.id.tv_bank_name);
            this.tvDistance = (TextView) view.findViewById(R.id.tv_distance);
            this.tvTime = (TextView) view.findViewById(R.id.tv_time);
        }
    }

    public CustomRecyclerViewAdapter(Context context, List<AtmResponse.AtmObject> dataList) {
        this.dataList = dataList;
        this.mLayoutInflater = LayoutInflater.from(context);
        location = PreferenceManager.getLocation(context);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = mLayoutInflater.inflate(R.layout.item_layout, parent, false);
        return new CustomViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null)
                    itemClickListener.onItemClick(view, holder.getAdapterPosition());
            }
        });
        holder.tvBankName.setText(dataList.get(position).bankName);
        holder.tvDistance.setText(getDistance(position) + "");
        holder.tvTime.setText(dataList.get(position).time + "");
    }

    private double getDistance(int position) {
        if (location.equals("") || dataList.get(position).location.equals(""))
            return -1;
        String userLatLong[] = location.split(",");
        double userLat = Double.parseDouble(userLatLong[0]);
        double userLongt = Double.parseDouble(userLatLong[1]);

        String latLong[] = dataList.get(position).location.split(",");
        double lat = Double.parseDouble(latLong[0]);
        double longt = Double.parseDouble(latLong[1]);

        Location startPoint = new Location("LocationA");
        startPoint.setLatitude(userLat);
        startPoint.setLongitude(userLongt);

        Location endPoint = new Location("LocationA");
        startPoint.setLatitude(lat);
        startPoint.setLongitude(longt);

        return Math.abs(startPoint.distanceTo(endPoint) / 1000.0);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface ItemClickListener {
        void onItemClick(View view, int adapterPosition);
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
