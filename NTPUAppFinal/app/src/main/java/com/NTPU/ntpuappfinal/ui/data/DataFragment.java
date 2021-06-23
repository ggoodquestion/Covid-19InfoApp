package com.NTPU.ntpuappfinal.ui.data;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.NTPU.ntpuappfinal.R;
import com.NTPU.ntpuappfinal.SocketCilent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Array;
import java.sql.ClientInfoStatus;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DataFragment extends Fragment {

    TextView g_case, g_death, g_cfr, c_case, c_cfr, c_death, t_case, tt_case, t_death, t_cfr;
    Spinner choose_country;
    SocketCilent client, client2, client3;
    CheckSocket check, check2, check3;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data, container, false);

        g_case = root.findViewById(R.id.g_case);
        g_death = root.findViewById(R.id.g_death);
        g_cfr = root.findViewById(R.id.g_cfr);

        c_case = root.findViewById(R.id.c_case);
        c_death = root.findViewById(R.id.c_death);
        c_cfr = root.findViewById(R.id.c_cfr);

        t_case = root.findViewById(R.id.t_case);
        tt_case = root.findViewById(R.id.tt_case);
        t_death = root.findViewById(R.id.t_death);
        t_cfr = root.findViewById(R.id.t_cfr);

        choose_country = root.findViewById(R.id.choose_contry);
        choose_country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getSelectedItem().toString().equals("選擇國家")) {
                    ArrayList<String> tmp = client2.getData();
                    String line = "";
                    float caseN = 0, deathN = 0;
                    String caseS = "", deathS = "";
                    for (String str : tmp) {
                        if (str.contains(parent.getSelectedItem().toString())) {
                            line = str.split(",", 3)[2];
                            String s[] = govCSVread(str);
                            caseS = s[2];
                            deathS = s[3];
                            caseN = Float.parseFloat(caseS.replace(",", "").replace("\"", ""));
                            deathN = Float.parseFloat(deathS.replace(",", "").replace("\"", ""));
                            c_case.setText("感染數: " + caseS.replace("\"", ""));
                            c_death.setText("死亡數: " + deathS.replace("\"", ""));
                            DecimalFormat fnum = new DecimalFormat("##0.00");
                            String dd = fnum.format((deathN / caseN) * 100);
                            if ((deathN / caseN) * 100 < 0.1f) c_cfr.setText("死亡率: <0.1%");
                            else c_cfr.setText("死亡率: " + dd + "%");
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        client = new SocketCilent("https://od.cdc.gov.tw/eic/covid19/covid19_global_stats.csv");
        client.start();
        check = new CheckSocket(client, 1);
        check.start();

        client2 = new SocketCilent("https://od.cdc.gov.tw/eic/covid19/covid19_global_cases_and_deaths.csv");
        client2.start();
        check2 = new CheckSocket(client2, 2);
        check2.start();

        client3 = new SocketCilent("https://od.cdc.gov.tw/eic/covid19/covid19_tw_stats.csv");
        client3.start();
        check3 = new CheckSocket(client3, 3);
        check3.start();

        return root;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try{
                switch (msg.what) {
                    case 1:
                        ArrayList<String> tmp = client.getData();
                        String gdata[] = tmp.get(1).split("\",");
                        g_case.setText("感染數: " + gdata[0].replace("\"", ""));
                        g_death.setText("死亡數: " + gdata[1].replace("\"", ""));
                        g_cfr.setText("死亡率: " + gdata[2].split(",")[0]);
                        break;
                    case 2:
                        ArrayList<String> tmp2 = client2.getData();
                        String src[] = new String[196];
                        Log.d("size", "size" + tmp2.size());
                        src[0] = "選擇國家";
                        for (int i = 1; i < tmp2.size(); i++) {
                            String tar = tmp2.get(i).split(",")[0];
                            src[i] = tar;
                        }
                        ArrayAdapter countries = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, src);
                        choose_country.setAdapter(countries);
                        break;
                    case 3:
                        ArrayList<String> tmp3 = client3.getData();
                        String ddata[] = govCSVread(tmp3.get(1));

                        t_case.setText("昨日確診: " + ddata[4].replace("\"", ""));
                        tt_case.setText("確診總數: " + ddata[0].replace("\"", ""));
                        t_death.setText("死亡數: " + ddata[1].replace("\"", ""));
                        Float tc, td;
                        tc = Float.parseFloat(ddata[0].replace("\"", "").replace(",", ""));
                        td = Float.parseFloat(ddata[1].replace("\"", "").replace(",", ""));
                        DecimalFormat fnum = new DecimalFormat("##0.00");
                        String dd = fnum.format((td / tc) * 100);
                        t_cfr.setText("死亡率: " + dd + "%");
                        break;
                }
            }catch (Exception e){
                Log.e("Grap data", e.getMessage() );
            }
        }
    };

    public class CheckSocket extends Thread {
        SocketCilent sc;
        int msg;

        public CheckSocket(SocketCilent sclient, int msg) {
            this.sc = sclient;
            this.msg = msg;
        }

        @Override
        public void run() {
            while (!sc.isFinish()) {
//                    try {
//                        wait(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
            }
            Message msg = new Message();
            msg.what = this.msg;
            mHandler.sendMessage(msg);
        }
    }

    public String[] govCSVread(String src) {
        ArrayList<String> tmp = new ArrayList<>();
        boolean quoatFlag = false;
        int hcut = 0;
        int tcut = 0;
        for (int i = 0; i < src.length(); i++) {
            if (!quoatFlag) {
                if (src.charAt(i) == '\"') quoatFlag = true;
                else if (src.charAt(i) == ',') {
                    tcut = i;
                    String s = src;
                    s = s.substring(hcut, tcut);
                    Log.d("cut", s);
                    tmp.add(s);
                    hcut = tcut + 1;
                }
            } else if (quoatFlag) {
                if (src.charAt(i) == '\"') quoatFlag = false;
            }
        }
        tmp.add(src.substring(hcut, src.length()));
        String res[] = new String[tmp.size()];
        int i = 0;
        for (String s : tmp) {
            res[i++] = s;
        }
        return res;
    }
}