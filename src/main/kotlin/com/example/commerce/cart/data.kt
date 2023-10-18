package com.example.commerce.cart

data class CartRequest(
    val cartId: Long,        // 장바구니 id
)

data class CartCreateRequest(
    val cartId: Long,       // 장바구니 id
    val itemId: Int,        // book id
    val quantity: Int,           // book 수량
)

fun CartCreateRequest.validate() =
    !((this.cartId == 0L) || (this.itemId == 0) || (quantity == 0))

data class CartItemsCreateRequest(
    val cartId: Long,       // 장바구니 id
    val cartItems: MutableList<CartItemResponse>,  // 장바구니 item 목록
)

data class CartResponse(
    val cartId: Long,        // 장바구니 id
    val profileId: Long,     // 사용자 id
    val cartItems: MutableList<CartItemResponse>,  // 장바구니 item 목록
)

data class CartItemRequest(
    val profileId: Long,    // 사용자 id
    val itemId: Int,        // book id
    val quantity: Int,      // book 수량
)

fun CartItemRequest.validate() =
    !((this.itemId == 0) || (quantity == 0))

data class CartItemResponse(
//    val cartId: Long,       // 장바구니 id
    val itemId: Int,        // book id
    val title: String,
    val cover: String,
    val author: String,
    val priceStandard: Int,
    val priceSales: Int,
    val categoryName: String,
    val quantity: Int,           // book 수량
)

