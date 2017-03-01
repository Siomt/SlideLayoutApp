# SlideLayoutApp


##需求：
将复杂的内容布局 通过向右拖拽或者是快速向右滑动将其移动到最右边
当在向左拖动或者是快速向左滑动会将移除的布局恢复到原位

##使用方法

```
compile 'com.slidelayout:slipe_layout_library:0.0.3'
```

```
//滑动完成监听
        slide.setOnSlideStatusListener(new SlideLayout.OnSlideStatusListener() {
            @Override
            public void slideOutComplete() {
                Log.d("SHF", "slideOutComplete");
            }

            @Override
            public void slideInComplete() {
                Log.d("SHF", "slideInComplete");
            }
        });
```

> 注意：
> 1、SlideLayout使用相当于RelativeLayout
> 2、 第一个子布局就是可拖动的布局 
> 3、至少一个子布局


![这里写图片描述](http://img.blog.csdn.net/20160825203509659)
