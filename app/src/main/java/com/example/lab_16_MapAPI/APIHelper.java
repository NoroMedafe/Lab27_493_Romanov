package com.example.lab_16_MapAPI;

import android.app.Activity;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIHelper
{
    Activity ctx;

    public APIHelper(Activity ctx)
    {
        this.ctx = ctx;
    }

    public void on_ready(String res)
    {

    }

    public void on_fail()
    {

    }

    String http_get(String req)
    {
        try {
            URL url = new URL(req);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedInputStream inp = new BufferedInputStream(con.getInputStream());

            byte[] buf = new byte[512];
            String res = "";

            while (true) {
                int num = inp.read(buf);
                if (num < 0) break;

                res += new String(buf, 0, num);
            }

            con.disconnect();

            return res;
        }
        catch (Exception e)
        {
            return "";
        }
    }

    public class NetOp implements Runnable
    {
        public String req;

        @Override
        public void run()
        {
            String res = http_get(req);

            if (res.equals(""))
            {
                on_fail();
                return;
            }

            ctx.runOnUiThread(() -> on_ready(res));
        }
    }

    public void send(String req)
    {
        NetOp nop = new NetOp();
        nop.req = req;

        Thread th = new Thread(nop);
        th.start();
    }
}
