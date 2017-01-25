package claudioshigemi.com.top10downloader;

import android.os.AsyncTask;
import android.os.PersistableBundle;
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
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
     private String feedCacheUrl = "INVALIDATED";
    private static final String STATE_URL = "feedUrl";
    private static final String STATE_lIMIT = "feedlIMIT";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if (feedLimit == 10){
            menu.findItem(R.id.menu10).setChecked(true);
        }else{
            menu.findItem(R.id.menu25).setChecked(true);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState ) {
        outState.putString(STATE_URL,feedUrl);
        outState.putInt(STATE_lIMIT,feedLimit);
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.menuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.menu10:
            case R.id.menu25:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + "ajustando o feedlimit para " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + "feedLimit nao alterado .");
                }
                break;
            case R.id.menuRefresh:
                feedCacheUrl = "Invalidado.";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }



        downloadUrl(String.format(feedUrl, feedLimit));
        return true;
    }

    private void downloadUrl(String feedUrl) {

        if (!feedUrl.equalsIgnoreCase(feedCacheUrl)){
            Log.d(TAG, "downloadUrl: iniciando ASynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);
            feedCacheUrl = feedUrl;
            Log.d(TAG, "downloadUrl: terminado.");
        }else{
            Log.d(TAG, "downloadUrl: URL não mudou ");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);

        if (savedInstanceState != null){
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_lIMIT);
        }

        downloadUrl(String.format(feedUrl, feedLimit));

    }


    private class DownloadData extends AsyncTask<String, Void, String> {
        //            Ctrl + O = para criar  Override  de métodos
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.d(TAG, "onPostExecute:  parametro é -->   " + s);

            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            FeedAdapter <FeedEntry> feedAdapter = new FeedAdapter<>(MainActivity.this, R.layout.list_record,
                    parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(
//                    MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);


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
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
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
