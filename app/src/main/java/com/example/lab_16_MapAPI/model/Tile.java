package com.example.lab_16_MapAPI.model;

import static com.example.lab_16_MapAPI.MapView.database;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONObject;

import android.util.Base64;

import com.example.lab_16_MapAPI.APIHelper;

import java.util.Date;

public class Tile
{
    public int x;
    public int y;
    public Bitmap img;
    public String image;
    public int level;

    public Tile(int x, int y, int level, Activity ctx, String base, int lifetime)
    {
        this.x = x;
        this.y = y;
        this.level = level;
        this.img = null;
        this.image = "";

        APIHelper req = new APIHelper(ctx)
        {
            @Override
            public void on_ready(String res)
            {
                try {
                    JSONObject obj = new JSONObject(res);
                    String b64 = obj.getString("data");
                    image = b64;
                    byte[] pic = Base64.decode(b64, Base64.DEFAULT);
                    img = BitmapFactory.decodeByteArray(pic, 0, pic.length);

                    Runnable insertTileInDB = () ->
                    {
                        database.InsertTile(x, y, level, image, new Date().getTime() + lifetime);
                    };

                    new Thread(insertTileInDB).start();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        req.send(base + "raster/" + level + "/" + x + "-" + y);
    }

    public Tile(int x, int y, int level, String image)
    {
        this.x = x;
        this.y = y;
        this.level = level;
        byte[] pic = Base64.decode(image, Base64.DEFAULT);
        this.img = BitmapFactory.decodeByteArray(pic, 0,pic.length);
    }
}
