import Foundation
import Capacitor
import SDWebImage
import Photos
let KEY = "_CAP_IMAGE_CACHE_"
typealias JSObject = [String:Any]
@objc(ImageCachePlugin)
public class ImageCachePlugin: CAPPlugin {
    private var cache: NSDictionary?
    private var manager: SDWebImageManager?
    public override func load() {
        manager = SDWebImageManager.shared()
        manager?.imageDownloader?.shouldDecompressImages = false
    }

    @objc func get(_ call: CAPPluginCall) {
        let src = call.getString("src") ?? ""
        if(src.contains("http:") || src.contains("https:")){
            let url = URL.init(string: src )

            self.manager?.loadImage(with: url, options: SDWebImageOptions.scaleDownLargeImages, progress: { (receivedSize, expectedSize, path) in


            }, completed: { (image, data, error, type, finished, completedUrl) in

                if(image == nil && error != nil && data == nil){
                    call.reject(error!.localizedDescription)
                }else if(finished && completedUrl != nil ){
                    if(type == SDImageCacheType.disk){
                        DispatchQueue.main.async {
                        let key = self.manager?.cacheKey(for: completedUrl)
                        let source = self.manager?.imageCache?.defaultCachePath(forKey: key)
                        var obj = JSObject()
                        obj["value"] = CAPFileManager.getPortablePath(host: self.bridge!.getLocalUrl(), uri: URL.init(string: source!));
                        call.resolve(obj)
                        }
                    }else{
                        SDImageCache.shared().store(image, forKey: completedUrl?.absoluteString, completion: {
                            DispatchQueue.main.async {
                                let key = self.manager?.cacheKey(for: completedUrl)
                                let source = self.manager?.imageCache?.defaultCachePath(forKey: key)
                                var obj = JSObject()
                                obj["value"] = CAPFileManager.getPortablePath(host: self.bridge!.getLocalUrl(), uri: URL.init(string: source!));
                                call.resolve(obj)
                            }
                        })
                    }
                }

            })
        }else{
            var obj = JSObject()
            obj["value"] = src;
            call.resolve(obj)
        }

    }

    @objc func hasItem(_ call: CAPPluginCall) {
        let src = call.getString("src") ?? ""
        let url = URL.init(string: src)
        manager?.cachedImageExists(for: url , completion: { (exists) in
            DispatchQueue.main.async {
                var obj = JSObject()
                obj["value"] = exists
                call.resolve(obj)
            }
        })
    }

    @objc func clearItem(_ call: CAPPluginCall) {
        let src = call.getString("src") ?? ""
        manager?.imageCache?.removeImage(forKey: src, fromDisk: true, withCompletion: {
            DispatchQueue.main.async {
                call.resolve()
            }
        })

    }


    @objc func clear(_ call: CAPPluginCall) {
        manager?.imageCache?.clearMemory()
    }
    
    @objc func saveImage(_ call: CAPPluginCall) {
        let src = call.getString("src") ?? ""
        if(src.contains("http:") || src.contains("https:")) {
            let image = SDImageCache.shared().imageFromDiskCache(forKey: src)
            if (image != nil) {
                checkAuthorization(allowed: {
                    // Add it to the photo library.
                    PHPhotoLibrary.shared().performChanges({
                        PHAssetChangeRequest.creationRequestForAsset(from: image!)
                    }, completionHandler: {success, error in
                        if !success {
                            call.reject("Unable to save image to camera poll", error)
                        } else {
                            call.resolve()
                        }
                    })
                }, notAllowed: {
                    call.reject("Access to photos not allowed by user")
                })
            } else {
                call.reject("Image is not in cache")
            }
        } else {
            call.reject("Must provide src")
        }
    }
    
    func checkAuthorization(allowed: @escaping () -> Void, notAllowed: @escaping () -> Void) {
        let status = PHPhotoLibrary.authorizationStatus()
        if status == PHAuthorizationStatus.authorized {
            allowed()
        } else {
            PHPhotoLibrary.requestAuthorization({ (newStatus) in
                if newStatus == PHAuthorizationStatus.authorized {
                    allowed()
                } else {
                    notAllowed()
                }
            })
        }
    }
}
