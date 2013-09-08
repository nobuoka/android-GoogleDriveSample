package info.vividcode.android.app.googledrivesample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class MainActivity extends Activity {
    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final int REQUEST_AUTHORIZATION = 2;

    static final String FILE_TITLE = "google_drive_test";

    private Drive service = null;
    private GoogleAccountCredential credential = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (service == null) {
            credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }

        ((Button)findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("save");
                saveTextToDrive();
            }
        });
        ((Button)findViewById(R.id.loadButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("load");
                loadTextFromDrive();
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    saveTextToDrive();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }
                break;
        }
    }

    private void saveTextToDrive() {
        final String inputText = ((EditText)findViewById(R.id.editText)).getText().toString();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 指定のタイトルのファイルの ID を取得
                    String fileIdOrNull = null;
                    FileList list = service.files().list().execute();
                    for (File f : list.getItems()) {
                        if (FILE_TITLE.equals(f.getTitle())) {
                            fileIdOrNull = f.getId();
                        }
                    }

                    File body = new File();
                    body.setTitle(FILE_TITLE);//fileContent.getName());
                    body.setMimeType("text/plain");

                    ByteArrayContent content = new ByteArrayContent("text/plain", inputText.getBytes(Charset.forName("UTF-8")));
                    if (fileIdOrNull == null) {
                        service.files().insert(body, content).execute();
                        showToast("insert!");
                    } else {
                        service.files().update(fileIdOrNull, body, content).execute();
                        showToast("update!");
                    }
                    // TODO 失敗時の処理?
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("error occur...");
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void loadTextFromDrive() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 指定のタイトルのファイルの ID を取得
                    String fileIdOrNull = null;
                    FileList list = service.files().list().execute();
                    for (File f : list.getItems()) {
                        if (FILE_TITLE.equals(f.getTitle())) {
                            fileIdOrNull = f.getId();
                        }
                    }

                    InputStream is = null;
                    if (fileIdOrNull != null) {
                        File f = service.files().get(fileIdOrNull).execute();
                        is = downloadFile(service, f);
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    try {
                        StringBuffer sb = new StringBuffer();
                        sb.append(br.readLine());

                        final String text = sb.toString();
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                ((EditText)findViewById(R.id.editText)).setText(text);
                            }
                        });
                    } finally {
                        if (br != null) br.close();
                    }
                    // TODO 失敗時の処理?
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("error occur...");
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    // https://developers.google.com/drive/v2/reference/files/get より
    private static InputStream downloadFile(Drive service, File file) {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                HttpResponse resp =
                        service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
                                .execute();
                return resp.getContent();
            } catch (IOException e) {
                // An error occurred.
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .build();
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
