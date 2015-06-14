//
//  ViewController.h
//  PushReceiver
//
//  Created by Christian Ulrich on 09/06/15.
//  Copyright (c) 2015 Christian Ulrich. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController

@property (weak, nonatomic) IBOutlet UILabel *lblToken;

@property (weak, nonatomic) IBOutlet UILabel *lblInfo;
@property (weak, nonatomic) IBOutlet UITextView *txtToken;
@property (weak, nonatomic) IBOutlet UITextView *txtInfo;

@end

