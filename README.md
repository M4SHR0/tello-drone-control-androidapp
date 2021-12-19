# tello-drone-control-androidapp
Tello ドローンをスマホの傾きで操作するためのAndroidアプリ
# 動作環境
Android 11(API 30)
# 操作方法
1. 画面右下のFABで離陸
2. 端末を画面を天面に水平に持つ
3. 端末を傾けることで前後移動と左右への旋回を行う
4. 離陸後に表示されたFABで上昇と下降
5. 再度右下のFABを押すことで着陸
6. 離陸中に右下のFABを長押しすると緊急停止

![Videotogif](https://user-images.githubusercontent.com/76655329/146679025-cf5ee8b2-41df-433d-999a-f2ac49fe93a1.gif)
# 使用技術
- UDP通信
- KotlinCorutines
- 方位センサ
- ViewBinding
- アニメーション
