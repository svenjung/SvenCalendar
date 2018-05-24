## SvenCalendar
本项目是使用Behavior实现仿照Flyme日历滑动效果，其主要behavior参考BottomSheetBehavior, 已实现以下功能：

> * 主页嵌套滑动效果，事件列表展开和收起时切换月视图和周视图
> * 月周视图中事件标记，使用LiveData + RxJava实时更新
> * 事件列表显示（暂未添加监听数据库变化功能）

------

### TODO List

- [ ] 切换周首日功能
- [ ] 实时加载头图，切换状态
- [ ] 月视图选择日期超过周视图的缓存页面时，切换到周视图会闪
- [ ] 月视图长按弹出新建事件对话框后，touch事件没有终止（还能滑动）
- [x] 修复回到今天后滑动效果异常问题
- [x] 渲染事件标识
- [x] 加载事件列表

![](calendar_behavior_demo.gif)