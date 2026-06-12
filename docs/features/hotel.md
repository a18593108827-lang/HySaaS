# 酒店协议

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| ent@test.com | 企业 | `/enterprise/hotels` |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/enterprise.ts`、`frontend/src/types/index.ts`  
> 页面源码：`frontend/src/views/enterprise/HotelsView.vue`  
> 参会预订：`frontend/src/views/portal/HotelsView.vue`  
> 房单核销：[booking.md](./booking.md)  
> 支付：[payment.md](./payment.md)

## 侧栏与路由

| 入口 | 路由 | 组件 |
|------|------|------|
| 酒店协议 | `/enterprise/hotels` | HotelsView |

**范围**：仅管理当前登录租户下的协议酒店与房型配额，后端按 `tenant_id` 隔离。

活动需开启 `hotelEnabled=true` 后，参会人才能在 `/portal/hotels/:eventId` 预订。

## 页面行为

### 酒店列表 `/enterprise/hotels`

| 操作 | API |
|------|-----|
| 添加酒店 | 弹窗 `POST /enterprise/hotels` |
| 编辑 | 弹窗 `PUT /enterprise/hotels/{id}` |
| 删除 | `DELETE /enterprise/hotels/{id}` |
| 房型配额 | 抽屉 `GET /enterprise/hotels/{id}/room-types` |
| 刷新 | `GET /enterprise/hotels` |

操作列顺序：房型配额 → 编辑 → 删除

酒店字段：名称、地址、联系电话。

### 房型配额（抽屉）

| 操作 | API |
|------|-----|
| 添加房型 | 弹窗 `POST /enterprise/hotels/{hotelId}/room-types` |
| 编辑 | 弹窗 `PUT /enterprise/hotels/{hotelId}/room-types/{id}` |
| 删除 | `DELETE /enterprise/hotels/{hotelId}/room-types/{id}` |
| 刷新 | `GET /enterprise/hotels/{hotelId}/room-types` |

房型字段：名称、协议价、配额。列表展示已用/总量（`used/quota`）。

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/hotels` | 酒店列表 |
| GET | `/enterprise/hotels/{id}` | 单条酒店 |
| POST | `/enterprise/hotels` | 新建 `HotelInfoPayload` |
| PUT | `/enterprise/hotels/{id}` | 编辑 |
| DELETE | `/enterprise/hotels/{id}` | 删除 |
| GET | `/enterprise/hotels/{hotelId}/room-types` | 房型列表 |
| POST | `/enterprise/hotels/{hotelId}/room-types` | 新建 `HotelRoomTypePayload` |
| PUT | `/enterprise/hotels/{hotelId}/room-types/{id}` | 编辑 |
| DELETE | `/enterprise/hotels/{hotelId}/room-types/{id}` | 删除 |

### 参会端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/portal/hotels/{eventId}` | 可订房型列表（含剩余配额） |
| POST | `/portal/hotels/{eventId}/book` | 预订，body: `{ roomTypeId, nights? }`，自动创建支付订单 |

活动须 `hotelEnabled=true` 且已发布。

### HotelInfo

```ts
{
  id: number
  name: string
  address: string
  contactPhone: string
}
```

### HotelInfoPayload

```ts
{
  name: string
  address: string
  contactPhone: string
}
```

### HotelRoomType

```ts
{
  id: number
  hotelId: number
  name: string
  price: number
  quota: number
  used?: number
}
```

### HotelRoomTypePayload

```ts
{
  name: string
  price: number
  quota: number
}
```

---

## Demo 数据

| 酒店 | 房型 |
|------|------|
| 上海国际会议中心酒店 | 标准大床房 680/50、豪华双床房 880/30 |
| 世博洲际酒店 | 商务标间 520/40 |

---

## 后端实现

| 后端 | 说明 |
|------|------|
| EnterpriseHotelController | 酒店 + 房型 CRUD |
| PortalHotelController | 参会端房型列表、预订 |
| HotelService | 配额同步、订房创建 pay_order |
| `hotel_info` / `hotel_room_type` / `hotel_quota` | 按 `tenant_id` 隔离 |
