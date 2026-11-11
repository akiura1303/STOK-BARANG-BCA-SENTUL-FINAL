package com.bca.stokbarang;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context c) { super(c, "stok_barang.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE materials(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, unit TEXT, price REAL, stock REAL DEFAULT 0, type TEXT DEFAULT 'Material')");
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT, material_id INTEGER, date TEXT, kind TEXT, qty REAL, note TEXT)");
        String[][] seed = {
            {"Besi 12 mm","Kg","15000","Material"},{"Besi 10 mm","Kg","13000","Material"},
            {"Besi 8 mm","Kg","12000","Material"},{"Besi beton polos 6 mm","Kg","11000","Material"},
            {"Wiremesh M8","Lembar","550000","Material"},{"Pipa PVC 4 inci","m","45000","Material"},
            {"Pipa PVC 3 inci","m","32000","Material"},{"Pipa galvanis 2 inci","Batang","125000","Material"},
            {"Semen Portland","Zak","70000","Material"},{"Pasir beton","m3","250000","Material"},
            {"Pasir pasang","m3","220000","Material"},{"Batu split","m3","280000","Material"},
            {"Batu kali","m3","300000","Material"},{"Readymix K-250","m3","950000","Material"},
            {"Readymix K-300","m3","1050000","Material"},{"Bata merah","Pcs","1000","Material"},
            {"Bata ringan","Pcs","8500","Material"},{"Hebel perekat","Zak","65000","Material"},
            {"Papan kayu","Lembar","85000","Material"},{"Multipleks 12 mm","Lembar","175000","Material"},
            {"Tripleks 3 mm","Lembar","55000","Material"},{"Paku 7 cm","Kg","22000","Material"},
            {"Kawat bendrat","Kg","23000","Material"},{"Cat tembok","Pail","450000","Material"},
            {"Thinner","Liter","25000","Material"},{"Keramik 40x40","Dus","65000","Material"},
            {"Granit 60x60","Dus","180000","Material"},{"Siku aluminium","Batang","35000","Material"},
            {"Kabel NYM 2x1.5","Roll","450000","Material"},{"Kabel NYM 3x2.5","Roll","850000","Material"},
            {"Lampu LED 18W","Pcs","45000","Material"},{"Saklar tunggal","Pcs","18000","Material"},
            {"Stop kontak","Pcs","25000","Material"},{"Pipa conduit","Batang","18000","Material"},
            {"Elbow PVC 4 inci","Pcs","18000","Material"},{"Lem PVC","Kaleng","35000","Material"},
            {"Gypsum 9 mm","Lembar","65000","Material"},{"Rangka hollow","Batang","45000","Material"},
            {"Atap spandek","m2","85000","Material"},{"Talang air","Batang","75000","Material"},
            {"Sewa molen beton","Hari","250000","Alat"},{"Mesin las","Unit","0","Alat"},
            {"Gerinda tangan","Unit","0","Alat"},{"Bor listrik","Unit","0","Alat"},
            {"Concrete vibrator","Unit","0","Alat"},{"Waterpass","Unit","0","Alat"},
            {"Meteran 5 m","Unit","0","Alat"},{"Cangkul","Unit","0","Alat"},
            {"Sekop","Unit","0","Alat"},{"Gerobak dorong","Unit","0","Alat"}
        };
        for (String[] s : seed) {
            ContentValues v = new ContentValues();
            v.put("name",s[0]); v.put("unit",s[1]); v.put("price",Double.parseDouble(s[2])); v.put("type",s[3]);
            db.insertWithOnConflict("materials", null, v, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV) {}

    public long addMaterial(String name,String unit,double price,String type) {
        ContentValues v=new ContentValues(); v.put("name",name);v.put("unit",unit);v.put("price",price);v.put("type",type);
        return getWritableDatabase().insert("materials",null,v);
    }
    public Cursor materials(String type) {
        return getReadableDatabase().rawQuery("SELECT id,name,unit,price,stock,type FROM materials WHERE (?='Semua' OR type=?) ORDER BY name",new String[]{type,type});
    }
    public Cursor materialById(long id) {
        return getReadableDatabase().rawQuery("SELECT id,name,unit,price,stock FROM materials WHERE id=?",new String[]{String.valueOf(id)});
    }
    public boolean transact(long id,String date,String kind,double qty,String note) {
        SQLiteDatabase db=getWritableDatabase(); db.beginTransaction();
        try {
            Cursor c=db.rawQuery("SELECT stock FROM materials WHERE id=?",new String[]{String.valueOf(id)});
            if(!c.moveToFirst()){c.close();return false;}
            double stock=c.getDouble(0); c.close();
            if(kind.equals("Keluar") && qty>stock) return false;
            ContentValues t=new ContentValues();t.put("material_id",id);t.put("date",date);t.put("kind",kind);t.put("qty",qty);t.put("note",note);
            db.insert("transactions",null,t);
            ContentValues m=new ContentValues();m.put("stock",stock+(kind.equals("Masuk")?qty:-qty));
            db.update("materials",m,"id=?",new String[]{String.valueOf(id)});
            db.setTransactionSuccessful(); return true;
        } finally { db.endTransaction(); }
    }
    public Cursor transactions() {
        return getReadableDatabase().rawQuery("SELECT t.date,m.name,t.kind,t.qty,m.unit,t.note FROM transactions t JOIN materials m ON m.id=t.material_id ORDER BY t.id DESC",null);
    }
    public Cursor totals() {
        return getReadableDatabase().rawQuery("SELECT COUNT(*),COALESCE(SUM(stock),0) FROM materials",null);
    }
}
