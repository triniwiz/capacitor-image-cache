package com.github.triniwiz.capacitorimagecache;

import android.net.Uri;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
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

@NativePlugin()
public class ImageCachePlugin extends Plugin {


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
                obj.put("value", FileUtils.getPortablePath(getContext(), Uri.fromFile(getCachedImageOnDisk(url))));
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
                                obj.put("value", FileUtils.getPortablePath(getContext(), Uri.fromFile(getCachedImageOnDisk(url))));
                                call.resolve(obj);
                            }
                            dataSource.close();
                        }
                    }

                    @Override
                    public void onFailure(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        call.reject("failed");
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


}
