package com.example.commerce.order

data class OrderRequest(
    val orderId: Long,        // 주문 id
)

data class OrderCreateRequest(
    val paymentMethod: String,       // 결제수단
    val orderStatus: String,         // 주문상태 (1: 완료, 2:취소)
    val orderItems: MutableList<OrderItemRequest>,  // 주문 item 목록
    val orderAddress: OrderAddressRequest,   // 배송지
)

fun OrderCreateRequest.validate() =
    !((this.paymentMethod.equals("")) || (this.orderStatus.equals("")))


data class OrderItemRequest(
    val itemId: Int,        // book id
    val quantity: Int,  // 수량
    val orderPrice: Int,// 주문금액
)

fun OrderItemRequest.validate() =
    !((this.itemId == 0) || (quantity == 0))

data class OrderItemResponse(
    val itemId: Int,        // book id
    val title: String,
    val cover: String,
    val author: String,
    val priceStandard: Int,
    val priceSales: Int,
    val categoryName: String,
    val quantity: Int,           // book 수량
)

data class OrderAddressRequest(
    val postcode: String,       // 우편번호
    val address: String,        // 기본주소
    val detailAddress: String,  // 상세주소
)