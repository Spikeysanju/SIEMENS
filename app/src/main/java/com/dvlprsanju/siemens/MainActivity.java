package com.dvlprsanju.siemens;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static com.itextpdf.text.PageSize.A4;

public class MainActivity extends AppCompatActivity {

    ProgressDialog pd;
    private Button btn;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    private GridView gvGallery;
    private GalleryAdapter galleryAdapter;
    ArrayList<Uri> mArrayUri;

    public static final String DEST = Environment.getExternalStorageDirectory().getPath() + "/siemensPDF/"+"Siemens.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pd = new ProgressDialog(MainActivity.this);



        btn = findViewById(R.id.btn);
        gvGallery = (GridView)findViewById(R.id.gv);

        // select multiple image from gallery

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);


        // Saving the image of Gallery adapter Card view

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                pd.setMessage("saving your image");
                pd.show();
                LinearLayout savingLayout =(LinearLayout) findViewById(R.id.linearForSave);
                File file = saveBitMap(MainActivity.this, savingLayout);
                if (file != null) {
                    pd.cancel();
                    Log.i("TAG", "Drawing saved to the gallery!");

                } else {
                    pd.cancel();
                    Log.i("TAG", "Oops! Image could not be saved.");
                }
                return false;
            }
        });


        // Generate PDF every time when clicking

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Document document = new Document(A4);

                String root = Environment.getExternalStorageDirectory().toString();

                File myDir = new File(root + "/siemensPDF");
                myDir.mkdir();


                try {
                    PdfWriter.getInstance(document,new FileOutputStream(DEST));
                    document.open();

                    addTitlePage(document);
                    document.close();

                    Snackbar.make(view, "Created PDF", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                } catch (DocumentException | IOException e) {

                    Snackbar.make(view, "Error Occured", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }


            }
        });


    }


// fuction to create pdf

    private void addTitlePage(Document document) throws DocumentException, IOException {

        Font googlesans = new Font(Font.FontFamily.TIMES_ROMAN,18,Font.BOLD,BaseColor.BLACK);
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN,22,Font.BOLD|Font.UNDERLINE,BaseColor.GRAY);

        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD,BaseColor.BLACK);
        Font normal = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.NORMAL,BaseColor.BLACK);

        Paragraph prHead = new Paragraph();
        prHead.setSpacingAfter(30f);
        prHead.setFont(titleFont);
        prHead.setAlignment(Element.ALIGN_CENTER);
        prHead.add("SIEMENS"+ "\n");
        prHead.setFont(googlesans);


        String no = "+91-8939420381";
        String name = "Sanju";
        String paraps = "When technologies provide sustainable growth that benefits everyone - Siemens. Innovative technology can help to turn India's vast potential into reality. Moving Societies. Optimizing infrastructure. Sustainable development. Reliable energy. Future of Manufacturing. Shape the Future.\n";
        String address = "No - 4/14A Kaandha Kannan St, Kandha Sammy Nagar, Karambakkam, Porur, Chennai -600116";

        Paragraph para = new Paragraph();
        para.setSpacingAfter(30f);
        para.setFont(normal);
        para.setAlignment(Element.ALIGN_LEFT);
        para.add(paraps);
        prHead.setSpacingAfter(30f);

        prHead.setAlignment(Element.ALIGN_LEFT);

        // New Para in PDF

        Paragraph pPersonalInfo = new Paragraph();
        pPersonalInfo.setFont(smallBold);
        pPersonalInfo.add("Address - " + address + "\n");
        pPersonalInfo.add("Mobile:" + no);
        pPersonalInfo.setAlignment(Element.ALIGN_LEFT);


            // get input stream
            InputStream img = getAssets().open("gicon.png");

            Bitmap bmp = BitmapFactory.decodeStream(img);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image image = Image.getInstance(stream.toByteArray());
            image.scaleToFit(150, 150);

        // get input stream
        InputStream ims = getAssets().open("apple.png");
        Bitmap bimp = BitmapFactory.decodeStream(ims);
        ByteArrayOutputStream strm = new ByteArrayOutputStream();
        bimp.compress(Bitmap.CompressFormat.PNG, 100, strm);
        Image image1 = Image.getInstance(strm.toByteArray());
        image1.scaleToFit(150, 150);
        image1.setSpacingAfter(30f);





//        // Add Multiple Images
//        InputStream ims = getAssets().open("apple.png");
//        Bitmap bimp = BitmapFactory.decodeStream(i);
//        ByteArrayOutputStream strm = new ByteArrayOutputStream();
//        bimp.compress(Bitmap.CompressFormat.PNG, 100, strm);
//        Image image1 = Image.getInstance(strm.toByteArray());
//        image1.scaleToFit(150, 150);
//        image1.setSpacingAfter(30f);





        document.add(prHead);
        document.add(para);
        document.add(pPersonalInfo);
        document.add(image);
        document.add(image1);
        document.newPage();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();

                    mArrayUri = new ArrayList<Uri>();

                    mArrayUri.add(mImageUri);

                    galleryAdapter = new GalleryAdapter(getApplicationContext(),mArrayUri);
                    gvGallery.setAdapter(galleryAdapter);
                    gvGallery.setVerticalSpacing(gvGallery.getHorizontalSpacing());
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvGallery
                            .getLayoutParams();
                    mlp.setMargins(0, gvGallery.getHorizontalSpacing(), 0, 0);





                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                            galleryAdapter = new GalleryAdapter(getApplicationContext(),mArrayUri);
                            gvGallery.setAdapter(galleryAdapter);
                            gvGallery.setVerticalSpacing(gvGallery.getHorizontalSpacing());
                            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvGallery
                                    .getLayoutParams();
                            mlp.setMargins(0, gvGallery.getHorizontalSpacing(), 0, 0);






                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    //Save current grid view
    private File saveBitMap(Context context, View drawView){
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"SiemensImages");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated)
                Log.i("TAG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap =getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);



            Document document = new Document(A4);

            String root = Environment.getExternalStorageDirectory().toString();

            File myDir = new File(root + "/siemensPDF");
            myDir.mkdir();

            oStream.flush();
            oStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery( context,pictureFile.getAbsolutePath());
        return pictureFile;
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }


    // used for scanning gallery
    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue scanning gallery.");
        }
    }

}

