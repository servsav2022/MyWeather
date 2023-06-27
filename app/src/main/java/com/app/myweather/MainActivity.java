package com.app.myweather;

import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.type.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView tempYa;
    TextView tempGis;
    TextView tempMail;
    TextInputEditText city;
    TextView dateUpdate;
    private Button upd_btn;

    String yaConnect="https://yandex.ru/pogoda/yekaterinburg";
    String gisConnect="https://www.gismeteo.ru/weather-yekaterinburg-4517/now/";
    String mailConnect="https://pogoda.mail.ru/prognoz/ekaterinburg/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempYa = (TextView) findViewById(R.id.tempYa);
        tempGis = (TextView) findViewById(R.id.tempGis);
        tempMail = (TextView) findViewById(R.id.tempMail);
        dateUpdate = (TextView) findViewById(R.id.dateUpdate);
        upd_btn = findViewById(R.id.button);

        city = (TextInputEditText) findViewById(R.id.cityInput);
        String[] cityNames = getResources().getStringArray(R.array.city_names);//0-314
        String[] yaConnects=getResources().getStringArray(R.array.ya_connections);
        String[] gisConnects=getResources().getStringArray(R.array.gis_connections);
        String[] mailConnects=getResources().getStringArray(R.array.mail_connections);// 0-314
        new PogodaParsing().execute();
        upd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (city.getText().toString().trim().equals("")) {
                    yaConnect="https://yandex.ru/pogoda/yekaterinburg";
                    gisConnect="https://www.gismeteo.ru/weather-yekaterinburg-4517/now/";
                    mailConnect="https://pogoda.mail.ru/prognoz/ekaterinburg/";
                    city.setText("Екатеринбург");
                }
            //
                //tests
                String sSity = city.getText().toString().trim().toLowerCase(Locale.ROOT);
                int index =-1; // not found
                for(int i = 0; i < cityNames.length; i++)
                {
                    if(cityNames[i].toLowerCase(Locale.ROOT).equals(sSity))
                    {
                        index = i;
                        //upd_btn.setText(cityNames[index]);
                        yaConnect=yaConnects[index];
                        gisConnect=gisConnects[index];
                        mailConnect=mailConnects[index];
                        new PogodaParsing().execute();
                        break; // stop looking
                    }
                };
                if (index==-1) {
                    yaConnect="https://yandex.ru/pogoda/yekaterinburg";
                    gisConnect="https://www.gismeteo.ru/weather-yekaterinburg-4517/now/";
                    mailConnect="https://pogoda.mail.ru/prognoz/ekaterinburg/";
                    city.setText("Екатеринбург");
                    new PogodaParsing().execute();
                }
            };

        });
    }
    class PogodaParsing extends AsyncTask<String, String, String> {
     // Метод выполняющий запрос в фоне, в версиях выше 4 андроида, запросы в главном потоке выполнять
     // нельзя, поэтому все что нужно выполнять - выносим в отдельный тред
         @Override
         protected void onPreExecute() {
            super.onPreExecute();
             tempYa.setText("Загрузка...");
             tempGis.setText("Загрузка...");
             tempMail.setText("Загрузка...");
        }
        @Override
        protected String doInBackground(String... arg) {
            String pogoda;
            String yaPogoda = null;
            String gisPogoda = null;
            String mailPogoda = null;
            String dateUpdate = null;

            try {
              //парсинг с яндекса
              Document docYa = Jsoup.connect(yaConnect).get();
              Elements tempYa = docYa.getElementsByClass("temp__value_with-unit");
              Elements windYa = docYa.getElementsByClass("a11y-hidden");
              Element elTempYa = tempYa.get(1);
              Element elWindYa = windYa.get(1);
              yaPogoda = String.valueOf(elTempYa.childNodes().get(0)).trim() + " " + elWindYa.childNodes().get(0) ;
            } catch (IOException e) {
                e.printStackTrace();
                // Обработка ошибки подключения или получения данных для яндекса
                yaPogoda = "Ошибка получения данных с яндекса";
            }

            try {
              //парсинг с гизметео
              Document docGis = Jsoup.connect(gisConnect).get();
              Elements tempGis = docGis.getElementsByClass("unit unit_temperature_c");
              Elements signTempGis = docGis.getElementsByClass("sign");
              Elements windGis = docGis.getElementsByClass("unit unit_wind_m_s");
              Elements windGis2 = docGis.getElementsByClass("item-measure");
              Element elGisTemp = tempGis.get(0);
              Element elSignTempGis = signTempGis.get(0);
              Element elWindGis = windGis.get(0);
              Element elWindGis2 = windGis2.get(0);
              gisPogoda = String.valueOf(elSignTempGis.childNodes().get(0)).trim() + " "
                      + String.valueOf(elGisTemp.childNodes().get(1)).trim() + " Ветер: "+
                      String.valueOf(elWindGis.childNodes().get(0)).trim() + " м/с " +
                      elWindGis2.childNodes().get(1).childNode(0).toString().trim();
            } catch (IOException e) {
                e.printStackTrace();
                // Обработка ошибки подключения или получения данных для гизметео
                gisPogoda = "Ошибка получения данных с гизметео";
            }

            try {
              //  парсинг с Мейл.ру
              Document docMail = Jsoup.connect(mailConnect).get();
              Elements tempMail = docMail.getElementsByClass("information__content__temperature");
              Elements windMail = docMail.getElementsByClass("information__content__additional__item");
              Element elTempMail = tempMail.get(0);
              Element elWindMail = windMail.get(5);
              if(city.getText().toString().trim().toLowerCase(Locale.ROOT).equals("санкт-петербург")){
                    elWindMail = windMail.get(6);
              }
              if(city.getText().toString().trim().toLowerCase(Locale.ROOT).equals("севастополь")){
                    elWindMail = windMail.get(6);
              }
              mailPogoda = elTempMail.childNodes().get(2).toString().trim() + " "
              + elWindMail.childNodes().get(1).toString().trim()+" ";
            } catch (IOException e) {
                e.printStackTrace();
                // Обработка ошибки подключения или получения данных для Мейл.ру
                mailPogoda = "Ошибка получения данных с Мейл.ру";
            }

            pogoda =yaPogoda + " " + gisPogoda + " " + mailPogoda;
            return pogoda;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                dateUpdate.setText("Обновлено в " +currentTime+"\n " + currentDate);
            }

            //разбиваем резултат на массив слов
            String[] words = result.split(" ");
            //готовим текстовые предложения и отправляем в форму главного окна
            tempYa.setText("На яндексе сейчас: " + words[0] + " " + words[1] + " " +
                    words[2] + " " + words[3] + " " + words[4] + " " + words[5] + " " + words[6]);

            tempGis.setText("На Gismeteo сейчас: " + words[7] + " " + words[8] + " " + words[9] + " "
                    + words[10] + " " + words[11] + " " + words[12]);

            tempMail.setText("На mail.ru сейчас: " + words[13] + " " + words[15].substring(7)
                    + " " + words[16] + " " + words[17] + " " + words[18] + " ");
        }
    }
}