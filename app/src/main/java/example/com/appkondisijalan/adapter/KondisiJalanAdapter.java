package example.com.appkondisijalan.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import example.com.appkondisijalan.R;
import example.com.appkondisijalan.model.KondisiJalan;

public class KondisiJalanAdapter extends BaseAdapter{
    Activity activity;
    List<KondisiJalan> kondisiJalanList;
    LayoutInflater layoutInflater;

    public KondisiJalanAdapter(Activity activity, List<KondisiJalan> kondisiJalanList) {
        this.activity = activity;
        this.kondisiJalanList = kondisiJalanList;
    }

    @Override
    public int getCount() {
        return kondisiJalanList.size();
    }

    @Override
    public Object getItem(int position) {
        return kondisiJalanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (layoutInflater == null)
            layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = layoutInflater.inflate(R.layout.row_list_laporan, null);

        CircleImageView foto = (CircleImageView) convertView.findViewById(R.id.picture);
        TextView txtPengunggah = (TextView) convertView.findViewById(R.id.txtPengunggah);
        TextView txtLokasi = (TextView) convertView.findViewById(R.id.txtLokasi);
        TextView txtTglUnggah = (TextView) convertView.findViewById(R.id.txtTglUnggah);
        TextView txtLatitude = (TextView) convertView.findViewById(R.id.txtLatitude);
        TextView txtLongitude = (TextView) convertView.findViewById(R.id.txtLongitude);

        KondisiJalan kondisiJalan = kondisiJalanList.get(position);

        Glide.with(activity).load(kondisiJalan.getFoto()).into(foto);
        txtPengunggah.setText("Pengunggah : "+ kondisiJalan.getPengunggah());
        txtLokasi.setText("Lokasi : "+ kondisiJalan.getLokasi());
        txtTglUnggah.setText("Tgl Unggah : "+ kondisiJalan.getTgl_unggah());
        txtLatitude.setText(kondisiJalan.getLatitude());
        txtLongitude.setText(kondisiJalan.getLongitude());

        return convertView;
    }
}
