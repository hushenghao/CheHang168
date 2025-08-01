# 车行168接口加解密工具

利用车行168 Android app 的 加解密库 libencrypt.so， 对接口抓包内容进行解密

车行168 app 安全隐患：

* libencrypt.so 只对 app 包名做了校验，所以只要将 applicationId 改为 com.zjw.chehang168 即可绕过检查
* 代码只进行了混淆，没有采用dex加固处理
* iOS端未对app抓包做防护，通过iOS抓包数据，在Android端进行解密即可绕过 app 防抓包
