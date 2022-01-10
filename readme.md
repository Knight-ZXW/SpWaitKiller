# SpWaitKiller
解决 Sharedpreferences 造成的主线程阻塞问题，避免应用因此造成ANR问题，降低ANR率

## 使用方式

### 引入依赖

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency
```
	dependencies {
	        implementation 'com.github.Knight-ZXW:SpWaitKiller:0.0.2'
	}
``` 
### 代码开启
```
 SpWaitKiller.builder(getApplication())
          .build()
          .work();
```
