package com.example.retina_test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.google.common.io.ByteStreams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private ImageView iv_image;  
	private ImageView show_image;  
	Uri myUri;
	Bitmap myBitmap;
    private byte[] mContent;
    
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String upLoadServerUri = null;
    
    TextView messageText;
	
    /**********  File Path *************/
    final String uploadFilePath = "/sdcard";
    final String uploadFileName = "/image017.png";
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title);
		setContentView(R.layout.activity_main);
						
		iv_image = (ImageView) this.findViewById(R.id.iv_image);  
		Button Button1 = (Button)findViewById(R.id.button1);
		Button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setTitle("Choose Pic");		
				builder.setPositiveButton("Camera",
                        new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface
                                		dialog, int which) {
                                        Intent intent = new Intent(
                                                        "android.media.action.IMAGE_CAPTURE");
                                        startActivityForResult(intent, 0);
                                }
                          });
				builder.setNegativeButton("Photo Album",
                        new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface

                                dialog, int which) {
                                        Intent intent = new Intent(
                                                        Intent.ACTION_PICK,
                                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        startActivityForResult(intent, 1);

                                }
                        });
				
				AlertDialog alert = builder.create();
                alert.show();				
			}
		});
		
		messageText  = (TextView)findViewById(R.id.messageText);
        
        messageText.setText("Uploading file path :- '/mnt/sdcard/"+uploadFileName+"'");
         
        /************* Php script path ****************/
        upLoadServerUri = "http://192.168.1.5:3000/attachments";
		
		Button Button2 = (Button)findViewById(R.id.button2);
		Button2.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file...", true);
                
                new Thread(new Runnable() {
                        public void run() {
                             runOnUiThread(new Runnable() {
                                    public void run() {
                                        messageText.setText("uploading started.....");
                                    }
                                });                      
                           
                             uploadFile(uploadFilePath + uploadFileName);
                                                      
                        }
                }).start();    
			}
		});
	}

	public int uploadFile(String sourceFileUri) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;  
        String boundary = "WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
        File sd = Environment.getExternalStorageDirectory();
        File[] sdDirList = sd.listFiles();
        File sourceFile = new File(sourceFileUri); 
        if (!sourceFile.isFile()) {
             dialog.dismiss(); 
             Log.e("uploadFile", "Source File not exist :"
                                 +uploadFilePath + "" + uploadFileName);
             runOnUiThread(new Runnable() {
                 public void run() {
                     messageText.setText("Source File not exist :"
                             +uploadFilePath + "" + uploadFileName);
                 }
             }); 
             return 0;
        }
        else
        {
             try { 
            	 HttpClient client = new HttpClient("http://192.168.1.4:3000/attachments");
 				 client.connectForMultipart();
 				 client.addFormPart("file_name", uploadFileName);
 				 client.addFilePart("file", uploadFileName, ByteStreams.toByteArray(new FileInputStream(sourceFileUri)));
 				 client.finishMultipart();
				 String data = client.getResponse();
				 JSONObject jsonObj = new JSONObject(data);
				 final String address = jsonObj.getString("uri");
				
				runOnUiThread(new Runnable() {  
                    @Override
                    public void run() {
                    
                    	iv_image.setImageBitmap(returnBitMap(address));
                    }

					private Bitmap returnBitMap(String address) {
						URL myFileUrl = null; 
						Bitmap bitmap = null; 
						
						try { 
							myFileUrl = new URL(address); 
						} catch (MalformedURLException e) { 
							e.printStackTrace(); 
						} 
						try { 
						
							HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection(); 
							conn.setDoInput(true); 
							conn.connect(); 
						    InputStream is = conn.getInputStream(); 
						    bitmap = BitmapFactory.decodeStream(is); 
						    is.close(); 
						} catch (IOException e) { 
							e.printStackTrace(); 
						} 
						return bitmap; 
					}
				});
				
	
            } catch (MalformedURLException ex) {
                 
                dialog.dismiss();  
                ex.printStackTrace();
                 
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(MainActivity.this, "MalformedURLException", 
                                                            Toast.LENGTH_SHORT).show();
                    }
                });
                 
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
            } catch (Exception e) {
                 
                dialog.dismiss();  
                e.printStackTrace();
                 
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(MainActivity.this, "Got Exception : see logcat ", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : "
                                                 + e.getMessage(), e);  
            }
            dialog.dismiss();       
            return serverResponseCode; 
             
         } // End else block 
       } 
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ContentResolver resolver = getContentResolver();
     
        if (data != null) {
                
                if (requestCode == 1) {

                        try {
                                // Get the pic's uri
                                Uri originalUri = data.getData();
                                // Decode the pic's data
                                mContent = readStream(resolver.openInputStream(Uri.parse(originalUri.toString())));
                                // Transform ImageView to Bitmap
                                myBitmap = getPicFromBytes(mContent, null);
                                // //Display the image
                                iv_image.setImageBitmap(myBitmap);
                        } catch (Exception e) {
                                System.out.println(e.getMessage());
                        }

                } else if (requestCode == 0) {

                        String sdStatus = Environment.getExternalStorageState();
                        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // Check SD card
                                return;
                        }
                        Bundle bundle = data.getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        FileOutputStream b = null;
                        File file = new File("/sdcard/myImage/");
                        file.mkdirs();

                        
                        String str = null;
                        Date date = null;
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                        date = new Date();
                        str = format.format(date);
                        String fileName = "/sdcard/myImage/" + str + ".jpg";
                        try {
                                b = new FileOutputStream(fileName);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
                        } catch (FileNotFoundException e) {
                                e.printStackTrace();
                        } finally {
                                try {
                                        b.flush();
                                        b.close();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }
                                if (data != null) {
                                        Bitmap cameraBitmap = (Bitmap) data.getExtras().get(
                                                        "data");
                                        System.out.println("fdf================="
                                                        + data.getDataString());
                                        iv_image.setImageBitmap(cameraBitmap);

                                        System.out.println("成功======" + cameraBitmap.getWidth()
                                                        + cameraBitmap.getHeight());
                                }

                        }
                }
        }
}

	public static Bitmap getPicFromBytes(byte[] bytes,
                BitmapFactory.Options opts) {
        if (bytes != null)
                if (opts != null)
                        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                                        opts);
                else
                        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
	}

	public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
		}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
			case R.id.Quit:
				finish();
			default:
			return super.onOptionsItemSelected(item);			
		}
	}

}

