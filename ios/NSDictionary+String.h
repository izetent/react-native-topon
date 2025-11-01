//
//  NSDictionary+String.h
//  react-native-topon
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

extern NSString *const kToponCallbackPlacementIdKey;
extern NSString *const kToponCallbackExtraKey;
extern NSString *const kToponCallbackErrorKey;

@interface NSDictionary (ToponString)

- (NSString *)topon_jsonString;

- (NSString *)topon_adInfoJSONString;

@end

NS_ASSUME_NONNULL_END
