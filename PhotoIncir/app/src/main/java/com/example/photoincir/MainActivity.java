package com.example.photoincir;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;

    private ImageView imageView;
    private Button btnGallery, btnCamera;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar'ı ayarlama
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // View bileşenlerini tanımlama
        imageView = findViewById(R.id.imageView);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        statusTextView = findViewById(R.id.statusTextView);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Kamera butonuna tıklanınca
        btnCamera.setOnClickListener(v -> checkAndRequestPermission(
                Manifest.permission.CAMERA, CAMERA_REQUEST_CODE, this::openCamera));
    }

    // İzinleri kontrol eden yardımcı fonksiyon
    private void checkAndRequestPermission(String permission, int requestCode, Runnable onPermissionGranted) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, permission + " izni isteniyor...", Toast.LENGTH_SHORT).show();  // Kontrol amaçlı Toast
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            onPermissionGranted.run();
        }
    }

    // Galeri açma işlemi
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    // Kamera açma işlemi
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Toast.makeText(this, "Kamera izni verildi!", Toast.LENGTH_SHORT).show();
                openCamera();
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Toast.makeText(this, "Galeri izni verildi!", Toast.LENGTH_SHORT).show();
                openGallery();
            }
        } else {
            Toast.makeText(this, "İzin gerekli!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    Bitmap squareBitmap = cropToSquare(bitmap);
                    imageView.setImageBitmap(squareBitmap);
                    statusTextView.setText("Galeri fotoğrafı yüklendi.");
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Fotoğraf yüklenirken bir hata oluştu.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap squarePhoto = cropToSquare(photo);
                imageView.setImageBitmap(squarePhoto);
                statusTextView.setText("Kamera fotoğrafı çekildi.");
            }
        }
    }

    // Kare formatta kırpma işlemi
    private Bitmap cropToSquare(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(width, height);
        int newHeight = newWidth;
        int cropW = (width - newWidth) / 2;
        int cropH = (height - newHeight) / 2;
        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }
}
