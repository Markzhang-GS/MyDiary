package com.kiminonawa.mydiary.backup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kiminonawa.mydiary.R;
import com.kiminonawa.mydiary.db.DBManager;
import com.kiminonawa.mydiary.shared.FileManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by daxia on 2017/2/16.
 */

public class ImportAsyncTask extends AsyncTask<Void, Void, Boolean> {

    public interface ImportCallBack {
        void onImportCompiled(boolean importSuccessful);
    }

    private final static String TAG = "ImportAsyncTask";


    /*
     * Backup
     */
    private BackupManager backupManager;
    private String backupJsonFilePath;
    private String backupZieFilePath;
    /*
     * DB
     */
    private DBManager dbManager;

    /*
     * UI
     */
    //From MyDiaryApplication Context
    private Context mContext;
    private ProgressDialog progressDialog;
    private ImportCallBack callBack;

    public ImportAsyncTask(Context context, ImportCallBack callBack, String backupZieFilePath) {
        this.mContext = context;
        this.dbManager = new DBManager(context);
        this.backupJsonFilePath = new FileManager(context, FileManager.BACKUP_DIR).getDirAbsolutePath() + "/"
                + BackupManager.BACKUP_JSON_FILE_NAME;
        this.backupZieFilePath = backupZieFilePath;
        this.callBack = callBack;
        this.progressDialog = new ProgressDialog(context);

        //Init progressDialog
        progressDialog.setMessage(context.getString(R.string.process_dialog_loading));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean importSuccessful = true;
        try {
            ZipManager zipManager = new ZipManager(mContext);
            zipManager.unzip(backupZieFilePath,
                    new FileManager(mContext, FileManager.BACKUP_DIR).getDirAbsolutePath() + "/");
            loadBackupJsonFileIntoManager();
        } catch (Exception e) {
            Log.e(TAG, "export fail", e);
            importSuccessful = false;
        }
        return importSuccessful;
    }

    @Override
    protected void onPostExecute(Boolean importSuccessful) {
        super.onPostExecute(importSuccessful);
        progressDialog.dismiss();
        if (importSuccessful) {
            Toast.makeText(mContext, "匯入成功", Toast.LENGTH_LONG);
        } else {
            Toast.makeText(mContext, "匯入失敗...請檢查檔案或版本", Toast.LENGTH_LONG);
        }
        //callBack.onImportCompiled(importSuccessful);
    }

    private void loadBackupJsonFileIntoManager() throws Exception {
        FileInputStream fis = new FileInputStream(backupJsonFilePath);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        String json = sb.toString();
        Gson gson = new Gson();
        BackupManager test = gson.fromJson(json, BackupManager.class);
        Log.e("BackupManager", "header = " + test.getHeader());
        Log.e("BackupManager", "time = " + test.getCreate_time());
        if (test.getHeader() == null ||
                !test.getHeader().equals(BackupManager.header)) {
            throw new Exception("This is not mydiary backup file");
        }
    }


}
