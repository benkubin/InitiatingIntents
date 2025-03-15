package com.example.initiatingintents;

import static android.view.View.VISIBLE;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.initiatingintents.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            String savedTopTxt = savedInstanceState.getString("topText");
            String savedBottomTxt = savedInstanceState.getString("bottomText");

            if (savedTopTxt != null) {
                binding.topTextInput.getEditText().setText(savedTopTxt);
            }
            if (savedBottomTxt != null) {
                binding.bottomTextInput.getEditText().setText(savedBottomTxt);
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.takePhotoButton.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });

        binding.addTextBtn.setOnClickListener(v -> {
            if (!binding.topTextInput.getEditText().getText().toString().isEmpty() && !binding.bottomTextInput.getEditText().getText().toString().isEmpty()) {
                binding.topImgText.setText(binding.topTextInput.getEditText().getText().toString());
                binding.topImgText.setVisibility(VISIBLE);

                binding.bottomImgText.setText(binding.bottomTextInput.getEditText().getText().toString());
                binding.bottomImgText.setVisibility(VISIBLE);
            } else if (binding.topTextInput.getEditText().getText().toString().isEmpty() && binding.bottomTextInput.getEditText().getText().toString().isEmpty()) {
                Snackbar error = Snackbar.make(binding.main, "Empty Text", Snackbar.LENGTH_SHORT);
                error.show();
            }

        });

        binding.shareBtn.setOnClickListener(v -> {
            Bitmap composite = createCompositeBitmap(binding.memeContainer);
            Uri imageUri = getImgUri(composite);
            if (imageUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Meme"));
            } else {
                Toast.makeText(this, "Image not available.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            binding.memeView.setImageBitmap(image);

            Uri imageUri = getImgUri(image);
            binding.memeView.setTag(imageUri);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("topText", binding.topTextInput.getEditText().getText().toString());
        outState.putString("bottomText", binding.bottomTextInput.getEditText().getText().toString());
    }

    private Uri getImgUri(Bitmap bitmap) {
        File cachePath = new File(getCacheDir(), "images");
        cachePath.mkdirs();

        File imageFile = new File(cachePath, "shared_img.png");
        try (FileOutputStream stream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileProvider.getUriForFile(this, "com.example.initiatingintents.fileprovider", imageFile);
    }

    public Bitmap createCompositeBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

}