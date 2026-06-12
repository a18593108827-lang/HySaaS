# 房单核销

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| ent@test.com | 企业 | `/enterprise/bookings` |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/enterprise.ts`、`frontend/src/types/index.ts`  
> 页面源码：`frontend/src/views/enterprise/BookingsView.vue`  
> 参会预订：`frontend/src/views/portal/HotelsView.vue`  
> 酒店协议：[hotel.md](./hotel.md)

## 侧栏与路由

| 入口 | 路由 | 组件 |
|------|------|------|
| 房单核销 | `/enterprise/bookings` | BookingsView |

**范围**：仅本租户房单，后端按 `tenant_id` 隔离。

## 业务流程

```
参会人预订房型 → 支付成功 → 房单 LOCKED → 现场入住 → 企业核销 → CHECKED_IN
```

| 状态 | 说明 |
|------|------|
| PENDING_PAY | 待支付 |
| LOCKED | 已支付锁定，可核销 |
| CHECKED_IN | 已核销入住 |
| CANCELLED | 已取消 |

## 页面行为

### 房单列表 `/enterprise/bookings`

| 操作 | 条件 | API |
|------|------|-----|
| 核销 | 仅 `LOCKED` | `POST /enterprise/bookings/{id}/checkin` |
| 状态筛选 | — | `GET /enterprise/bookings?status=` |
| 刷新 | — | `GET /enterprise/bookings` |

操作列：仅「核销」按钮（`LOCKED` 时显示）

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/bookings` | Query: `status?`, `eventId?`, `page?`, `size?` |
| POST | `/enterprise/bookings/{id}/checkin` | 核销入住 |

### HotelBooking

```ts
{
  id: number
  bookingNo: string
  eventId: number
  eventTitle: string
  guestName: string
  hotelName: string
  roomTypeName: string
  nights: number
  amount: number
  status: 'PENDING_PAY' | 'LOCKED' | 'CHECKED_IN' | 'CANCELLED'
  createdAt: string
  checkedInAt?: string
}
```

---

## Demo 数据

| bookingNo | 入住人 | 状态 |
|-----------|--------|------|
| B20260601001 | 王明 | LOCKED（可核销） |
| B20260602002 | 陈丽 | CHECKED_IN |
| B20260603003 | 赵强 | PENDING_PAY |

---

## 后端实现

| 后端 | 说明 |
|------|------|
| EnterpriseBookingController | 列表 + 核销 |
| HotelService | 支付成功 LOCKED、关单 CANCELLED、核销 CHECKED_IN |
| `hotel_booking` | 关联 event_id、room_type_id、user_id；V5 增 booking_no/nights/checked_in_at |
