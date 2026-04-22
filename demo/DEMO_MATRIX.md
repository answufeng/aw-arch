# aw-arch Demo 功能矩阵

| 按钮 | 说明 |
|------|------|
| LoadState / RetryLoadState | `LoadState` 密封类与重试 |
| Post / TryPost / Sticky / RemoveSticky | `FlowEventBus` |
| MVVM / MVI / SimpleMVI / Hilt | 各基类演示 Activity |
| Nav | `AwNav` 路由 |
| WeChat | 复合示例 |

主界面日志区可观察事件流；顶部 **「演示清单」** 可打开摘要对话框。Hilt 示例需正确 `Application` 注解与 `ksp`。

## 推荐手测（边界与极端场景）

| 场景 | 建议操作 |
|------|----------|
| 生命周期 | 旋转屏幕、进程恢复后重复 `AwNav.init` 与 Fragment 回退栈 |
| FlowEventBus | 仅 post 无订阅、Sticky 清理顺序、页面销毁后是否仍收事件 |
| ViewModel | 快速重复触发 `launch`，确认异常走 `handleException` 且不重复弹 Toast |
| 低内存 | 多 Activity 串联 Hilt / MVI demo 后压后台 |
