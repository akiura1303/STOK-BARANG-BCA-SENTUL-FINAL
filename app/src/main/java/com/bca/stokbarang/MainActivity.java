package com.bca.stokbarang;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    DatabaseHelper db;
    LinearLayout root, body;
    int blue=Color.rgb(21,101,216);
    String current="Dashboard";
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new DatabaseHelper(this);showDashboard();}
    TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(30,40,55));if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(8,8,8,8);return t;}
    Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setOnClickListener(v->r.run());return b;}
    void frame(String title){
        current=title; root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(247,249,252));setContentView(root);
        TextView bar=text((title.equals("Dashboard")?"  Stok Barang":"  ‹   "+title),20,true);bar.setTextColor(Color.WHITE);bar.setBackgroundColor(blue);bar.setPadding(16,17,12,17);
        if(!title.equals("Dashboard"))bar.setOnClickListener(v->showDashboard());root.addView(bar);
        body=new LinearLayout(this);body.setPadding(14,12,14,12);body.setOrientation(1);ScrollView sc=new ScrollView(this);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
    }
    void showDashboard(){
        frame("Dashboard");body.addView(text("Kelola Stok dengan Mudah",16,false));
        String[] labels={"Transaksi Masuk / Keluar","Master Data Material & Alat","Laporan Stok","Pengaturan / Export CSV"};
        Runnable[] actions={()->showTransaction(),()->showMaster(),()->showReport(),()->showSettings()};
        for(int i=0;i<labels.length;i++){Button b=button("▣   "+labels[i],actions[i]);b.setTextSize(17);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,105);p.setMargins(0,8,0,8);body.addView(b,p);}
        Cursor c=db.totals();if(c.moveToFirst())body.addView(text("Total jenis barang: "+c.getInt(0)+"   |   Total stok: "+c.getDouble(1),15,true));c.close();
        body.addView(text("Data tersimpan di perangkat (SQLite).",13,false));
    }
    void showMaster(){
        frame("Master Data");body.addView(button("+ Tambah Material / Alat",()->addMaterialDialog()));
        LinearLayout tabs=new LinearLayout(this);Button all=button("Semua",()->listMaterials("Semua"));Button mat=button("Material",()->listMaterials("Material"));Button alat=button("Alat",()->listMaterials("Alat"));tabs.addView(all,new LinearLayout.LayoutParams(0,-2,1));tabs.addView(mat,new LinearLayout.LayoutParams(0,-2,1));tabs.addView(alat,new LinearLayout.LayoutParams(0,-2,1));body.addView(tabs);listMaterials("Semua");
    }
    void listMaterials(String type){
        // Rebuild screen to keep filter state simple
        frame("Master Data");body.addView(button("+ Tambah Material / Alat",()->addMaterialDialog()));
        LinearLayout tabs=new LinearLayout(this);for(String s:new String[]{"Semua","Material","Alat"})tabs.addView(button(s,()->listMaterials(s)),new LinearLayout.LayoutParams(0,-2,1));body.addView(tabs);
        Cursor c=db.materials(type);while(c.moveToNext()){
            String line=c.getString(1)+"  •  "+c.getDouble(4)+" "+c.getString(2)+"\nHarga standar: Rp "+String.format(Locale.US,"%,.0f",c.getDouble(3));
            long id=c.getLong(0);TextView row=text(line,15,true);row.setBackgroundColor(Color.WHITE);row.setPadding(12,13,12,13);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,4,0,4);body.addView(row,p);
        }c.close();
    }
    void addMaterialDialog(){
        LinearLayout form=new LinearLayout(this);form.setPadding(18,5,18,5);form.setOrientation(1);
        EditText name=new EditText(this);name.setHint("Nama material / alat");form.addView(name);
        EditText unit=new EditText(this);unit.setHint("Satuan (Kg, m, Zak, Unit)");form.addView(unit);
        EditText price=new EditText(this);price.setHint("Harga standar (angka)");price.setInputType(8194);form.addView(price);
        Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Material","Alat"}));form.addView(type);
        new AlertDialog.Builder(this).setTitle("Tambah Master Data").setView(form).setNegativeButton("Batal",null).setPositiveButton("Simpan",(d,w)->{
            try{if(name.getText().toString().trim().isEmpty()||unit.getText().toString().trim().isEmpty())throw new Exception();
                long result=db.addMaterial(name.getText().toString().trim(),unit.getText().toString().trim(),price.getText().length()==0?0:Double.parseDouble(price.getText().toString()),type.getSelectedItem().toString());
                if(result==-1)Toast.makeText(this,"Nama sudah ada atau gagal disimpan",0).show();listMaterials("Semua");
            }catch(Exception e){Toast.makeText(this,"Periksa nama, satuan, dan harga",0).show();}
        }).show();
    }
    void showTransaction(){
        frame("Transaksi Stok");
        Spinner kind=new Spinner(this);kind.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Masuk","Keluar"}));body.addView(text("Jenis transaksi",14,true));body.addView(kind);
        ArrayList<Long> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();Cursor c=db.materials("Semua");while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1)+" ("+c.getString(2)+")");}c.close();
        Spinner material=new Spinner(this);material.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));body.addView(text("Material / alat",14,true));body.addView(material);
        EditText qty=new EditText(this);qty.setHint("Jumlah");qty.setInputType(8194);body.addView(text("Jumlah",14,true));body.addView(qty);
        EditText note=new EditText(this);note.setHint("Keterangan transaksi");body.addView(text("Keterangan",14,true));body.addView(note);
        body.addView(button("Simpan Transaksi",()->{
            try{if(ids.isEmpty()||qty.getText().length()==0)throw new Exception();String date=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
                boolean ok=db.transact(ids.get(material.getSelectedItemPosition()),date,kind.getSelectedItem().toString(),Double.parseDouble(qty.getText().toString()),note.getText().toString());
                Toast.makeText(this,ok?"Transaksi tersimpan":"Stok tidak cukup untuk transaksi keluar",1).show();if(ok)showDashboard();
            }catch(Exception e){Toast.makeText(this,"Isi jumlah transaksi dengan benar",0).show();}
        }));
    }
    void showReport(){
        frame("Laporan Stok");body.addView(text("Daftar stok saat ini",17,true));
        Cursor c=db.materials("Semua");int n=1;while(c.moveToNext())body.addView(text(n++ +". "+c.getString(1)+"  |  "+c.getDouble(4)+" "+c.getString(2),14,false));c.close();
        body.addView(text("Riwayat transaksi",17,true));Cursor t=db.transactions();while(t.moveToNext())body.addView(text(t.getString(0)+" • "+t.getString(1)+" • "+t.getString(2)+" • "+t.getDouble(3)+" "+t.getString(4)+"\n"+t.getString(5),13,false));t.close();
    }
    void showSettings(){
        frame("Pengaturan");body.addView(button("Export CSV Laporan Stok",()->exportCsv()));
        body.addView(text("Database disimpan lokal di perangkat. Untuk menjaga data, lakukan ekspor CSV secara berkala.",14,false));
        body.addView(button("Kembali ke Dashboard",()->showDashboard()));
    }
    void exportCsv(){
        StringBuilder csv=new StringBuilder("Nama,Satuan,Harga Standar,Stok,Jenis\n");Cursor c=db.materials("Semua");
        while(c.moveToNext()){csv.append('"').append(c.getString(1).replace("\"","\"\"")).append("\",").append(c.getString(2)).append(",").append(c.getDouble(3)).append(",").append(c.getDouble(4)).append(",").append(c.getString(5)).append("\n");}c.close();
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/csv");i.putExtra(Intent.EXTRA_TITLE,"laporan_stok_barang.csv");startActivityForResult(i,77);pendingCsv=csv.toString();
    }
    String pendingCsv="";
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==77&&result==RESULT_OK&&data!=null)try(java.io.OutputStream o=getContentResolver().openOutputStream(data.getData())){o.write(pendingCsv.getBytes("UTF-8"));Toast.makeText(this,"CSV berhasil disimpan",1).show();}catch(Exception e){Toast.makeText(this,"Gagal ekspor CSV",1).show();}}
    @Override public void onBackPressed(){if(!current.equals("Dashboard"))showDashboard();else super.onBackPressed();}
}
