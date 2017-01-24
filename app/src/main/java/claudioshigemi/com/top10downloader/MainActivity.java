package claudioshigemi.com.top10downloader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    //    Logt = cria a linha abaixo
    private static final String TAG = "MainActivity";

    private ListView listApps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);

        Log.d(TAG, "onCreate: iniciando ASynctask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
        Log.d(TAG, "onCreate: terminado.");

    }


    private class DownloadData extends AsyncTask<String, Void, String> {
//            Ctrl + O = para criar  Override  de métodos
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute:  parametro é -->   " + s);

            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(
                    MainActivity.this, R.layout.list_item, parseApplications.getApplications());
            listApps.setAdapter(arrayAdapter);


        }

        @Override
        protected String doInBackground(String... strings) {

            Log.d(TAG, "doInBackground: começa com --> " + strings[0]);

            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Erro ao realizar o Download.");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: O código de resposta --> " + response);

//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while(true) {
                    charsRead = reader.read(inputBuffer);
                    if(charsRead < 0) {
                        break;
                    }
                    if(charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();


                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, " downloadXML: invalido URL --> " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, " downloadXML: IO Exception na leitura de dados --> " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Seguranca da Internet. Necessita de Permissão? " + e.getMessage());
                e.printStackTrace();
            }
            return null;

        }

    }


}
