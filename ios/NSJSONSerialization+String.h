//
//  NSJSONSerialization+String.h
//  react-native-topon
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSJSONSerialization (ToponString)

+ (id)topon_JSONObjectWithString:(NSString *)string
                         options:(NSJSONReadingOptions)opt
                           error:(NSError **)error;

@end

NS_ASSUME_NONNULL_END
