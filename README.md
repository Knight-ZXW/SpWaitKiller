# SpWaitKiller
解决 Sharedpreferences 造成的主线程阻塞问题，避免应用因此造成ANR问题，降低ANR率

## 使用方式
```
 SpWaitKiller.builder(getApplication())
          .build()
          .work();
```
