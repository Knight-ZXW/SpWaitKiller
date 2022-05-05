# SpWaitKiller [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.knight-zxw/spwaitkiller/badge.svg?style=flat)](https://github.com/Knight-ZXW/SpWaitKiller)

解决 Sharedpreferences 造成的主线程阻塞问题，避免应用因此造成ANR问题，降低ANR率。

blog介绍:https://www.yuque.com/zhuoxiuwu/brap8y/eo1sgu

## 使用方式

### 引入依赖

Step 1. 添加依赖
> 当前版本  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.knight-zxw/spwaitkiller/badge.svg?style=flat)](https://github.com/Knight-ZXW/SpWaitKiller)
```
	dependencies {
	        implementation 'com.github.Knight-ZXW:SpWaitKiller:${latestVersion}'
	}
```
Step 2. 代码开启
```
 SpWaitKiller.builder(getApplication())
          .build()
          .work();
```

## 兼容性
Android 5.0 ~ Android 12