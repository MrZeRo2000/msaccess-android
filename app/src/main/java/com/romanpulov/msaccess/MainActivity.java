package com.romanpulov.msaccess;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.romanpulov.jutilscore.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FILE = 2;
    private static final String FILE_NAME = "Cat2000.mdb";

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                try (
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        OutputStream outputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
                )
                {
                    FileUtils.copyStream(inputStream, outputStream);
                    displayMessage("File loaded");
                } catch (IOException e) {
                    displayMessage("File not loaded:" + e.getMessage());
                    e.printStackTrace();
                }
            });

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }

    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupButtons();
    }

    private void setupButtons() {
        Button openFileButton = findViewById(R.id.open_file_button);
        openFileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            mGetContent.launch("*/*");
        });

        Button openTableButton = findViewById(R.id.open_table_button);
        openTableButton.setOnClickListener(v -> {
            File file = new File(getFilesDir(), FILE_NAME);
            if (file.exists()) {
                try {
                    Database database = DatabaseBuilder.open(file);
                    Table table = database.getTable("ArtistList");
                    displayMessage("Columns:" + table.getColumns().toString());
                    List<? extends Column> columns = table.getColumns();
                    for(Row row : table) {
                        for (Column column: columns) {
                            log("Column " + column.getName() + " has value: " + row.get(column.getName()));
                        }
                    }
                } catch (IOException e) {
                    displayMessage("Error opening table:" + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                displayMessage("File " + FILE_NAME + " does not exist");
            }

        });
    }
}