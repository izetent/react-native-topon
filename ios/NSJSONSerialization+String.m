//
//  NSJSONSerialization+String.m
//  react-native-topon
//

#import "NSJSONSerialization+String.h"

@implementation NSJSONSerialization (ToponString)

+ (id)topon_JSONObjectWithString:(NSString *)string
                         options:(NSJSONReadingOptions)opt
                           error:(NSError **)error {
  if (![string isKindOfClass:[NSString class]] || string.length == 0) {
    return nil;
  }
  NSData *data = [string dataUsingEncoding:NSUTF8StringEncoding];
  if (data == nil) {
    return nil;
  }
  return [NSJSONSerialization JSONObjectWithData:data options:opt error:error];
}

@end
