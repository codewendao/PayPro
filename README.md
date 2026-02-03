# 个人支付系统

这是一个专为个人和小微商户设计的支付系统，无需签约第三方，支持全自动/半自动收款。

## 功能特性

- 🚀 **无需签约**：零门槛接入，无需企业资质和第三方签约流程
- 🤖 **全自动收款**：智能识别到账，实时更新订单状态
- 🔧 **半自动模式**：支持手动确认，灵活切换
- 💰 **充值功能**：支持预设金额和自定义金额充值
- 💳 **多种支付方式**：支付宝、微信支付、支付宝当面付
- 📱 **支付页面**：独立的支付页面，支持二维码展示和实时状态监控
- ❓ **帮助中心**：支付相关帮助信息和FAQ
- 📱 **响应式设计**：适配各种屏幕尺寸
- 🎨 **美观界面**：现代化UI设计

## 支付方式说明

1. **支付宝**：拼接二维码url
2. **微信支付**：拼接二维码url

## 文件结构

```
templates/
├── error.html          # 错误页面
├── help.html           # 帮助中心页面
├── help-zfb.html       # 支付宝开通帮助页面
├── history.html        # 支付记录页面
├── home.html           # 首页（系统介绍和功能展示）
├── index.html          # 入口页面（自动跳转到home.html）
├── openAlipay.html     # 支付宝内打开页面
├── order-del.html      # 审核订单跳转至删除页面
├── order-edit.html     # 审核订单跳转至编辑页面
├── order-fail.html     # 审核订单跳转至失败页面
├── order-success.html  # 订单成功支付发送邮件页面模板
├── payment.html        # 支付页面（显示二维码，处理支付流程）
├── payment-review.html # 支付信息预览邮件页面
├── recharge.html       # 充值页面（选择金额和支付方式）
├── statistic.html      # 数据统计页面
├── success.html        # 成功跳转页面
```

## 自定义修改

### 部署修改点
application.yml

数据库连接（必须）

redis连接（必须）

邮箱密码，收件邮箱地址和发件邮箱地址（必须）

token（建议）

## 注意事项

1. 本项目使用CDN引入Vue和相关库，需要网络连接
2. 如需离线使用，请下载相关库文件到本地
3. 生产环境建议使用HTTPS协议
4. 支付功能需要后端API支持

效果展示：

**首页展示**
<img src=".\images\home.png"/>
**支付选择**
<img src=".\images\recharge.png"/>

**支付提示**
![支付选择](.\images\payment-help.png)

**支付页面**
![支付选择](.\images\payment.png)

**支付审核邮件通知内容**
![支付选择](.\images\payment-review.png)
