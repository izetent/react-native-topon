require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "Topon"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/Mahuoooo/react-native-topon.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,cpp}"
  s.private_header_files = "ios/**/*.h"

  s.dependency "TPNiOS", "6.4.93"
  s.dependency "TPNUnityAdsSDKAdapter", "6.4.93"
  s.dependency "TPNFacebookSDKAdapter", "6.4.93.1"
  s.dependency "TPNAdmobSDKAdapter", "6.4.93.1"
  s.dependency "TPNApplovinSDKAdapter", "6.4.93.1"
  s.dependency "TPNMintegralSDKAdapter", "6.4.93"
  s.dependency "TPNYandexSDKAdapter", "6.4.93"


  install_modules_dependencies(s)
end
