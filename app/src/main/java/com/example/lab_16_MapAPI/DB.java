package com.example.lab_16_MapAPI;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import androidx.annotation.Nullable;

import com.example.lab_16_MapAPI.model.Tile;

import java.util.ArrayList;
import java.util.Date;

public class DB extends SQLiteOpenHelper
{
    public DB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE Settings (http TEXT, ofsX REAL, ofsY REAL, scale INT, colorCoastlines INT, colorRivers INT, colorRailroads INT, colorRoads INT, lifetimeTile INT);";
        db.execSQL(sql);

        sql = "INSERT INTO Settings VALUES ('" + "http://labs-api.spbcoit.ru/lab/tiles/api" + "', " + 0 + ", " + 0 + ", " + 0 + "," +
                "" + Color.DKGRAY + ", " + Color.BLUE + ", " + Color.GRAY + ", " + Color.BLACK + ", " + 1000 + ");";
        db.execSQL(sql);

        sql = "CREATE TABLE ChecksShapes (coastlines INT, rivers INT, railroads INT, roads INT);";
        db.execSQL(sql);

        sql = "INSERT INTO ChecksShapes VALUES (" + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ");";
        db.execSQL(sql);

        sql = "CREATE TABLE Tiles (x INT, y INT, scale INT, image TEXT, time INT, CONSTRAINT tilePK PRIMARY KEY (x, y, scale));";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }

    public void InsertTile(int x, int y, int scale, String image, long time)
    {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "INSERT INTO Tiles VALUES(" + x + ", " + y + ", " + scale + ", '" + image + "', " + time + ");";

        try
        {
            db.execSQL(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void InsertTile(Tile tile, int time)
    {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "INSERT INTO Tiles VALUES(" + tile.x + ", " + tile.y + ", " + tile.level + ", '" + tile.image + "', " + time + ");";

        try
        {
            db.execSQL(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Tile GetTile(int x, int y, int scale)
    {
        Tile t = null;

        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT image FROM Tiles WHERE x = " + x + " AND y = " + y + " AND scale = " + scale + ";";

        try {
            Cursor cur = db.rawQuery(sql, null);

            if (cur.moveToFirst() == true)
            {
                do {
                    t = new Tile(x, y, scale, cur.getString(0));
                }while (cur.moveToNext() == true);
            }
        }
        catch (Exception e)
        {
            return t;
        }

        return t;
    }

    public void CheckTile()
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM Tiles WHERE time <= " + new Date().getTime() + ";";

        db.execSQL(sql);
    }

    public void UpdateCheckShapes(int[] checks)
    {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "UPDATE ChecksShapes SET coastlines = " + checks[0] + ", rivers = " + checks[1] + ", railroads = " + checks[2] + ", roads = " + checks[3] + ";";

        try
        {
            db.execSQL(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public int[] GetChecksShapes()
    {
        int[] checks = new int[4];

        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM ChecksShapes;";
        Cursor cur = db.rawQuery(sql, null);

        if (cur.moveToFirst() == true)
        {
            for (int i = 0; i < 4; i++)
            {
                checks[i] = cur.getInt(i);
            }

            return checks;
        }

        return null;
    }

    public void UpdateFieldInSetting(String nameSetting, Object value)
    {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "UPDATE Settings SET " + nameSetting + " = " + value + ";";

        try
        {
            db.execSQL(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Object GetSetting(String nameSetting)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT " + nameSetting + " FROM Settings;";
        Cursor cur = db.rawQuery(sql, null);

        switch (nameSetting)
        {
            case "http":
                if (cur.moveToFirst()) return cur.getString(0);
                break;
            case "ofsX":
            case "ofsY":
                if (cur.moveToFirst()) return cur.getFloat(0);
                break;
            case "scale":
            case "colorCoastlines":
            case "colorRivers":
            case "colorRailroads":
            case "colorRoads":
            case "lifetimeTile":
                if (cur.moveToFirst()) return cur.getInt(0);
                break;
        }

        return 0;
    }
}
