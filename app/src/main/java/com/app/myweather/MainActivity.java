package com.app.myweather;

import androidx.appcompat.app.AppCompatActivity;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
public class MainActivity extends AppCompatActivity {
    TextView tempYa;
    TextView tempGis;
    TextView tempMail;
    private Button upd_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tempYa = (TextView) findViewById(R.id.tempYa);
        tempGis = (TextView) findViewById(R.id.tempGis);
        tempMail = (TextView) findViewById(R.id.tempMail);
        upd_btn = findViewById(R.id.button);
        new PogodaThread().execute();
        upd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PogodaThread().execute();
            }
        });
    }
    class PogodaThread extends AsyncTask<String, Void, String> {
     // Метод выполняющий запрос в фоне, в версиях выше 4 андроида, запросы в главном потоке выполнять
     // нельзя, поэтому все что нужно выполнять - выносим в отдельный тред
        @Override
        protected String doInBackground(String... arg) {
            String pogoda;
            String yaPogoda = null;
            String gisPogoda = null;
            String mailPogoda = null;

            try {
              //парсинг с яндекса
              Document docYa = Jsoup.connect("https://yandex.ru/pogoda/yekaterinburg").get();
              Elements tempYa = docYa.getElementsByClass("temp__value_with-unit");
              Elements windYa = docYa.getElementsByClass("a11y-hidden");
              Element elTempYa = tempYa.get(1);
              Element elWindYa = windYa.get(1);
              yaPogoda = String.valueOf(elTempYa.childNodes().get(0)).trim() + " " + elWindYa.childNodes().get(0) ;

              //парсинг с гизметео
              Document docGis = Jsoup.connect("https://www.gismeteo.ru/weather-yekaterinburg-4517/now/").get();
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

              //  парсинг с Мейл.ру
              Document docMail = Jsoup.connect("https://pogoda.mail.ru/prognoz/ekaterinburg/").get();
              Elements tempMail = docMail.getElementsByClass("information__content__temperature");
              Elements windMail = docMail.getElementsByClass("information__content__additional__item");
              Element elTempMail = tempMail.get(0);
             Element elWindMail = windMail.get(5);
              mailPogoda = elTempMail.childNodes().get(2).toString().trim() + " "
              + elWindMail.childNodes().get(1).toString().trim()+" ";


            } catch (IOException e) {
                e.printStackTrace();
            }
            pogoda =yaPogoda + " " + gisPogoda + " " + mailPogoda;

            return pogoda;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //разбиваем резултат на массив слов
            String[] words = result.split(" ");
            //готовим текстовые предложения и отправляем в форму главного окна
         tempYa.setText("На яндексе сейчас: " + words[0] + " " + words[1] + " " +
                    words[2] + " " + words[3] + " " + words[4] + " " + words[5] + " " + words[6]);

         tempGis.setText("На Gismeteo сейчас: "  + words[7] + " " + words[8] + " " + words[9] + " "
                   + words[10] + " " + words[11] +" "+ words[12]);

         tempMail.setText("На mail.ru сейчас: " + words[13] + " " + words[15].substring(7)
                 + " " + words[16]+ " " + words[17]+ " " + words[18]+ " ");
        }
    }
}