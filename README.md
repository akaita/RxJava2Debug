# RxJava2Debug [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Quickly%20find%20out%20the%20source%20issue%20causing%20your%20RxJava2%20stream%20fail&url=https://github.com/akaita/RxJava2Debug&via=github&hashtags=rxjava,debug,fixthatcrash)

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven](https://img.shields.io/badge/%20Maven%20-1.2.2-5bc0de.svg) ](https://mvnrepository.com/artifact/com.akaita.java/rxjava2-debug/1.2.2)
[![Jcenter](https://img.shields.io/badge/%20Jcenter%20-1.2.2-5bc0de.svg) ](https://bintray.com/akaita/java/rxjava2-debug/_latestVersion)
[![Arsenal](https://img.shields.io/badge/%20Arsenal%20-%20RxJava2Debug%20-4cae4c.svg?style=flat)](https://android-arsenal.com/details/1/6027)
[![Methods](https://img.shields.io/badge/%20Methods%20%7C%20Size%20-%20239%20%7C%2040%20KB-d9534f.svg)](http://www.methodscount.com/?lib=com.akaita.java%3Arxjava2-debug%3A1.2.2)

A library to make StackTraces involving RxJava2 more meaningful (they will always point to your code!). 

# Rationale

If you use RxJava2, you know the pain of debugging exceptions generated somwhere across the stream, in another thread, in a nested stream, ... The StackTrace provided by RxJava2 will tell you almost nothing. Even more, if you are using systems like Crashlytics to collect reports for and Android app, most RxJava-related crashes will be reported as a single error instance (omg, no way to fix that).

This library was created from the need to debug such situations when they happen in Android apps; even more, I needed to have a clear and precise report in Crashlytics for each distinct exception.

1. If you handle exceptions generated in RxJava2 streams, you can query RxJava2Debug to obtain an extended StackTrace pointing to the exact line of code that created the issue.

2. If you let exceptions generated in RxJava2 streams crash your app, RxJava2Debug will automatically extend the default StackTrace.

3. If you let exceptions generated in RxJava2 streams crash your app and you configure RxJava2Debug with your package-names, RxJava2Debug will make sure you get unique reports for each issue (to Crashlytics, or whichever reporting system you use). 


# Installation

Using *Maven Central* :
```groovy
repositories {
    mavenCentral()
}

dependencies {
    compile 'com.akaita.java:rxjava2-debug:1.2.2'
}
```

Using *JCenter*:
```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.akaita.java:rxjava2-debug:1.2.2'
}
```

# Requirements

RxJava 2.1.0+

# Usage

Just enable RxJava2Debug as soon as possible. In Android, for example:

```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        Fabric.with(this, new Crashlytics());
        
        // Enable RxJava assembly stack collection, to make RxJava crash reports clear and unique
        // Make sure this is called AFTER setting up any Crash reporting mechanism as Crashlytics
        RxJava2Debug.enableRxJava2AssemblyTracking(new String[]{"com.example.myapp", "com.example.mylibrary"});
    }
}
```

This will:

 - Enhance stack traces of all RxJava2-related crashes making sure they contain a reference to the method that generated the first event in the Rx pipeline
 - Make sure that Stack Traces contain a reference to your code (some class in `com.example.myapp` or `com.example.mylibrary`)
 - Make sure that crash reports in Crashlytics are actually different for each pipeline (avoid bundling every RXJava error into one reports)
 
You will now also be able to obtain an enhanced Stack Trace even when you implement `onError` (really, you should implement it):
 
```java
responseSubject
    .subscribe(
        responseObservable -> handleResponse(responseObservable),
        throwable -> RxJava2Debug.getEnhancedStackTrace(throwable)
    );
```

| Without RxJava2Debug | With RxJava2Debug |
| - | - |
| ![raw stack trace](https://github.com/akaita/RxJava2Debug/blob/master/screenshots/raw_stack_trace.png "Raw Stack Trace") | ![enhanced stack trace](https://github.com/akaita/RxJava2Debug/blob/master/screenshots/enhanced_stack_trace.png "Enhanced Stack Trace") |
| ![raw crash report](https://github.com/akaita/RxJava2Debug/blob/master/screenshots/raw_crash_report.png "Raw Crash Report") | ![enhanced crash report](https://github.com/akaita/RxJava2Debug/blob/master/screenshots/enhanced_crash_report.png "Enhanced Crash Report") |

# API

Start collecting information about RxJava's execution to provide a more meaningful StackTrace in case of crash  
<b>Beware:</b> Any crash-reporting handler should be set up <i>before</i> calling this method
```java
void enableRxJava2AssemblyTracking()
```

Start collecting filtered information about RxJava's execution to provide a more meaningful StackTrace in case of crash  
<b>Beware:</b> Any crash-reporting handler should be set up <i>before</i> calling this method

```java
void enableRxJava2AssemblyTracking(@Nullable String[] basePackageNames)
```

Disable the collection of more information about RxJava's execution  
Information collected before calling this method will still be reported

```java
void disableRxJava2AssemblyTracking()
```


Obtain a copy of the original Throwable with an extended StackTrace

```java
@Nullable Throwable getEnhancedStackTrace(Throwable originalException)
```

# Features

  - [StackTrace generation](#stacktrace-generation)
  - [StackTrace filtering](#stacktrace-filtering)

## StackTrace generation

Regardless of the thread a crash happens in, a nice StackTrace referring to the origin of the RxJava2 pipeline will be
 added to the causes of the crash.

Config:
 
 ```java
 RxJava2Debug.enableRxJava2AssemblyTracking();
 ```
 
Example (pointing to my own faulty code in `com.akaita.fgas.activities.TopActivity.onResume(TopActivity.java:205)`):

```java
FATAL EXCEPTION: main
Process: com.akaita.fgas.debug, PID: 22538
java.lang.Throwable: The mapper function returned a null value.
   at io.reactivex.internal.functions.ObjectHelper.requireNonNull(ObjectHelper.java:39)
   at io.reactivex.internal.operators.observable.ObservableMap$MapObserver.onNext(ObservableMap.java:59)
   at hu.akarnokd.rxjava2.debug.ObservableOnAssembly$OnAssemblyObserver.onNext(ObservableOnAssembly.java:55)
   at io.reactivex.internal.operators.observable.ObservableSubscribeOn$SubscribeOnObserver.onNext(ObservableSubscribeOn.java:58)
   at hu.akarnokd.rxjava2.debug.ObservableOnAssembly$OnAssemblyObserver.onNext(ObservableOnAssembly.java:55)
   at io.reactivex.internal.operators.observable.ObservableScalarXMap$ScalarDisposable.run(ObservableScalarXMap.java:248)
   at io.reactivex.internal.operators.observable.ObservableJust.subscribeActual(ObservableJust.java:35)
   at io.reactivex.Observable.subscribe(Observable.java:10838)
   at hu.akarnokd.rxjava2.debug.ObservableOnAssemblyScalarCallable.subscribeActual(ObservableOnAssemblyScalarCallable.java:41)
   at io.reactivex.Observable.subscribe(Observable.java:10838)
   at io.reactivex.internal.operators.observable.ObservableSubscribeOn$SubscribeTask.run(ObservableSubscribeOn.java:96)
   at io.reactivex.Scheduler$DisposeTask.run(Scheduler.java:452)
   at io.reactivex.internal.schedulers.ScheduledRunnable.run(ScheduledRunnable.java:61)
   at io.reactivex.internal.schedulers.ScheduledRunnable.call(ScheduledRunnable.java:52)
   at java.util.concurrent.FutureTask.run(FutureTask.java:237)
   at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:272)
   at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
   at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
   at java.lang.Thread.run(Thread.java:761)
Caused by: java.lang.Throwable
   at io.reactivex.internal.functions.ObjectHelper.requireNonNull(ObjectHelper.java:39)
   at com.akaita.fgas.activities.TopActivity.onResume(TopActivity.java:205)
   at android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1269)
   at android.app.Activity.performResume(Activity.java:6766)
   at android.app.ActivityThread.performResumeActivity(ActivityThread.java:3377)
   at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3440)
   at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2713)
   at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1460)
   at android.os.Handler.dispatchMessage(Handler.java:102)
   at android.os.Looper.loop(Looper.java:154)
   at android.app.ActivityThread.main(ActivityThread.java:6077)
   at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:866)
   at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:756)
```

Instead of the obscure:

```java
FATAL EXCEPTION: main
Process: com.akaita.fgas.debug, PID: 27300
io.reactivex.exceptions.OnErrorNotImplementedException: The mapper function returned a null value.
   at io.reactivex.internal.functions.Functions$OnErrorMissingConsumer.accept(Functions.java:704)
   at io.reactivex.internal.functions.Functions$OnErrorMissingConsumer.accept(Functions.java:701)
   at io.reactivex.internal.observers.LambdaObserver.onError(LambdaObserver.java:74)
   at io.reactivex.internal.operators.observable.ObservableObserveOn$ObserveOnObserver.checkTerminated(ObservableObserveOn.java:276)
   at io.reactivex.internal.operators.observable.ObservableObserveOn$ObserveOnObserver.drainNormal(ObservableObserveOn.java:172)
   at io.reactivex.internal.operators.observable.ObservableObserveOn$ObserveOnObserver.run(ObservableObserveOn.java:252)
   at io.reactivex.android.schedulers.HandlerScheduler$ScheduledRunnable.run(HandlerScheduler.java:109)
   at android.os.Handler.handleCallback(Handler.java:751)
   at android.os.Handler.dispatchMessage(Handler.java:95)
   at android.os.Looper.loop(Looper.java:154)
   at android.app.ActivityThread.main(ActivityThread.java:6077)
   at java.lang.reflect.Method.invoke(Native Method)
   at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:866)
   at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:756)
Caused by: java.lang.NullPointerException: The mapper function returned a null value.
   at io.reactivex.internal.functions.ObjectHelper.requireNonNull(ObjectHelper.java:39)
   at io.reactivex.internal.operators.observable.ObservableMap$MapObserver.onNext(ObservableMap.java:59)
   at io.reactivex.internal.operators.observable.ObservableSubscribeOn$SubscribeOnObserver.onNext(ObservableSubscribeOn.java:58)
   at io.reactivex.internal.operators.observable.ObservableScalarXMap$ScalarDisposable.run(ObservableScalarXMap.java:248)
   at io.reactivex.internal.operators.observable.ObservableJust.subscribeActual(ObservableJust.java:35)
   at io.reactivex.Observable.subscribe(Observable.java:10838)
   at io.reactivex.internal.operators.observable.ObservableSubscribeOn$SubscribeTask.run(ObservableSubscribeOn.java:96)
   at io.reactivex.Scheduler$DisposeTask.run(Scheduler.java:452)
   at io.reactivex.internal.schedulers.ScheduledRunnable.run(ScheduledRunnable.java:61)
   at io.reactivex.internal.schedulers.ScheduledRunnable.call(ScheduledRunnable.java:52)
   at java.util.concurrent.FutureTask.run(FutureTask.java:237)
   at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:272)
   at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
   at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
   at java.lang.Thread.run(Thread.java:761)
```


## StackTrace filtering

Tell RxJava2Debug which packages names you want to highlight, and RxJava2Debug will make sure they appear as
 the top of the StackTrace

Config:

```java
RxJava2Debug.enableRxJava2AssemblyTracking(new String[]{"com.akaita.fgas", "com.akaita.android"});
```

Result: 

```java
FATAL EXCEPTION: main
Process: com.akaita.fgas.debug, PID: 22538
java.lang.Throwable: The mapper function returned a null value.
   at io.reactivex.internal.functions.ObjectHelper.requireNonNull(ObjectHelper.java:39)
   at io.reactivex.internal.operators.observable.ObservableMap$MapObserver.onNext(ObservableMap.java:59)
   [...]
Caused by: java.lang.Throwable
   at com.akaita.fgas.activities.TopActivity.onResume(TopActivity.java:205)
   at android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1269)
   at android.app.Activity.performResume(Activity.java:6766)
   at android.app.ActivityThread.performResumeActivity(ActivityThread.java:3377)
   at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3440)
   at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2713)
   at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1460)
   at android.os.Handler.dispatchMessage(Handler.java:102)
   at android.os.Looper.loop(Looper.java:154)
   at android.app.ActivityThread.main(ActivityThread.java:6077)
   at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:866)
   at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:756)
```

This comes in very handy when you work with a error reporting system such as [Crashlytics](http://try.crashlytics.com/).

Instead of getting a multitude of reports involving RxJava2 into a single report entry, you'll get one entry per crash.

  
# Credits

This library is using a subset of classes from [akarnokd's RxJava2Extensions](https://github.com/akarnokd/RxJava2Extensions). 

RxJava2Extensions takes about `kb` and contains `` methods. RxJava2Debug takes about `38kb` and contains `` methods.
