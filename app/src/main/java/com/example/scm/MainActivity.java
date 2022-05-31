package com.example.scm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.SpringAnimation;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nombreProveedor;
    private EditText nombreProducto;
    private EditText codigoProducto;

    ImageButton btnTakePicture;
    EditText etDate;
    DatePickerDialog.OnDateSetListener setListener;

    //-----------------NUEVO CODIGO
    Button btnSavePicture;
    ImageView imageView;
    Bitmap bitmap;

    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int TAKE_PICTURE = 101;

    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 200;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombreProveedor = (EditText) findViewById(R.id.nombreProveedor);
        nombreProducto = (EditText) findViewById(R.id.nombreProducto);
        codigoProducto = (EditText) findViewById(R.id.codigoProducto);
        etDate = (EditText) findViewById(R.id.etPlannedDate);
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        //UI
        initUI();

        btnTakePicture.setOnClickListener(this);
        btnSavePicture.setOnClickListener(this);
//permisos
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1000);
        }

        etDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        String date = day+"/"+month+"/"+year;
                        etDate.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });

        //--------------------------BOTONES ANTIGUOS-------------------------------------------

        /*btn_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        btn_Guardar_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePicture();
            }
        });*/

        //--------------------------------------------------------------------------

    }

    private void initUI() {
        btnSavePicture = findViewById(R.id.btnSaveImage);
        btnTakePicture =findViewById(R.id.btnTakePicture);
        imageView = findViewById(R.id.imageView);

    }



 /*   *//**OVERRIDE **//*
    @Override
   public void onClick(View v){
        int id = v.getId();

        if (id == R.id.btnTakePicture){
            checkPermissionCamera();
        }else if(id == R.id.btnSaveImage){
            checkPermissionStorage();

        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_PICTURE){
            if (resultCode == Activity.RESULT_OK && data != null){
                bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CAMERA){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePicture();
            }
        }else if(requestCode == REQUEST_PERMISSION_WRITE_STORAGE){
            if (permissions.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkPermissionStorage() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    saveImage();
                }else{
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_WRITE_STORAGE
                    );
                }
            }else{
                saveImage();
            }
        }else{
            saveImage();
        }
    }

    private void saveImage(){
        OutputStream fos = null;
        File file = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();


            String fileName = System.currentTimeMillis() + "image_example";

            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri imageuri = resolver.insert(collection,values);

            try {
                fos = resolver.openOutputStream(imageuri);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING,0);
            resolver.update(imageuri,values,null,null);
        }else{
            String imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

            String fileName = System.currentTimeMillis() + ".jpg";
            file = new File(imageDir,fileName);
            try {
                fos = new FileOutputStream(file);

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }

        boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        if (saved){
            Toast.makeText(this, "Picture was saved successfully", Toast.LENGTH_SHORT).show();
        }
        if (fos != null){
            try {
                fos.flush();
                fos.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if (file != null){
            MediaScannerConnection.scanFile(this, new String[]{file.toString()},null,null);

        }
    }

    private void checkPermissionCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                takePicture();
            }else{
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA
                );
            }
        }else{
            takePicture();
        }

    }

    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, TAKE_PICTURE);
        }
    }

    public void Registrar(View view) throws IOException {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String codigo = codigoProducto.getText().toString();
        String proveedor = nombreProveedor.getText().toString();
        String producto = nombreProducto.getText().toString();
        String fechaVto = etDate.getText().toString();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(20480);
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 , baos);
        byte[] blob = baos.toByteArray();



        if (!codigo.isEmpty() && !producto.isEmpty() && !proveedor.isEmpty()){
            ContentValues registro = new ContentValues();
            registro.put("ean", codigo);
            registro.put("producto", producto);
            registro.put("proveedor", proveedor);
            registro.put("fechaVto", fechaVto);
            registro.put("img", blob);



            BaseDeDatos.insert("articulos", null,registro);
            BaseDeDatos.close();

            codigoProducto.setText("");
            nombreProveedor.setText("");
            nombreProducto.setText("");
            etDate.setText("");
            imageView.setImageBitmap(null);
            


            Toast.makeText(this, "Su producto fue guardado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    //metodo insertar imagen

    public void InsertImage(String codigo){
        String stringFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        Bitmap bitmap = BitmapFactory.decodeFile(stringFilePath);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bytesImage = byteArrayOutputStream.toByteArray();

        ContentValues contentValues = new ContentValues();
        contentValues.put("img", bytesImage);
    }

    //Metodo para consultar productos

    public void Buscar (View view){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String codigo = codigoProducto.getText().toString();
        if (!codigo.isEmpty()){
            Cursor fila = BaseDeDatos.rawQuery("select producto, proveedor, fechaVto, img from articulos where ean =" + codigo, null);

                if (fila.moveToFirst()){
                    nombreProveedor.setText(fila.getString(0));
                    nombreProducto.setText(fila.getString(1));
                    etDate.setText(fila.getString(2));
                    byte[] imgByte = fila.getBlob(3);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte,0,imgByte.length);
                    imageView.setImageBitmap(bitmap);
                    BaseDeDatos.close();
                }else{
                    Toast.makeText(this, "No existe el articulo", Toast.LENGTH_SHORT).show();
                    BaseDeDatos.close();
                }
        } else {
            Toast.makeText(this, "Debes introducir el codigo", Toast.LENGTH_SHORT).show();
        }

    }

    //CODIGO ANTERIOR
/*
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iv_mini.setImageBitmap(imageBitmap);
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Backup_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    public void savePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.scm.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Toast.makeText(this, "Foto guardada con exito ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }*/


    @Override
    public void onClick(View v) {
//CONDICIONAL PARA BTN CAMARA
        int id = v.getId();

        if (id == R.id.btnTakePicture){
            checkPermissionCamera();
        }else if(id == R.id.btnSaveImage){
            checkPermissionStorage();

        }
    }


}