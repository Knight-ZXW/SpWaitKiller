# SpWaitKiller [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.knight-zxw/spwaitkiller/badge.svg?style=flat)](https://github.com/Knight-ZXW/SpWaitKiller)

解决 Sharedpreferences 造成的主线程阻塞问题，避免应用因此造成ANR问题，降低ANR率。

背景及实现介绍: https://juejin.cn/post/7054766647026352158

## 使用方式

### 引入依赖

Step 1. 添加依赖
> 当前版本  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.knight-zxw/spwaitkiller/badge.svg?style=flat)](https://github.com/Knight-ZXW/SpWaitKiller)
```
	dependencies {
	        implementation 'io.github.knight-zxw:spwaitkiller:${latestVersion}'
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