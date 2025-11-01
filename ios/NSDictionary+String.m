//
//  NSDictionary+String.m
//  react-native-topon
//

#import "NSDictionary+String.h"

NSString *const kToponCallbackPlacementIdKey = @"placementId";
NSString *const kToponCallbackExtraKey = @"adCallbackInfo";
NSString *const kToponCallbackErrorKey = @"errorMsg";

@implementation NSDictionary (ToponString)

- (NSString *)topon_jsonString {
  if (![NSJSONSerialization isValidJSONObject:self]) {
    return @"";
  }
  NSData *data = [NSJSONSerialization dataWithJSONObject:self options:0 error:nil];
  return data.length > 0 ? [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding] : @"";
}

- (NSString *)topon_adInfoJSONString {
  id adInfo = self[@"adInfo"];
  if (adInfo == nil) {
    return @"";
  }
  if ([adInfo isKindOfClass:[NSString class]]) {
    return adInfo;
  }
  if ([NSJSONSerialization isValidJSONObject:adInfo]) {
    NSData *data = [NSJSONSerialization dataWithJSONObject:adInfo options:0 error:nil];
    return data.length > 0 ? [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding] : @"";
  }
  return @"";
}

@end
