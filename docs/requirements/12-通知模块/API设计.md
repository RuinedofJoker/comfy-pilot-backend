# 通知模块 - API设计

> 本文档定义通知模块的API接口

## 说明

通知模块为**内部服务模块**,不对外暴露REST API接口。
其他模块通过依赖注入方式调用邮件服务。

## 服务接口

### EmailService

**服务类**: `org.joker.comfypilot.notification.application.service.EmailService`

#### 方法列表

**1. 发送邮件**
```java
void sendEmail(String recipient, String subject, String content,
               String businessType, String businessId)
```

**2. 发送密码重置邮件**
```java
void sendPasswordResetEmail(String recipient, String resetToken)
```

**3. 查询邮件日志**
```java
List<EmailLog> findByRecipient(String recipient)
List<EmailLog> findByBusinessType(String businessType)
```

