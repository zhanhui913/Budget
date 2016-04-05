package com.zhan.budget.Activity;

import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hendrix.pdfmyxml.PdfDocument;
import com.hendrix.pdfmyxml.viewRenderer.AbstractViewRenderer;
import com.zhan.budget.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class PdfActivity extends BaseActivity {

    private AbstractViewRenderer page;
    private PdfDocument doc;
    private Toolbar toolbar;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_pdf;
    }

    @Override
    protected void init(){
        createToolbar();

        page = new AbstractViewRenderer(this, R.layout.activity_pdf) {
            private String _text;

            public void setText(String text) {
                _text = text;
            }

            @Override
            protected void initView(View view) {
                TextView tv_hello = (TextView)view.findViewById(R.id.tv_hello);
                tv_hello.setText(_text);
            }
        };

        // you can reuse the bitmap if you want
        page.setReuseBitmap(true);

        buildPDF();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("PDF");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void buildPDF(){
        /*new PdfDocument.Builder(this)
                .addPage(page)
                .filename("test")
                .orientation(PdfDocument.A4_MODE.LANDSCAPE)
                .progressMessage(R.string.app_name)
                .progressTitle(R.string.action_settings)
                .renderWidth(2115).renderHeight(1500)
                .listener(new PdfDocument.Callback() {
                    @Override
                    public void onComplete(File file) {
                        Log.d(PdfDocument.TAG_PDF_MY_XML, "Complete");
                    }

                    @Override
                    public void onError() {
                        Log.d(PdfDocument.TAG_PDF_MY_XML, "Error");
                    }
                }).create()
                .createPdf(this);

        */


        doc            = new PdfDocument(this);

        // add as many pages as you have
        doc.addPage(page);

        doc.setRenderWidth(2115);
        doc.setRenderHeight(1500);
        doc.setOrientation(PdfDocument.A4_MODE.PORTRAIT);
        doc.setProgressTitle(R.string.app_name);
        doc.setProgressMessage(R.string.action_settings);
        doc.setFileName("test_Budget_PDF");
        doc.setInflateOnMainThread(false);
        doc.setListener(new PdfDocument.Callback() {
            @Override
            public void onComplete(File file) {
                Log.i(PdfDocument.TAG_PDF_MY_XML, "Complete : ");
                savePDF();
            }

            @Override
            public void onError() {
                Log.i(PdfDocument.TAG_PDF_MY_XML, "Error");
            }
        });
        Log.d("PDF", "file at path :" + doc.getFileName());

        doc.createPdf(this);



    }

    private void savePDF(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = doc.getFile();

            if (sd.canWrite()) {
                if(createDirectory()){ Log.d("FILE", "can write file");
                    String backupDBPath = "Budget/" + doc.getFileName() ;
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(data).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }else{
                    Log.d("FILE","cannot write file");
                    Toast.makeText(getApplicationContext(), "Fail to write PDF : "+doc.getFileName(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean createDirectory(){
        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Budget");

        //If file doesnt exist
        if(!directory.exists()){
            return directory.mkdirs();
        }else{
            return true;
        }
    }
}
