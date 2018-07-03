
  Pod::Spec.new do |s|
    s.name = 'CapacitorImageCache'
    s.version = '0.0.10'
    s.summary = 'Image cache'
    s.license = 'MIT'
    s.homepage = 'https://github.com/triniwiz/capacitor-image-cache'
    s.author = 'Osei Fortune'
    s.source = { :git => '', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '10.0'
    s.dependency 'Capacitor'
    s.dependency 'SDWebImage', '~> 4.0'
  end
