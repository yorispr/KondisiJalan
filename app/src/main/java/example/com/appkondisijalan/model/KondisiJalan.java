package example.com.appkondisijalan.model;

/**
 * Created by Typo-Co on 26/11/2016.
 */

public class KondisiJalan {
    private String lokasi;
    private String foto;
    private String pengunggah;
    private String tgl_unggah;
    private String latitude;
    private String longitude;
    private String status;
    private String id_laporan;

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getPengunggah() {
        return pengunggah;
    }

    public void setPengunggah(String pengunggah) {
        this.pengunggah = pengunggah;
    }

    public String getTgl_unggah() {
        return tgl_unggah;
    }

    public void setTgl_unggah(String tgl_unggah) {
        this.tgl_unggah = tgl_unggah;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId_laporan() {
        return id_laporan;
    }

    public void setId_laporan(String id_laporan) {
        this.id_laporan = id_laporan;
    }
}
