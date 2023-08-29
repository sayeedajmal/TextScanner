package com.strong.textscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.strong.textscanner.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding BindMain;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private File capturedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BindMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(BindMain.getRoot());

        BindMain.ImageText.setMovementMethod(new ScrollingMovementMethod());
        BindMain.ScanBtn.setOnClickListener(v -> {
            if (checkPermission()) {
                CaptureImage();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void CaptureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            capturedImageFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this, "com.strong.textscanner.fileprovider", capturedImageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, CAMERA_SERVICE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap capturedBitmap = BitmapFactory.decodeFile(capturedImageFile.getPath());
            BindMain.Image.setRotation(90);
            BindMain.Image.setImageBitmap(capturedBitmap);
            DetectText(capturedBitmap);
        }
    }

    // Create a file to store the captured image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void DetectText(Bitmap imageMap) {
        InputImage inputImage = InputImage.fromBitmap(imageMap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(inputImage).addOnSuccessListener(text -> {
            if (text != null) {
                String recognizedText = text.getText();
                BindMain.ImageText.setText(recognizedText);
            } else {
                Toast.makeText(MainActivity.this, "No text was recognized.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                CaptureImage();
            } else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}