package com.github.triniwiz.capacitorimagecache;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.Manifest;
import android.util.Log;
import android.content.pm.PackageManager;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.getcapacitor.FileUtils;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@NativePlugin(
    permissions={
        Manifest.permission.ACCESS_NETWORK_STATE
    }
)
public class ImageCachePlugin extends Plugin {
    private final int SAVE_IMAGE_REQUEST = 2102;

    @Override
    public void load() {
        super.load();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(getContext())
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(getContext(), config);
    }

    private static boolean isImageDownloaded(Uri loadUri) {
        if (loadUri == null) {
            return false;
        }
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(loadUri), null);
        return ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey) || ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey);
    }


    private static File getCachedImageOnDisk(Uri loadUri) {
        File localFile = null;
        if (loadUri != null) {
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(loadUri), null);
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }


    @PluginMethod()
    public void get(final PluginCall call) {
        final String src = call.getString("src", "");
        final JSObject obj = new JSObject();
        if (src.contains("http:") || src.contains("https:")) {

            final Uri url = Uri.parse(src);
            if (isImageDownloaded(url)) {
                obj.put("value", FileUtils.getPortablePath(getContext(), bridge.getLocalUrl(), Uri.fromFile(getCachedImageOnDisk(url))));
                call.resolve(obj);
            } else {

                ImageRequest request = ImageRequestBuilder.fromRequest(ImageRequest.fromUri(src))
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                        .build();

                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(request, null);
                dataSource.subscribe(new DataSubscriber<CloseableReference<CloseableImage>>() {
                    @Override
                    public void onNewResult(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        if (!dataSource.isFinished()) {
                            return;
                        }
                        if (dataSource.isFinished()) {
                            if (isImageDownloaded(url)) {
                                obj.put("value", FileUtils.getPortablePath(getContext(), bridge.getLocalUrl(), Uri.fromFile(getCachedImageOnDisk(url))));
                                call.resolve(obj);
                            }
                            dataSource.close();
                        }
                    }

                    @Override
                    public void onFailure(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        Throwable ex = dataSource.getFailureCause();
                        call.reject(ex.getLocalizedMessage());
                        dataSource.close();
                    }

                    @Override
                    public void onCancellation(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        if (!isImageDownloaded(url)) {
                            call.reject("cancelled");
                        }
                        dataSource.close();
                    }

                    @Override
                    public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {

                    }
                }, UiThreadImmediateExecutorService.getInstance());

            }

        } else {
            JSObject _obj = new JSObject();
            _obj.put("value", src);
            call.resolve(_obj);
        }
    }

    @PluginMethod()
    public void hasItem(PluginCall call) {
        String src = call.getString("src", "");
        Uri url = Uri.parse(src);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        boolean has = imagePipeline.isInDiskCacheSync(url);
        JSObject obj = new JSObject();
        obj.put("value", has);
        call.resolve(obj);
    }

    @PluginMethod()
    public void clearItem(PluginCall call) {
        String src = call.getString("src", "");
        Uri url = Uri.parse(src);
        JSObject obj = new JSObject();
        try{
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            imagePipeline.evictFromCache(url);
            obj.put("value",true);
            call.resolve();
        }catch (Exception e){
            obj.put("value",false);
            call.resolve(obj);
        }
    }


    @PluginMethod()
    public void clear(PluginCall call) {
        JSObject obj = new JSObject();
        try{
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            imagePipeline.clearCaches();
            obj.put("value",true);
            call.resolve();
        }catch (Exception e){
            obj.put("value",false);
            call.resolve(obj);
        }
    }


    @PluginMethod()
    public void saveImage(final PluginCall call) {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d("DEBUG LOG", "HAS PERMISSIONS");
            _saveImage(call);
        } else {
            Log.d("DEBUG LOG", "NOT ALLOWED");
            saveCall(call);
            pluginRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, SAVE_IMAGE_REQUEST);
        }
    }

    private void _saveImage(final PluginCall call) {
        final String src = call.getString("src", "");

        if (src.contains("http:") || src.contains("https:")) {
            final Uri url = Uri.parse(src);

            if (isImageDownloaded(url)) {
                final File imageFile = getCachedImageOnDisk(url);

                ImageRequest request = ImageRequestBuilder.fromRequest(ImageRequest.fromFile(imageFile))
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                        .build();

                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                DataSource<CloseableReference<CloseableImage>> dataSource =
                        imagePipeline.fetchDecodedImage(request, null);

                dataSource.subscribe(new DataSubscriber<CloseableReference<CloseableImage>>() {
                    @Override
                    public void onNewResult(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        if (!dataSource.isFinished()) {
                            return;
                        } else {
                            try {
                                CloseableReference<CloseableImage> imageReference = dataSource.getResult();
                                if (imageReference != null) {
                                    try {
                                        CloseableImage image = imageReference.get();
                                        if (image instanceof CloseableBitmap) {
                                            // do something with the bitmap
                                            Bitmap bitmap = ((CloseableBitmap) image).getUnderlyingBitmap();

                                            try {
                                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
                                                File photosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                                File newFile = new File(photosDir, "IMG_" + timeStamp + ".jpg");

                                                // Copy the bitmap to a new file
                                                FileOutputStream out = new FileOutputStream(newFile);
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                                out.flush();
                                                out.close();

                                                // Copy the new file to the gallery
                                                ContentValues values = new ContentValues();
                                                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                                                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                                values.put(MediaStore.MediaColumns.DATA, newFile.getPath());

                                                getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                                                call.resolve();
                                            } catch (Exception e) {
                                                call.reject(e.getLocalizedMessage());
                                            }
                                        } else {
                                            call.reject("Unkown image format");
                                        }
                                    } finally {
                                        CloseableReference.closeSafely(imageReference);
                                    }
                                } else {
                                    // cache miss
                                    call.reject("Cache miss");
                                }
                            } finally {
                                dataSource.close();
                            }
                        }
                    }

                    @Override
                    public void onFailure(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        Throwable ex = dataSource.getFailureCause();
                        call.reject(ex.getLocalizedMessage());
                        dataSource.close();
                    }

                    @Override
                    public void onCancellation(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        call.reject("cancelled");
                        dataSource.close();
                    }

                    @Override
                    public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {

                    }
                }, UiThreadImmediateExecutorService.getInstance());
            } else {
                call.reject("Image must exist in cache before saving to gallery");
            }
        } else {
            call.reject("src must use an http or https scheme");
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied permission");
                return;
            }
        }

        if (requestCode == SAVE_IMAGE_REQUEST) {
            // We got the permission
            _saveImage(savedCall);
        }
    }

}
